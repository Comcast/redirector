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
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.UrlRulesController;
import org.junit.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.assertEquals;

public class UrlRulesControllerIntegrationTest {

    private static final String SERVICE_NAME_TEST = "UrlRulesService_test";
    private static final String NON_EXISTENT_SERVICENAME = "non_existent_servicename";
    private static final String NON_EXISTENT_URLRULE = "non_existent_urlrule";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     * test check for {@link UrlRulesController#addUrlRule(String, String, IfExpression, javax.ws.rs.core.UriInfo)}
     * test check for {@link UrlRulesController#getAllUrlRules(String)}
     */
    @Test
    public void testAddGetAllUrlRules() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);
        UrlRuleSteps firstRule = new UrlRuleSteps(serviceName).createUniqueRule().post().verifyPostResponse().verifyNotApplied();
        UrlRuleSteps secondRule = new UrlRuleSteps(serviceName).createUniqueRule().post().verifyPostResponse().verifyNotApplied();

        apiFacade.approvePendingChangesForService(serviceName);

        URLRules rules = apiFacade.getUrlRulesForService(serviceName);
        firstRule.verifyPresentInCollection(rules);
        secondRule.verifyPresentInCollection(rules);
    }

    /**
     * test check for {@link UrlRulesController#getUrlRule(String, String)}
     */
    @Test
    public void testGetUrlRule() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new UrlRuleSteps(serviceName).createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied()
            .approve().verifyApplied();
    }

    /**
     * test check for {@link UrlRulesController#deleteUrlRule(String, String)}
     */
    @Test
    public void testDeleteUrlRule() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new UrlRuleSteps(serviceName)
            .createUniqueRule()
            .post().verifyPostResponse().verifyNotApplied().approve()
            .delete().verifyDeleteNotApplied().approve().verifyDeleteApplied();
    }

    /**
     * test check for {@link UrlRulesController#addUrlParams(String, UrlRule)}
     */
    @Test
    public void testAddUrlParams() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        setupEnv(serviceName);

        new UrlParamsSteps(serviceName)
            .createUrlParams()
            .post().verifyPostResponse().verifyNotApplied().approve().verifyApplied();
    }

    @Test
    public void testGetIdsForUrlRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new UrlRuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .getIdsForUrlRuleAndVerifyResponse()
                .getIdsForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    @Test
    public void testExportAllUrlRules() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new UrlRuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .exportAllUrlRulesAndVerifyResponse()
                .exportAllUrlRulesForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    @Test
    public void testExportUrlRule() throws Exception {
        final String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new UrlRuleSteps(serviceName)
                .createUniqueRule()
                .post()
                .approve()
                .exportUrlRuleAndVerifyResponse()
                .exportUrlRuleForNonExistentUrlRuleAndVerifyResponseIsUrlRuleNotFound()
                .exportUrlRuleForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound();
    }

    private static IfExpression uniqueUrlRule() throws InstantiationException, IllegalAccessException {
        String suffix = System.currentTimeMillis() + "_" + (int)Math.floor(Math.random() * 100);
        return new IfExpressionBuilder()
            .withRuleName("ANY_RULE_JsonObject" + suffix)
            .withExpression(
                newSingleParamExpression(Equals.class, "param", "value" + suffix))
            .withReturnStatement(
                newUrlParams("any", "xre", "8888", "4")
            ).build();
    }

    private class UrlParamsSteps {
        private String serviceName;
        private UrlRule params;
        private UrlRule originalParams;
        private UrlRule responseEntityObject;

        UrlParamsSteps(String serviceName) {
            this.serviceName = serviceName;
            originalParams = getParamsFromApi().getUrlRule();
        }

        UrlParamsSteps createUrlParams() {
            params = newUrlParams("testAddUrlParams_JsonObject_urn", "xre", "8888", "4");
            return this;
        }

        UrlParamsSteps post() {
            responseEntityObject = apiFacade.postDefaultUrlParamsForService(params, serviceName);
            return this;
        }

        UrlParamsSteps verifyPostResponse() {
            assertEquals(params, responseEntityObject);
            return this;
        }

        UrlParamsSteps verifyNotApplied() {
            assertEquals(originalParams, getParamsFromApi().getUrlRule());
            return this;
        }

        UrlParamsSteps approve() {
            apiFacade.approvePendingChangesForService(serviceName);
            return this;
        }

        UrlParamsSteps verifyApplied() {
            assertEquals(params, getParamsFromApi().getUrlRule());
            return this;
        }

        private Default getParamsFromApi() {
            return apiFacade.getUrlParamsForService(serviceName);
        }
    }

    private class UrlRuleSteps {
        private String serviceName;
        private IfExpression rule;
        private IfExpression responseEntityObject;

        UrlRuleSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        UrlRuleSteps createUniqueRule() throws IllegalAccessException, InstantiationException {
            rule = uniqueUrlRule();
            return this;
        }

        UrlRuleSteps post() {
            responseEntityObject = apiFacade.postUrlRuleForService(rule, serviceName);
            return this;
        }

        UrlRuleSteps verifyPostResponse() {
            verifyRulesEqual(rule, responseEntityObject);
            return this;
        }

        UrlRuleSteps verifyNotApplied() {
            try {
                getRuleFromApi();
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        UrlRuleSteps verifyDeleteNotApplied() {
            verifyRulesEqual(rule, getRuleFromApi());
            return this;
        }

        UrlRuleSteps verifyApplied() {
            verifyRulesEqual(rule, getRuleFromApi());
            return this;
        }

        UrlRuleSteps verifyDeleteApplied() {
            try {
                getRuleFromApi();
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        UrlRuleSteps approve() {
            apiFacade.approveUrlRulePendingChanges(serviceName, rule);
            return this;
        }

        UrlRuleSteps delete() {
            apiFacade.deleteUrlRule(serviceName, rule);
            return this;
        }

        UrlRuleSteps verifyPresentInCollection(URLRules rules) {
            Optional<IfExpression> testee = rules.getItems().stream()
                .filter(item -> rule.getId().equals(item.getId())).findFirst();

            Assert.assertTrue(testee.isPresent());
            verifyRulesEqual(rule, testee.get());

            return this;
        }

        UrlRuleSteps getIdsForUrlRuleAndVerifyResponse() {
            RuleIdsWrapper ruleIdsWrapper = apiFacade.getUrlRulesIdsForService(serviceName);
            Assert.assertEquals(1, ruleIdsWrapper.getRuleIds().size());
            return this;
        }

        UrlRuleSteps getIdsForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.getUrlRulesIdsForService(NON_EXISTENT_SERVICENAME);
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        UrlRuleSteps exportAllUrlRulesAndVerifyResponse() {
            final IfExpression ifExpression = apiFacade.exportUrlRulesForService(serviceName);
            Assert.assertEquals(1, ifExpression.getItems().size());
            Assert.assertEquals(rule, ifExpression.getItems().get(0));
            return this;
        }

        UrlRuleSteps exportAllUrlRulesForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.exportUrlRulesForService(NON_EXISTENT_SERVICENAME);
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        UrlRuleSteps exportUrlRuleAndVerifyResponse () {
            final IfExpression actualUrlRule = apiFacade.exportUrlRuleForServiceById(serviceName, rule.getId());
            Assert.assertEquals(rule, actualUrlRule);
            return this;
        }

        UrlRuleSteps exportUrlRuleForNonExistentUrlRuleAndVerifyResponseIsUrlRuleNotFound() {
            try {
                apiFacade.exportUrlRuleForServiceById(serviceName, NON_EXISTENT_URLRULE);
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        UrlRuleSteps exportUrlRuleForUrlRuleNonExistentServiceNameAndVerifyResponseIsServiceNotFound() {
            try {
                apiFacade.exportUrlRuleForServiceById(NON_EXISTENT_SERVICENAME, rule.getId());
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        private IfExpression getRuleFromApi() {
            return apiFacade.getUrlRuleByIdForService(serviceName, rule.getId());
        }

        void verifyRulesEqual(IfExpression rule, IfExpression other) {
            assertEquals(rule, other);
        }

        IfExpression getRule() {
            return rule;
        }
    }
}

