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
package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.namespaced.NamespaceChangesStatus;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NamespacesChangesServiceTest {

    public static final String SERVICE_NAME = "serviceName";

    @Mock
    private NamespacedListsService namespacedListsService;

    @Mock(name = "coreBackupNamespaceChangesDAO")
    private ISimpleServiceDAO<NamespaceChangesStatus> coreBackupNamespaceChangesDAO;

    @InjectMocks
    private NamespacesChangesService namespacesChangesService;

    private NamespacedList namespacedList1;
    private NamespacedList namespacedList2;
    private NamespacedList namespacedList3;

    @Before
    public void setUp() {
        Map<NamespacedList, ActionType> namespaceChanges = new LinkedHashMap<>();
        List<NamespacedList> listList = new ArrayList<>();

        namespacedList1 = createNamespacedList("namespacedList1", 2);
        namespacedList2 = createNamespacedList("namespacedList2", 1);
        namespacedList3 = createNamespacedList("namespacedList3", 3);

        namespaceChanges.put(namespacedList1, ActionType.ADD);
        namespaceChanges.put(namespacedList2, ActionType.UPDATE);
        namespaceChanges.put(namespacedList3, ActionType.DELETE);

        Namespaces namespaces = new Namespaces();
        namespaces.setNamespaces(listList);

        NamespaceChangesStatus namespaceChangesStatus = new NamespaceChangesStatus(namespaceChanges);

        when(namespacedListsService.getAllNamespacedLists()).thenReturn(namespaces);
        when(coreBackupNamespaceChangesDAO.get(SERVICE_NAME)).thenReturn(namespaceChangesStatus);
    }

    @Test
    public void approveAllTest() {
        NamespaceChangesStatus namespacesChangesStatusReturned = namespacesChangesService.approveAll(SERVICE_NAME);

        verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList1);
        verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList2);
        verify(namespacedListsService, atLeastOnce()).deleteNamespacedList(namespacedList3.getName());

        Assert.assertEquals(0, namespacesChangesStatusReturned.getNamespaceChanges().size());
    }

    @Test
    public void approveAllinOrderTest() {

        InOrder inOrder = inOrder(namespacedListsService);

        NamespaceChangesStatus namespacesChangesStatusReturned = namespacesChangesService.approveAll(SERVICE_NAME);

        inOrder.verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList2);
        inOrder.verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList1);
        inOrder.verify(namespacedListsService, atLeastOnce()).deleteNamespacedList(namespacedList3.getName());

        Assert.assertEquals(0, namespacesChangesStatusReturned.getNamespaceChanges().size());

    }

    @Test
    public void approveAllWithExeptionTest() throws SerializerException {

        doThrow(new WebApplicationException()).when(namespacedListsService).deleteNamespacedList(namespacedList3.getName());

        try {
            namespacesChangesService.approveAll(SERVICE_NAME);
        } catch (WebApplicationException ex) {

        }

        verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList1);
        verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList2);
        verify(namespacedListsService, atLeastOnce()).deleteNamespacedList(namespacedList3.getName());

        ArgumentCaptor<NamespaceChangesStatus> argument = ArgumentCaptor.forClass(NamespaceChangesStatus.class);

        verify(coreBackupNamespaceChangesDAO).save(argument.capture(), eq(SERVICE_NAME));
        Assert.assertEquals(1, argument.getValue().getNamespaceChanges().size());
        Assert.assertTrue(argument.getValue().getNamespaceChanges().containsKey(namespacedList3));
    }

    @Test
    public void approveTest() {

        NamespaceChangesStatus namespacesChangesStatusReturned = namespacesChangesService.approve(SERVICE_NAME, namespacedList1);

        verify(namespacedListsService, atLeastOnce()).addNamespacedList(namespacedList1);
        Assert.assertEquals(2, namespacesChangesStatusReturned.getNamespaceChanges().size());
    }

    @Test
    public void cancelTest() {

        NamespaceChangesStatus namespacesChangesStatusReturned = namespacesChangesService.cancel(SERVICE_NAME, namespacedList1);

        verify(namespacedListsService, never()).addNamespacedList(namespacedList1);
        Assert.assertEquals(2, namespacesChangesStatusReturned.getNamespaceChanges().size());

    }

    @Test
    public void cancelAllTest() {

        NamespaceChangesStatus namespacesChangesStatusReturned = namespacesChangesService.cancelAll(SERVICE_NAME);

        Assert.assertEquals(0, namespacesChangesStatusReturned.getNamespaceChanges().size());
        verify(namespacedListsService, never()).addNamespacedList(namespacedList1);
        verify(namespacedListsService, never()).addNamespacedList(namespacedList2);
        verify(namespacedListsService, never()).deleteNamespacedList(namespacedList3.getName());
    }

    private NamespacedList createNamespacedList(String name, long version) {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(name);
        namespacedList.setVersion(version);
        namespacedList.setValues(Collections.singleton(name));
        return namespacedList;
    }
}
