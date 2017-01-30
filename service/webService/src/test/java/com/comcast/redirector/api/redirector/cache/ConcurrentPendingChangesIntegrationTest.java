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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.cache;

import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.builders.ServerBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.*;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;

public class ConcurrentPendingChangesIntegrationTest extends BaseWebServiceIntegrationTest {

    @Test
    public void entitiesAreSavedCorrectlyInSingleUserEnvironment() throws Exception {
        String appName = "test";
        TestContext context = new ContextBuilder().forApp(appName)
            .withWhitelist("/dc/stack1", "/dc/stack2", "/dc/stack3")
            .withHosts()
                .stack("/dc/stack1").flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app("test")
                .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app("test")
                .stack("/dc/stack3").flavor("flavor3").ipv4("10.0.0.4").ipv6("ff01::44").app("test")
            .build();

        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();

        Server defaultServer = new ServerBuilder().withFlavor("flavor1").build();
        IfExpression flavorRule = new IfExpressionBuilder()
            .withRuleName("flavorRule")
            .withExpression(newSingleParamExpression(Equals.class, "anyParam", "anyValue" + appName))
            .withReturnStatement(newSimpleServerForFlavor("flavor1"))
            .build();
        IfExpression urlRule = new IfExpressionBuilder()
            .withRuleName("urlRule")
            .withExpression(newSingleParamExpression(Equals.class, "anyParam", "anyValue" + appName))
            .withReturnStatement(newUrlParams("any", "xre", "10000", "4"))
            .build();
        UrlRule defaultUrlParams = newUrlParams("any", "xre", "10000", "6");
        Distribution distribution = new DistributionBuilder()
            .withRules(
                Arrays.asList(
                    newDistributionRule(0, 10.0f, new ServerBuilder().withName("0").withFlavor("flavor2").build()),
                    newDistributionRule(1, 20.0f, new ServerBuilder().withName("1").withFlavor("flavor3").build())
                )
            )
            .build();
        Whitelisted whitelisted = new Whitelisted(Arrays.asList("/dc/stack1", "/dc/stack2", "/dc/stack3", "/dc/stack4"));

        restApiFacade.postDefaultServerForService(defaultServer, appName);
        restApiFacade.postFlavorRuleForService(flavorRule, appName);
        restApiFacade.postUrlRuleForService(urlRule, appName);
        restApiFacade.postDefaultUrlParamsForService(defaultUrlParams, appName);
        restApiFacade.postDistributionForService(distribution, appName);
        restApiFacade.postWhitelistForService(whitelisted, appName);

        PendingChangesStatus pendingChangesStatus = restApiFacade.getPendingChangesForService(appName);
        IfExpression actualFlavorRule = (IfExpression) pendingChangesStatus.getPathRules().get("flavorRule").getChangedExpression();
        IfExpression actualUrlRuleRule = (IfExpression) pendingChangesStatus.getUrlRules().get("urlRule").getChangedExpression();
        Rule actualDistributionRule0 = (Rule) pendingChangesStatus.getDistributions().get("0").getChangedExpression();
        Rule actualDistributionRule1 = (Rule) pendingChangesStatus.getDistributions().get("1").getChangedExpression();
        Server actualDefaultServer = (Server) pendingChangesStatus.getServers().get("default").getChangedExpression();
        UrlRule actualDefaultUrlParams = (UrlRule) pendingChangesStatus.getUrlParams().get("default").getChangedExpression();
        Value actualWhitelistedStack = (Value)pendingChangesStatus.getWhitelisted().values().iterator().next().getChangedExpression();

        Assert.assertEquals(flavorRule, actualFlavorRule);
        Assert.assertEquals(urlRule, actualUrlRuleRule);
        Assert.assertEquals(distribution.getRules().get(0), actualDistributionRule0);
        Assert.assertEquals(distribution.getRules().get(1), actualDistributionRule1);
        Assert.assertEquals(defaultServer.getPath(), actualDefaultServer.getPath());
        Assert.assertEquals(defaultUrlParams, actualDefaultUrlParams);
        Assert.assertEquals("/dc/stack4", actualWhitelistedStack.getValue());

        helper.stopDataStore();
    }

    @Test
    @Ignore("TODO: figure out cause of failure")
    public void entitiesAreSavedCorrectlyInMultiUserEnvironment() throws Exception {
        int numberOfActionsPerItem = 10;
        int numberOfItemTypes = 6;
        int numberOfThreads = numberOfItemTypes * numberOfActionsPerItem;

        String appName = "test";
        TestContext context = new ContextBuilder().forApp(appName)
            .withWhitelist("/dc/stack1", "/dc/stack2", "/dc/stack3")
            .withHosts()
                .stack("/dc/stack1").flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app("test")
                .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app("test")
                .stack("/dc/stack3").flavor("flavor3").ipv4("10.0.0.4").ipv6("ff01::44").app("test")
            .build();
        UrlRule defaultUrlParams = newUrlParams("any", "xre", "10000", "6");
        Server defaultServer = new ServerBuilder().withFlavor("flavor1").build();
        Whitelisted whitelisted = new Whitelisted(Arrays.asList("/dc/stack1", "/dc/stack2", "/dc/stack3", "/dc/stack4"));
        Distribution distribution = new DistributionBuilder()
            .withRules(
                Arrays.asList(
                    newDistributionRule(0, 10.0f, new ServerBuilder().withName("0").withFlavor("flavor2").build()),
                    newDistributionRule(1, 20.0f, new ServerBuilder().withName("1").withFlavor("flavor3").build())
                )
            )
            .build();

        Set<Callable<Expressions>> tasks = new HashSet<>(numberOfThreads);
        for (int i=0; i < numberOfActionsPerItem; i++) {
            tasks.add(() -> {
                restApiFacade.postDefaultServerForService(defaultServer, appName);
                return defaultServer;
            });

            tasks.add(() -> {
                restApiFacade.postDefaultUrlParamsForService(defaultUrlParams, appName);
                return defaultUrlParams;
            });

            tasks.add(() -> {
                restApiFacade.postWhitelistForService(whitelisted, appName);
                return whitelisted;
            });

            tasks.add(() -> {
                restApiFacade.postDistributionForService(distribution, appName);
                return distribution;
            });

            final String flavorRuleName = "flavorRule" + i;
            tasks.add(() -> {
                IfExpression flavorRule = new IfExpressionBuilder()
                    .withRuleName(flavorRuleName)
                    .withExpression(newSingleParamExpression(Equals.class, "anyParam", "anyValue" + appName))
                    .withReturnStatement(newSimpleServerForFlavor("flavor1"))
                    .build();
                restApiFacade.postFlavorRuleForService(flavorRule, appName);

                return flavorRule;
            });

            final String urlRuleName = "urlRule" + i;
            tasks.add(() -> {
                IfExpression urlRule = new IfExpressionBuilder()
                    .withRuleName(urlRuleName)
                    .withExpression(newSingleParamExpression(Equals.class, "anyParam", "anyValue" + appName))
                    .withReturnStatement(newUrlParams("any", "xre", "10000", "4"))
                    .build();
                restApiFacade.postUrlRuleForService(urlRule, appName);

                return urlRule;
            });
        }

        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        executorService.invokeAll(tasks);

        PendingChangesStatus pendingChangesStatus = restApiFacade.getPendingChangesForService(appName);
        Rule actualDistributionRule0 = (Rule) pendingChangesStatus.getDistributions().get("0").getChangedExpression();
        Rule actualDistributionRule1 = (Rule) pendingChangesStatus.getDistributions().get("1").getChangedExpression();
        Server actualDefaultServer = (Server) pendingChangesStatus.getServers().get("default").getChangedExpression();
        UrlRule actualDefaultUrlParams = (UrlRule) pendingChangesStatus.getUrlParams().get("default").getChangedExpression();
        Value actualWhitelistedStack = (Value)pendingChangesStatus.getWhitelisted().values().iterator().next().getChangedExpression();

        for (int i=0; i< 10; i++) {
            Assert.assertTrue(pendingChangesStatus.getPathRules().keySet().contains("flavorRule" + i));
            Assert.assertTrue(pendingChangesStatus.getUrlRules().keySet().contains("urlRule" + i));
        }
        Assert.assertEquals(distribution.getRules().get(0), actualDistributionRule0);
        Assert.assertEquals(distribution.getRules().get(1), actualDistributionRule1);
        Assert.assertEquals(defaultServer.getPath(), actualDefaultServer.getPath());
        Assert.assertEquals(defaultUrlParams, actualDefaultUrlParams);
        Assert.assertEquals("/dc/stack4", actualWhitelistedStack.getValue());

        helper.stopDataStore();
    }
}
