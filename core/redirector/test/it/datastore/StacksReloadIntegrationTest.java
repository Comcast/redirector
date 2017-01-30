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
 * @author Paul Guslisty (pguslisty@productengine.com)
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
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static it.context.Operations.EQUALS;

@RunWith(SpringJUnit4ClassRunner.class)
@TestContextConfiguration
@ActiveProfiles(IntegrationTestConfigBeans.ProfileNames.STACKS_RELOAD_TEST)
public class StacksReloadIntegrationTest {

    @Autowired
    private ZKConfig config;

    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;

    @Autowired
    private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;

    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;

    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelInitListener;

    @Autowired
    private IntegrationTestChangeListener<String> integrationTestStacksReloadListener;

    private TestContext context;
    private IntegrationTestHelper testHelper;

    private String appName;

    private static String DEFAULT_SERVER_IPv4 = "10.0.0.2";
    private static String FLAVOR_IP_TO_REDIRECT_TO = "10.0.0.16";
    private HttpServer httpServer;

    @Before
    public void setUp() throws Exception {

        appName = "StacksReloadIntegrationTest_" + System.currentTimeMillis();

        ((Config)config).setExcludedAppsForStackAutoDiscovery(appName);

        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.ProfileNames.STACKS_RELOAD_TEST);
        httpServer.start();

        String whitelistedStack = DELIMETER + "po" + DELIMETER + "poc7";

        context = new ContextBuilder().forApp(appName)
                .withDefaultServer().flavor("discovery-default")
                .withFlavorRule().id("NativeReceiver").left("receiverType").operation(EQUALS).right("Native").flavor("new-added-stack")
                .withDistribution().percent("50.00").flavor("discovery-distribution")
                .withUrlRule().id("no-matter").left("A").operation(EQUALS).right("B").urn("shell").protocol("xre").port("10001").ipv("4")
                .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
                .withWhitelist("/sanityTestDC/sanityZone", "/po/poc7", "/po/poc6", "/br/brc6")
                .withNamespacedList("stub", "stub")
                .withHosts()
                .stack(whitelistedStack).defaultFlavor().ipv4(DEFAULT_SERVER_IPv4).ipv6("ff01::42").currentApp()
                .stack(whitelistedStack).distributionFlavor().ipv4("10.0.0.4").ipv6("ff01::44").currentApp()
                .stack("/po/poc7").defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
                .stack("/po/poc7").distributionFlavor().ipv4("10.0.0.4").ipv6("ff01::44").currentApp()
                .build();

        testHelper = new IntegrationTestHelper.Builder()
                .context(context)
                .config(config)
                .redirectorEngine(redirectorEngine)
                .integrationTestModelRefreshListener(integrationTestModelInitListener)
                .integrationTestStacksChangeListener(integrationTestStacksChangeListener)
                .integrationTestStacksReloadListener(integrationTestStacksReloadListener)
                .build();
        testHelper.getDataStore().setupEnvForContext();
        redirectorEngine.startLookingForAppsChanges();

        Thread.sleep(config.getDiscoveryPullInterval());
        testHelper.getRedirectorSupport().triggerModelUpdate();
    }
    
    @After
    public void tearDown () {
        httpServer.shutdownNow();
    }
    
    @Test
    public void testStacksReload() throws Exception {

        // try to redirect to 'NativeReceiver' flavor rule which server in not registered yet
        // should redirect to default server
        Map<String, String> parameters = Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getRightOperand());
        InstanceInfo defaultRedirectResult = testHelper.getRedirectorSupport().redirect(parameters);

        // redirect should go to the default server
        Assert.assertEquals(defaultRedirectResult.getAddress(), DEFAULT_SERVER_IPv4);

        // now register server of 'NativeReceiver' flavor rule
        testHelper.getDataStore().registerNewStaticHost(new TestContext.Host("po", "poc6", "new-added-stack", appName, FLAVOR_IP_TO_REDIRECT_TO, "ff01::16"));
        //waiting for polling refresh
        TimeUnit.SECONDS.sleep(3);
        // trigger stacks reload
        testHelper.getRedirectorSupport().triggerStacksReload();

        // now server of 'NativeReceiver' flavor rule should be added and "seen" by redirector engine
        // try again to redirect do 'NativeReceiver' flavor rule
        InstanceInfo flavorReidrectResult = testHelper.getRedirectorSupport().redirect(parameters);


        // since server of 'NativeReceiver' flavor rule is registered redirect should go to this flavor rule
        Assert.assertEquals(flavorReidrectResult.getAddress(), FLAVOR_IP_TO_REDIRECT_TO);
    }

}
