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

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.applications.Applications;
import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilder;
import it.context.ContextBuilderMain;
import it.context.TestContext;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.context.ContextBuilderUtils.getDefaultFlavorForApp;
import static it.context.ContextBuilderUtils.getDistributionFlavorForApp;

public class StaticAppWorkWithBackupsDataStoreIntegrationTest extends BaseOfflineIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(StaticAppWorkWithBackupsDataStoreIntegrationTest.class);


    @Before
    public void init() throws Exception {
        super.init(IntegrationTestConfigBeans.Profiles.OFFLINE_STATIC_APP.getName());

        makeAppBeConsideredStatic(STATIC_APP);

        ((Config)config).setAppsRetrievingIntervalInSeconds(1);
        ((Config)config).setDiscoveryUpdatePeriod(1);
        httpServer.start();
    }

    @Test
    public void modelIsInitializedFromBackupAndAfterSomeTimeRefreshedFromDataStore() throws Exception {
        httpServer.shutdownNow();
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(STATIC_APP, Mode.STATIC);
        TestContext backupContext = contextBuilder.build();
        IntegrationTestHelper backupBasedHelper = setupBackupBasedTestHelper(backupContext).setupEnvironmentAndModel();

        verifyRedirect(backupBasedHelper);
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.Profiles.OFFLINE_STATIC_APP.getName());
        httpServer.start();

        TestContext dataSourceContext = contextBuilder
            .withDefaultServer().flavor("NEW-FLAVOR-FOR-STATIC-APP-REFRESHED-FROM-DS")
            .withHosts()
                .stack(WHITELISTED_STACK).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
            .build();
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(dataSourceContext).startDataStoreAndSetupModel();

        verifyRedirect(dataSourceBasedHelper);
    }

    @Test
    public void modelIsNotComingUp_BecauseOfIncorrectBackup_ButComesUp_WhenValidDataComesFromDataStore() throws Exception {
        ContextBuilderMain failingContextBuilder = contextBuilderWithoutFlavorRulesAndDefaultAndDistribution(STATIC_APP);
        TestContext failingContext = failingContextBuilder.build();
        IntegrationTestHelper backupBasedHelper = setupFailingBackupBasedTestHelper(failingContext).initRedirectorWithFailure();

        Assert.assertNull(backupBasedHelper.getRedirectorSupport().redirect());

        TestContext redirectableContext = appendDefaultAndDistributionToContextBuilder(failingContextBuilder, STATIC_APP).build();
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(redirectableContext).startDataStoreAndSetupModel();

        verifyRedirect(dataSourceBasedHelper);
    }

    @Test
    public void backupsAreUsed_WhenConnectedToDataSource_ButBackupsVersionIsHigherOrEqualToModelVersionInDataStore() throws Exception {
        httpServer.shutdownNow();
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(STATIC_APP, Mode.STATIC).withVersion(1);
        IntegrationTestHelper backupTestHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();
        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), IntegrationTestConfigBeans.Profiles.OFFLINE_STATIC_APP.getName());
        httpServer.start();

        verifyRedirect(backupTestHelper);

        TestContext dataSourceContext = contextBuilder
            .withDefaultServer().flavor("NEW-FLAVOR-STATIC-APP-BACKUP-HAS-HIGHER-VERSION")
            .withHosts()
                .stack(WHITELISTED_STACK).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
            .build();
        IntegrationTestHelper dataSourceTestHelper = setupDataSourceBasedTestHelper(dataSourceContext).startDataStoreAndSetupModel();

        verifyRedirect(backupTestHelper);

        dataSourceTestHelper.getRedirectorSupport().triggerModelUpdate();
        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);
        verifyRedirect(dataSourceTestHelper);
    }

    @Test
    public void backupsAreUpdated_WhenNewModelComesFromDataSource() throws Exception {
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(STATIC_APP, Mode.STATIC);
        IntegrationTestHelper testHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();

        SelectServer flavorRules = testHelper.getBackups().getFlavorRules();
        Whitelisted whitelist = testHelper.getBackups().getWhitelist();

        // Check correctness of initial backups
        Assert.assertEquals(getDefaultFlavorForApp(STATIC_APP), flavorRules.getDistribution().getDefaultServer().getPath());
        Assert.assertTrue(whitelist.getPaths().stream().anyMatch(WHITELISTED_STACK::equals));

        String newDefaultFlavor = "NEW-FLAVOR-IN-BACKUPS";
        String newWhitelistedStack = "/DC-NEW/STACK-NEW";
        TestContext dataSourceContext = contextBuilder
            .withDefaultServer().flavor(newDefaultFlavor)
            .withWhitelist(WHITELISTED_STACK, newWhitelistedStack)
            .withHosts()
                .stack(WHITELISTED_STACK).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
            .build();

        testHelper = setupDataSourceBasedTestHelper(dataSourceContext).startDataStoreAndSetupModel();

        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);
        // check correctness of backups updated after model came from ZK
        SelectServer newFlavorRules = testHelper.getBackups().getFlavorRules();
        Whitelisted newWhitelist = testHelper.getBackups().getWhitelist();

        Assert.assertEquals(newDefaultFlavor, newFlavorRules.getDistribution().getDefaultServer().getPath());
        Assert.assertTrue(newWhitelist.getPaths().stream().anyMatch(newWhitelistedStack::equals));
    }

    @Test(timeout = 60000)
    public void backupOfApplicationsIsUpdated_WhenNewAppIsRegisteredWithModel() throws Exception {
        ContextBuilderMain contextBuilder = setupContextBuilderForApp(STATIC_APP, Mode.STATIC);
        IntegrationTestHelper dataSourceBasedHelper = setupDataSourceBasedTestHelper(contextBuilder.build()).startDataStoreAndSetupModel();

        Thread.sleep(config.getAppsRetrievingIntervalInSeconds() * 1000);
        Applications backup = dataSourceBasedHelper.getBackups().getApplications();

        verifyBackupsForApps(backup, STATIC_APP);
    }

    @Test
    public void defaultWeightIsApplied_WhenNoWeightInHostIsDefined() throws Exception {
        String appName = "defaultWeightTestStaticApp";
        makeAppBeConsideredStatic(appName);
        int numOfUniqueHosts = 2;
        TestContext context = setupContextWithNHostsInDefaultFlavor(appName, numOfUniqueHosts, false); // TODO: reduce number of parameters
        IntegrationTestHelper helper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();

        List<String> ipAddresses = new ArrayList<>();

        for (int i = 0; i < config.getDefaultWeightOfTheNode() * numOfUniqueHosts; i++) {
            InstanceInfo result = helper.getRedirectorSupport().redirect();
            ipAddresses.add(result.getServerIp());
        }

        verifyWeights(numOfUniqueHosts, ipAddresses);
    }

    @Test
    public void customWeightIsApplied_WhenHostIsRegisteredWithWeight() throws Exception {
        String appName = "customWeightTestStaticApp";
        makeAppBeConsideredStatic(appName);
        int numOfUniqueHostsWithDefaultWeight = 2;
        TestContext context = setupContextWithNHostsInDefaultFlavor(appName, numOfUniqueHostsWithDefaultWeight, false);
        IntegrationTestHelper helper = setupDataSourceBasedTestHelper(context).startDataStoreAndSetupModel();
        int customWeight = 10;
        helper.getDataStore().registerNewStaticHost(new TestContext.Host(DC, STACK, getDefaultFlavorForApp(appName), appName, "ipv4_3", "ipv6_3", Integer.toString(customWeight)));
        helper.getRedirectorSupport().triggerModelUpdate();

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
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_1", 20, 30));
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_2", 20, 30));
        Assert.assertTrue(trafficDistributionFitsBounds(trafficDistributionPerIp, "ipv4_3", 40, 60));
    }

    private ContextBuilderMain contextBuilderWithoutFlavorRulesAndDefaultAndDistribution(String app) {
        return new ContextBuilder().forApp(app)
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist(WHITELISTED_STACK)
            .withNamespacedList(NAMESPACED_LIST, NAMESPACED_LIST_VALUE)
            .withApplications(app);
    }

    private ContextBuilderMain appendDefaultAndDistributionToContextBuilder(ContextBuilderMain builder, String app) {
        return builder
            .withDefaultServer().flavor(getDefaultFlavorForApp(app))
            .withDistribution().percent("50.00").flavor(getDistributionFlavorForApp(app)) // TODO: simplify validator. We don't really need distribution for valid model
            .withHosts()
                .stack(WHITELISTED_STACK).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp();
    }

    private void makeAppBeConsideredStatic(String appName) {
        ((Config)config).setExcludedAppsForStackAutoDiscovery(STATIC_APP + "," + appName);
    }
}
