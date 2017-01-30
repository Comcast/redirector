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

import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.client.RedirectorNoConnectionToDataSourceException;
import com.comcast.redirector.dataaccess.client.RedirectorNoNodeInPathException;
import junit.framework.Assert;
import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ListServiceDAOTest extends BaseListDAOTest {
    static final String ANY_SERVICE = "anyService";
    private ListServiceDAO<TestModel> testee;

    @Parameterized.Parameters
    public static Collection<Object[]> compressed() {
        return Arrays.asList(new Object[]{true}, new Object[]{false});
    }

    public ListServiceDAOTest(boolean isCompressed) {
        super(isCompressed);
    }

    @Before
    public void setUp() throws Exception {
        doSetUp();
        testee = new ListServiceDAO<TestModel>(clazz, serializer, connector, pathHelper, isCompressed, ANY_USE_CACHE);
    }

    @Test
    public void testGetById() throws Exception {
        String id = "id";
        String service = "service";
        String resultSerialized = "testModel";
        setupCacheReturnOne(resultSerialized);
        setupDeserialize(resultSerialized, new TestModel(id));

        TestModel result = testee.getById(service, id);

        Assert.assertEquals(id, result.getId());
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testGetByIdExceptionHappens() throws Exception {
        setupExceptionOnGetOne(new RedirectorNoConnectionToDataSourceException(new Exception()));

        testee.getById(ANY_SERVICE, ANY_ID);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testGetByIdNoConnection() throws Exception {
        setupExceptionOnGetOne(new RedirectorNoConnectionToDataSourceException(new KeeperException.ConnectionLossException()));

        testee.getById(ANY_SERVICE, ANY_ID);
    }

    @Test
    public void testGetByIdCantDeserialize() throws Exception {
        String id = "id";
        String service = "service";
        String resultSerialized = "testModel";
        setupCacheReturnOne(resultSerialized);
        setupDeserializeThrowsException();

        TestModel result = testee.getById(service, id);

        Assert.assertNull(result);
    }

    @Test
    public void testGetAll() throws Exception {
        String id = "id";
        String service = "service";
        String resultSerialized = "testModel";
        setupCacheReturnMap(id, resultSerialized);
        setupDeserialize(resultSerialized, new TestModel(id));

        List<TestModel> result = testee.getAll(service);

        Assert.assertEquals(id, result.get(0).getId());
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testGetAllExceptionHappens() throws Exception {
        setupExceptionOnGetAll(new RedirectorDataSourceException(new Exception()));

        testee.getAll(ANY_SERVICE);
    }

    @Test(expected = RedirectorNoNodeInPathException.class)
    public void testGetAllNoConnection() throws Exception {
        setupExceptionOnGetAll(new RedirectorNoNodeInPathException(new KeeperException.ConnectionLossException()));

        testee.getAll(ANY_SERVICE);
    }

    @Test
    public void testGetAllCantSerialize() throws Exception {
        String id = "id";
        String resultSerialized = "testModel";
        setupCacheReturnMap(id, resultSerialized);
        setupDeserializeThrowsException();

        List<TestModel> result = testee.getAll(ANY_SERVICE);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testSaveById() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);

        testee.saveById(new TestModel("id"), ANY_SERVICE, ANY_ID);

        verifySave(resultSerialized, path);
        verify(getWrapper(), times(1)).rebuildNode(path);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testSaveByIdExceptionHappens() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorNoConnectionToDataSourceException(new Exception()));

        testee.saveById(new TestModel(ANY_ID), ANY_SERVICE, ANY_ID);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testSaveByIdNoConnection() throws Exception {
        String path = "/path";
        String resultSerialized = "testModel";
        setupPathHelperReturn(path);
        setupSerialize(resultSerialized);
        setupExceptionOnSave(new RedirectorNoConnectionToDataSourceException(new Exception()));

        testee.saveById(new TestModel(ANY_ID), ANY_SERVICE, ANY_ID);
    }

    @Test(expected = SerializerException.class)
    public void testSaveByIdCantSerialize() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupSerializeThrowsException();

        testee.saveById(new TestModel(ANY_ID), ANY_SERVICE, ANY_ID);
    }

    @Test
    public void testDeleteById() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);

        testee.deleteById(ANY_SERVICE, ANY_ID);

        verify(connector, times(1)).delete(path);
        verify(getWrapper(), times(1)).rebuild();
    }

    @Test(expected = RedirectorDataSourceException.class)
    public void testDeleteByIdExceptionHappens() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupExceptionOnDelete(new RedirectorDataSourceException(new Exception()));

        testee.deleteById(ANY_SERVICE, ANY_ID);
    }

    @Test(expected = RedirectorNoConnectionToDataSourceException.class)
    public void testDeleteByIdNoConnection() throws Exception {
        String path = "/path";
        setupPathHelperReturn(path);
        setupExceptionOnDelete(new RedirectorNoConnectionToDataSourceException(new Exception()));

        testee.deleteById(ANY_SERVICE, ANY_ID);
    }

    @Override
    protected void setupPathHelperReturn(String path) {
        when(pathHelper.getPathByService(anyString(), anyString())).thenReturn(path);
    }
}
