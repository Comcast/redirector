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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.NoNodeInZookeeperException;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.dataaccess.cache.ZKPathHelperConstants.STACK_ELEMENTS_COUNT;
import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class ZKStacksCache implements IStacksCache {
    private static final Logger log = LoggerFactory.getLogger(ZKStacksCache.class);

    private TreeCache stacksCache;

    private volatile boolean stacksCacheAvailable = false;
    private final Lock lock = new ReentrantLock();
    private final Condition stacksCacheAvailability = lock.newCondition();

    private String stackPathPrefix;

    private IHostGetter hostGetter;
    private ServiceDiscoveryHostDeserializer hostsSerializer;

    public ZKStacksCache(CuratorFramework curator, IDataSourceConnector connector,
                         ServiceDiscoveryHostDeserializer hostsSerializer,
                         boolean cacheHosts, String basePath) {
        log.info("starting stacks cache");

        this.stackPathPrefix = getStackPathPrefix(basePath);

        this.hostGetter = cacheHosts ? new CachedHostsGetter() : new ZookeeperHostGetter(connector);
        this.hostsSerializer = hostsSerializer;

        stacksCache = TreeCache.newBuilder(curator, basePath + ZKPathHelperConstants.STACKS_PATH)
            .setCacheData(cacheHosts)
            .setMaxDepth(cacheHosts ? STACK_ELEMENTS_COUNT + 1 : STACK_ELEMENTS_COUNT)
            .build();  //(APPDS-1904-related) please be careful: listener somehow may be initialized after cache population, so the INITIALIZE event may be missed
        stacksCache.getListenable().addListener(new StackCacheListener());//this is a curator problem, so now ZooKeeper
        // is connected _after_ cache start, which prevents initialize to happen too early
        try {
            stacksCache.start();
        } catch (Exception e) {
            log.error("Failed to start stacks cache", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitForStackCacheAvailability() throws InterruptedException {
        lock.lock();
        try {
            while ((!stacksCacheAvailable)) {
                log.warn("Stacks cache is NOT available, waiting for it...");
                if (! stacksCacheAvailability.await(3, TimeUnit.MINUTES)) {
                    throw new InterruptedException("failed to wait for cache");
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Set<XreStackPath> getAllStackPaths() {
        try {
            return getStackPaths().stream()
                .map(this::cutStackPrefixFromFullPath)
                .map(XreStackPath::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    private List<String> getStackPaths() throws DataSourceConnectorException {
        List<String> paths = Collections.singletonList(stackPathPrefix);
        for (int i = 0; i < STACK_ELEMENTS_COUNT; i++) {
            List<String> childrenPaths = new ArrayList<>();
            for (String path : paths) {
                childrenPaths.addAll(getStacksChildrenForPath(path));
            }
            paths = childrenPaths;
        }
        return paths;
    }

    @Override
    public List<String> getAppVersionsDeployedOnStack(String stack, String appName) {
        return doGetStringXrePathStream(stack, path -> true, path -> path.endsWith(appName))
            .map(path -> path.replaceFirst(stack + "/", ""))
            .map(path -> path.substring(0, path.lastIndexOf("/")))
            .collect(Collectors.toList());
    }

    @Override
    public List<HostIPs> getHostsRunningOnStack(String stack, String appName) {
        return doGetHosts(stack, path -> true, path -> path.endsWith(appName));
    }

    private Stream<String> doGetStringXrePathStream(String stackPath, Predicate<String> versionFilter, Predicate<String> appNameFilter) {
        return getStacksChildrenForPath(getAbsolutePath(stackPath)).stream()
            .filter(versionFilter)
            .map(this::getStacksChildrenForPath)
            .flatMap(Collection::stream)
            .filter(appNameFilter)
            .map(path -> path.replaceFirst(stackPathPrefix, ""));
    }

    private List<HostIPs> doGetHosts(String stackPath, Predicate<String> versionFilter, Predicate<String> appNameFilter) {
        return doGetStringXrePathStream(stackPath, versionFilter, appNameFilter)
            .map(XreStackPath::new)
            .map(this::getHosts)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private Collection<HostIPs> getHosts(XreStackPath path) {
        try {
            return hostGetter.getHosts(path);
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public List<HostIPs> getHostsRunningAppVersionOnStack(XreStackPath xreStackPath) {
        return doGetHosts(xreStackPath.getStackOnlyPath(),
            path -> path.endsWith(xreStackPath.getFlavor()),
            path -> path.endsWith(xreStackPath.getServiceName()));
    }

    @Override
    public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
        return hostGetter.getHostsCount(path);
    }

    @Override
    public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
        return hostGetter.getHostByIndex(path, index);
    }

    private List<String> getStacksChildrenForPath(String path) {
        Map<String, ChildData> pathsMap = stacksCache.getCurrentChildren(path);
        List<String> paths;
        if (pathsMap != null) {
            paths = pathsMap.values().stream()
                .map(ChildData::getPath)
                .collect(Collectors.toList());
        } else {
            paths = Collections.emptyList();
        }

        return paths;
    }

    private Collection<String> getRawHostsForPath(String path) {
        Map<String, ChildData> hostsData = stacksCache.getCurrentChildren(path);
        Collection<String> hosts;

        if (hostsData != null) {
            hosts = Collections2.transform(hostsData.values(), input -> new String(input.getData(), UTF8_CHARSET));
        } else {
            hosts = Collections.emptyList();
        }
        return hosts;
    }

    private String getRawHostData(String path) {
        ChildData data = stacksCache.getCurrentData(path);
        return data != null && data.getData() != null ? new String(data.getData(), UTF8_CHARSET) : StringUtils.EMPTY;
    }

    @Override
    public void addCacheListener(ICacheListener listener) {
        log.info("Adding cache listener");
        stacksCache.getListenable().addListener(new StackCacheListener(listener));
    }

    @Override
    public String toString() {
        return "Zookeeper stack cache";
    }

    private class StackCacheListener implements TreeCacheListener {
        private Optional<ICacheListener> listener;

        private StackCacheListener(){
            this(null);
        }

        private StackCacheListener(ICacheListener listener){
            this.listener = Optional.ofNullable(listener);
        }

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            try {
                lock.lock();
                if (!stacksCacheAvailable) {
                    log.info("Stack cache is NOT initialized, event intercepted: " + event.getType().name());
                }
                if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
                    stacksCacheAvailable = true;
                    stacksCacheAvailability.signalAll();
                    log.info("stacks cache is initialized");
                } else if (listener.isPresent() && stacksCacheAvailable && event.getType() != TreeCacheEvent.Type.CONNECTION_LOST
                    && event.getType() != TreeCacheEvent.Type.CONNECTION_SUSPENDED) {
                    listener.get().onChanged(event);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private class ZookeeperHostGetter implements IHostGetter {
        private IDataSourceConnector connector;

        ZookeeperHostGetter(IDataSourceConnector connector) {
            this.connector = connector;
        }

        @Override
        public List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException {
            List<HostIPs> result = new ArrayList<>();
            String fullPath = getAbsolutePath(path.getPath());
            if (connector.isPathExists(fullPath)) {
                for (String host : connector.getChildren(fullPath)) {
                    String hostPath = fullPath + DELIMETER + host;
                    try {
                        result.add(getHost(hostPath));
                    } catch (NoNodeInZookeeperException e) {
                        log.warn("Failed to load data for host path {} : node is absent", hostPath);
                    }
                }
            } else {
                log.info("Path {} was queried but does not exists", fullPath);
            }
            log.info("Retrieved {} hosts for path {} from zookeeper", result.size(), fullPath);
            return result;
        }

        @Override
        public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
            String fullPath = getAbsolutePath(path.getPath());
            if (connector.isPathExists(fullPath)) {
                return connector.getChildren(fullPath).size();
            } else {
                log.info("Path {} was queried but does not exists", fullPath);
            }
            return 0;
        }

        @Override
        public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
            String absoluteParentPathForHosts = getAbsolutePath(path.getPath());
            String hostName = this.getRelativeSubNodePaths(absoluteParentPathForHosts).get(index);
            String absoluteHostPath = absoluteParentPathForHosts + DELIMETER + hostName;
            return this.getHost(absoluteHostPath);
        }

        private List<String> getRelativeSubNodePaths(String path) throws DataSourceConnectorException {
            return connector.getChildren(path).stream()
                .map(ZKStacksCache::extractLastNodeFromPath)
                .collect(Collectors.toList());
        }

        private HostIPs getHost(String path) throws DataSourceConnectorException {
            String data = new String(connector.getData(path), UTF8_CHARSET);
            try {
                return hostsSerializer.deserialize(data);
            } catch (SerializerException e) {
                throw new DataSourceConnectorException("failed to deserialize " + data + " for path " + path, e);
            }
        }

    }

    private class CachedHostsGetter implements IHostGetter {

        @Override
        public List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException {
            List<HostIPs> result = new ArrayList<>();
            Collection<String> hostNodes = getRawHostsForPath(getAbsolutePath(path.getPath()));
            for (String host : hostNodes) {
                try {
                    result.add(hostsSerializer.deserialize(host));
                } catch (SerializerException e) {
                    log.warn("Failed to deserialize data for host path {} - {} ", path.getPath(), host);
                }
            }

            return result;
        }

        @Override
        public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
            return getRawHostsForPath(getAbsolutePath(path.getPath())).size();
        }

        private HostIPs getHost(String path) throws DataSourceConnectorException {
            String data = getRawHostData(path);
            try {
                return hostsSerializer.deserialize(data);
            } catch (SerializerException e) {
                throw new DataSourceConnectorException("failed to deserialize " + data + " for path " + path, e);
            }
        }


        private List<String> getRelativeSubNodePaths(String path) {
            return getStacksChildrenForPath(path).stream()
                .map(ZKStacksCache::extractLastNodeFromPath)
                .collect(Collectors.toList());
        }

        @Override
        public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
            String absoluteParentPathForHosts = getAbsolutePath(path.getPath());
            String hostName = this.getRelativeSubNodePaths(absoluteParentPathForHosts).get(index);
            String absoluteHostPath = absoluteParentPathForHosts + DELIMETER + hostName;
            return this.getHost(absoluteHostPath);
        }
    }

    private String getStackPathPrefix(String dataSourceBasePath) {
        return PathHelper.getPathHelper(EntityType.STACK, dataSourceBasePath).getPath();
    }

    private String getAbsolutePath(String path) {
        return stackPathPrefix + path;
    }

    private static String extractLastNodeFromPath(String path) {
        return path.substring(path.lastIndexOf(DELIMETER) + 1);
    }

    private String cutStackPrefixFromFullPath(String path) {
        return path.substring(stackPathPrefix.length());
    }
}
