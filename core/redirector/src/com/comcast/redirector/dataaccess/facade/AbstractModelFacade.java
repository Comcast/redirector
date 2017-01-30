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

package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractModelFacade implements IModelFacade {
    private static final Logger log = LoggerFactory.getLogger(AbstractModelFacade.class);

    protected IDataSourceConnector connector;
    protected volatile boolean available;

    AbstractModelFacade(IDataSourceConnector connector) {
        this.connector = connector;
    }

    final void doStart() {
        boolean connectedOnStart = connector.isConnected();
        if (connectedOnStart) { // already connectedOnStart, then start caches.
            try {
                start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            connector.addConnectionListener(newState -> {
                log.info("State changed: {}", newState);
                if (newState == IDataSourceConnector.ConnectorState.CONNECTED ||
                    newState == IDataSourceConnector.ConnectorState.RECONNECTED) {
                    try {
                        if (!isAvailable()) {
                            start();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    protected String concatEndpointAndVersion(Integer version, String... paths) {
        StringBuilder urlStringBuilder = new StringBuilder(concatEndpoint(paths));
        if (version != null && version > 0) {
            urlStringBuilder.append("?version=");
            urlStringBuilder.append(version);
        }
        return urlStringBuilder.toString();
    }
    
    protected String concatEndpoint(String... paths) {
        StringBuilder urlStringBuilder = new StringBuilder();
        for (String path : paths) {
            if (path.startsWith("/") || urlStringBuilder.toString().endsWith("/")) {
                urlStringBuilder.append(path);
            } else {
                urlStringBuilder.append("/").append(path);
            }
        }
        return urlStringBuilder.toString();
    }

    @Override
    public synchronized void start() throws Exception {
        if (available) {
            log.info("Cache is available");
            return;
        }

        if ( connector.blockUntilConnectedOrTimedOut() ) {
            available = true;
        } else {
            log.warn("Cache is not started. DataStore is not available");
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
