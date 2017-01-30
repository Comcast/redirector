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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.core.modelupdate.helper.NamespacedListsHelper;
import com.comcast.redirector.core.modelupdate.holder.IDataStoreAwareNamespacedListsHolder;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetNamespacedListsTaskTest {

    private GetNamespacedListsTask testee;
    private IDataStoreAwareNamespacedListsHolder namespacedListsHolder;

    @Before
    public void setUp() throws Exception {
        namespacedListsHolder = mock(IDataStoreAwareNamespacedListsHolder.class);
    }
    
    @Test
    public void testIsValidIfNamespacedListsIsNotEmpty() {
        testee = initTask();
        when(namespacedListsHolder.getNamespacedListsBatch()).thenReturn(nonEmptyBatch());

        assertTrue(testee.handle(mock(ModelContext.class)).isSuccessful());
    }

    private static NamespacedListsBatch emptyBatch() {
        return new NamespacedListsBatch();
    }

    private static NamespacedListsBatch nonEmptyBatch() {
        NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
        namespacedListsBatch.setNamespacedLists(NamespacedListsHelper.getNamespacedListMap());

        return namespacedListsBatch;
    }

    private GetNamespacedListsTask initTask() {
        return new GetNamespacedListsTask(namespacedListsHolder, IModelHolder.GET_FROM_BACKUP);
    }
}
