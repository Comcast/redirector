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

import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.cache.NodeCacheListenerWrapper;
import com.comcast.redirector.dataaccess.cache.INodeCacheWrapper;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.common.serializers.Serializer;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.common.function.Wrappers.unchecked;

public class SimpleServiceDAO<T> extends BaseDAO<T> implements ISimpleServiceDAO<T>, ICacheableDAO {
    private IPathHelper pathHelper;
    private Map<String, INodeCacheWrapper> cache = new HashMap<>();

    SimpleServiceDAO(Class<T> clazz,
                            Serializer marshalling,
                            IDataSourceConnector connector,
                            IPathHelper pathHelper,
                            boolean isCompressed,
                            boolean useCache,
                            boolean useCacheWhenNotConnectedToDataSource) {
        super(clazz, marshalling, connector, isCompressed, useCache, useCacheWhenNotConnectedToDataSource);
        this.pathHelper = pathHelper;
    }

    public SimpleServiceDAO(Class<T> clazz,
                            Serializer marshalling,
                            IDataSourceConnector zookeeperConnector,
                            IPathHelper pathHelper,
                            boolean isCompressed,
                            boolean useCache) {
        this(clazz, marshalling, zookeeperConnector, pathHelper, isCompressed, useCache, false);
    }

    @Override
    public T get(String serviceName) {
        try {
            return deserializeOrReturnNull(getCache(serviceName).getCurrentData());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void save(T data, String serviceName) throws SerializerException {
        try {
            save(serialize(data), pathHelper.getPathByService(serviceName));
            rebuildCache(serviceName);
        } catch (DataSourceConnectorException e){
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void delete(String serviceName) {
        try {
            deleteByPath(pathHelper.getPathByService(serviceName));
            rebuildCache(serviceName);
        } catch (DataSourceConnectorException e){
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public int getObjectVersion(String serviceName) {
        try {
            return getCache(serviceName).getCurrentDataVersion();
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public synchronized void rebuildCache() {
        cache.values().forEach(INodeCacheWrapper::rebuild);
    }

    @Override
    public void addCacheListener(String serviceName, ICacheListener listener){
        try {
            if(useCache) {
                getCache(serviceName).addListener(new NodeCacheListenerWrapper(listener));
            }
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(String.format("Failed to add listener for %s service",  serviceName), e);
        }
    }

    // TODO: move 2 methods below into base class
    private void rebuildCache(String serviceName) throws DataSourceConnectorException {
        getCache(serviceName).rebuild();
    }

    private String getPathForCache(String serviceName) {
        return pathHelper.getPathByService(serviceName);
    }

    protected synchronized INodeCacheWrapper getCache(String serviceName) throws DataSourceConnectorException {
        if (cache.get(serviceName) == null) {
            String path = getPathForCache(serviceName);
            INodeCacheWrapper aCache;
            if (compressed) {
                aCache = connector.newNodeCacheWrapper(path, useCache, useCacheWhenNotConnectedToDataSource, true);
            } else {
                aCache = connector
                        .newNodeCacheWrapper(path, useCache);
            }

            aCache.start(false);
            cache.put(serviceName, aCache);
        }
        return cache.get(serviceName);
    }

    @Override
    public void close() {
        cache.values().forEach(unchecked(Closeable::close));
    }
}
