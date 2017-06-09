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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.offlineserivces;

import com.comcast.redirector.api.OfflineRestApiFacade;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class DefaultServerOfflineControllerIntegrationTest {
    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testSaveFlavorRule() {
        final String serviceName = getServiceNameForTest();
        new DefaultServerOfflineSteps(serviceName)
                .createSnapshot()
                .postDefaultService()
                .verifyOperationResult();
    }

    class DefaultServerOfflineSteps {
        private final Server serverToSave = OfflineHelper.getServer("flavoursPathValue1");
        private final Server currentServer = OfflineHelper.getServer("flavoursPathValue2");
        private String serviceName;
        private Snapshot snapshot;
        private OperationResult responseEntityObject;

        public DefaultServerOfflineSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public DefaultServerOfflineSteps createSnapshot() {
            snapshot = new Snapshot();
            snapshot.setEntityToSave(serverToSave); // set new server
            snapshot.setServers(currentServer); // set current server
            snapshot.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            snapshot.setPendingChanges(new PendingChangesStatus());

            return this;
        }

        public DefaultServerOfflineSteps postDefaultService() {
            responseEntityObject = apiFacade.postDefaultServerForService(snapshot, serviceName);

            return this;
        }

        public DefaultServerOfflineSteps verifyOperationResult() {
            Expressions approvedEntity = responseEntityObject.getApprovedEntity();

            Assert.assertNotNull(approvedEntity);
            Assert.assertTrue(serverToSave.equals(approvedEntity));
            Assert.assertTrue(!responseEntityObject.getPendingChanges().getServers().isEmpty());
            Assert.assertTrue(serverToSave.equals(responseEntityObject.getPendingChanges().getServers().get(RedirectorConstants.DEFAULT_SERVER_NAME).getChangedExpression()));
            Assert.assertTrue(currentServer.equals(responseEntityObject.getPendingChanges().getServers().get(RedirectorConstants.DEFAULT_SERVER_NAME).getCurrentExpression()));

            return this;
        }
    }
}
