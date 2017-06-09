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
 */
package com.comcast.redirector.core.balancer.serviceprovider.stacks;

import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.IAppBackupManagerFactories;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscoveryBackupManagerTest {
    private IBackupManagerFactory backupManagerFactory;
    private IBackupManager backupManager;
    private IBackupManager backupManagerApp;
    private IBackupManager backupManagerFile;
    private IAppBackupManagerFactories appBackupManagerFactories;
    private Set<String> excludedApplications;

    private String data = "{\n" +
            "  \"version\": 0,\n" +
            "  \"snapshotList\": [\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.02/test1\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"8\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.03/test2\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"8\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.0/xreGuide\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"4\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.19/test2\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"2\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.9/xreTest\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"8\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.01/xreTest\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"3\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"path\": \"/PO/POC5/1.20/xreTest\",\n" +
            "      \"hosts\": [\n" +
            "        {\n" +
            "          \"ipv4\": \"100.200.100.111\",\n" +
            "          \"ipv6\": \"2001:0DB8:0000:0003:0000:01FF:0000:002E\",\n" +
            "          \"weight\": \"2\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";


    @Before
    public void setUp() throws Exception {
        backupManagerFactory = mock(IBackupManagerFactory.class);
        backupManager = mock(IBackupManager.class);
        backupManagerApp = mock(IBackupManager.class);
        backupManagerFile = mock(IBackupManager.class);

        when(backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.DISCOVERY)).thenReturn(backupManager);
        when(backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.APPLICATIONS)).thenReturn(backupManagerApp);
        when(backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.STACKS_MANUAL)).thenReturn(backupManagerFile);
        excludedApplications = new HashSet<>();
    }

    @Test
    public void discoveryBackupManagerRemoveStaticStacksTest () {

        when(backupManager.load()).thenReturn(data);
        excludedApplications.add("xreTest");

        DiscoveryBackupManager discoveryBackupManager = new DiscoveryBackupManager(backupManagerFactory, excludedApplications, appBackupManagerFactories);
        verify(backupManager, atLeastOnce()).backup(any());
        Assert.assertEquals(4, discoveryBackupManager.getCurrentSnapshot().getSnapshotList().size());

        StackSnapshot snapshotXreTest1 = createStackSnapshot("/PO/POC5/1.01/xreTest", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "3");
        StackSnapshot snapshotXreTest2 = createStackSnapshot("/PO/POC5/1.20/xreTest", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "2");
        StackSnapshot snapshotXreTest3 = createStackSnapshot("/PO/POC5/1.9/xreTest", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "8");

        Assert.assertFalse(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotXreTest1));
        Assert.assertFalse(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotXreTest2));
        Assert.assertFalse(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotXreTest3));

        StackSnapshot snapshotTest1 = createStackSnapshot("/PO/POC5/1.02/test1", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "8");
        StackSnapshot snapshotTest2_1 = createStackSnapshot("/PO/POC5/1.03/test2", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "8");
        StackSnapshot snapshotXreGuide= createStackSnapshot("/PO/POC5/1.0/xreGuide", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "4");
        StackSnapshot snapshotTest2_2 = createStackSnapshot("/PO/POC5/1.19/test2", "100.200.100.111", "2001:0DB8:0000:0003:0000:01FF:0000:002E", "2");

        Assert.assertTrue(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotTest1));
        Assert.assertTrue(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotTest2_1));
        Assert.assertTrue(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotXreGuide));
        Assert.assertTrue(discoveryBackupManager.getCurrentSnapshot().getSnapshotList().contains(snapshotTest2_2));
    }

    @Test
    public void simpleDiscoveryBackupManagerCreationTest () {

        when(backupManager.load()).thenReturn(null);
        when(backupManagerApp.load()).thenReturn(null);

        DiscoveryBackupManager discoveryBackupManager = new DiscoveryBackupManager(backupManagerFactory, excludedApplications, appBackupManagerFactories);
        verify(backupManager, never()).backup(any());
        Assert.assertEquals(0, discoveryBackupManager.getCurrentSnapshot().getSnapshotList().size());
    }

    private StackSnapshot createStackSnapshot(String path, String ipv4, String ipv6, String weight){

        StackSnapshot snapshot = new StackSnapshot();
        snapshot.setPath(path);

        List<StackSnapshot.Host> hosts = new ArrayList<>();
        hosts.add(new StackSnapshot.Host(ipv4, ipv6, weight));
        snapshot.setHosts(hosts);
        return snapshot;
    }
}

