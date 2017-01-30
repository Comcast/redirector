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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.*;

import java.util.Set;

/**
 * Business logic layer for Stacks
 */
public interface IStacksService {

    /**
     * Get all stack and flavors for given service name including amount of hosts for each item.
     * See {@link PathItem} for details
     *
     * @param serviceName service name for which to obtain service paths
     * @return {@link ServicePaths} instance representing stacks and flavors for given service
     */
    ServicePaths getStacksForService(String serviceName);

    ServicePaths updateStacksForService(String serviceName, ServicePaths servicePaths, Whitelisted whitelisted);

    /**
     * @return set of service names registered in stack paths
     */
    Set<String> getAllServiceNames();

    /**
     * Get all stack and flavors including amount of hosts for each item.
     * See {@link PathItem} for details
     *
     * @return {@link ServicePaths} instance representing all stacks and flavors currently registered
     */
    ServicePaths getStacksForAllServices();

    /**
     * Get a set of active stacks and flavors (which have more than one hosts registered)
     * @param serviceName
     * @return set of active stacks and flavors by given service names
     */
    Set<PathItem> getActiveStacksAndFlavors(String serviceName);

    Set<PathItem> getActiveStacksAndFlavors(String serviceName, ServicePaths servicePaths);

    /**
     * Delete single stack path
     *
     * @param serviceName
     * @param pathDataCenter data center e.g. PO
     * @param availabilityZone availability zone e.g. POC5
     * @param flavor BOM version e.g. 1.48P7
     */
    void deleteStack(String serviceName, String pathDataCenter, String availabilityZone, String flavor);

    /**
     * Delete multiple stack paths
     *
     * @param paths {@link Paths} instance encapsulating stacks for deletion
     */
    void deleteStacks(Paths paths);

    /**
     * Gets hosts by stack only path (without flavor), e.g. /PO/POC7
     * @param stackName  stack name without flavor e.g. /PO/POC7
     * @param serviceName service name e.g. xreGuide
     * @return  {@link HostIPsListWrapper} instance containing list of hosts for given stack and service
     */
    HostIPsListWrapper getHostsForStackOnlyAndService(String stackName, String serviceName);

    /**
     * Get a random host for given stack name (without flavor) and service
     * @param stackName stack name without flavor e.g. /PO/POC7
     * @param serviceName service name e.g. xreGuide
     * @return {@link HostIPsListWrapper} instance containing one random host for given stack and service
     */
    HostIPsListWrapper getRandomHostForStackOnlyAndService(String stackName, String serviceName);

    /**
     * Get hosts for given stack and service
     *
     * @param stackName stack name including flavor e.g. /PO/POC7/1.50
     * @param serviceName service name e.g. xreGuide
     * @return {@link HostIPsListWrapper} instance containing list of hosts for given stack and service
     */
    HostIPsListWrapper getHostsForStackAndService(String stackName, String serviceName);

    /**
     * Get a random host for given stack and service
     * @param stackName stack name including flavor e.g. /PO/POC7/1.50
     * @param serviceName service name e.g. xreGuide
     * @return {@link HostIPsListWrapper} instance containing one random host for given stack and service
     */
    HostIPsListWrapper getRandomHostForStackAndService(String stackName, String serviceName);

    /**
     * Get hosts for given flavor and service
     *
     * @param flavorName flavor name e.g. 1.50
     * @param serviceName service name e.g. xreGuide
     * @return {@link HostIPsListWrapper} instance containing list of hosts for given flavor and service
     */
    HostIPsListWrapper getHostsForFlavorAndService(String flavorName, String serviceName);

    /**
     * Get a random host for given flavor and service
     * @param flavorName flavor name e.g. 1.50
     * @param serviceName service name e.g. xreGuide
     * @return {@link HostIPsListWrapper} instance containing one random host for given flavor and service
     */
    HostIPsListWrapper getRandomHostForFlavorAndService(String flavorName, String serviceName);

    /**
     * @param serviceName service name e.g. xreGuide
     * @return Set of {@link StackData} instances containing stack paths and hosts for each path
     */
    Set<StackData> getAllStacksAndHosts(String serviceName);

    HostIPsListWrapper getAddressByStackOrFlavor(String appName, String stackName, String flavorName);

    HostIPsListWrapper getRandomAddressByStackOrFlavor(String serviceName, String stackName, String flavorName);

    /**
     * @param serviceName service name e.g. xreGuide
     * @return TRUE if service exist
     */
    Boolean isServiceExists(String serviceName);
}
