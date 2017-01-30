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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.cache.ICacheListener;

/**
 * DAO works with data entities stored globally and not related to any service name
 * Example: entity RedirectorConfig
 *
 * @param <T> type of data entity
 */
public interface ISimpleDAO<T> {

    /**
     * @return data entity
     */
    T get();


    /**
     * Save data for service
     *
     * @param data entity to save
     */
    void save(T data) throws SerializerException;

    void addCacheListener(ICacheListener listener);
}
