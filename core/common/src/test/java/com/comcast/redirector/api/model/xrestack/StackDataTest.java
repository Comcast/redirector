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

package com.comcast.redirector.api.model.xrestack;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StackDataTest {
    @Test
    public void testConstructFromStringPath() throws Exception {
        StackData stackData = new StackData("/A/B/C/D");

        Assert.assertEquals("A", stackData.getDataCenter());
        Assert.assertEquals("B", stackData.getAvailabilityZone());
        Assert.assertEquals("C", stackData.getFlavor());
        Assert.assertEquals("D", stackData.getServiceName());
        Assert.assertFalse(stackData.getHosts().isPresent());
    }

    @Test
    public void testConstructWithHosts() throws Exception {
        List<HostIPs> hosts = new ArrayList<>();
        hosts.add(new HostIPs("ipv4_1", "ipv6_1"));
        hosts.add(new HostIPs("ipv4_2", "ipv6_2"));
        StackData stackData = new StackData("/A/B/C/D", hosts);

        Assert.assertEquals(hosts, stackData.getHosts().get());
    }

    @Test
    public void testConstructFromParameters() throws Exception {
        List<HostIPs> hosts = new ArrayList<>();
        hosts.add(new HostIPs("ipv4_1", "ipv6_1"));
        hosts.add(new HostIPs("ipv4_2", "ipv6_2"));
        StackData stackData = new StackData("A","B","C","D", hosts);

        Assert.assertEquals(hosts, stackData.getHosts().get());
        Assert.assertEquals("A", stackData.getDataCenter());
        Assert.assertEquals("B", stackData.getAvailabilityZone());
        Assert.assertEquals("C", stackData.getFlavor());
        Assert.assertEquals("D", stackData.getServiceName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromIncorrectPath() throws Exception {
        new StackData("/A/B/C");
    }
}
