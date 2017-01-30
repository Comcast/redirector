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

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.cache.IPathChildrenCacheWrapper;
import com.comcast.redirector.dataaccess.cache.PathChilderCacheListenerWrapper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;

import java.util.List;

public class ListDAO<T> extends BaseListDAO<T> implements IListDAO<T> {
    private IPathHelper pathHelper;

    public ListDAO(Class<T> clazz,
                   Serializer marshalling,
                   IDataSourceConnector connector,
                   IPathHelper pathHelper,
                   boolean isCompressed,
                   boolean useCache) {
        super(clazz, marshalling, connector, isCompressed, useCache);
        this.pathHelper = pathHelper;
    }

    @Override
    public List<T> getAll() {
        try {
            return getAll(getCache());
        } catch (DataSourceConnectorException e){
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public List<String> getAllIDs() {
        try {
            return getAllIDs(getCache());
        } catch (DataSourceConnectorException e){
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public T getById(String id) {
        try {
            return getByPath(pathHelper.getPath(id), getCache());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void saveById(T data, String id) throws SerializerException {
        try {
            saveByPath(data, pathHelper.getPath(id), getCache());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            super.deleteByPath(pathHelper.getPath(id), getCache());
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    @Override
    public void addCacheListener(ICacheListener listener) {
        try {
            if(useCache) {
                getCache().addListener(new PathChilderCacheListenerWrapper(listener));
            }
        } catch (DataSourceConnectorException e) {
            throw new RedirectorDataSourceException(e);
        }
    }

    private synchronized IPathChildrenCacheWrapper getCache() throws DataSourceConnectorException {
        String path = pathHelper.getPath();
        return getCacheByKeyAndPath(path, path);
    }
}
