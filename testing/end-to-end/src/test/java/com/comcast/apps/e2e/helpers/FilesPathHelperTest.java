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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FilesPathHelperTest {
    private static final String TMP_FILE = File.separator + "tmp";

    @Before
    public void setUp () throws URISyntaxException, IOException {
        String path = getCurrentPath();
        Files.deleteIfExists(Paths.get(path  + TMP_FILE));
    }

    @Test
    public void getFilenameTest() {
        FilesPathHelper filesPathHelper = new FilesPathHelper("");
        assertEquals(File.separator + "multiruntime_config.xml", filesPathHelper.getFilename(FilesPathHelper.TestEntity.MULTI_RUNTIME_CONFIG));
        assertEquals(File.separator + "namespacedlists.json", filesPathHelper.getFilename(FilesPathHelper.TestEntity.NAMESPACED_LISTS));
        assertEquals(File.separator + "guide_startup.xml", filesPathHelper.getFilename(FilesPathHelper.TestEntity.GUIDE_STARTUP));
        assertEquals(File.separator + "redirectorConfig.json", filesPathHelper.getFilename(FilesPathHelper.TestEntity.REDIRECTOR_CONFIG));
        assertEquals(File.separator + "manualbackup.json", filesPathHelper.getFilename(FilesPathHelper.TestEntity.STATIC_STACKS));
        assertEquals(File.separator + "stacks.json", filesPathHelper.getFilename(FilesPathHelper.TestEntity.DYNAMIC_STACKS));
        assertEquals(File.separator + "report.json", filesPathHelper.getFilename(FilesPathHelper.TestEntity.REPORT));
        assertEquals(File.separator + "selectserver.xml", filesPathHelper.getFilename(FilesPathHelper.TestEntity.SELECT_SERVER));
        assertEquals(File.separator + "urlrules.xml", filesPathHelper.getFilename(FilesPathHelper.TestEntity.URL_RULES));
        assertEquals(File.separator + "whitelist.xml", filesPathHelper.getFilename(FilesPathHelper.TestEntity.WHITELISTED));
    }

    @Test
    public void getGeneratedBackupFilenamesFromBaseFolderAloneDirectoryTest() throws URISyntaxException, IOException {
        String path = getCurrentPath();

        FilesPathHelper filesPathHelper = new FilesPathHelper(path);
        Set<String> files = filesPathHelper.getGeneratedBackupFilenamesFromBaseFolder();

        Assert.assertEquals(0, files.size());
    }

    @Test
    public void getGeneratedBackupFilenamesFromBaseFolderMultiDirectoryTest() throws URISyntaxException, IOException {
        String path = getCurrentPath();

        Files.createDirectories(Paths.get(path + TMP_FILE));

        FilesPathHelper filesPathHelper = new FilesPathHelper(path);
        Set<String> files = filesPathHelper.getGeneratedBackupFilenamesFromBaseFolder();

        Assert.assertEquals(2, files.size());

        Assert.assertTrue(files.contains(path + TMP_FILE + File.separator + "multiruntime_config.xml"));
        Assert.assertTrue(files.contains(path + TMP_FILE + File.separator + "report.json"));

        Files.deleteIfExists(Paths.get(path  + TMP_FILE));
    }

    private String getCurrentPath() throws URISyntaxException {
        URL currentDir = FilesPathHelperTest.class.getResource(".");
        return Paths.get(currentDir.toURI()).toFile().getAbsolutePath();
    }
}
