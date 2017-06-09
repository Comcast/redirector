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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.dataaccess.EntityCategory;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.comcast.redirector.dataaccess.EntityType.*;

@Service
public class DefaultNodesService {
    private static final Logger log = LoggerFactory.getLogger(DefaultNodesService.class);

    @Autowired
    private IDataSourceConnector connector;

    public boolean areNodesPresentForService(String serviceName){
        try {
            String path = PathHelper.getPathHelper(DISTRIBUTION, connector.getBasePath()).getPathByService(serviceName);
            return connector.isPathExists(path);
        } catch (DataSourceConnectorException e) {
            log.error("Failed to check if nodes exist for service " + serviceName, e);
            return false;
        }
    }

    public boolean createNodesForService(String serviceName) {
        try {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, connector.getBasePath());

            connector.create(pathHelper.getPathByService(serviceName, DISTRIBUTION));
            connector.create(pathHelper.getPathByService(serviceName, WHITELIST));
            connector.create(pathHelper.getPathByService(serviceName, RULE));
            connector.create(pathHelper.getPathByService(serviceName, SERVER));
            connector.create(pathHelper.getPathByService(serviceName, URL_PARAMS));
            connector.create(pathHelper.getPathByService(serviceName, PENDING_CHANGES_STATUS));
            connector.create(pathHelper.getPathByService(serviceName, URL_RULE));
            connector.create(pathHelper.getPathByService(serviceName, TEST_CASE));
            return true;
        } catch (Exception e) {
            log.error("Failed to create nodes for service " + serviceName, e);
            return false;
        }
    }

}
