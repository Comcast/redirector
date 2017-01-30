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

package com.comcast.redirector.core.balancer;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.metrics.Metrics;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.StackServiceProvider;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.redirector.core.engine.FilterMode;
import com.comcast.redirector.core.engine.ServerLookupMode;
import com.comcast.redirector.common.util.AppLoggingHelper;
import com.comcast.redirector.ruleengine.model.Server;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.*;

public class Balancer implements IBalancer {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(Balancer.class);
    private AppLoggingHelper loggingHelper;
    private String appName;

    private IServiceProviderManager serviceProviderManager;
    private StackDataForService stackDataForService;
    private IWhitelistPredicate whiteListedStacksManager;

    public Balancer(String appName, IServiceProviderManager serviceProviderManager,
                    IWhitelistPredicate whiteListedStacksManager,
                    int modelVersion) {
        this.appName = appName;
        loggingHelper = new AppLoggingHelper(log, appName, modelVersion);
        stackDataForService = new StackDataForService(appName);
        this.serviceProviderManager = serviceProviderManager;
        this.whiteListedStacksManager = whiteListedStacksManager;
    }

    @Override
    public InstanceInfo getServiceInstance(Server server, ServerLookupMode mode) {
        ServiceInstance<MetaData> serviceInstance;
        if (server == null) {
            return null;
        }

        String flavorPath = server.getPath();
        boolean isStackBased = isStackBasedPath(flavorPath);
        IFilteredServiceProvider provider = getServiceProvider(flavorPath);

        InstanceInfo result = null;

        if (provider != null) {
            serviceInstance = mode.getFilterMode() == FilterMode.NO_FILTER ?
                provider.getInstance() : provider.getFilteredInstance(mode.getFilterMode(), whiteListedStacksManager);

            if (serviceInstance == null) {
                loggingHelper.info(", NO HOSTS FOUND,  balancerStatus=NoHosts type={}, path={}, byLookupMode=[ applyFilter={} , forceGetFromBackup={} ]",
                    (isStackBased ? "STACK" : "FLAVOR"), flavorPath,
                    mode.getFilterMode(), mode.isForceGetFromBackup());
            } else {
                result = new InstanceInfo(server,
                    (isStackBased) ? ServiceProviderUtils.formatStackPath(flavorPath) : ServiceProviderUtils.getStackNameFromServiceInstance(serviceInstance), // TODO: return own model that has following: IPv4, IPv6, StackPath
                    ServiceProviderUtils.getAddress(serviceInstance, IpProtocolVersion.IPV4),
                    ServiceProviderUtils.getAddress(serviceInstance, IpProtocolVersion.IPV6),
                    isStackBased);
            }
        }

        if (result == null) {
            Metrics.reportGatewayFailedResponseNoHostsStats();
        }

        return result;
    }

    @Override
    public int getCountOfHostsForPath(String path, ServerLookupMode mode){
        IFilteredServiceProvider provider = getServiceProvider(path);

        if (provider != null) {
            FilterMode filterMode = mode.getFilterMode();
            Collection<ServiceInstance<MetaData>> instances;
            if (filterMode == FilterMode.NO_FILTER) {
                instances = provider.getAllInstances();
            } else {
                instances = provider.getAllFilteredInstances(filterMode, whiteListedStacksManager);
            }

            if (instances != null) {
                return StackServiceProvider.UniqueInstancesExtractor.extract(instances).size();
            }
        }

        return 0;
    }

    private static boolean isStackBasedPath(String path) {
        return path.contains(RedirectorConstants.DELIMETER);
    }

    private IFilteredServiceProvider getServiceProvider(String path) {
        IFilteredServiceProvider provider;
        boolean isStackBased = isStackBasedPath(path);
        if (isStackBased) {
            provider = getStackServiceProvider(path);
        } else {
            provider = getFlavorServiceProvider(path);
        }

        if (provider == null) {
            loggingHelper.warn(", NO HOSTS FOUND, balancerStatus=NoHosts type={} path={}", (isStackBased ? "STACK" : "FLAVOR"), path);
            return null;
        }

        return provider;
    }

    private IFilteredServiceProvider getStackServiceProvider(String stackPath) {
        return serviceProviderManager.getStackServiceProvider(stackDataForService.create(stackPath));
    }

    private IFilteredServiceProvider getFlavorServiceProvider(String flavor) {
        return serviceProviderManager.getServiceProviderForFlavorAndApp(flavor, appName);
    }

    /**
     * Helper class. Create new instance with provided String serviceName. After that call create() with /<dataCenter>/<availabilityZone>/<flavor>
     * and get full path concatenated with serviceName.
     */
    private static class StackDataForService {

        private String serviceName;

        StackDataForService(String serviceName) {
            this.serviceName = serviceName;
        }

        /**
         * @param partialPath /<dataCenter>/<availabilityZone>/<flavor>
         * @return /<dataCenter>/<availabilityZone>/<flavor>/<serviceName>
         */
        public StackData create(String partialPath) throws IllegalArgumentException {
            return new StackData(partialPath + RedirectorConstants.DELIMETER + serviceName);
        }

        public StackData create(String dataCenter, String availabilityZone, String flavor) {
            return new StackData(dataCenter, availabilityZone, flavor, serviceName);
        }
    }
}
