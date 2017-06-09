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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.NoNodeInZookeeperException;
import com.comcast.redirector.dataaccess.cache.IStacksCache;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.dataaccess.cache.ZKPathHelperConstants.STACKS_PATH;
import static org.mockito.Mockito.*;

@Ignore
public class StacksDAOTest {
    private IStacksCache stacksCache;
    private IDataSourceConnector connector;
    private ServiceDiscoveryHostDeserializer hostsSerializer;

    @Before
    public void setUp() throws Exception {
        stacksCache = mock(IStacksCache.class);
        connector = mock(IDataSourceConnector.class);
        hostsSerializer = mock(ServiceDiscoveryHostDeserializer.class);
        when(connector.getStacksCache()).thenReturn(stacksCache);
    }

    @Test
    public void testGetAllStackPaths() throws Exception {
        when(connector.getBasePath()).thenReturn("");
        setupCacheResult(STACKS_PATH, "PO", "BR");
        setupCacheResult(STACKS_PATH + "/PO", "POC1", "POC2");
        setupCacheResult(STACKS_PATH + "/PO/POC1", "1.40");
        setupCacheResult(STACKS_PATH + "/PO/POC1/1.40", "xreGuide");
        setupCacheResult(STACKS_PATH + "/PO/POC2", "1.40", "1.41");
        setupCacheResult(STACKS_PATH + "/PO/POC2/1.40", "xreGuide", "pandora");
        setupCacheResult(STACKS_PATH + "/PO/POC2/1.41", "xreGuide", "sports");
        setupCacheResult(STACKS_PATH + "/BR", "BRC1", "BRC2");
        setupCacheResult(STACKS_PATH + "/BR/BRC2", "1.41", "1.42");
        setupCacheResult(STACKS_PATH + "/BR/BRC2/1.41", "xreGuide");

        StacksDAO testee = new StacksDAO(connector);
        XreStackPath[] result = testee.getAllStackPaths().toArray(new XreStackPath[]{});

        Assert.assertEquals("/PO/POC1/1.40/xreGuide", result[0].getPath());
        Assert.assertEquals("/PO/POC2/1.40/xreGuide", result[1].getPath());
        Assert.assertEquals("/PO/POC2/1.40/pandora", result[2].getPath());
        Assert.assertEquals("/PO/POC2/1.41/xreGuide", result[3].getPath());
        Assert.assertEquals("/PO/POC2/1.41/sports", result[4].getPath());
        Assert.assertEquals("/BR/BRC2/1.41/xreGuide", result[5].getPath());
    }

    @Test
    public void testGetAllStackPathsWithBasePath() throws Exception {
        when(connector.getBasePath()).thenReturn("/base");
        setupCacheResult("/base" + STACKS_PATH, "PO", "BR");
        setupCacheResult("/base" + STACKS_PATH + "/PO", "POC1", "POC2");
        setupCacheResult("/base" + STACKS_PATH + "/PO/POC1", "1.40");
        setupCacheResult("/base" + STACKS_PATH + "/PO/POC1/1.40", "xreGuide");

        StacksDAO testee = new StacksDAO(connector);
        XreStackPath[] result = testee.getAllStackPaths().toArray(new XreStackPath[]{});

        Assert.assertEquals("/PO/POC1/1.40/xreGuide", result[0].getPath());
    }

    @Test
    public void testGetHosts() throws Exception {
        String path = "/PO/POC1/1.40/xreGuide";
        when(connector.getBasePath()).thenReturn("");
        setupGetChildrenResult(STACKS_PATH + path, Arrays.asList("host1", "host2"));
        setupGetDataResult(STACKS_PATH + path + "/host1", "host1Data");
        setupHostDeserializeResult("host1Data", new HostIPs("host1ipv4", "host1ipv6"));
        setupGetDataResult(STACKS_PATH + path + "/host2", "host2Data");
        setupHostDeserializeResult("host2Data", new HostIPs("host2ipv4", "host2ipv6"));
        setupNodeExists(STACKS_PATH + path, true);

        StacksDAO testee = new StacksDAO(connector);
        HostIPs[] result = testee.getHosts(new XreStackPath(path)).toArray(new HostIPs[]{});

        Assert.assertEquals("host1ipv4", result[0].getIpV4Address());
        Assert.assertEquals("host1ipv6", result[0].getIpV6Address());
        Assert.assertEquals("host2ipv4", result[1].getIpV4Address());
        Assert.assertEquals("host2ipv6", result[1].getIpV6Address());
    }

    @Test
    public void testGetHostsFromCache() throws Exception {
        String path = "/PO/POC1/1.40/xreGuide";
        when(connector.isCacheHosts()).thenReturn(true);
        when(connector.getBasePath()).thenReturn("");
        setupGetRawHostsResult(STACKS_PATH + path, Arrays.asList("host1Data", "host2Data"));
        setupHostDeserializeResult("host1Data", new HostIPs("host1ipv4", "host1ipv6"));
        setupHostDeserializeResult("host2Data", new HostIPs("host2ipv4", "host2ipv6"));

        StacksDAO testee = new StacksDAO(connector);
        HostIPs[] result = testee.getHosts(new XreStackPath(path)).toArray(new HostIPs[]{});

        Assert.assertEquals("host1ipv4", result[0].getIpV4Address());
        Assert.assertEquals("host1ipv6", result[0].getIpV6Address());
        Assert.assertEquals("host2ipv4", result[1].getIpV4Address());
        Assert.assertEquals("host2ipv6", result[1].getIpV6Address());
    }

    @Test
    public void testGetHostsSkipsNoNodeInZookeeperException() throws Exception {
        String path = "/PO/POC1/1.40/xreGuide";
        when(connector.getBasePath()).thenReturn("");
        setupGetChildrenResult(STACKS_PATH + path, Arrays.asList("host1", "host2"));
        setupGetDataResult(STACKS_PATH + path + "/host1", "host1Data");
        setupHostDeserializeResult("host1Data", new HostIPs("host1ipv4", "host1ipv6"));
        setupGetDataThrowsException(STACKS_PATH + path + "/host2", new NoNodeInZookeeperException());
        setupNodeExists(STACKS_PATH + path, true);

        StacksDAO testee = new StacksDAO(connector);
        HostIPs[] result = testee.getHosts(new XreStackPath(path)).toArray(new HostIPs[]{});

        Assert.assertEquals(1, result.length);
        Assert.assertEquals("host1ipv4", result[0].getIpV4Address());
        Assert.assertEquals("host1ipv6", result[0].getIpV6Address());
    }

    @Test
    public void testDeleteStackPath() throws Exception {
        when(connector.getBasePath()).thenReturn("");
        when(connector.isPathExists(anyString())).thenReturn(true);
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1/1.40/xreGuide", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1/1.40", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO", Collections.<String>emptyList());

        StacksDAO testee = new StacksDAO(connector);
        testee.deleteStackPath(new XreStackPath("/PO/POC1/1.40/xreGuide"));

        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1/1.40/xreGuide");
        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1/1.40");
        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1");
        verify(connector, times(1)).delete(STACKS_PATH + "/PO");
    }

    @Test
    public void testDeleteStackPathNodeHasChildren() throws Exception {
        when(connector.getBasePath()).thenReturn("");
        when(connector.isPathExists(anyString())).thenReturn(true);
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1/1.40/xreGuide", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1/1.40", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO/POC1", Collections.<String>emptyList());
        setupGetChildrenResult(STACKS_PATH + "/PO", Arrays.asList("POC2", "POC3"));

        StacksDAO testee = new StacksDAO(connector);
        testee.deleteStackPath(new XreStackPath("/PO/POC1/1.40/xreGuide"));

        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1/1.40/xreGuide");
        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1/1.40");
        verify(connector, times(1)).delete(STACKS_PATH + "/PO/POC1");
        verify(connector, never()).delete(STACKS_PATH + "/PO");
    }

    @Test
    public void testGetHostsCount() throws Exception {
        String path = "/PO/POC1/1.40/xreGuide";
        when(connector.getBasePath()).thenReturn("/base");
        setupGetChildrenResult("/base" + STACKS_PATH + path, Arrays.asList("host1", "host2", "host3"));
        setupNodeExists("/base" + STACKS_PATH + path, true);

        StacksDAO testee = new StacksDAO(connector);
        int result = testee.getHostsCount(new XreStackPath(path));

        Assert.assertEquals(3, result);
    }

    private void setupCacheResult(final String input, String... result) {
//        when(stacksCache.getStacksChildrenForPath(input))
//                .thenReturn(Collections2.transform(Arrays.asList(result), new Function<String, String>() {
//                    @Override
//                    public String apply(String s) {
//                        return input + DELIMETER + s;
//                    }
//                }));
    }

    private void setupGetChildrenResult(String input, List<String> result) throws DataSourceConnectorException {
        when(connector.getChildren(input)).thenReturn(result);
    }

    private void setupGetRawHostsResult(String input, List<String> result) throws DataSourceConnectorException {
        //when(stacksCache.getRawHostsForPath(input)).thenReturn(result);
    }

    private void setupGetDataResult(String input, String result) throws DataSourceConnectorException {
        when(connector.getData(input))
            .thenReturn(result.getBytes());
    }

    private void setupGetDataThrowsException(String input, Throwable t) throws DataSourceConnectorException {
        doThrow(t).when(connector).getData(input);
    }

    private void setupNodeExists(String path, boolean result) throws DataSourceConnectorException {
        when(connector.isPathExists(path)).thenReturn(result);
    }

    private void setupHostDeserializeResult(String input, HostIPs result) throws SerializerException {
        when(hostsSerializer.deserialize(input)).thenReturn(result);
    }
}
