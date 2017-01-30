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

import org.apache.curator.framework.api.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ZookeeperConnectorTest extends ZookeeperConnectorTestBase {

    private GetDataBuilder getDataBuilder;
    private GetChildrenBuilder getChildrenBuilder;
    private SetDataBuilder setDataBuilder;
    private CreateBuilder createBuilder;
    private SetDataBackgroundVersionable setDataCompressible;
    private CreateBackgroundModeACLable createCompressible;
    private DeleteBuilder deleteBuilder;
    private BackgroundVersionable deleteChildren;

    @Before
    public void setUp() throws Exception {
        setupClient();
        getDataBuilder = mock(GetDataBuilder.class);
        getChildrenBuilder = mock(GetChildrenBuilder.class);

        setDataBuilder = mock(SetDataBuilder.class);
        setDataCompressible = mock(SetDataBackgroundVersionable.class);
        createCompressible = mock(CreateBackgroundModeACLable.class);
        createBuilder = mock(CreateBuilder.class);
        deleteBuilder = mock(DeleteBuilder.class);
        deleteChildren = mock(BackgroundVersionable.class);

        when(client.getData()).thenReturn(getDataBuilder);
        when(client.getChildren()).thenReturn(getChildrenBuilder);
        when(client.setData()).thenReturn(setDataBuilder);
        when(client.create()).thenReturn(createBuilder);
        when(setDataBuilder.compressed()).thenReturn(setDataCompressible);
        when(createBuilder.compressed()).thenReturn(createCompressible);
        when(client.delete()).thenReturn(deleteBuilder);
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(deleteChildren);

        initZookeeperConnector();
    }

    @Test
    public void testIsZNodeExists() throws Exception {
        String existingPath = "/test/NodeA";
        setupCheckExists(existingPath, true);
        String nonExistingPath = "/test/NodeB";
        setupCheckExists(nonExistingPath, false);

        Assert.assertTrue(testee.isPathExists(existingPath));
        Assert.assertFalse(testee.isPathExists(nonExistingPath));
    }

    @Test
    public void testGetChildren() throws Exception {
        List<String> children = Arrays.asList("child1", "child2", "child3");
        List<String> expected = Arrays.asList("child1", "child2", "child3");
        setupGetChildren("/path", children);

        List<String> result = testee.getChildren("/path");

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGetZookeeperBasePath() throws Exception {
        testee = new ZookeeperConnector(client, "/test", false, pathCreator, stackCache, nodeCacheFactory, pathChildrenCacheFactory);

        Assert.assertEquals("/test", testee.getBasePath());
    }

    @Test
    public void testGetData() throws Exception {
        setupGetData("/path", "success".getBytes());

        byte[] result = testee.getData("/path");

        Assert.assertEquals("success", new String(result));
    }

    @Test
    public void testSaveForExistingNode() throws Exception {
        setupCheckExists("/path", true);

        testee.save("success", "/path");

        verify(pathCreator, times(1)).createPath("/path");
        verify(setDataBuilder, times(1)).forPath("/path", "success".getBytes());
    }

    @Test
    public void testSaveForNonExistingNode() throws Exception {
        setupCheckExists("/path", false);

        testee.save("success", "/path");

        verify(pathCreator, times(1)).createPath("/path");
        verify(createBuilder, times(1)).forPath("/path", "success".getBytes());
    }

    @Test
    public void testSaveForExistingNodeCompressed() throws Exception {
        setupCheckExists("/path", true);

        testee.saveCompressed("success", "/path");

        verify(pathCreator, times(1)).createPath("/path");
        verify(setDataCompressible, times(1)).forPath("/path", "success".getBytes());
    }

    @Test
    public void testSaveForNonExistingNodeCompressed() throws Exception {
        setupCheckExists("/path", false);

        testee.saveCompressed("success", "/path");

        verify(pathCreator, times(1)).createPath("/path");
        verify(createCompressible, times(1)).forPath("/path", "success".getBytes());
    }

    @Test
    public void testDeleteWithChildren() throws Exception {
        testee.deleteWithChildren("/path");

        verify(deleteChildren, times(1)).forPath("/path");
    }

    @Test
    public void testDelete() throws Exception {
        testee.delete("/path");

        verify(deleteBuilder, times(1)).forPath("/path");
    }

    private void setupGetChildren(String input, List<String> expected) throws Exception {
        when(getChildrenBuilder.forPath(eq(input))).thenReturn(expected);
    }

    private void setupGetData(String input, byte[] expected) throws Exception {
        when(getDataBuilder.forPath(eq(input))).thenReturn(expected);
    }
}
