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
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class FlavorRuleOfflineControllerIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testSaveFlavorRule() {
        String serviceName = getServiceNameForTest();
        new FlavorRuleOfflineServiceSteps(serviceName)
                .createRule()
                .createSnapshotForAddingRule()
                .saveRule()
                .verifyRuleAddingIsPending();
    }

    @Test
    public void testDeleteFlavorRule() {
        String serviceName = getServiceNameForTest();
        new FlavorRuleOfflineServiceSteps(serviceName)
                .createRule()
                .createSnapshotForDeletingRule()
                .deleteRule()
                .verifyRuleDeletionIsPending();
    }

    private class FlavorRuleOfflineServiceSteps {
        private String serviceName;
        private Snapshot snapshot;
        private IfExpression rule;
        private OperationResult responseEntityObject;

        FlavorRuleOfflineServiceSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public FlavorRuleOfflineServiceSteps createSnapshotForAddingRule() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            result.setEntityToSave(rule);
            result.setPendingChanges(new PendingChangesStatus());
            snapshot = result;

            return this;
        }

        public FlavorRuleOfflineServiceSteps createRule() {
            rule = OfflineHelper.getRule("ruleId");
            return this;
        }

        public FlavorRuleOfflineServiceSteps createSnapshotForDeletingRule() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            SelectServer selectServer = new SelectServer();
            selectServer.setItems(Arrays.asList(rule));
            result.setFlavorRules(selectServer);
            result.setPendingChanges(new PendingChangesStatus());
            snapshot = result;

            return this;
        }

        public FlavorRuleOfflineServiceSteps verifyRuleAddingIsPending() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertTrue(rule.equals(responseEntityObject.getApprovedEntity()));
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNotNull(responseEntityObject.getPendingChanges().getPathRules().containsKey(rule.getId()));
            Assert.assertTrue(ActionType.ADD == responseEntityObject.getPendingChanges().getPathRules().get(rule.getId()).getChangeType());
            Assert.assertTrue(rule.equals(responseEntityObject.getPendingChanges().getPathRules().get(rule.getId()).getChangedExpression()));

            return this;
        }

        public FlavorRuleOfflineServiceSteps verifyRuleDeletionIsPending() {
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNotNull(responseEntityObject.getPendingChanges().getPathRules().containsKey(rule.getId()));
            Assert.assertTrue(ActionType.DELETE == responseEntityObject.getPendingChanges().getPathRules().get(rule.getId()).getChangeType());
            Assert.assertTrue(rule.equals(responseEntityObject.getPendingChanges().getPathRules().get(rule.getId()).getCurrentExpression()));

            return this;
        }

        public FlavorRuleOfflineServiceSteps saveRule() {
            responseEntityObject = apiFacade.postFlavorRuleForServiceOffline(snapshot, rule.getId(), serviceName);
            return this;
        }

        public FlavorRuleOfflineServiceSteps deleteRule() {
            responseEntityObject = apiFacade.deleteFlavorRuleForServiceOffline(snapshot, rule.getId(), serviceName);
            return this;
        }
    }
}
