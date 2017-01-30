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
 */
package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedValuesToDeleteByName;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.redirector.service.NamespacedListsService;
import com.comcast.redirector.api.auth.NamespacedListsPermissionPostProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static com.comcast.redirector.api.NamespaceServiceIntegrationTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class NamespacedListsServiceTest {

    @Mock
    private IListDAO<NamespacedList> namespacedListDAO;

    @Mock
    private NamespacedListsPermissionPostProcessService namespacedListsPermissionPostProcessService;

    @InjectMocks
    private NamespacedListsService namespacedListsService;

    @Before
    public void setUp() {
        when(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList(any())).thenReturn(true);
    }

    @Test
    public void deleteEntitiesFromMultipleNamespacedListsTest() throws DataSourceConnectorException {
        NamespacedValuesToDeleteByName namespacedValuesToDeleteByName1 = new NamespacedValuesToDeleteByName();

        NamespacedList listTest1 = getNamespacedListByName(createNamespaces(), "namespacedListTest1");
        namespacedValuesToDeleteByName1.setName("namespacedListTest1");
        namespacedValuesToDeleteByName1.setValuesToDelete(Collections.singletonList("value1_1"));
        namespacedValuesToDeleteByName1.setCurrentNamespacedList(listTest1);

        NamespacedValuesToDeleteByName namespacedValuesToDeleteByName2 = new NamespacedValuesToDeleteByName();
        NamespacedList listTest2 = getNamespacedListByName(createNamespaces(), "namespacedListTest2");
        namespacedValuesToDeleteByName2.setName("namespacedListTest2");
        namespacedValuesToDeleteByName2.setValuesToDelete(Collections.singletonList("value3_2"));
        namespacedValuesToDeleteByName2.setCurrentNamespacedList(listTest2);

        List<NamespacedValuesToDeleteByName> toDelete = new ArrayList<>();
        toDelete.add(namespacedValuesToDeleteByName1);
        toDelete.add(namespacedValuesToDeleteByName2);

        Namespaces namespaces = namespacedListsService.deleteEntitiesFromMultipleNamespacedLists(toDelete, ApplicationStatusMode.OFFLINE);

        List<NamespacedList> list = namespaces.getNamespaces();

        Assert.assertEquals(2, list.size());

        Assert.assertTrue(list.get(1).getValues().contains("value1_2"));
        Assert.assertTrue(list.get(1).getValues().contains("value2_2"));
        Assert.assertFalse(list.get(1).getValues().contains("value3_2"));

        Assert.assertTrue(list.get(0).getValues().contains("value3_1"));
        Assert.assertTrue(list.get(0).getValues().contains("value2_1"));
        Assert.assertFalse(list.get(0).getValues().contains("value1_1"));
    }

    @Test(expected = WebApplicationException.class)
    public void deleteEntitiesFromMultipleNamespacedListsTestWithException() throws DataSourceConnectorException {
        NamespacedValuesToDeleteByName namespacedValuesToDeleteByName1 = new NamespacedValuesToDeleteByName();

        NamespacedList listTest1 = getNamespacedListByName(createNamespaces(), "namespacedListTest1");
        namespacedValuesToDeleteByName1.setName("namespacedListTest1");
        namespacedValuesToDeleteByName1.setValuesToDelete(Collections.singletonList("value1_1"));
        namespacedValuesToDeleteByName1.setCurrentNamespacedList(listTest1);

        NamespacedValuesToDeleteByName namespacedValuesToDeleteByName2 = new NamespacedValuesToDeleteByName();
        namespacedValuesToDeleteByName2.setName("namespacedListTest2");
        namespacedValuesToDeleteByName2.setValuesToDelete(Collections.singletonList("value3_2"));

        List<NamespacedValuesToDeleteByName> toDelete = new ArrayList<>();
        toDelete.add(namespacedValuesToDeleteByName1);
        toDelete.add(namespacedValuesToDeleteByName2);

        namespacedListsService.deleteEntitiesFromMultipleNamespacedLists(toDelete, ApplicationStatusMode.OFFLINE);
    }

    public NamespacedList getNamespacedListByName(Namespaces namespaces, String name) {
        for(NamespacedList namespacedList : namespaces.getNamespaces()) {
            if(name.equals(namespacedList.getName())) {
                return namespacedList;
            }
        }
        return null;
    }
}
