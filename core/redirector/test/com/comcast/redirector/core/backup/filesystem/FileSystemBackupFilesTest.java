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

package com.comcast.redirector.core.backup.filesystem;

import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.backup.filesystem.FileSystemBackupFiles;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(DataProviderRunner.class)
public class FileSystemBackupFilesTest {
    private FileSystemBackupFiles testee;
    private String basePath = "some path";
    private String appName = "appName";

    @DataProvider
    public static Object[][] data() {
        return new Object[][] {
                { IBackupManagerFactory.BackupEntity.STACKS_AUTOMATIC, "automaticbackup.json" },
                { IBackupManagerFactory.BackupEntity.STACKS_MANUAL, "manualbackup.json" },
                { IBackupManagerFactory.BackupEntity.FLAVOR_RULES, "selectserver.xml" },
                { IBackupManagerFactory.BackupEntity.URL_RULES, "urlrules.xml" },
                { IBackupManagerFactory.BackupEntity.WHITE_LIST, "whitelist.xml" },
                { IBackupManagerFactory.BackupEntity.MODEL_METADATA, "modelmetadata.json" }
        };
    }

    @DataProvider
    public static Object[][] dataNamespacedLists() {
        return new Object[][] {
                   { IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS, "namespacedlists.json" }
        };
    }

    @Before
    public void setUp() throws Exception {
        testee = new FileSystemBackupFiles(appName, basePath);
    }

    @Test
    @UseDataProvider("data")
    public void testWithoutNamespacedListsFile(IBackupManagerFactory.BackupEntity entity, String expectedFilename) throws Exception {
        String result = testee.getFilename(entity);
        Assert.assertNotNull(result);
        Assert.assertEquals(basePath + File.separator + appName + File.separator + expectedFilename, result);
    }

    @Test
    @UseDataProvider("dataNamespacedLists")
    public void testForNamespacedListsFile(IBackupManagerFactory.BackupEntity entity, String expectedFilename) throws Exception {
        String result = testee.getFilename(entity);
        Assert.assertNotNull(result);
        Assert.assertEquals(basePath + File.separator  + expectedFilename, result);
    }

    @Test
    public void testBasePathMisConfigured() throws Exception {
        String appName = "appName";
        testee = new FileSystemBackupFiles(appName, null);
        String filename = testee.getFilename(IBackupManagerFactory.BackupEntity.STACKS_AUTOMATIC);
        Assert.assertNull(filename);
    }
}
