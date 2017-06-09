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

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import static com.comcast.redirector.api.model.factory.WhitelistFactory.createWhitelisted;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.assertEquals;

public class PendingChangesControllerWhitelistedIntegrationTest {

    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

// ************************************* WHITELISTED PENDING CHANGES TESTS ********************************** //

    /**
     * test check for {@link PendingChangesController#approveAllPendingWhitelists(String, int)}
     */
    @Test
    public void testApproveAddedPendingWhitelisted() throws Exception{
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceWhitelistedSteps(serviceName)
                .get()
                .verifyResponseIsInitialWhitelisted()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#approveAllPendingWhitelists(String, int)}
     */
    @Test
    public void testApproveUpdatedPendingWhitelists() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceWhitelistedSteps(serviceName)
                .createAndPostWhitelistedForUpdating()
                .approve()
                .get()
                .verifyResponseIsWhitelistedForUpdating()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingWhitelisted(String, int)}
     */
    @Test
    public void testCancelAddedPendingWhitelisted() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceWhitelistedSteps(serviceName)
                .createAndPostWhitelistedForCancelTest()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsModelHasNoChanges()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingWhitelisted(String, int)}
     */
    @Test
    public void testCancelUpdatedPendingWhitelisted() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceWhitelistedSteps(serviceName)
                .createAndPostWhitelistedForUpdating()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsInitialWhitelisted()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#getWhitelistedPendingPreview(String)}
     */
    @Test
    public void testWhitelistedPreviewPendingChanges() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceWhitelistedSteps(serviceName)
                .createAndPostWhitelistedForUpdating()
                .getPendingChanges()
                .verifyResponseIsPendingChangesHaveWhitelisted()
                .getWhitelistedPendingChangesPreview()
                .verifyResponsePendingChangesHavePreview()
                .getWhitelistedPendingChangesPreviewForNonExistentServiceAndVerifyResponseIsServiceNotFound();

    }

    private class PendingChangesServiceWhitelistedSteps {
        private String serviceName;
        private Whitelisted whitelisted;
        private Whitelisted responseEntityObject;
        private  Map<String, PendingChange> whitelistedMap;

        PendingChangesServiceWhitelistedSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesServiceWhitelistedSteps createAndPostWhitelistedForCancelTest() {
            whitelisted = createWhitelistedForCancelTest();
            post();
            return this;
        }

        PendingChangesServiceWhitelistedSteps createAndPostWhitelistedForUpdating() {
            whitelisted = getWhitelisedForUpdating();
            post();
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyPendingChangesIsEmpty() {
            Assert.assertTrue(apiFacade.getAllPendingChangesForService(serviceName).getWhitelisted().isEmpty());
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyResponseIsInitialWhitelisted() {
            verifyWhitelistedEquals(getInitialWhitelisted(), responseEntityObject);
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyResponseIsWhitelistedForUpdating() {
            verifyWhitelistedEquals(getWhitelisedForUpdating(), responseEntityObject);
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyResponseIsModelHasNoChanges() {
            assertEquals(2, responseEntityObject.getPaths().size());
            return this;
        }

        PendingChangesServiceWhitelistedSteps approve() {
            apiFacade.approveWhitelistedPendingChanges(serviceName);
            return this;
        }

        PendingChangesServiceWhitelistedSteps delete() throws UnsupportedEncodingException {
            apiFacade.deleteWhitelisted(serviceName, whitelisted);
            return this;
        }

        PendingChangesServiceWhitelistedSteps cancelPendingChanges() {
            apiFacade.cancelWhitelisedPendingChangesForService(serviceName);
            return this;
        }

        PendingChangesServiceWhitelistedSteps post() {
            responseEntityObject = apiFacade.postWhitelistForService(whitelisted, serviceName);
            return this;
        }

        PendingChangesServiceWhitelistedSteps get() {
            responseEntityObject = apiFacade.getWhiteListForService(serviceName);
            return this;
        }

        PendingChangesServiceWhitelistedSteps getPendingChanges() {
            whitelistedMap = apiFacade.getAllPendingChangesForService(serviceName).getWhitelisted();
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyResponseIsPendingChangesHaveWhitelisted() {
            Assert.assertEquals(1, whitelistedMap.size());
            PendingChange pendingChange = whitelistedMap.entrySet().iterator().next().getValue();
            Assert.assertEquals("/DC1/Stack1", pendingChange.getCurrentExpression().toString());
            Assert.assertEquals(ActionType.DELETE, pendingChange.getChangeType());
            return this;
        }

        PendingChangesServiceWhitelistedSteps getWhitelistedPendingChangesPreview() {
            responseEntityObject = apiFacade.getWhitelistedPendingChangesPreview(serviceName);
            return this;
        }

        PendingChangesServiceWhitelistedSteps verifyResponsePendingChangesHavePreview () {
            verifyWhitelistedEquals(getWhitelisedForUpdating(), responseEntityObject);
            return this;
        }

        PendingChangesServiceWhitelistedSteps getWhitelistedPendingChangesPreviewForNonExistentServiceAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getWhitelistedPendingChangesPreview(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        private Whitelisted getInitialWhitelisted() {
            return WhitelistedHelper.createDefaultWhitelisted();
        }

        private Whitelisted createWhitelistedForCancelTest() {
            return WhitelistedHelper.createWhitelisted("/DC0/Stack1");
        }

        private Whitelisted getWhitelisedForUpdating() {
            return createWhitelisted("/DC0/Stack0");
        }

        void verifyWhitelistedEquals(Whitelisted whitelisted, Whitelisted other) {
            Collections.sort(whitelisted.getPaths());
            Collections.sort(other.getPaths());
            assertEquals(whitelisted.getPaths(), other.getPaths());
        }
    }

    public static void setupEnv(String serviceName) throws Exception {
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("zone2")
                .withWhitelist("/DC0/Stack0", "/DC1/Stack1")
                .withDefaultUrlParams()
                .urn("any").protocol("any").port("0").ipv("4")
                .withHosts()
                .stack("/DC0/Stack0").flavor("zone2").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/DC0/Stack0").flavor("zone1").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .build();

        testHelperBuilder(context).setupEnv();
    }
}
