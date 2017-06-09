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

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(DataProviderRunner.class)
public class StackSnapshotTest {
    private static final String PATH = "PATH";
    private static final String IPV4 = "IPV4";
    private static final String IPV6 = "IPV6";

    @DataProvider
    public static Object[][] ipsData() {
        return new Object[][] {
                { IPV4, IPV6, IPV4, IPV6, IPV4 + "," + IPV6 },
                { IPV4, null, IPV4, null, IPV4 + ",null" },
                { null, IPV6, null, IPV6, "null,"+ IPV6 },
                { null, null, null, null, "null,null" },
        };
    }

    @DataProvider
    public static Object[][] ipsDataIncorrect() {
        return new Object[][] {
                { null, IPV6 },
                { null, null }
        };
    }

    @DataProvider
    public static Object[][] hostsData() {
        return new Object[][] {
                { IPV4 + "," + IPV6, IPV4, IPV6, IPV4 + "," + IPV6 },
                { IPV4 + ":test", IPV4, null, IPV4 + ",null"},
        };
    }

    @DataProvider
    public static Object[][] hostsDataIncorrect() {
        return new Object[][] {
                { " " },
                { null },
                { "" }
        };
    }

    @DataProvider
    public static Object[][] equalsData() {
        return new Object[][] {
                { new StackSnapshot.Host("a,b"), new StackSnapshot.Host("a","b"), true },
                { new StackSnapshot.Host("a", null), new StackSnapshot.Host("a"), true },
                { new StackSnapshot.Host("a,bc"), new StackSnapshot.Host("a","b"), false },
                { new StackSnapshot.Host("aaa", null), new StackSnapshot.Host("a"), false }
        };
    }

    @Test
    @UseDataProvider("ipsData")
    public void testHostConstruct(String ipv4Input, String ipv6Input, String ipv4Expected, String ipv6Expected, String toStringExpected) {
        StackSnapshot.Host host = new StackSnapshot.Host(ipv4Input, ipv6Input);
        Assert.assertEquals(ipv4Expected, host.getIpv4());
        Assert.assertEquals(ipv6Expected, host.getIpv6());
        Assert.assertEquals(toStringExpected, host.toString());
    }

    @Test
    @UseDataProvider("hostsData")
    public void testHostConstructFromString(String input, String ipv4Expected, String ipv6Expected, String toStringExpected) {
        StackSnapshot.Host host = new StackSnapshot.Host(input);
        Assert.assertEquals(ipv4Expected, host.getIpv4());
        Assert.assertEquals(ipv6Expected, host.getIpv6());
        Assert.assertEquals(toStringExpected, host.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    @UseDataProvider("hostsDataIncorrect")
    public void testHostConstructFromStringNegative(String input) {
        new StackSnapshot.Host(input);
    }

    @Test
    @UseDataProvider("equalsData")
    public void testEquals(StackSnapshot.Host left, StackSnapshot.Host right, boolean result) {
        Assert.assertEquals(result, left.equals(right));
    }

    @Test
    public void testSnapshotEquals() {
        StackSnapshot left = new StackSnapshot(PATH, null);
        left.setHosts(new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("a","b"));
            add(new StackSnapshot.Host("c"));
        }});

        StackSnapshot right = new StackSnapshot(PATH, new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("a,b"));
            add(new StackSnapshot.Host("c"));
        }});

        Assert.assertTrue(left.equals(right));

        left.setPath("test");
        Assert.assertFalse(left.equals(right));

        left.setPath(PATH);
        left.setHosts(null);
        Assert.assertFalse(left.equals(right));
    }

    @Test
    public void testDeserializeSnapshotWithHostAsString() throws Exception {
        Serializer serializer = new JsonSerializer();
        StackSnapshot stackSnapshot = serializer.deserialize("{\"path\":\"/p3/b3cb/xappl-XRE.41P8/xreGuide\",\"hosts\":[\"100.200.100.111:10004\"]}",
                StackSnapshot.class);
        Assert.assertNotNull(stackSnapshot);
        Assert.assertNotNull(stackSnapshot.getHosts());
        Assert.assertNotNull(stackSnapshot.getHosts().get(0));
        Assert.assertEquals("100.200.100.111", stackSnapshot.getHosts().get(0).getIpv4());
        Assert.assertNull(stackSnapshot.getHosts().get(0).getIpv6());
    }

    @Test
    public void testDeserializeSnapshotWithHostAsObject() throws Exception {
        Serializer serializer = new JsonSerializer();
        StackSnapshot stackSnapshot = new StackSnapshot("path", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("ipv4", "ipv6"));
        }});
        String serialized = serializer.serialize(stackSnapshot, false);
        StackSnapshot deserialized = serializer.deserialize(serialized, StackSnapshot.class);
        Assert.assertNotNull(deserialized);
        Assert.assertNotNull(deserialized.getHosts());
        Assert.assertNotNull(deserialized.getHosts().get(0));
        Assert.assertEquals("ipv4", deserialized.getHosts().get(0).getIpv4());
        Assert.assertEquals("ipv6", deserialized.getHosts().get(0).getIpv6());
    }
}
