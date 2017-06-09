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
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class PendingChangesOfflineControllerDefaultServerIntegrationTest {
    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testApproveDefaultServer() {
        final String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceDefaultServerSteps(serviceName)
                .createSnapshotList()
                .approveDefaultServerChanges()
                .verifyDefaultServerChangesWereApproved();
    }

    @Test
    public void testCancelDefaultServer() {
        final String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceDefaultServerSteps(serviceName)
                .createSnapshot()
                .cancelDefaultServerChanges()
                .verifyDefaultServerChangesWereCancelled();
    }


    public class PendingChangesOfflineServiceDefaultServerSteps {
        private Server newServer = OfflineHelper.getServer("flavoursPathValue1");
        private String serviceName;
        private OperationResult responseEntityObject;
        private SnapshotList snapshotList;
        private Snapshot snapshot;

        public PendingChangesOfflineServiceDefaultServerSteps(final String serviceName) {
            this.serviceName = serviceName;
        }

        public PendingChangesOfflineServiceDefaultServerSteps createSnapshotList() {
            snapshotList = OfflineHelper.getSnapshotListForDefaultServer(serviceName);

            return this;
        }

        public PendingChangesOfflineServiceDefaultServerSteps createSnapshot() {
            snapshot = new Snapshot();
            snapshot.setPendingChanges(OfflineHelper.getPendingChangesStatusDefaultServer());

            return this;
        }

        public PendingChangesOfflineServiceDefaultServerSteps approveDefaultServerChanges() {
            responseEntityObject = apiFacade.approveDefaultServerForService(serviceName, snapshotList);

            return this;
        }

        public PendingChangesOfflineServiceDefaultServerSteps cancelDefaultServerChanges() {
            responseEntityObject = apiFacade.cancelDefaultServerForService(serviceName, snapshot);

            return this;
        }

        public PendingChangesOfflineServiceDefaultServerSteps verifyDefaultServerChangesWereApproved() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertTrue(newServer.equals(responseEntityObject.getApprovedEntity()));
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNull(responseEntityObject.getPendingChanges().getServers().get(RedirectorConstants.DEFAULT_SERVER_NAME));

            return this;
        }

        public PendingChangesOfflineServiceDefaultServerSteps verifyDefaultServerChangesWereCancelled() {
            Assert.assertNull(responseEntityObject.getApprovedEntity());
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNull(responseEntityObject.getPendingChanges().getServers().get(RedirectorConstants.DEFAULT_SERVER_NAME));

            return this;
        }
    }
}
