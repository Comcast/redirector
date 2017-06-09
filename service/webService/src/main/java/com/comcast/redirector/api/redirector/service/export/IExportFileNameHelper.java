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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.export;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.dataaccess.EntityType;

/**
 * A hepler class which builds a header and a filename for exported files.
 */
public interface IExportFileNameHelper {
    /**
     * Returns a file name for entity with service name and id
     * @param entityType
     * @param serviceName
     * @param id
     * @return
     */
    public String getFileNameForOneEntity(EntityType entityType, String serviceName, String id);

    /**
     * Returns a file anme for an entity unrelated to any service
     * (as {@link NamespacedList})
     * @param entityType
     * @param id
     * @return
     */
    public String getFileNameForOneEntityWithoutService(EntityType entityType,String id);

    /**
     * Returns a file for all entities which are related to service
     * @param entityType
     * @param serviceName
     * @return
     */
    public String getFileNameForAll(EntityType entityType, String serviceName);

    /**
     * Returns a file name for all entities which are unrelated to servicep
     * @param entityType
     * @return
     */
    public String getFileNameForAll(EntityType entityType);

    /**
     * Returns a header name
     * @return
     */
    String getHeader();
}
