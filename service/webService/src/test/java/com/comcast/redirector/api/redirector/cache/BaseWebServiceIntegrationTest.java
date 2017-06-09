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

package com.comcast.redirector.api.redirector.cache;

import com.comcast.redirector.api.config.RedirectorConfig;
import com.comcast.redirector.api.negativetests.RestApiEndpoint;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.helper.IntegrationTestHelper;
import org.junit.After;
import org.junit.Before;

import static com.comcast.redirector.api.WebServiceAPINegativeTestSuite.CONFIG_OVERRIDE_PROPERTY;

public class BaseWebServiceIntegrationTest {
    RestApiFacade restApiFacade = new RestApiFacade();
    RestApiEndpoint restApiEndpoint = new RestApiEndpoint();
    private RedirectorConfig redirectorConfig;

    @Before
    public void before() throws Exception {
        redirectorConfig = ConfigLoader.doParse(RedirectorConfig.class, CONFIG_OVERRIDE_PROPERTY, "redirector");
    }

    @After
    public void after() throws Exception {
        restApiEndpoint.stop();
    }

    IntegrationTestHelper.SimpleDataSourceBasedIntegrationHelperBuilder setupTestHelperBuilder(it.context.TestContext context) throws Exception {
        return new IntegrationTestHelper.Builder()
            .config(coreConfigFromApiConfig(redirectorConfig))
            .context(context)
            .buildSimpleDataSourceBasedIntegrationHelper();
    }

    public static Config coreConfigFromApiConfig(RedirectorConfig apiConfig) {
        Config config = new Config();
        config.setZooKeeperBasePath(apiConfig.getZookeeperBasePath());
        config.setZooKeeperConnection(apiConfig.getConnectionUrl());
        config.setZooKeeperConnectionTimeout(apiConfig.getConnectionTimeoutMs());
        config.setZooKeeperSessionTimeout(apiConfig.getConnectionTimeoutMs() * 2);

        return config;
    }

    int getDataSourceSessionTimeout() {
        return redirectorConfig.getConnectionTimeoutMs() * 2;
    }
}
