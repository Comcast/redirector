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
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.builders.ServerBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import com.comcast.redirector.common.RedirectorConstants;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static com.comcast.redirector.api.model.ActionType.*;
import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static com.comcast.redirector.api.model.factory.WhitelistFactory.createWhitelisted;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class PendingChangesControllerIntegrationTest {
    private static final String SERVICE_NAME_TEST = "PendingChangesService_test";
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

// ************************************* APPROVE/CANCEL ALL PENDING CHANGES TEST CASE  ********************************** //

    /**
     * test check for {@link PendingChangesController#approvePendingChanges(String, int)}
     */
    @Test
    public void testApprovePendingChanges() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("1.0")
                .withWhitelist("/PO/POC1", "/DataCenter1/Region1")
                .withDefaultUrlParams()
                    .urn("shell").protocol("xre").port("10001").ipv("4")
                .withHosts()
                    .stack("/PO/POC1").flavor("1.0").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.1").currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.2").currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.3").currentApp().ipv4("10.0.0.4").ipv6("ipv6")
                    .stack("/DataCenter1/Region1").flavor("Zone1").currentApp().ipv4("10.0.0.5").ipv6("ipv6")
                    .stack("/DataCenter1/Region1").flavor("Zone2").currentApp().ipv4("10.0.0.6").ipv6("ipv6")
                    .stack("/DataCenter1/Region1").flavor("Stack0").currentApp().ipv4("10.0.0.7").ipv6("ipv6")
                .build();
        testHelperBuilder(context).setupEnv();

        new PendingChangesSteps(serviceName)
            .buildAndPostDistribution()
            .buildAndPostFlavorRule()
            .buildAndPostDefaultServer()
            .buildAndPostWhitelist()
            .buildAndPostUrlRule()
            .buildAndPostDefaultUrlParams()

            .approvePendingChanges()

            .verifyNoPendingChangesLeft()
            .verifyDistributionApplied()
            .verifyFlavorRuleApplied()
            .verifyDefaultServerApplied()
            .verifyWhitelistApplied()
            .verifyUrlRuleApplied()
            .verifyDefaultUrlParamsApplied();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingChanges(String, int)}
     */
    @Test
    public void testCancelPendingChanges() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        TestContext context = new ContextBuilder().forApp(serviceName)
            .withDefaultServer().flavor("1.0")
            .withWhitelist("/PO/POC1", "/DataCenter1/Region1")
            .withDefaultUrlParams()
                .urn("shell").protocol("xre").port("10001").ipv("4")
            .withHosts()
                .stack("/PO/POC1").flavor("1.0").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.1").currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.2").currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.3").currentApp().ipv4("10.0.0.4").ipv6("ipv6")
                .stack("/DataCenter1/Region1").flavor("Zone1").currentApp().ipv4("10.0.0.5").ipv6("ipv6")
                .stack("/DataCenter1/Region1").flavor("Zone2").currentApp().ipv4("10.0.0.6").ipv6("ipv6")
                .stack("/DataCenter1/Region1").flavor("Stack0").currentApp().ipv4("10.0.0.7").ipv6("ipv6")
            .build();
        testHelperBuilder(context).setupEnv();

        new PendingChangesSteps(serviceName)
            .buildAndPostDistribution()
            .buildAndPostFlavorRule()
            .buildAndPostDefaultServer()
            .buildAndPostWhitelist()
            .buildAndPostUrlRule()
            .buildAndPostDefaultUrlParams()

            .cancelPendingChanges()

            .verifyNoPendingChangesLeft()
            .verifyDistributionNOTApplied()
            .verifyFlavorRuleNOTApplied()
            .verifyDefaultServerNOTApplied()
            .verifyWhitelistNOTApplied()
            .verifyUrlRuleNOTApplied()
            .verifyDefaultUrlParamsNOTApplied();
    }

// ************************************* GET ALL PENDING CHANGES TEST CASE  ********************************** //

    /**
     * test check for {@link PendingChangesController#getPendingChanges(String)}
     */
    @Test
    public void testGetAddedPendingChanges() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        TestContext context = new ContextBuilder().forApp(serviceName)
            .withDefaultServer().flavor("1.0")
            .withWhitelist("/PO/POC1")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withHosts()
                .stack("/PO/POC1").flavor("1.0").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.1").currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.2").currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                .stack("/PO/POC1").flavor("1.3").currentApp().ipv4("10.0.0.4").ipv6("ipv6")
            .build();
        testHelperBuilder(context).setupEnv();

        new PendingChangesSteps(serviceName)
            .buildAndPostDistribution()
            .buildAndPostWhitelist("/PO/POC1", "/DataCenter1/Region1", "/PO/POC2")
            .buildAndPostFlavorRule()
            .buildAndPostUrlRule()
            .buildAndPostDefaultUrlParams()
            .buildAndPostDefaultServer("1.3")

            .verifyDistributionPending()
            .verifyWhitelistPending()
            .verifyFlavorRulePending()
            .verifyUrlRulePending()
            .verifyDefaultUrlParamsPending()
            .verifyDefaultServerPending();
    }

    /**
     * test check for {@link PendingChangesController#getPendingChanges(String)}
     */
    @Test
    public void testGetUpdatedPendingChanges_JsonObject() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("1.0")
                .withWhitelist("/PO/POC1")
                .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
                .withHosts()
                    .stack("/PO/POC1").flavor("1.0").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.1").currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.2").currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                    .stack("/PO/POC2").flavor("2.1").currentApp().ipv4("10.0.0.4").ipv6("ipv6")
                    .stack("/PO/POC3").flavor("2.1").currentApp().ipv4("10.0.0.5").ipv6("ipv6")
                .withDistribution()
                    .percent("25.5").flavor("1.1").and()
                    .percent("25.5").flavor("1.2")
                .build();
        testHelperBuilder(context).setupEnv();

        new PendingChangesSteps(serviceName)
            .buildAndPostFlavorRule("flavorRule", "param1", "value1", "1.1")
            .approveFlavorRule()
            .buildAndPostFlavorRule("flavorRule", "param2", "value2", "1.1")
            .buildAndPostUrlRule("urlRule", "param", "value")
            .approveUrlRule()
            .buildAndPostUrlRule("urlRule", "param2", "value2")
            .buildAndPostDistribution(
                new DistributionBuilder()
                    .withRule(newDistributionRule(0, 50f, newSimpleServerForFlavor("1.1")))
                    .withDefaultServer(new ServerBuilder().withFlavor("1.0").build())
                    .build()
            )

            .verifyDistributionPendingUpdate()
            .verifyFlavorRulePendingUpdate()
            .verifyUrlRulePendingUpdate()

            .buildAndPostWhitelist("/PO/POC1", "/PO/POC3")
            .verifyWhitelistPendingUpdate();

    }

    /**
     * test check for {@link PendingChangesController#getPendingChanges(String)}
     */
    @Test
    public void testGetDeletedPendingChanges_JsonObject() throws Exception {
        String serviceName = getServiceNameForTest(SERVICE_NAME_TEST);
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("1.0")
                .withWhitelist("/PO/POC1")
                .withDefaultUrlParams()
                    .urn("shell").protocol("xre").port("10001").ipv("4")
                .withHosts()
                    .stack("/PO/POC1").flavor("1.0").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.1").currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                    .stack("/PO/POC1").flavor("1.2").currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                    .stack("/PO/POC2").flavor("2.1").currentApp().ipv4("10.0.0.4").ipv6("ipv6")
                    .stack("/PO/POC3").flavor("2.1").currentApp().ipv4("10.0.0.5").ipv6("ipv6")
                .withDistribution()
                    .percent("10").flavor("1.1")
                .build();
        testHelperBuilder(context).setupEnv();

        new PendingChangesSteps(serviceName)
            .buildAndPostFlavorRule("rule1_to_be_deleted", "param1", "value1", "1.0")
            .approveFlavorRule()
            .deleteFlavorRule()
            .buildAndPostUrlRule("rule1_to_be_deleted", "param1", "value1")
            .approveUrlRule()
            .deleteUrlRule()

            .verifyFlavorRulePendingDelete()
            .verifyUrlRulePendingDelete();
    }

    @Test
    public void testApprovePendingChangesNotExistRule() throws AssertionError {
        String ruleId = "NON-EXISTING-RULE";
        String version = "2563988";
        WebTarget webTargetWithPath = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_CONTROLLER_PATH).path(SERVICE_NAME_TEST).path(UrlRulesHelper._URL_RULE).path(ruleId).path(version);

        Response response = webTargetWithPath.request().post(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    private IfExpression getFlavorRule(String ruleName, String paramName, String value, String returnFlavor) throws InstantiationException, IllegalAccessException {
        IfExpressionBuilder builder = new IfExpressionBuilder();
        return builder.
                withRuleName(ruleName).
                withExpression(newSingleParamExpression(Equals.class, paramName, value)).
                withReturnStatement(newSimpleServerForFlavor(returnFlavor)).build();
    }

    private IfExpression getUrlRuleExpression(String ruleName, String paramName, String value) throws InstantiationException, IllegalAccessException {
        IfExpressionBuilder builder = new IfExpressionBuilder();
        return builder.
                withRuleName(ruleName).
                withExpression(newSingleParamExpression(Equals.class, paramName, value)).
                withReturnStatement(newUrlParams("urn", "xre", "8888", "4")).build();
    }

    private class PendingChangesSteps {
        private String serviceName;
        private Distribution distribution;
        private Whitelisted whitelisted;
        private IfExpression rule;
        private IfExpression urlRule;
        private UrlRule  urlParams;
        private Server server;

        private Server currentServer;
        private Default currentUrlParams;
        private Whitelisted currentWhitelist;

        public PendingChangesSteps(String serviceName) {
            this.serviceName = serviceName;

            currentUrlParams = apiFacade.getUrlParamsForService(serviceName);
            currentServer = apiFacade.getServerForService(serviceName);
            currentWhitelist = apiFacade.getWhiteListForService(serviceName);
        }

        PendingChangesSteps buildAndPostDistribution() {
            return buildAndPostDistribution(
                new DistributionBuilder()
                    .withRule(newDistributionRule(0, 25.5f, newSimpleServerForFlavor("1.1")))
                    .withRule(newDistributionRule(1, 25.5f, newSimpleServerForFlavor("1.2")))
                    .withDefaultServer(new ServerBuilder().withFlavor("1.0").build())
                    .build()
            );
        }

        PendingChangesSteps buildAndPostDistribution(Distribution distribution) {
            this.distribution = distribution;
            apiFacade.postDistributionForService(distribution, serviceName);
            return this;
        }

        PendingChangesSteps buildAndPostWhitelist() {
            return buildAndPostWhitelist("/PO/POC1", "/DataCenter1/Region1", "/PO/POC2");
        }

        PendingChangesSteps buildAndPostWhitelist(String ... whitelist) {
            whitelisted = createWhitelisted(whitelist);
            apiFacade.postWhitelistForService(whitelisted, serviceName);
            return this;
        }

        PendingChangesSteps buildAndPostFlavorRule() throws IllegalAccessException, InstantiationException {
            return buildAndPostFlavorRule("Rule_testApprovePendingChanges", "param", "value", "1.1");
        }

        PendingChangesSteps buildAndPostFlavorRule(String id, String left, String right, String flavor) throws IllegalAccessException, InstantiationException {
            rule = getFlavorRule(id, left, right, flavor);
            apiFacade.postFlavorRuleForService(rule, serviceName);
            return this;
        }

        PendingChangesSteps buildAndPostUrlRule() throws IllegalAccessException, InstantiationException {
            return buildAndPostUrlRule("UrlRule_testApprovePendingChanges", "param", "value");
        }

        PendingChangesSteps buildAndPostUrlRule(String id, String left, String right) throws IllegalAccessException, InstantiationException {
            urlRule = getUrlRuleExpression(id, left, right);
            apiFacade.postUrlRuleForService(urlRule, serviceName);
            return this;
        }

        PendingChangesSteps buildAndPostDefaultServer() {
            return buildAndPostDefaultServer("1.3");
        }

        PendingChangesSteps buildAndPostDefaultServer(String flavor) {
            server = new ServerBuilder().withFlavor(flavor).build();
            apiFacade.postDefaultServerForService(server, serviceName);
            return this;
        }

        PendingChangesSteps buildAndPostDefaultUrlParams() {
            urlParams =  newUrlParams("testApprovePendingChanges_urn", "xre", "8888", "4");
            apiFacade.postDefaultUrlParamsForService(urlParams, serviceName);
            return this;
        }

        PendingChangesSteps approvePendingChanges() {
            apiFacade.approvePendingChangesForService(serviceName);
            return this;
        }

        PendingChangesSteps approveFlavorRule() {
            apiFacade.approveFlavorRulePendingChanges(serviceName, rule);
            return this;
        }

        PendingChangesSteps approveUrlRule() {
            apiFacade.approveUrlRulePendingChanges(serviceName, urlRule);
            return this;
        }

        PendingChangesSteps cancelPendingChanges() {
            apiFacade.cancelPendingChangesForService(serviceName);
            return this;
        }

        PendingChangesSteps verifyDistributionApplied() {
            Distribution responseDistribution = apiFacade.getDistributionForService(serviceName);
            assertEquals(distribution.getRules().size(), responseDistribution.getRules().size());
            assertEquals(distribution.getRules(), responseDistribution.getRules());

            return this;
        }

        PendingChangesSteps verifyDistributionPending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(distribution.getRules().size(), pendingChangesStatus.getDistributions().size());

            assertEquals(String.valueOf(distribution.getRules().get(0).getId()), pendingChangesStatus.getDistributions().get("0").getId());
            assertEquals(ADD, pendingChangesStatus.getDistributions().get("0").getChangeType());
            assertNull(pendingChangesStatus.getDistributions().get("0").getCurrentExpression());
            assertEquals(distribution.getRules().get(0), pendingChangesStatus.getDistributions().get("0").getChangedExpression());

            assertEquals(String.valueOf(distribution.getRules().get(1).getId()), pendingChangesStatus.getDistributions().get("1").getId());
            assertEquals(ADD, pendingChangesStatus.getDistributions().get("1").getChangeType());
            assertNull(pendingChangesStatus.getDistributions().get("1").getCurrentExpression());
            assertEquals(distribution.getRules().get(1), pendingChangesStatus.getDistributions().get("1").getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyDistributionPendingUpdate() {
            Distribution updatedDistribution = distribution;
            Distribution currentdistribution = apiFacade.getDistributionForService(serviceName);
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(Math.max(currentdistribution.getRules().size(), updatedDistribution.getRules().size()), pendingChangesStatus.getDistributions().size());
            assertEquals(String.valueOf(currentdistribution.getRules().get(0).getId()), pendingChangesStatus.getDistributions().get("0").getId());
            assertEquals(UPDATE, pendingChangesStatus.getDistributions().get("0").getChangeType());
            assertEquals(currentdistribution.getRules().get(0), pendingChangesStatus.getDistributions().get("0").getCurrentExpression());
            assertEquals(updatedDistribution.getRules().get(0), pendingChangesStatus.getDistributions().get("0").getChangedExpression());

            assertEquals(String.valueOf(currentdistribution.getRules().get(1).getId()), pendingChangesStatus.getDistributions().get("1").getId());
            assertEquals(DELETE, pendingChangesStatus.getDistributions().get("1").getChangeType());
            assertEquals(currentdistribution.getRules().get(1), pendingChangesStatus.getDistributions().get("1").getCurrentExpression());
            assertNull(pendingChangesStatus.getDistributions().get("1").getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyDistributionNOTApplied() {
            Distribution responseDistribution = apiFacade.getDistributionForService(serviceName);
            assertEquals(0, responseDistribution.getRules().size());

            return this;
        }

        PendingChangesSteps verifyWhitelistApplied() {
            Whitelisted responseWhitelisted = apiFacade.getWhiteListForService(serviceName);
            assertEquals(whitelisted.getPaths().size(), responseWhitelisted.getPaths().size());
            assertEquals(whitelisted.getPaths(), responseWhitelisted.getPaths());

            return this;
        }

        PendingChangesSteps verifyWhitelistPending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(2, pendingChangesStatus.getWhitelisted().size());

            String key = (whitelisted.getPaths().get(1));
            Assert.assertThat(pendingChangesStatus.getWhitelisted().keySet(), hasItem(key));
            assertEquals(key, pendingChangesStatus.getWhitelisted().get(key).getId());
            assertEquals(ADD, pendingChangesStatus.getWhitelisted().get(key).getChangeType());
            assertEquals(new Value(""), pendingChangesStatus.getWhitelisted().get(key).getCurrentExpression());
            assertEquals(new Value(whitelisted.getPaths().get(1)), pendingChangesStatus.getWhitelisted().get(key).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyWhitelistPendingUpdate() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(1, pendingChangesStatus.getWhitelisted().size());
            String key = whitelisted.getPaths().get(1);
            Assert.assertThat(pendingChangesStatus.getWhitelisted().keySet(), hasItem(key));
            assertEquals(key, pendingChangesStatus.getWhitelisted().get(key).getId());
            assertEquals(ADD, pendingChangesStatus.getWhitelisted().get(key).getChangeType());
            assertEquals(new Value(whitelisted.getPaths().get(1)),
                pendingChangesStatus.getWhitelisted().get(key).getChangedExpression());
            assertEquals(new Value(""), pendingChangesStatus.getWhitelisted().get(key).getCurrentExpression());

            return this;
        }

        PendingChangesSteps verifyWhitelistNOTApplied() {
            Whitelisted responseWhitelisted = apiFacade.getWhiteListForService(serviceName);
            assertEquals(currentWhitelist.getPaths().size(), responseWhitelisted.getPaths().size());
            assertEquals(currentWhitelist.getPaths(), responseWhitelisted.getPaths());

            return this;
        }

        PendingChangesSteps verifyFlavorRuleApplied() {
            IfExpression responseRules = apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());
            assertEquals(rule, responseRules);

            return this;
        }

        PendingChangesSteps verifyFlavorRulePending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(rule.getItems().size(), pendingChangesStatus.getPathRules().size());
            assertEquals(rule.getId(), pendingChangesStatus.getPathRules().get(rule.getId()).getId());
            assertEquals(ADD, pendingChangesStatus.getPathRules().get(rule.getId()).getChangeType());
            assertNull(pendingChangesStatus.getPathRules().get(rule.getId()).getCurrentExpression());
            assertEquals(rule, pendingChangesStatus.getPathRules().get(rule.getId()).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyFlavorRulePendingUpdate() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            IfExpression currentFlavorRule = apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());

            assertEquals(1, pendingChangesStatus.getPathRules().size());
            assertEquals(rule.getId(), pendingChangesStatus.getPathRules().get(rule.getId()).getId());
            assertEquals(UPDATE, pendingChangesStatus.getPathRules().get(rule.getId()).getChangeType());
            assertEquals(currentFlavorRule, pendingChangesStatus.getPathRules().get(rule.getId()).getCurrentExpression());
            assertEquals(rule, pendingChangesStatus.getPathRules().get(rule.getId()).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyFlavorRuleNOTApplied() {
            try {
                apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PendingChangesSteps verifyUrlRuleApplied() {
            IfExpression responseUrlRule = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
            assertEquals(urlRule, responseUrlRule);

            return this;
        }

        PendingChangesSteps verifyUrlRulePending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(urlRule.getItems().size(), pendingChangesStatus.getUrlRules().size());
            assertEquals(urlRule.getId(), pendingChangesStatus.getUrlRules().get(urlRule.getId()).getId());
            assertEquals(ADD, pendingChangesStatus.getUrlRules().get(urlRule.getId()).getChangeType());
            assertNull(pendingChangesStatus.getUrlRules().get(urlRule.getId()).getCurrentExpression());
            assertEquals(urlRule, pendingChangesStatus.getUrlRules().get(urlRule.getId()).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyUrlRulePendingUpdate() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            IfExpression currentUrlRule = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());

            assertEquals(1, pendingChangesStatus.getUrlRules().size());
            assertEquals(currentUrlRule.getId(), pendingChangesStatus.getUrlRules().get(currentUrlRule.getId()).getId());
            assertEquals(UPDATE, pendingChangesStatus.getUrlRules().get(urlRule.getId()).getChangeType());
            assertEquals(currentUrlRule, pendingChangesStatus.getUrlRules().get(currentUrlRule.getId()).getCurrentExpression());
            assertEquals(urlRule, pendingChangesStatus.getUrlRules().get(urlRule.getId()).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyUrlRuleNOTApplied() {
            try {
                apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }

        PendingChangesSteps verifyDefaultServerApplied() {
            Server responseServer = apiFacade.getServerForService(serviceName);
            assertEquals(server.getPath(), responseServer.getPath()); // TODO: there is a bug - validation visitor is changing name and description of service. Need to fix it.

            return this;
        }

        PendingChangesSteps verifyDefaultServerPending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(1, pendingChangesStatus.getServers().size());
            assertEquals(server.getName(), pendingChangesStatus.getServers().get(server.getName()).getId());
            assertEquals(UPDATE, pendingChangesStatus.getServers().get(server.getName()).getChangeType());
            assertEquals(currentServer, pendingChangesStatus.getServers().get(server.getName()).getCurrentExpression());
            assertEquals(server.getPath(), ((Server)pendingChangesStatus.getServers().get(server.getName()).getChangedExpression()).getPath());

            return this;
        }

        PendingChangesSteps verifyDefaultServerNOTApplied() {
            Server responseServer = apiFacade.getServerForService(serviceName);
            assertEquals(currentServer, responseServer);

            return this;
        }

        PendingChangesSteps verifyDefaultUrlParamsApplied() {
            Default responseUrlParams = apiFacade.getUrlParamsForService(serviceName);
            assertEquals(urlParams, responseUrlParams.getUrlRule());

            return this;
        }

        PendingChangesSteps verifyDefaultUrlParamsPending() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            assertEquals(1, pendingChangesStatus.getUrlParams().size());
            Assert.assertThat(pendingChangesStatus.getUrlParams().keySet(), hasItem("default"));
            assertEquals("default", pendingChangesStatus.getUrlParams().get("default").getId());
            assertEquals(UPDATE, pendingChangesStatus.getUrlParams().get("default").getChangeType());
            if (pendingChangesStatus.getUrlParams().get("default").getCurrentExpression() == null) {
                assertEquals(currentUrlParams.getUrlRule().getIpProtocolVersion(), null);
                assertEquals(currentUrlParams.getUrlRule().getPort(), null);
                assertEquals(currentUrlParams.getUrlRule().getUrn(), null);
            }
            else {
                assertEquals(currentUrlParams.getUrlRule(), pendingChangesStatus.getUrlParams().get("default").getCurrentExpression());
            }
            assertEquals(urlParams, pendingChangesStatus.getUrlParams().get("default").getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyDefaultUrlParamsNOTApplied() {
            Default responseUrlParams = apiFacade.getUrlParamsForService(serviceName);
            assertEquals(currentUrlParams.getUrlRule(), responseUrlParams.getUrlRule());

            return this;
        }

        PendingChangesSteps verifyNoPendingChangesLeft() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);

            Assert.assertTrue(pendingChangesStatus.getDistributions().isEmpty());
            Assert.assertTrue(pendingChangesStatus.getWhitelisted().isEmpty());
            Assert.assertTrue(pendingChangesStatus.getPathRules().isEmpty());
            Assert.assertTrue(pendingChangesStatus.getUrlRules().isEmpty());
            Assert.assertTrue(pendingChangesStatus.getUrlParams().isEmpty());
            Assert.assertTrue(pendingChangesStatus.getServers().isEmpty());

            return this;
        }

        PendingChangesSteps deleteFlavorRule() {
            apiFacade.deleteFlavorRule(serviceName, rule);
            return this;
        }

        PendingChangesSteps deleteUrlRule() {
            apiFacade.deleteUrlRule(serviceName, urlRule);
            return this;
        }

        PendingChangesSteps verifyFlavorRulePendingDelete() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            IfExpression flavorRuleToBeDeleted = apiFacade.getFlavorRuleByIdForService(serviceName, rule.getId());

            // validate rules pending response
            assertEquals(flavorRuleToBeDeleted.getItems().size(), pendingChangesStatus.getPathRules().size());
            assertEquals(flavorRuleToBeDeleted.getId(), pendingChangesStatus.getPathRules().get(flavorRuleToBeDeleted.getId()).getId());
            assertEquals(DELETE, pendingChangesStatus.getPathRules().get(flavorRuleToBeDeleted.getId()).getChangeType());
            assertEquals(flavorRuleToBeDeleted, pendingChangesStatus.getPathRules().get(flavorRuleToBeDeleted.getId()).getCurrentExpression());
            assertNull(pendingChangesStatus.getPathRules().get(flavorRuleToBeDeleted.getId()).getChangedExpression());

            return this;
        }

        PendingChangesSteps verifyUrlRulePendingDelete() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            IfExpression urlRuleToBeDeleted = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());

            assertEquals(urlRuleToBeDeleted.getItems().size(), pendingChangesStatus.getUrlRules().size());
            assertEquals(urlRuleToBeDeleted.getId(), pendingChangesStatus.getUrlRules().get(urlRuleToBeDeleted.getId()).getId());
            assertEquals(DELETE, pendingChangesStatus.getUrlRules().get(urlRuleToBeDeleted.getId()).getChangeType());
            assertEquals(urlRuleToBeDeleted, pendingChangesStatus.getUrlRules().get(urlRuleToBeDeleted.getId()).getCurrentExpression());
            assertNull(pendingChangesStatus.getUrlRules().get(urlRuleToBeDeleted.getId()).getChangedExpression());

            return this;
        }
    }
}
