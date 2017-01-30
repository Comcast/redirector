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

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.comcast.redirector.common.RedirectorConstants.NO_MODEL_NODE_VERSION;
import static org.mockito.Mockito.*;

public class ZkNodeCacheWrapperTest {

    private NodeCache cache;
    private IDataSourceConnector client;
    private ZkNodeCacheWrapper testee;

    @Before
    public void setUp() throws Exception {
        cache = mock(NodeCache.class);
        client = mock(IDataSourceConnector.class);
        when(client.isConnected()).thenReturn(true);
    }

    @Test(expected = DataSourceConnectorException.class)
    public void startCache_WithoutPreloading_Fails_WhenNoConnected() throws DataSourceConnectorException {
        when(client.isConnected()).thenReturn(false);
        testee = new ZkNodeCacheWrapper(client, "/test", cache);

        testee.start(false);
    }

    @Test(expected = DataSourceConnectorException.class)
    public void startCache_WithPreloading_Fails_WhenNoConnected() throws DataSourceConnectorException {
        when(client.isConnected()).thenReturn(false);
        testee = new ZkNodeCacheWrapper(client, "/test", cache);

        testee.start(true);
    }

    @Test
    public void testConstructWithCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);

        Assert.assertTrue(testee.isUseCache());
    }

    @Test
    public void testConstructWithoutCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);

        Assert.assertFalse(testee.isUseCache());
    }

    @Test
    public void testStartUseCacheBuildInitial() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        testee.start(true);

        verify(cache, times(1)).start(true);
    }

    @Test
    public void testStartUseCacheDoNotBuildInitial() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        testee.start(false);

        verify(cache, times(1)).start(false);
    }

    @Test
    public void testStartUseCacheExceptionHappens() throws Exception {
        setupCacheThrowsExceptionOnStart(new Exception());
        testee = new ZkNodeCacheWrapper(client, "/test", cache);

        testee.start(true);
    }

    @Test
    public void testStartUseCacheNoConnection() throws Exception {
        setupCacheThrowsExceptionOnStart(new KeeperException.ConnectionLossException());
        testee = new ZkNodeCacheWrapper(client, "/test", cache);

        testee.start(true);
    }

    @Test
    public void testStartNotUseCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        testee.start(true);

        verify(cache, never()).start(anyBoolean());
    }

    @Test
    public void testRebuildUseCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        testee.start(true);
        testee.rebuild();

        verify(cache, times(1)).rebuild();
    }

    @Test
    public void testRebuildUseCacheExceptionHappens() throws Exception {
        setupCacheThrowsExceptionOnRebuild(new Exception());
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        testee.start(true);

        testee.rebuild();
    }

    @Test
    public void testRebuildUseCacheNoConnection() throws Exception {
        setupCacheThrowsExceptionOnRebuild(new KeeperException.ConnectionLossException());
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        testee.start(true);

        testee.rebuild();
    }

    @Test
    public void testRebuildDoNotUseCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        testee.rebuild();

        verify(cache, never()).rebuild();
    }

    @Test
    public void testGetCurrentDataUseCacheWithData() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        when(cache.getCurrentData()).thenReturn(new ChildData("/test", null, "result".getBytes()));
        testee.start(true);

        byte[] result = testee.getCurrentData();

        Assert.assertEquals("result", new String(result));
    }

    @Test
    public void testGetCurrentDataUseCacheWithoutData() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        when(cache.getCurrentData()).thenReturn(null);

        byte[] result = testee.getCurrentData();

        Assert.assertNull(result);
    }

    @Test
    public void testGetCurrentDataDoNotUseCache() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        setupZookeeperConnectorReturnResult("result".getBytes());

        byte[] result = testee.getCurrentData();

        Assert.assertEquals("result", new String(result));
    }

    @Test
    public void returnNoVersion_WhenCacheHasNoData() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        when(cache.getCurrentData()).thenReturn(null);
        testee.start(true);

        int result = testee.getCurrentDataVersion();

        Assert.assertEquals(NO_MODEL_NODE_VERSION, result);
    }

    @Test
    public void returnVersion_WhenCacheData_AndCacheEnabled() throws Exception {
        int expectedVersion = 6;
        testee = new ZkNodeCacheWrapper(client, "/test", cache);
        Stat stat = mock(Stat.class);
        when(stat.getVersion()).thenReturn(expectedVersion);
        when(cache.getCurrentData()).thenReturn(new ChildData("/test", stat, "result".getBytes()));
        testee.start(true);

        int result = testee.getCurrentDataVersion();

        Assert.assertEquals(expectedVersion, result);
    }

    @Test
    public void returnVersion_WhenDataStoreHasData_AndCacheIsNotUsed() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        setupZookeeperConnectorReturnVersion(5);

        int result = testee.getCurrentDataVersion();

        Assert.assertEquals(5, result);
    }

    @Test
    public void returnNoVersion_WhenDataStoreHasNoData_AndCacheIsNotUsed() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        doThrow(new DataSourceConnectorException()).when(client).getNodeVersion(anyString());

        int result = testee.getCurrentDataVersion();

        Assert.assertEquals(NO_MODEL_NODE_VERSION, result);
    }

    @Test
    public void returnNoVersion_WhenDataStoreThrowsException_AndCacheIsNotUsed() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        setupZookeeperConnectorThrowsException(new DataSourceConnectorException());

        int result = testee.getCurrentDataVersion();

        Assert.assertEquals(NO_MODEL_NODE_VERSION, result);
    }

    @Test
    public void testGetCurrentDataDoNotUseCacheCuratorReturnsNull() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        setupZookeeperConnectorReturnResult(null);

        byte[] result = testee.getCurrentData();

        Assert.assertNull(result);
    }

    @Test
    public void testGetCurrentDataDoNotUseCacheCuratorThrowsException() throws Exception {
        testee = new ZkNodeCacheWrapper(client, "/test", null);
        setupZookeeperConnectorThrowsException(new DataSourceConnectorException());

        Assert.assertNull(testee.getCurrentData());
    }

    private void setupZookeeperConnectorReturnResult(byte[] result) throws Exception {
        when(client.getData(anyString())).thenReturn(result);
    }

    private void setupZookeeperConnectorReturnVersion(int version) throws Exception {
        when(client.isPathExists(anyString())).thenReturn(true);
        when(client.getNodeVersion(anyString())).thenReturn(version);
    }

    private void setupZookeeperConnectorThrowsException(Throwable throwable) throws Exception {
        when(client.getData(anyString())).thenThrow(throwable);
    }

    private void setupCacheThrowsExceptionOnStart(Throwable throwable) throws Exception {
        doThrow(throwable).when(cache).start(anyBoolean());
    }

    private void setupCacheThrowsExceptionOnRebuild(Throwable throwable) throws Exception {
        doThrow(throwable).when(cache).rebuild();
    }
}
