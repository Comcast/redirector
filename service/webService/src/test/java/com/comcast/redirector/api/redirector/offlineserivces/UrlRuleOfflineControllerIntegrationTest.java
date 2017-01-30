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
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
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
public class UrlRuleOfflineControllerIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testSaveUrlRule() {
        String serviceName = getServiceNameForTest();
        new UrlRuleOfflineServiceSteps(serviceName)
                .createRule()
                .createSnapshotForAddingRule()
                .saveRule()
                .verifyRuleAddingIsPending();
    }

    @Test
    public void testDeleteUrlRule() {
        String serviceName = getServiceNameForTest();
        new UrlRuleOfflineServiceSteps(serviceName)
                .createRule()
                .createSnapshotForDeletingRule()
                .deleteRule()
                .verifyRuleDeletionIsPending();
    }

    private class UrlRuleOfflineServiceSteps {
        private String serviceName;
        private Snapshot snapshot;
        private IfExpression rule;
        private OperationResult responseEntityObject;

        UrlRuleOfflineServiceSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public UrlRuleOfflineServiceSteps createSnapshotForAddingRule() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            result.setEntityToSave(rule);
            result.setPendingChanges(new PendingChangesStatus());
            snapshot = result;

            return this;
        }

        public UrlRuleOfflineServiceSteps createRule() {
            rule = getRule("ruleId");
            return this;
        }

        public UrlRuleOfflineServiceSteps createSnapshotForDeletingRule() {
            Snapshot result = new Snapshot();
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            URLRules urlRule = new URLRules();
            urlRule.setItems(Arrays.asList(rule));
            result.setUrlRules(urlRule);
            result.setEntityToSave(rule);
            result.setPendingChanges(new PendingChangesStatus());
            snapshot = result;

            return this;
        }

        public UrlRuleOfflineServiceSteps verifyRuleAddingIsPending() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertTrue(rule.equals(responseEntityObject.getApprovedEntity()));
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNotNull(responseEntityObject.getPendingChanges().getPathRules().containsKey(rule.getId()));
            Assert.assertTrue(ActionType.ADD == responseEntityObject.getPendingChanges().getUrlRules().get(rule.getId()).getChangeType());
            Assert.assertTrue(rule.equals(responseEntityObject.getPendingChanges().getUrlRules().get(rule.getId()).getChangedExpression()));

            return this;
        }

        public UrlRuleOfflineServiceSteps verifyRuleDeletionIsPending() {
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNotNull(responseEntityObject.getPendingChanges().getPathRules().containsKey(rule.getId()));
            Assert.assertTrue(ActionType.DELETE == responseEntityObject.getPendingChanges().getUrlRules().get(rule.getId()).getChangeType());
            Assert.assertTrue(rule.equals(responseEntityObject.getPendingChanges().getUrlRules().get(rule.getId()).getCurrentExpression()));

            return this;
        }

        public UrlRuleOfflineServiceSteps saveRule() {
            responseEntityObject = apiFacade.postUrlRuleForServiceOffline(serviceName, rule.getId(), snapshot);
            return this;
        }

        public UrlRuleOfflineServiceSteps deleteRule() {
            responseEntityObject = apiFacade.deleteUrlRuleForServiceOffline(serviceName, rule.getId(), snapshot);
            return this;
        }

        private IfExpression getRule(String ruleId) {
            UrlRule urlRule = new UrlRule();
            urlRule.setProtocol("xre");
            urlRule.setPort("8080");
            urlRule.setUrn("shell");
            urlRule.setIpProtocolVersion("4");

            Equals equals = new Equals();
            equals.setParam("a");
            equals.setValue("1");

            IfExpression rule = new IfExpression();
            rule.setId(ruleId);
            rule.setItems(Arrays.<Expressions>asList(equals));
            rule.setReturn(urlRule);

            return rule;
        }
    }
}
