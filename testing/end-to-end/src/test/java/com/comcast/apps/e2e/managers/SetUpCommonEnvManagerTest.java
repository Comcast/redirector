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
package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.helpers.ServicePathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class SetUpCommonEnvManagerTest {

    private static final String SERVICE_NAME = "http://localhost:10540/redirectorWebService/data";

    private static final String NAMESPACE_NAME_1 = "Namespace01";
    private static final String NAMESPACE_NAME_2 = "Namespace02";
    private static final String NAMESPACE_VALUE_1 = "10.10.10.1";
    private static final String NAMESPACE_VALUE_2 = "10.10.10.2";

    private SetUpCommonEnvManager manager;

    private ServicePathHelper servicePathHelper;

    private Context context;

    private ServiceHelper serviceHelper;

    private RedirectorConfig redirectorConfig;

    private NamespacedListsBatch namespaces;

    private Map<String, Set<String>> namespacedMap = new HashMap<String, Set<String>>() {
        {
            put(NAMESPACE_NAME_1, Collections.singleton(NAMESPACE_VALUE_1));
            put(NAMESPACE_NAME_2, Collections.singleton(NAMESPACE_VALUE_2));
        }
    };

    @Before
    public void setUp() throws Exception {
        serviceHelper = mock(ServiceHelper.class);

        servicePathHelper = new ServicePathHelper(SERVICE_NAME);
        redirectorConfig = new RedirectorConfig();

        namespaces = new NamespacedListsBatch();
        namespaces.setNamespacedLists(namespacedMap);

        context = new Context(SERVICE_NAME);
        context.setServicePathHelper(servicePathHelper);
        context.setRedirectorConfig(redirectorConfig);
        context.setNamespaces(namespaces);
    }

    @Test
    public void setUpCommonEnvManagerVerifySaveConfigTest() throws Exception {
        manager = new SetUpCommonEnvManager(context, serviceHelper);
        manager.setUp();

        verifyRedirectorConfigPosted();
    }

    @Test
    public void setUpCommonEnvManagerVerifySaveNamespacesTest() throws Exception {
        manager = new SetUpCommonEnvManager(context, serviceHelper);
        manager.setUp();

        NamespacedList namespacedList1 = createNamespacedList(NAMESPACE_NAME_1, Collections.singleton(NAMESPACE_VALUE_1));
        NamespacedList namespacedList2 = createNamespacedList(NAMESPACE_NAME_2, Collections.singleton(NAMESPACE_VALUE_2));

        verifyNamespacesPosted(NAMESPACE_NAME_1, namespacedList1);
        verifyNamespacesPosted(NAMESPACE_NAME_2, namespacedList2);
    }

    @Test(expected = WebApplicationException.class)
    public void setUpCommonEnvManagerExceptionDuringSaveConfigTest() throws Exception {
        doThrow(new WebApplicationException(new Exception())).when(serviceHelper).post(servicePathHelper.getRedirectorConfigServicePath(), redirectorConfig, MediaType.APPLICATION_JSON);
        manager = new SetUpCommonEnvManager(context, serviceHelper);
        manager.setUp();
    }

    @Test(expected = WebApplicationException.class)
    public void setUpCommonEnvManagerExceptionDuringSaveNamespacesTest() throws Exception {
        doThrow(new WebApplicationException(new Exception())).when(serviceHelper).post(servicePathHelper.getNamespacedListPostOneNamespacedServicePath(NAMESPACE_NAME_2),
                createNamespacedList(NAMESPACE_NAME_2, Collections.singleton(NAMESPACE_VALUE_2)), MediaType.APPLICATION_JSON);
        manager = new SetUpCommonEnvManager(context, serviceHelper);
        manager.setUp();
    }

    private NamespacedList createNamespacedList(String namespacedListName, Set<String> values) {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(namespacedListName);
        namespacedList.setValues(values);
        return namespacedList;
    }

    private void verifyRedirectorConfigPosted() {
        verify(serviceHelper, times(1)).post(servicePathHelper.getRedirectorConfigServicePath(), redirectorConfig, MediaType.APPLICATION_JSON);
    }

    private void verifyNamespacesPosted(String namespacedName, NamespacedList namespacedList) throws IllegalAccessException, InstantiationException {
        verify(serviceHelper, times(1)).post(servicePathHelper.getNamespacedListPostOneNamespacedServicePath(namespacedName), namespacedList, MediaType.APPLICATION_JSON);
    }
}
