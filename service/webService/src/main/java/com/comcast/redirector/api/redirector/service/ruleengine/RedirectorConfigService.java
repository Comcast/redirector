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

package com.comcast.redirector.api.redirector.service.ruleengine;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.ISimpleDAO;
import com.comcast.redirector.api.model.RedirectorConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class RedirectorConfigService implements IRedirectorConfigService {

    @Autowired
    ISimpleDAO<RedirectorConfig> redirectorConfigDAO;

    @Override
    public RedirectorConfig getRedirectorConfig() {
        return redirectorConfigDAO.get();
    }

    @Override
    public synchronized void saveRedirectorConfig(RedirectorConfig redirectorConfig) {
        try {
            redirectorConfigDAO.save(redirectorConfig);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }
}
