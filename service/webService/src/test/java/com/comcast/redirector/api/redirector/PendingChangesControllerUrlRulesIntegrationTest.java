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

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.*;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class PendingChangesControllerUrlRulesIntegrationTest {
    private final static String URLRULE_NAME_PREFIX = "UrlRule";
    private final static String PARAMNAME = "param";
    private final static String VALUE = "value";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     * test check for {@link PendingChangesController#approvePendingUrlRule(String, String, int)}
     */
    @Test
    public void testApprovePendingUrlRule() throws Exception {

        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .post()
                .approve()
                .verifyUrlRuleApplied()
                .verifyPendingChangesHaveNoUrlRule();
    }

    /**
     * test check for {@link PendingChangesController#approvePendingUrlRule(String, String, int)}
     */
    @Test
    public void testApproveUpdatedPendingUrlRule() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .createChangeForUrlRule()
                .updateUrlRule()
                .approveChangeForUrlRule()
                .verifyUrlRuleAfterUpdate()
                .verifyPendingChangesHaveNoUrlRule();

    }

    /**
     * test check for {@link PendingChangesController#cancelPendingChanges(String, int)}
     */
    @Test
    public void testApproveDeletedPendingUrlRule() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .post()
                .approve()
                .verifyUrlRuleApplied()
                .deleteUrlRule()
                .approve()
                .getUrlRuleAndVerifyResponseIsRuleNotFound()
                .verifyPendingChangesHaveNoUrlRule();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingUrlParams(String, String, int)}
     */
    @Test
    public void testCancelAddedPendingUrlRule() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .post()
                .cancelUrlRule()
                .getUrlRuleAndVerifyResponseIsRuleNotFound()
                .verifyPendingChangesHaveNoUrlRule();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingUrlParams(String, String, int)}
     */
    @Test
    public void testCancelUpdatedPendingUrlRule() throws Exception {

        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .createChangeForUrlRule()
                .updateUrlRule()
                .cancelUrlRule()
                .verifyUrlRuleHasNoChanges()
                .verifyPendingChangesHaveNoUrlRule();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingUrlParams(String, String, int)}
     */
    @Test
    public void testCancelDeletedPendingUrlRule() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlRulesSteps(serviceName)
                .createUrlRule()
                .post()
                .approve()
                .verifyUrlRuleApplied()
                .deleteUrlRule()
                .cancelUrlRule()
                .verifyUrlRuleHasNoChanges()
                .verifyPendingChangesHaveNoUrlRule();
    }

    private class PendingChangesServiceUrlRulesSteps {

        private String serviceName;
        private String urlRuleName;
        private IfExpression urlRule;
        private IfExpression changedUrlRule;

        PendingChangesServiceUrlRulesSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesServiceUrlRulesSteps createUrlRule() throws Exception {
            String suffix = System.currentTimeMillis() + "_" + (int) Math.floor(Math.random() * 100);
            this.urlRuleName = URLRULE_NAME_PREFIX + "_" + suffix;
            this.urlRule = buildUrlRule(urlRuleName, PARAMNAME, VALUE);
            return this;
        }

        PendingChangesServiceUrlRulesSteps createChangeForUrlRule() throws Exception {
            this.changedUrlRule = buildUrlRule(urlRuleName, PARAMNAME, VALUE + "_UpdatedValue");
            return this;
        }

        PendingChangesServiceUrlRulesSteps updateUrlRule() {
            apiFacade.updateUrlRuleForService(urlRule, changedUrlRule, serviceName);
            return this;
        }

        PendingChangesServiceUrlRulesSteps post() {
            apiFacade.postUrlRuleForService(urlRule, serviceName);
            return this;
        }

        PendingChangesServiceUrlRulesSteps approve() {
            apiFacade.approveUrlRulePendingChanges(serviceName, urlRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps approveChangeForUrlRule() {
            apiFacade.approveUrlRulePendingChanges(serviceName, changedUrlRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps verifyUrlRuleApplied() {
            IfExpression responseRule = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
            assertEquals(urlRule, responseRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps verifyUrlRuleHasNoChanges() {
            IfExpression responseRule = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
            assertEquals(urlRule, responseRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps verifyUrlRuleAfterUpdate() {
            IfExpression responseRule = apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
            assertEquals(changedUrlRule, responseRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps verifyPendingChangesHaveNoUrlRule() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            assertTrue(pendingChangesStatus.getUrlRules().isEmpty());
            return this;
        }

        PendingChangesServiceUrlRulesSteps deleteUrlRule() {
            apiFacade.deleteUrlRule(serviceName, urlRule);
            return this;
        }

        PendingChangesServiceUrlRulesSteps cancelUrlRule() {
            apiFacade.cancelUrlRuleForService(urlRule, serviceName);
            return this;
        }

        PendingChangesServiceUrlRulesSteps getUrlRuleAndVerifyResponseIsRuleNotFound() {
            try {
                apiFacade.getUrlRuleByIdForService(serviceName, urlRule.getId());
                Assert.fail();
            } catch (WebApplicationException exception) {
                Assert.assertEquals(exception.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode());
            }
            return this;
        }
    }
}
