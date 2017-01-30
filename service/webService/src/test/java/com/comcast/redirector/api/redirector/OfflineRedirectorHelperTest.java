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
package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IRedirectorConfigService;
import com.comcast.redirector.api.redirectorOffline.OfflineRedirectorHelper;
import com.comcast.redirector.common.serializers.*;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class OfflineRedirectorHelperTest {

    @Mock
    private INamespacedListsService namespacedListsService;

    @Mock
    private IRedirectorConfigService redirectorConfigService;

    @Mock
    private IDataSourceConnector connector;

    @InjectMocks
    @Resource
    private OfflineRedirectorHelper offlineRedirectorHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JAXBContextBuilder jaxbContextBuilder = new JAXBContextBuilder();
        JSONSerializer jsonSerializer = new JSONSerializer(jaxbContextBuilder.createContextForJSON());
        ReflectionTestUtils.setField(offlineRedirectorHelper, "jsonSerializer", jsonSerializer);
    }

    @Test
    public void expectedZeroBytes_getSettingsJsonSnapshotTest() throws SerializerException {
        when(redirectorConfigService.getRedirectorConfig()).thenReturn(null);

        byte[] bytes = offlineRedirectorHelper.getSettingsJsonSnapshot();

        Assert.assertEquals(0, bytes.length);
    }

    @Test
    public void expectedNormalBytesArray_getSettingsJsonSnapshotTest() throws SerializerException {
        RedirectorConfig config = new RedirectorConfig();
        when(redirectorConfigService.getRedirectorConfig()).thenReturn(config);

        byte[] bytes = offlineRedirectorHelper.getSettingsJsonSnapshot();

        Assert.assertTrue(bytes.length > 0);
    }

    @Test
    public void expectedZeroBytes_getNamespacedListJsonSnapshot() throws Exception {
        when(namespacedListsService.getAllNamespacedLists()).thenReturn(null);

        byte[] bytes = offlineRedirectorHelper.getNamespacedListJsonSnapshot();

        Assert.assertEquals(0, bytes.length);
    }

    @Test
    public void expectedNormalBytesArray_getNamespacedListJsonSnapshot() throws Exception {
        Namespaces namespaces = createNamespaces();
        when(namespacedListsService.getAllNamespacedLists()).thenReturn(namespaces);
        when(connector.getNodeVersion(any())).thenReturn(0);

        byte[] bytes = offlineRedirectorHelper.getNamespacedListJsonSnapshot();

        Assert.assertTrue(bytes.length > 0);
    }

    private Namespaces createNamespaces() {
        List<NamespacedList> listList = new ArrayList<>();

        listList.add(createNamespacedList("namespacedList1", 1));
        listList.add(createNamespacedList("namespacedList2", 2));
        listList.add(createNamespacedList("namespacedList3", 3));

        Namespaces namespaces = new Namespaces();
        namespaces.setNamespaces(listList);
        return namespaces;
    }

    private NamespacedList createNamespacedList(String name, long version) {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(name);
        namespacedList.setVersion(version);
        namespacedList.setValues(Collections.singleton(name));
        return namespacedList;
    }

}
