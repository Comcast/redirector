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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import org.junit.Before;
import org.junit.Test;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class SettingsControllerIntegrationTest {

    private static final String REDIRECTOR_CONFIG_PATH = HttpTestServerHelper.BASE_URL + "settings/redirectorConfig";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(REDIRECTOR_CONFIG_PATH);
    }


    // Get default Redirector Config
    @Test
    public void testGetRedirectorConfigSettings() throws Exception {
        setupEnv(getServiceNameForTest());
        new SettingsServiceSteps()
                .verifyIsDefaultRedirectorConfig();
    }

    //Save Redirector Config
    @Test
    public void testSaveRedirectorConfigSettings() throws Exception {
        setupEnv(getServiceNameForTest());
        new SettingsServiceSteps()
                .createAndPostCustomRedirectorConfig()
                .verifySuccessfulPost()
                .verifyIsCustomRedirectorConfig();
    }

    private class SettingsServiceSteps {
        RedirectorConfig config;
        RedirectorConfig responseEntityObject;

        SettingsServiceSteps createAndPostCustomRedirectorConfig() {
            config = getCustomRedirectorConfig();
            post();
            return this;
        }

        SettingsServiceSteps post() {
            responseEntityObject = apiFacade.postRedirectorConfig(config);
            return this;
        }

        SettingsServiceSteps verifyIsDefaultRedirectorConfig() {
            RedirectorConfig expectedConfig = new RedirectorConfig();
            expectedConfig.setMinHosts(1);
            expectedConfig.setAppMinHosts(1);
            RedirectorConfig actualConfig = getRedirectorConfigFromApi();

            assertRedirectorConfigsEqual(expectedConfig, actualConfig);

            return this;
        }

        SettingsServiceSteps verifyIsCustomRedirectorConfig() {
            RedirectorConfig expectedConfig = getCustomRedirectorConfig();
            RedirectorConfig actualConfig = getRedirectorConfigFromApi();

            assertRedirectorConfigsEqual(expectedConfig, actualConfig);

            return this;
        }

        SettingsServiceSteps verifySuccessfulPost() {
            assertRedirectorConfigsEqual(getCustomRedirectorConfig(), responseEntityObject);
            return this;
        }

        private RedirectorConfig getRedirectorConfigFromApi() {
            return apiFacade.getRedirectorConfig();
        }

        private RedirectorConfig getCustomRedirectorConfig() {
            RedirectorConfig result = new RedirectorConfig();
            result.setMinHosts(10);
            result.setAppMinHosts(15);

            return result;
        }

        private void assertRedirectorConfigsEqual(RedirectorConfig config, RedirectorConfig other) {
            assertEquals(config.getMinHosts(), other.getMinHosts());
            assertEquals(config.getAppMinHosts(), other.getAppMinHosts());
        }
    }
}
