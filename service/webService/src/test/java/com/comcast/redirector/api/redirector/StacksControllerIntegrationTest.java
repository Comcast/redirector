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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.xrestack.*;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.StacksHelper;
import com.comcast.redirector.api.redirector.helpers.StacksValidationHelper;
import com.comcast.redirector.api.redirector.controllers.StacksController;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class StacksControllerIntegrationTest {

    private static final int TICK_TIME = 100;
    private static final int WAIT_TIME = 1000;
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private static final String NON_EXISTENT_FLAVOR = "non_existent_flavor";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    /**
     * test check for {@link StacksController#getStacksForService(String)} <p>
     * acceptedResponseType - JSON Object
     */
    @Test
    public void testGetStacksForService() throws Exception {
        new PathsServiceSteps()
                .getPaths()
                .verifyWithExpectedPaths();
    }

    /**
     * test check for {@link StacksController#getStacksForService(String)} <p>
     * acceptedResponseType - JSON Object <p>
     * Test case when no data exists for serviceName
     */
    @Test
    public void testGetStacksForNotExistingService() throws Exception {
        new PathsServiceSteps()
                .getPathsForNonExistentService();
    }

    /**
     * test check for {@link StacksController#getAllStacks()}
     * acceptedResponseType - JSON Object
     */
    @Test
    public void testGetAllStacks() throws Exception {
        new PathsServiceSteps()
                .getAllPaths()
                .verifyWithAllPaths();
    }


    /**
     * test check for {@link StacksController#getAddressByStackOrFlavor(String, String, String)}
     * acceptedResponseType - JSON Object
     * Test case for GetAddressByStack
     */
    @Test
    public void testGetAddressByStackForActiveNodes() throws Exception {
        new PathsServiceSteps()
                .getAddressByStackForActiveNodes()
                .verifyWithAddressByStack()
                .getAddressByStackForNonExistentStackNameAndVerifyResponseIsStackNotFound();
    }

    /**
     * test check for {@link StacksController#getAddressByStackOrFlavor(String, String, String)}
     * acceptedResponseType - JSON Object
     * Test case for GetAddressByStack
     */
    @Test
    public void testGetAddressByStackWithoutFlavor() throws Exception {
        new PathsServiceSteps()
                .getAddressByStackWithoutFlavor()
                .verifyWithAddressByStackWithoutFlavor();

    }

    /**
     * test check for {@link StacksController#getAddressByStackOrFlavor(String, String, String)}
     * acceptedResponseType - JSON Object
     * Test case for GetAddressByStack
     */
    @Test
    public void testGetAddressByStackForInactiveNodes() throws Exception {
        new PathsServiceSteps()
                .getAddressByStackForInactiveNodes()
                .verifyWithEmptyAddress();
    }

    /**
     * test check for {@link StacksController#getAddressByStackOrFlavor(String, String, String)}
     * acceptedResponseType - JSON Object
     * Test case for GetAddressByFlavor
     */
    @Test
    public void testGetAddressByFlavorForActiveNodes() throws Exception {
        new PathsServiceSteps()
                .getAddressByFlavorForActiveNodes()
                .verifyWithAddressByFlavor()
                .getAddressByFlavorForNonExistentServiceAndVerifyResponseIsServiceNotFound();
    }

    /**
     * test check for {@link StacksController#getAddressByStackOrFlavor(String, String, String)}
     * acceptedResponseType - JSON Object
     * Test case for GetAddressByFlavor
     */
    @Test
    public void testGetAddressByFlavorForInactiveNodes() throws Exception {
        new PathsServiceSteps()
                .getAddressByFlavorForInactiveNodes()
                .verifyWithEmptyAddress();
    }

    /**
     * test check for {@link StacksController#deleteStacks(Paths)}
     * MediaType - JSON Object
     */
    @Test
    public void testDeleteActiveStack() throws Exception {
        new PathsServiceSteps()
                .deleteActiveStack()
                .verifyStackWasNotDeleted();
    }

    /**
     * test check for {@link StacksController#deleteStacks(Paths)}
     * MediaType - JSON Object
     */
    @Test
    public void testDeleteInactiveStack() throws Exception {
        new PathsServiceSteps()
                .deleteInactiveStack()
                .verifyInactiveStackWasDeleted();
    }

    /**
     * test check for {@link StacksController#deleteStacks(Paths)}
     * MediaType - JSON Object
     */
    @Test
    public void testDeleteSeveralStacks() throws Exception {
        new PathsServiceSteps()
                .deleteActiveAndInactiveStacks()
                .verifyInactiveStackWasDeleted();
    }

    /**
     * test check for {@link StacksController#getRandomAddressByStackOrFlavor(String serviceName, String stackName, String flavorName)
     * MediaType - JSON Object
     */
    @Test
    public void testGetAddressByRandomFlavorForNonExistentService() throws Exception {

        new PathsServiceSteps()
                .getRandomAddressByFlavor()
                .verifyRandomAddressByFlavor()
                .getRandomAddressByFlavorForNonExistentServiceAndVerifyResponseIsServiceNotFound();
    }

    /**
     * test check for {@link StacksController#getRandomAddressByStackOrFlavor(String serviceName, String stackName, String flavorName)
     * MediaType - JSON Object
     */
    @Test
    public void testGetAddressByRandomFlavorForNonExistentFlavor() throws Exception {
        new PathsServiceSteps()
                .getRandomAddressByFlavor()
                .verifyRandomAddressByFlavor()
                .getRandomAddressByFlavorForNonExistentFlavorAndVerifyResponseIsFlavorNotFound();
    }

    /**
     * test check for {@link StacksController#getRandomAddressByStackOrFlavor(String serviceName, String stackName, String flavorName)
     * MediaType - JSON Object
     */
    @Test
    public void testGetRandomAddressByStack() throws Exception {
        new PathsServiceSteps()
                .getRandomAddressByStackWithoutFlavor()
                .verifyRandomAddressByStackWithoutFlavor()
                .getRandomAddressByStack()
                .verifyRandomAddressByStack()
                .getRandomAddressByFlavor()
                .verifyRandomAddressByFlavor();
    }

    private class PathsServiceSteps {
        private ServicePaths initialServicePathsWithFlavors;
        private String serviceName1;
        private String serviceName2;
        private ServicePaths pathsResponseEntityObject;
        private HostIPsListWrapper ipsResponseEntityObject;

        PathsServiceSteps() throws Exception {
            this.serviceName1 = getServiceNameForTest();
            this.serviceName2 = serviceName1 + "_2";
            initialServicePathsWithFlavors = StacksHelper.createServicePaths(serviceName1, serviceName2);
            StacksHelper.generateFlavorsForServicePaths(initialServicePathsWithFlavors);
            setupEnv();
        }

        PathsServiceSteps verifyWithExpectedPaths() throws Exception {
            Paths expectedPaths = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            StacksValidationHelper.validatePathsInResponse(expectedPaths, pathsResponseEntityObject);

            return this;
        }

        PathsServiceSteps verifyWithAllPaths() throws Exception {
            Paths expectedPaths1 = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            Paths expectedPaths2 = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName2);
            Paths actualPaths1 = StacksHelper.getPathsObjectByServiceName(pathsResponseEntityObject, serviceName1);
            Paths actualPaths2 = StacksHelper.getPathsObjectByServiceName(pathsResponseEntityObject, serviceName2);

            StacksValidationHelper.validatePaths(expectedPaths1, actualPaths1);
            StacksValidationHelper.validatePaths(expectedPaths2, actualPaths2);

            return this;
        }

        PathsServiceSteps verifyWithAddressByStack() {
            verifyAddressInResponse(2);
            return this;
        }

        PathsServiceSteps verifyWithAddressByFlavor() {
            verifyAddressInResponse(3);
            return this;
        }

        PathsServiceSteps verifyWithEmptyAddress() {
            assertEquals(0, ipsResponseEntityObject.getHostIPsList().size());
            return this;
        }

        PathsServiceSteps verifyStackWasNotDeleted() throws Exception {
            Paths expectedPaths = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            pathsResponseEntityObject = getServicePathsForServiceFromApi(serviceName1);

            StacksValidationHelper.validatePathsInResponse(expectedPaths, pathsResponseEntityObject);

            return this;
        }

        PathsServiceSteps verifyInactiveStackWasDeleted() throws Exception {
            verifyStackWasDeleted("/DataCenter2/Region2/Zone2");
            return this;
        }

        PathsServiceSteps getPaths() {
            pathsResponseEntityObject = getServicePathsForServiceFromApi(serviceName1);
            return this;
        }

        PathsServiceSteps getAllPaths() {
            pathsResponseEntityObject = apiFacade.getAllServicePaths();
            return this;
        }

        PathsServiceSteps getAddressByStackForActiveNodes() {
            getAddressByStackForService(serviceName1, "/DataCenter1/Region1/Zone1");
            return this;
        }

        PathsServiceSteps getAddressByStackWithoutFlavor() {
            getAddressByStackForService(serviceName1, "/DataCenter1/Region1");
            return this;
        }

        PathsServiceSteps getAddressByStackForInactiveNodes() {
            getAddressByStackForService(serviceName1, "/DataCenter2/Region2/Zone2");
            return this;
        }

        PathsServiceSteps getAddressByStackForNonExistentStackNameAndVerifyResponseIsStackNotFound() {
            try {
                getAddressByStackForService(serviceName1, "/DataCenter2");
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PathsServiceSteps getAddressByFlavorForActiveNodes() {
            getAddressByFlavorForService(serviceName1, "Zone1");
            return this;
        }

        PathsServiceSteps getAddressByFlavorForInactiveNodes() {
            getAddressByFlavorForService(serviceName1, "Zone2");
            return this;
        }

        PathsServiceSteps deleteActiveStack() {
            apiFacade.deleteStacksForService(serviceName1, "/DataCenter1/Region1/Zone1");
            return this;
        }

        PathsServiceSteps deleteInactiveStack() {
            apiFacade.deleteStacksForService(serviceName1, "/DataCenter2/Region2/Zone2");
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return this;
        }

        PathsServiceSteps deleteActiveAndInactiveStacks() {
            apiFacade.deleteStacksForService(serviceName1,
                    "/DataCenter1/Region1/Zone1",
                    "/DataCenter2/Region2/Zone2",
                    "/DataCenter2/Region1/Zone1");
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return this;
        }

        PathsServiceSteps getPathsForNonExistentService() {
            try {
                apiFacade.getServicePathsForService(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PathsServiceSteps getRandomAddressByFlavor() {
            ipsResponseEntityObject = apiFacade.getRandomAddressByFlavorForService(serviceName1, "Zone1");
            return this;
        }

        PathsServiceSteps verifyRandomAddressByFlavor() {
            List<HostIPs> expectedHostIps = createHostIPs();
            assertEquals(1, ipsResponseEntityObject.getHostIPsList().size());
            assertTrue(expectedHostIps.contains(ipsResponseEntityObject.getHostIPsList().get(0)));
            return this;
        }

        PathsServiceSteps getRandomAddressByFlavorForNonExistentServiceAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getRandomAddressByFlavorForService(NON_EXISTENT_SERVICENAME, "Zone1");
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PathsServiceSteps getRandomAddressByFlavorForNonExistentFlavorAndVerifyResponseIsFlavorNotFound() {
            try {
                apiFacade.getRandomAddressByFlavorForService(serviceName1, NON_EXISTENT_FLAVOR);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PathsServiceSteps getAddressByFlavorForNonExistentServiceAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getAddressByFlavorForService(NON_EXISTENT_SERVICENAME, "Zone1");
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PathsServiceSteps getRandomAddressByStackWithoutFlavor() {
            ipsResponseEntityObject = apiFacade.getRandomAddressByStackForService(serviceName2, "/DataCenter4/Region4");
            return this;
        }

        PathsServiceSteps verifyRandomAddressByStackWithoutFlavor() {
            assertEquals(1, ipsResponseEntityObject.getHostIPsList().size());
            assertEquals("1.1.1.1", ipsResponseEntityObject.getHostIPsList().get(0).getIpV4Address());
            assertEquals("1::1", ipsResponseEntityObject.getHostIPsList().get(0).getIpV6Address());
            return this;
        }

        PathsServiceSteps getRandomAddressByStack() {
            ipsResponseEntityObject = apiFacade.getRandomAddressByStackForService(serviceName1, "/DataCenter1/Region1/Zone1");
            return this;
        }

        PathsServiceSteps verifyRandomAddressByStack() {
            List<HostIPs> expectedHostIps = createHostIPs();
            assertEquals(1, ipsResponseEntityObject.getHostIPsList().size());
            assertTrue(expectedHostIps.contains(ipsResponseEntityObject.getHostIPsList().get(0)));
            return this;
        }

        PathsServiceSteps verifyWithAddressByStackWithoutFlavor() {
            verifyAddressInResponse(2);
            return this;
        }

        private List<HostIPs> createHostIPs() {
            List<HostIPs> expectedHostIps = new ArrayList<>();
            expectedHostIps.add(new HostIPs("1.1.1.1", "1::1"));
            expectedHostIps.add(new HostIPs("2.2.2.2", "2::2"));
            return expectedHostIps;
        }

        private void verifyStackWasDeleted(String stack) throws Exception {
            Paths expectedPaths = StacksHelper.getPathsObjectByServiceName(initialServicePathsWithFlavors, serviceName1);
            assertNotNull(expectedPaths);
            expectedPaths.getStacks().remove(StacksHelper.getStackItemByPath(expectedPaths, stack));
            StacksHelper.generateFlavorsForServicePaths(initialServicePathsWithFlavors);
            pathsResponseEntityObject = getServicePathsForServiceFromApi(serviceName1);

            StacksValidationHelper.validatePathsInResponse(expectedPaths, pathsResponseEntityObject);
        }

        private void verifyAddressInResponse(int size) {
            assertEquals(size, ipsResponseEntityObject.getHostIPsList().size());
            checkIPv4ParamExist(ipsResponseEntityObject.getHostIPsList(), "1.1.1.1");
            checkIPv4ParamExist(ipsResponseEntityObject.getHostIPsList(), "2.2.2.2");
            checkIPv6ParamExist(ipsResponseEntityObject.getHostIPsList(), "1::1");
            checkIPv6ParamExist(ipsResponseEntityObject.getHostIPsList(), "2::2");
        }

        private void getAddressByStackForService(String serviceName, String stack) {
            ipsResponseEntityObject = apiFacade.getAddressByStackForService(serviceName, stack);
        }

        private void getAddressByFlavorForService(String serviceName, String flavor) {
            ipsResponseEntityObject = apiFacade.getAddressByFlavorForService(serviceName, flavor);
        }

        private ServicePaths getServicePathsForServiceFromApi(String serviceName) {
            return apiFacade.getServicePathsForService(serviceName);
        }

        private void checkIPv4ParamExist(List<HostIPs> hostIPsList, String ipV4Value) throws AssertionError {
            for (HostIPs hostIPs: hostIPsList) {
                if (ipV4Value.equals(hostIPs.getIpV4Address())) {
                    return;
                }
            }
            fail("ipV4Address: " + ipV4Value + " not found in the response!");
        }

        private void checkIPv6ParamExist(List<HostIPs> hostIPsList, String ipV6Value) {
            for (HostIPs hostIPs: hostIPsList) {
                if (ipV6Value.equals(hostIPs.getIpV6Address())) {
                    return;
                }
            }
            fail("ipV6Address: " + ipV6Value + " not found in the response!");
        }

        private void setupEnv() throws Exception {
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
            waitAllStacksSetUp(WAIT_TIME, 2, serviceName2);
        }

        private void waitAllStacksSetUp (int waitTime, int hostsNumber, String serviceName) throws InterruptedException {
            long lastTestTime = System.currentTimeMillis() + waitTime;
            int count = 0;
            while(count < hostsNumber) {
                if (lastTestTime < System.currentTimeMillis()) {
                    fail("Couldn't register hosts for running test");
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
