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
 */
package com.comcast.redirector.api;

import com.comcast.redirector.api.exceptionmapper.*;
import com.comcast.redirector.api.redirector.controllers.StacksController;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StackWorkerIntegrationTest.class,
})
public class WebServiceAPIStackWorkerBackgroundTestSuite {

    @BeforeClass
    public static void startTestSuite() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig(
                StacksController.class,
                DataServiceExceptionMapper.class,
                DataSourceConnectorExceptionMapper.class,
                ServiceUnavailableExceptionMapper.class,
                ServiceExceptionMapper.class,
                RedirectorLockReleaseExceptionMapper.class,
                NoDataSourceConnectionExceptionMapper.class,
                RedirectorDataSourceExceptionMapper.class,
                ValidationExceptionMapper.class);
        resourceConfig.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        HttpTestServerHelper.createAndStartHttpServer(resourceConfig);
    }

    @AfterClass
    public static  void stopTestSuite() throws Exception {
        HttpTestServerHelper.shutDownHttpServer();
    }
}
