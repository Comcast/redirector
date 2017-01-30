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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.PendingChangesController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;

public class PendingChangesControllerUrlParamsIntegrationTest {

    private final static String URN_APPROVE_TEST = "testApprovePendingUrlParams_urn";
    private final static String URN_CANCEL_TEST = "testCancelPendingUrlParams_urn";

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     * test check for {@link PendingChangesController#approvePendingUrlParams(String, String, int)}
     */
    @Test
    public void testApprovePendingUrlParams() throws Exception {

        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlParamsSteps(serviceName)
                .createUrlParams(URN_APPROVE_TEST)
                .post().approve()
                .verifyUrlParamsAfterApprove().verifyPendingChangesHaveEmptyUrlRules();
    }

    /**
     * test check for {@link PendingChangesController#cancelPendingUrlParams(String, String, int)}
     */
    @Test
    public void testCancelPendingUrlParams() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new PendingChangesServiceUrlParamsSteps(serviceName)
                .createUrlParams(URN_CANCEL_TEST)
                .getCurrentUrlParams().post().cancelUrlParamsPendingChange()
                .verifyUrlParamsAfterCancel().verifyPendingChangesHaveEmptyUrlRules();
    }

    private class PendingChangesServiceUrlParamsSteps {
        private String serviceName;
        private UrlRule urlParams;
        private UrlRule urlRuleResponse;
        private UrlRule urlRuleCurrent;

        PendingChangesServiceUrlParamsSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        PendingChangesServiceUrlParamsSteps createUrlParams(String urn) {
            urlParams = newUrlParams(urn, "xre", "8888", "4");
            return this;
        }

        PendingChangesServiceUrlParamsSteps post() {
            urlRuleResponse = apiFacade.postDefaultUrlParamsForService(urlParams, serviceName);
            return this;
        }

        PendingChangesServiceUrlParamsSteps approve() {
            apiFacade.approveUrlRulePendingChanges(serviceName);
            return this;
        }

        PendingChangesServiceUrlParamsSteps verifyUrlParamsAfterApprove() {
            URLRules responseRules = apiFacade.getAllUrlRules(serviceName);
            Assert.assertEquals(0, responseRules.getItems().size());
            Assert.assertEquals(urlParams, responseRules.getDefaultStatement().getUrlRule());
            return this;
        }

        PendingChangesServiceUrlParamsSteps verifyPendingChangesHaveEmptyUrlRules() {
            PendingChangesStatus pendingChangesStatus = apiFacade.getPendingChangesForService(serviceName);
            Assert.assertTrue(pendingChangesStatus.getUrlParams().isEmpty());
            return this;
        }

        PendingChangesServiceUrlParamsSteps getCurrentUrlParams() {
            URLRules responseRules = apiFacade.getAllUrlRules(serviceName);
            urlRuleCurrent = responseRules.getDefaultStatement().getUrlRule();
            return this;
        }

        PendingChangesServiceUrlParamsSteps cancelUrlParamsPendingChange() {
            apiFacade.cancelPendingChangesForService(serviceName);
            return this;
        }

        PendingChangesServiceUrlParamsSteps verifyUrlParamsAfterCancel() {
            URLRules responseRules = apiFacade.getAllUrlRules(serviceName);
            Assert.assertEquals(0, responseRules.getItems().size());
            Assert.assertEquals(urlRuleCurrent, responseRules.getDefaultStatement().getUrlRule());
            return this;
        }
    }
}
