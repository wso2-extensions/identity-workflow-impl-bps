/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.workflow.mgt.api.v1.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.api.common.WorkflowManagementServiceHolder;
import org.wso2.carbon.identity.workflow.mgt.WorkFlowExecutorManager;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

/**
 * Call internal osgi services to perform user organization management related operations.
 */
public class WorkflowManagementService {

    private static final Log LOG = LogFactory.getLog(WorkflowManagementService.class);

    /**
     * Retrieves the root organization visible to the user in the organization hierarchy.
     *
     * @return The root organization of the authenticated user.
     */
    public String approveWorkflowRequest(String requestId) throws WorkflowException {

        getWorkFlowExecutorManager().handleCallback(requestId, "APPROVED", null);
        return null;
    }

    private WorkFlowExecutorManager getWorkFlowExecutorManager() {

        return WorkflowManagementServiceHolder.getWorkFlowExecutorManager();
    }
}
