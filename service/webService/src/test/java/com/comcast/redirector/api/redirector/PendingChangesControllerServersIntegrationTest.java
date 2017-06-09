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

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.builders.ServerBuilder;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import com.comcast.redirector.common.RedirectorConstants;
import org.junit.*;


import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class PendingChangesControllerServersIntegrationTest {

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }


// ************************************* RULES PENDING CHANGES TESTS ********************************** //
    /**
     * test check for {@link PendingChangesController#approvePendingServer(String,  int)}
     */
    @Test
    public void testApprovePendingServer() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceServersSteps(serviceName)
                .createAndPostCustomServer()
                .approve()
                .verifyIsCustomServer()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingServer(String, int)}
     */
    @Test
    public void testCancelPendingServer() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceServersSteps(serviceName)
                .createAndPostCustomServer()
                .cancelPendingChanges()
                .verifyIsDefaultServer()
                .verifyPendingChangesIsEmpty();
    }

    private class PendingChangesServiceServersSteps {
        Server defaultServer;
        String serviceName;
        Server server;

        PendingChangesServiceServersSteps(String serviceName) {
            this.serviceName = serviceName;
            this.defaultServer = getServerFromApi();
        }

        PendingChangesServiceServersSteps approve() {
            apiFacade.approveServerPendingChangesForService(serviceName, server.getName());
            return this;
        }

        PendingChangesServiceServersSteps createAndPostCustomServer() {
            server = getCustomServer();
            apiFacade.postDefaultServerForService(server, serviceName);
            return this;
        }

        PendingChangesServiceServersSteps cancelPendingChanges() {
            apiFacade.cancelServerPendingChangesForService(serviceName);
            return this;
        }

        PendingChangesServiceServersSteps verifyIsCustomServer() throws InstantiationException, IllegalAccessException {
            verifyServersEqual(getCustomServer(), getServerFromApi());
            return this;
        }

        PendingChangesServiceServersSteps verifyIsDefaultServer() {
            verifyServersEqual(defaultServer, getServerFromApi());
            return this;
        }

        PendingChangesServiceServersSteps verifyPendingChangesIsEmpty() {
            Assert.assertTrue(apiFacade.getAllPendingChangesForService(serviceName).getServers().isEmpty());
            return this;
        }

        private Server getServerFromApi() {
            return apiFacade.getServerForService(serviceName);
        }

        private Server getCustomServer() {
            return new ServerBuilder()
                    .withName(RedirectorConstants.DEFAULT_SERVER_NAME)
                    .withFlavor("zone1").build();
        }

        void verifyServersEqual(Server server, Server other) {
            if (RedirectorConstants.DEFAULT_SERVER_NAME.equals(server.getName())) {
                server.setName("default");
                server.setDescription("Default server route");
            }
            assertEquals(server, other);
        }
    }
}
