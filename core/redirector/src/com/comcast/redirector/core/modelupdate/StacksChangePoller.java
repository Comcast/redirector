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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */
package com.comcast.redirector.core.modelupdate;


import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.dataaccess.cache.newzkstackscache.IServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.comcast.redirector.common.RedirectorConstants.STACKS_CONTROLLER_PATH;

public class StacksChangePoller {
    private static final Logger log = LoggerFactory.getLogger(StacksChangePoller.class);

    private IDataChangePoller dataChangePoller;
    private Integer currentStacksRefreshVersion = 0;
    private int stacksRefreshUpdateIntervalSeconds = 60;
    
    public StacksChangePoller(ZKConfig zkConfig, IDataChangePoller dataChangePoller) {
        this.dataChangePoller = dataChangePoller;
        stacksRefreshUpdateIntervalSeconds = zkConfig.getStacksRefreshPollIntervalSeconds();
    }

    public Boolean initStacksPolling(IServiceDiscovery serviceDiscovery) {
        dataChangePoller.startDataChangePolling("refreshStacks", STACKS_CONTROLLER_PATH + "/getVersion/",
                stacksRefreshUpdateIntervalSeconds,
                serviceDiscovery::discoverAppsAndStacksChanges,
                this::getCurrentStacksRefreshVersion,
                this::setCurrentStacksRefreshVersion,
                null /* next stack version */,
                null /* application Name*/);
        
        return true;
    }

    public Integer getCurrentStacksRefreshVersion() {
        return currentStacksRefreshVersion;
    }

    public void setCurrentStacksRefreshVersion(Integer currentStacksRefreshVersion) {
        this.currentStacksRefreshVersion = currentStacksRefreshVersion;
    }
}
