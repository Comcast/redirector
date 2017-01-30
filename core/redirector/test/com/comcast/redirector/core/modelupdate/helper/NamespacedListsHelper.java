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

package com.comcast.redirector.core.modelupdate.helper;


import com.comcast.redirector.api.model.builders.NamespacedListBuilder;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.modelupdate.holder.NamespacedListsHolder;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NamespacedListsHelper {

    public static String namespacedListName = "TestNamespacedList";

    public static String namespacedLists = "{\"namespacedLists\":{\"3\":[\"6\",\"4\"],\"2\":[\"11\"]}}";

    public static ICommonModelFacade prepareModelFacadeBeforeTest(){
        ICommonModelFacade modelFacade = mock(ICommonModelFacade.class);
        when(modelFacade.getAllNamespacedLists()).thenReturn(getNamespacedList("Val"));
        return modelFacade;
    }

    public static NamespacedListsHolder prepareNamespacedListsHolderBeforeTest(){
        IBackupManagerFactory backupManagerFactory = mock(IBackupManagerFactory.class);
        when(backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS)).thenReturn(mock(IBackupManager.class));
        when(backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS).load()).thenReturn(namespacedLists);
        NamespacedListsHolder namespacedListsHolder = new NamespacedListsHolder();
        ReflectionTestUtils.setField(namespacedListsHolder, "globalBackupManagerFactory", backupManagerFactory);
        return namespacedListsHolder;
    }

    public static Map<String, Set<String>> getNamespacedListMap() {
        Map<String, Set<String>> namespacedLists = new HashMap<>();

        Set<String> values = new HashSet<>();
        values.add("6");
        values.add("4");
        namespacedLists.put("3", values);

        values = new HashSet<>();
        values.add("11");
        namespacedLists.put("2", values);

        return namespacedLists;
    }

    public static Collection<NamespacedList> getNamespacedList(String valueNamespacedLists) {
        Collection<NamespacedList> namespacedLists = new ArrayList<>();

        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(namespacedListName);

        Set<NamespacedListValueForWS> values = new HashSet<>();

        NamespacedListValueForWS value = new NamespacedListValueForWS();
        value.setValue(valueNamespacedLists);

        values.add(value);

        namespacedList.setType(NamespacedListType.TEXT);
        namespacedList.setValueSet(values);

        namespacedLists.add(namespacedList);

        return namespacedLists;
    }

    public static NamespacedList createNamespacedList(String name, String... values) {
        return new NamespacedListBuilder()
            .withName(name)
            .withValues(values).
                        withType(NamespacedListType.TEXT)
            .build();
    }
}
