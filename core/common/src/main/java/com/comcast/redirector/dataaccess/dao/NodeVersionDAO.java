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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.comcast.redirector.common.logging.ErrorStates.DatastoreError;
import static com.comcast.redirector.common.logging.ErrorStates.DatastoreIsUnavailable;

/**
 * It is impossible to update version in zk without changing data.
 * Do not try to do saves , updates and deletes here, this class is only for get version operations
 */
public class NodeVersionDAO implements INodeVersionDAO {
    private static final Logger log = LoggerFactory.getLogger(NodeVersionDAO.class);
    private final Map<String, Long> versionsByApplicationMap = new LinkedHashMap<>();
    private long namespacedListsVersion = -1;

    private BaseDAO.BaseOperations operations;
    private IDataSourceConnector connector;

    public static final long NO_VERSION = -1L;

    public NodeVersionDAO(IDataSourceConnector connector) {
        this.connector = connector;
        this.operations = new BaseDAO.BaseOperations(null, connector);
    }

    @Override
    public long getStacksReloadNodeVersion(String serviceName) {
        if (!connector.isConnected()) {
            throw new ServiceUnavailableException("errorState=" + DatastoreIsUnavailable);
        }
        try {
            return operations.getVersion(PathHelper.getPathHelper(EntityType.STACKS_RELOAD, connector.getBasePath()).getPathByService(serviceName));
        } catch (DataSourceConnectorException e) {
            return NO_VERSION;
        }
    }

    @Override
    public long getModelChangedVersion(String serviceName) {
        if (!connector.isConnected()) {
            throw new ServiceUnavailableException("errorState=" + DatastoreIsUnavailable);
        }
        try {
            return getModelChangedVersionInternal(serviceName);
        } catch (DataSourceConnectorException e) {
            return NO_VERSION;
        }
    }

    @Override
    public long getNamespacedListsVersion() {
        if (!connector.isConnected()) {
            throw new ServiceUnavailableException("errorState=" + DatastoreIsUnavailable);
        }
        try {
            return getNamespacedListsVersionInternal();
        } catch (DataSourceConnectorException e) {
            return NO_VERSION;
        }
    }

    @Override
    public long getModelChangedVersion(String serviceName, boolean fallbackToMemory) {
        if (!connector.isConnected()) {
            if (fallbackToMemory) {
                if (versionsByApplicationMap.containsKey(serviceName)) {
                    return versionsByApplicationMap.get(serviceName);
                } else {
                    return NO_VERSION;
                }
            }  else {
                throw new ServiceUnavailableException("errorState="+ DatastoreIsUnavailable);
            }
        }
        try {
            return getModelChangedVersionInternal(serviceName);
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get model version failedMethod=getModelChangedVersion, errorState=" + DatastoreError, e.getMessage());
            return NO_VERSION;
        }
    }

    @Override
    public long getNamespacedListsVersion(boolean fallbackToMemory) {
        if (!connector.isConnected()) {
            if (fallbackToMemory) {
                if (namespacedListsVersion > 0) {
                    return namespacedListsVersion;
                } else {
                    return NO_VERSION;
                }
            }  else {
                throw new ServiceUnavailableException("errorState=" + DatastoreIsUnavailable);
            }
        }
        try {
            return getNamespacedListsVersionInternal();
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get model version failedMethod=getNamespacedListsVersion, errorState=" + DatastoreError, e.getMessage());
            return NO_VERSION;
        }
    }

    private long getModelChangedVersionInternal (String serviceName) throws DataSourceConnectorException {
        int version = operations.getVersion(PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath()).getPathByService(serviceName));
        versionsByApplicationMap.put(serviceName, (long) version);
        return version;
    }

    private long getNamespacedListsVersionInternal () throws DataSourceConnectorException {
        namespacedListsVersion = operations.getVersion(PathHelper.getPathHelper(EntityType.NAMESPACED_LIST, connector.getBasePath()).getPath());
        return namespacedListsVersion;
    }

    @Override
    public long getStacksVersion() {
        if (!connector.isConnected()) {
            throw new ServiceUnavailableException("errorState=" + DatastoreIsUnavailable);
        }
        try {
            return operations.getVersion(PathHelper.getPathHelper(EntityType.SERVICES_CHANGED, connector.getBasePath()).getPath());
        } catch (DataSourceConnectorException e) {
            return NO_VERSION;
        }
    }
}
