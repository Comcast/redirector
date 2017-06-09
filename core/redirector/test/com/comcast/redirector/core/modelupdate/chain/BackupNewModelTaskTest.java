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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BackupNewModelTaskTest {
    private BackupNewModelTask task;

    private ModelContext modelContext;
    private IModelHolder<SelectServer> flavorRulesHolder;
    private IModelHolder<Whitelisted> whiteListHolder;
    private IModelHolder<URLRules> urlRulesHolder;
    private IModelHolder<ModelMetadata> modelMetaDataHolder;

    @Before
    public void setUp() throws Exception {
        flavorRulesHolder = (IModelHolder<SelectServer>) mock(IModelHolder.class);
        whiteListHolder = (IModelHolder<Whitelisted>) mock(IModelHolder.class);
        urlRulesHolder = (IModelHolder<URLRules>) mock(IModelHolder.class);
        modelMetaDataHolder = (IModelHolder<ModelMetadata>) mock(IModelHolder.class);
        when(flavorRulesHolder.backup(any())).thenReturn(true);
        when(whiteListHolder.backup(any())).thenReturn(true);
        when(urlRulesHolder.backup(any())).thenReturn(true);
        when(modelMetaDataHolder.backup(any())).thenReturn(true);
        modelContext = new ModelContext();
        modelContext.setFlavorRules(mock(SelectServer.class));
        modelContext.setUrlRules(mock(URLRules.class));
        modelContext.setWhitelistedStacks(mock(Whitelisted.class));
        modelContext.setModelVersion(1);
        task = new BackupNewModelTask(flavorRulesHolder, whiteListHolder, urlRulesHolder, modelMetaDataHolder);
    }

    @Test
    public void testSuccessfulBackup() throws Exception {
        ModelMetadata modelMetadata = new ModelMetadata();
        modelMetadata.setVersion(0);
        when(modelMetaDataHolder.load(anyBoolean())).thenReturn(modelMetadata);
        verifySuccess();
    }

    @Test
    public void testFailedBackupNoFlavorRules() throws Exception {
        modelContext.setFlavorRules(null);
        verifyFailed();
    }

    @Test
    public void testFailedBackupNoUrlRules() throws Exception {
        modelContext.setUrlRules(null);
        verifyFailed();
    }

    @Test
    public void testFailedBackupNoWhitelistedStacks() throws Exception {
        modelContext.setWhitelistedStacks(null);
        verifyFailed();
    }

    private void verifySuccess() {
        Assert.assertTrue(task.handle(modelContext).isSuccessful());
        verify(flavorRulesHolder, times(1)).backup(any(SelectServer.class));
        verify(whiteListHolder, times(1)).backup(any(Whitelisted.class));
        verify(urlRulesHolder, times(1)).backup(any(URLRules.class));
        verify(modelMetaDataHolder, times(1)).load(eq(true));
        verify(modelMetaDataHolder, times(1)).backup(any(ModelMetadata.class));
    }

    private void verifyFailed() {
        Assert.assertFalse(task.handle(modelContext).isSuccessful());
        verify(flavorRulesHolder, never()).backup(any(SelectServer.class));
        verify(whiteListHolder, never()).backup(any(Whitelisted.class));
        verify(urlRulesHolder, never()).backup(any(URLRules.class));
        verify(modelMetaDataHolder, never()).backup(any(ModelMetadata.class));
    }
}
