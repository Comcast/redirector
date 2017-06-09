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

package com.comcast.redirector.api.redirector.cache;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.CachedModelVerifier;
import it.helper.IntegrationTestHelper;
import it.helper.ModelCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.common.RedirectorConstants.DEFAULT_SERVER_NAME;
import static com.comcast.redirector.common.RedirectorConstants.URL_TEMPLATE;
import static it.context.ContextBuilderUtils.*;
import static it.context.Operations.EQUALS;

public class CacheIntegrationTest extends BaseWebServiceIntegrationTest {

    private IntegrationTestHelper helper;

    @After
    public void after() throws Exception {
        if (helper != null)
            helper.stopDataStore();
        restApiEndpoint.stop();
    }

    @Test
    public void namespacedLists_AreCompletelyCached_AndReturnedFromCache_WhenDataSourceGoesDown() throws Exception {
        TestContext context = new ContextBuilder()
            .withNamespacedList("list1", "value1", "value2")
            .withNamespacedList("list2", "value21", "value22")
            .build();
        helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Thread.sleep(1000);
        stopDataSource(helper);

        Namespaces namespacedLists = restApiFacade.getAllNamespacedLists();

        Assert.assertNotNull(namespacedLists.getNamespaceByName("list1"));
        Assert.assertNotNull(namespacedLists.getNamespaceByName("list2"));
    }

    @Test
    public void hosts_AreCompletelyCached_AndReturnedFromCache_WhenDataSourceGoesDown() throws Exception {
        TestContext context = new ContextBuilder().withHosts()
            .stack("/dc/stack1").flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app("test")
            .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app("otherApp")
            .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.4").ipv6("ff01::44").app("newApp")
            .build();
        helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Thread.sleep(1000);
        stopDataSource(helper);

        ServicePaths allStacks = restApiFacade.getAllStackPaths();

        StacksResultVerifier verification = new StacksResultVerifier(allStacks);
        verification.verifyFlavorExistsForService("flavor1", "test");
        verification.verifyFlavorExistsForService("flavor2", "otherApp");
        verification.verifyFlavorExistsForService("flavor2", "newApp");
        verification.verifyActiveNodesCountForService(1, "test");
        verification.verifyActiveNodesCountForService(1, "otherApp");
        verification.verifyActiveNodesCountForService(1, "newApp");
    }

    @Test
    public void allStacks_ReturnWhiteListedCounts_WhenWhitelistIsPreCached_ForApp() throws Exception {
        String whiteListedAppName = "test";
        String whiteListedStack = "/dc/stack1";
        TestContext context = new ContextBuilder().forApp(whiteListedAppName)
            .withWhitelist(whiteListedStack)
            .withHosts()
                .stack(whiteListedStack).flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app(whiteListedAppName)
                .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app("otherApp")
                .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.4").ipv6("ff01::44").app("newApp")
            .build();
        helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Thread.sleep(1000);
        stopDataSource(helper);

        ServicePaths allStacks = restApiFacade.getAllStackPaths();

        StacksResultVerifier verifier = new StacksResultVerifier(allStacks);
        verifier.verifyWhitelistedNodesCountForService(1, whiteListedAppName);
        verifier.verifyWhitelistedNodesCountForService(0, "otherApp");
        verifier.verifyWhitelistedNodesCountForService(0, "newApp");
    }

    @Test
    public void model_IsCompletelyCached_AndReturnedFromCache_WhenDataSourceGoesDown() throws Exception {
        String service = "myService";
        String whiteListedStack = "/dc/uniqueStack";
        String ruleFlavor = getRuleFlavorForApp(service);
        String defaultFlavor = getDefaultFlavorForApp(service);
        String distributionFlavor = getDistributionFlavorForApp(service);
        String defaultUrlProtocol = "4";

        TestContext context = new ContextBuilder().forApp(service)
            .withFlavorRule().id("flavorRule").left("A").operation(EQUALS).right("B").flavor(ruleFlavor)
            .withDefaultServer().flavor(defaultFlavor)
            .withDistribution().percent("50.00").flavor(distributionFlavor)
            .withUrlRule().id("urlRule").left("A").operation(EQUALS).right("B").ipv("6")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv(defaultUrlProtocol)
            .withWhitelist(whiteListedStack)
                .withHosts()
                .stack(whiteListedStack).flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app(service)
            .build();

        helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Thread.sleep(1000);
        ModelCache cache = new ModelCacheOnRestEndpoint(service);
        stopDataSource(helper);

        CachedModelVerifier verifier = new CachedModelVerifier(cache);
        verifier.verifyFlavorRulePresentInCache("flavorRule");
        verifier.verifyUrlRulePresentInCache("urlRule");
        verifier.verifyDefaultServerFlavor(defaultFlavor);
        verifier.verifyDistributionFlavor(distributionFlavor);
        verifier.verifyDefaultUrlProtocol(defaultUrlProtocol);
        verifier.verifyWhitelistContainsStack(whiteListedStack);
    }

    @Test
    public void pendingChanges_AreReturnedFromCache_WhenDataSourceGoesDown() throws Exception {
        String service = "myService";
        String whiteListedStack = "/dc/uniqueStack";
        String anotherWhiteListedStack = "/dc/uniqueStack2";
        String ruleFlavor = getRuleFlavorForApp(service);
        String defaultFlavor = getDefaultFlavorForApp(service);
        String distributionFlavor = getDistributionFlavorForApp(service);
        String defaultUrlProtocol = "4";

        TestContext context = new ContextBuilder().forApp(service)
            .withFlavorRule().id("flavorRule").left("A").operation(EQUALS).right("B").flavor(ruleFlavor)
            .withDefaultServer().flavor(defaultFlavor)
            .withDistribution().percent("50.00").flavor(distributionFlavor)
            .withUrlRule().id("urlRule").left("A").operation(EQUALS).right("B").ipv("6")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv(defaultUrlProtocol)
            .withWhitelist(whiteListedStack)
            .withWhitelist(whiteListedStack, anotherWhiteListedStack)
            .withHosts()
                .stack(whiteListedStack).flavor(defaultFlavor).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                .stack(anotherWhiteListedStack).flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app(service)
                .stack("/dc/stack2").flavor("flavor2").ipv4("10.0.0.4").ipv6("ff01::44").app(service)
            .build();

        helper = setupTestHelperBuilder(context).startDataStore();
        restApiEndpoint.start();
        Thread.sleep(1000);

        Server expectedPendingDefaultServer = new Server();
        expectedPendingDefaultServer.setName(DEFAULT_SERVER_NAME);
        expectedPendingDefaultServer.setPath("flavor2");
        expectedPendingDefaultServer.setUrl(URL_TEMPLATE);
        restApiFacade.postDefaultServerForService(expectedPendingDefaultServer, service);

        stopDataSource(helper);

        PendingChangesStatus pendingChangesStatus = restApiFacade.getPendingChangesForService(service);
        Assert.assertEquals(1, pendingChangesStatus.getVersion().intValue());
        Assert.assertEquals(expectedPendingDefaultServer.getUrl(), pendingChangesStatus.getPendingDefaultServer().getUrl());
    }

    private void stopDataSource(IntegrationTestHelper helper) throws IOException, InterruptedException {
        helper.stopDataStore();
        TimeUnit.MILLISECONDS.sleep(getDataSourceSessionTimeout());
    }

    private class ModelCacheOnRestEndpoint implements ModelCache {
        private String service;

        ModelCacheOnRestEndpoint(String service) {
            this.service = service;
        }

        @Override
        public Whitelisted getWhitelist() {
            return restApiFacade.getWhiteListForService(service);
        }

        @Override
        public Collection<IfExpression> getFlavorRules() {
            return restApiFacade.getFlavorRulesForService(service).getItems();
        }

        @Override
        public Collection<IfExpression> getUrlRules() {
            return restApiFacade.getUrlRulesForService(service).getItems();
        }

        @Override
        public Distribution getDistribution() {
            return restApiFacade.getDistributionForService(service);
        }

        @Override
        public Server getDefaultServer() {
            return restApiFacade.getServerForService(service);
        }

        @Override
        public UrlRule getUrlParams() {
            return restApiFacade.getUrlParamsForService(service).getUrlRule();
        }
    }

    private static class StacksResultVerifier {
        private ServicePaths allStacks;

        StacksResultVerifier(ServicePaths allStacks) {
            this.allStacks = allStacks;
        }

        private void verifyFlavorExistsForService(String flavor, String service) {
            Assert.assertEquals(1,
                allStacks.getPaths(service).getFlavors().stream()
                    .map(PathItem::getValue)
                    .filter(flavor::equals)
                    .count());
        }

        private void verifyActiveNodesCountForService(int expectedCount, String service) {
            Assert.assertEquals(
                new Integer(expectedCount),
                allStacks.getPaths(service).getFlavors().stream()
                    .map(PathItem::getActiveNodesCount)
                    .reduce((i,j) -> i + j).get());

        }

        private void verifyWhitelistedNodesCountForService(int expectedCount, String service) {
            Assert.assertEquals(
                new Integer(expectedCount),
                allStacks.getPaths(service).getFlavors().stream()
                    .map(PathItem::getWhitelistedNodesCount)
                    .reduce((i,j) -> i + j).get());

        }
    }

}
