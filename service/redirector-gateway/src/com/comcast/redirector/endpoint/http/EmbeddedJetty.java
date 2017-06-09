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

package com.comcast.redirector.endpoint.http;

import com.comcast.redirector.endpoint.http.springconfig.WebAppInitializer;
import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ClassInheritanceHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

public class EmbeddedJetty {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJetty.class);

    private Config config = ConfigLoader.doParse(Config.class);
    private Server server;
    private volatile boolean started = false;

    public static EmbeddedJetty getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final EmbeddedJetty INSTANCE = new EmbeddedJetty();
    }

    private EmbeddedJetty() {
    }

    public void start() {
        // TODO: remove redundant fields from config and move this check to XRE Redirector
        if (! config.getEnableCommunicationEndpoint()) {
            log.warn("skipping Jetty endpoint due to configuration");
            return;
        }

        if (started) {
            log.warn("Jetty is already started");
        }

        started = true;
        Integer port = config.getCommunicationEndpointPort();

        log.info("Starting embedded jetty server (XRERedirector Gateway) on port: {}", port);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setConfigurations(new Configuration[]{new AnnotationConfiguration() {
            @Override
            public void preConfigure(WebAppContext context) {
                ClassInheritanceMap map = new ClassInheritanceMap();
                map.put(WebApplicationInitializer.class.getName(), new ConcurrentHashSet<String>() {{
                    add(WebAppInitializer.class.getName());
                }});
                context.setAttribute(CLASS_INHERITANCE_MAP, map);
                _classInheritanceHandler = new ClassInheritanceHandler(map);
            }
        }});

        server = new Server(port);
        server.setHandler(webAppContext);

        try {
            server.start();
        } catch (Exception e) {
            log.error("Failed to start embedded jetty server (XRERedirector communication endpoint) on port: " + port, e);
        }

        log.info("Started embedded jetty server (Redirector Gateway) on port: {}", port);
    }
}
