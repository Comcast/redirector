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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.xrestack.*;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.*;

import static org.mockito.Mockito.*;

public class StacksServiceTest {
    private IStacksDAO stacksDAO;
    private IWhiteListService whiteListService;
    private StacksService testee;

    @Before
    public void setUp() throws Exception {
        stacksDAO = mock(IStacksDAO.class);
        testee = new StacksService();
        testee.setStacksDAO(stacksDAO);
        whiteListService = mock(IWhiteListService.class);
        testee.setWhiteListService(whiteListService);
    }

    @Test
    public void testGetStacksForService() throws Exception {
        String serviceName = "xreGuide";
        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide", "/po/poc1/1.52/xreApp"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(5);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc2/1.50/xreGuide"))).thenReturn(6);
        setupWhitelistedService();

        ServicePaths result = testee.getStacksForService(serviceName);

        Assert.assertEquals(1, result.getPaths().size());

        Paths paths = result.getPaths().get(0);
        Assert.assertEquals(serviceName, paths.getServiceName());
        Assert.assertEquals(1, paths.getFlavors().size());
        Assert.assertEquals(2, paths.getStacks().size());
        verifyPathItem("1.50", 11, paths.getFlavors().get(0));
        verifyPathItem("/po/poc1/1.50", 5, paths.getStacks().get(0));
        verifyPathItem("/po/poc2/1.50", 6, paths.getStacks().get(1));
    }

    @Test
    public void testGetStacksForServiceWithWhitelistedNodes() throws Exception {
        String serviceName = "xreGuide";
        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide", "/po/poc1/1.52/xreApp"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(5);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc2/1.50/xreGuide"))).thenReturn(6);
        setupWhitelistedService("/po/poc1");

        ServicePaths result = testee.getStacksForService(serviceName);

        Assert.assertEquals(1, result.getPaths().size());

        Paths paths = result.getPaths().get(0);
        Assert.assertEquals(serviceName, paths.getServiceName());
        Assert.assertEquals(1, paths.getFlavors().size());
        Assert.assertEquals(2, paths.getStacks().size());
        verifyPathItem("1.50", 11, 5, paths.getFlavors().get(0));
        verifyPathItem("/po/poc1/1.50", 5, 5, paths.getStacks().get(0));
        verifyPathItem("/po/poc2/1.50", 6, 0, paths.getStacks().get(1));
    }

    private void verifyPathItem(String name, int nodes, PathItem item) {
        Assert.assertEquals(name, item.getValue());
        Assert.assertEquals(nodes, item.getActiveNodesCount());
    }

    private void verifyPathItem(String name, int nodes, int nodesWhitelisted, PathItem item) {
        Assert.assertEquals(name, item.getValue());
        Assert.assertEquals(nodes, item.getActiveNodesCount());
        Assert.assertEquals(nodesWhitelisted, item.getWhitelistedNodesCount());
    }

    @Test
    public void testGetAllServiceNames() throws Exception {
        when(stacksDAO.getAllAppNamesRegisteredInStacks()).thenReturn(
                new HashSet<>(Arrays.asList("xreGuide", "xreApp")));

        Collection<String> allNames = testee.getAllServiceNames();

        Assert.assertTrue(allNames.contains("xreGuide"));
        Assert.assertTrue(allNames.contains("xreApp"));
    }

    @Test
    public void testGetStacksForAllServices() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide", "/po/poc1/1.52/xreApp"));
        when(stacksDAO.getAllAppNamesRegisteredInStacks()).thenReturn(
                new LinkedHashSet<>(Arrays.asList("xreGuide", "xreApp")));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(5);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc2/1.50/xreGuide"))).thenReturn(6);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.52/xreApp"))).thenReturn(8);
        setupWhitelistedService();

        ServicePaths result = testee.getStacksForAllServices();

        Assert.assertEquals(2, result.getPaths().size());

        Paths paths = result.getPaths().get(0);
        Assert.assertEquals("xreGuide", paths.getServiceName());
        Assert.assertEquals(1, paths.getFlavors().size());
        Assert.assertEquals(2, paths.getStacks().size());
        verifyPathItem("1.50", 11, paths.getFlavors().get(0));
        verifyPathItem("/po/poc1/1.50", 5, paths.getStacks().get(0));
        verifyPathItem("/po/poc2/1.50", 6, paths.getStacks().get(1));

        paths = result.getPaths().get(1);
        Assert.assertEquals("xreApp", paths.getServiceName());
        Assert.assertEquals(1, paths.getFlavors().size());
        Assert.assertEquals(1, paths.getStacks().size());
        verifyPathItem("1.52", 8, paths.getFlavors().get(0));
        verifyPathItem("/po/poc1/1.52", 8, paths.getStacks().get(0));
    }

    @Test
    public void testGetActiveStacksAndFlavors() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide", "/po/poc1/1.52/xreGuide"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(5);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc2/1.50/xreGuide"))).thenReturn(0);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.52/xreGuide"))).thenReturn(0);
        setupWhitelistedService();

        Set<PathItem> result = testee.getActiveStacksAndFlavors("xreGuide");

        Assert.assertTrue(containsInPaths(result, "/po/poc1/1.50"));
        Assert.assertTrue(containsInPaths(result, "1.50"));
        Assert.assertFalse(containsInPaths(result, "/po/poc2/1.50"));
        Assert.assertFalse(containsInPaths(result, "1.51"));
    }

    private boolean containsInPaths (Set<PathItem> paths, String path) {
        for (PathItem item : paths) {
            if (item.getValue().equals(path)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testDeleteStack() throws Exception {
        testee.deleteStack("xreGuide", "po", "poc7", "1.40");

        verify(stacksDAO, times(1)).deleteStackPath(new XreStackPath("/po/poc7/1.40/xreGuide"));
    }

    @Test
    public void testDeleteStacks() throws Exception {
        Paths paths = new Paths("xreGuide");
        paths.getStacks().add(new PathItem("/po/poc7/1.50", 0, 0));
        paths.getStacks().add(new PathItem("/po/poc7/1.51", 0, 0));
        paths.getStacks().add(new PathItem("/po/poc2/1.51", 0, 0));

        testee.deleteStacks(paths);

        verify(stacksDAO, times(1)).deleteStackPath(new XreStackPath("/po/poc7/1.50/xreGuide"));
        verify(stacksDAO, times(1)).deleteStackPath(new XreStackPath("/po/poc7/1.51/xreGuide"));
        verify(stacksDAO, times(1)).deleteStackPath(new XreStackPath("/po/poc2/1.51/xreGuide"));
    }

    @Test
    public void testGetServiceAddressByStack() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(getStackPaths("/po/poc1/1.50/xreGuide"));
        when(stacksDAO.getHosts(new XreStackPath("/po/poc1/1.50/xreGuide")))
                .thenReturn(Arrays.asList(new HostIPs("ipv4", "ipv6"), new HostIPs("ipv4_2", "ipv6_2")));

        HostIPsListWrapper result = testee.getHostsForStackAndService("/po/poc1/1.50", "xreGuide");

        Assert.assertEquals(new HostIPs("ipv4", "ipv6"), result.getHostIPsList().get(0));
        Assert.assertEquals(new HostIPs("ipv4_2", "ipv6_2"), result.getHostIPsList().get(1));
    }

    @Test
    public void testGetServiceAddressByStackOnly() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(getStackPaths("/po/poc1/1.50/xreGuide"));
        when(stacksDAO.getHostsByStackOnlyPath(any(XreStackPath.class)))
                .thenReturn(Arrays.asList(new HostIPs("ipv4", "ipv6"), new HostIPs("ipv4_2", "ipv6_2")));

        HostIPsListWrapper result = testee.getHostsForStackOnlyAndService("/po/poc1", "xreGuide");

        Assert.assertEquals(new HostIPs("ipv4", "ipv6"), result.getHostIPsList().get(0));
        Assert.assertEquals(new HostIPs("ipv4_2", "ipv6_2"), result.getHostIPsList().get(1));
    }

    @Test
    public void testGetRandomServiceAddressByStack() throws Exception {
        HostIPs hostIps1 = new HostIPs("ipv4", "ipv6");
        HostIPs hostIps2 = new HostIPs("ipv4_2", "ipv6_2");

        when(stacksDAO.getAllStackPaths()).thenReturn(getStackPaths("/po/poc1/1.50/xreGuide"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(2);

        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.50/xreGuide"), 0)).thenReturn(hostIps1);
        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.50/xreGuide"), 1)).thenReturn(hostIps2);

        int returnFirstAddressCount = 0;
        int returnSecondAddressCount = 0;
        for (int i = 0; i < 100; i++) {
            HostIPsListWrapper result = testee.getRandomHostForStackAndService("/po/poc1/1.50", "xreGuide");
            Assert.assertTrue(result.getHostIPsList().size() == 1);
            HostIPs resultHostIps = result.getHostIPsList().get(0);
            if (new HostIPs("ipv4", "ipv6").equals(resultHostIps)) {
                returnFirstAddressCount ++;
            }
            if (new HostIPs("ipv4_2", "ipv6_2").equals(resultHostIps)) {
                returnSecondAddressCount ++;
            }
            Assert.assertTrue(hostIps1.equals(resultHostIps) ||
                              hostIps2.equals(resultHostIps));
        }
        Assert.assertTrue(returnFirstAddressCount > 0);
        Assert.assertTrue(returnSecondAddressCount > 0);
    }

    @Test
    public void testGetRandomServiceAddressByStackWithoutFlavor() throws Exception {
        HostIPs hostIps1 = new HostIPs("ipv4", "ipv6");
        HostIPs hostIps2 = new HostIPs("ipv4_2", "ipv6_2");
        List<String> flavors = new ArrayList<>(2);
        flavors.add("1.50");
        flavors.add("1.49");

        when(stacksDAO.getAllStackPaths()).thenReturn(getStackPaths("/po/poc1/1.50/xreGuide"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(1);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.49/xreGuide"))).thenReturn(1);
        when(stacksDAO.getFlavorsByStackOnlyPath(any(XreStackPath.class))).thenReturn(flavors);

        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.50/xreGuide"), 0)).thenReturn(hostIps1);
        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.49/xreGuide"), 0)).thenReturn(hostIps2);

        int returnFirstAddressCount = 0;
        int returnSecondAddressCount = 0;
        for (int i = 0; i < 100; i++) {
            HostIPsListWrapper result = testee.getRandomHostForStackOnlyAndService("/po/poc1", "xreGuide");
            Assert.assertTrue(result.getHostIPsList().size() == 1);
            HostIPs resultHostIps = result.getHostIPsList().get(0);
            if (new HostIPs("ipv4", "ipv6").equals(resultHostIps)) {
                returnFirstAddressCount ++;
            }
            if (new HostIPs("ipv4_2", "ipv6_2").equals(resultHostIps)) {
                returnSecondAddressCount ++;
            }
            Assert.assertTrue(hostIps1.equals(resultHostIps) ||
                    hostIps2.equals(resultHostIps));
        }
        Assert.assertTrue(returnFirstAddressCount > 0);
        Assert.assertTrue(returnSecondAddressCount > 0);
    }

    @Test
    public void testGetServiceAddressByFlavor() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide", "/po/poc3/1.50/xreGuide"));
        when(stacksDAO.getHosts(new XreStackPath("/po/poc1/1.50/xreGuide")))
                .thenReturn(Arrays.asList(new HostIPs("ipv4", "ipv6"), new HostIPs("ipv4_2", "ipv6_2")));
        when(stacksDAO.getHosts(new XreStackPath("/po/poc2/1.50/xreGuide")))
                .thenReturn(Arrays.asList(new HostIPs("ipv4_3", "ipv6_3")));

        HostIPsListWrapper result = testee.getHostsForFlavorAndService("1.50", "xreGuide");

        Assert.assertEquals(new HostIPs("ipv4", "ipv6"), result.getHostIPsList().get(0));
        Assert.assertEquals(new HostIPs("ipv4_2", "ipv6_2"), result.getHostIPsList().get(1));
        Assert.assertEquals(new HostIPs("ipv4_3", "ipv6_3"), result.getHostIPsList().get(2));
    }

    @Test
    public void testGetRandomServiceAddressByFlavor() throws Exception {

        HostIPs hostIps1 = new HostIPs("ipv4", "ipv6");
        HostIPs hostIps2 = new HostIPs("ipv4_2", "ipv6_2");
        HostIPs hostIps3 = new HostIPs("ipv4_3", "ipv6_3");

        when(stacksDAO.getAllStackPaths()).thenReturn(
                getStackPaths("/po/poc1/1.50/xreGuide", "/po/poc2/1.50/xreGuide"));

        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc1/1.50/xreGuide"))).thenReturn(2);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc2/1.50/xreGuide"))).thenReturn(1);
        when(stacksDAO.getHostsCount(new XreStackPath("/po/poc3/1.50/xreGuide"))).thenReturn(0);

        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.50/xreGuide"), 0)).thenReturn(hostIps1);
        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc1/1.50/xreGuide"), 1)).thenReturn(hostIps2);
        when(stacksDAO.getHostByIndex(new XreStackPath("/po/poc2/1.50/xreGuide"), 0)).thenReturn(hostIps3);


        int [] returnAddressesCount = {0, 0, 0};
        for (int i = 0; i < 100; i++) {
            HostIPsListWrapper result = testee.getRandomHostForFlavorAndService("1.50", "xreGuide");
            Assert.assertTrue(result.getHostIPsList().size() == 1);
            HostIPs resultHostIps = result.getHostIPsList().get(0);
            if (hostIps1.equals(resultHostIps)) {
                returnAddressesCount [0] ++;
            }
            if (hostIps2.equals(resultHostIps)) {
                returnAddressesCount [1] ++;
            }
            if (hostIps3.equals(resultHostIps)) {
                returnAddressesCount [2] ++;
            }
            Assert.assertTrue(hostIps1.equals(resultHostIps) ||
                              hostIps2.equals(resultHostIps) ||
                              hostIps3.equals(resultHostIps));
        }
        for (int i = 0; i < returnAddressesCount.length ; i++) {
             Assert.assertTrue(returnAddressesCount [i] > 0);
        }
    }

    @Test
    public void testGetAllStacksAndHosts() throws Exception {
        when(stacksDAO.getAllStackPaths()).thenReturn(
                new LinkedHashSet<>(Arrays.asList(
                        new XreStackPath("/po/poc1/1.50/xreGuide"),
                        new XreStackPath("/po/poc2/1.50/xreGuide"))));
        when(stacksDAO.getHosts(new XreStackPath("/po/poc1/1.50/xreGuide")))
                .thenReturn(Arrays.asList(new HostIPs("ipv4", "ipv6"), new HostIPs("ipv4_2", "ipv6_2")));
        when(stacksDAO.getHosts(new XreStackPath("/po/poc2/1.50/xreGuide")))
                .thenReturn(Arrays.asList(new HostIPs("ipv4_3", "ipv6_3")));

        StackData[] stacks = testee.getAllStacksAndHosts("xreGuide").toArray(new StackData[]{});

        Assert.assertEquals("/po/poc1/1.50/xreGuide", stacks[0].getPath());
        Assert.assertEquals("ipv4", stacks[0].getHosts().get().get(0).getIpV4Address());
        Assert.assertEquals("ipv6", stacks[0].getHosts().get().get(0).getIpV6Address());
        Assert.assertEquals("ipv4_2", stacks[0].getHosts().get().get(1).getIpV4Address());
        Assert.assertEquals("ipv6_2", stacks[0].getHosts().get().get(1).getIpV6Address());
        Assert.assertEquals("/po/poc2/1.50/xreGuide", stacks[1].getPath());
        Assert.assertEquals("ipv4_3", stacks[1].getHosts().get().get(0).getIpV4Address());
        Assert.assertEquals("ipv6_3", stacks[1].getHosts().get().get(0).getIpV6Address());
    }

    private Set<XreStackPath> getStackPaths(String... path) {
        return new LinkedHashSet<>(Collections2.transform(Arrays.asList(path), new Function<String, XreStackPath>() {
            @Nullable
            @Override
            public XreStackPath apply(String input) {
                return new XreStackPath(input);
            }
        }));
    }

    private void setupWhitelistedService(String... stack) {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Arrays.asList(stack));
        when(whiteListService.getWhitelistedStacks(anyString())).thenReturn(whitelisted);
    }
}
