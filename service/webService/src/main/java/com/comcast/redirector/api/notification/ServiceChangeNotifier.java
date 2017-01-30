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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.notification;

import com.comcast.redirector.api.redirector.service.IDataChangesNotificationService;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import com.comcast.redirector.dataaccess.dao.NodeVersionDAO;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class ServiceChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(ServiceChangeNotifier.class);

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private IStacksDAO stacksDAO;

    @Autowired
    private IDataChangesNotificationService dataChangesNotificationService;

    @PostConstruct
    private void init() {
        initStacksVersionCounter();
        stacksDAO.addCacheListener(new ServiceChangeListeners());
    }

    private void initStacksVersionCounter() {
        if (connector.isConnected()) {
            createCounterOnEmptyZookeeper();
        } else {
            connector.addConnectionListener(newState -> {
                if (newState == IDataSourceConnector.ConnectorState.CONNECTED) {
                    createCounterOnEmptyZookeeper();
                }
            });
        }
    }

    private void createCounterOnEmptyZookeeper() {
        if (dataChangesNotificationService.getStacksVersion() == NodeVersionDAO.NO_VERSION) {
            dataChangesNotificationService.updateStacksVersion();
        }
    }

    private class ServiceChangeListeners implements ICacheListener {

        @Override
        public void onChanged() {
            dataChangesNotificationService.updateStacksVersion();
        }

        @Override
        public void onChanged(TreeCacheEvent event) {
            switch (event.getType()) {
                case NODE_ADDED:
                case NODE_REMOVED:
                case NODE_UPDATED:
                    if (needUpdate(event.getData().getPath())) {
                        onChanged();
                        log.info("Service discovery changes: type: {} path: {}", event.getType(), event.getData().getPath());
                    }
                    break;
            }
        }

        private boolean needUpdate(String path) {
            return StringUtils.isNotBlank(path) && path.split("/").length == 6;
        }
    }

}
