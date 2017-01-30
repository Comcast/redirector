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

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.applications.Applications;
import com.comcast.redirector.core.applications.IApplicationsManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.redirector.core.spring.AppScope;
import com.comcast.redirector.core.spring.AppsContextHolder;
import com.comcast.redirector.core.spring.IntegrationTestBackupBeans;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import it.IntegrationTestConstants;
import it.context.ContextBuilder;
import it.context.ContextBuilderMain;
import it.context.TestContext;
import it.helper.DataStoreSupport;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static it.context.ContextBuilderUtils.*;
import static it.context.Operations.EQUALS;

public class BaseOfflineIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(BaseOfflineIntegrationTest.class);

    protected static final String DC = "offlinemode";
    protected static final String STACK = "stack";
    protected static final String NAMESPACED_LIST = "NAMESPACED_LIST";
    protected static final String NAMESPACED_LIST_VALUE = "NAMESPACED_LIST_VALUE";
    protected static final String WHITELISTED_STACK = DELIMETER + DC + DELIMETER + STACK;

    protected static final String STATIC_APP = "offlineTestStaticApp";
    protected static final String DYNAMIC_APP = "offlineTestDynamicApp";

    enum Mode {STATIC, DYNAMIC}

    protected ZKConfig config;
    protected IDynamicAppsAwareRedirectorFactory redirectorEngine;
    protected IntegrationTestChangeListener<String> integrationTestModelInitListener;
    protected IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    protected IntegrationTestChangeListener<String> modelFailsInitListener;
    protected IntegrationTestChangeListener<NamespacedListsBatch> newBatchAppliedListener;
    protected IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;
    protected ApplicationContext applicationContext;
    protected IBackupManagerFactory globalBackupManagerFactory;
    protected CuratorFramework curatorFramework;
    protected DataStoreSupport dataStoreSupport;
    protected HttpServer httpServer;

    protected void init(String profile) throws Exception {
        applicationContext = createApplicationContext(profile);
        globalBackupManagerFactory = (IBackupManagerFactory)applicationContext.getBean("globalBackupManagerFactory");
        integrationTestModelRefreshListener = (IntegrationTestChangeListener<String>)applicationContext.getBean("integrationTestModelRefreshListener");
        integrationTestModelInitListener = (IntegrationTestChangeListener<String>)applicationContext.getBean("integrationTestModelInitListener");
        modelFailsInitListener = (IntegrationTestChangeListener<String>)applicationContext.getBean("modelFailsInitListener");
        newBatchAppliedListener = (IntegrationTestChangeListener<NamespacedListsBatch>)applicationContext.getBean("newBatchAppliedListener");
        integrationTestStacksChangeListener = (IntegrationTestChangeListener<StackSnapshot>)applicationContext.getBean("integrationTestChangeListener");
        redirectorEngine = applicationContext.getBean(IDynamicAppsAwareRedirectorFactory.class);
        config = applicationContext.getBean(ZKConfig.class);
        curatorFramework = applicationContext.getBean(CuratorFramework.class);

        dataStoreSupport = new DataStoreSupport(config);

        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath(), profile);
    }

    private ApplicationContext createApplicationContext(String profile) {
        AnnotationConfigApplicationContext  context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(profile);
        context.register(IntegrationTestConstants.CONFIGS);
        context.refresh();

        return context;
    }

    @After
    public void destroy() throws Exception {
        new Shutdown().run();
        ((ConfigurableApplicationContext)applicationContext).close();
        httpServer.shutdownNow();
    }

    static void verifyRedirect(IntegrationTestHelper testHelper) throws InterruptedException {
        InstanceInfo result = testHelper.getRedirectorSupport().redirect();

        Assert.assertEquals(testHelper.getContext().getDefaultServer().getFlavor(), result.getFlavor());
    }

    static void verifyRedirectAfterPause(IntegrationTestHelper testHelper, int pauseInMilliSeconds) throws InterruptedException {
        Thread.sleep(pauseInMilliSeconds);

        InstanceInfo result = testHelper.getRedirectorSupport().redirect();

        Assert.assertEquals(testHelper.getContext().getDefaultServer().getFlavor(), result.getFlavor());
    }

    static void verifyWeights(int numOfUniqueHosts, List<String> ipAddresses) {
        Assert.assertEquals(numOfUniqueHosts, ipAddresses.stream().distinct().count());

        boolean redirectsAreReturnedInRoundRobinOrder = true;
        for (int i = 1; i < ipAddresses.size(); i++) {
            if (ipAddresses.get(i).equals(ipAddresses.get(i - 1))) {
                redirectsAreReturnedInRoundRobinOrder = false;
                break;
            }
        }

        Assert.assertFalse(redirectsAreReturnedInRoundRobinOrder);
    }

    static boolean trafficDistributionFitsBounds(Map<String, Long> trafficDistributionPerIp, String ip, int minPercent, int maxPercent) {
        return trafficDistributionPerIp.get(ip) >= minPercent && trafficDistributionPerIp.get(ip) <= maxPercent;
    }

    protected final TestContext setupContextWithNHostsInDefaultFlavor(String appName, int numberOfHosts, boolean dynamic) {
        ContextBuilderMain builder = new ContextBuilder().forApp(appName).dynamic(dynamic)
            .withFlavorRule().left("A").operation(EQUALS).right("B").flavor(getRuleFlavorForApp(appName))
            .withDefaultServer().flavor(getDefaultFlavorForApp(appName))
            .withDistribution().percent("50.00").flavor(getDistributionFlavorForApp(appName))
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist(WHITELISTED_STACK);

        IntStream.range(1, numberOfHosts + 1)
            .forEach(i -> builder.withHosts().stack(WHITELISTED_STACK).defaultFlavor().ipv4("ipv4_" + i).ipv6("ipv6_" + i).currentApp());

        return builder.withNamespacedList(NAMESPACED_LIST, NAMESPACED_LIST_VALUE).build();
    }

    protected IBackupManagerFactory getBackupManagerFactory(String app) {
        return applicationContext.getBean(IntegrationTestBackupBeans.BackupManagerFactoryWrapper.class, app, config).getValue();
    }

    protected ContextBuilderMain setupContextBuilderForApp(String appName, Mode mode) {
        return setupContextBuilderForApp(appName, Mode.DYNAMIC == mode);
    }

    private ContextBuilderMain setupContextBuilderForApp(String appName, boolean dynamic) {
        return new ContextBuilder().forApp(appName).dynamic(dynamic)
            .withFlavorRule().left("A").operation(EQUALS).right("B").flavor(getRuleFlavorForApp(appName))
            .withDefaultServer().flavor(getDefaultFlavorForApp(appName))
            .withDistribution().percent("50.00").flavor(getDistributionFlavorForApp(appName))
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist(WHITELISTED_STACK)
            .withHosts()
                .stack(WHITELISTED_STACK).flavorRuleFlavor().ipv4("10.0.0.1").ipv6("ff01::41").currentApp()
                .stack(WHITELISTED_STACK).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
                .stack(WHITELISTED_STACK).distributionFlavor().ipv4("10.0.0.4").ipv6("ff01::44").currentApp()
            .withNamespacedList(NAMESPACED_LIST, NAMESPACED_LIST_VALUE)
            .withApplications(appName);
    }

    protected final IntegrationTestHelper.DataSourceBasedIntegrationHelperBuilder setupDataSourceBasedTestHelper(TestContext context) throws Exception {
        IntegrationTestHelper.Builder builder = setupTestHelperBuilder(context);
        builder.integrationTestModelRefreshListener(integrationTestModelRefreshListener);
        return builder.buildDataSourceBasedIntegrationHelper();
    }

    protected final IntegrationTestHelper.BackupBasedIntegrationHelperBuilder setupBackupBasedTestHelper(TestContext context) throws Exception {
        IntegrationTestHelper.Builder builder = setupTestHelperBuilder(context);
        builder.integrationTestModelRefreshListener(integrationTestModelInitListener);
        return builder.buildBackupBasedIntegrationHelper();
    }

    protected final IntegrationTestHelper.BackupBasedFailingIntegrationHelperBuilder setupFailingBackupBasedTestHelper(TestContext context) throws Exception {
        IntegrationTestHelper.Builder builder = setupTestHelperBuilder(context);
        builder.integrationTestModelRefreshListener(integrationTestModelInitListener);
        return builder.buildBackupBasedFailingIntegrationHelper();
    }

    protected final IntegrationTestHelper prepareTestHelperForInitRedirectorModel(TestContext context) throws Exception {
        return setupTestHelperBuilder(context)
            .integrationTestModelRefreshListener(integrationTestModelInitListener)
            .build();
    }

    protected final IntegrationTestHelper prepareTestHelperForUpdateRedirectorModel(TestContext context) throws Exception {
        return setupTestHelperBuilder(context)
            .integrationTestModelRefreshListener(integrationTestModelRefreshListener)
            .build();
    }

    private IntegrationTestHelper.Builder setupTestHelperBuilder(TestContext context) throws Exception {
        return new IntegrationTestHelper.Builder()
                .config(config).redirectorEngine(redirectorEngine).globalBackupManagerFactory(globalBackupManagerFactory)
                .context(context)
                .dataStoreSupport(dataStoreSupport)
                .backupManagerFactory(getBackupManagerFactory(context.getAppName()))
                .modelFailsInitListener(modelFailsInitListener)
                .integrationTestStacksChangeListener(integrationTestStacksChangeListener);
    }

    protected void verifyBackupsForApps(Applications backup, String ... apps) {
        Set<String> expectedApps = Stream.of(apps).collect(Collectors.toSet());
        Assert.assertTrue(backup.getApps().containsAll(expectedApps));
    }

    private class Shutdown {
        void run() {
            closeCurator();
            closeComponents();
            applicationContext.getBean(AppScope.class).getApps().stream()
                .filter(app -> !AppsContextHolder.GLOBAL_APP.equals(app))
                .forEach(this::closeRedirector);
        }

        void closeComponents() {
            try {
                applicationContext.getBean(IApplicationsManager.class).close();
            } catch (IOException e) {
                log.error("Failed to close components", e);
            }
        }

        void closeCurator() {
            if (curatorFramework.getState() == CuratorFrameworkState.STARTED) {
                log.info("Closing curator");
                curatorFramework.close();
            }
            dataStoreSupport.shutdown();
        }

        void closeRedirector(String app) {
            try {
                IRedirector redirector = redirectorEngine.createRedirector(app);
                if (redirector != null)
                    redirector.close();
            } catch (IOException e) {
                log.error("Failed to close redirector for app=" + app, e);
            }
        }
    }
}
