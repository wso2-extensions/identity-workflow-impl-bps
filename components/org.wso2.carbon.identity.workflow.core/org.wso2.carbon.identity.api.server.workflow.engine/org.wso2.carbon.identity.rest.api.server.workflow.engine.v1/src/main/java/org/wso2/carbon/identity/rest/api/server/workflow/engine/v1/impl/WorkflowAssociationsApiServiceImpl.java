/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rest.api.server.workflow.engine.v1.impl;

import org.wso2.carbon.identity.rest.api.server.workflow.engine.v1.*;
import org.wso2.carbon.identity.rest.api.server.workflow.engine.v1.core.WorkflowService;
import org.wso2.carbon.identity.rest.api.server.workflow.engine.v1.model.*;
import java.util.List;

import javax.ws.rs.core.Response;

public class WorkflowAssociationsApiServiceImpl implements WorkflowAssociationsApiService {

    private WorkflowService workflowService;

    public WorkflowAssociationsApiServiceImpl(){
        this.workflowService = new WorkflowService();
    }

    @Override
    public Response createWorkflowAssociation(WorkflowAssociationCreation requestBody) {

        // do some magic!
        return Response.ok().entity(workflowService.addAssociation(requestBody)).build();
    }

    @Override
    public Response deleteWorkflowAssociationById(Integer associationId) {

        // do some magic!
        return Response.ok().entity(workflowService.removeAssociation(associationId)).build();
    }

    @Override
    public Response listWorkflowAssociations(Integer limit, Integer offset, String filter) {

        // do some magic!
        return Response.ok().entity(workflowService.listPaginatedAssociations(limit, offset, filter)).build();
    }

    @Override
    public Response updateWorkflowAssociationStatus(Integer associationId, Status status) {

        // do some magic!
        return Response.ok().entity(workflowService.changeAssociationState(associationId, status)).build();
    }
}
