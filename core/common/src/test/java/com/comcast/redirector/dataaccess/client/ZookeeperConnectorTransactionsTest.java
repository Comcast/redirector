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

package com.comcast.redirector.dataaccess.client;

import org.apache.curator.framework.api.transaction.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ZookeeperConnectorTransactionsTest extends ZookeeperConnectorTestBase {
    private CuratorTransaction transaction;
    private TransactionSetDataBuilder transactionSetDataBuilder;
    private TransactionCreateBuilder transactionCreateBuilder;
    private TransactionDeleteBuilder transactionDeleteBuilder;
    private CuratorTransactionBridge curatorTransactionBridge;
    private CuratorTransactionFinal curatorTransactionFinal;

    @Before
    public void setUp() throws Exception {
        setupClient();

        transaction = mock(CuratorTransaction.class);
        transactionSetDataBuilder = mock(TransactionSetDataBuilder.class);
        curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        transactionDeleteBuilder = mock(TransactionDeleteBuilder.class);

        when(transaction.setData()).thenReturn(transactionSetDataBuilder);
        when(transaction.create()).thenReturn(transactionCreateBuilder);
        when(transaction.delete()).thenReturn(transactionDeleteBuilder);
        when(transactionSetDataBuilder.forPath(anyString(), any(byte[].class))).thenReturn(curatorTransactionBridge);
        when(transactionCreateBuilder.forPath(anyString(), any(byte[].class))).thenReturn(curatorTransactionBridge);
        when(transactionCreateBuilder.forPath(anyString())).thenReturn(curatorTransactionBridge);
        when(transactionDeleteBuilder.forPath(anyString())).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);

        initZookeeperConnector();
    }

    @Test
    public void testSaveInTransactionInitiateTransactionExistingNode() throws Exception {
        setupCheckExists("/path", true);
        when(client.inTransaction()).thenReturn(transaction);

        ZookeeperConnector.ZookeeperTransaction transaction =
            (ZookeeperConnector.ZookeeperTransaction) testee.createTransaction();
        transaction.save("success", "/path");

        CuratorTransaction result = transaction.getTransaction();
        verify(transactionSetDataBuilder, times(1)).forPath("/path", "success".getBytes());
        Assert.assertEquals(curatorTransactionFinal, result);
    }

    @Test
    public void testSaveInTransactionInitiateTransactionNewNode() throws Exception {
        setupCheckExists("/path/node1", false);
        setupCheckExists("/path", false);
        when(client.inTransaction()).thenReturn(transaction);
        when(curatorTransactionFinal.create()).thenReturn(transactionCreateBuilder);

        ZookeeperConnector.ZookeeperTransaction transaction =
            (ZookeeperConnector.ZookeeperTransaction) testee.createTransaction();
        transaction.save("success", "/path/node1");

        CuratorTransaction result = transaction.getTransaction();
        verify(transactionCreateBuilder, times(1)).forPath("/path");
        verify(transactionCreateBuilder, times(1)).forPath("/path/node1", "success".getBytes());
        Assert.assertEquals(curatorTransactionFinal, result);
    }

    @Test
    public void testDeleteInTransactionInitiateTransaction() throws Exception {
        when(client.inTransaction()).thenReturn(transaction);

        ZookeeperConnector.ZookeeperTransaction transaction =
            (ZookeeperConnector.ZookeeperTransaction) testee.createTransaction();
        transaction.delete("/path/node1");

        CuratorTransaction result = transaction.getTransaction();
        verify(transactionDeleteBuilder, times(1)).forPath("/path/node1");
        Assert.assertEquals(curatorTransactionFinal, result);
    }

    @Test
    public void testCommitSuccessful() throws Exception {
        when(client.inTransaction()).thenReturn(transaction);

        ZookeeperConnector.ZookeeperTransaction transaction =
            (ZookeeperConnector.ZookeeperTransaction) testee.createTransaction();
        transaction.delete("/path/node1");
        transaction.commit();

        verify(curatorTransactionFinal, times(1)).commit();
    }
}
