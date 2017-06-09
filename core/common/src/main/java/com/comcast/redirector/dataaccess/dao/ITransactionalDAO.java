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

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;

/**
 * DAO allows to make set of operations in single transaction. When client code needs such a behavior
 * it should get new transaction be calling {@link #beginTransaction()} method and invoke methods
 * of {@link ITransaction} instance to perform necessary modifications. Once all modifications are called
 * {@link ITransaction#commit()} should be invoked.
 */
public interface ITransactionalDAO {
    ITransaction beginTransaction();

    /**
     * Encapsulate operations made in a particular transaction. Due to operations are done on objects of different types
     * methods of this interface are generic. So client code will specify type of entity and model class type for particular
     * operation.
     */
    interface ITransaction {
        <T> void save(T data, EntityType entityType, String serviceName, String id) throws SerializerException;
        <T> void operationByActionType(T data, EntityType entityType, String serviceName, String id, ActionType actionType) throws SerializerException;
        <T> void save(T data, EntityType entityType, String serviceName) throws SerializerException;
        void incVersion(EntityType entityType, String serviceName);
        // TODO: check if exists before delete
        void delete(EntityType entityType, String serviceName, String id);
        void commit();
    }
}
