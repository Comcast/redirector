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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.cache.IPathChildrenCacheWrapper;
import com.comcast.redirector.common.serializers.Serializer;
import org.apache.curator.utils.ThreadUtils;

import java.io.Closeable;
import java.util.*;

import static com.comcast.redirector.common.function.Wrappers.unchecked;
import static java.util.stream.Collectors.toList;
import static org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode.BUILD_INITIAL_CACHE;

abstract class BaseListDAO<T> extends BaseDAO<T> implements ICacheableDAO {
    private Map<String, IPathChildrenCacheWrapper> cache = new HashMap<>();

    BaseListDAO(Class<T> clazz,
                       Serializer marshalling,
                       IDataSourceConnector connector,
                       boolean compressed, boolean useCache) {
        super(clazz, marshalling, connector, compressed, useCache);
    }

    @Override
    public synchronized void rebuildCache() throws DataSourceConnectorException {
        for (IPathChildrenCacheWrapper each : cache.values()) {
            each.rebuild();
        }
    }

    protected List<T> getAll(IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        Map<String, byte[]> map = cacheWrapper.getNodeIdToDataMap();

        List<T> items = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            T item = deserializeOrReturnNull(entry.getValue());
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    protected List<String> getAllIDs(IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        Map<String, byte[]> map = cacheWrapper.getNodeIdToDataMap();
        return map.entrySet().stream().map(node -> node.getKey()).collect(toList());
    }


    Map<String, T> getAllInMap(IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        Map<String, byte[]> map = cacheWrapper.getNodeIdToDataMap();

        Map<String, T> items = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            T item = deserializeOrReturnNull(entry.getValue());
            if (item != null) {
                items.put(entry.getKey(), item);
            }
        }

        return items;
    }

    T getByPath(String path, IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        byte[] data = cacheWrapper.getCurrentData(path);
        return deserializeOrReturnNull(data);
    }

    void saveByPath(T data, String path, IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException, SerializerException {
        save(serialize(data), path);
        rebuildCacheNode(cacheWrapper, path);
    }

    void deleteByPath(String path, IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        super.deleteByPath(path);
        rebuildCache(cacheWrapper);
    }

    private void rebuildCache(IPathChildrenCacheWrapper cacheWrapper) throws DataSourceConnectorException {
        cacheWrapper.rebuild();
    }

    private void rebuildCacheNode(IPathChildrenCacheWrapper cacheWrapper, String path) throws DataSourceConnectorException {
        cacheWrapper.rebuildNode(path);
    }

    synchronized IPathChildrenCacheWrapper getCacheByKeyAndPath(String key, String path) throws DataSourceConnectorException {
        if (cache.get(key) == null) {
            IPathChildrenCacheWrapper aCache;
            if (compressed) {
                aCache = connector.newPathChildrenCacheWrapper(path,
                    true, ThreadUtils.newThreadFactory(clazz.getSimpleName() + " cache"), useCache);
            } else {
                aCache = connector
                        .newPathChildrenCacheWrapper(path, useCache);
            }
            aCache.start(BUILD_INITIAL_CACHE);
            cache.put(key, aCache);
        }
        return cache.get(key);
    }

    @Override
    public void close() {
        cache.values().forEach(unchecked(Closeable::close));
    }
}
