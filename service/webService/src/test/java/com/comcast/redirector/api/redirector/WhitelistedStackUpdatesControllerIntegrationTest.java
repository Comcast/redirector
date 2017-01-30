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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.whitelisted.WhitelistUpdate;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.api.redirector.controllers.WhitelistedController;
import org.junit.*;

import java.util.List;

import static com.comcast.redirector.api.model.factory.WhitelistFactory.createWhitelisted;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import  static org.junit.Assert.*;

public class WhitelistedStackUpdatesControllerIntegrationTest {

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    /**
     * test check for {@link WhitelistedController#saveWhitelistedStacks(Whitelisted, javax.ws.rs.core.UriInfo, String)}
     */
    @Test
    public void testSaveApproveGetWhitelistedUpdates() {
        String serviceName = getServiceNameForTest();
        new WhitelistedStackUpdatesSteps(serviceName)
                .postInitialWhitelisted()
                .approve()
                .getWhitelistUpdates()
                .verifyUpdatesWithAddAction();
    }

    /**
     * test check for {@link WhitelistedController#saveWhitelistedStacks(Whitelisted, javax.ws.rs.core.UriInfo, String)}
     */
    @Test
    public void testSaveApproveGetWhitelistedUpdatesWithDifferentStatuses() {
        String serviceName = getServiceNameForTest();
        new WhitelistedStackUpdatesSteps(serviceName)
                .postInitialWhitelisted()
                .getWhitelistUpdates()
                .verifyUpdatesEmpty()
                .approve()
                .getWhitelistUpdates()
                .verifyUpdatesWithAddAction()
                .postWhitelistedWithDeletedStack()
                .approve()
                .getWhitelistUpdates()
                .verifyUpdatesWithDeleteAction();
    }

    private class WhitelistedStackUpdatesSteps {
        String serviceName;
        List<WhitelistUpdate> responseEntityObject;

        WhitelistedStackUpdatesSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public WhitelistedStackUpdatesSteps getWhitelistUpdates() {
            responseEntityObject = apiFacade.getWhitelistUpdatesForService(serviceName);
            return this;
        }

        public WhitelistedStackUpdatesSteps approve() {
            apiFacade.approveWhitelistedPendingChanges(serviceName);
            return this;
        }

        WhitelistedStackUpdatesSteps postInitialWhitelisted() {
            apiFacade.postWhitelistForService(getInitialWhitelisted(), serviceName);
            return this;
        }

        WhitelistedStackUpdatesSteps postWhitelistedWithDeletedStack() {
            apiFacade.postWhitelistForService(getWhitelistedWithRemovedStack(), serviceName);
            return this;
        }

        WhitelistedStackUpdatesSteps verifyUpdatesWithAddAction() {
            assertNotNull(responseEntityObject);
            assertEquals(2, responseEntityObject.size());
            assertEquals(ActionType.ADD, responseEntityObject.get(0).getAction());
            assertEquals(ActionType.ADD, responseEntityObject.get(1).getAction());
            return this;
        }

        WhitelistedStackUpdatesSteps verifyUpdatesWithDeleteAction() {
            assertNotNull(responseEntityObject);
            assertEquals(2, responseEntityObject.size());
            assertEquals(ActionType.ADD, responseEntityObject.get(0).getAction());
            assertEquals(ActionType.DELETE, responseEntityObject.get(1).getAction());
            return this;
        }

        WhitelistedStackUpdatesSteps verifyUpdatesEmpty() {
            assertEquals(0, responseEntityObject.size());
            return this;
        }

        private Whitelisted getInitialWhitelisted() {
            return createWhitelisted("/DC0/Stack0", "/DC1/Stack1");
        }

        private Whitelisted getWhitelistedWithRemovedStack() {
            return createWhitelisted("/DC0/Stack0");
        }
    }


}
