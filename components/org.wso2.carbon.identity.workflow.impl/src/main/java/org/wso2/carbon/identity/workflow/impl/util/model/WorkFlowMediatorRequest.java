
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
    private String requestId;
    private String workflowID;
    private List<Variable> variables;




    public WorkFlowMediatorRequest() {

    }
    public WorkFlowMediatorRequest(String processDefinitionId, String workflowID, List<Variable> variables) {
        this.requestId = processDefinitionId;
        this.variables = variables;
        this.workflowID=workflowID;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String processDefinitionId) {
        this.requestId = processDefinitionId;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void setWorkflowID(String workflowName) {

        this.workflowID = workflowName;
    }
}
