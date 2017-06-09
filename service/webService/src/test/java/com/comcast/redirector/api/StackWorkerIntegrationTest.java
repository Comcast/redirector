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
 */
package com.comcast.redirector.api;

import com.comcast.redirector.api.model.xrestack.*;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.StacksHelper;
import com.comcast.redirector.api.redirector.helpers.StacksValidationHelper;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:stackWorker-context.xml"})
public class StackWorkerIntegrationTest {
    
    private static final int TICK_TIME = 100;
    private static final int WAIT_TIME = 4000;

    private RestApiFacade apiFacade;
    
    @Before
    public void before() {
        apiFacade = new RestApiFacade((HttpTestServerHelper.BASE_URL));
    }
    
    @Test
    @Ignore
    public void testGetAddressByStackForInactiveNodes() throws Exception {
        String serviceName = getServiceNameForTest();
        String serviceName_1 = serviceName + "_1";
        String serviceName_2 = serviceName + "_2";

        new StackWorkerSteps(serviceName_1, serviceName_2)
                .registerStacks()
                .verifyAllStacksPresentBeforeCleanup()
                .verifyOnlyActiveStacksRemainAfterCleanup();
    }
    
    private class StackWorkerSteps {
        private ServicePaths initialServicePathsWithFlavors;
        private String serviceName1;
        private String serviceName2;
        private ServicePaths pathsResponseEntityObject;

        StackWorkerSteps(String serviceName1, String serviceName2) throws Exception {
            this.serviceName1 = serviceName1;
            this.serviceName2 = serviceName2;
        }

        StackWorkerSteps registerStacks() throws Exception {

            TestContext context = new ContextBuilder().forApp(serviceName1)
                    .withDefaultServer().flavor("Zone2")
                    .withDefaultUrlParams()
                    .urn("any").protocol("any").port("0").ipv("4")
                    .withHosts()
                    .stack("/DataCenter1/Region1").flavor("Zone1").app(serviceName1).ipv4("1.1.1.1").ipv6("1::1")
                    .stack("/DataCenter1/Region1").flavor("Zone1").app(serviceName1).ipv4("2.2.2.2").ipv6("2::2")
                    .stack("/DataCenter2/Region1").flavor("Zone1").app(serviceName1).ipv4("1.1.1.1").ipv6("1::1")

                    .stack("/DataCenter3/Region3").flavor("Zone3").app(serviceName2).ipv4("1.1.1.1").ipv6("1::1")
                    .stack("/DataCenter3/Region3").flavor("Zone3").app(serviceName2).ipv4("2.2.2.2").ipv6("2::2")
                    .stack("/DataCenter4/Region4").flavor("Zone4").app(serviceName2).ipv4("1.1.1.1").ipv6("1::1")
                    .withEmptyStacks()
                    .stack("/DataCenter2/Region2").flavor("Zone2").app(serviceName1)

                    .stack("/DataCenter3/Region3").flavor("Zone4").app(serviceName2)
                    .build();

            testHelperBuilder(context).setupEnv();

            waitAllStacksSetUp(WAIT_TIME, 3, serviceName1);
            waitAllStacksSetUp(WAIT_TIME, 3, serviceName2);

            initialServicePathsWithFlavors = StacksHelper.getAllStacks(MediaType.APPLICATION_JSON);
            return this;
        }

        StackWorkerSteps verifyAllStacksPresentBeforeCleanup() throws Exception {
            Paths expectedPaths = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            pathsResponseEntityObject = getServicePathsForServiceFromApi(serviceName1);

            StacksValidationHelper.validatePathsInResponse(expectedPaths, pathsResponseEntityObject);

            return this;
        }

        StackWorkerSteps verifyOnlyActiveStacksRemainAfterCleanup() throws Exception {
            waitAllStacksSetUp(WAIT_TIME, 2, serviceName1);
            waitAllStacksSetUp(WAIT_TIME, 2, serviceName2);
            verifyStackWasDeleted("/DataCenter2/Region2/Zone2");
            return this;
        }

        private void verifyStackWasDeleted(String stack) throws Exception {
            Paths expectedPaths = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            assertNotNull(expectedPaths);
            expectedPaths.getStacks().remove(StacksHelper.getStackItemByPath(expectedPaths, stack));
            StacksHelper.generateFlavorsForServicePaths(initialServicePathsWithFlavors);
            pathsResponseEntityObject = getServicePathsForServiceFromApi(serviceName1);

            StacksValidationHelper.validatePathsInResponse(expectedPaths, pathsResponseEntityObject);
        }

        private ServicePaths getServicePathsForServiceFromApi(String serviceName) {
            return apiFacade.getServicePathsForService(serviceName);
        }
        
        private void waitAllStacksSetUp (int waitTime, int hostsNumber, String serviceName) throws InterruptedException {
            long lastTestTime = System.currentTimeMillis() + waitTime;
            int count = 0;
            while(count != hostsNumber) {
                if (lastTestTime < System.currentTimeMillis()) {
                    fail("Couldn't found hosts for running test");
                }
                List<Paths> stacksAndFlavorList = apiFacade.getServicePathsForService(serviceName).getPaths();
                if (stacksAndFlavorList.size() > 0) {
                    count = stacksAndFlavorList.get(0).getStacks().size();
                }
                TimeUnit.MILLISECONDS.sleep(TICK_TIME);
            }
        }
    }
    
}
