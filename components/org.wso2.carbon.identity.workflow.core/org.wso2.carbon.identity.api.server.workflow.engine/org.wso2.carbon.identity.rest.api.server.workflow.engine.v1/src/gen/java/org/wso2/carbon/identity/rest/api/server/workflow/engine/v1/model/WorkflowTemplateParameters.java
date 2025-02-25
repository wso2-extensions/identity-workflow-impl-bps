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

package org.wso2.carbon.identity.rest.api.server.workflow.engine.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class WorkflowTemplateParameters  {
  
    private String paramName;
    private String paramValue;
    private String qName;

    /**
    **/
    public WorkflowTemplateParameters paramName(String paramName) {

        this.paramName = paramName;
        return this;
    }
    
    @ApiModelProperty(example = "UserAndRole", required = true, value = "")
    @JsonProperty("paramName")
    @Valid
    @NotNull(message = "Property paramName cannot be null.")

    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
    **/
    public WorkflowTemplateParameters paramValue(String paramValue) {

        this.paramValue = paramValue;
        return this;
    }
    
    @ApiModelProperty(example = "Internal/admin", required = true, value = "")
    @JsonProperty("paramValue")
    @Valid
    @NotNull(message = "Property paramValue cannot be null.")

    public String getParamValue() {
        return paramValue;
    }
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
    **/
    public WorkflowTemplateParameters qName(String qName) {

        this.qName = qName;
        return this;
    }
    
    @ApiModelProperty(example = "UserAndRoles-step-1-roles", required = true, value = "")
    @JsonProperty("qName")
    @Valid
    @NotNull(message = "Property qName cannot be null.")

    public String getqName() {
        return qName;
    }
    public void setqName(String qName) {
        this.qName = qName;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkflowTemplateParameters workflowTemplateParameters = (WorkflowTemplateParameters) o;
        return Objects.equals(this.paramName, workflowTemplateParameters.paramName) &&
            Objects.equals(this.paramValue, workflowTemplateParameters.paramValue) &&
            Objects.equals(this.qName, workflowTemplateParameters.qName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramValue, qName);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowTemplateParameters {\n");
        
        sb.append("    paramName: ").append(toIndentedString(paramName)).append("\n");
        sb.append("    paramValue: ").append(toIndentedString(paramValue)).append("\n");
        sb.append("    qName: ").append(toIndentedString(qName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

