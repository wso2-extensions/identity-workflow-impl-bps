<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile" %>
<%@ page import="org.wso2.carbon.identity.workflow.impl.ui.WorkflowImplAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.impl.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>


<%
    WorkflowImplAdminServiceClient client = null;

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    client = new WorkflowImplAdminServiceClient(cookie, backendServerURL, configContext);

    String profileName = request.getParameter(WorkflowUIConstants.PARAM_BPS_PROFILE_NAME);

    BPSProfile bpsProfile = client.getBPSProfiles(profileName);



%>


<fmt:bundle basename="org.wso2.carbon.identity.workflow.impl.ui.i18n.Resources">
    <carbon:breadcrumb
            label="workflow.mgt"
            resourceBundle="org.wso2.carbon.identity.workflow.impl.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript">
        function doCancel(){
            window.location = "list-bps-profiles.jsp";
        }
        function editBPSAuthPassword(){
            var passwordField = document.getElementById("id_<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>");
            if(document.getElementById('chkbox_<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>').checked){
                passwordField.disabled=false;
            }else{
                passwordField.disabled=true;
                passwordField.value="" ;
            }
        }
        function editCarbonAuthPassword(){
            var passwordField = document.getElementById("id_<%=WorkflowUIConstants.PARAM_CARBON_AUTH_PASSWORD%>");
            if(document.getElementById('chkbox_<%=WorkflowUIConstants.PARAM_CARBON_AUTH_PASSWORD%>').checked){
                passwordField.disabled=false;
            }else{
                passwordField.disabled=true;
                passwordField.value="" ;
            }
        }

        function doValidation() {
            // validate input elements of the form
            var form = document.getElementsByName("serviceAdd")[0];
            if (!doValidateForm(form, "<fmt:message key="error.input.validation.error"/>")) {
                return false;
            }

            document.serviceAdd.submit();
        }

    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.bps.profile.add'/></h2>

        <div id="workArea">
            <form method="post" name="serviceAdd" action="update-bps-profile-finish-ajaxprocessor.jsp">
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_ACTION%>"
                       value="<%=WorkflowUIConstants.ACTION_VALUE_UPDATE%>">
                <table class="styledLeft noBorders">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="workflow.bps.profile"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td width="30%"><fmt:message key='workflow.bps.profile.name'/></td>
                        <td><input readonly type="text" name="<%=WorkflowUIConstants.PARAM_BPS_PROFILE_NAME%>"
                                   label="<fmt:message key='workflow.bps.profile.name'/>" maxlength="45"
                                   value="<%=bpsProfile.getProfileName()%>"  style="width:30%" class="text-box-big"/></td>
                    </tr>
                    </tbody>
                </table>
                <table class="styledLeft noBorders" style="margin-top: 10px">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="workflow.bps.profile.connection.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td width="30%"><fmt:message key='workflow.bps.profile.manager.host'/></td>
                        <td>
                            <input type="text" name="<%=WorkflowUIConstants.PARAM_BPS_MANAGER_HOST%>"
                                   value="<%=bpsProfile.getManagerHostURL()%>" maxlength="255"
                                   black-list-patterns="^(\s*)$"
                                   label="<fmt:message key='workflow.bps.profile.manager.host'/>"
                                   style="width:50%" class="text-box-big"/>
                            <div class="sectionHelp">
                                <fmt:message key='help.desc.manager'/>
                            </div>
                        </td></tr>
                    <tr>
                        <td width="30%"><fmt:message key='workflow.bps.profile.worker.host'/></td>
                        <td><input type="text" name="<%=WorkflowUIConstants.PARAM_BPS_WORKER_HOST%>"
                                   value="<%=bpsProfile.getWorkerHostURL()%>" maxlength="255"
                                   black-list-patterns="^(\s*)$"
                                   label="<fmt:message key='workflow.bps.profile.worker.host'/>"
                                   style="width:60%" class="text-box-big"/>
                            <div class="sectionHelp">
                                <fmt:message key='help.desc.worker'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td width="30%"><fmt:message key='workflow.bps.profile.auth.user'/></td>
                        <td><input type="text" name="<%=WorkflowUIConstants.PARAM_BPS_AUTH_USER%>"
                                   value="<%=bpsProfile.getUsername()%>" maxlength="45"
                                   black-list-patterns="^(\s*)$"
                                   label="<fmt:message key='workflow.bps.profile.auth.user'/>"
                                   style="width:60%" class="text-box-big"/></td>
                    </tr>
                    <tr>
                        <td width="30%"><fmt:message key='workflow.bps.profile.auth.password'/></td>
                        <td>
                            <input disabled type="password" id="id_<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>"
                                   name="<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>" placeholder="**********" autocomplete="off"
                                   label="<fmt:message key='workflow.bps.profile.name'/>"
                                   style="width:30%" class="text-box-big"/>
                            <input onclick="editBPSAuthPassword();" type="checkbox" id="chkbox_<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>" />
                            <label class="control-label" for="chkbox_<%=WorkflowUIConstants.PARAM_BPS_AUTH_PASSWORD%>">Edit Password</label>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttonRow" colspan="2" style="margin-top: 10px">
                    <input class="button" value="<fmt:message key="update"/>" type="button" onclick="doValidation()"/>
                    <input class="button" style="margin-left: 10px;" value="<fmt:message key="cancel"/>"
                                   type="button" onclick="doCancel();"/>
                </div>
                <br/>
            </form>
        </div>
    </div>
</fmt:bundle>
