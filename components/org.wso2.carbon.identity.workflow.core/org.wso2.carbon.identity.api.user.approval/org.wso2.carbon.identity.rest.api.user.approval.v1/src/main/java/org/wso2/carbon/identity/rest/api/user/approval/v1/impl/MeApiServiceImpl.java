/*
 * Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.rest.api.user.approval.v1.impl;

import org.wso2.carbon.identity.rest.api.user.approval.v1.MeApiService;
import org.wso2.carbon.identity.workflow.engine.ApprovalEventService;
import org.wso2.carbon.identity.rest.api.user.approval.v1.dto.StateDTO;

import java.util.List;
import javax.ws.rs.core.Response;

/**
 * API service implementation of a logged in user's approval operations
 */
public class MeApiServiceImpl extends MeApiService {

    ApprovalEventService approvalEventService;
     public MeApiServiceImpl(){
         this.approvalEventService = new ApprovalEventService();
     }

    @Override
    public Response getApprovalTaskInfo(String taskId) {

        return Response.ok().entity(approvalEventService.getTaskData(taskId)).build();
    }

    @Override
    public Response listApprovalTasksForLoggedInUser(Integer limit, Integer offset, List<String> status) {

        return Response.ok().entity(approvalEventService.listTasks(limit, offset, status)).build();
    }

    @Override
    public Response updateStateOfTask(String taskId, StateDTO nextState) {

        org.wso2.carbon.identity.workflow.engine.dto.StateDTO nextStateDTO = new org.wso2.carbon.identity.workflow.engine.dto.StateDTO();
        if (nextState.getAction() == StateDTO.ActionEnum.APPROVE){
            nextStateDTO.setAction(org.wso2.carbon.identity.workflow.engine.dto.StateDTO.ActionEnum.APPROVE);
            approvalEventService.updateStatus(taskId, nextStateDTO);
        } else if (nextState.getAction() == StateDTO.ActionEnum.REJECT){
            nextStateDTO.setAction(org.wso2.carbon.identity.workflow.engine.dto.StateDTO.ActionEnum.REJECT);
            approvalEventService.updateStatus(taskId, nextStateDTO);
        } else if (nextState.getAction() == StateDTO.ActionEnum.RELEASE){
            nextStateDTO.setAction(org.wso2.carbon.identity.workflow.engine.dto.StateDTO.ActionEnum.RELEASE);
            approvalEventService.updateStatus(taskId, nextStateDTO);
        } else if (nextState.getAction() == StateDTO.ActionEnum.CLAIM){
            nextStateDTO.setAction(org.wso2.carbon.identity.workflow.engine.dto.StateDTO.ActionEnum.CLAIM);
            approvalEventService.updateStatus(taskId, nextStateDTO);
        }

        return Response.ok().build();
    }
}
