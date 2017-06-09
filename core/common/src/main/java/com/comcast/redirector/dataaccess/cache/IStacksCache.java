/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
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

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// TODO: passing just a string is not clear if it's a stack path like /PO/POC6 or internal zk path
public interface IStacksCache {
    void waitForStackCacheAvailability() throws InterruptedException;
    void addCacheListener(ICacheListener listener);

    Set<XreStackPath> getAllStackPaths();

    List<String> getAppVersionsDeployedOnStack(String stack, String appName);
    List<HostIPs> getHostsRunningOnStack(String stack, String appName);
    List<HostIPs> getHostsRunningAppVersionOnStack(XreStackPath path);

    int getHostsCount(XreStackPath path) throws DataSourceConnectorException;
    HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException;

    interface IHostGetter {
        List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException;
        int getHostsCount(XreStackPath path) throws DataSourceConnectorException;
        HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException;
    }
}
