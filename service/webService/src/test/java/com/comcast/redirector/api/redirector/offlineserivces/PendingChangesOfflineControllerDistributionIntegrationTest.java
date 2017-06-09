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
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.service.pending.entityview.DistributionHelper.prepareDistribution;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class PendingChangesOfflineControllerDistributionIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testApprovePendingDistribution() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceDistributionSteps(serviceName)
                .createSnapshotList()
                .approvePendingChanges()
                .verifyDistributionChangesWereApproved();
    }

    @Test
    public void testCancelPendingDistribution() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceDistributionSteps(serviceName)
                .createSnapshot()
                .cancelPendingChanges()
                .verifyDistributionChangesWereCancelled();
    }

    private class PendingChangesOfflineServiceDistributionSteps {
        private PendingChangesBatchOperationResult approveResponse;
        private OperationResult cancelResponse;
        private SnapshotList snapshotList;
        private Snapshot snapshot;
        private String serviceName;

        PendingChangesOfflineServiceDistributionSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesOfflineServiceDistributionSteps createSnapshotList() {
            snapshotList = OfflineHelper.getSnapshotList(serviceName);
            return this;
        }

        PendingChangesOfflineServiceDistributionSteps createSnapshot() {
            Snapshot result = new Snapshot();
            result.setPendingChanges(OfflineHelper.getPendingChangesStatus());
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            snapshot = result;

            return this;
        }

        PendingChangesOfflineServiceDistributionSteps approvePendingChanges() {
            approveResponse = apiFacade.approveDistributionPendingChangesForServiceOffline(serviceName, snapshotList);
            return this;
        }

        PendingChangesOfflineServiceDistributionSteps cancelPendingChanges() {
            cancelResponse = apiFacade.cancelDistributionPendingChangesForServiceOffline(serviceName, snapshot);
            return this;
        }

        PendingChangesOfflineServiceDistributionSteps verifyDistributionChangesWereApproved() {
            Distribution expected = new Distribution();
            prepareDistribution(expected, 0, 10.0f, "flavoursPathValue1");
            prepareDistribution(expected, 1, 20.0f, "flavoursPathValue2");

            assertTrue(expected.equals(approveResponse.getDistribution().getEntitiesToSave().get(0)));
            assertTrue(approveResponse.getPendingChangesStatus().getDistributions().isEmpty());

            return this;
        }

        PendingChangesOfflineServiceDistributionSteps verifyDistributionChangesWereCancelled() {
            assertNull(cancelResponse.getApprovedEntity());
            assertTrue(cancelResponse.getPendingChanges().getDistributions().isEmpty());

            return this;
        }

        private Distribution getDistribution() {
            return prepareDistribution(new HashMap<Float, String>() {{
                put(10.0f, "flavoursPathValue2");
                put(30.0f, "flavoursPathValue3");
            }});
        }
    }
}
