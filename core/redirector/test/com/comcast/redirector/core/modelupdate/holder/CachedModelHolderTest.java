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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CachedModelHolderTest {
    private Serializer serializer;
    private IBackupManager backupManager;

    private TestCachedModelHolder testee;

    @Before
    public void setUp() throws Exception {
        serializer = mock(Serializer.class);
        backupManager = mock(IBackupManager.class);

        testee = new TestCachedModelHolder(ModelMetadata.class, serializer, backupManager);
    }

    @Test
    public void testLoadFromDataStore() throws Exception {
        Assert.assertNull(testee.load(IModelHolder.GET_FROM_DATA_STORE));
        ModelMetadata data1 = new ModelMetadata();
        data1.setVersion(12);
        ModelMetadata data2 = new ModelMetadata();
        data1.setVersion(13);
        testee.dataFromDataStore = data1;
        Assert.assertEquals(data1, testee.load(IModelHolder.GET_FROM_DATA_STORE));
        testee.dataFromDataStore = data2;
        Assert.assertEquals(data1, testee.load(IModelHolder.GET_FROM_DATA_STORE));
        testee.resetCache();
        Assert.assertEquals(data2, testee.load(IModelHolder.GET_FROM_DATA_STORE));
    }

    @Test
    public void testLoadFromBackup() throws Exception {
        Assert.assertNull(testee.load(IModelHolder.GET_FROM_BACKUP));

        ModelMetadata data1 = new ModelMetadata();
        data1.setVersion(12);
        ModelMetadata data2 = new ModelMetadata();
        data1.setVersion(13);

        when(backupManager.load()).thenReturn("test");
        when(serializer.deserialize(anyString(), eq(ModelMetadata.class))).thenReturn(data1);

        Assert.assertEquals(data1, testee.load(IModelHolder.GET_FROM_BACKUP));

        when(serializer.deserialize(anyString(), eq(ModelMetadata.class))).thenReturn(data2);
        Assert.assertEquals(data1, testee.load(IModelHolder.GET_FROM_BACKUP));
        testee.resetCache();
        Assert.assertEquals(data2, testee.load(IModelHolder.GET_FROM_BACKUP));
    }

    @Test
    public void testBackup() throws Exception {
        Assert.assertNull(testee.load(IModelHolder.GET_FROM_BACKUP));

        ModelMetadata data1 = new ModelMetadata();
        data1.setVersion(12);
        ModelMetadata data2 = new ModelMetadata();
        data1.setVersion(13);

        testee.backup(data1);

        when(backupManager.load()).thenReturn("test");
        when(serializer.deserialize(anyString(), eq(ModelMetadata.class))).thenReturn(data2);
        Assert.assertEquals(data1, testee.load(IModelHolder.GET_FROM_BACKUP));

        testee.resetCache();
        Assert.assertEquals(data2, testee.load(IModelHolder.GET_FROM_BACKUP));
    }

    private class TestCachedModelHolder extends CachedModelHolder<ModelMetadata> {
        ModelMetadata dataFromDataStore;

        TestCachedModelHolder(Class<ModelMetadata> classType, Serializer serializer, IBackupManager backupManager) {
            super(classType, serializer, backupManager);
        }

        @Override
        protected ModelMetadata loadFromDataStore() {
            return dataFromDataStore;
        }
    }
}
