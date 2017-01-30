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
import com.comcast.redirector.core.spring.IntegrationTestConfigBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class BackupModeIntegrationTest extends BaseOfflineIntegrationTest {
    @Before
    public void init() throws Exception {
        super.init(IntegrationTestConfigBeans.Profiles.NO_CONNECTION.getName());
        ((Config)config).setExcludedAppsForStackAutoDiscovery(STATIC_APP);
    }

    @Test
    public void modelIsInitializedFromBackup_ForStaticApp_WhenDataStoreIsDown() throws Exception {
        TestContext context = setupContextBuilderForApp(STATIC_APP, Mode.STATIC).build();
        IntegrationTestHelper testHelper = newTestHelperBuilder()
            .context(context)
            .backupManagerFactory(getBackupManagerFactory(STATIC_APP))
            .globalBackupManagerFactory(globalBackupManagerFactory)
            .build();
        testHelper.getBackups().setupBackupEnvForContext();

        testHelper.getRedirectorSupport().initRedirector();
        InstanceInfo result = testHelper.getRedirectorSupport().redirect();

        Assert.assertEquals(context.getDefaultServer().getFlavor(), result.getFlavor());
    }

    @Test
    public void modelIsInitiatedFromBackup_ForDynamicApp_WhenDataStoreIsDown() throws Exception {
        TestContext context = setupContextBuilderForApp(DYNAMIC_APP, Mode.DYNAMIC).build();
        IntegrationTestHelper testHelper = newTestHelperBuilder()
            .context(context)
            .backupManagerFactory(getBackupManagerFactory(DYNAMIC_APP))
            .globalBackupManagerFactory(globalBackupManagerFactory)
            .build();
        testHelper.getBackups().setupBackupEnvForContext();

        testHelper.getRedirectorSupport().initRedirector();
        InstanceInfo defaultPathResult = testHelper.getRedirectorSupport().redirect();
        InstanceInfo flavorPathResult = testHelper.getRedirectorSupport().redirect(
            Collections.singletonMap(context.getFlavorRule().getLeftOperand(), context.getFlavorRule().getRightOperand()));

        Assert.assertEquals(context.getDefaultServer().getFlavor(), defaultPathResult.getFlavor());
        Assert.assertEquals(context.getFlavorRule().getFlavor(), flavorPathResult.getFlavor());
    }

    @Test
    public void redirectForNonInitedAppReturnsNull() throws Exception {
        InstanceInfo result = redirectorEngine.createRedirector("not-inited-app").redirect(Collections.emptyMap());

        Assert.assertNull(result);
    }

    private IntegrationTestHelper.Builder newTestHelperBuilder() {
        return new IntegrationTestHelper.Builder()
            .config(config)
            .redirectorEngine(redirectorEngine)
            .integrationTestModelRefreshListener(integrationTestModelInitListener);
    }
}
