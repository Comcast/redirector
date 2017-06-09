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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.cache.INodeCacheWrapper;
import com.comcast.redirector.dataaccess.cache.NodeCacheListenerWrapper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.common.function.Wrappers.unchecked;

public class SimpleDAO<T> extends BaseDAO<T> implements ISimpleDAO<T>, ICacheableDAO {

    private IPathHelper pathHelper;
    private Map<String, INodeCacheWrapper> cache = new HashMap<>();

    public SimpleDAO(Class<T> clazz,
                     Serializer marshalling,
                     IDataSourceConnector connector,
                     IPathHelper pathHelper,
                     boolean compressed,
                     boolean useCache) {
        super(clazz, marshalling, connector, compressed, useCache);
        this.pathHelper = pathHelper;
    }

    @Override
    public T get() {
        try {
            return deserializeOrReturnNull(getCache().getCurrentData());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void save(T data) throws SerializerException {
        try {
            save(serialize(data), pathHelper.getPath());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void addCacheListener(ICacheListener listener) {
        try {
            if(useCache) {
                getCache().addListener(new NodeCacheListenerWrapper(listener));
            }
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public synchronized void rebuildCache() throws DataSourceConnectorException {
        getCache().rebuild();
    }

    private synchronized INodeCacheWrapper getCache() throws DataSourceConnectorException {
        String path = pathHelper.getPath();
        return getCacheByPath(path);
    }

    private synchronized INodeCacheWrapper getCacheByPath(String path) throws DataSourceConnectorException {
        if (cache.get(path) == null) {
            INodeCacheWrapper aCache;
            if (compressed) {
                aCache = connector.newNodeCacheWrapper(path, useCache, false, true);
            } else {
                aCache = connector
                        .newNodeCacheWrapper(path, useCache);
            }
            aCache.start(true);
            cache.put(path, aCache);
        }
        return cache.get(path);
    }

    @Override
    public void close() {
        cache.values().forEach(unchecked(Closeable::close));
    }
}
