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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.ZookeeperModelReloadDispatcher;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.helpers.ServicePathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class SetUpEnvManagerTest {
    private static final Logger log = LoggerFactory.getLogger(SetUpEnvManagerTest.class);

    private static final String SERVICE_NAME = "SOME_SERVICE";
    private static final String URL_RULE_1_NAME = "URL_RULE_1_NAME";
    private static final String URL_RULE_2_NAME = "URL_RULE_2_NAME";
    private static final String FLAVOR_RULE_1_NAME = "FLAVOR_RULE_1_NAME";
    private static final String FLAVOR_RULE_2_NAME = "FLAVOR_RULE_2_NAME";

    private SetUpEnvManager testee;

    private ZookeeperModelReloadDispatcher zookeeperModelReloadDispatcher;
    private ServicePathHelper servicePathHelper;
    private ServiceHelper serviceHelper;

    @Before
    public void before() throws Exception {
        zookeeperModelReloadDispatcher = mock(ZookeeperModelReloadDispatcher.class);
        servicePathHelper = new ServicePathHelper(SERVICE_NAME);
        serviceHelper = mock(ServiceHelper.class);
        when(serviceHelper.get(eq(servicePathHelper.getPendingChangesServicePath()), any(), any()))
            .thenReturn(new PendingChangesStatus());

        Context context = new Context(SERVICE_NAME, "someBaseUrl");
        context.setZookeeperModelReloadDispatcher(zookeeperModelReloadDispatcher);
        context.setServicePathHelper(servicePathHelper);

        URLRules urlRules = setupUrlRules();
        context.setUrlRules(urlRules);

        SelectServer flavorRules = setupFlavorRules();
        context.setSelectServer(flavorRules);

        Whitelisted whitelisted = setupWhitelisted();
        context.setWhitelisted(whitelisted);

        testee = new SetUpEnvManager(context, serviceHelper);
    }

    private URLRules setupUrlRules() throws Exception {
        URLRules urlRules = new URLRules();

        IfExpression urlRule1 = setupUrlRule(URL_RULE_1_NAME);
        IfExpression urlRule2 = setupUrlRule(URL_RULE_2_NAME);
        urlRules.setItems(Arrays.asList(urlRule1, urlRule2));

        Default defaultUrlRule = new Default();
        defaultUrlRule.setUrlRule(setupDefaultUrlRule());
        urlRules.setDefaultStatement(defaultUrlRule);

        return urlRules;
    }

    private SelectServer setupFlavorRules() throws Exception {
        SelectServer flavorRules = new SelectServer();

        IfExpression flavorRule1 = setupFlavorRule(FLAVOR_RULE_1_NAME);
        IfExpression flavorRule2 = setupFlavorRule(FLAVOR_RULE_2_NAME);
        flavorRules.setItems(Arrays.asList(flavorRule1, flavorRule2));
        flavorRules.setDistribution(setupDistribution());

        return flavorRules;
    }

    private Whitelisted setupWhitelisted() {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Collections.singletonList("/dc/zone"));

        return whitelisted;
    }

    private IfExpression setupUrlRule(String name) throws InstantiationException, IllegalAccessException {
        return new IfExpressionBuilder()
            .withRuleName(name)
            .withExpression(
                newSingleParamExpression(Equals.class, "anyParam", "anyValue" + name))
            .withReturnStatement(
                newUrlParams("test" + name, "anyprotocol", "10000", "4")
            ).build();
    }

    private UrlRule setupDefaultUrlRule() {
        return newUrlParams("default", "any", "10000", "4");
    }

    private IfExpression setupFlavorRule(String name) throws InstantiationException, IllegalAccessException {
        return new IfExpressionBuilder()
            .withRuleName(name)
            .withExpression(
                newSingleParamExpression(Equals.class, "anyParam", "anyValue" + name))
            .withReturnStatement(
                newSimpleServerForFlavor("flavor" + name)
            ).build();
    }

    private Distribution setupDistribution() {
        return new DistributionBuilder()
            .withRules(
                Arrays.asList(
                    newDistributionRule(0, 20.0f, newSimpleServerForFlavor("rule0")),
                    newDistributionRule(1, 30.0f, newSimpleServerForFlavor("rule1"))
                )
            )
            .withDefaultServer(newSimpleServerForFlavor("defaultServer"))
            .build();
    }

    @Test
    public void modelIsPutAndApprovedIntoWS() throws Exception {
        Result resultOutputParameter = new Result(true);
        setupRunNextStepAfterPostWhitelist();
        setupRunNextStepAfterModelIsApproved();

        runTesteeInSeparateThread(resultOutputParameter);

        Assert.assertTrue(resultOutputParameter.result);
        verifyStacksWhiteListIsPosted();
        verifyFlavorRuleIsPosted(FLAVOR_RULE_1_NAME);
        verifyFlavorRuleIsPosted(FLAVOR_RULE_2_NAME);
        verifyDistributionIsPosted();
        verifyDefaultServerIsPosted();
        verifyUrlRuleIsPosted(URL_RULE_1_NAME);
        verifyUrlRuleIsPosted(URL_RULE_2_NAME);
        verifyDefaultUrlRuleIsPosted();
    }

    private void setupRunNextStepAfterPostWhitelist() {
        doAnswer((invocation) -> {
            runNextStepAfterDelayInSeparateThread(1);
            log.info("Run next step after whitelist is added");
            return null;
        }).when(serviceHelper).post(eq(servicePathHelper.getWhitelistedServicePath()), any(), any());
    }

    private void setupRunNextStepAfterModelIsApproved() {
        String approvePath = servicePathHelper.getPendingChangesApproveServicePath("any");
        String approvePathStart = approvePath.substring(0, approvePath.lastIndexOf(DELIMETER));
        doAnswer((invocation) -> {
            runNextStepAfterDelayInSeparateThread(1);
            log.info("Run next step after model is approved");
            return null;
        }).when(serviceHelper).post(startsWith(approvePathStart), any(), any());
    }

    private void runTesteeInSeparateThread(Result resultWrapperOutput) throws InterruptedException {
        CountDownLatch setupFinished = new CountDownLatch(1);
        Thread setupThread = new Thread(() -> {
            try {
                testee.setUp();
            } catch (Exception e) {
                log.error("Failed while executing tested method", e);
                resultWrapperOutput.result = false;
            }
            setupFinished.countDown();
        }, "testeeThread");

        setupThread.start();
        setupFinished.await(10, TimeUnit.SECONDS);
    }

    private void verifyUrlRuleIsPosted(String name) throws IllegalAccessException, InstantiationException {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getUrlRulesServicePath(name)), eq(setupUrlRule(name)), any());
    }

    private void verifyDefaultUrlRuleIsPosted() {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getDefaultUrlParamsServicePath()), eq(setupDefaultUrlRule()), any());
    }

    private void verifyStacksWhiteListIsPosted() {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getWhitelistedServicePath()), eq(setupWhitelisted()), any());
    }

    private void verifyFlavorRuleIsPosted(String name) throws IllegalAccessException, InstantiationException {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getFlavorRulesServicePath(name)), eq(setupFlavorRule(name)), any());
    }

    private void verifyDistributionIsPosted() {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getDistributionServicePath()), eq(setupDistribution()), any());
    }

    private void verifyDefaultServerIsPosted() {
        verify(serviceHelper, times(1)).post(
            eq(servicePathHelper.getDefaultServerServicePath()), eq(setupDistribution().getDefaultServer()), any());
    }

    private void runNextStepAfterDelayInSeparateThread(int delayInSeconds) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(delayInSeconds);
                zookeeperModelReloadDispatcher.runNextStep();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class Result {
        boolean result;
        public Result(boolean result) {
            this.result = result;
        }
    }
}
