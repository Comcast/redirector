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

package it.offline;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.applications.Applications;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilderMain;
import it.context.TestContext;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.context.ContextBuilderUtils.getDefaultFlavorForApp;

public class DynamicAppWorkWithBackupAndDataStoreIntegrationTest extends BaseOfflineIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(DynamicAppWorkWithBackupAndDataStoreIntegrationTest.class);

    @Before
    public void init() throws Exception {
        super.init(IntegrationTestConfigBeans.Profiles.OFFLINE_DYNAMIC_APP.getName());

        ((Config)config).setAppsRetrievingIntervalInSeconds(1);
        ((Config)config).setDiscoveryUpdatePeriod(1);
        httpServer.start();
    }

    @Test(timeout = 120000)
    public void backupsOfDynamicHostsAreUpdated_WhenHostsChangeInDataSource() throws Exception {
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC);
        TestContext context = contextBuilder.build();
        IntegrationTestHelper backupBasedHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();

        StackBackup backup = backupBasedHelper.getBackups().getDynamicHosts();
        verifyBackupHasHostsAndFlavors(backup, context.getFlavorRule().getFlavor(), context.getDefaultServer().getFlavor(), context.getDistribution().getFirstRuleFlavor());

        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();

        backup = dataSourceBasedHelper.getBackups().getDynamicHosts();
        verifyBackupHasHostsAndFlavors(backup, context.getFlavorRule().getFlavor(), context.getDefaultServer().getFlavor(), context.getDistribution().getFirstRuleFlavor());

        dataSourceBasedHelper.getDataStore().registerNewDynamicHost(new TestContext.Host("newdc", "newstack", "newFlavor", context.getAppName(), "127.0.0.1", "ff01::41"));
        dataSourceBasedHelper.getDataStore().simulateStacksChangedEventFromWS();

        backup = dataSourceBasedHelper.getBackups().getDynamicHosts();
        verifyBackupHasHostsAndFlavors(backup, "newFlavor", context.getFlavorRule().getFlavor(), context.getDefaultServer().getFlavor(), context.getDistribution().getFirstRuleFlavor());
    }

    @Test(timeout = 120000)
    public void backupOfApplicationsIsUpdated_WhenNewAppHostIsRegistered() throws Exception {
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC);
        contextBuilder.withApplications("AppA", "AppB", DYNAMIC_APP);

        TestContext context = contextBuilder.build();
        IntegrationTestHelper backupBasedHelper = setupBackupBasedTestHelper(context).setupEnvironmentAndModel();

        Applications backup = backupBasedHelper.getBackups().getApplications();
        verifyBackupsForApps(backup, "AppA", "AppB", DYNAMIC_APP);

        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);

        backup = dataSourceBasedHelper.getBackups().getApplications();
        verifyBackupsForApps(backup, DYNAMIC_APP);

        dataSourceBasedHelper.getDataStore().registerNewDynamicHost(new TestContext.Host("newdc", "newstack", "newFlavor", "newApp", "127.0.0.1", "ff01::41"));
        dataSourceBasedHelper.getDataStore().simulateStacksChangedEventFromWS();

        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);
        backup = dataSourceBasedHelper.getBackups().getApplications();
        verifyBackupsForApps(backup, DYNAMIC_APP, "newApp");
    }

    @Test(timeout = 120000)
    public void backupOfApplicationsIsUpdated_WhenNewAppIsRegisteredWithModel() throws Exception {
        ContextBuilderMain contextBuilder = setupContextBuilderForApp("AppA", Mode.DYNAMIC);
        TestContext context = contextBuilder.build();
        IntegrationTestHelper backupBasedHelper = setupBackupBasedTestHelper(context).setupEnvironmentAndModel();

        log.info("About to verify Applications backup before Zookeeper started");
        Applications backup = backupBasedHelper.getBackups().getApplications();
        log.info("Backup is {}", backup.getApps());

        contextBuilder = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC);
        context = contextBuilder.build();

        log.info("About to start zookeeper");
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);

        log.info("Zookeeper is successfully started. Let's check backup");
        backup = dataSourceBasedHelper.getBackups().getApplications();
        log.info("Backup2 is {}", backup.getApps());
        verifyBackupsForApps(backup, DYNAMIC_APP);
    }

    @Test
    public void dynamicAppUseInMemoryCache_WhenDataSourceGoesDown() throws Exception {
        String appName = "inMemoryCacheTestDynamicApp";
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(appName, Mode.DYNAMIC);
        contextBuilder.withApplications("AppA", "AppB", appName);

        TestContext context = contextBuilder.build();
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        TimeUnit.SECONDS.sleep(1); // TODO: get rid of it.

        verifyRedirect(dataSourceBasedHelper);

        dataSourceBasedHelper.stopDataStore();

        verifyRedirectAfterPause(dataSourceBasedHelper, config.getZooKeeperConnectionTimeout() + 500);
        verifyRedirectAfterPause(dataSourceBasedHelper, config.getZooKeeperSessionTimeout() + 500);
    }

    @Test
    public void defaultWeightIsApplied_WhenNoWeightInHostIsDefined() throws Exception {
        String appName = "defaultWeightDynamicApp";
        int numOfUniqueHosts = 2;
        TestContext context = setupContextWithNHostsInDefaultFlavor(appName, numOfUniqueHosts, true);
        IntegrationTestHelper helper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        Thread.sleep(1000); // TODO: use listener instead

        List<String> ipAddresses = new ArrayList<>();
        for (int i = 0; i < config.getDefaultWeightOfTheNode() * numOfUniqueHosts; i++) {
            InstanceInfo result = helper.getRedirectorSupport().redirect();
            ipAddresses.add(result.getServerIp());
        }

        verifyWeights(numOfUniqueHosts, ipAddresses);
    }

    @Test
    public void customWeightIsApplied_WhenHostIsRegisteredWithWeight() throws Exception {
        String appName = "CustomWeightDynamicApp";
        int numOfUniqueHostsWithDefaultWeight = 2;
        TestContext context = setupContextWithNHostsInDefaultFlavor(appName, numOfUniqueHostsWithDefaultWeight, true);
        IntegrationTestHelper helper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        int customWeight = 10;
        helper.getDataStore().registerNewDynamicHost(new TestContext.Host(DC, STACK, getDefaultFlavorForApp(appName), appName, "ipv4_3", "ipv6_3", Integer.toString(customWeight)));

        List<String> ipAddresses = new ArrayList<>();

        int totalUniqueHosts = numOfUniqueHostsWithDefaultWeight + 1;
        int numberOfVerificationCycles = 20;
        int numberOfRedirectRequests = numberOfVerificationCycles * (config.getDefaultWeightOfTheNode() * numOfUniqueHostsWithDefaultWeight + customWeight);
        for (int i = 0; i < numberOfRedirectRequests; i++) {
            InstanceInfo result = helper.getRedirectorSupport().redirect();
            ipAddresses.add(result.getServerIp());
        }

        verifyWeights(totalUniqueHosts, ipAddresses);

        Map<String, Long> redirectsPerIp = ipAddresses.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> trafficDistributionPerIp = redirectsPerIp.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Math.round((entry.getValue().doubleValue() / numberOfRedirectRequests) * 100)));
        log.info("traffic distributions per ip is {}", trafficDistributionPerIp);
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_1", 20, 35));
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_2", 20, 35));
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_3", 40, 60));
    }

    @Test(timeout = 120000)
    public void dynamicAppsStartWorkingFromBackup_AndUpdateFromDataStore_OnceDataStoreComesUp() throws Exception {
        httpServer.shutdownNow();
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC);
        IntegrationTestHelper backupBasedHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();

        verifyRedirect(backupBasedHelper);
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.Profiles.OFFLINE_DYNAMIC_APP.getName());
        httpServer.start();

        String newDefaultFlavor = "NEW-FLAVOR-DYNAMIC-UPDATE-FROM-DS";
        TestContext context = contextBuilder
                                .withDefaultServer().flavor(newDefaultFlavor)
                                .withWhitelist("/newdc/newstack")
                                .withHosts()
                                    .flavor(newDefaultFlavor).ipv4("ipv4").ipv6("ipv6").stack("/newdc/newstack").currentApp()
                                .build();
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();

        int discoveryUpdateWaitTime = (config.getDiscoveryUpdatePeriodInSeconds() + 1) * 1000;
        Thread.sleep(discoveryUpdateWaitTime); // TODO: get rid of thread sleep

        verifyRedirect(dataSourceBasedHelper);

        dataSourceBasedHelper.getDataStore().deRegisterDynamicHost(new TestContext.Host("newdc", "newstack", newDefaultFlavor, DYNAMIC_APP, "ipv4", "ipv6"));
        dataSourceBasedHelper.getDataStore().registerNewDynamicHost(new TestContext.Host("newdc", "newstack", newDefaultFlavor, DYNAMIC_APP, "55.55.55.55", "ff01::41"));

        InstanceInfo firstRedirectAttempt = dataSourceBasedHelper.getRedirectorSupport().redirect();
        Assert.assertEquals("55.55.55.55", firstRedirectAttempt.getAddress());
        InstanceInfo secondRedirectAttempt = dataSourceBasedHelper.getRedirectorSupport().redirect();
        Assert.assertEquals("55.55.55.55", secondRedirectAttempt.getAddress());
    }

    @Test
    // TODO: figure out if such behavior is a valid scenario. It could be a bug and behavior could be opposite
    public void stacksOfDynamicAppsAre__NOT__SavedIntoDynamicBackups_WhenBackupIsMissingInDynamicBackups_ForParticularApp_ButPresentInManualBackups_And_DynamicBackupIsPresentForAnotherApp() throws Exception {
        TestContext context = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC)
            .withHosts()
                .stack("/otherdc/otherzone").flavor("otherflavor").ipv4("ipv4").ipv6("ipv6").app("otherapp")
                .stack("/anotherdc/anotherzone").flavor("anotherflavor").ipv4("ipv4").ipv6("ipv6").app("anotherapp")
            .build();

        IntegrationTestHelper helper = setupEnvForBackupBasedRedirector(context);

        IntegrationTestHelper.Backups backupSupport = helper.getBackups();
        StackBackup backup = backupSupport.getDynamicHosts();
        verifyBackupHasHostsAndFlavors(backup,
            "otherflavor", "anotherflavor"
        );
        verifyBackupHasNoHostsAndFlavors(backup,
            context.getFlavorRule().getFlavor(),
            context.getDefaultServer().getFlavor(),
            context.getDistribution().getFirstRuleFlavor());
    }

    private IntegrationTestHelper setupEnvForBackupBasedRedirector(TestContext context) throws Exception {
        IntegrationTestHelper backupBasedHelper = prepareTestHelperForInitRedirectorModel(context);

        IntegrationTestHelper.Backups backupSupport = backupBasedHelper.getBackups();
        backupSupport.forceBackupHostsStatic(host -> Objects.equals(host.getAppName(), context.getAppName()));
        backupSupport.forceBackupHostsDynamic(host -> ! Objects.equals(host.getAppName(), context.getAppName()));
        backupSupport.setupModelBackupForContext();

        redirectorEngine.startLookingForAppsChanges();

        backupBasedHelper.getRedirectorSupport().initRedirector(1 /* we are not starting DataSource in this test at all. So no need to wait for it */);

        return backupBasedHelper;
    }

    // TODO: test of statistics is updated

    private void verifyBackupHasHostsAndFlavors(StackBackup backup, String... flavors) {
        Set<String> expectedFlavors = Stream.of(flavors).collect(Collectors.toSet());
        Set<String> actualFlavors = backup.getAllStacks().stream().map(StackData::getFlavor).collect(Collectors.toSet());
        Assert.assertTrue(actualFlavors.containsAll(expectedFlavors));
    }

    private void verifyBackupHasNoHostsAndFlavors(StackBackup backup, String... flavors) {
        Set<String> unExpectedFlavors = Stream.of(flavors).collect(Collectors.toSet());
        Set<String> actualFlavors = backup.getAllStacks().stream().map(StackData::getFlavor).collect(Collectors.toSet());
        log.info("actual flavors: {}, flavors that should not appear in backup: {}", actualFlavors, unExpectedFlavors);
        actualFlavors.retainAll(unExpectedFlavors);
        Assert.assertEquals(0, actualFlavors.size());
    }
}
