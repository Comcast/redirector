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
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.*;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.core.spring.configurations.common.ModelBeans;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.WebServiceClientHelper;
import it.helper.IntegrationTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.common.function.Wrappers.unchecked;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        CommonBeans.class, BackupBeans.class, ModelBeans.class,
        IntegrationTestBeans.class, FileSystemIntegrationTestBackupBeans.class, IntegrationTestApplicationsBeans.class
    }
)
@DirtiesContext
@Ignore
public class BackupFromFileSystemWithoutDataSourceIntegrationTest {
    private String currentPath;

    @Autowired
    private ZKConfig config;
    @Autowired
    private IDynamicAppsAwareRedirectorFactory redirectorEngine;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IntegrationTestChangeListener<String> integrationTestModelInitListener;
    @Autowired
    @Qualifier("globalBackupManagerFactory")
    private IBackupManagerFactory globalBackupManagerFactory;

    private HttpServer httpServer;

    @Before
    public void before () throws Exception {
        currentPath = config.getBackupBasePath();

        httpServer = WebServiceClientHelper.httpBuilder(config.getRestBasePath());
        httpServer.start();

        deleteBackups();
    }

    @After
    public void after () {
        httpServer.shutdownNow();
    }

    private void deleteBackups() throws Exception {
        if (Files.exists(Paths.get(currentPath))) {
            Files.walk(Paths.get(currentPath)).sorted((path1, path2) -> path2.getNameCount() - path1.getNameCount())
                .forEach(unchecked(Files::delete));
        }
    }

    @Test(timeout = 20000)
    public void stacksOfDynamicAppsAreSavedIntoDynamicBackups_WhenBackupIsAbsentInDynamicBackupsButPresentInManualBackups() throws Exception {
        String appName = "dynamicapp";
        String whitelistedStack = DELIMETER + "dc" + DELIMETER + "stack";
        Serializer serializer = new JsonSerializer();


        TestContext context =  new ContextBuilder().forApp(appName).dynamic()
            .withDistribution().percent("50.0").flavor("any")
            .withDefaultServer().flavor("DEFAULT_FLAVOR")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv("4")
            .withWhitelist(whitelistedStack)
            .withNamespacedList("stub", "stub")
            .withHosts()
                .stack(whitelistedStack).defaultFlavor().ipv4("10.0.0.2").ipv6("ff01::42").currentApp()
            .withApplications("dynamicapp", "otherapp")
            .build();

        IntegrationTestHelper helper = new IntegrationTestHelper.Builder()
            .config(config).redirectorEngine(redirectorEngine)
            .backupManagerFactory(getBackupManagerFactory(appName))
            .globalBackupManagerFactory(globalBackupManagerFactory)
            .integrationTestModelRefreshListener(integrationTestModelInitListener)
            .context(context)
            .build();

        deleteBackups();

        helper.getBackups().setupBackupEnvForContext(true /* force backup hosts into static backups (manualbackups.json) */);
        helper.getRedirectorSupport().initRedirector(10);

        Assert.assertTrue("Dynamic stacks backup should be present", Files.exists(Paths.get(currentPath + File.separator + "stacks.json")));

        StackBackup backup = serializer.deserialize(new String(Files.readAllBytes(Paths.get(currentPath + File.separator + "stacks.json"))), StackBackup.class);
        Set<String> actualFlavors = backup.getAllStacks().stream().map(StackData::getFlavor).collect(Collectors.toSet());
        Assert.assertTrue(actualFlavors.contains("DEFAULT_FLAVOR"));
    }

    private IBackupManagerFactory getBackupManagerFactory(String app) {
        return applicationContext.getBean(IntegrationTestBackupBeans.BackupManagerFactoryWrapper.class, app, config).getValue();
    }
}
