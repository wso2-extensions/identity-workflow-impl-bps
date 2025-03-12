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

package org.wso2.carbon.identity.workflow.impl.constant;

/**
 * Define the workflow constants related to the workflow request send to external workflow mediator
 */
public class WorkflowConstant {

    public static final String PROCESS_UUID = "uuid";
    public static final String WORKFLOW_PARAMETERS = "parameters";
    public static final String WORKFLOW_CONFIGURATION = "configuration";
    public static final String NAME_PARAMETER_ATTRIBUTE = "name";
    public static final String TASK_INITIATOR = "taskInitiator";
    public static final String EVENT_TYPE = "eventType";

    public static final String REQUEST_ID = "REQUEST ID";
    public static final String  MEDIATOR_API_KEY = "API-Key";
    public static final String DESCRIPTION = "The operation should be approved by an authorized person with given " +
            "role, to complete.";

    public static final String APPROVAL_TEMPLATE_NAME = "External Workflow Template";

    public static final String TEMPLATE_ID = "ExternalWorkflowTemplate";

    public static final String BPS_PROFILE = "BPSProfile";

    public static final String HT_SUBJECT = "HTSubject";

    public static final String TEMPLATE_PARAMETER_METADATA_FILE_NAME = "ExternalWorkflowTemplateMetaData.xml";

    public static final String WORKFLOW_IMPL_PARAMETER_METADATA_FILE_NAME = "ExternalWorkflowImplMetaData.xml";

}
