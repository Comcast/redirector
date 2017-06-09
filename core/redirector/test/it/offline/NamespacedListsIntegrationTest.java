/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
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

import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilderMain;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.comcast.redirector.common.function.Wrappers.unchecked;

public class NamespacedListsIntegrationTest extends BaseOfflineIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(NamespacedListsIntegrationTest.class);

    private static final String NS_LIST_STATIC_APP = "NS_LIST_STATIC_APP";

    private ContextBuilderMain contextBuilder;


    @Before
    public void init() throws Exception {
        super.init(IntegrationTestConfigBeans.Profiles.OFFLINE_NAMESPACED_LISTS.getName());
        ((Config)config).setExcludedAppsForStackAutoDiscovery(NS_LIST_STATIC_APP);
        contextBuilder = setupContextBuilderForApp(NS_LIST_STATIC_APP, Mode.STATIC);
        httpServer.start();
    }

    @Test(timeout = 60000)
    public void namespacedListBackupIsUpdated_EachTime_NamespacedListIsChanged_InDataStore() throws Exception {
        initNamespacedListInBackup();
        backupsAreUpdatedAfterDataSourceComesUp();
        backupsAreUpdatedAfterNamespacedListIsChangedInDataStore();
    }

    @Test(timeout = 80000)
    public void namespacedLists_AreNotSavedInBackup_WhenDateStoreVersion_IsLessOrEqualToBackupVersion() throws Exception {
        TestContext dataStoreContext = contextBuilder
            .withNamespacedList(NAMESPACED_LIST, NAMESPACED_LIST_VALUE)
            .build();
        IntegrationTestHelper dataStoreHelper = setupDataSourceBasedTestHelper(dataStoreContext).startDataStoreAndSetupModel();
        TimeUnit.SECONDS.sleep(2);

        NamespacedListsBatch batch = new NamespacedListsBatch();
        batch.addValues(NAMESPACED_LIST, Collections.singleton("fromBackup"));
        dataStoreHelper.getBackups().backupNamespacedList(batch);
        dataStoreHelper.getRedirectorSupport().triggerModelUpdate();

        NamespacedListsBatch namespacedListsFromBackup = dataStoreHelper.getBackups().getNamespacedLists();
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get(NAMESPACED_LIST).contains("fromBackup"));

        dataStoreHelper.getDataStore().bumpNamespacedListVersion();
        dataStoreHelper.getRedirectorSupport().triggerModelUpdate();

        namespacedListsFromBackup = dataStoreHelper.getBackups().getNamespacedLists();
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get(NAMESPACED_LIST).contains(NAMESPACED_LIST_VALUE));
    }

    private void initNamespacedListInBackup() throws Exception {
        IntegrationTestHelper testHelper = setupBackupBasedTestHelper(contextBuilder.build()).setupEnvironmentAndModel();
        NamespacedListsBatch namespacedListsFromBackup = testHelper.getBackups().getNamespacedLists();
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get(NAMESPACED_LIST).contains(NAMESPACED_LIST_VALUE));
    }

    private void backupsAreUpdatedAfterDataSourceComesUp() throws Exception {
        TestContext context = contextBuilder.withNamespacedList(NAMESPACED_LIST, "new value").build();
        IntegrationTestHelper testHelper = prepareTestHelperForInitRedirectorModel(context);
        Lock lock = new ReentrantLock();
        Condition listUpdated = lock.newCondition();

        newBatchAppliedListener.setCallback(event -> {
            log.info("Namespaced Lists updated after zookeeper came up");
            lock.lock();
            try {
                listUpdated.signalAll();
            } finally {
                lock.unlock();
            }
        });

        dataStoreSupport.startZookeeper();
        testHelper.getDataStore().setupEnvForContext();
        lock.lock();
        try {
            listUpdated.await(3, TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
        log.info("Unlocked test. Keep going");
        NamespacedListsBatch namespacedListsFromBackup = testHelper.getBackups().getNamespacedLists();
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get(NAMESPACED_LIST).contains("new value"));
        Assert.assertFalse(namespacedListsFromBackup.getNamespacedLists().get(NAMESPACED_LIST).contains(NAMESPACED_LIST_VALUE));
    }

    private void backupsAreUpdatedAfterNamespacedListIsChangedInDataStore() throws Exception {
        TestContext context = contextBuilder.withNamespacedList("NewList", "value1", "value2").build();
        IntegrationTestHelper testHelper = prepareTestHelperForUpdateRedirectorModel(context);
        Lock lock = new ReentrantLock();
        Condition listUpdated = lock.newCondition();

        newBatchAppliedListener.setCallback(unchecked(event -> {
            log.info("New namespaced list is applied: {}", event.getData().getNamespacedLists());
            if (event.getData() != null && event.getData().getNamespacedLists() != null) {
                if (event.getData().getNamespacedLists().containsKey("NewList")) {
                    log.info("Event for update of new namespaced list happened. Continue processing");
                } else {
                    log.info("Skip this event");
                    return;
                }
            }

            lock.lock();
            try {
                listUpdated.signalAll();
            } finally {
                lock.unlock();
            }
        }));

        lock.lock();

        try {
            testHelper.getDataStore().putNamespacedList();
            if (listUpdated.await(1, TimeUnit.SECONDS)) {
                log.info("Namespaced list is applied. Going to verification");
            } else {
                log.info("Timeout exceed. Unlocking test");
            }
        } finally {
            lock.unlock();
        }

        NamespacedListsBatch namespacedListsFromBackup = testHelper.getBackups().getNamespacedLists();
        log.info("Got namespaced list from backup {}", namespacedListsFromBackup.getNamespacedLists());
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get("NewList").contains("value1"));
        Assert.assertTrue(namespacedListsFromBackup.getNamespacedLists().get("NewList").contains("value2"));
    }
}
