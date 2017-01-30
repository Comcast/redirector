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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.factory;

import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.api.model.xrestack.ServicePaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServicePathsFactory {

    public static ServicePaths newServicePaths(final String serviceName, final Map<String, Integer> paths) {
        final ServicePaths servicePaths = new ServicePaths();
        List<Paths> pathsList = new ArrayList<>();
        List<PathItem> stacks = new ArrayList<>();
        List<PathItem> flavours = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: paths.entrySet()) {
            String path = entry.getKey();
            Integer activeNodesCount = entry.getValue();
            PathItem stacksPathItem = new PathItem();
            stacksPathItem.setActiveNodesCount(activeNodesCount);
            stacksPathItem.setWhitelistedNodesCount(activeNodesCount);
            stacksPathItem.setValue(path);
            stacks.add(stacksPathItem);

            PathItem flavoursPathItem = new PathItem();
            flavoursPathItem.setActiveNodesCount(activeNodesCount);
            flavoursPathItem.setWhitelistedNodesCount(activeNodesCount);
            flavoursPathItem.setValue(path.substring(path.lastIndexOf("/") + 1, path.length()));
            flavours.add(flavoursPathItem);
        }
        Paths newPaths = new Paths(serviceName, stacks, flavours);
        pathsList.add(newPaths);
        servicePaths.setPaths(pathsList);

        return servicePaths;
    }

}
