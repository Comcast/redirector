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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TransactionalDAOTest {
    private static final String ANY_SERVICE = "anyService";
    private static final String ANY_ID = "anyId";
    private static final String ANY_SERIALIZED_DATA = "serializedData";

    private IDataSourceConnector connector;
    private IRegisteringDAOFactory daoFactory;
    private Serializer serializer;

    private TransactionalDAO testee;

    @Before
    public void setUp() throws Exception {
        connector = mock(IDataSourceConnector.class);
        daoFactory = mock(IRegisteringDAOFactory.class);
        serializer = mock(Serializer.class);

        testee = new TransactionalDAO(connector, serializer, daoFactory);
    }

    @Test
    public void testCommitAndRebuildCache() throws Exception {
        ICacheableDAO mockCacheableDAO = mock(ICacheableDAO.class);
        IDataSourceConnector.Transaction mockTransaction = mock(IDataSourceConnector.Transaction.class);
        when(connector.createTransaction()).thenReturn(mockTransaction);
        when(serializer.serialize(anyObject(), anyBoolean())).thenReturn(ANY_SERIALIZED_DATA);
        when(daoFactory.getRegisteredCacheableDAO(any(EntityType.class))).thenReturn(mockCacheableDAO);

        ITransactionalDAO.ITransaction transaction = testee.beginTransaction();
        transaction.save(new IfExpression(), EntityType.RULE, ANY_SERVICE, ANY_ID);
        transaction.save(new Server(), EntityType.SERVER, ANY_SERVICE, ANY_ID);
        transaction.save(new Whitelisted(), EntityType.WHITELIST, ANY_SERVICE);
        transaction.operationByActionType(new IfExpression(), EntityType.URL_RULE, ANY_SERVICE, ANY_ID, ActionType.DELETE);
        transaction.incVersion(EntityType.MODEL_CHANGED, ANY_SERVICE);
        transaction.commit();

        verify(mockTransaction, times(1)).commit();
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testExceptionOnSerialize() throws Exception {
        when(serializer.serialize(anyObject(), anyBoolean())).thenReturn(null);

        ITransactionalDAO.ITransaction transaction = testee.beginTransaction();
        transaction.save(new IfExpression(), EntityType.RULE, ANY_SERVICE, ANY_ID);
    }
}
