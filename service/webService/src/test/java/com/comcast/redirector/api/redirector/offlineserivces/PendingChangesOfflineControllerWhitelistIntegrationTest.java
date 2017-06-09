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
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class PendingChangesOfflineControllerWhitelistIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testApproveWhitelist() throws SerializerException {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceWhitelistSteps(serviceName)
                .createSnapshotList()
                .approvePendingChanges()
                .verifyWhitelistedChangesWereApproved();
    }

    private class PendingChangesOfflineServiceWhitelistSteps {
        private String serviceName;
        private SnapshotList snapshotList;
        private OperationResult responseEntityObject;

        PendingChangesOfflineServiceWhitelistSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public PendingChangesOfflineServiceWhitelistSteps createSnapshotList() {
            snapshotList = OfflineHelper.getSnapshotList(serviceName);
            return this;
        }

        public PendingChangesOfflineServiceWhitelistSteps approvePendingChanges() {
            responseEntityObject = apiFacade.approveWhitelistPendingChangesForServiceOffline(serviceName, snapshotList);
            return this;
        }

        public PendingChangesOfflineServiceWhitelistSteps verifyWhitelistedChangesWereApproved() {
            Whitelisted expected = OfflineHelper.getWhitelisted();

            assertEquals(3, ((Whitelisted) responseEntityObject.getApprovedEntity()).getPaths().size());
            assertTrue(expected.getPaths().contains(((Whitelisted) responseEntityObject.getApprovedEntity()).getPaths().get(0)));
            assertTrue(expected.getPaths().contains(((Whitelisted) responseEntityObject.getApprovedEntity()).getPaths().get(1)));

            return this;
        }
    }
}
