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
package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.core.applications.Applications;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.mockito.Mockito.when;

public class ApplicationsLoaderTest {

    public static final String APPLICATIONS_1 = "Applications_1";
    public static final String APPLICATIONS_2 = "Applications_2";

    @Mock
    private FileUtil fileUtil;

    @Mock
    private FilesPathHelper filesPathHelper;

    private String backupFileName = "applications.json";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Applications applications = new Applications();
        applications.addApp(APPLICATIONS_1);
        applications.addApp(APPLICATIONS_2);

        when(filesPathHelper.getFilename(FilesPathHelper.TestEntity.APPLICATIONS)).thenReturn(backupFileName);
        when(fileUtil.readJson(backupFileName, Applications.class)).thenReturn(applications);
    }

    @Test
    public void loadApplicationTest() {
        ApplicationsLoader applicationsLoader = new ApplicationsLoader(fileUtil, filesPathHelper);
        Set<String> loadAppNamesSet = applicationsLoader.loadAppNames();
        Assert.assertEquals(2, loadAppNamesSet.size());
        Assert.assertTrue(loadAppNamesSet.contains(APPLICATIONS_1));
        Assert.assertTrue(loadAppNamesSet.contains(APPLICATIONS_2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failLoadApplicationTest() {
        when(fileUtil.readJson(backupFileName, Applications.class)).thenReturn(null);

        ApplicationsLoader applicationsLoader = new ApplicationsLoader(fileUtil, filesPathHelper);
        applicationsLoader.loadAppNames();
    }
}
