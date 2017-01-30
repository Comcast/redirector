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
package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.ModelState;
import com.comcast.redirector.api.model.ModelStatesWrapper;
import com.comcast.redirector.api.model.whitelisted.WhitelistUpdate;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.*;

public class RedirectorModelInitializerControllerIntegrationTest {

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void testGetAllExistingApplications() throws Exception {
        new RedirectorModelInitializerControllerIntegrationTest.RedirectorModelInitializerSteps()
                .getExistingApplications()
                .verifyExistingApplications();
    }

    @Test
    public void testActivateModelForService() throws Exception {
        new RedirectorModelInitializerControllerIntegrationTest.RedirectorModelInitializerSteps()
                .getExistingApplications()
                .verifyExistingApplications()
                .activateModelForApplications()
                .getExistingApplications()
                .isValidModelExists()
                .getWhitelistedUpdate()
                .verifyExistingApplicationsAfterActivateOne();
    }

    private class RedirectorModelInitializerSteps {
        private String appName;
        private String appName1;
        private String appName2;
        private Boolean validModelExistent;
        ModelStatesWrapper modelStates;
        List<WhitelistUpdate> whitelistUpdateList;

        RedirectorModelInitializerSteps() throws Exception {
            this.appName = getServiceNameForTest();
            this.appName1 = appName + UUID.randomUUID().toString();
            this.appName2 = appName + UUID.randomUUID().toString();
            setupEnv();
        }

        RedirectorModelInitializerSteps getExistingApplications() {
            modelStates = apiFacade.getAllExistingApplications();
            return this;
        }

        RedirectorModelInitializerSteps activateModelForApplications() {
            apiFacade.activateModelForService(appName1);
            return this;
        }

        RedirectorModelInitializerSteps verifyExistingApplications() {
            ModelState modelStateExpected1 = new ModelState(appName1, false);
            ModelState modelStateExpected2 = new ModelState(appName2, false);

            assertNotNull(modelStates.getModelStates());
            assertTrue(modelStates.getModelStates().contains(modelStateExpected1));
            assertTrue(modelStates.getModelStates().contains(modelStateExpected2));

            return this;
        }

        RedirectorModelInitializerSteps verifyExistingApplicationsAfterActivateOne() {
            assertTrue(validModelExistent);

            ModelState modelStateExpected1 = new ModelState(appName1, true);
            ModelState modelStateExpected2 = new ModelState(appName2, false);

            assertNotNull(modelStates.getModelStates());
            assertTrue(modelStates.getModelStates().contains(modelStateExpected1));
            assertTrue(modelStates.getModelStates().contains(modelStateExpected2));

            assertTrue(whitelistUpdateList.size() > 0);
            assertEquals("/DataCenter1/Region1" , whitelistUpdateList.get(0).getPath());
            assertEquals(ActionType.ADD, whitelistUpdateList.get(0).getAction());
            
            return this;
        }

        RedirectorModelInitializerSteps isValidModelExists() {
            validModelExistent = apiFacade.isValidModelExists();
            return this;
        }
    
        RedirectorModelInitializerSteps getWhitelistedUpdate() {
            whitelistUpdateList  = apiFacade.getWhitelistUpdatesForService(appName1);
            return this;
        }

        private void setupEnv() throws Exception {
            TestContext context = new ContextBuilder().forApp(appName1)
                    .withHosts()
                    .stack("/DataCenter1/Region1").flavor("Zone1").app(appName1).ipv4("1.1.1.1").ipv6("1::1")
                    .stack("/DataCenter4/Region4").flavor("Zone4").app(appName2).ipv4("1.1.1.1").ipv6("1::1")
                    .build();

            testHelperBuilder(context).setupEnv();
        }
    }
}
