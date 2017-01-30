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

package com.comcast.redirector.dataaccess.cache.newzkstackscache;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.NoConnectionToDataSourceException;

import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class NonCachedServiceDiscovery extends AbstractServiceDiscovery {

    private static final ThreadLocalLogger log = new ThreadLocalLogger(NonCachedServiceDiscovery.class);

    private Set<XreStackPath> stackPaths = new LinkedHashSet<>();

    public NonCachedServiceDiscovery(ServiceDiscoveryHostDeserializer hostsSerializer, IDataSourceConnector connector, INotifier listenersNotifier) {
        super(connector, hostsSerializer, listenersNotifier);
    }

    @Override
    void applyChangesToHostCaches(Set<XreStackPath> allStackPaths) {
        if (allStackPaths != null) {
            this.stackPaths = allStackPaths;
            log.info("DiscoveredStacksCount=" + allStackPaths.size() + " and AppliedStacksCount=" + stackPaths.size());
        }
    }

    @Override
    public List<HostIPs> getHosts(XreStackPath path) throws DataSourceConnectorException {

        if (!connector.isConnected()) {
            throw new NoConnectionToDataSourceException("Failed to get hosts for path=" + path.getPath());
        }

        if (!stackPaths.contains(path)) {
            return Collections.emptyList();
        }

        List<HostIPs> hostsData = new ArrayList<>();
        try {
            Collection<String> children = connector.getChildren(stackPathPrefix + path.getPath()).stream().map(child -> (stackPathPrefix + path + "/" + child)).collect(Collectors.toList());
            for (String childPath : children) {
                byte[] hostData = connector.getData(childPath);
                if (hostData != null && hostData.length > 0) {
                    hostsData.add(hostsSerializer.deserialize(new String(hostData, UTF8_CHARSET)));
                }
            }
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get hosts for path="+path.getPath() + " due to connection problems {}", e.getMessage());
        } catch (SerializerException e) {
            log.error("Failed to get hosts for path="+path.getPath()+" due to serialization error {}", e.getMessage());
        }

        log.info("Retrieved {} hosts for path {} from zookeeper", hostsData.size(), path.getPath());

        return hostsData;
    }

    @Override
    public int getHostsCount(XreStackPath path) throws DataSourceConnectorException {

        if (!connector.isConnected()) {
            throw new NoConnectionToDataSourceException("Failed to get hosts for path=" + path.getPath());
        }

        if (!stackPaths.contains(path)) {
            return 0;
        }

        int hostsCount = 0;
        try {
            hostsCount = connector.getChildren(stackPathPrefix + path.getPath()).size();
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get hosts count for path=" + path.getPath(), e.getMessage());
        }

        return hostsCount;
    }

    @Override
    public HostIPs getHostByIndex(XreStackPath path, int index) throws DataSourceConnectorException {

        if (!connector.isConnected()) {
            throw new NoConnectionToDataSourceException("Failed to get hosts for path=" + path.getPath());
        }

        if (!stackPaths.contains(path)) {
            return null;
        }

        try {
            List<String> children = connector.getChildren(stackPathPrefix + path.getPath()).stream().map(child -> (stackPathPrefix + path + "/" + child)).collect(Collectors.toList());
            if (children.size() > index) {
                byte[] hostData = connector.getData(children.get(index));
                if (hostData != null && hostData.length > 0) {
                    return hostsSerializer.deserialize(new String(hostData, UTF8_CHARSET));
                }
            }
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get hosts for path {} due to connection problems {}", path.getPath(), e.getMessage());
        } catch (SerializerException e) {
            log.error("Failed to get hosts for path {} due to serialization error{}", path.getPath(), e.getMessage());
        }

        return null;
    }

    public Set<XreStackPath> getAllStackPaths() {
        return new LinkedHashSet<>(stackPaths);
    }

    public List<String> getAppVersionsDeployedOnStack(String stack, String appName) {
        return stackPaths.stream()
                .filter(xreStackPath -> xreStackPath.getServiceName().equals(appName))
                .filter(xreStackPath -> xreStackPath.getStackOnlyPath().equals(stack))
                .map(XreStackPath::getFlavor)
                .collect(Collectors.toList());
    }

    public List<XreStackPath> getPathsForStackAndApp(String stack, String appName) {
        return stackPaths.stream()
                .filter(xreStackPath -> xreStackPath.getServiceName().equals(appName))
                .filter(xreStackPath -> xreStackPath.getStackOnlyPath().equals(stack))
                .collect(Collectors.toList());
    }
}
