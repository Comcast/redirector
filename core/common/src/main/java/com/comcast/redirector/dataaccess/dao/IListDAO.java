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
import com.comcast.redirector.dataaccess.cache.ICacheListener;

import java.util.List;

/**
 * DAO works wirh list of data entities stored globally and not related to any service name
 * Example: collection of namespaced lists
 * @param <T> type of data entity
 */
public interface IListDAO<T> {
    /**
     * @return list of all data entities
     */
    List<T> getAll();

    /**
     * @param id id of entity. e.g. i200
     * @return particular entity by given id
     */
    T getById(String id);

    /**
     * Save single entity for id
     *
     * @param data entity to save
     * @param id id of entity. e.g. i200
     */
    void saveById(T data, String id) throws SerializerException;

    /**
     * Delete single entity for given id
     *
     * @param id id of entity. e.g. i200
     */
    void deleteById(String id);

    void addCacheListener(ICacheListener listener);
    
    List<String> getAllIDs();
}
