/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.workflow.impl.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.impl.ApprovalWorkflow;
import org.wso2.carbon.identity.workflow.impl.BPELDeployer;
import org.wso2.carbon.identity.workflow.impl.RequestExecutor;
import org.wso2.carbon.identity.workflow.impl.WFImplConstant;
import org.wso2.carbon.identity.workflow.impl.WorkflowImplService;
import org.wso2.carbon.identity.workflow.impl.WorkflowImplServiceImpl;
import org.wso2.carbon.identity.workflow.impl.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.impl.listener.WorkflowImplAuditLogger;
import org.wso2.carbon.identity.workflow.impl.listener.WorkflowImplServiceListener;
import org.wso2.carbon.identity.workflow.impl.listener.WorkflowImplTenantMgtListener;
import org.wso2.carbon.identity.workflow.impl.listener.WorkflowImplValidationListener;
import org.wso2.carbon.identity.workflow.impl.listener.WorkflowListenerImpl;
import org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.identity.workflow.impl", 
         immediate = true)
public class WorkflowImplServiceComponent {

    private static final Log log = LogFactory.getLog(WorkflowImplServiceComponent.class);
    private static final String INTERNAL_DOMAIN_DEFAULT = "localhost";

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        try {
            String metaDataXML = readWorkflowImplParamMetaDataXML(WFImplConstant.WORKFLOW_IMPL_PARAMETER_METADATA_FILE_NAME);
            bundleContext.registerService(AbstractWorkflow.class, new ApprovalWorkflow(BPELDeployer.class, RequestExecutor.class, metaDataXML), null);
            bundleContext.registerService(WorkflowListener.class, new WorkflowListenerImpl(), null);
            bundleContext.registerService(WorkflowImplServiceListener.class, new WorkflowImplAuditLogger(), null);
            bundleContext.registerService(WorkflowImplServiceListener.class, new WorkflowImplValidationListener(), null);
            WorkflowImplServiceDataHolder.getInstance().setWorkflowImplService(new WorkflowImplServiceImpl());
            WorkflowImplTenantMgtListener workflowTenantMgtListener = new WorkflowImplTenantMgtListener();
            ServiceRegistration tenantMgtListenerSR = bundleContext.registerService(TenantMgtListener.class.getName(), workflowTenantMgtListener, null);
            if (tenantMgtListenerSR != null) {
                log.debug("Workflow Management - WorkflowTenantMgtListener registered");
            } else {
                log.error("Workflow Management - WorkflowTenantMgtListener could not be registered");
            }
            this.addDefaultBPSProfile();
        } catch (Throwable e) {
            log.error("Error occurred while activating WorkflowImplServiceComponent bundle", e);
        }
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.mgt.workflowservice", 
             service = org.wso2.carbon.identity.workflow.mgt.WorkflowManagementService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowManagementService")
    protected void setWorkflowManagementService(WorkflowManagementService workflowManagementService) {
        WorkflowImplServiceDataHolder.getInstance().setWorkflowManagementService(workflowManagementService);
    }

    protected void unsetWorkflowManagementService(WorkflowManagementService workflowManagementService) {
        WorkflowImplServiceDataHolder.getInstance().setWorkflowManagementService(null);
    }

    @Reference(
             name = "org.wso2.carbon.user.core.service.realmservice", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        WorkflowImplServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        WorkflowImplServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        WorkflowImplServiceDataHolder.getInstance().setConfigurationContextService(null);
    }

    @Reference(
             name = "org.wso2.carbon.utils.contextservice", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        WorkflowImplServiceDataHolder.getInstance().setConfigurationContextService(contextService);
    }

    private void addDefaultBPSProfile() throws URLBuilderException {
        try {
            WorkflowImplService workflowImplService = WorkflowImplServiceDataHolder.getInstance().getWorkflowImplService();
            BPSProfile currentBpsProfile = workflowImplService.getBPSProfile(WFImplConstant.DEFAULT_BPS_PROFILE_NAME, MultitenantConstants.SUPER_TENANT_ID);
            String url = buildDefaultBPSServerUrl();
            String userName = WorkflowImplServiceDataHolder.getInstance().getRealmService().getBootstrapRealmConfiguration().getAdminUserName();
            String password = WorkflowImplServiceDataHolder.getInstance().getRealmService()
                    .getBootstrapRealmConfiguration().getAdminPassword();
            if (currentBpsProfile == null || !currentBpsProfile.getWorkerHostURL().equals(url) || !currentBpsProfile.getUsername().equals(userName)) {
                BPSProfile bpsProfileDTO = new BPSProfile();
                bpsProfileDTO.setManagerHostURL(url);
                bpsProfileDTO.setWorkerHostURL(url);
                bpsProfileDTO.setUsername(userName);
                bpsProfileDTO.setPassword(password.toCharArray());
                bpsProfileDTO.setProfileName(WFImplConstant.DEFAULT_BPS_PROFILE_NAME);
                if (currentBpsProfile == null) {
                    workflowImplService.addBPSProfile(bpsProfileDTO, MultitenantConstants.SUPER_TENANT_ID);
                    log.info("Default BPS profile added to the DB.");
                } else {
                    workflowImplService.updateBPSProfile(bpsProfileDTO, MultitenantConstants.SUPER_TENANT_ID);
                    log.info("Default BPS profile updated.");
                }
            }
        } catch (WorkflowException e) {
            // This is not thrown exception because this is not blocked to the other functionality. User can create
            // default profile by manually.
            log.error("Error occured while adding default bps profile.", e);
        }
    }

    private String readWorkflowImplParamMetaDataXML(String fileName) throws WorkflowRuntimeException {
        String content = null;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
            content = WorkflowManagementUtil.readFileFromResource(resourceAsStream);
        } catch (IOException e) {
            String errorMsg = "Error occurred while reading file from class path, " + e.getMessage();
            log.error(errorMsg);
            throw new WorkflowRuntimeException(errorMsg, e);
        } catch (URISyntaxException e) {
            String errorMsg = "Error occurred while reading file from class path, " + e.getMessage();
            log.error(errorMsg);
            throw new WorkflowRuntimeException(errorMsg, e);
        } finally {
            IdentityIOStreamUtils.closeInputStream(resourceAsStream);
        }
        return content;
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
             name = "identityCoreInitializedEventService", 
             service = org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetIdentityCoreInitializedEventService")
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
             name = "org.wso2.carbon.identity.workflow.impl.listener.workflowimplservicelistner", 
             service = org.wso2.carbon.identity.workflow.impl.listener.WorkflowImplServiceListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetWorkflowImplServiceListener")
    protected void setWorkflowImplServiceListener(WorkflowImplServiceListener workflowListener) {
        WorkflowImplServiceDataHolder.getInstance().getWorkflowListenerList().add(workflowListener);
    }

    protected void unsetWorkflowImplServiceListener(WorkflowImplServiceListener workflowListener) {
        WorkflowImplServiceDataHolder.getInstance().getWorkflowListenerList().remove(workflowListener);
    }

    @Reference(
            name = "server.configuration.service",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Set the ServerConfiguration Service");
        }
        WorkflowImplServiceDataHolder.getInstance().setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset the ServerConfiguration Service");
        }
        WorkflowImplServiceDataHolder.getInstance().setServerConfigurationService(null);
    }

    private String buildDefaultBPSServerUrl() {

        String internalHostName = IdentityUtil.getProperty(IdentityCoreConstants.SERVER_HOST_NAME);
        if (StringUtils.isBlank(internalHostName)) {
            if (log.isDebugEnabled()) {
                log.debug("'ServerHostName' property not configured in identity.xml.");
            }
            return INTERNAL_DOMAIN_DEFAULT;
        }

        internalHostName = internalHostName.trim();
        if (internalHostName.endsWith("/")) {
            internalHostName = internalHostName.substring(0, internalHostName.length() - 1);
        }

        String mgtTransport = CarbonUtils.getManagementTransport();
        AxisConfiguration axisConfiguration = WorkflowImplServiceDataHolder.getInstance().getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        int transportPort = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        StringBuilder serverUrl = (new StringBuilder(mgtTransport)).append("://").append(internalHostName.toLowerCase());
        if (transportPort != 443) {
            serverUrl.append(":").append(transportPort);
        }

        if (serverUrl.toString().endsWith("/")) {
            serverUrl.setLength(serverUrl.length() - 1);
        }
        serverUrl.append("/services");
        return serverUrl.toString();
    }
}

