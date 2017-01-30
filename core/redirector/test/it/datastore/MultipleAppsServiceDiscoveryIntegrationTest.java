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
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: come up with isolation of tests. Since all datastore integration tests use same zookeeper these tests are not isolated enough.
@RunWith(SpringJUnit4ClassRunner.class)
@TestContextConfiguration
@ActiveProfiles(IntegrationTestConfigBeans.ProfileNames.MULTIPLE_APPS_DYNAMIC_DISCOVERY)
public class MultipleAppsServiceDiscoveryIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(MultipleAppsServiceDiscoveryIntegrationTest.class);

    @Autowired
    private ZKConfig config;
    @Autowired
    private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private IBackupManagerFactory globalBackupManagerFactory;

    private HttpServer httpServer;

    @Before
    public void before() throws Exception {
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.ProfileNames.MULTIPLE_APPS_DYNAMIC_DISCOVERY);
        httpServer.start();

        ((Config) config).setDefaultWeightOfTheNode(1);
        ((Config) config).setAppsRetrievingIntervalInSeconds(2);
    }

    @After
    public void tearDown () {
        httpServer.shutdownNow();
    }

    @Test
    public void redirectHappensToHostOfProperApp_WhenMoreThenOneAppUsesSameFlavor_ForDynamicApps() throws Exception {
        TestContext contextFor1stApp = buildContextForDynamicAppStackAndFlavor("1stApp", "/1stAppDC/1stAppZone", "shared-flavor");
        TestContext contextFor2ndApp = buildContextForDynamicAppStackAndFlavor("2ndApp", "/1stAppDC/1stAppZone", "shared-flavor");

        verifyRedirectToFlavorReturnsIpForGivenApp(contextFor1stApp, contextFor2ndApp);
    }

    @Test
    public void redirectHappensToHostOfProperApp_WhenMoreThenOneAppUsesSameFlavor_ForStaticApps() throws Exception {
        makeAppsBeConsideredAsStatic("1stStaticApp", "2ndStaticApp");
        TestContext contextFor1stApp = buildContextForStaticAppStackAndFlavor("1stStaticApp", "/1stStaticAppDC/1stAppZone", "shared-flavor");
        TestContext contextFor2ndApp = buildContextForStaticAppStackAndFlavor("2ndStaticApp", "/1stStaticAppDC/1stAppZone", "shared-flavor");

        verifyRedirectToFlavorReturnsIpForGivenApp(contextFor1stApp, contextFor2ndApp);
    }

    private void makeAppsBeConsideredAsStatic(String... apps) {
        ((Config) config).setExcludedAppsForStackAutoDiscovery(Stream.of(apps).collect(Collectors.joining(",")));
    }

    private void verifyRedirectToFlavorReturnsIpForGivenApp(TestContext firstApp, TestContext secondApp) throws Exception {
        IntegrationTestHelper helperFirstApp = buildHelperForContext(firstApp);
        IntegrationTestHelper helperSecondApp = buildHelperForContext(secondApp);
        setupEnvironmentForTest(helperFirstApp, helperSecondApp);
        updateModels(helperFirstApp, helperSecondApp);


        InstanceInfo responseFirst = helperFirstApp.getRedirectorSupport().redirect();
        InstanceInfo responseSecond = helperFirstApp.getRedirectorSupport().redirect();

        Assert.assertSame(responseFirst.getAddress(), responseSecond.getAddress());
    }

    @Test
    public void discoveryBackupContainsAllHosts_WhenModelForDynamicAppIsRefreshed() throws Exception {
        TestContext contextFor1stApp = buildContextForDynamicAppStackAndFlavor("TestDiscoveryBackup1stApp", "/TestDiscoveryBackup/Zone1", "any-flavor");
        TestContext contextFor2ndApp = buildContextForDynamicAppStackAndFlavor("TestDiscoveryBackup2ndApp", "/TestDiscoveryBackup/Zone2", "any-flavor");
        IntegrationTestHelper helperFirstApp = buildHelperForContext(contextFor1stApp);
        IntegrationTestHelper helperSecondApp = buildHelperForContext(contextFor2ndApp);
        setupEnvironmentForTest(helperFirstApp, helperSecondApp);

        updateModels(helperFirstApp, helperSecondApp);


        String backup = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.DISCOVERY).load();
        log.info("Backup of service discovery is {}", backup);
        Assert.assertTrue(backup.contains("/TestDiscoveryBackup/Zone1"));
        Assert.assertTrue(backup.contains("/TestDiscoveryBackup/Zone2"));
    }

    private void setupEnvironmentForTest(IntegrationTestHelper... helpers) throws Exception {
        for (IntegrationTestHelper helper : helpers) {
            helper.getDataStore().setupEnvForContext();
        }
        redirectorEngine.startLookingForAppsChanges();
    }

    private void updateModels(IntegrationTestHelper... helpers) throws Exception {
        for (IntegrationTestHelper helper : helpers) {

            helper.getRedirectorSupport().triggerModelUpdate();
        }
        TimeUnit.SECONDS.sleep(4); // Need to wait for custom cache to perform backup
    }

    private TestContext buildContextForDynamicAppStackAndFlavor(String app, String stack, String flavor) {
        return buildContextForAppStackAndFlavor(app, stack, flavor, true);
    }

    private TestContext buildContextForStaticAppStackAndFlavor(String app, String stack, String flavor) {
        return buildContextForAppStackAndFlavor(app, stack, flavor, false);
    }

    private TestContext buildContextForAppStackAndFlavor(String app, String stack, String flavor, boolean dynamic) {
        return new ContextBuilder().forApp(app).dynamic(dynamic)
                .withDefaultServer().flavor(flavor)
                .withDistribution().percent("50.00").flavor("distribution")
                .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
                .withWhitelist(stack)
                .withNamespacedList("stub", "stub")
                .withHosts()
                    .stack(stack).defaultFlavor().ipv4(getIpV4ForApp(app)).ipv6(getIpV6ForApp(app)).currentApp()
            .build();
    }

    private static String getIpV4ForApp(String app) {
        return "ipv4_" + app;
    }

    private static String getIpV6ForApp(String app) {
        return "ipv6_" + app;
    }

    private IntegrationTestHelper buildHelperForContext(TestContext context) {
        return new IntegrationTestHelper.Builder()
                .context(context)
                .config(config)
                .redirectorEngine(redirectorEngine)
                .integrationTestModelRefreshListener(integrationTestModelRefreshListener)
                .integrationTestStacksChangeListener(integrationTestStacksChangeListener)
            .build();
    }
}
