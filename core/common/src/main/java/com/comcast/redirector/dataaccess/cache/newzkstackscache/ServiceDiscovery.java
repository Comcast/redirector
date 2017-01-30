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

package com.comcast.redirector.dataaccess.cache.newzkstackscache;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class ServiceDiscovery extends AbstractServiceDiscovery { // TODO: use this class for getting data about stacks and hosts

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CuratorFramework curator;

    private Map<XreStackPath, PathChildrenCache> hostCaches = new HashMap<>(); // TODO: read-write lock

    public ServiceDiscovery(ServiceDiscoveryHostDeserializer hostsSerializer, IDataSourceConnector connector, CuratorFramework curator, INotifier listenersNotifier) {
        super(connector, hostsSerializer, listenersNotifier);
        this.curator = curator;
    }

    @Override
    public List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException {
        if (!hostCaches.containsKey(path)) {
            return Collections.emptyList();
        }

        List<HostIPs> result = hostCaches.get(path).getCurrentData().stream()
                .map(childData -> {
                    String data = new String(childData.getData(), UTF8_CHARSET);
                    try {
                        return hostsSerializer.deserialize(data);
                    } catch (SerializerException e) {
                        log.error("failed to de-serialize " + data, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Retrieved {} hosts for path {} from zookeeper", result.size(), path.getPath());
        return result;
    }

    @Override
    public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {
        if (!hostCaches.containsKey(path)) {
            return 0;
        } else {
            return hostCaches.get(path).getCurrentData().size();
        }
    }

    @Override
    public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {
        ChildData hostData = hostCaches.get(path).getCurrentData().get(index);
        if (hostData != null) {
            String data = new String(hostData.getData(), UTF8_CHARSET);
            try {
                return hostsSerializer.deserialize(data);
            } catch (SerializerException e) {
                log.error("failed to de-serialize " + data, e.getMessage());
            }
        }

        return null;
    }

    public Set<XreStackPath> getAllStackPaths() {
        return new LinkedHashSet<>(hostCaches.keySet());
    }

    public List<String> getAppVersionsDeployedOnStack(String stack, String appName) {
        return hostCaches.keySet().stream()
            .filter(xreStackPath -> xreStackPath.getServiceName().equals(appName))
            .filter(xreStackPath -> xreStackPath.getStackOnlyPath().equals(stack))
            .map(XreStackPath::getFlavor)
            .collect(Collectors.toList());
    }

    public List<XreStackPath> getPathsForStackAndApp(String stack, String appName) {
        return hostCaches.keySet().stream()
            .filter(xreStackPath -> xreStackPath.getServiceName().equals(appName))
            .filter(xreStackPath -> xreStackPath.getStackOnlyPath().equals(stack))
            .collect(Collectors.toList());
    }

    private void closeCache(XreStackPath key) {
        try {
            hostCaches.get(key).close();
        } catch (IOException e) {
            log.error("failed to close cache for " + key, e.getMessage());
        }
    }

    void applyChangesToHostCaches(Set<XreStackPath> allStackPaths) {
        List<XreStackPath> keysToRemove = hostCaches.keySet().stream().filter(xreStackPath -> !allStackPaths.contains(xreStackPath)).collect(Collectors.toList());
        keysToRemove.forEach(key -> {
            closeCache(key);
            hostCaches.remove(key);
        });

        allStackPaths.stream()
            .filter(path -> ! hostCaches.keySet().contains(path))
            .forEach(xreStackPath -> {
                PathChildrenCache cache = new PathChildrenCache(curator, getAbsolutePath(xreStackPath.getPath()), true);
                cache.getListenable().addListener((client, event) -> listenersNotifier.notifyListeners());
                try {
                    cache.start();
                } catch (Exception e) {
                    log.error("Failed to start cache for discovered xreStackPath=" + xreStackPath.getPath(), e.getMessage());
                }

                hostCaches.put(xreStackPath, cache);
            });
        log.info("DiscoveredStacksCount=" + allStackPaths.size() + " and AppliedStacksCount=" + hostCaches.size());
    }

    private String getAbsolutePath(String path) {
        return stackPathPrefix + path;
    }
}
