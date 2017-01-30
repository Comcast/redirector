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

package it.backup.filesystem;

import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.*;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.core.spring.configurations.common.ModelBeans;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.context.TestNamespacedList;
import it.helper.DataStoreSupport;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.common.function.Wrappers.unchecked;
import static it.context.Operations.EQUALS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        CommonBeans.class, BackupBeans.class, ModelBeans.class,
        IntegrationTestBeans.class, FileSystemIntegrationTestBackupBeans.class, IntegrationTestApplicationsBeans.class
    }
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BackupsIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(BackupsIntegrationTest.class);

    private static final String APP_A = "backupAppA";
    private static final String APP_B = "backupAppB";

    private static final String DC = "BackupsIntegrationTest";
    private static final String STACK = "stack";

    private static final String DEFAULT_FLAVOR = "default";
    private static final String RULE_FLAVOR = "rule";
    private static final String DISTRIBUTION_FLAVOR = "distribution";

    @Autowired
    private ZKConfig config;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    @Autowired
    private IntegrationTestChangeListener<NamespacedListsBatch> newBatchAppliedListener;
    @Autowired
    @Qualifier("integrationTestChangeListener")
    private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;

    private TestContext contextA;
    private TestContext contextB;
    private IntegrationTestHelper testHelperA;
    private IntegrationTestHelper testHelperB;

    private IntegrationTestHelper.Builder helperBuilder;
    private DataStoreSupport dataStoreSupport;

    private String currentPath;

    private HttpServer httpServer;

    private void deleteBackups() throws Exception {
        if (Files.exists(Paths.get(currentPath))) {
            Files.walk(Paths.get(currentPath)).sorted((path1, path2) -> path2.getNameCount() - path1.getNameCount())
                .forEach(unchecked(Files::delete));
        }
    }

    @After
    public void after() throws Exception {
        deleteBackups();

        httpServer.shutdownNow();
        dataStoreSupport.shutdown();
    }

    @Before
    public void before() throws Exception {
        currentPath = config.getBackupBasePath();
        deleteBackups();

        ((Config)config).setExcludedAppsForStackAutoDiscovery(APP_A);
        ((Config)config).setAppsRetrievingIntervalInSeconds(1);
        ((Config)config).setDiscoveryUpdatePeriod(1);

        dataStoreSupport = new DataStoreSupport(config);
        dataStoreSupport.startZookeeper();
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath());
        httpServer.start();

        helperBuilder = new IntegrationTestHelper.Builder().config(config).redirectorEngine(redirectorEngine)
            .integrationTestStacksChangeListener(integrationTestStacksChangeListener)
            .integrationTestModelRefreshListener(integrationTestModelRefreshListener);
    }

    @Test(timeout = 60000)
    public void test1_StaticModelIsBackedUpIntoProperFiles() throws Exception {
        contextA = setupContextForApp(APP_A, false /* static */);
        testHelperA = helperBuilder.context(contextA).build();
        testHelperA.getDataStore().setupEnvForContext();

        verifyStaticModelIsBackedUpIntoProperFiles_WhenModelIsReloaded();
        verifyNamespacedListIsBackedUpIntoProperFiles_WhenNamespacedListIsReloaded();
    }

    private void verifyStaticModelIsBackedUpIntoProperFiles_WhenModelIsReloaded() throws Exception {
        deleteBackups();
        testHelperA.getRedirectorSupport().triggerModelUpdate();


        Assert.assertTrue("Flavor rules backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_A + File.separator + "selectserver.xml")));
        Assert.assertTrue("Url rules backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_A + File.separator + "urlrules.xml")));
        Assert.assertTrue("Whitelist backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_A + File.separator + "whitelist.xml")));
        Assert.assertTrue("Model metadata backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_A + File.separator + "modelmetadata.json")));
        Assert.assertTrue("Static stacks backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_A + File.separator + "manualbackup.json")));
    }

    private void verifyDynamicModelIsBackedUpIntoProperFiles_WhenModelIsReloaded() throws Exception {
        deleteBackups();
        Thread.sleep(config.getDiscoveryPullInterval());
        testHelperB.getRedirectorSupport().triggerModelUpdate();
        Thread.sleep(2000); // TODO: find better way to wait till all backup files are written

        Assert.assertFalse("Static stacks backup should be absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "manualbackup.json")));
        Assert.assertTrue("Dynamic stacks backup should be present", Files.exists(Paths.get(currentPath + File.separator + "stacks.json")));
        Assert.assertTrue("Flavor rules backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "selectserver.xml")));
        Assert.assertTrue("Url rules backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "urlrules.xml")));
        Assert.assertTrue("Whitelist backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "whitelist.xml")));
        Assert.assertTrue("Model metadata backup is absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "modelmetadata.json")));
    }

    private void verifyNamespacedListIsBackedUpIntoProperFiles_WhenNamespacedListIsReloaded() throws Exception {
        deleteBackups();
        contextA.setNamespacedLists(Arrays.asList(new TestNamespacedList("integrationTest", "1", "2")));

        Lock listUpdateLock = new ReentrantLock();
        Condition updated = listUpdateLock.newCondition();
        newBatchAppliedListener.setCallback(event -> {
            log.info("Namespaced List updated in data store");
            if (event.getData().getNamespacedLists().containsKey("integrationTest")) {
                listUpdateLock.lock();
                try {
                    updated.signalAll();
                } finally {
                    listUpdateLock.unlock();
                }
            }
        });
        testHelperA.getDataStore().putNamespacedList();
        listUpdateLock.lock();
        try {
            updated.await(1, TimeUnit.SECONDS);
        } finally {
            listUpdateLock.unlock();
        }
        log.info("Unlocked test. Keep going");
        Thread.sleep(1000); // TODO: find better way to wait till all backup files are written

        Assert.assertTrue("Namespaced list backup is absent", Files.exists(Paths.get(currentPath + File.separator + "namespacedlists.json")));
    }

    private void verifyDynamicStacksBackupsAreWritten_WhenNewHostsFromExistingStackFlavorAreRegistered() throws Exception {
        deleteBackups();

        testHelperB.getDataStore().registerNewDynamicHost(new TestContext.Host(DC, STACK, DEFAULT_FLAVOR + "-" + APP_B, APP_B, "ipv4_new", "ipv6_new"));

        Assert.assertFalse("Static stacks backup should be absent", Files.exists(Paths.get(currentPath + File.separator + APP_B + File.separator + "manualbackup.json")));
        Assert.assertTrue("Dynamic stacks backup should be present", Files.exists(Paths.get(currentPath + File.separator + "stacks.json")));
    }

    private void verifyDynamicStacksBackupsAreWritten_WhenNewHostsFromNonExistingAreRegistered() throws Exception {
        deleteBackups();

        testHelperB.getDataStore().registerNewDynamicHost(new TestContext.Host(DC + "_new", STACK, "someCompletelyNewFlavor", "someNewApp", "ipv4_new", "ipv6_new"));

        Assert.assertTrue("Dynamic stacks backup should be present", Files.exists(Paths.get(currentPath + File.separator + "stacks.json")));
    }

    @Test(timeout = 30000)
    public void test2_DynamicStacksAreBackedUpIntoProperFiles() throws Exception {
        contextB = setupContextForApp(APP_B, true /* dynamic */);
        testHelperB = helperBuilder.context(contextB).build();
        testHelperB.getDataStore().setupEnvForContext();

        verifyDynamicModelIsBackedUpIntoProperFiles_WhenModelIsReloaded();
        verifyApplicationsAreBackedUpIntoProperFiles();
        verifyDynamicStacksBackupsAreWritten_WhenNewHostsFromExistingStackFlavorAreRegistered();
        verifyDynamicStacksBackupsAreWritten_WhenNewHostsFromNonExistingAreRegistered();
    }

    private void verifyApplicationsAreBackedUpIntoProperFiles() throws Exception {
        redirectorEngine.startLookingForAppsChanges(); // This line is very important. It enables lookup for apps. In tests we don't enable apps lookup by default
        Thread.sleep(1000);
        Assert.assertTrue("Dynamic applications backup should be present", Files.exists(Paths.get(currentPath + File.separator + "applications.json")));
    }

    private TestContext setupContextForApp(String appName, boolean dynamic) {
        String whitelistedStack = DELIMETER + DC + DELIMETER + STACK;

        return new ContextBuilder().forApp(appName).dynamic(dynamic)
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
