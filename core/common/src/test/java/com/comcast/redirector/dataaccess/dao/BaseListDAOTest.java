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

import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.cache.factory.IPathChildrenCacheFactory;
import com.comcast.redirector.dataaccess.cache.ZkPathChildrenCacheWrapper;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import org.apache.curator.framework.CuratorFramework;

import java.util.HashMap;
import java.util.concurrent.ThreadFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BaseListDAOTest {
    protected static final String ANY_ID = "anyId";
    protected static final boolean ANY_USE_CACHE = false;
    protected static final boolean ANY_FAIL_WHEN_NO_CONNECTION = false;

    protected boolean isCompressed;
    protected Class clazz = TestModel.class;
    protected Serializer serializer;
    protected CuratorFramework curatorFramework;
    protected IDataSourceConnector connector;
    protected IPathChildrenCacheFactory cacheFactory;
    protected IPathHelper pathHelper;

    private ZkPathChildrenCacheWrapper compressedCacheWrapper;
    private ZkPathChildrenCacheWrapper nonCompressedCacheWrapper;

    public BaseListDAOTest(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    protected void doSetUp() {
        serializer = mock(Serializer.class);
        curatorFramework = mock(CuratorFramework.class);
        connector = mock(IDataSourceConnector.class);
        cacheFactory = mock(IPathChildrenCacheFactory.class);
        pathHelper = mock(IPathHelper.class);

        setupCacheWrapperMocks();
    }

    protected void setupCacheReturnOne(String result) throws DataSourceConnectorException {
        setupCacheWrapperReturnOne(getWrapper(), result);
    }

    protected void setupCacheReturnMap(String resultKey, String resultValue) throws DataSourceConnectorException {
        setupCacheWrapperReturnMap(getWrapper(), resultKey, resultValue);
    }

    protected ZkPathChildrenCacheWrapper getWrapper() {
        return (isCompressed) ? compressedCacheWrapper : nonCompressedCacheWrapper;
    }

    protected static void setupCacheWrapperReturnMap(ZkPathChildrenCacheWrapper mock, final String resultKey, final String resultValue) throws DataSourceConnectorException {
        when(mock.getNodeIdToDataMap()).thenReturn(new HashMap<String, byte[]>() {{
            put(resultKey, resultValue.getBytes());
        }});
    }

    protected static void setupCacheWrapperReturnOne(ZkPathChildrenCacheWrapper mock, String result) throws DataSourceConnectorException {
        when(mock.getCurrentData(anyString())).thenReturn(result.getBytes());
    }

    protected void setupPathHelperReturn(String path) {
        when(pathHelper.getPath(anyString())).thenReturn(path);
    }

    protected void setupSerialize(String to) throws SerializerException {
        when(serializer.serialize(anyObject(), anyBoolean())).thenReturn(to);
    }

    protected void setupDeserialize(String from, TestModel to) throws SerializerException {
        when(serializer.deserialize(from, clazz)).thenReturn(to);
    }

    protected void setupSerializeThrowsException() throws SerializerException {
        doThrow(new SerializerException("test", new Exception())).when(serializer).serialize(anyObject(), anyBoolean());
    }

    protected void setupDeserializeThrowsException() throws SerializerException {
        doThrow(new SerializerException("test", new Exception())).when(serializer).deserialize(anyString(), eq(clazz));
    }

    protected void setupCacheWrapperMocks() {
        compressedCacheWrapper = mock(ZkPathChildrenCacheWrapper.class);
        nonCompressedCacheWrapper = mock(ZkPathChildrenCacheWrapper.class);
        when(connector.newPathChildrenCacheWrapper(anyString(), anyBoolean(), any(ThreadFactory.class), anyBoolean()))
                .thenReturn(compressedCacheWrapper);
        when(connector.newPathChildrenCacheWrapper(anyString(), anyBoolean()))
                .thenReturn(nonCompressedCacheWrapper);
    }

    protected void setupExceptionOnGetOne(Throwable throwable) throws Exception {
        doThrow(throwable).when(getWrapper()).getCurrentData(anyString());
    }

    protected void setupExceptionOnGetAll(Throwable throwable) throws Exception {
        doThrow(throwable).when(getWrapper()).getNodeIdToDataMap();
    }

    protected void setupExceptionOnSave(Throwable throwable) throws Exception {
        if (isCompressed)
            doThrow(throwable).when(connector).saveCompressed(anyString(), anyString());
        else
            doThrow(throwable).when(connector).save(anyString(), anyString());
    }

    protected void verifySave(String resultSerialized, String path) throws DataSourceConnectorException {
        if (isCompressed) {
            verify(connector, times(1)).saveCompressed(resultSerialized, path);
        } else {
            verify(connector, times(1)).save(resultSerialized, path);
        }
    }

    protected void setupExceptionOnDelete(Throwable throwable) throws Exception {
        doThrow(throwable).when(connector).delete(anyString());
    }
}
