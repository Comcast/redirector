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
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.common.serializers.Serializer;

import java.util.LinkedHashSet;
import java.util.Set;

public class TransactionalDAO implements ITransactionalDAO {
    private IDataSourceConnector connector;
    private BaseDAO.BaseOperations operations;
    private IRegisteringDAOFactory daoFactory;

    public TransactionalDAO(IDataSourceConnector connector, Serializer serializer, IRegisteringDAOFactory daoFactory) {
        this.connector = connector;
        this.daoFactory = daoFactory;

        operations = new BaseDAO.BaseOperations(serializer, connector);
    }

    @Override
    public ITransaction beginTransaction() {
        return new Transaction();
    }

    private class Transaction implements ITransaction {
        private IDataSourceConnector.Transaction connectorTransaction;
        private Set<EntityType> entities = new LinkedHashSet<>();

        Transaction() {
            this.connectorTransaction = connector.createTransaction();
        }

        @Override
        public <T> void save(T data, EntityType entityType, String serviceName, String id) throws SerializerException {
            try {
                connectorTransaction.save(
                        serializeOrThrowException(data),
                        PathHelper.getPathHelper(entityType, connector.getBasePath()).getPathByService(serviceName, id));
                registerEntityType(entityType);
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        @Override
        public <T> void save(T data, EntityType entityType, String serviceName) throws SerializerException {
            try {
                connectorTransaction.save(
                        serializeOrThrowException(data),
                        PathHelper.getPathHelper(entityType, connector.getBasePath()).getPathByService(serviceName));
                registerEntityType(entityType);
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        @Override
        public <T> void operationByActionType(T data, EntityType entityType, String serviceName, String id, ActionType actionType) throws SerializerException {
            if (actionType == ActionType.DELETE) {
                delete(entityType, serviceName, id);
            } else {
                save(data, entityType, serviceName, id);
            }
            registerEntityType(entityType);
        }

        @Override
        public void incVersion(EntityType entityType, String serviceName) {
            try {
                connectorTransaction.save("", PathHelper.getPathHelper(entityType, connector.getBasePath()).getPathByService(serviceName));
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        @Override
        public void delete(EntityType entityType, String serviceName, String id) {
            try {
                connectorTransaction.delete(PathHelper.getPathHelper(entityType, connector.getBasePath()).getPathByService(serviceName, id));
                registerEntityType(entityType);
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        @Override
        public void commit() {
            try {
                connectorTransaction.commit();
                rebuildCaches(); // TODO: optimize, for example rebuild only for specific services
            } catch (DataSourceConnectorException e) {
                throw new RedirectorDataSourceException(e);
            }
        }

        private <T> String serializeOrThrowException(T data) throws DataSourceConnectorException, SerializerException {
            String rawData = operations.serialize(data);
            if (rawData == null) {
                throw new DataSourceConnectorException("Failed to serialize data " + data);
            }
            return rawData;
        }

        private void registerEntityType(EntityType entityType) {
            entities.add(entityType);
        }

        private void rebuildCaches() throws DataSourceConnectorException {
            for (EntityType registeredEntity : entities) {
                ICacheableDAO dao = daoFactory.getRegisteredCacheableDAO(registeredEntity);
                if (dao != null) {
                    dao.rebuildCache();
                }
            }
        }
    }
}
