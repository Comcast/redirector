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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.PathHelper;
import org.junit.Assert;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ZkPathChildrenCacheWrapperTest {
    private PathChildrenCache cache;
    private IDataSourceConnector client;
    private ZkPathChildrenCacheWrapper testee;

    @Before
    public void setUp() throws Exception {
        cache = mock(PathChildrenCache.class);
        client = mock(IDataSourceConnector.class);
        when(client.isConnected()).thenReturn(true);
    }

    @Test(expected = DataSourceConnectorException.class)
    public void startCache_WithoutPreloading_Fails_WhenNoConnected() throws DataSourceConnectorException {
        when(client.isConnected()).thenReturn(false);
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);

        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;
        testee.start(startMode);
    }

    @Test(expected = DataSourceConnectorException.class)
    public void startCache_WithPreloading_Fails_WhenNoConnected() throws DataSourceConnectorException {
        when(client.isConnected()).thenReturn(false);
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);

        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.BUILD_INITIAL_CACHE;
        testee.start(startMode);
    }

    @Test
    public void testConstructWithCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);

        Assert.assertTrue(testee.isUseCache());
    }

    @Test
    public void testConstructWithoutCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);

        Assert.assertFalse(testee.isUseCache());
    }

    @Test
    public void testStartWithUseCache() throws Exception {
        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;

        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(startMode);

        verify(cache, times(1)).start(startMode);
    }

    @Test
    public void testStartWithUseCacheExceptionHappens() throws Exception {
        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;
        setupCacheThrowsExceptionOnStart(new Exception());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);

        testee.start(startMode);
    }

    @Test
    public void testStartWithUseCacheNoConnection() throws Exception {
        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;
        setupCacheThrowsExceptionOnStart(new KeeperException.ConnectionLossException());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);

        testee.start(startMode);
    }

    @Test
    public void testStartWithDoNotUseCache() throws Exception {
        PathChildrenCache.StartMode startMode = PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;

        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        testee.start(startMode);

        verify(cache, never()).start(any(PathChildrenCache.StartMode.class));
    }

    @Test
    public void testRebuildUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        testee.rebuild();

        verify(cache, times(1)).rebuild();
    }

    @Test
    public void testRebuildUseCacheExceptionHappens() throws Exception {
        setupCacheThrowsExceptionOnRebuild(new Exception());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        testee.rebuild();
    }

    @Test
    public void testRebuildUseCacheNoConnection() throws Exception {
        setupCacheThrowsExceptionOnRebuild(new Exception());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        testee.rebuild();
    }

    @Test
    public void testRebuildNodeUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        testee.rebuildNode("/test");

        verify(cache, times(1)).rebuildNode("/test");
    }

    @Test
    public void testRebuildNodeUseCacheExceptionHappens() throws Exception {
        setupCacheThrowsExceptionOnRebuildNode(new Exception());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        testee.rebuildNode("/test");
    }

    @Test
    public void testRebuildNodeUseCacheNoConnection() throws Exception {
        setupCacheThrowsExceptionOnRebuildNode(new KeeperException.ConnectionLossException());
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        testee.rebuildNode("/test");
    }

    @Test
    public void testRebuildDoNotUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        testee.rebuild();

        verify(cache, never()).rebuild();
    }

    @Test
    public void testRebuildNodeDoNotUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        testee.rebuildNode("/test");

        verify(cache, never()).rebuildNode(anyString());
    }

    @Test
    public void testGetNodeIdToDataMapUseCache() throws Exception {
        String key = "test";
        byte[] value = "result".getBytes();

        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        when(cache.getCurrentData()).thenReturn(Collections.singletonList(new ChildData("/test", null, value)));
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        Map<String, byte[]> result = testee.getNodeIdToDataMap();

        Assert.assertEquals(key, result.keySet().toArray()[0]);
        Assert.assertEquals(new String(value), new String((byte[])result.values().toArray()[0]));
    }

    @Test
    public void testGetNodeIdToDataMapDoNotUseCacheDataIsNotCompressed() throws Exception {
        String key = "test";
        byte[] value = "result".getBytes();
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        setupZookeeperClientReturnResult("/test", key, value, false);

        Map<String, byte[]> result = testee.getNodeIdToDataMap();

        Assert.assertEquals(key, result.keySet().toArray()[0]);
        Assert.assertEquals(new String(value), new String((byte[])result.values().toArray()[0]));
    }

    @Test
    public void testGetNodeIdToDataMapDoNotUseCacheDataIsCompressed() throws Exception {
        String key = "test";
        byte[] value = "result".getBytes();
        testee = new ZkPathChildrenCacheWrapper(client, "/test", true, null);
        setupZookeeperClientReturnResult("/test", key, value, true);

        Map<String, byte[]> result = testee.getNodeIdToDataMap();

        Assert.assertEquals(key, result.keySet().toArray()[0]);
        Assert.assertEquals(new String(value), new String((byte[])result.values().toArray()[0]));
    }

    @Test
    public void testGetNodeIdToDataMapDoNotUseCacheNoChildren() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", true, null);
        when(client.getChildren("/test")).thenReturn(Collections.<String>emptyList());

        Map<String, byte[]> result = testee.getNodeIdToDataMap();

        Assert.assertEquals(0, result.size());
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testGetNodeIdToDataMapDoNotUseCacheException() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", true, null);
        when(client.getChildren("/test")).thenThrow(new RedirectorDataSourceException("test"));

        testee.getNodeIdToDataMap();
    }

    @Test
    public void testGetCurrentDataUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        when(cache.getCurrentData(anyString())).thenReturn(new ChildData("/test", null, "result".getBytes()));
        testee.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        byte[] result = testee.getCurrentData("/test");

        Assert.assertEquals("result", new String(result));
    }

    @Test
    public void testGetCurrentDataUseCacheNullData() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, cache);
        when(cache.getCurrentData(anyString())).thenReturn(null);

        byte[] result = testee.getCurrentData("/test");

        Assert.assertNull(result);
    }

    @Test
    public void testGetCurrentDataDoNotUseCache() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        setupZookeeperClientReturnResult("/test", "result".getBytes(), false);

        byte[] result = testee.getCurrentData("/test");

        Assert.assertEquals("result", new String(result));
    }

    @Test
    public void testGetCurrentDataDoNotUseCacheCompressed() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", true, null);
        setupZookeeperClientReturnResult("/test", "result".getBytes(), true);

        byte[] result = testee.getCurrentData("/test");

        Assert.assertEquals("result", new String(result));
    }

    @Test
    public void testGetCurrentDataDoNotUseCacheNullData() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        setupZookeeperClientReturnResult("/test", null, false);

        byte[] result = testee.getCurrentData("/test");

        Assert.assertNull(result);
    }

    @Test
    public void testGetCurrentDataDoNotUseCacheThrowsException() throws Exception {
        testee = new ZkPathChildrenCacheWrapper(client, "/test", false, null);
        setupCuratorThrowsException(new DataSourceConnectorException());

        Assert.assertNull(testee.getCurrentData("/test"));
    }

    private void setupZookeeperClientReturnResult(String path, String key, byte[] value, boolean isCompressed) throws Exception {
        String fullPath = path + PathHelper.DELIMETER + key;
        when(client.getChildren(path)).thenReturn(Collections.singletonList(key));
        if (isCompressed) {
            when(client.getDataDecompressed(fullPath)).thenReturn(value);
        } else {
            when(client.getData(fullPath)).thenReturn(value);
        }
    }

    private void setupZookeeperClientReturnResult(String path, byte[] result, boolean isCompressed) throws Exception {
        if (isCompressed) {
            when(client.getDataDecompressed(path)).thenReturn(result);
        } else {
            when(client.getData(path)).thenReturn(result);
        }
    }

    private void setupCuratorThrowsException(Throwable throwable) throws Exception {
        when(client.getData(anyString())).thenThrow(throwable);
    }

    private void setupCacheThrowsExceptionOnStart(Throwable throwable) throws Exception {
        doThrow(throwable).when(cache).start(any(PathChildrenCache.StartMode.class));
    }

    private void setupCacheThrowsExceptionOnRebuild(Throwable throwable) throws Exception {
        doThrow(throwable).when(cache).rebuild();
    }

    private void setupCacheThrowsExceptionOnRebuildNode(Throwable throwable) throws Exception {
        doThrow(throwable).when(cache).rebuildNode(anyString());
    }
}
