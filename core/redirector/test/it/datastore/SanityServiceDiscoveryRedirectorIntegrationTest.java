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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package it.datastore;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.TestContextConfiguration;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.context.TestWhitelist;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;

import static it.context.Operations.EQUALS;

// TODO: introduce a way to shutdown Redirector per app. So at the end of test Redirector is shutdown and zNodes are removed
@RunWith(SpringJUnit4ClassRunner.class)
@TestContextConfiguration
@ActiveProfiles(IntegrationTestConfigBeans.ProfileNames.SANITY_DYNAMIC_DISCOVERY)
public class SanityServiceDiscoveryRedirectorIntegrationTest {
    @Autowired
    private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelInitListener;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private ZKConfig config;

    private TestContext context;
    private IntegrationTestHelper testHelper;

    private HttpServer httpServer;

    @Before
    public void setUp() throws Exception {
        ((Config)config).setDiscoveryUpdatePeriod(1);

        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.ProfileNames.SANITY_DYNAMIC_DISCOVERY);
        httpServer.start();

        context = new ContextBuilder().forApp("sampleAppDiscovery" + System.currentTimeMillis())
            .withDefaultServer().flavor("discovery-default")
            .withFlavorRule().id("NativeReceiver").left("receiverType").operation(EQUALS).right("Native").flavor("discovery-rule")
            .withDistribution().percent("50.00").flavor("discovery-distribution")
            .withUrlRule().id("no-matter").left("A").operation(EQUALS).right("B").urn("shell").protocol("xre").port("10001").ipv("4")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist("/sanityTestDC/sanityZone", "/po/poc6", "/br/brc6")
            .withNamespacedList("stub", "stub")
            .withHosts()
                .stack("/po/poc6").defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
                .stack("/po/poc6").distributionFlavor().ipv4("10.0.0.4").ipv6("ff01::44").currentApp()
        .build();

        testHelper = new IntegrationTestHelper.Builder()
            .context(context)
            .config(config)
            .redirectorEngine(redirectorEngine)
            .integrationTestModelRefreshListener(integrationTestModelInitListener)
            .integrationTestStacksChangeListener(integrationTestStacksChangeListener)
            .build();
        testHelper.getDataStore().setupEnvForContext();
        redirectorEngine.startLookingForAppsChanges();
    }

    @After
    public void tearDown () {
        httpServer.shutdownNow();
    }

    @Test(timeout = 170000)
    public void test() throws Exception {
        //////////// dynamically registered ip is visible without reloading the model
        testHelper.getRedirectorSupport().initRedirector(20);

        testHelper = new IntegrationTestHelper.Builder()
            .context(context)
            .config(config)
            .redirectorEngine(redirectorEngine)
            .integrationTestModelRefreshListener(integrationTestModelRefreshListener)
            .integrationTestStacksChangeListener(integrationTestStacksChangeListener)
            .build();

        testHelper.getDataStore().simulateStacksChangedEventFromWS();
        testHelper.getRedirectorSupport().triggerModelUpdate();
        testHelper.getDataStore().registerNewDynamicHost(new TestContext.Host("sanityTestDC", "sanityZone", context.getFlavorRule().getFlavor(), context.getAppName(), "127.0.0.1", "ff01::41"));

        testHelper.getDataStore().simulateStacksChangedEventFromWS();

        testFlavorRuleIp("127.0.0.1", context.getFlavorRule().getFlavor());

        //////////// model refresh for same app updates existing provider manager with new whitelist
        context.setWhitelist(new TestWhitelist("/po/poc6", "/br/brc6"));
        testHelper.getDataStore().putWhitelist();

        testHelper.getRedirectorSupport().triggerModelUpdate();

        testFlavorRuleIp("10.0.0.2", context.getDefaultServer().getFlavor());
    }

    private void testFlavorRuleIp(String ipShouldBe, String flavor) throws Exception {
        Map<String, String> parameters = Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getRightOperand());

        InstanceInfo result = testHelper.getRedirectorSupport().redirect(parameters);

        Assert.assertEquals("Flavor Rule should be applied. Redirector should return rule flavor", flavor, result.getFlavor());
        Assert.assertEquals("Flavor rule should return to stack for zookeeper service discovery", ipShouldBe, result.getServerIp());
    }
}
