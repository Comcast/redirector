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
import com.comcast.redirector.dataaccess.cache.PathChilderCacheListenerWrapper;
import com.comcast.redirector.dataaccess.cache.IPathChildrenCacheWrapper;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.common.serializers.Serializer;

import java.util.List;
import java.util.Map;

public class ListServiceDAO<T> extends BaseListDAO<T> implements IListServiceDAO<T> {
    private IPathHelper pathHelper;

    public ListServiceDAO(Class<T> clazz,
                          Serializer marshalling,
                          IDataSourceConnector connector,
                          IPathHelper pathHelper,
                          boolean isCompressed,
                          boolean useCache) {
        super(clazz, marshalling, connector, isCompressed, useCache);
        this.pathHelper = pathHelper;
    }

    @Override
    public List<T> getAll(String serviceName) {
        try {
            return getAll(getCache(serviceName));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public Map<String, T> getAllInMap(String serviceName) {
        try {
            return getAllInMap(getCache(serviceName));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public T getById(String serviceName, String id) {
        try {
            return getByPath(pathHelper.getPathByService(serviceName, id), getCache(serviceName));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void saveById(T data, String serviceName, String id) throws SerializerException {
        try {
            saveByPath(data, pathHelper.getPathByService(serviceName, id), getCache(serviceName));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void deleteById(String serviceName, String id) {
        try {
            super.deleteByPath(pathHelper.getPathByService(serviceName, id), getCache(serviceName));
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void addCacheListener(String serviceName, ICacheListener listener) {
        try {
            if(useCache) {
                getCache(serviceName).addListener(new PathChilderCacheListenerWrapper(listener));
            }
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    private IPathChildrenCacheWrapper getCache(String serviceName) throws DataSourceConnectorException {
        String path = pathHelper.getPathByService(serviceName);
        return getCacheByKeyAndPath(serviceName, path);
    }
}
