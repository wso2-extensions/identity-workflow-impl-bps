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

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMAttributeImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.impl.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.impl.internal.WorkflowImplServiceDataHolder;
import org.wso2.carbon.identity.workflow.impl.util.WorkflowRequestBuilder;

import org.wso2.carbon.identity.workflow.impl.util.model.WorkflowVariable;
import org.wso2.carbon.identity.workflow.impl.util.model.WorkFlowMediatorRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowManagementUtil;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.MEDIATOR_API_KEY;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.NAME_PARAMETER_ATTRIBUTE;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.PROCESS_UUID;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.REQUEST_ID;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.TASK_INITIATOR;
import static org.wso2.carbon.identity.workflow.impl.constant.WorkflowConstant.WORKFLOW_PARAMETERS;

import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class WorkflowMediatorRequestExecutor implements WorkFlowExecutor {

    private static final Log log = LogFactory.getLog(WorkflowMediatorRequestExecutor.class);
    private static final String EXECUTOR_NAME = "Workflow Mediator Request Executor";

    private static final Gson gson = new Gson();

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

        validateExecutionParams();
        OMElement requestBody = WorkflowRequestBuilder.buildXMLRequest(workFlowRequest, this.parameterList);

        try {
            WorkFlowMediatorRequest workFlowMediatorRequest = createWorkflowRequest(requestBody);
            callMediator(gson.toJson(workFlowMediatorRequest));
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private String getExternalWorkflowId(String workflowId) throws WorkflowException {

        return WorkflowImplServiceDataHolder.getInstance().getWorkflowManagementService().
                getExternalWorkflowId(workflowId);

    }

    private WorkFlowMediatorRequest createWorkflowRequest(OMElement messagePayload) throws WorkflowException {

        WorkFlowMediatorRequest workFlowMediatorRequest = new WorkFlowMediatorRequest();
        workFlowMediatorRequest.setWorkflow_iD(getExternalWorkflowId(this.parameterList.get(0).getWorkflowId()));
        List<WorkflowVariable> parameterArray = new ArrayList<>();

        Iterator workflowDetails = messagePayload.getChildElements();

        while (workflowDetails.hasNext()) {
            OMElementImpl workflowDetail = (OMElementImpl) workflowDetails.next();
            switch (workflowDetail.getLocalName()) {
                case PROCESS_UUID:
                    workFlowMediatorRequest.setRequest_id(workflowDetail.getText());
                case TASK_INITIATOR:
                    WorkflowVariable workflowVariable = new WorkflowVariable(TASK_INITIATOR, workflowDetail.getText());
                    parameterArray.add(workflowVariable);
                case WORKFLOW_PARAMETERS:
                    String parameterName = null;
                    Iterator parameters = workflowDetail.getChildElements();

                    while (parameters.hasNext()) {
                        OMElementImpl parameter = (OMElementImpl) parameters.next();
                        Iterator parameterValues = parameter.getChildElements();
                        Iterator parameterAttributes = parameter.getAllAttributes();

                        while (parameters.hasNext()) {
                            OMAttributeImpl parameterAttribute = (OMAttributeImpl) parameterAttributes.next();
                            if (NAME_PARAMETER_ATTRIBUTE.equals(parameterAttribute.getLocalName())) {
                                parameterName = parameterAttribute.getAttributeValue();
                                break;
                            }
                        }
                        while (parameterValues.hasNext()) {
                            OMElementImpl parameterValue = (OMElementImpl) parameterValues.next();
                            Iterator parameterItemValues = parameterValue.getChildElements();
                            while (parameterItemValues.hasNext()) {
                                OMElementImpl parameterItemValue = (OMElementImpl) parameterItemValues.next();
                                if (!REQUEST_ID.equals(parameterName)) {
                                    workflowVariable = new WorkflowVariable(parameterName, parameterItemValue.getText());
                                    parameterArray.add(workflowVariable);
                                }
                            }
                        }
                    }
            }
        }
        workFlowMediatorRequest.setWorkflow_variables(parameterArray);

        return workFlowMediatorRequest;
    }

    private void callMediator(String workflowRequest) throws IOException {

        String workflowMediatorURL = bpsProfile.getWorkerHostURL();
        URL url = new URL(workflowMediatorURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod(POST);
        con.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        con.setRequestProperty(MEDIATOR_API_KEY, bpsProfile.getApiKey());
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = workflowRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        con.getResponseCode();
    }

}
