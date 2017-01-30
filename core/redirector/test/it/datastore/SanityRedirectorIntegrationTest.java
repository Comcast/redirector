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

package it.datastore;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.RedirectorConstants;
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
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static it.context.Operations.EQUALS;

// TODO: introduce a way to shutdown Redirector per app. So at the end of test Redirector is shutdown and zNodes are removed
// so far as a workaround each integration test should work with different app names
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@TestContextConfiguration
@ActiveProfiles(IntegrationTestConfigBeans.ProfileNames.SANITY_STATIC_DISCOVERY)
public class SanityRedirectorIntegrationTest {
    private static final String ACCOUNT_ID_THAT_FIT_0_TO_50_PERCENTS = "5ee8bb7c3c285851db9b969e956afc36";
    private static final String ACCOUNT_ID_THAT_FIT_51_TO_100_PERCENTS = "cc004e653cc78176c82cba30329b1c68";

    @Autowired
    private ZKConfig config;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;

    private TestContext context;
    private IntegrationTestHelper testHelper;

    private HttpServer httpServer;

    @Before
    public void setup() throws Exception {

        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.ProfileNames.SANITY_STATIC_DISCOVERY);
        httpServer.start();

        context = new ContextBuilder().forApp("sampleApp" + System.currentTimeMillis()) // TODO: helper for getting unique names of apps into IntegrationTestHelper
            .withFlavorRule().id("NativeReceiver").operation(EQUALS).left("receiverType").right("Native").flavor("sanity-rule")
            .withDistribution().percent("50.00").flavor("sanity-distribution")
            .withDefaultServer().flavor("sanity-default")
            .withUrlRule().id("URL").operation(EQUALS).left("name").right("value").protocol("xres").port("10001").ipv("4").urn("shell")
            .withDefaultUrlParams().protocol("xre").port("10001").ipv("4").urn("shell")
            .withWhitelist("/po/poc6", "/br/brc6")
            .withNamespacedList("stub", "stub")
            .withHosts()
                .stack("/po/poc6").flavorRuleFlavor().ipv4("10.0.0.1").ipv6("rule-ipv6").currentApp()
                .stack("/po/poc6").defaultFlavor().ipv4("10.0.0.21").ipv6("default-ipv6").currentApp()
                .stack("/po/poc6").distributionFlavor().ipv4("10.0.0.4").ipv6("distribution-ipv6").currentApp()
            .build();
        ((Config)config).setExcludedAppsForStackAutoDiscovery(context.getAppName());

        if (testHelper == null) { // init test helper only once
            testHelper = new IntegrationTestHelper.Builder()
                .context(context)
                .config(config)
                .redirectorEngine(redirectorEngine)
                .integrationTestModelRefreshListener(integrationTestModelRefreshListener)
                .build();

            testHelper.getDataStore().setupEnvForContext();
            redirectorEngine.startLookingForAppsChanges();
            testHelper.getRedirectorSupport().triggerModelUpdate();
            TimeUnit.SECONDS.sleep(config.getModelPollIntervalSeconds() * 2);
        }
    }

    @After
    public void tearDown () {
        httpServer.shutdownNow();
    }

    @Test
    public void defaultServer() throws Exception {
        InstanceInfo result = testHelper.getRedirectorSupport().redirect();
        // TODO: fixTEST
        Assert.assertEquals("Redirector should return default server flavor by default", context.getDefaultServer().getFlavor(), result.getFlavor());
    }

    @Test
    public void flavorRuleSuccess() throws Exception {
        Map<String, String> parameters = Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getRightOperand());

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);

        Assert.assertEquals("Flavor Rule should be applied. Redirector should return rule flavor", context.getFlavorRule().getFlavor(), result.getFlavor());
    }

    @Test
    public void flavorRuleFailure() throws Exception {
        Map<String, String> parameters = Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getWrongRightOperand());

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);

        Assert.assertEquals("Flavor Rule should NOT be applied. Redirector should default server flavor", context.getDefaultServer().getFlavor(), result.getFlavor());
    }

    @Test
    public void distributionRuleSuccess() throws Exception {
        Map<String, String> parameters = Collections.singletonMap(RedirectorConstants.Parameters.ACCOUNT_ID, ACCOUNT_ID_THAT_FIT_0_TO_50_PERCENTS);

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);
// TODO: fixTEST
        Assert.assertEquals("Distribution rule should be applied", context.getDistribution().getFirstRuleFlavor(), result.getFlavor());
    }

    @Test
    public void distributionRuleFailure() throws Exception {
        Map<String, String> parameters = Collections.singletonMap(RedirectorConstants.Parameters.ACCOUNT_ID, ACCOUNT_ID_THAT_FIT_51_TO_100_PERCENTS);

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);
// TODO: fixTEST
        Assert.assertEquals("Distribution rule should NOT be applied", context.getDefaultServer().getFlavor(), result.getFlavor());
    }

    //do we need it?
    /*@Test
    public void testDefaultRuleAfterFlavorRuleReturnedNoHosts() throws Exception {
        testHelper.getDataStore().updateFlavorRuleToReturnFlavorWithNoHosts();
        testHelper.getRedirectorSupport().triggerModelUpdate();
        Map<String, String> parameters = Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getRightOperand());

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);

        Assert.assertEquals("Flavor Rule should be applied but since no hosts in returned flavor, fallback rule should be applied",
                context.getDefaultServer().getFlavor(), result.getFlavor());
    }*/
}
