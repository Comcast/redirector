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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.WhitelistedHelper;
import com.comcast.redirector.api.redirector.controllers.WhitelistedController;
import org.junit.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class WhitelistedControllerIntegrationTest {
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    /**
     * test check for {@link WhitelistedController#saveWhitelistedStacks(Whitelisted, javax.ws.rs.core.UriInfo, String)}
     */
    @Test
    public void testSaveGetWhitelisted() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        new WhitelistedSteps(serviceName)
                .createInitialWhitelisted()
                .postWhitelisted()
                .verifySuccessfulPost()
                .verifyNotApplied()
                .approve()
                .verifyApplied();
    }

    /**
     * test check for whitelisted stack that have the same hashCode of path
     * {@see APPDS-2605}
     */
    @Test
    public void testSaveGetWhitelistedWithSamePathHashCodes() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        new WhitelistedSteps(serviceName)
                .createWhitelistedWithSameHashes()
                .postWhitelisted()
                .verifySuccessfulPost()
                .verifyNotApplied()
                .approve()
                .verifyApplied();
    }

    /**
     * test check for {@link WhitelistedController#addWhitelistedStacks(Whitelisted, javax.ws.rs.core.UriInfo, String)}
     */

    @Test
    public void testAddWhitelistedStacks() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new WhitelistedSteps(serviceName)
                .createWhitelistedForUpdating()
                .postWhitelisted()
                .createWhitelistedForAddingStacks()
                .addStacks()
                .verifySuccessfulPost()
                .verifyWithInitialStacks()
                .approve()
                .verifyWithInitialAndAddedStacks();
    }

    /**
     * test check for {@link WhitelistedController#deleteWhitelistedStacks(String, String)}
     */
    @Test
    public void testDeleteWhitelistedStacks() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new WhitelistedSteps(serviceName)
                .createWhitelistedForAddingStacks()
                .addStacks()
                .approve()
                .createWhitelistedForUpdating()
                .postWhitelisted()
                .delete()
                .verifyWithInitialAndAddedStacks()
                .approve()
                .verifyWithAddedStacks();
    }

    @Test
    public void testGetWhitelistedStacksForNonExistentService() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        new WhitelistedSteps(serviceName)
                .getWhitelistedAndVerifyResponse()
                .getWhitelistedNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    @Test
    public void testAddWhitelistedStacksForNonExistentService() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        new WhitelistedSteps(serviceName)
                .createWhitelistedForAddingStacks()
                .addStacksAndVerifyResponse()
                .addStacksNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    private class WhitelistedSteps {
        private String serviceName;
        private Whitelisted whitelisted;
        private Whitelisted responseEntityObject;

        WhitelistedSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        WhitelistedSteps postWhitelisted() {
            responseEntityObject = apiFacade.postWhitelistForService(whitelisted, serviceName);
            return this;
        }

        WhitelistedSteps addStacks() {
            responseEntityObject = apiFacade.addStacks(serviceName, whitelisted);
            return this;
        }

        WhitelistedSteps createInitialWhitelisted() {
            whitelisted = getInitialWhitelisted();
            return this;
        }

        WhitelistedSteps createWhitelistedForAddingStacks() {
            whitelisted = getWhitelistedForAddingStacks();
            return this;
        }

        WhitelistedSteps createWhitelistedWithSameHashes() {
            whitelisted = getInitialWhitelistedWithSameHashes();
            return this;
        }

        WhitelistedSteps createWhitelistedForUpdating() {
            whitelisted = getWhitelisedForUpdating();
            return this;
        }

        WhitelistedSteps verifySuccessfulPost() {
            verifyWhitelistedEquals(whitelisted, responseEntityObject);
            return this;
        }

        WhitelistedSteps verifyNotApplied() {
            assertEquals(5, getWhitelistedFromApi().getPaths().size());
            return this;
        }

        WhitelistedSteps verifyWithInitialStacks() {
            verifyWhitelistedEquals(getExpectedWhitelisted(), getWhitelistedFromApi());
            return this;
        }

        WhitelistedSteps verifyApplied() {
            verifyWhitelistedEquals(whitelisted, getWhitelistedFromApi());
            return this;
        }

        WhitelistedSteps verifyWithInitialAndAddedStacks() {
            List<String> allPaths = Stream.concat(
                    getExpectedWhitelisted().getPaths().stream(),
                    getWhitelistedForAddingStacks().getPaths().stream())
                    .collect(Collectors.toList());
            Whitelisted whitelistedWithAllStacks = new Whitelisted(allPaths);

            verifyWhitelistedEquals(whitelistedWithAllStacks, getWhitelistedFromApi());
            return this;
        }

        WhitelistedSteps verifyWithAddedStacks() {
            verifyWhitelistedEquals(getExpectedWhitelisted(), getWhitelistedFromApi());
            return this;
        }

        WhitelistedSteps approve() {
            apiFacade.approveWhitelistedPendingChanges(serviceName);
            return this;
        }

        WhitelistedSteps delete() throws UnsupportedEncodingException {
            apiFacade.deleteWhitelisted(serviceName, getWhitelistedForAddingStacks());
            return this;
        }

        WhitelistedSteps getWhitelistedAndVerifyResponse() {
            Whitelisted actualWhitelisted = getWhitelistedFromApi();
            Whitelisted expectedWhitelisted = getExpectedWhitelisted();
            verifyWhitelistedEquals(expectedWhitelisted, actualWhitelisted);
            return this;
        }

        WhitelistedSteps getWhitelistedNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getWhiteListForService(NON_EXISTENT_SERVICENAME);
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        WhitelistedSteps addStacksAndVerifyResponse() {
            responseEntityObject = apiFacade.addStacks(serviceName, whitelisted);
            verifyWhitelistedEquals(responseEntityObject, whitelisted);
            return this;
        }

        WhitelistedSteps addStacksNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.addStacks(NON_EXISTENT_SERVICENAME, whitelisted);
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        private Whitelisted getWhitelistedFromApi() {
            return apiFacade.getWhiteListForService(serviceName);
        }

        private Whitelisted getInitialWhitelisted() {
            return WhitelistedHelper.createWhitelisted("/DC0/Stack0", "/DC1/Stack1", "/DataCenter1/Region1", "/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2", "/DataCenter2/Zone2");
        }

        private Whitelisted getInitialWhitelistedWithSameHashes() {
            return WhitelistedHelper.createWhitelisted("/DC0/Stack0", "/DC1/Stack1", "/DataCenter1/Region1",
                    "/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2", "/DataCenter2/Zone2",
                    "/aw/aw-c8", "/aw/aw-av");//those two strings have the same hashes
        }

        private Whitelisted getWhitelistedForAddingStacks() {
            return WhitelistedHelper.createWhitelisted("/DC0/Stack0", "/DC1/Stack1");
        }

        private Whitelisted getExpectedWhitelisted() {
            return WhitelistedHelper.createWhitelisted("/DataCenter1/Region1", "/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2", "/DataCenter2/Zone2");
        }

        private Whitelisted getWhitelisedForUpdating() {
            return WhitelistedHelper.createWhitelisted("/DC0/Stack0");
        }

        void verifyWhitelistedEquals(Whitelisted whitelisted, Whitelisted other) {
            Collections.sort(whitelisted.getPaths());
            Collections.sort(other.getPaths());
            assertEquals(whitelisted.getPaths(), other.getPaths());
        }
    }
}
