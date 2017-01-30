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

import com.comcast.redirector.dataaccess.cache.INodeCacheWrapper;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.*;
import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.comcast.redirector.dataaccess.dao.BaseListDAOTest.*;
import static com.comcast.redirector.dataaccess.dao.ListServiceDAOTest.ANY_SERVICE;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class SimpleServiceDAOTest {
    private SimpleServiceDAO<TestModel> testee;

    @Parameterized.Parameters
    public static Collection<Object[]> compressed() {
        return Arrays.asList(new Object[]{true}, new Object[]{false});
    }

    private boolean isCompressed;
    private Class clazz = TestModel.class;
    private Serializer serializer;
    private IDataSourceConnector connector;
    private IPathHelper pathHelper;
    private INodeCacheWrapper cacheWrapper;

    public SimpleServiceDAOTest(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    @Before
    public void setUp() throws Exception {
        serializer = mock(Serializer.class);
        connector = mock(IDataSourceConnector.class);
        pathHelper = mock(IPathHelper.class);
        cacheWrapper = mock(INodeCacheWrapper.class);

        when(connector.newNodeCacheWrapper(anyString(), anyBoolean()))
                .thenReturn(cacheWrapper);
        when(connector.newNodeCacheWrapper(anyString(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(cacheWrapper);
        testee = new SimpleServiceDAO<>(clazz, serializer, connector, pathHelper, isCompressed, ANY_USE_CACHE);
    }

    @Test
    public void testGet() throws Exception {
        String id = "id";
        String resultSerialized = "testModel";
        setupCacheReturnResult(resultSerialized);
        setupDeserialize(resultSerialized, new TestModel(id));

        TestModel result = testee.get(ANY_SERVICE);

        Assert.assertEquals(id, result.getId());
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testGetExceptionHappens() throws Exception {
        setupExceptionOnGetOne(new RedirectorDataSourceException(new Exception()));

        testee.get(ANY_SERVICE);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testGetNoConnection() throws Exception {
        setupExceptionOnGetOne(new RedirectorNoConnectionToDataSourceException(new KeeperException.ConnectionLossException()));

        testee.get(ANY_SERVICE);
    }

    @Test
    public void testGetCantDeserialize() throws Exception {
        String resultSerialized = "testModel";
        setupCacheReturnResult(resultSerialized);
        setupDeserializeThrowsException();

        TestModel result = testee.get(ANY_SERVICE);

        Assert.assertNull(result);
    }

    @Test
    public void testSave() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);

        testee.save(new TestModel(ANY_ID), ANY_SERVICE);

        verifySave(resultSerialized, path);
        verify(cacheWrapper, times(1)).rebuild();
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testSaveExceptionHappens() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorDataSourceException(new Exception()));

        testee.save(new TestModel(ANY_ID), ANY_SERVICE);
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testSaveNoConnection() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorDataSourceException(new Exception()));

        testee.save(new TestModel(ANY_ID), ANY_SERVICE);
    }

    @Test(expected = SerializerException.class)
    public void testSaveCantSerialize() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupSerializeThrowsException();

        testee.save(new TestModel(ANY_ID), ANY_SERVICE);
    }

    @Test
    public void testDelete() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);

        testee.delete(ANY_SERVICE);

        verify(connector, times(1)).delete(path);
        verify(cacheWrapper, times(1)).rebuild();
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testDeleteExceptionHappens() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupExceptionOnDelete(new RedirectorDataSourceException(new Exception()));

        testee.delete(ANY_SERVICE);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testDeleteNoConnection() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupExceptionOnDelete(new RedirectorNoConnectionToDataSourceException(new Exception()));

        testee.delete(ANY_SERVICE);
    }

    @Test
    public void testGetObjectVersion() throws Exception {
        int version = 5;
        String path = "/path";
        setupPathHelperReturn(path);
        setupReturnVersion(version);

        int result = testee.getObjectVersion(ANY_SERVICE);

        Assert.assertEquals(version, result);
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testGetObjectVersionExceptionHappens() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupVersionThrowsException(new RedirectorDataSourceException(new Exception()));

        testee.getObjectVersion(ANY_SERVICE);
    }

    @Test(expected = RedirectorNoNodeInPathException.class)
    public void testGetObjectVersionNoConnection() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupVersionThrowsException(new RedirectorNoNodeInPathException(new KeeperException.ConnectionLossException()));

        testee.getObjectVersion(ANY_SERVICE);
    }

    private void setupCacheReturnResult(String result) throws DataSourceConnectorException {
        when(cacheWrapper.getCurrentData()).thenReturn(result.getBytes());
    }

    private void setupDeserialize(String from, TestModel to) throws SerializerException {
        when(serializer.deserialize(from, clazz)).thenReturn(to);
    }

    private void setupDeserializeThrowsException() throws SerializerException {
        doThrow(new SerializerException("test", new Exception())).when(serializer).deserialize(anyString(), eq(clazz));
    }

    private void setupSerialize(String to) throws SerializerException {
        when(serializer.serialize(anyObject(), anyBoolean())).thenReturn(to);
    }

    private void setupPathHelperReturn(String path) {
        when(pathHelper.getPathByService(anyString())).thenReturn(path);
    }

    private void setupExceptionOnGetOne(Throwable throwable) throws Exception {
        doThrow(throwable).when(cacheWrapper).getCurrentData();
    }

    private void setupExceptionOnSave(Throwable throwable) throws Exception {
        if (isCompressed)
            doThrow(throwable).when(connector).saveCompressed(anyString(), anyString());
        else
            doThrow(throwable).when(connector).save(anyString(), anyString());
    }

    private void setupSerializeThrowsException() throws SerializerException {
        doThrow(new SerializerException("test", new Exception())).when(serializer).serialize(anyObject(), anyBoolean());
    }

    private void setupExceptionOnDelete(Throwable throwable) throws Exception {
        doThrow(throwable).when(connector).delete(anyString());
    }

    private void setupReturnVersion(int version) throws DataSourceConnectorException {
        when(cacheWrapper.getCurrentDataVersion()).thenReturn(version);
    }

    private void setupVersionThrowsException(Throwable throwable) throws DataSourceConnectorException {
        doThrow(throwable).when(cacheWrapper).getCurrentDataVersion();
    }

    private void verifySave(String resultSerialized, String path) throws DataSourceConnectorException {
        if (isCompressed) {
            verify(connector, times(1)).saveCompressed(resultSerialized, path);
        } else {
            verify(connector, times(1)).save(resultSerialized, path);
        }
    }
}
