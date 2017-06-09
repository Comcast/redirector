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
public class PendingChangesOfflineControllerUrlRulesIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testApproveUrlRule() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceUrlRulesSteps(serviceName)
                .createRule()
                .createSnapshotList()
                .approvePendingChanges()
                .verifyRuleChangesWereApproved();
    }

    @Test
    public void testCancelUrlRule() {
        String serviceName = getServiceNameForTest();
        new PendingChangesOfflineServiceUrlRulesSteps(serviceName)
                .createRule()
                .createSnapshot()
                .cancelPendingChanges()
                .verifyRuleChangesWereCancelled();
    }

    private class PendingChangesOfflineServiceUrlRulesSteps {
        private String serviceName;
        private Snapshot snapshot;
        private SnapshotList snapshotList;
        private OperationResult responseEntityObject;
        private IfExpression rule;

        PendingChangesOfflineServiceUrlRulesSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesOfflineServiceUrlRulesSteps createSnapshotList() {
            snapshotList = OfflineHelper.getSnapshotList(serviceName);
            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps createSnapshot() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            result.setEntityToSave(rule);
            result.setPendingChanges(OfflineHelper.getPendingChangesStatus());
            snapshot = result;

            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps createRule() {
            rule = OfflineHelper.getRule(OfflineHelper.RULE_URL);
            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps approvePendingChanges() {
            responseEntityObject = apiFacade.approveUrlRulesPendingChangesForServiceOffline(serviceName, rule.getId(), snapshotList);
            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps cancelPendingChanges() {
            responseEntityObject = apiFacade.cancelUrlRulesPendingChangesForServiceOffline(serviceName, rule.getId(), snapshot);
            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps verifyRuleChangesWereApproved() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertTrue(rule.equals(responseEntityObject.getApprovedEntity()));
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertTrue(responseEntityObject.getPendingChanges().getUrlRules().isEmpty());

            return this;
        }

        PendingChangesOfflineServiceUrlRulesSteps verifyRuleChangesWereCancelled() {
            Assert.assertNull(responseEntityObject.getApprovedEntity());
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertTrue(responseEntityObject.getPendingChanges().getUrlRules().isEmpty());

            return this;
        }
    }
}
