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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.cache;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.Assert;
import org.junit.Test;

import static com.comcast.redirector.common.RedirectorConstants.DEFAULT_SERVER_NAME;

public class PendingChangesIntegrationTest extends BaseWebServiceIntegrationTest {

    @Test
    public void pendingChangesApi_NOT_failing_forAppThatIsJustRegisteredInDataStore() throws Exception {
        String serviceName = "brandNewApp" + System.currentTimeMillis();
        TestContext context = new ContextBuilder()
            .withHosts()
                .stack("/dc/stack1").flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41")
                    .app(serviceName)
            .build();
        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();

        PendingChangesStatus pendingChangesStatus = restApiFacade.getPendingChangesForService(serviceName);

        Assert.assertNotNull(pendingChangesStatus);

        helper.stopDataStore();
    }

    @Test
    public void pendingChangesStatus_IsUpdated_ForJustRegisteredApp_WhenModelItemIsChanged() throws Exception {
        String serviceName = "brandNewApp" + System.currentTimeMillis();
        TestContext context = new ContextBuilder()
            .withHosts()
                .stack("/dc/stack1").flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41")
                    .app(serviceName)
            .build();
        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Server expectedPendingDefaultServer = anyDefaultServer();
        restApiFacade.postDefaultServerForService(expectedPendingDefaultServer, serviceName);

        PendingChangesStatus pendingChangesStatus = restApiFacade.getPendingChangesForService(serviceName);

        Assert.assertEquals(expectedPendingDefaultServer.getUrl(), pendingChangesStatus.getPendingDefaultServer().getUrl());

        helper.stopDataStore();
    }

    private static Server anyDefaultServer() {
        Server server = new Server("xre://sample.url");
        server.setName(DEFAULT_SERVER_NAME);

        return server;
    }
}
