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

package com.comcast.redirector.api.redirectorOffline;

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.cache.IDataListener;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.comcast.redirector.common.RedirectorConstants.NO_MODEL_NODE_VERSION;

public class DataListenersFacade {
    private static final Logger log = LoggerFactory.getLogger(DataListenersFacade.class);

    private IDataSourceConnector connector;

    public DataListenersFacade(IDataSourceConnector connector) {
        this.connector = connector;
    }

    public void addModelChangedListener(IDataListener listener, String serviceName) {
        String modelReloadPath = PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath())
            .getPathByService(serviceName);
        connector.addNodeDataChangeListener(modelReloadPath, listener);
    }

    public int getModelVersion(String serviceName) {
        try {
            return connector.getNodeVersion(PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath()).getPathByService(serviceName));
        } catch (DataSourceConnectorException e) {
            log.warn("Failed to get model version for " + serviceName);
            return NO_MODEL_NODE_VERSION;
        }
    }

    //todo: is it still used somewhere?
    public int getNamespacedListsVersion() {
        try {
            return connector.getNodeVersion(PathHelper.getPathHelper(EntityType.NAMESPACED_LIST, connector.getBasePath()).getPath());
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get namespacedList version", e);
            return NO_MODEL_NODE_VERSION;
        }
    }
}
