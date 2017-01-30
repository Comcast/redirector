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

import com.comcast.redirector.dataaccess.EntityType;

public interface IDAOFactory {
    
    <T> IListDAO<T> getListDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed);

    <T> IListDAO<T> getNamespacedListsDAO(EntityType entityType, boolean isCompressed);

    <T> ISimpleDAO<T> getSimpleDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed);
    <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed, boolean useCache, boolean useCacheWhenNotConnectedToDataSource);
    <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed, boolean useCache);
    <T> ISimpleServiceDAO<T> getSimpleServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed);
    <T> IListServiceDAO<T> getListServiceDAO(Class<T> modelClass, EntityType entityType, boolean isCompressed);
    IEmptyObjectDAO getEmptyObjectDAO(EntityType entityType);
    INodeVersionDAO getNodeVersionDAO();

    IStacksDAO createStacksDAO();
}
