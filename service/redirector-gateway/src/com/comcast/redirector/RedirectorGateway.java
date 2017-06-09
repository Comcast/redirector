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

package com.comcast.redirector;

import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.redirector.core.config.RedirectorCoreConfigUtil;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RedirectorGateway {
    private static final Logger log = LoggerFactory.getLogger(RedirectorGateway.class);

    private RootContext rootContext = RedirectorGatewayRootContext.getInstance();

    public static RedirectorGateway getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final RedirectorGateway INSTANCE = new RedirectorGateway();
    }

    private RedirectorGateway() {
    }

    public void start() {
        log.info("Init redirector gateway started");
        validateConfig();

        try {
            startRootContext();
        } catch (Exception ex) {
            log.error("Init redirector gateway failed ", ex);
        }
        log.info("Init redirector gateway completed");
    }

    private void validateConfig() {
        ZKConfig config = ConfigLoader.doParse(Config.class);
        RedirectorCoreConfigUtil.validate(config);
    }

    private void startRootContext() {
        rootContext.start();
    }

    public IRedirector getRedirector(String appName) {
        return rootContext.getRedirector(appName);
    }

    public boolean isAppRegistered(String appName) {
        return rootContext.isAppRegistered(appName);
    }

    public Set<String> getApplications() {
        return rootContext.getApplications();
    }
}
