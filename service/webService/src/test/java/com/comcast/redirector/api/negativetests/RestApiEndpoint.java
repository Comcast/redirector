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

package com.comcast.redirector.api.negativetests;

import com.comcast.redirector.api.decider.DeciderRulesController;
import com.comcast.redirector.api.decider.PartnersController;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.*;
import com.comcast.redirector.api.redirector.controllers.templates.TemplateFlavorRulesController;
import com.comcast.redirector.api.redirector.controllers.templates.TemplateUrlRulesController;
import com.comcast.redirector.api.exceptionmapper.*;
import com.comcast.redirector.api.redirectorOffline.controllers.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class RestApiEndpoint {
    public void start() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig(
            DeciderRulesController.class,
            DistributionController.class,
            DistributionControllerOffline.class,
            NamespaceController.class,
            NamespaceControllerOffline.class,
            PendingChangesControllerOffline.class,
            PendingChangesController.class,
            PartnersController.class,
            StacksController.class,
            RulesController.class,
            RulesControllerOffline.class,
            RedirectorController.class,
            RedirectorTestSuiteController.class,
            SummaryController.class,
            ServersController.class,
            ServersControllerOffline.class,
            UrlRulesController.class,
            UrlRulesControllerOffline.class,
            WhitelistedController.class,
            WhiteListOfflineController.class,
            WhitelistedStackUpdatesController.class,
            SettingsController.class,
            TrafficController.class,
            TemplateFlavorRulesController.class,
            TemplateUrlRulesController.class,
            DataServiceExceptionMapper.class,
            DataSourceConnectorExceptionMapper.class,
            ServiceUnavailableExceptionMapper.class,
            ServiceExceptionMapper.class,
            RedirectorLockReleaseExceptionMapper.class,
            NoDataSourceConnectionExceptionMapper.class,
            RedirectorDataSourceExceptionMapper.class);
        resourceConfig.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        HttpTestServerHelper.createAndStartHttpServer(resourceConfig);
    }

    public void stop() throws Exception {
        HttpTestServerHelper.shutDownHttpServer();
    }
}
