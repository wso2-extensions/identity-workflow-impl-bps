# Configuring the Workflow connector

To use workflow with WSO2 Identity Server 7 onwards, you need to follow below steps. 

## Instructions to enable workkflow

1. Unzip the zip file.
2. Run identity workflow-related db scripts against the identity database known as IDENTITY_DB (WSO2IDENTITY_DB.mv.db).
3. Copy bps-datasources.xml to <IS_HOME>/repository/conf/datasources.
4. Run bps-related scripts against the database called JPA_DB (jpadb.mv.db).
   More on:
   https://is.docs.wso2.com/en/latest/deploy/work-with-databases/, 
   https://is.docs.wso2.com/en/latest/deploy/change-datasource-bpsds/

5. Copy 4 XML files in xml folder to <IS_HOME>/repository/conf.
6. Add jars in the dropins folder to <IS_HOME>/repository/components/dropins.
7. Copy weblib jars to <IS_HOME>/repository/deployment/server/webapps/api/WEB-INF/lib.
8. Add listener and handler to <IS_HOME>/repository/conf/deployment.toml

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