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

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.balancer.serviceprovider.weight.InstanceWeigher;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.InstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

class StaticServiceProviderFactory implements IStaticServiceProviderFactory {
    private ProviderStrategy<MetaData> providerStrategy;
    private ZKConfig config;

    StaticServiceProviderFactory(ProviderStrategy<MetaData> providerStrategy, ZKConfig config) {
        this.providerStrategy = providerStrategy;
        this.config = config;
    }

    @Override
    public IFilteredServiceProvider createServiceProvider(XreStackPath servicePath, List<HostIPs> hosts) {
        return new StackServiceProvider(
            new StaticServiceProvider(providerStrategy, servicePath.getServiceName(), hosts, config),
            servicePath);
    }

    private static class StaticServiceProvider implements ServiceProvider<MetaData> {
        private static Logger log = LoggerFactory.getLogger(StaticServiceProvider.class);

        private final ProviderStrategy<MetaData> providerStrategy;
        private final InstanceProvider<MetaData> instanceProvider;

        /**
         * @param providerStrategy strategy of selecting particular service instance (host)
         * @param serviceName name of service served by the provider, e.g. xreGuide
         * @param serviceInstances list of serviceInstances ([host]:[port]) running particular service
         */
        StaticServiceProvider(ProviderStrategy<MetaData> providerStrategy, String serviceName,
                              List<HostIPs> serviceInstances, ZKConfig config) {
            this.providerStrategy = providerStrategy;
            this.instanceProvider = new StaticInstanceProvider(serviceName, serviceInstances, config);
        }

        @Override
        public void start() throws Exception {
            throw new UnsupportedOperationException("no need to start offline service provider");
        }

        @Override
        public ServiceInstance<MetaData> getInstance() throws Exception {
            return providerStrategy.getInstance(instanceProvider);
        }

        @Override
        public Collection<ServiceInstance<MetaData>> getAllInstances() throws Exception {
            return instanceProvider.getInstances();
        }

        @Override
        public void noteError(ServiceInstance<MetaData> instance) {
            throw new UnsupportedOperationException("noteError is not supported");
        }

        @Override
        public void close() throws IOException {
            try {
                instanceProvider.getInstances().clear();
            } catch (Exception e) {
                log.error("Can't close provider ", e);
            }
        }

        /**
         * Provider holds static list {@link org.apache.curator.x.discovery.ServiceInstance} objects
         */
        private static class StaticInstanceProvider implements InstanceProvider<MetaData> {
            private final List<ServiceInstance<MetaData>> instances;
            private final ZKConfig config;
            private final InstanceWeigher weigher;

            /**
             * @param serviceName name of service served by the provider, e.g. xreGuide
             * @param instances list of service ids this provider holds. Service id is [ip address]:[port]
             */
            StaticInstanceProvider(String serviceName, List<HostIPs> instances, ZKConfig config) {
                this.config = config;
                this.instances = new ArrayList<>(getFinalInstancesSize(instances));
                this.weigher = new InstanceWeigher(config);

                try {
                    for (HostIPs instance : instances) {
                        ServiceInstanceBuilder<MetaData> builder = new LiteServiceInstanceBuilder();
                        ServiceInstance<MetaData> serviceInstance = builder.id(instance.getIpV4Address()) // id is <ip address>:<port>
                            .name(serviceName)
                            .address(instance.getIpV4Address()) // get address from instance id
                            .payload(ServiceProviderUtils.getMetaDataFromHost(instance))
                            .build();

                        int weight = weigher.getWeight(serviceInstance);
                        if (weight > 0) {
                            for (int i = 0; i < weight; i++) {
                                this.instances.add(serviceInstance);
                            }
                        }
                        else {
                            log.warn("No traffic will be sent to the host: IPv4={} IPv6={}, because its weight is {}, but should be above 0.",
                                    String.valueOf(instance.getIpV4Address()), String.valueOf(instance.getIpV4Address()), instance.getWeight());
                        }
                    }
                    Collections.shuffle(this.instances, new Random(System.currentTimeMillis()));
                } catch (Exception e) {
                    log.error("failed to build offline instance provider", e);
                }
            }

            @Override
            public List<ServiceInstance<MetaData>> getInstances() throws Exception {
                return instances;
            }

            private int getFinalInstancesSize(List<HostIPs> instances) {
                return instances.size() > 0 ? instances.size() * config.getDefaultWeightOfTheNode() : 0;
            }
        }
    }
}
