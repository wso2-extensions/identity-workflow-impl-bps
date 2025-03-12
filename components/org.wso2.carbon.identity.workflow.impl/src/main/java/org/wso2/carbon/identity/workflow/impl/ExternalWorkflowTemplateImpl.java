/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.impl.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.impl.internal.WorkflowImplServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.Item;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.MapType;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.identity.workflow.mgt.workflow.TemplateInitializer;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;

import java.util.List;

import static org.wso2.carbon.identity.workflow.impl.WFImplConstant.ParameterName.BPS_PROFILE;

/**
 *  Workflow Template for External workflow mediator
 */
public class ExternalWorkflowTemplateImpl extends AbstractWorkflow {

    public ExternalWorkflowTemplateImpl(Class<? extends TemplateInitializer> templateInitializerClass,
                                        Class<? extends WorkFlowExecutor> workFlowExecutorClass, String metaDataXML) {

        super(templateInitializerClass, workFlowExecutorClass, metaDataXML);
    }

    @Override
    protected InputData getInputData(ParameterMetaData parameterMetaData) throws WorkflowException {

        InputData inputData = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (parameterMetaData != null && parameterMetaData.getName() != null) {
            String parameterName = parameterMetaData.getName();
            if (BPS_PROFILE.equals(parameterName)) {
                List<BPSProfile> bpsProfiles = WorkflowImplServiceDataHolder.getInstance().getWorkflowImplService()
                        .listBPSProfiles(tenantId);
                if (bpsProfiles != null && bpsProfiles.size() > 0) {
                    inputData = new InputData();
                    MapType mapType = new MapType();
                    inputData.setMapType(mapType);
                    Item[] items = new Item[bpsProfiles.size()];
                    for (int i = 0; i < bpsProfiles.size(); i++) {
                        BPSProfile bpsProfile = bpsProfiles.get(i);
                        //filter only workflow mediator profile
                      if( bpsProfile.getUsername()==null || bpsProfile.getUsername()==""){
                          Item item = new Item();
                          item.setKey(bpsProfile.getProfileName());
                          item.setValue(bpsProfile.getProfileName());
                          items[i] = item;
                      }

                    }
                    mapType.setItem(items);
                }
            }
        }
        return inputData;
    }

    /**
     *  No need to deploy the template since the workflow is already deployed in External BPS Engine
     * @param parameterList
     */
    @Override
    public void deploy(List<Parameter> parameterList) {

    }

}
