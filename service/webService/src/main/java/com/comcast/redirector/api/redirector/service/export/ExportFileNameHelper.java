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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.export;

import com.comcast.redirector.dataaccess.EntityType;
import org.springframework.stereotype.Component;

@Component
public class ExportFileNameHelper implements IExportFileNameHelper {
    @Override
    public String getFileNameForOneEntity(EntityType entityType, String serviceName, String id) {
        return "attachment; filename = exported"+ entityType.getPath() + ((id != null) ? ("-byId-" + id) : "") + ((serviceName !=  null) ? ("-forService-" + serviceName) : "") + ".json";
    }

    @Override
    public String getFileNameForOneEntityWithoutService(EntityType entityType,String id) {
        return getFileNameForOneEntity(entityType, null, id);
    }

    @Override
    public String getFileNameForAll(EntityType entityType, String serviceName) {
        return getFileNameForOneEntity(entityType, serviceName, null);
    }

    @Override
    public String getFileNameForAll(EntityType entityType) {
        return getFileNameForOneEntity(entityType, null, null);
    }

    @Override
    public String getHeader() {
        return  "content-disposition";
    }
}
