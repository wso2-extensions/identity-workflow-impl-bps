# Configuring the Workflow connector

To use workflow with WSO2 Identity Server 7 onwards, you need to follow below steps. 

# Instructions to enable workflow

1. Unzip the zip file.
2. If the system is in a production environment, execute the identity workflow-related database scripts on the production Identity Server database.  Follow [this](https://is.docs.wso2.com/en/next/deploy/configure/databases/carbon-database/)  guide to configure a production database. In case you are not utilizing a production database and are instead using the H2 database, run the identity workflow-related database scripts against the identity database located at <IS_HOME>/repository/database/IDENTITY_DB (WSO2IDENTITY_DB.mv.db).
3. Copy bps-datasources.xml to  <IS_HOME>/repository/conf/datasources.
4. Copy the provided file (jpadb.mv.db) or run bps-related scripts against the database in <IS_HOME>/repository/database/JPA_DB(jpadb.mv.db). If you need to change to a different db follow this [guide](https://is.docs.wso2.com/en/6.1.0/deploy/change-datasource-bpsds/) and copy bps-datasources.xml.j2 to <IS_HOME>/repository/resources/conf/templates/repository/conf/datasources/bps-datasources.xml.j2
5. Copy 4 XML files in xml folder to <IS_HOME>/repository/conf.
6. Add all jars in the dropins folder to <IS_HOME>/repository/components/dropins.
7. Copy weblib jars to <IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/lib.
8. To enable APIs related to approval,
   - **For versions below IS-7.1.0**
      - add `<import resource="classpath:META-INF/cxf/user-approval-v1-cxf.xml"/>` under beans starting tag and add `<bean class="org.wso2.carbon.identity.rest.api.user.approval.v1.MeApi"/>` under <jaxrs:server id="users" address="/users/v1"> to beans.xml located in <IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/beans.xml.
   - **For IS-7.1.0 onwards For IS-7.1.0** 
      - add `org.wso2.carbon.identity.rest.api.user.approval.v1.MeApi` under <init-params> tag of <jaxrs.serviceClasses> under <servlet-name>UserV1Servlet</servlet-name> to beans.xml located in <IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/web.xml.
9. Add listener and handler to <IS_HOME>/repository/conf/deployment.toml

```
[[event_listener]]
id = "mgt_workflow_listner"
type = "org.wso2.carbon.user.core.listener.UserOperationEventListener"
name = "org.wso2.carbon.user.mgt.workflow.userstore.UserStoreActionListener"
order = 10

[[event_handler]]
name= "WorkflowPendingUserAuthnHandler"
subscriptions =["PRE_AUTHENTICATION"]

```
10. Add below config to <IS_HOME>/repository/conf/deployment.toml and enable approval from UI console.
```
    [console.approvals]
    enabled=true
```

11. Re-enable workflow approval API by adding the configuration below to the <IS_HOME>/repository/conf/deployment.toml

```
[[api_resources]]
name = "workflow Approval API"
identifier = "/me/approval-tasks"
requiresAuthorization = true
type = "TENANT"
description = "API representation of the workflow Approval API"

[[api_resources.scopes]]
displayName = "View Workflow Approvals"
name = "internal_humantask_view"

```

# [Related REST APIs to workflow](https://is.docs.wso2.com/en/latest/apis/approvals-rest-api/)

## [WorkFlow Engine](../components/org.wso2.carbon.identity.workflow.core/org.wso2.carbon.identity.api.server.workflow.engine/org.wso2.carbon.identity.rest.api.server.workflow.engine.v1/src/main/resources/workflow-engine.yaml)
This is the RESTful API for managing WorkFlow Engines in WSO2 Identity Server

### Version: v1

**Contact information:**  
WSO2 Identity Server  
https://wso2.com/identity-and-access-management/  
architecture@wso2.com

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

### Security
**BasicAuth**

|basic|*Basic*|
|---|---|

**OAuth2**

|oauth2|*OAuth 2.0*|
|---|---|
|Flow|accessCode|
|Authorization URL|https://localhost:9443/oauth2/authorize|
|Token URL|https://localhost:9443/oauth2/token|

### /workflow-engines

#### GET
##### Summary:

Retrieve all the available workflow engines.

##### Description:

Retrieve metadata information of all the workflow engines in the system.

<b>Permission required:</b>
* /permission/admin/manage/humantask/viewtasks


##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | search results matching criteria | [ [WorkFlowEngine](#WorkFlowEngine) ] |
| 401 |  |  |
| 403 |  |  |
| 404 |  |  |
| 500 |  |  |

### Models


#### WorkFlowEngine

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| profileName | string | A unique name for the workflow engine. | Yes |
| workerHostURL | string | URL of the workflow worker node. | No |
| managerHostURL | string | URL of the workflow manager node. | No |
| userName | string | Username of the creator of the workflow engine. | No |

#### Error

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| code | string |  | Yes |
| message | string |  | Yes |
| description | string |  | No |
| traceId | string |  | No |

## [WSO2 Identity Server - Workflow Approval API Definition](../components/org.wso2.carbon.identity.workflow.core/org.wso2.carbon.identity.api.user.approval/org.wso2.carbon.identity.rest.api.user.approval.v1/src/main/resources/approval.yaml)
This is the RESTful API for a user to manage his/her pending approvals in WSO2 Identity Server. This API can be used to retrieve pending approvals and update the status of the approval tasks for the authenticated user.


### Version: v1

**Contact information:**  
WSO2 Identity Server  
https://wso2.com/identity-and-access-management/  
architecture@wso2.org

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

### Security
**BasicAuth**

|basic|*Basic*|
|---|---|

**OAuth2**

|oauth2|*OAuth 2.0*|
|---|---|
|Flow|accessCode|
|Authorization URL|https://localhost:9443/oauth2/authorize|
|Token URL|https://localhost:9443/oauth2/token|

### /me/approval-tasks

#### GET
##### Summary:

Retrieves available approvals for the authenticated user

##### Description:

Retrieve the available approval tasks in the system for the authenticated user. This API returns the following types of approvals:
* READY - Tasks that are _claimable_ by the user. If a particular task is in the READY state, the user is eligible to self-assign the task and complete it.
* RESERVED -  Tasks that are _assigned_ to the user and to be approved by this user.
* COMPLETED - Tasks that are already _completed_ (approved or denied) by this user.

<b>Permission required:</b>
* /permission/admin/manage/humantask/viewtasks
<b>Scope required:</b>
* internal_humantask_view

A user can also invoke the endpoint with the following query parameters.


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
|  |  |  | No | [limitQueryParam](#limitQueryParam) |
|  |  |  | No | [offsetQueryParam](#offsetQueryParam) |
|  |  |  | No | [statusQueryParam](#statusQueryParam) |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Array of approval tasks matching the search criteria | [ [TaskSummary](#TaskSummary) ] |
| 400 |  |  |
| 401 |  |  |
| 403 |  |  |
| 500 |  |  |

### /me/approval-tasks/{task-id}

#### GET
##### Summary:

Retrieves an approval task by the task-id

##### Description:

Retrieves information of a specific approval task identified by the task-id <br/>
<b>Permission required:</b>
* /permission/admin/manage/humantask/viewtasks
<b>Scope required:</b>
* internal_humantask_view


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
|  |  |  | No | [taskIdPathParam](#taskIdPathParam) |

##### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Detailed information of the approval task identified by the task-id | [TaskData](#TaskData) |
| 400 |  |  |
| 401 |  |  |
| 403 |  |  |
| 404 |  |  |
| 409 |  |  |
| 500 |  |  |

### /me/approval-tasks/{task-id}/state

#### PUT
##### Summary:

Changes the state of an approval task

##### Description:

Update the approval task status by defining one of the following actions:
* CLAIM - Reserve the task for the user. Status of the task will be changed from READY to RESERVED.
* RELEASE - Release the task for other users to claim. Status of the task will be changed from RESERVED to READY.
* APPROVE - Approve the task. Status of the task will be changed to COMPLETED.
* REJECT - Deny the task. Status of the task will be changed to COMPLETED.
  <br/>

<b>Permission required:</b>
* /permission/admin/manage/humantask/viewtasks
<b>Scope required:</b>
* internal_humantask_view


##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
|  |  |  | No | [taskIdPathParam](#taskIdPathParam) |
| next-state | body | To which state the task should be changed. | No | [State](#State) |

##### Responses

| Code | Description |
| ---- | ----------- |
| 200 |  |
| 400 |  |
| 401 |  |
| 403 |  |
| 404 |  |
| 500 |  |

### Models


#### TaskSummary

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | Unique ID to represent an Approval Task | No |
| name | string | Unique name for the Approval Task | No |
| presentationSubject | string | Display value for Approval Operation | No |
| presentationName | string | Display value for Approval Task | No |
| taskType | string | Type of the Approval | No |
| status | string | State of the Approval task | No |
| priority | integer | Priority of the Approval task | No |
| createdTimeInMillis | string | The time that the operation for approval initiated | No |

#### TaskData

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | Unique ID to represent a approval task | No |
| subject | string | Subject of the Approval | No |
| description | string | Description on the Approval task | No |
| priority | integer | Priority of the Approval task | No |
| initiator | string | The user who initiated the task | No |
| approvalStatus | string | Available only for the completed Tasks, APPROVED or REJECTED if the task has been completed, PENDING otherwise  | No |
| assignees | [ [Property](#Property) ] | To whom the task is assigned:   * user - username(s) if the task is reserved for specific user(s).   * group - role name(s) if the task is assignable for group(s).  | No |
| properties | [ [Property](#Property) ] |  | No |

#### Property

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| key | string |  | No |
| value | string |  | No |

#### State

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| action | string | Action to perform on the task. | No |

#### Error

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| code | string |  | Yes |
| message | string |  | Yes |
| description | string |  | No |
| traceId | string |  | No |



