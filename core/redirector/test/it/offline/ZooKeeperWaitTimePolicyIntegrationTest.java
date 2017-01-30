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
 */
package it.offline;

import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilderMain;
import it.context.TestNamespacedList;
import it.helper.IntegrationTestHelper;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.junit.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static com.comcast.redirector.common.function.Wrappers.unchecked;

@Ignore
public class ZooKeeperWaitTimePolicyIntegrationTest extends BaseOfflineIntegrationTest {

    private static final String NAMESPACEDLIST = "NAMESPACEDLIST";
    private static final int TICK_TIME = 100;
    private static final int MAX_CONNECTION = 2;
    private static final int MAX_WAIT_TIME = 2000;

    private ContextBuilderMain contextBuilder;

    private TestingServer zookeeperServer;
    private String currentPath;

    @Before
    public void setUp() throws Exception {
        super.init(IntegrationTestConfigBeans.Profiles.CURATOR_RETRY.getName());
        createZookeeperServer(IntegrationTestConfigBeans.Profiles.CURATOR_RETRY.getDataSourceConnectionPort(), TICK_TIME, MAX_CONNECTION);

        ((Config) config).setExcludedAppsForStackAutoDiscovery(NAMESPACEDLIST);
        contextBuilder = setupContextBuilderForApp(NAMESPACEDLIST, BaseOfflineIntegrationTest.Mode.STATIC);
        currentPath = config.getBackupBasePath();
    }

    @After
    public void cleanUp() throws Exception {
        deleteBackups();
        super.destroy();
    }

    @Test
    public void checkIsNamespacedListBackupAppiedAfterReconnectedZookeeperTest() throws Exception {
        new ZooKeeperWaitTimePolicySteps(IntegrationTestConfigBeans.Profiles.CURATOR_RETRY.getName())
                .setupEnvironmentAndModel()
                .startAndWaitZookeper()
                .stopAndWaitZookeper()
                .restartAndWaitZookeper()
                .deleteBackupFilesAfterStopZooKeeper()
                .verifyBackupFilesAreNotPresent()
                .updateNamespacedListAndWait()
                .verifyBackupFilesArePresent();
    }

    private void deleteBackups() throws Exception {
        if (Files.exists(Paths.get(currentPath))) {
            Files.walk(Paths.get(currentPath)).sorted((path1, path2) -> path2.getNameCount() - path1.getNameCount())
                    .forEach(unchecked(Files::delete));
        }
    }

    private void createZookeeperServer(int port, int tickTime, int maxConnections) throws Exception {
        InstanceSpec spec = new InstanceSpec(null, port, -1, -1, true, -1, tickTime, maxConnections);
        this.zookeeperServer = new TestingServer(spec, false);
    }

    private class ZooKeeperWaitTimePolicySteps {
        private IntegrationTestHelper testHelper;
        private String serviceName;

        ZooKeeperWaitTimePolicySteps(String serviceName) {
            this.serviceName = serviceName;
        }

        ZooKeeperWaitTimePolicySteps setupEnvironmentAndModel() throws Exception {
            testHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();
            return this;
        }

        ZooKeeperWaitTimePolicySteps startAndWaitZookeper() throws Exception {
            zookeeperServer.start();
            dataStoreSupport.getStartedConnector();
            waitForDataSourceStart(MAX_WAIT_TIME);
            return this;
        }

        ZooKeeperWaitTimePolicySteps stopAndWaitZookeper() throws Exception {
            zookeeperServer.stop();
            waitForDataSourceStop(MAX_WAIT_TIME);
            return this;
        }

        ZooKeeperWaitTimePolicySteps deleteBackupFilesAfterStopZooKeeper() throws Exception {
            deleteBackups();
            return this;
        }

        ZooKeeperWaitTimePolicySteps restartAndWaitZookeper() throws Exception {
            zookeeperServer.restart();
            waitForDataSourceStart(MAX_WAIT_TIME);
            return this;
        }

        ZooKeeperWaitTimePolicySteps verifyBackupFilesAreNotPresent() throws Exception {
            Assert.assertFalse(Files.exists(Paths.get(currentPath + File.separator + "namespacedlists.json")));
            return this;
        }

        ZooKeeperWaitTimePolicySteps updateNamespacedListAndWait() throws Exception {
            testHelper.getContext().setNamespacedLists(Collections.singletonList(new TestNamespacedList(NAMESPACED_LIST, "new value")));
            testHelper.getDataStore().putNamespacedList();
            return this;
        }

        ZooKeeperWaitTimePolicySteps verifyBackupFilesArePresent() throws Exception {
            Path backupFilename = Paths.get(currentPath + File.separator + "namespacedlists.json");
            waitForFileSystem(backupFilename, MAX_WAIT_TIME);
            Assert.assertTrue(Files.exists(Paths.get(currentPath + File.separator + "namespacedlists.json")));
            return this;
        }

        private void  waitForDataSourceStart (int waitTime) throws InterruptedException {
            long endTime = System.currentTimeMillis() + waitTime;
            while(!dataStoreSupport.isConnectorConnected()
                    || endTime < System.currentTimeMillis()) {
                Thread.sleep(TICK_TIME);
            }
        }

        private void  waitForDataSourceStop (int waitTime) throws InterruptedException {
            long endTime = System.currentTimeMillis() + waitTime;
            while(dataStoreSupport.isConnectorConnected()
                    || endTime < System.currentTimeMillis()) {
                Thread.sleep(TICK_TIME);
            }
        }

        private void  waitForFileSystem (Path backupFilename, int waitTime) throws InterruptedException {
            long endTime = System.currentTimeMillis() + MAX_WAIT_TIME;
            while(!Files.exists(backupFilename) || endTime < System.currentTimeMillis()) {
                Thread.sleep(TICK_TIME);
            }
        }
    }
}
