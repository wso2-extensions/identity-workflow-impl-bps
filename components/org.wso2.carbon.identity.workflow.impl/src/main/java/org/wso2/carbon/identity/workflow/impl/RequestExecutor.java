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

package org.wso2.carbon.identity.workflow.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.IdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.NotificationSender;
import org.wso2.carbon.identity.mgt.NotificationSendingModule;
import org.wso2.carbon.identity.mgt.config.Config;
import org.wso2.carbon.identity.mgt.config.ConfigBuilder;
import org.wso2.carbon.identity.mgt.config.ConfigType;
import org.wso2.carbon.identity.mgt.config.StorageType;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.mail.DefaultEmailSendingModule;
import org.wso2.carbon.identity.mgt.mail.Notification;
import org.wso2.carbon.identity.mgt.mail.NotificationBuilder;
import org.wso2.carbon.identity.mgt.mail.NotificationData;
import org.wso2.carbon.identity.workflow.impl.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.impl.internal.WorkflowImplServiceDataHolder;
import org.wso2.carbon.identity.workflow.impl.util.WorkflowRequestBuilder;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.DOMAIN_NAME;

public class RequestExecutor implements WorkFlowExecutor {

    private static final Log log = LogFactory.getLog(RequestExecutor.class);
    private static final String EXECUTOR_NAME = "BPELExecutor";
    private static final String DEFAULT_MANAGER_CLAIM = "http://wso2.org/claims/managers";
    private static final String MANAGER_RDN_CLAIM = "http://wso2.org/claims/managerRDN";


    private static final String WORK_FLOW_MANAGER_CLAIM = "WorkFlowManagerClaimConfig";
    public static final String AXIS2 = "axis2.xml";
    public static final String AXIS2_FILE = "repository/conf/axis2/axis2.xml";
    public static final String TRANSPORT_MAILTO = "mailto";

    private List<Parameter> parameterList;
    private BPSProfile bpsProfile;

    @Override
    public boolean canHandle(WorkflowRequest workFlowRequest) {
        //Since this is handled by manager level
        return true;
    }

    @Override
    public void initialize(List<Parameter> parameterList) throws WorkflowException {

        this.parameterList = parameterList;

        Parameter bpsProfileParameter = WorkflowManagementUtil
                .getParameter(parameterList, WFImplConstant.ParameterName.BPS_PROFILE, WFConstant.ParameterHolder
                        .WORKFLOW_IMPL);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (bpsProfileParameter != null) {
            String bpsProfileName = bpsProfileParameter.getParamValue();
            try {
                bpsProfile = WorkflowImplServiceDataHolder.getInstance().getWorkflowImplService().getBPSProfile
                        (bpsProfileName, tenantId);
            } catch (WorkflowImplException e) {
                String errorMsg = "Error occurred while reading bps profile, " + e.getMessage();
                log.error(errorMsg);
                throw new WorkflowException(errorMsg, e);
            }
        }
    }

    @Override
    public void execute(WorkflowRequest workFlowRequest) throws WorkflowException {

        Map<String, String> notificationData = new HashMap<>();
        validateExecutionParams();
        prepareForExecute(workFlowRequest, notificationData);
        OMElement requestBody = WorkflowRequestBuilder.buildXMLRequest(workFlowRequest, this.parameterList);
        try {
            callService(requestBody);
            triggerWorkFlowNotification(notificationData);
        } catch (AxisFault axisFault) {
            throw new InternalWorkflowException("Error invoking service for request: " +
                                                workFlowRequest.getUuid(), axisFault);
        }
    }

    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }


    private void validateExecutionParams() throws InternalWorkflowException {
        //Since missing a parameter should lead to a exception scenario here, throwing a exception with what is
        // missing and returning void for successful cases.
        if (parameterList == null) {
            throw new InternalWorkflowException("Init params for the BPELExecutor is null.");
        }
    }

    private void callService(OMElement messagePayload) throws AxisFault {

        ServiceClient client = new ServiceClient(WorkflowImplServiceDataHolder.getInstance()
                                                         .getConfigurationContextService()
                                                         .getClientConfigContext(), null);
        Options options = new Options();
        options.setAction(WFImplConstant.DEFAULT_APPROVAL_BPEL_SOAP_ACTION);

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        String host = bpsProfile.getWorkerHostURL();
        String serviceName = StringUtils.deleteWhitespace(WorkflowManagementUtil
                                                                  .getParameter(parameterList, WFConstant
                                                                          .ParameterName.WORKFLOW_NAME, WFConstant
                                                                          .ParameterHolder.WORKFLOW_IMPL)
                                                                  .getParamValue());
        serviceName = StringUtils.deleteWhitespace(serviceName);

        if (host.endsWith("/")) {
            host = host.substring(0,host.lastIndexOf("/"));
        }

        String endpoint;
        if (tenantDomain != null && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            endpoint = host + "/t/" + tenantDomain + "/" + serviceName + WFConstant.TemplateConstants.SERVICE_SUFFIX;
        } else {
            endpoint = host + "/" + serviceName + WFConstant.TemplateConstants.SERVICE_SUFFIX;
        }

        options.setTo(new EndpointReference(endpoint));

        options.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, SOAP11Constants
                .SOAP_11_CONTENT_TYPE);

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(bpsProfile.getUsername());
        auth.setPassword(String.valueOf(bpsProfile.getPassword()));
        auth.setPreemptiveAuthentication(true);
        List<String> authSchemes = new ArrayList<>();
        authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
        auth.setAuthSchemes(authSchemes);
        options.setProperty(HTTPConstants.AUTHENTICATE, auth);

        options.setManageSession(true);

        client.setOptions(options);
        client.fireAndForget(messagePayload);

    }

    private void prepareForExecute(WorkflowRequest workFlowRequest, Map<String, String> notificationData)
            throws WorkflowException {

        String initiator = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        RealmService realmService = WorkflowImplServiceDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        String manager = null;
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error(e);
            throw new WorkflowException("User Store Error occurred.", e);
        }
        List<RequestParameter> requestParameters = workFlowRequest.getRequestParameters();
        for (RequestParameter requestParameter : requestParameters) {
            if (userStoreManager != null && "Username".equalsIgnoreCase(requestParameter.getName())) {
                try {
                    if (requestParameter.getValue() != null) {
                        String managerDn = userStoreManager.getUserClaimValue(initiator,
                                getWorkFlowManagerClaimConfig(), null);
                        manager = getManger(managerDn, initiator, tenantId);
                        notificationData.put("managers", manager);
                        notificationData.put("target", String.valueOf(requestParameter.getValue()));
                    }
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new WorkflowException("User Store error occurred.", e);
                }
            }
        }
        boolean switchApprovalManagerDone = false;
        if (StringUtils.isNotBlank(manager)) {
            for (Parameter parameter : this.parameterList) {
                if (!switchApprovalManagerDone &&
                        parameter.getParamName().equals(WFImplConstant.ParameterName.STEPS_USER_AND_ROLE)) {
                    String[] key = parameter.getqName().split("-");
                    String step = key[2];
                    String value = parameter.getParamValue();
                    if (StringUtils.isNotBlank(value)) {
                        String stepName = WFImplConstant.ParameterName.STEPS_USER_AND_ROLE + "-step-" + step + "-users";
                        if (stepName.equals(parameter.getqName())) {
                            parameter.setParamValue(manager);
                            switchApprovalManagerDone = true;
                        }
                    }
                } else if (switchApprovalManagerDone) {
                    String qName = parameter.getqName();
                    if (StringUtils.isNotBlank(qName) &&
                            qName.contains(WFImplConstant.ParameterName.STEPS_USER_AND_ROLE)) {
                        this.parameterList.remove(parameter);
                    }
                }
            }
        }
    }

    private String getWorkFlowManagerClaimConfig() {

        String workFlowManagerClaim = IdentityUtil.getProperty(
                WORK_FLOW_MANAGER_CLAIM);
        if (StringUtils.isBlank(workFlowManagerClaim)) {
            workFlowManagerClaim =
                    DEFAULT_MANAGER_CLAIM;
        }
        return workFlowManagerClaim;
    }

   /**
     * Trigger event after initiating the workflow.
     */
    private void triggerWorkFlowNotification(Map<String, String> notificationInfo)
            throws WorkflowException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String[] managers = StringUtils.split(notificationInfo.get("managers"), ',');
        String target = notificationInfo.get("target");
        RealmService realmService = WorkflowImplServiceDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        UserRealm userRealm;
        try {
            userRealm = realmService.getTenantUserRealm(tenantId);
            userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
        } catch (Exception e) {
            log.error(e);
            throw new WorkflowException("User Store error occurred.", e);
        }
        for (String manager : managers) {
            String email = null;
            if (userStoreManager != null && StringUtils.isNotBlank(manager)) {
                try {
                    if (userStoreManager.isExistingUser(manager)) {
                        email = userStoreManager.getUserClaimValue(manager, "http://wso2.org/claims/emailaddress", null);
                    }
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error(e);
                    throw new WorkflowException("User Store error occurred.", e);
                }
            }
            try {
                System.setProperty(AXIS2, AXIS2_FILE);
                ConfigurationContext configurationContext =
                        ConfigurationContextFactory
                                .createConfigurationContextFromFileSystem(null, null);
                if (configurationContext.getAxisConfiguration().getTransportsOut()
                        .containsKey(TRANSPORT_MAILTO)) {
                    if (!StringUtils.isNotBlank(email)) {
                        log.error("Notification will not be triggered as the manager does not have configured the " +
                                "email" +
                                " address in his/her profile.");
                    }
                    NotificationSender notificationSender = new NotificationSender();
                    NotificationDataDTO notificationData = new NotificationDataDTO();
                    Notification emailNotification = null;
                    NotificationData emailNotificationData = new NotificationData();
                    ConfigBuilder configBuilder = ConfigBuilder.getInstance();
                    String emailTemplate;
                    Config config;
                    try {
                        config = configBuilder.loadConfiguration(ConfigType.EMAIL, StorageType.REGISTRY, tenantId);
                    } catch (IdentityMgtConfigException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error occurred while loading email templates for user : " + target, e);
                        }
                        throw new WorkflowException("Error occurred while loading email templates for user : "
                                + manager, e);
                    }
                    emailNotificationData.setTagData("Manager", manager);
                    emailNotificationData.setTagData("Target", target);
                    emailNotificationData.setSendTo(email);
                    emailTemplate = config.getProperty("WorkFlowApprovalNotification");
                    try {
                        emailNotification = NotificationBuilder.createNotification("EMAIL", emailTemplate,
                                emailNotificationData);
                    } catch (IdentityMgtServiceException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error occurred while creating notification from email template : "
                                    + emailTemplate, e);
                        }
                        throw new WorkflowException("Error occurred while creating notification from " +
                                "email template : " + emailTemplate, e);
                    }
                    notificationData.setNotificationAddress(email);
                    NotificationSendingModule module = new DefaultEmailSendingModule();
                    module.setNotificationData(notificationData);
                    module.setNotification(emailNotification);
                    notificationSender.sendNotification(module);
                    notificationData.setNotificationSent(true);
                } else {
                    throw new WorkflowException("MAILTO transport sender is not defined in axis2 " +
                            "configuration file");
                }
            } catch (AxisFault axisFault) {
                throw new WorkflowException("Error while getting the SMTP configuration");
            }
        }
    }

    private String getManger(String managerDn, String initiator, int tenantId) throws UserStoreException {

        RealmConfiguration realmConfig = getInitiatorRealmConfig(initiator, tenantId);
        LdapName ln;
        try {
            ln = new LdapName(managerDn);
        } catch (InvalidNameException e) {
            throw new UserStoreException(e);
        }
        int lastIndex = (ln.getRdns()).size() - 1;
        String managerRdnValue = ln.getRdn(lastIndex).getValue().toString();

        RealmService realmService = WorkflowImplServiceDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager = realmService.getUserRealm(realmConfig).getUserStoreManager();
        String[] managerCandidates = userStoreManager.getUserList(MANAGER_RDN_CLAIM, managerRdnValue, null);
        if (managerCandidates != null && managerCandidates.length > 0) {
            return managerCandidates[0];
        } else {
            throw new UserStoreException();
        }
    }

    private RealmConfiguration getInitiatorRealmConfig(String initiator, int tenantId) throws UserStoreException {

        RealmConfiguration realmConfig;
        List<RealmConfiguration> realmConfigurations = new ArrayList<>();
        // Add PRIMARY user store
        realmConfig = WorkflowImplServiceDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                .getRealmConfiguration();
        realmConfigurations.add(realmConfig);
        do {
            // Check for the tenant's secondary user stores
            realmConfig = realmConfig.getSecondaryRealmConfig();
            if (realmConfig != null) {
                realmConfigurations.add(realmConfig);
            }
        } while (realmConfig != null);

        String userStoreDomain = UserCoreUtil.extractDomainFromName(initiator);
        for (RealmConfiguration entry : realmConfigurations) {
            if (entry.getUserStoreProperties().get(DOMAIN_NAME).equalsIgnoreCase(userStoreDomain)) {
                return entry;
            }
        }
        return null;
    }
}
