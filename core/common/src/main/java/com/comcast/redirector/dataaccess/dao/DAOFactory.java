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

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;

import java.util.HashMap;
import java.util.Map;

public class DAOFactory implements IDAOFactory, IRegisteringDAOFactory {
    private Serializer serializer;
    private IDataSourceConnector connector; // TODO: rename to connector
    private boolean useCache = true;
    private Map<EntityType, ICacheableDAO> cacheableDAOs = new HashMap<>();

    public DAOFactory(IDataSourceConnector connector,
                      Serializer serializer) {
        this.connector = connector;
        this.serializer = serializer;
    }

    public DAOFactory(IDataSourceConnector connector,
                       boolean useCache,
                       Serializer serializer) {
        this(connector, serializer);
        this.useCache = useCache;
    }

    @Override
    public <T> IListDAO<T> getListDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed) {
        ListDAO dao = new ListDAO<>(modelClass, serializer, connector, getHelper(entityType), isCompressed, useCache);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public <T> IListDAO<T> getNamespacedListsDAO(EntityType entityType, boolean isCompressed) {
        ListDAO dao = new NamespacedListsDAO(serializer, connector, getHelper(entityType), isCompressed, useCache);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public <T> ISimpleDAO<T> getSimpleDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed) {
        SimpleDAO dao = new SimpleDAO<>(modelClass, serializer, connector, getHelper(entityType), isCompressed, useCache);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed, boolean useCache, boolean useCacheWhenNotConnectedToDataSource) {
        SimpleServiceDAO dao = new SimpleServiceDAO<>(modelClass, serializer, connector,
                getHelper(entityType), isCompressed, useCache, useCacheWhenNotConnectedToDataSource);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed, boolean useCache) {
        SimpleServiceDAO dao = new SimpleServiceDAO<>(modelClass, serializer, connector, getHelper(entityType), isCompressed, useCache);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed) {
        return getSimpleServiceDAO(modelClass, entityType, isCompressed, useCache);
    }

    @Override
    public <T> IListServiceDAO<T> getListServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed) {
        ListServiceDAO dao = new ListServiceDAO<>(modelClass, serializer, connector, getHelper(entityType), isCompressed, useCache);
        registerCacheableDAO(entityType, dao);
        return dao;
    }

    @Override
    public IEmptyObjectDAO getEmptyObjectDAO(EntityType entityType) {
        return new EmptyObjectDAO(connector, getHelper(entityType));
    }

    @Override
    public INodeVersionDAO getNodeVersionDAO() {
        return new NodeVersionDAO(connector);
    }

    @Override
    public IStacksDAO createStacksDAO() {
        return new StacksDAO(connector);
    }

    @Override
    public ICacheableDAO getRegisteredCacheableDAO(EntityType entityType) {
        return cacheableDAOs.get(entityType);
    }

    private void registerCacheableDAO(EntityType entityType, ICacheableDAO dao) {
        cacheableDAOs.put(entityType, dao);
    }

    private IPathHelper getHelper(EntityType entityType) {
        return PathHelper.getPathHelper(entityType, connector.getBasePath());
    }
}
