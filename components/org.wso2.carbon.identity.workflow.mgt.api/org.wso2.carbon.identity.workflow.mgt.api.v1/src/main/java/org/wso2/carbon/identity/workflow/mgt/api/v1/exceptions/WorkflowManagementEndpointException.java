/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.workflow.mgt.api.v1.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Custom exception for user organization management endpoint.
 */
public class WorkflowManagementEndpointException extends WebApplicationException {

    public WorkflowManagementEndpointException(Response.Status status, Error error) {

        super(Response.status(status).entity(error).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build());
    }

    public WorkflowManagementEndpointException(Response.Status status) {

        super(Response.status(status).build());
    }
}
