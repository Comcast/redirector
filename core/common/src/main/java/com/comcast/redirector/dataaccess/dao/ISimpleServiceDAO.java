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
import com.comcast.redirector.dataaccess.cache.ICacheListener;

/**
 * DAO works with data entities which are singletons per serviceName
 * Example: we have one white list for "xreGuide", one for "pandora", one for "sports"
 * @param <T> type of data entity
 */
public interface ISimpleServiceDAO<T> {
    /**
     * @param serviceName name of service, e.g. xreGuide
     * @return data entity by given service name
     */
    T get(String serviceName);

    /**
     * Save data for service
     *
     * @param data entity to save
     * @param serviceName name of service, e.g. xreGuide
     */
    void save(T data, String serviceName) throws SerializerException;

    /**
     * Delete entity for given service
     * @param serviceName name of service, e.g. xreGuide
     */
    void delete(String serviceName);

    /**
     * @param serviceName name of service, e.g. xreGuide
     * @return version of data entity
     */
    int getObjectVersion(String serviceName);

    void addCacheListener(String serviceName, ICacheListener listener);
}
