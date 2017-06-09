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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class PendingChangesControllerRulesIntegrationTest {

    private RestApiFacade apiFacade;
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

// ************************************* RULES PENDING CHANGES TESTS ********************************** //

    /**
     * test check for {@link PendingChangesController#approvePendingRule(String, String, int)}
     */
    @Test
    public void testApproveAddedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .approve()
                .get()
                .verifyResponseIsInitialRule()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#approvePendingRule(String, String, int)}
     */
    @Test
    public void testApproveUpdatedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .approve()
                .createRuleForUpdating()
                .post()
                .approve()
                .get()
                .verifyResponseIsRuleForUpdating()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#approvePendingRule(String, String, int)}
     */
    @Test
    public void testApproveDeletedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .approve()
                .delete()
                .approve()
                .getRulesAndVerifyResponseIsRuleNotFound()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingRule(String, String, int)}
     */
    @Test
    public void testCancelAddedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .cancelPendingChanges()
                .getRulesAndVerifyResponseIsRuleNotFound()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingRule(String, String, int)}
     */
    @Test
    public void testCancelUpdatedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .approve()
                .createRuleForUpdating()
                .post()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsInitialRule()
                .verifyPendingChangesIsEmpty();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingRule(String, String, int)}
     */
    @Test
    public void testCancelDeletedPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .approve()
                .delete()
                .cancelPendingChanges()
                .get()
                .verifyResponseIsInitialRule()
                .verifyPendingChangesIsEmpty();
    }

    @Test
    public void testNewRuleIdsPendingRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceRulesSteps(serviceName)
                .createInitialRule()
                .post()
                .getNewRuleIds()
                .verifyNewRuleIdsForPathRules()
                .getNewRuleIdsForNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    private class PendingChangesServiceRulesSteps {
        String serviceName;
        IfExpression rule;
        IfExpression responseEntityObject;
        RuleIdsWrapper newRuleIds;

        PendingChangesServiceRulesSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesServiceRulesSteps createInitialRule() throws InstantiationException, IllegalAccessException {
            rule = getInitialRule();
            return this;
        }

        PendingChangesServiceRulesSteps createRuleForUpdating() throws InstantiationException, IllegalAccessException {
            rule = getRuleForUpdating();
            return this;
        }

        PendingChangesServiceRulesSteps post() {
            responseEntityObject = apiFacade.postFlavorRuleForService(rule, serviceName);
            return this;
        }

        PendingChangesServiceRulesSteps approve() {
            apiFacade.approveFlavorRulePendingChanges(serviceName, rule);
            return this;
        }

        PendingChangesServiceRulesSteps delete() {
            apiFacade.deleteFlavorRule(serviceName, rule);
            return this;
        }

        PendingChangesServiceRulesSteps verifyResponseIsInitialRule() throws InstantiationException, IllegalAccessException {
            verifyRulesEqual(getInitialRule(), responseEntityObject);
            return this;
        }

        PendingChangesServiceRulesSteps verifyResponseIsRuleForUpdating() throws InstantiationException, IllegalAccessException {
            verifyRulesEqual(getRuleForUpdating(), responseEntityObject);
            return this;
        }

        PendingChangesServiceRulesSteps cancelPendingChanges() {
            apiFacade.cancelFlavorRulePendingChangesForService(serviceName, rule.getId());
            return this;
        }

        PendingChangesServiceRulesSteps getRulesAndVerifyResponseIsRuleNotFound() {
            try {
                apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PendingChangesServiceRulesSteps verifyPendingChangesIsEmpty() {
            Assert.assertTrue(apiFacade.getAllPendingChangesForService(serviceName).getPathRules().isEmpty());
            return this;
        }

        PendingChangesServiceRulesSteps get() {
            responseEntityObject = apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());
            return this;
        }

        PendingChangesServiceRulesSteps getNewRuleIds() {
            newRuleIds = apiFacade.getNewRuleIds(serviceName, "pathRules");
            return this;
        }

        PendingChangesServiceRulesSteps verifyNewRuleIdsForPathRules() {
            List<String> ruleIds = (List<String>) newRuleIds.getRuleIds();
            assertEquals(1, ruleIds.size());
            assertEquals("ruleName", ruleIds.get(0));
            return this;
        }

        PendingChangesServiceRulesSteps getNewRuleIdsForNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                newRuleIds = apiFacade.getNewRuleIds(NON_EXISTENT_SERVICENAME, "pathRules");
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        private IfExpression getInitialRule() throws InstantiationException, IllegalAccessException {
            return getRule("initialRuleValue");
        }

        private IfExpression getRuleForUpdating() throws InstantiationException, IllegalAccessException {
            return getRule("ruleForUpdatingValue");
        }

        private IfExpression getRule(String value) throws InstantiationException, IllegalAccessException {
            return new IfExpressionBuilder()
                    .withRuleName("ruleName")
                    .withExpression(
                            newSingleParamExpression(Equals.class, "param", value))
                    .withReturnStatement(
                            newSimpleServerForFlavor("/DataCenter2/Region1/zone1")
                    ).build();
        }

        void verifyRulesEqual(IfExpression rule, IfExpression other) {
            assertEquals(rule, other);
        }
    }
}
