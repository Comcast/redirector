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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.ServiceInstances;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.RedirectorController;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class RedirectorControllerIntegrationTest {

    private RestApiFacade apiFacade;

    private List<String> redirectorInstances = Arrays.asList("redirectorInstance_1", "redirectorInstance_2", "redirectorInstance_3");

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     *
     * test check for {@link RedirectorController#getRedirectorInstances(String)}
     */
    @Test
    public void testGetRedirectorInstances() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new RedirectorInstancesServiceSteps(serviceName)
                .getRedirectorInstances()
                .verifyWithExpectedInstances();
    }


    /**
     *
     * test check for {@link RedirectorController#getRedirectorAppNames()}
     */
    @Test
    public void testGetRedirectorAppNames() throws Exception {
        final String serviceName = getServiceNameForTest();

        new RedirectorInstancesServiceSteps(serviceName)
                .getRedirectorAppNames()
                .verifyAppNamesDoNotContainCurrentApp()
                .setupEnvironment()
                .getRedirectorAppNames()
                .verifyAppNamesContainCurrentApp();
    }

    private class RedirectorInstancesServiceSteps {
        private String serviceName;
        private ServiceInstances responseEntityObject;
        private String redirectorAppNamesResponse;
        String JSON_REGEX_PATTERN;

        RedirectorInstancesServiceSteps(String serviceName) throws Exception {
            this.serviceName = serviceName;
            JSON_REGEX_PATTERN = "^\\{\"version\":0," + "\"appNames\":\\[.*\"" + serviceName + "\".*\\]\\}$";
        }

        public RedirectorInstancesServiceSteps getRedirectorInstances() {
            responseEntityObject = apiFacade.getRedirectorInstancesForService(serviceName);
            return this;
        }

        public RedirectorInstancesServiceSteps getRedirectorAppNames() {
            redirectorAppNamesResponse = apiFacade.getRedirectorAppNames();
            return this;
        }

        public RedirectorInstancesServiceSteps verifyWithExpectedInstances() {
            assertNotNull(responseEntityObject);
            assertTrue(!responseEntityObject.getInstances().isEmpty());
            for (String instanceName : redirectorInstances) {
                assertTrue(responseEntityObject.getInstances().contains(instanceName));
            }
            return this;
        }

        public RedirectorInstancesServiceSteps setupEnvironment() throws Exception {
            setupEnv(serviceName);
            return this;
        }

        public RedirectorInstancesServiceSteps verifyAppNamesDoNotContainCurrentApp() {
            assertFalse("appName: " + serviceName + " must not been in the response: "
                    + redirectorAppNamesResponse, redirectorAppNamesResponse.matches(JSON_REGEX_PATTERN));
            return this;
        }

        public RedirectorInstancesServiceSteps verifyAppNamesContainCurrentApp() {
            assertTrue("JSON REGEX NOT MATCHED! : " + redirectorAppNamesResponse, redirectorAppNamesResponse.matches(JSON_REGEX_PATTERN));
            return this;
        }
    }

    private IntegrationTestHelper setupEnv(String serviceName) throws Exception {
        String instance1 = redirectorInstances.get(0);
        String instance2 = redirectorInstances.get(1);
        String instance3 = redirectorInstances.get(2);
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("zone2")
                .withDefaultUrlParams()
                .urn("any").protocol("any").port("0").ipv("4")
                .withRedirectorInstances()
                    .app(serviceName).instance(instance1).data(instance1 + " node test data")
                    .app(serviceName).instance(instance2).data(instance2 + " node test data")
                    .app(serviceName).instance(instance3).data(instance3 + " node test data")
                .withStacksWithData()
                    .stack("/DataCenterServerNameTest/Region1").flavor("Zone1").app(serviceName).data(serviceName + " node test data")
                .build();

        return testHelperBuilder(context).setupEnv();
    }
}
