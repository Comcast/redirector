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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.api.model.xrestack.HostIPsListWrapper;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.junit.Assert;

import javax.ws.rs.client.WebTarget;
import java.util.*;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

// TODO: use IntegrationTestHelper and TestServiceUtil
// TODO: group all setup routine into single class like IntegrationTestHelper so test code will become cleaner as it live separate from setup code
public class StacksHelper {

    private static final String PATHS_SERVICE_PATH = RedirectorConstants.STACKS_CONTROLLER_PATH;

    public static final String _STACK_NAME = "stackName";
    public static final String _FLAVOR_NAME = "flavorName";
    public static final String _ADDRESSES = "addresses";
    public static final String _DELETE_STACKS = "deleteStacks";

    public static ServicePaths getServicePaths(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, ServicePaths.class);
    }

    public static ServicePaths getAllStacks(String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH);
        return ServiceHelper.get(webTarget, responseMediaType, ServicePaths.class);
    }

    public static HostIPsListWrapper getAddressByStack(String serviceName, String stackName,
                                                       String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(serviceName).path(_ADDRESSES)
                .queryParam(_STACK_NAME, stackName);
        return ServiceHelper.get(webTarget, responseMediaType, HostIPsListWrapper.class);
    }

    public static HostIPsListWrapper getAddressByFlavor(String serviceName, String stackName,
                                                            String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(serviceName).path(_ADDRESSES)
                .queryParam(_FLAVOR_NAME, stackName);
        return ServiceHelper.get(webTarget, responseMediaType, HostIPsListWrapper.class);
    }

    public static HostIPsListWrapper getRandomAddressByFlavor(String serviceName, String flavorName,
                                                        String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(serviceName).path(_ADDRESSES).path("random")
                .queryParam(_FLAVOR_NAME, flavorName);
        return ServiceHelper.get(webTarget, responseMediaType, HostIPsListWrapper.class);
    }

    public static HostIPsListWrapper getRandomAddressByStack(String serviceName, String stackName,
                                                              String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(serviceName).path(_ADDRESSES).path("random")
                .queryParam(_STACK_NAME, stackName);
        return ServiceHelper.get(webTarget, responseMediaType, HostIPsListWrapper.class);
    }

    public static Paths createDeletePathsObject(String serviceName, String... pathItemsForDelete) {
        Paths pathsToDelete = new Paths(serviceName);
        List<PathItem> stacks = new ArrayList<>();
        for (String stackValue : pathItemsForDelete) {
            PathItem pathItem = new PathItem();
            pathItem.setValue(stackValue);
            stacks.add(pathItem);
        }
        pathsToDelete.setStacks(stacks);
        return pathsToDelete;
    }

    public static void deleteStacks(Paths stackValues, String mediaType) {
        // PUT request to delete
        WebTarget webTarget = HttpTestServerHelper.target().path(PATHS_SERVICE_PATH).path(_DELETE_STACKS);
        ServiceHelper.put(webTarget, stackValues, mediaType);
    }

    public static PathItem getStackItemByPath(Paths paths, String path) {
        for (PathItem item : paths.getStacks()) {
            if (path.equals(item.getValue())) {
                return item;
            }
        }
        return null;
    }

    public static Paths getPathsObjectByServiceName(ServicePaths servicePaths, String serviceName) {
        for (Paths paths: servicePaths.getPaths()) {
            if (serviceName.equals(paths.getServiceName())) {
                return paths;
            }
        }
        return null;
    }

    /**
     * @return ServicePaths object that needed for post and comparing with all responses and successful validation during saving data <p>
     * <pre>
     *  {@code
     *    <servicePaths>
     *      <paths serviceName="pathServiceTest_1">
     *        <stack nodes="2">/DataCenter1/Region1/Zone1</stack>
     *        <stack nodes="1">/DataCenter2/Region2/Zone2</stack>
     *        <stack nodes="1">/DataCenter2/Region1/Zone1</stack>
     *      </paths>
     *        <paths serviceName="pathServiceTest_2">
     *        <stack nodes="2">/DataCenter3/Region3/Zone3</stack>
     *        <stack nodes="0">/DataCenter3/Region3/Zone4</stack>
     *        <stack nodes="1">/DataCenter4/Region4/Zone4</stack>
     *      </paths>
     *    </servicePaths>
     *  }
     * </pre>
     */
    public static ServicePaths createServicePaths(String serviceName) {

        PathItem stack1_1 = new PathItem(DELIMETER + "DataCenter1" + DELIMETER + "Region1" + DELIMETER + "Zone1", 2, 2);
        PathItem stack1_2 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region2" + DELIMETER + "Zone2", 1, 1);
        PathItem stack1_3 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region1" + DELIMETER + "Zone1", 1, 1);
        Paths paths1 = new Paths(serviceName,
                new ArrayList<PathItem>(Arrays.asList(stack1_1, stack1_2, stack1_3)),
                new ArrayList<PathItem>());
        List<Paths> paths = new ArrayList<>();
        paths.add(paths1);
        ServicePaths servicePaths = new ServicePaths(paths);
        return servicePaths;
    }

    /**
     * @return ServicePaths object that needed for post and comparing with all responses and successful validation during saving data <p>
     * <pre>
     *  {@code
     *    <servicePaths>
     *      <paths serviceName="pathServiceTest_1">
     *        <stack nodes="2">/DataCenter1/Region1/Zone1</stack>
     *        <stack nodes="0">/DataCenter2/Region2/Zone2</stack>
     *        <stack nodes="1">/DataCenter2/Region1/Zone1</stack>
     *      </paths>
     *        <paths serviceName="pathServiceTest_2">
     *        <stack nodes="2">/DataCenter3/Region3/Zone3</stack>
     *        <stack nodes="0">/DataCenter3/Region3/Zone4</stack>
     *        <stack nodes="1">/DataCenter4/Region4/Zone4</stack>
     *      </paths>
     *    </servicePaths>
     *  }
     * </pre>
     */
    public static ServicePaths createServicePaths(String serviceName1, String serviceName2) {

        PathItem stack1_1 = new PathItem(DELIMETER + "DataCenter1" + DELIMETER + "Region1" + DELIMETER + "Zone1", 2, 2);
        PathItem stack1_2 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region2" + DELIMETER + "Zone2", 0, 0);
        PathItem stack1_3 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region1" + DELIMETER + "Zone1", 1, 1);

        PathItem stack2_1 = new PathItem(DELIMETER + "DataCenter3" + DELIMETER + "Region3" + DELIMETER + "Zone3", 2, 2);
        PathItem stack2_2 = new PathItem(DELIMETER + "DataCenter3" + DELIMETER + "Region3" + DELIMETER + "Zone4", 0, 0);
        PathItem stack2_3 = new PathItem(DELIMETER + "DataCenter4" + DELIMETER + "Region4" + DELIMETER + "Zone4", 1, 1);

        Paths paths1 = new Paths(serviceName1,
                new ArrayList<PathItem>(Arrays.asList(stack1_1, stack1_2, stack1_3)),
                new ArrayList<PathItem>());

        Paths paths2 = new Paths(serviceName2,
                new ArrayList<PathItem>(Arrays.asList(stack2_1, stack2_2, stack2_3)),
                new ArrayList<PathItem>());

        List<Paths> paths = new ArrayList<>();
        paths.add(paths1);
        paths.add(paths2);
        ServicePaths servicePaths = new ServicePaths(paths);
        return servicePaths;
    }

    /**
     * Generates and joins FlavorList to ServicePaths object. <p>
     * FlavorList is used for comparing it with all StacksController responses. <p>
     * For example, for created ServicePaths object in the method {@link #createServicePaths(String, String)} will be returned such data: <p>
     * @param servicePaths ServicePaths object which FlavorList object will be generated and join to
     * @return ServicePaths object with generated FlavorList
     */
    public static ServicePaths generateFlavorsForServicePaths(ServicePaths servicePaths) {
        for (Paths paths : servicePaths.getPaths()) {
            // cleanup data in flavor list if it exist
            paths.getFlavors().clear();
            // generate new flavor data
            paths.setFlavors(generateFlavorsResultForPaths(paths));
        }
        return servicePaths;
    }

    public static List<PathItem> generateFlavorsResultForPaths(Paths paths) {
        Map<String, PathItem> flavorsMap = new HashMap<String, PathItem>();
        for (PathItem stack : paths.getStacks()) {
            String flavorKey = stack.getValue().substring(stack.getValue().lastIndexOf(DELIMETER) + 1);
            PathItem flavor = flavorsMap.get(flavorKey);
            if (flavor == null) {
                flavor = new PathItem(flavorKey, stack.getActiveNodesCount(), 1);
            } else {
                flavor.setActiveNodesCount(flavor.getActiveNodesCount() + stack.getActiveNodesCount());
            }
            flavorsMap.put(flavorKey, flavor);
        }
        return new ArrayList<>(flavorsMap.values());
    }

    public static void zkPostStacks(IDataSourceConnector zClient, ServicePaths servicePaths) {
        try {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityType.STACK, zClient.getBasePath());
            String baseStacksPath = pathHelper.getPath();
            for (Paths paths : servicePaths.getPaths()) {
                String serviceName = paths.getServiceName();
                for (PathItem stacks : paths.getStacks()) {
                    String stacksPath = stacks.getValue();
                    int activeNodesCount = stacks.getActiveNodesCount();
                    String fullStacksPath = baseStacksPath + stacksPath + DELIMETER + serviceName;
                    zClient.save("", fullStacksPath);
                    for (int i = 0; i < activeNodesCount; i++) {
                        String hostNode = "Host" + i;
                        zClient.save("{\"payload\" : {\"parameters\" : {\"ipv4Address\":\"testActiveNode" + i + "-ccpapp-po-c534-p.po.ccp.cable.comcast.com\", \"ipv6Address\":\"testActiveNode" + i + "-fe80:0:0:0:200:f8ff:fe21:67cf\"}}}",
                                fullStacksPath + DELIMETER + hostNode);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public static void zkDeleteStacks(IDataSourceConnector zClient) {
        try {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityType.STACK, zClient.getBasePath());
            String baseStacksPath = pathHelper.getPath();
            for (String node: zClient.getChildren(baseStacksPath)) {
                zClient.deleteWithChildren(baseStacksPath + DELIMETER + node);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }



}
