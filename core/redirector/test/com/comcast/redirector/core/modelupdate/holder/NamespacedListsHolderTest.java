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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.core.modelupdate.helper.NamespacedListsHelper;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.comcast.redirector.core.modelupdate.helper.NamespacedListsHelper.getNamespacedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class NamespacedListsHolderTest {
    private NamespacedListsHolder namespacedListsHolder;
    private ICommonModelFacade commonModelFacade;

    @Before
    public void setUp() throws Exception {
        commonModelFacade = NamespacedListsHelper.prepareModelFacadeBeforeTest();
        namespacedListsHolder = NamespacedListsHelper.prepareNamespacedListsHolderBeforeTest();
        ReflectionTestUtils.setField(namespacedListsHolder, "commonModelFacade", commonModelFacade);
    }

    @Test
    public void loadFromDataStoreSavesNamespacedListBatchInHolder() {
        namespacedListsHolder.loadFromDataStore();

        List<String> values = new ArrayList<>();
        values.addAll(namespacedListsHolder.getNamespacedListValues(NamespacedListsHelper.namespacedListName));
        assertEquals(1, namespacedListsHolder.getNamespacedListsBatch().getNamespacedLists().size());
        assertEquals("Val", values.get(0));
    }

    @Test
    public void loadFromDataStoreOverridesExistingNamespacedListBatchInHolder() {
        namespacedListsHolder.loadFromDataStore();

        when(commonModelFacade.getAllNamespacedLists()).thenReturn(getNamespacedList("Val2"));
        namespacedListsHolder.loadFromDataStore();

        List<String> values = new ArrayList<>();
        values.addAll(namespacedListsHolder.getNamespacedListValues(NamespacedListsHelper.namespacedListName));
        assertEquals(1, namespacedListsHolder.getNamespacedListsBatch().getNamespacedLists().size());
        assertFalse("Val".equals(values.get(0)));
        assertEquals("Val2", values.get(0));
    }

    @Test
    public void testLoadGetFromBackUp() throws Exception {
        NamespacedListsHolder spy = spy(namespacedListsHolder);
        spy.load(IModelHolder.GET_FROM_BACKUP);

        verify(spy, never()).loadFromDataStore();
        verify(spy, times(1)).loadFromBackup();
    }

    @Test
    public void testLoadFromBackUp() throws Exception {
        namespacedListsHolder.load(IModelHolder.GET_FROM_BACKUP);
        int actualSize = namespacedListsHolder.getNamespacedListsBatch().getNamespacedLists().size();
        int expectedSize = 2;
        assertEquals(expectedSize, actualSize);
        assertEquals(NamespacedListsHelper.getNamespacedListMap(), namespacedListsHolder.getNamespacedListsBatch().getNamespacedLists());
    }

    @Test
    public void testLoadGetFromDataStore() throws Exception {
        NamespacedListsHolder spy = spy(namespacedListsHolder);
        spy.load(IModelHolder.GET_FROM_DATA_STORE);

        verify(spy, times(1)).loadFromDataStore();
        verify(spy, never()).loadFromBackup();
    }
}
