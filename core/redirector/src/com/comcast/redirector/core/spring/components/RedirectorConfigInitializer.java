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

package com.comcast.redirector.core.spring.components;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RedirectorConfigInitializer {
    private static final Logger log = LoggerFactory.getLogger(RedirectorConfigInitializer.class);

    @Autowired
    private ZKConfig config;
    @Autowired
    private ICommonModelFacade modelFacade;
    @Autowired
    private IDataSourceConnector connector;
    private boolean initialized = false;

    private RedirectorConfig redirectorConfig;

    @PostConstruct
    void init() {
        redirectorConfig = createRedirectorConfig();

        connector.addConnectionListener((newState) -> {
            if (!initialized && connector.isConnected()) {
                saveRedirectorConfig(redirectorConfig);
            }
        });

        if (connector.isConnected()) {
            saveRedirectorConfig(redirectorConfig);
        }
    }

    private RedirectorConfig createRedirectorConfig() {
        RedirectorConfig redirectorConfig = new RedirectorConfig();
        redirectorConfig.setMinHosts(config.getMinHosts());
        redirectorConfig.setAppMinHosts(config.getAppMinHosts());
        return redirectorConfig;
    }

    private void saveRedirectorConfig(RedirectorConfig redirectorConfig) {
        try {
            modelFacade.saveRedirectorConfig(redirectorConfig);
            initialized = true;
        } catch (Exception e) {
            log.error("Failed to saveCompressed redirectorConfig. {}", e.getMessage());
        }
    }
}
