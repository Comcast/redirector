/**
 * Copyright 2016 Comcast Cable Communications Management, LLC 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.model.ServerGroup;

import java.util.Map;

/**
 * Encapsulate redirector logic engine
 */
public interface IRedirectorEngine {
    /**
     * Returns instance info for redirect based on input parameters map
     * @param context map of parameters STB
     * @return instance info containing ready-to-use redirector URL and additional info for tracking/testing
     */
    InstanceInfo redirect(Map<String, String> context);

    /**
     * Returns {@link ServerGroup} with url resolved for each server
     * @param serverGroup server group with path and template url set for each server
     * @param context map of parameters from STB
     * @return {@link ServerGroup} with url resolved for each server
     */
    ServerGroup redirectServerGroup(ServerGroup serverGroup, Map<String, String> context);

    interface IHostSelector {
        InstanceInfo getHostByContext(Map<String, String> context);

        InstanceInfo getHostByServer(Server server);

        InstanceInfo getDefaultHost();

        int getCountOfHostsForDefaultServer();
        int getCountOfHostsForDistribution();
        int getPercentDeviationCountOfHostsForDistribution();
    }
}
