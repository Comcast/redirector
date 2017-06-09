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

package it.datastore;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.TestContextConfiguration;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static it.context.Operations.EQUALS;

@RunWith(SpringJUnit4ClassRunner.class)
@TestContextConfiguration
@ActiveProfiles(IntegrationTestConfigBeans.ProfileNames.MULTIPLE_APPS_STATIC_DISCOVERY)
public class MultipleAppsIntegrationTest {
    private static final int APPS_RETRIEVAL_PERIOD_SECONDS = 1;

    @Autowired
    private ZKConfig config;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;

    private static final String DC = "multiapp";
    private static final String STACK = "stack";

    private static final String DEFAULT_FLAVOR = "default";
    private static final String RULE_FLAVOR = "rule";
    private static final String DISTRIBUTION_FLAVOR = "distribution";
    private static final String APP_A = "appA" + System.currentTimeMillis();
    private static final String APP_B = "appB" + System.currentTimeMillis();

    private IntegrationTestHelper testHelperA;
    private IntegrationTestHelper testHelperB;

    private HttpServer httpServer;

    @Before
    public void setUp() throws Exception {
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.ProfileNames.MULTIPLE_APPS_STATIC_DISCOVERY);
        httpServer.start();
        TimeUnit.SECONDS.sleep(2);
        ((Config)config).setExcludedAppsForStackAutoDiscovery(APP_A + "," + APP_B);
        ((Config)config).setAppsRetrievingIntervalInSeconds(APPS_RETRIEVAL_PERIOD_SECONDS);

        TestContext contextA = setupContextForApp(APP_A);
        TestContext contextB = setupContextForApp(APP_B);

        IntegrationTestHelper.Builder helperBuilder = new IntegrationTestHelper.Builder()
            .config(config).redirectorEngine(redirectorEngine)
            .integrationTestModelRefreshListener(integrationTestModelRefreshListener);
        testHelperA = helperBuilder.context(contextA).build();
        testHelperB = helperBuilder.context(contextB).build();

        testHelperA.getDataStore().setupEnvForContext();
        redirectorEngine.startLookingForAppsChanges();
    }

    @After
    public void tearDown () {
        httpServer.shutdownNow();
    }

    @Test
    public void test() throws Exception {
        testHelperA.getRedirectorSupport().triggerModelUpdate();
        testDefaultServer(testHelperA); // model for app A return server
        testDefaultServerFailure(testHelperB); // model for app B returns null

        testHelperB.getDataStore().setupEnvForContext();
        TimeUnit.SECONDS.sleep(APPS_RETRIEVAL_PERIOD_SECONDS + 1);

        testHelperB.getRedirectorSupport().triggerModelUpdate(); // update model B
        testDefaultServer(testHelperB); // model B now returns server as well
        testDefaultServer(testHelperA); // model A keeps working correctly as well
    }

    private void testDefaultServer(IntegrationTestHelper testHelper) throws Exception {

        //todo temp
        TimeUnit.SECONDS.sleep(5);
        InstanceInfo result = testHelper.getRedirectorSupport().redirect();
        Assert.assertEquals("Redirector should return default server flavor by default",
            testHelper.getContext().getDefaultServer().getFlavor(), result.getFlavor());
    }

    private void testDefaultServerFailure(IntegrationTestHelper testHelper) throws Exception {
        InstanceInfo result = testHelper.getRedirectorSupport().redirect();
        Assert.assertNull("Redirector should return fallback server flavor", result);
    }

    private TestContext setupContextForApp(String appName) {
        String whitelistedStack = DELIMETER + DC + DELIMETER + STACK;

        return new ContextBuilder().forApp(appName)
            .withFlavorRule().left("A").operation(EQUALS).right("B").flavor(RULE_FLAVOR + "-" + appName)
            .withDefaultServer().flavor(DEFAULT_FLAVOR + "-" + appName)
            .withDistribution().percent("50.00").flavor(DISTRIBUTION_FLAVOR + "-" + appName)
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist(DELIMETER + DC + DELIMETER + STACK)
            .withNamespacedList("stub", "stub")
            .withHosts()
                .stack(whitelistedStack).flavorRuleFlavor().ipv4("10.0.0.1").ipv6("ff01::41").currentApp()
                .stack(whitelistedStack).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
                .stack(whitelistedStack).distributionFlavor().ipv4("10.0.0.4").ipv6("ff01::44").currentApp()
            .build();
    }
}
