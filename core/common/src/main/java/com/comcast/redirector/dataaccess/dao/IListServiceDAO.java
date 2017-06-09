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
import java.util.Map;

/**
 * DAO works with lists of data entities grouped by service name
 * Example: we have list of rules for "xreGuide" and another list of rules for "pandora"
 * @param <T> type of data entity
 */
public interface IListServiceDAO<T> {
    /**
     * @param serviceName name of service, e.g. xreGuide
     * @return list of all data entities by given service name
     */
    List<T> getAll(String serviceName) ;
    /**
     * @param serviceName name of service, e.g. xreGuide
     * @return map of all data entities by given service name
     */
    Map<String, T> getAllInMap(String serviceName) ;

    /**
     * @param serviceName name of service, e.g. xreGuide
     * @param id entity id
     * @return particular entity by given id and service name
     */
    T getById(String serviceName, String id) ;

    /**
     * Save single entity for service and id
     *
     * @param data entity to save
     * @param serviceName name of service, e.g. xreGuide
     * @param id id of entity. e.g. RNG_Rule
     */
    void saveById(T data, String serviceName, String id) throws SerializerException;

    /**
     * Delete single entity for given service and id
     *
     * @param serviceName name of service, e.g. xreGuide
     * @param id id of entity. e.g. RNG_Rule
     */
    void deleteById(String serviceName, String id) ;

    void addCacheListener(String serviceName, ICacheListener listener) ;
}
