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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.client.RedirectorNoConnectionToDataSourceException;
import com.comcast.redirector.dataaccess.cache.INodeCacheWrapper;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.comcast.redirector.dataaccess.dao.BaseListDAOTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class SimpleDAOTest {
    private SimpleDAO<TestModel> testee;

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

    public SimpleDAOTest(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    @Before
    public void setUp() throws Exception {
        serializer = mock(Serializer.class);
        connector = mock(IDataSourceConnector.class);
        pathHelper = mock(IPathHelper.class);
        cacheWrapper = mock(INodeCacheWrapper.class);
        when(connector.newNodeCacheWrapper(anyString(), anyBoolean())).thenReturn(cacheWrapper);
        when(connector.newNodeCacheWrapper(anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(cacheWrapper);

        testee = new SimpleDAO<>(clazz, serializer, connector, pathHelper, isCompressed, ANY_USE_CACHE);
    }

    @Test
    public void testGet() throws Exception {
        String id = "id";
        String resultSerialized = "testModel";
        setupCacheReturnResult(resultSerialized);
        setupDeserialize(resultSerialized, new TestModel(id));

        TestModel result = testee.get();

        assertEquals(id, result.getId());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetExceptionHappens() throws Exception {
        exception.expect(RedirectorDataSourceException.class);
        setupExceptionOnGetOne(new RedirectorDataSourceException("Test"));
        testee.get();
    }

    @Test
    public void testGetNoConnection() throws Exception {
        exception.expect(RedirectorNoConnectionToDataSourceException.class);
        setupExceptionOnGetOne(new RedirectorNoConnectionToDataSourceException(new KeeperException.ConnectionLossException()));
        testee.get();
    }

    @Test
    public void testGetCantDeserialize() throws Exception {
        String resultSerialized = "testModel";
        setupCacheReturnResult(resultSerialized);
        setupDeserializeThrowsException();
        TestModel result = testee.get();

        assertNull(result);
    }

    @Test
    public void testSave() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);

        testee.save(new TestModel(ANY_ID));

        verifySave(resultSerialized, path);
    }

    @Test
    public void testSaveExceptionHappens() throws Exception {
        exception.expect(RedirectorDataSourceException.class);
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorDataSourceException("Test"));

        testee.save(new TestModel(ANY_ID));
    }

    @Test
    public void testSaveNoConnection() throws Exception {
        exception.expect(RedirectorDataSourceException.class);
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorDataSourceException(new Exception()));

        testee.save(new TestModel(ANY_ID));
    }

    @Test
    public void testSaveCantSerialize() throws Exception {
        exception.expect(SerializerException.class);
        String path = "/path";
        setupPathHelperReturn(path);
        setupSerializeThrowsException();

        testee.save(new TestModel(ANY_ID));
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
        when(pathHelper.getPath()).thenReturn(path);
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

    private void verifySave(String resultSerialized, String path) throws DataSourceConnectorException {
        if (isCompressed) {
            verify(connector, times(1)).saveCompressed(resultSerialized, path);
        } else {
            verify(connector, times(1)).save(resultSerialized, path);
        }
    }
}
