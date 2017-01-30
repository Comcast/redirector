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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.offlineserivces;

import com.comcast.redirector.api.OfflineRestApiFacade;
import com.comcast.redirector.api.model.IfExpression;
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
public class PendingChangesOfflineControllerFlavorRulesIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testApproveFlavorRule() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceFlavorRulesSteps(serviceName)
                .createRule()
                .createSnapshotList()
                .approvePendingChanges()
                .verifyRuleChangesWereApproved();
    }

    @Test
    public void testCancelFlavorRule() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceFlavorRulesSteps(serviceName)
                .createRule()
                .createSnapshot()
                .cancelPendingChanges()
                .verifyRuleChangesWereCancelled();
    }

    private class PendingChangesOfflineServiceFlavorRulesSteps {
        private String serviceName;
        private Snapshot snapshot;
        private SnapshotList snapshotList;
        private OperationResult responseEntityObject;
        private IfExpression rule;

        PendingChangesOfflineServiceFlavorRulesSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesOfflineServiceFlavorRulesSteps createSnapshotList() {
            snapshotList = OfflineHelper.getSnapshotList(serviceName);
            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps createSnapshot() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            result.setEntityToSave(rule);
            result.setPendingChanges(OfflineHelper.getPendingChangesStatus());
            snapshot = result;

            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps createRule() {
            rule = OfflineHelper.getRule(OfflineHelper.RULE);
            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps approvePendingChanges() {
            responseEntityObject = apiFacade.approveFlavorRulePendingChangesForServiceOffline(serviceName, rule.getId(), snapshotList);
            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps cancelPendingChanges() {
            responseEntityObject = apiFacade.cancelFlavorRulePendingChangesForServiceOffline(serviceName, rule.getId(), snapshot);
            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps verifyRuleChangesWereApproved() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertTrue(rule.equals(responseEntityObject.getApprovedEntity()));
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertTrue(responseEntityObject.getPendingChanges().getPathRules().isEmpty());

            return this;
        }

        PendingChangesOfflineServiceFlavorRulesSteps verifyRuleChangesWereCancelled() {
            Assert.assertNull(responseEntityObject.getApprovedEntity());
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertTrue(responseEntityObject.getPendingChanges().getPathRules().isEmpty());

            return this;
        }
    }
}
