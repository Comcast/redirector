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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.core.balancer.serviceprovider.discovery;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.balancer.serviceprovider.DynamicServiceProviderFactory;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.StackServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.IDiscoveryBackupManager;
import com.comcast.redirector.core.balancer.serviceprovider.weight.InstanceWeigher;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.CustomServiceProvider;
import org.apache.curator.x.discovery.details.ServiceDiscoveryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryServiceProviderFactory implements DynamicServiceProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryServiceProviderFactory.class);

    private ServiceDiscoveryImpl<MetaData> serviceDiscovery;
    private String zookeeperBasePath;

    private ZKConfig config;
    private ProviderStrategy<MetaData> providerStrategy;
    private CuratorFramework curatorFramework;
    private IDiscoveryBackupManager discoveryStacksBackupManager;

    public DiscoveryServiceProviderFactory(ZKConfig config,
                                           ProviderStrategy<MetaData> providerStrategy,
                                           CuratorFramework curatorFramework,
                                           IDiscoveryBackupManager discoveryStacksBackupManager) {
        this.config = config;
        this.providerStrategy = providerStrategy;
        this.curatorFramework = curatorFramework;
        this.discoveryStacksBackupManager = discoveryStacksBackupManager;

        init();
    }

    private void init() {
        this.zookeeperBasePath = PathHelper.getPathHelper(EntityType.STACK, config.getZooKeeperBasePath()).getPath();
        serviceDiscovery = (ServiceDiscoveryImpl<MetaData>) ServiceDiscoveryBuilder.builder(MetaData.class)
                .client(curatorFramework)
                .basePath(this.zookeeperBasePath)
                .build();
        try {
            serviceDiscovery.start();
        } catch (Exception e) {
            String description = "ZooKeeper connection issue should not cause this exception. Verified on org.apache.curator:curator-x-discovery:2.4.1" +
                    " serviceDiscovery.start() is not actually communicating with ZooKeeper unless ServiceDiscoveryBuilder.thisInstance(ServiceInstance) was called." +
                    " If exception was thrown our assumption is curator-x-discovery source code was incompatible changed.";

            throw new RuntimeException(description, e);
        }
    }

    @Override
    public IFilteredServiceProvider createServiceProvider(XreStackPath servicePath) {
        ServiceProvider<MetaData> customServiceProvider = new CustomServiceProvider<>(serviceDiscovery, zookeeperBasePath, servicePath.getPath(),
            providerStrategy, discoveryStacksBackupManager, new InstanceWeigher(config));
        try {
            customServiceProvider.start();
            return new StackServiceProvider(customServiceProvider, servicePath);
        } catch (Exception e) {
            log.error("failed to create service provider for path {}", servicePath.getPath(), e);
            return null;
        }
    }
}
