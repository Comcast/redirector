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

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.builders.ServerBuilder;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.ServersController;
import com.comcast.redirector.common.RedirectorConstants;
import org.junit.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class ServersControllerIntegrationTest {

    private final static String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     * test check for {@link ServersController#addServer(String, String, Server)}
     * test check for {@link ServersController#getServer(String, String)}
     */
    @Test
    public void testSaveGetServer() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceServersSteps(serviceName)
                .createAndPostCustomServer()
                .verifySuccessfulPost()
                .verifyIsDefaultServer()
                .approve()
                .verifyIsCustomServer();
    }

    @Test
    public void testGetServerIfServiceNotExists() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceServersSteps(serviceName)
                .getNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    private class PendingChangesServiceServersSteps {
        Server defaultServer;
        String serviceName;
        Server server;
        Server responseEntityObject;

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
            responseEntityObject = apiFacade.postDefaultServerForService(server, serviceName);
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

        PendingChangesServiceServersSteps verifySuccessfulPost() {
            verifyServersEqual(getCustomServer(), responseEntityObject);
            return this;
        }

        PendingChangesServiceServersSteps getNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getServerForService(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
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
