
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

package org.wso2.carbon.identity.workflow.impl.util.model;

import java.util.List;

/**
 * Define the structure the of the common payload of workflow request
 */
public class WorkFlowMediatorRequest {
    private String request_id;
    private String workflow_iD;
    private List<WorkflowVariable> workflow_Workflow_variables;




    public WorkFlowMediatorRequest() {

    }
    public WorkFlowMediatorRequest(String processDefinitionId, String workflow_iD, List<WorkflowVariable> workflow_Workflow_variables) {

        this.request_id = processDefinitionId;
        this.workflow_Workflow_variables = workflow_Workflow_variables;
        this.workflow_iD = workflow_iD;
    }

    public String getRequest_id() {

        return request_id;
    }

    public void setRequest_id(String processDefinitionId) {

        this.request_id = processDefinitionId;
    }

    public List<WorkflowVariable> getWorkflow_variables() {
        return workflow_Workflow_variables;
    }

    public void setWorkflow_variables(List<WorkflowVariable> workflow_Workflow_variables) {

        this.workflow_Workflow_variables = workflow_Workflow_variables;
    }

    public void setWorkflow_iD(String workflowName) {

        this.workflow_iD = workflowName;
    }
}
