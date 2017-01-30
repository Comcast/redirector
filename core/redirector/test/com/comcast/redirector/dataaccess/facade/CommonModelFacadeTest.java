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


package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.dao.*;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommonModelFacadeTest {
    private static final String BASE_PATH = "/BASE_PATH";
    private static final String REST_PATH = "http://localhost:9000/";

    private IDataSourceConnector connector;
    private IDAOFactory daoFactory;

    private NamespacedListsDAO namespacedListDAO;
    private ISimpleDAO<RedirectorConfig> redirectorConfigDAO;

    private CommonModelRestFacade testee;
    private ZKConfig zkConfig;
    private IWebServiceClient webServiceClient = mock (IWebServiceClient.class);
    private IDataChangePoller dataChangePoller = mock (IDataChangePoller.class);

    @Before
    public void setUp() throws Exception {
        setupConnector();
        setupInternalDAOs();
        setupDAOFactory();
        zkConfig  = mock(ZKConfig.class);
        when(zkConfig.getRestBasePath()).thenReturn(REST_PATH);
        when(zkConfig.getStacksPollIntervalSeconds()).thenReturn(2);
        when(zkConfig.getModelPollIntervalSeconds()).thenReturn(2);
    }

    private void setupInternalDAOs() {
        namespacedListDAO = setupNamespacedListDAO();
        redirectorConfigDAO = setupSimpleDAO();
    }

    private void setupDAOFactory() {
        daoFactory = mock(IDAOFactory.class);

        when(daoFactory
            .getNamespacedListsDAO(EntityType.NAMESPACED_LIST, BaseDAO.COMPRESSED))
            .thenReturn((IListDAO) namespacedListDAO);

        when(daoFactory
            .getSimpleDAO(RedirectorConfig.class, EntityType.CONFIG, BaseDAO.NOT_COMPRESSED))
            .thenReturn(redirectorConfigDAO);
    }

    private NamespacedListsDAO setupNamespacedListDAO() {
        return mock(NamespacedListsDAO.class);
    }

    private ISimpleDAO setupSimpleDAO() {
        return mock(ISimpleDAO.class);
    }

    @Test
    public void testStartGlobalWhenConnected() throws Exception {
        setupSuccessfullyConnectingGlobalCache();

        testee.start();

        Assert.assertTrue(testee.isAvailable());
    }

    @Test
    public void testStartWhenNotConnected() throws Exception {
        setupConnector(false);
        testee = new CommonModelRestFacade(connector, daoFactory, dataChangePoller, webServiceClient, zkConfig);

        testee.start();

        Assert.assertFalse(testee.isAvailable());
    }

    private void setupSuccessfullyConnecting() throws InterruptedException {
        setupConnector(true);
        testee = new CommonModelRestFacade(connector, daoFactory, dataChangePoller, webServiceClient, zkConfig);
    }

    @Test
    public void testGetRedirectorConfigNotNull() throws Exception {
        setupSuccessfullyConnecting();
        RedirectorConfig redirectorConfig = new RedirectorConfig();
        redirectorConfig.setMinHosts(10);
        redirectorConfig.setAppMinHosts(15);
        setupExpectedResultForDAO(redirectorConfigDAO, redirectorConfig);
        assertEquals(redirectorConfig, testee.getRedirectorConfig());
    }

    @Test
    public void testGetRedirectorConfigNull() throws Exception {
        setupSuccessfullyConnecting();
        setupExpectedResultForDAO(redirectorConfigDAO, null);
        assertNull(testee.getRedirectorConfig());
    }

    @Test
    public void testGetNamespacedListNull() throws Exception {
        setupSuccessfullyConnectingGlobalCache();
        setupExpectedResultForWebServiceClient((NamespacedList) null);

        Assert.assertNull(testee.getNamespacedList("test"));
    }

    @Test
    public void testGetAllNamespacedLists() throws Exception {
        setupSuccessfullyConnectingGlobalCache();
        List<NamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(new NamespacedList());
        Namespaces namespaces = new Namespaces();
        namespaces.setNamespaces(namespacedLists);
        setupExpectedResultForWebServiceClient(namespaces);

        assertEquals(namespacedLists, testee.getAllNamespacedLists());
    }

    @Test
    public void testGetNamespacedListNotNull() throws Exception {
        setupSuccessfullyConnectingGlobalCache();
        NamespacedList namespacedList = new NamespacedList();
        setupExpectedResultForWebServiceClient(namespacedList);

        assertEquals(namespacedList, testee.getNamespacedList("test"));
    }

    private void setupConnector() {
        connector = mock(IDataSourceConnector.class);
        when(connector.getBasePath()).thenReturn(BASE_PATH);
    }

    private void setupConnector(boolean isConnected) throws InterruptedException {
        when(connector.blockUntilConnectedOrTimedOut()).thenReturn(isConnected);
    }

    private void setupSuccessfullyConnectingGlobalCache() throws InterruptedException {
        setupConnector(true);
        testee = new CommonModelRestFacade(connector, daoFactory, dataChangePoller, webServiceClient, zkConfig);
    }

    private <T> void setupExpectedResultForDAO(ISimpleDAO<T> dao, T result) throws DataSourceConnectorException {
        when(dao.get()).thenReturn(result);
    }

    private <T> void setupExpectedResultForWebServiceClient(T result) {
        when(webServiceClient.getRequest(any(), anyString())).thenReturn(result);
        when(webServiceClient.getRequest(any(), anyString())).thenReturn(result);
    }
}
