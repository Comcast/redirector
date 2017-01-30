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

package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.modelupdate.NewVersionHandler;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ICommonModelFacade extends IModelFacade {
    Collection<NamespacedList> getAllNamespacedLists();
    NamespacedList getNamespacedList(String namespacedListName);
    int getNamespacedListsVersion();
    Integer getNextNamespacedListsVersion();

    void initNamespacedListDataChangePolling(NewVersionHandler<Integer> refreshNamespacedLists);

    RedirectorConfig getRedirectorConfig();
    void saveRedirectorConfig(RedirectorConfig config);

    Set<String> getAllRegisteredApps();
    Set<XreStackPath> getAllStackPaths();
    Set<XreStackPath> getAllStackPaths(Set<String> excludedApps);
    Collection<HostIPs> getHosts(XreStackPath path);

    List<String> getApplications();
    Boolean isValidModelForAppExists(String appName);
}
