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
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.ServicePathsFactory.newServicePaths;
import static com.comcast.redirector.api.model.factory.WhitelistFactory.createWhitelisted;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class PendingChangesControllerDistributionIntegrationTest {
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";

    @Autowired
    protected IDataSourceConnector zClient;

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

// ************************************* DISTRIBUTION PENDING CHANGES TESTS ********************************** //

    /**
     *
     * test check for {@link PendingChangesController#approveAllPendingDistribution(String, int)}
     *
     */
    @Test
    public void testApproveAddedPendingDistribution() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceDistributionSteps(serviceName)
                .createInitialDistribution()
                .post()
                .approve()
                .get()
                .verifyResponseIsInitialDistribution()
                .verifyPendingChangesIsEmpty();
    }

    /**
     *
     * test check for {@link PendingChangesController#approveAllPendingDistribution(String, int)}
     *
     */
    @Test
    public void testApproveUpdatedPendingDistribution() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceDistributionSteps(serviceName)
                .createInitialDistribution()
                .post()
                .approve()
                .createDistributionForUpdating()
                .post()
                .approve()
                .get()
                .verifyResponseIsDistributionForUpdating()
                .verifyPendingChangesIsEmpty();
    }

    /**
     *
     * test check for {@link PendingChangesController#cancelPendingDistribution(String, int)}
     *
     */
    @Test
    public void testCancelAddedPendingDistribution() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceDistributionSteps(serviceName)
                .createCustomDistribution()
                .post()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsEmptyDistribution()
                .verifyPendingChangesIsEmpty();
    }

    /**
     *
     * test check for {@link PendingChangesController#approveAllPendingDistribution(String, int)}
     *
     */
    @Test
    public void testCancelUpdatedPendingDistribution() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceDistributionSteps(serviceName)
                .createInitialDistribution()
                .post()
                .approve()
                .createDistributionForUpdating()
                .post()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsInitialDistribution()
                .verifyPendingChangesIsEmpty();
    }

    /**
     *
     * test check for {@link PendingChangesController#getDistributionPendingPreview(String)
     *
     */
    @Test
    public void testDistributionPendingPreview() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceDistributionSteps(serviceName)
                .createInitialDistribution()
                .post()
                .approve()
                .getDistributionPendingChangesPreview()
                .verifyResponseIsInitialDistribution()
                .getDistributionPendingChangesPreviewForNonExistentServiceAndVerifyResponseIsServiceNotFound();
    }


    private class PendingChangesServiceDistributionSteps {
        String serviceName;
        Distribution distribution;
        Distribution responseEntityObject;

        PendingChangesServiceDistributionSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesServiceDistributionSteps createInitialDistribution() {
            distribution = getInitialDistribution();
            return this;
        }

        PendingChangesServiceDistributionSteps createDistributionForUpdating() {
            distribution = getDistributionForUpdating();
            return this;
        }

        PendingChangesServiceDistributionSteps createCustomDistribution() {
            distribution = getCustomDistribution();
            return this;
        }

        PendingChangesServiceDistributionSteps post() {
            responseEntityObject = apiFacade.postDistributionForService(distribution, serviceName);
            return this;
        }

        PendingChangesServiceDistributionSteps cancelPendingChanges() {
            apiFacade.cancelDistributionPendingChangesForService(serviceName);
            return this;
        }

        PendingChangesServiceDistributionSteps verifyPendingChangesIsEmpty() {
            assertTrue(apiFacade.getAllPendingChangesForService(serviceName).getDistributions().isEmpty());
            return this;
        }

        PendingChangesServiceDistributionSteps verifyResponseIsInitialDistribution() {
            verifyDistributionEquals(getInitialDistribution(), responseEntityObject);
            return this;
        }

        PendingChangesServiceDistributionSteps verifyResponseIsDistributionForUpdating() {
            verifyDistributionEquals(getDistributionForUpdating(), responseEntityObject);
            return this;
        }

        PendingChangesServiceDistributionSteps verifyResponseIsEmptyDistribution() {
            Assert.assertEquals(0, responseEntityObject.getRules().size());
            return this;
        }

        PendingChangesServiceDistributionSteps approve() {
            apiFacade.approveDistributionForService(serviceName);
            return this;
        }

        PendingChangesServiceDistributionSteps get() {
            responseEntityObject = apiFacade.getDistributionForService(serviceName);
            return this;
        }

        PendingChangesServiceDistributionSteps getDistributionPendingChangesPreview() {
            responseEntityObject = apiFacade.getDistributionPendingChangesPreview(serviceName);
            return this;
        }

        PendingChangesServiceDistributionSteps getDistributionPendingChangesPreviewForNonExistentServiceAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getDistributionPendingChangesPreview(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }
        private Distribution getCustomDistribution() {
            String STACK0 = "/PO/POC1/1.0";
            String STACK1 = "/PO/POC1/1.1";
            String STACK2 = "/PO/POC1/1.2";
            String STACK3 = "/PO/POC1/1.3";
            String STACK4 = "/PO/POC2/1.4";
            String STACK5 = "/PO/POC5/1.5";
            String STACK6 = "/PO/POC5/1.6";

            Map<String, Integer> paths = new HashMap<>();
            paths.put(STACK0, 1);
            paths.put(STACK1, 1);
            paths.put(STACK2, 1);
            paths.put(STACK3, 1);
            paths.put(STACK4, 1);
            paths.put(STACK5, 1); // active but not whitelisted
            paths.put(STACK6, 0); // whitelisted but not active

            // save service path with active stacks first
            ServicePaths servicePaths = newServicePaths(serviceName, paths);
            StacksHelper.zkPostStacks(zClient, servicePaths);

            // make stack whitelisted
            Whitelisted whitelisted = createWhitelisted(STACK1.substring(0, STACK1.lastIndexOf("/")), STACK5.substring(0, STACK5.lastIndexOf("/")));
            WhitelistedHelper.postAndApprove(HttpTestServerHelper.target(), serviceName, whitelisted);

            DistributionBuilder builder = new DistributionBuilder();
            Distribution result = builder.
                    withRule(newDistributionRule(1, 5f, newSimpleServerForFlavor("1.1"))).
                    withDefaultServer(newSimpleServerForFlavor("1.0")).
                    build();

            return result;
        }
        
        private Distribution getInitialDistribution() {
            DistributionBuilder builder = new DistributionBuilder();
            builder.withRule(newDistributionRule(0, 10.3f, newSimpleServerForFlavor("/DataCenter1/Region1/Zone1")));
            builder.withRule(newDistributionRule(1, 25.5f, newSimpleServerForFlavor("/DataCenter2/Region1/Zone1")));
            return builder.build();
        }

        private Distribution getDistributionForUpdating() {
            DistributionBuilder builder = new DistributionBuilder();
            builder.withRule(newDistributionRule(0, 10f, newSimpleServerForFlavor("Zone1")));
            return builder.build();
        }

        void verifyDistributionEquals(Distribution distribution, Distribution other) {
            assertEquals(distribution.getRules().size(), other.getRules().size());
            assertEquals(distribution.getRules(), other.getRules());
        }

    }

    public static void setupEnv(String serviceName) throws Exception {
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("zone2")
                .withWhitelist("/DataCenter1/Region1", "/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2", "/DataCenter2/Zone2")
                .withDefaultUrlParams()
                .urn("any").protocol("any").port("0").ipv("4")
                .withHosts()
                .stack("/DataCenter2/Region1").flavor("zone2").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/DataCenter1/Region1").flavor("Zone1").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/DataCenter2/Region1").flavor("Zone1").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .build();

        testHelperBuilder(context).setupEnv();
    }

}
