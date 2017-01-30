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

package com.comcast.redirector.core.balancer.serviceprovider;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.engine.FilterMode;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class AggregateServiceProvider implements IAggregateServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(AggregateServiceProvider.class);

    private ProviderStrategy<MetaData> providerStrategy;
    private InstanceProviderImpl instanceProvider;
    private String flavor;

    public AggregateServiceProvider(ProviderStrategy<MetaData> providerStrategy,
                                    Collection<IFilteredServiceProvider> providers,
                                    String flavor) {
        this.providerStrategy = providerStrategy;
        this.instanceProvider = new InstanceProviderImpl(providers);
        this.flavor = flavor;
    }

    @Override
    public Collection<IFilteredServiceProvider> getAggregatedProviders() {
        return instanceProvider.getProviders();
    }

    @Override
    public ServiceInstance<MetaData> getInstance() {
        try {
            return providerStrategy.getInstance(instanceProvider);
        } catch (Exception e) {
            log.error("Failed to get service instance", e);
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllInstances() {
        try {
            return instanceProvider.getInstances();
        } catch (Exception e) {
            log.error("Failed to get service instances", e);
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllUniqueInstances() {
        try {
            return new HashSet<>(getAllInstances());
        } catch (Exception e) {
            log.error("failed to get all service instances", e);
            return null;
        }
    }

    @Override
    public ServiceInstance<MetaData> getFilteredInstance(FilterMode filterMode, IWhitelistPredicate whitelist) {
        try {
            return providerStrategy.getInstance(() -> filteredInstances(instanceProvider.getInstances(), filterMode, whitelist));
        } catch (Exception e) {
            log.error("Failed to get filtered service instance", e);
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllFilteredInstances(FilterMode filterMode, IWhitelistPredicate whitelist) {
        try {
            return filteredInstances(instanceProvider.getInstances(), filterMode, whitelist);
        } catch (Exception e) {
            log.error("Failed to get filtered service instances", e);
            return null;
        }
    }

    private static boolean appliesToFilter(ServiceInstance<MetaData> instance, FilterMode filterMode, IWhitelistPredicate whitelist) {
        String stack = ServiceProviderUtils.getStackNameFromServiceInstance(instance);
        if (stack == null) return false;

        if (filterMode == FilterMode.NO_FILTER) {
            return true;
        } else {
            boolean whitelisted = whitelist.isWhiteListed(stack);
            return (filterMode == FilterMode.WHITELIST_ONLY) ? whitelisted : !whitelisted;
        }
    }

    private static List<ServiceInstance<MetaData>> filteredInstances(Collection<ServiceInstance<MetaData>> rawInstances,
                                                              FilterMode filterMode, IWhitelistPredicate whitelist) throws Exception {
        return rawInstances.stream()
            .filter(instance -> appliesToFilter(instance, filterMode, whitelist))
            .collect(Collectors.toList());
    }

    @Override
    public XreStackPath getStack() {
        // this is flavor provider. it aggregates few stack providers so no stack name for him itself
        return null;
    }

    @Override
    public String getFlavor() {
        return flavor;
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("close each internal provider separately");
    }

    private static class InstanceProviderImpl implements InstanceProvider<MetaData> {

        private Collection<IFilteredServiceProvider> providers;

        private InstanceProviderImpl(Collection<IFilteredServiceProvider> providers) {
            this.providers = providers;
        }

        public Collection<IFilteredServiceProvider> getProviders() {
            return providers;
        }

        @Override
        public List<ServiceInstance<MetaData>> getInstances() throws Exception {
            List<ServiceInstance<MetaData>> result = new ArrayList<>();
            for (IFilteredServiceProvider provider : providers) {
                result.addAll(getAllInstances(provider));
            }
            return result;
        }

        protected Collection<ServiceInstance<MetaData>> getAllInstances(IFilteredServiceProvider provider) throws Exception {
            return serviceInstancesWithStackNames(provider.getAllInstances(), provider.getStack().getStackOnlyPath());
        }

        protected static Collection<ServiceInstance<MetaData>> serviceInstancesWithStackNames(Collection<ServiceInstance<MetaData>> instances, String stackName) {
            for (ServiceInstance<MetaData> instance : instances) {
                ServiceProviderUtils.setStackNameForServiceInstance(instance, stackName);
            }

            return instances;
        }
    }
}
