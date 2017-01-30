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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * DAO for access to Stacks data in data storage.
 * Please note: stacks tree will always be cached but hosts will always be retrieved non-cached.
 */
public interface IStacksDAO {
    /**
     * Search data store for all stack paths currently registered
     *
     * @return collection of objects representing all stack paths currently registered
     */
    Set<XreStackPath> getAllStackPaths();

    /**
     * Search data store for all applications instead of excludedApps
     *
     * @return collection of objects representing all stack paths currently registered
     */
    Set<XreStackPath> getAllStackPaths(Set<String> excludedApps);


    /**
     * Gets collection of hosts registered in data store for given stack path
     *
     * @param path stack path for which we get hosts
     * @return collection of objects representing hosts by given stack path
     */
    Collection<HostIPs> getHosts(XreStackPath path);

    /**
     * Gets collection of hosts registered in data store for given stack only path (e.g. /POC/POC7)
     * @param path stack path for which we get hosts
     * @return collection of objects representing hosts by given stack path
     */
    Collection<HostIPs> getHostsByStackOnlyPath(XreStackPath path);

    /**
     * Gets collection of flavors registered in data store for given stack only path (e.g. /POC/POC7)
     * @param path stack path for which we get flavors
     * @return  collection of objects representing flavors by given stack path
     */
    List<String> getFlavorsByStackOnlyPath(XreStackPath path);

    /**
     * Get one host from stack by host index
     * @param path stack path for which we get hosts
     * @param index number of the host in stack
     * @return an object representing host by given stack path and index
     */
    HostIPs getHostByIndex(XreStackPath path, int index);

    /**
     * Deletes given stack path from data store
     *
     * @param path stack path to delete
     */
    void deleteStackPath(XreStackPath path);

    /**
     * Get count of hosts registered in data store for given stack path
     * @param path stack path for which we get count of hosts
     * @return count of hosts for given stack path
     */
    int getHostsCount(XreStackPath path);

    Set<String> getAllAppNamesRegisteredInStacks();

    void addCacheListener(ICacheListener listener);
}
