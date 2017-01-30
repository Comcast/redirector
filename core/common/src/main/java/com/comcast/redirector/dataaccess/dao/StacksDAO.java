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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.cache.IStacksCache;
import com.comcast.redirector.dataaccess.client.*;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.dataaccess.cache.ZKPathHelperConstants.STACK_ELEMENTS_COUNT;
import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class StacksDAO implements IStacksDAO {
    private static final Logger log = LoggerFactory.getLogger(StacksDAO.class);

    private IStacksCache stacksCache;
    private StacksDeleter stacksDeleter;

    private String stackPathPrefix;

    public StacksDAO(IDataSourceConnector connector) {
        this.stacksCache = connector.getStacksCache();
        this.stacksDeleter = new StacksDeleter(connector);

        this.stackPathPrefix = getStackPathPrefix(connector.getBasePath());
    }

    @Override
    public Set<XreStackPath> getAllStackPaths() {
        return stacksCache.getAllStackPaths();
    }

    @Override
    public Set<XreStackPath> getAllStackPaths(final Set<String> applicationsToExclude)  {
        return getAllStackPaths().stream()
            .filter(filterByApplications(applicationsToExclude))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Predicate<XreStackPath> filterByApplications(Set<String> excludedApps) {
        return path -> ! excludedApps.contains(path.getServiceName());
    }

    @Override
    public Collection<HostIPs> getHosts(XreStackPath path) {
        return stacksCache.getHostsRunningAppVersionOnStack(path);
    }

    @Override
    public Collection<HostIPs> getHostsByStackOnlyPath(XreStackPath path) {
        return stacksCache.getHostsRunningOnStack(path.getStackOnlyPath(), path.getServiceName());
    }

    @Override
    public List<String> getFlavorsByStackOnlyPath(XreStackPath path) {
        return stacksCache.getAppVersionsDeployedOnStack(path.getStackOnlyPath(), path.getServiceName());
    }

    @Override
    public HostIPs getHostByIndex(XreStackPath path, int index) {
        try {
            return stacksCache.getHostByIndex(path, index);
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException("failed to get host by index", e);
        }
    }

    @Override
    public void deleteStackPath(XreStackPath xreStackPath){
        stacksDeleter.deleteStackPath(xreStackPath);
    }

    @Override
    public int getHostsCount(XreStackPath path) {
        try {
            return stacksCache.getHostsCount(path);
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException("failed to get hosts count", e);
        }
    }

    @Override
    public Set<String> getAllAppNamesRegisteredInStacks(){
        return getAllStackPaths().stream()
            .map(XreStackPath::getServiceName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void addCacheListener(ICacheListener listener){
        stacksCache.addCacheListener(listener);
    }

    private String getStackPathPrefix(String dataSourceBasePath) {
        return PathHelper.getPathHelper(EntityType.STACK, dataSourceBasePath).getPath();
    }

    private String getAbsolutePath(String path) {
        return stackPathPrefix + path;
    }

    private class StacksDeleter {
        private IDataSourceConnector connector;

        StacksDeleter(IDataSourceConnector connector) {
            this.connector = connector;
        }

        void deleteStackPath(XreStackPath xreStackPath) {
            try {
                String path = xreStackPath.getPath();
                String fullPath = getAbsolutePath(path);
                if (connector.isPathExists(fullPath)) {
                    tryDeleteNode(path);
                    deleteParentNodesIfTheyDontHaveChildren(xreStackPath);
                } else {
                    String errorMessage = String.format("Stack can't be deleted because path: \" %s \" does not exist", path);
                    log.error(errorMessage);
                    throw new RedirectorNoNodeInPathException(errorMessage);
                }
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        private void deleteParentNodesIfTheyDontHaveChildren(XreStackPath path) throws DataSourceConnectorException {
            String[] pathsToRemove = new String[] {path.getStackAndFlavorPath(), path.getStackOnlyPath(), path.getDataCenterPath()};
            for (String pathToRemove : pathsToRemove) {
                if (!tryDeleteNode(pathToRemove)) {
                    log.info("Stopped stack deletion on " + pathToRemove + " node since it has children");
                    break;
                }
            }
        }

        private boolean tryDeleteNode(String path) throws DataSourceConnectorException {
            String absolutePath = getAbsolutePath(path);
            if (connector.getChildren(absolutePath).isEmpty()) {
                connector.delete(absolutePath);
                return true;
            }
            return false;
        }
    }
}
