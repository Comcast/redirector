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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.cache.newzkstackscache.IServiceDiscovery;
import com.comcast.redirector.dataaccess.cache.newzkstackscache.NonCachedServiceDiscovery;
import com.comcast.redirector.dataaccess.cache.newzkstackscache.ServiceDiscovery;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NewZkStacksCache implements IStacksCache {
    private static final Logger log = LoggerFactory.getLogger(NewZkStacksCache.class);
    private String stackPathPrefix;

    private IHostGetter hostGetter;

    private List<ICacheListener> listeners = new CopyOnWriteArrayList<>();

    private IServiceDiscovery serviceDiscovery;

    public NewZkStacksCache(CuratorFramework curator, IDataSourceConnector connector,
                            ServiceDiscoveryHostDeserializer hostsSerializer,
                            boolean cacheHosts,
                            String basePath,
                            Function<IServiceDiscovery, Boolean> initListeners) {
        log.info("starting NEW stacks cache");

        this.stackPathPrefix = getStackPathPrefix(basePath);

        this.serviceDiscovery = cacheHosts ?
                new ServiceDiscovery(hostsSerializer, connector, curator, this::notifyListeners) :
                new NonCachedServiceDiscovery(hostsSerializer, connector, this::notifyListeners);

        this.hostGetter = new HostGetter(serviceDiscovery);

        if (initListeners != null) {
            initListeners.apply(serviceDiscovery);
        }

    }

    private String getStackPathPrefix(String dataSourceBasePath) {
        return PathHelper.getPathHelper(EntityType.STACK, dataSourceBasePath).getPath();
    }

    private String getAbsolutePath(String path) {
        return stackPathPrefix + path;
    }

    @Override
    public void waitForStackCacheAvailability() throws InterruptedException {
        // NO-OP
    }

    @Override
    public void addCacheListener(ICacheListener listener) {
        listeners.add(listener);
    }

    @Override
    public Set<XreStackPath> getAllStackPaths() {
        return serviceDiscovery.getAllStackPaths();
    }

    @Override
    public List<String> getAppVersionsDeployedOnStack(String stack, String appName) {
        return serviceDiscovery.getAppVersionsDeployedOnStack(stack, appName);
    }

    // TODO: implement custom discovery. We do need caches since we have offline mode
    void notifyListeners() {
        log.info("Stacks changes observed");
        listeners.forEach(ICacheListener::onChanged);
    }

    @Override
    public List<HostIPs> getHostsRunningOnStack(String stack, String appName) {
        try {
            return serviceDiscovery.getPathsForStackAndApp(stack, appName).stream()
                .flatMap(path -> getHostsRunningAppVersionOnStack(path).stream())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("failed to get app versions", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<HostIPs> getHostsRunningAppVersionOnStack(XreStackPath path) { // TODO: make it return list, make it get XreStackPath
        try {
            return hostGetter.getHosts(path);
        } catch (DataSourceConnectorException e) {
            log.error("method=getHostsRunningAppVersionOnStack", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
        return hostGetter.getHostsCount(path);
    }

    @Override
    public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
        return getHostsRunningAppVersionOnStack(path).get(index);
    }

    private class HostGetter implements IHostGetter {
        private IServiceDiscovery serviceDiscovery;

        HostGetter(IServiceDiscovery serviceDiscovery) {
            this.serviceDiscovery = serviceDiscovery;
        }

        @Override
        public List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException {
            return serviceDiscovery.getHosts(path);
        }

        @Override
        public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
            return serviceDiscovery.getHostsCount(path);
        }

        @Override
        public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
            return serviceDiscovery.getHostByIndex(path, index);
        }
    }
}
