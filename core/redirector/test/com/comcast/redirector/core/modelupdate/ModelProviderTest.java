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

package com.comcast.redirector.core.modelupdate;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.modelupdate.chain.ModelContext;
import com.comcast.redirector.core.modelupdate.chain.Result;
import com.comcast.redirector.core.modelupdate.chain.TaskChain;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.modelupdate.holder.ICachedModelHolder;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Predicate;

import static org.mockito.Mockito.*;

public class ModelProviderTest {
    private static final String APP_NAME = "appname";

    private ICachedModelHolder<ModelMetadata> modelMetadataHolder;

    private RedirectorEngineProvider testee;

    private TaskChain refreshModelChain;
    private TaskChain initModelChain;
    private TaskChain syncModelChain;

    @Before
    public void setUp() throws Exception {
        modelMetadataHolder = (ICachedModelHolder<ModelMetadata>)mock(ICachedModelHolder.class);

        initModelChain = mock(TaskChain.class);
        refreshModelChain = mock(TaskChain.class);
        syncModelChain = mock(TaskChain.class);

        testee = new RedirectorEngineProvider();
        ReflectionTestUtils.setField(testee, "appName", APP_NAME);
        ReflectionTestUtils.setField(testee, "modelFacade", mock(IAppModelFacade.class));
        ReflectionTestUtils.setField(testee, "xmlSerializer", mock(Serializer.class));
        ReflectionTestUtils.setField(testee, "backupManagerFactory", mock(IBackupManagerFactory.class));
        ReflectionTestUtils.setField(testee, "globalBackupManagerFactory", mock(IBackupManagerFactory.class));
        ReflectionTestUtils.setField(testee, "isStaticDiscoveryNeededForApp", mock(Predicate.class));
        ReflectionTestUtils.invokeMethod(testee, "init");

        ReflectionTestUtils.setField(testee, "modelMetadataHolder", modelMetadataHolder);
        ReflectionTestUtils.setField(testee, "initModelChain", initModelChain);
        ReflectionTestUtils.setField(testee, "refreshModelChain", refreshModelChain);
        ReflectionTestUtils.setField(testee, "syncModelChain", syncModelChain);
    }

    @Test
    public void testRefreshModelSuccessfully() throws Exception {
        int modelVersion = 10;
        IRedirectorEngine resultRedirectorEngine = mock(IRedirectorEngine.class);
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(resultRedirectorEngine));

        IRedirectorEngine engine = testee.refreshModel(modelVersion);

        verify(modelMetadataHolder, times(1)).resetCache();
        Assert.assertEquals(resultRedirectorEngine, engine);
    }

    @Test(expected=Exception.class)
    public void testRefreshModelFailedRefreshChain() throws Exception {
        int modelVersion = 10;
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.refreshModel(modelVersion);

        verify(modelMetadataHolder, times(1)).resetCache();
        Assert.assertNull(engine);
    }

    @Test
    public void testRefreshModelFailedModelIsUpToDate() throws Exception {
        int modelVersion = 10;
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.refreshModel(modelVersion);

        verify(modelMetadataHolder, times(1)).resetCache();
        Assert.assertNull(engine);
    }

    @Test
    public void testInitModelSuccessfullyFromBackupModelsAreUpToDate() throws Exception {
        IRedirectorEngine resultRedirectorEngine = mock(IRedirectorEngine.class);
        when(initModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(resultRedirectorEngine));
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.initModel();

        Assert.assertEquals(resultRedirectorEngine, engine);
    }

    @Test
    public void testInitModelSuccessfullyFromBackupModelsNeedToBeUpdated() throws Exception {
        IRedirectorEngine initRedirectorEngine = mock(IRedirectorEngine.class);
        IRedirectorEngine updateRedirectorEngine = mock(IRedirectorEngine.class);

        when(initModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(initRedirectorEngine));
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(updateRedirectorEngine));

        IRedirectorEngine engine = testee.initModel();

        Assert.assertEquals(updateRedirectorEngine, engine);
    }

    @Test
    public void testInitModelSuccessfullyFromBackupModelsNeedToBeUpdatedButFailed() throws Exception {
        IRedirectorEngine initRedirectorEngine = mock(IRedirectorEngine.class);
        when(initModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(initRedirectorEngine));
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.initModel();

        Assert.assertEquals(initRedirectorEngine, engine);
    }

    @Test
    public void testInitModelFailedFromBackup_AndUpdateFailedFromDataStore() throws Exception {
        when(initModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.initModel();

        Assert.assertNull(engine);
    }

    @Test
    public void testInitModelFailedFromBackupAndFromDataStore() throws Exception {
        IRedirectorEngine updateRedirectorEngine = mock(IRedirectorEngine.class);
        when(initModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());
        when(refreshModelChain.execute(any(ModelContext.class))).thenReturn(successfulResult(updateRedirectorEngine));
        when(syncModelChain.execute(any(ModelContext.class))).thenReturn(failedResult());

        IRedirectorEngine engine = testee.initModel();

        Assert.assertEquals(updateRedirectorEngine, engine);
    }

    private static Result successfulResult() {
        return successfulResult(mock(IRedirectorEngine.class));
    }

    private static Result successfulResult(IRedirectorEngine engine) {
        ModelContext context = new ModelContext();
        context.setRedirectorEngine(engine);
        return Result.success(context);
    }

    private static Result failedResult() {
        ModelContext context = new ModelContext();
        return Result.failure(context);
    }
}
