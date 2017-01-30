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

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.IAppBackupManagerFactories;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.balancer.serviceprovider.discovery.DiscoveryServiceProviderFactory;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.DiscoveryBackupManager;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.IDiscoveryBackupManager;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Supplier;

public class ServiceProviderManagerFactory implements IServiceProviderManagerFactory {
    private ProviderStrategy<MetaData> providerStrategy;
    private ZKConfig config;

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private CuratorFramework curatorFramework;

    @Autowired
    private ICommonModelFacade commonModelFacade;

    @Autowired
    private IBackupManagerFactory globalBackupManagerFactory;

    @Autowired
    private IAppBackupManagerFactories appBackupManagerFactories;

    @Autowired(required = false)
    private IntegrationTestChangeListener<StackSnapshot> integrationTestChangeListener;

    private IServiceProviderManager dynamicServiceProviderManager;

    // TODO: this is a workaround. We need it as a member of a factory only to create stacks.json from manualbackup.json
    private IDiscoveryBackupManager discoveryBackupManager;

    @PostConstruct
    private void initBackupManager() {
        // This is a workaround. We need it to init dynamic backup from static backup
        // TODO: remove this code
        discoveryBackupManager = createDiscoveryBackupManager();
    }

    @Autowired(required = false)
    public void setProviderStrategy(ProviderStrategy<MetaData> providerStrategy) {
        this.providerStrategy = providerStrategy;
    }

    @Autowired(required = false)
    public void setConfig(ZKConfig config) {
        this.config = config;
    }

    @Override
    public synchronized IServiceProviderManager newDynamicServiceProviderManager() {
        if (dynamicServiceProviderManager == null) {
            dynamicServiceProviderManager = createDynamicServiceProviderManager();
        }
        return dynamicServiceProviderManager;
    }

    private IServiceProviderManager createDynamicServiceProviderManager() {
        DynamicServiceProviderFactory dynamicServiceProviderFactory = new DiscoveryServiceProviderFactory(
            config,
            providerStrategy,
            curatorFramework,
            discoveryBackupManager
        );

        Supplier<Set<XreStackPath>> availableStacksSupplier =
            () -> commonModelFacade.getAllStackPaths(config.getExcludedAppsFromStackAutoDiscovery());

        DynamicServiceProviderManager.Initializer backupInitializer = internalInitializer -> {
            if (! connector.isConnected() ) {
                internalInitializer.accept(discoveryBackupManager.getCurrentSnapshot().getAllStacks());
                return true;
            }

            return false;
        };

        return new DynamicServiceProviderManager(config, providerStrategy,
            availableStacksSupplier,
            backupInitializer,
            dynamicServiceProviderFactory);
    }

    private IDiscoveryBackupManager createDiscoveryBackupManager() {
        Set<String> excludedApps = config.getExcludedAppsFromStackAutoDiscovery();

        return new DiscoveryBackupManager.Builder()
            .excludedApplications(excludedApps)
            .withGlobalBackupManagerFactory(globalBackupManagerFactory)
            .withAppBackupManagerFactories(appBackupManagerFactories)
            .withIntegrationTestChangeListener(integrationTestChangeListener)
            .build();
    }

    @Override
    public IServiceProviderManager newStaticServiceProviderManager(Set<StackData> stacks) {
        return new StaticServiceProviderManager(stacks, config, providerStrategy);
    }
}
