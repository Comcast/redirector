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

import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.PendingChangesHelper;
import com.comcast.redirector.api.redirector.helpers.RulesHelper;
import com.comcast.redirector.api.redirector.controllers.RulesController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newServerAdvanced;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RulesControllerIntegrationTest {
    private static final String SERVICE_NAME_TEST = "RulesService_test";
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private static final String NON_EXISTENT_RULE = "non_existent_rule";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void testAddGetRule() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new RuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied().approve().verifyApplied();
    }

    @Test
    public void testAddGetRuleWithAdvancedServer() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new RuleSteps(serviceName).createUniqueRuleWithAdvancedServer()
            .post().verifyPostResponse().verifyNotApplied().approve().verifyApplied();
    }

    @Test
    public void testAddGetRule_XmlObject() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        IfExpression rule = uniqueRule();
        IfExpression responseEntityObject = RulesHelper.postRule(serviceName, rule.getId(), rule,
                MediaType.APPLICATION_XML);
        // validate response
        assertEquals(rule, responseEntityObject);

        // check get response - it should has empty entity until approving changes
        try {
            RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, rule.getId(), MediaType.APPLICATION_XML, IfExpression.class);
            Assert.fail();
        } catch (WebApplicationException exception) {
            Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        }

        try {
            RulesHelper.approvePendingChanges(HttpTestServerHelper.target(), serviceName,  rule.getId());
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
        // check get response again
        responseEntityObject = RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, rule.getId(), MediaType.APPLICATION_XML,
                IfExpression.class);
        assertEquals(rule, responseEntityObject);
    }

    /**
     * test check for {@link RulesController#getAllRules(String)}
     */
    @Test
    public void testGetAllRules_JsonObject() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        RuleSteps firstRule = new RuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied();
        RuleSteps secondRule = new RuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied();

        apiFacade.approvePendingChangesForService(serviceName);

        SelectServer rules = apiFacade.getFlavorRulesForService(serviceName);
        firstRule.verifyPresentInCollection(rules);
        secondRule.verifyPresentInCollection(rules);
    }

    @Test
    public void testGetAllRules_XmlObject() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        IfExpression rule = uniqueRule();
        RulesHelper.postRule(serviceName, rule);

        // post other rule
        IfExpression otherRule = uniqueRule();
        RulesHelper.postRule(serviceName, otherRule);

        // check get response - it should has empty entity until approving changes
        try {
            RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, rule.getId());
            Assert.fail();
        } catch (WebApplicationException exception) {
            Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        }

        try {
            RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, otherRule.getId());
            Assert.fail();
        } catch (WebApplicationException exception) {
            Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        }

        // approve all pending changes
        try {
            PendingChangesHelper.approveAllPendingChanges(HttpTestServerHelper.target(), serviceName);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response again
        IfExpression responseRule = RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, rule.getId());
        assertEquals(rule, responseRule);

        IfExpression  responseOtherRule = RulesHelper.getRule(HttpTestServerHelper.target(), serviceName, otherRule.getId());
        assertEquals(otherRule, responseOtherRule);
    }

    @Test
    public void testDeleteRule() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new RuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied().approve().verifyApplied()
            .delete().verifyDeleteNotApplied().approve().verifyDeleteApplied();
    }

    @Test
    public void testDeleteNonExistentRule() throws InterruptedException, IllegalAccessException, InstantiationException {
        IfExpression rule = uniqueRule();
        int status = RulesHelper.deleteRuleWithResponce(HttpTestServerHelper.target(), SERVICE_NAME_TEST, rule.getId());
        assertEquals(HttpStatus.NOT_FOUND.value(), status);
    }

    @Test
    public void testDeleteRuleWithoutApprove() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new RuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied()
            .delete().verifyDeleteNotAppliedForPendingRule();
    }

    @Test
    public void testGetRuleIds() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new RuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .getRuleIdsAndVerifyResponse()
                .getRuleIdsForNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    @Test
    public void testExportAllRules() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new RuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .exportAllRulesAndVerifyResponse()
                .exportAllRulesForNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    @Test
    public void testExportRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new RuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .exportRuleAndVerifyResponse()
                .exportRuleForNonExistentUrlRuleAndVerifyResponseIsUrlRuleNotFound()
                .exportRuleForNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    private class RuleSteps {
        private String serviceName;
        private IfExpression rule;
        private IfExpression responseEntityObject;

        RuleSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        RuleSteps createUniqueRule() throws IllegalAccessException, InstantiationException {
            rule = uniqueRule();
            return this;
        }

        RuleSteps createUniqueRuleWithAdvancedServer() throws IllegalAccessException, InstantiationException {
            rule = uniqueRuleWithAdvancedServer();
            return this;
        }

        RuleSteps post() {
            responseEntityObject = apiFacade.postFlavorRuleForService(rule, serviceName);
            return this;
        }

        RuleSteps verifyPostResponse() {
            verifyRulesEqual(rule, responseEntityObject);
            return this;
        }

        RuleSteps verifyNotApplied() {
            try {
                getRuleFromApi();
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps verifyDeleteNotApplied() {
            verifyRulesEqual(rule, getRuleFromApi());
            return this;
        }

        RuleSteps verifyApplied() {
            verifyRulesEqual(rule, getRuleFromApi());
            return this;
        }

        RuleSteps verifyDeleteApplied() {
            try {
                getRuleFromApi();
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps approve() {
            apiFacade.approveFlavorRulePendingChanges(serviceName, rule);
            return this;
        }

        RuleSteps delete() {
            apiFacade.deleteFlavorRule(serviceName, rule);
            return this;
        }

        RuleSteps verifyPresentInCollection(SelectServer rules) {
            Optional<IfExpression> testee = rules.getItems().stream()
                .filter(item -> rule.getId().equals(item.getId())).findFirst();

            Assert.assertTrue(testee.isPresent());
            verifyRulesEqual(rule, testee.get());

            return this;
        }

        RuleSteps verifyDeleteNotAppliedForPendingRule() {
            try {
                getRuleFromApi();
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps getRuleIdsAndVerifyResponse() {
            RuleIdsWrapper ruleIdsWrapper = apiFacade.getFlavorRulesIdsForService(serviceName);
            Assert.assertEquals(1, ruleIdsWrapper.getRuleIds().size());
            return this;
        }

        RuleSteps getRuleIdsForNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getFlavorRulesIdsForService(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps exportAllRulesAndVerifyResponse() {
            final IfExpression ifExpression = apiFacade.exportFlavorRulesForService(serviceName);
            Assert.assertEquals(1, ifExpression.getItems().size());
            Assert.assertEquals(rule, ifExpression.getItems().get(0));
            return this;
        }

        RuleSteps exportAllRulesForNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.exportFlavorRulesForService(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps exportRuleAndVerifyResponse () {
            IfExpression actualRule = apiFacade.exportFlavorRuleForServiceById(serviceName, rule.getId());
            Assert.assertEquals(rule, actualRule);
            return this;
        }

        RuleSteps exportRuleForNonExistentUrlRuleAndVerifyResponseIsUrlRuleNotFound() {
            try {
                apiFacade.exportFlavorRuleForServiceById(serviceName, NON_EXISTENT_RULE);
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        RuleSteps exportRuleForNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.exportFlavorRuleForServiceById(NON_EXISTENT_SERVICENAME, rule.getId());
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }


        private IfExpression getRuleFromApi() {
            return apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());
        }

        void verifyRulesEqual(IfExpression rule, IfExpression other) {
            assertEquals(rule, other);
        }

        IfExpression getRule() {
            return rule;
        }
    }

    private static IfExpression uniqueRule() throws InstantiationException, IllegalAccessException {
        String suffix = System.currentTimeMillis() + "_" + (int)Math.floor(Math.random() * 100);
        return new IfExpressionBuilder()
            .withRuleName("ANY_RULE" + suffix)
            .withExpression(
                newSingleParamExpression(Equals.class, "param", "value" + suffix))
            .withReturnStatement(
                newSimpleServerForFlavor("/DataCenter2/Region1/zone1")
            ).build();
    }

    private static IfExpression uniqueRuleWithAdvancedServer() throws InstantiationException, IllegalAccessException {
        String suffix = System.currentTimeMillis() + "_" + (int)Math.floor(Math.random() * 100);
        return new IfExpressionBuilder()
            .withRuleName("ANY_RULE" + suffix)
            .withExpression(
                newSingleParamExpression(Equals.class, "param", "value" + suffix))
            .withReturnStatement(
                newServerAdvanced("http://qwerty")
            ).build();
    }
}

