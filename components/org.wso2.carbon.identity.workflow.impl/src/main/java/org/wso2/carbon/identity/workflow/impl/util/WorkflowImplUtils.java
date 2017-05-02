/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.workflow.impl.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.workflow.impl.internal.WorkflowImplServiceDataHolder;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Util class for workflow implementation.
 */
public class WorkflowImplUtils {

    /**
     * Returns server URL of the server.
     *
     * @return Server URL.
     */
    public static String getServerURL() {
        String serverURL = CarbonUtils.getServerURL(ServerConfiguration.getInstance(),
                                                    WorkflowImplServiceDataHolder.getInstance()
                                                                                 .getConfigurationContextService()
                                                                                 .getServerConfigContext());
        return StringUtils.chomp(serverURL, "/");
    }
}
