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

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.backup.IBackupManager;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class StacksBackupManagerTest {
    private static final String MANUAL_BACKUP_STRING = "MANUAL_BACKUP_STRING";
    private static final String SERIALIZED_STACK = "SERIALIZED_STACK";

    private Serializer serializer;
    private IBackupManager manualBackupManager;
    private StacksBackupManager stacksBackupManager;

    @Before
    public void before() throws Exception {
        serializer = mock(Serializer.class);
        manualBackupManager = mock(IBackupManager.class);

        when(manualBackupManager.load()).thenReturn(MANUAL_BACKUP_STRING);
        when(serializer.deserialize(anyString(), eq(StackBackup.class))).thenReturn(new StackBackup());
        when(serializer.serialize(any(StackBackup.class), anyBoolean())).thenReturn(SERIALIZED_STACK);

        stacksBackupManager = new StacksBackupManager(manualBackupManager, serializer);
    }

    @After
    public void after() {
        serializer = null;
        manualBackupManager = null;
        stacksBackupManager = null;
    }

    @Test
    public void testLoadManualEmpty() {
        // manual data absent, automatic data is present
        StackBackup result = stacksBackupManager.load();

        Assert.assertEquals(new StackBackup(), result);
    }

    @Test
    public void testLoadManualExists() throws Exception {
        // manual data present, automatic data is absent
        stacksBackupManager.load();

        when(manualBackupManager.load()).thenReturn("nextBackupValue");
        StackBackup nextStackBackup = new StackBackup(4, Collections.emptyList());
        when(serializer.deserialize(eq("nextBackupValue"), eq(StackBackup.class))).thenReturn(nextStackBackup);

        StackBackup resultFromCache = stacksBackupManager.load();

        Assert.assertNotEquals(nextStackBackup.getVersion(), resultFromCache.getVersion());
    }

    @Test
    public void testBackup() throws Exception {
        int savedVersion = 10;

        stacksBackupManager.backup(new StackBackup(savedVersion, Collections.emptyList()));
        StackBackup cachedBackup = stacksBackupManager.load();

        verify(manualBackupManager, atLeastOnce()).backup(SERIALIZED_STACK);
        Assert.assertEquals(savedVersion, cachedBackup.getVersion());
    }
}
