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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.balancer.IBalancer;
import com.comcast.redirector.core.engine.rules.IFlavorRuleSet;
import com.comcast.redirector.ruleengine.model.DistributionServer;
import com.comcast.redirector.ruleengine.model.Server;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HostSelectorTest {
    private static final String APP_NAME = "anyAppName";
    private IBalancer balancer;
    private IFlavorRuleSet flavorRules;
    RedirectorEngine.HostSelector hostSelector;

    @Before
    public void setUp() throws Exception {
        balancer = mock(IBalancer.class);
        flavorRules = mock(IFlavorRuleSet.class);

        hostSelector = new RedirectorEngine.HostSelector.Builder().setAppName(APP_NAME).setBalancer(balancer).setFlavorRules(flavorRules).build();
    }

    @Test
    public void testGetCountOfHostsForDistribution() throws Exception {
        final Server server1 = new Server("1","url1","");
        server1.setPath("path1");

        final Server server2 = new Server("2","url2","");
        server1.setPath("path2");

        List<DistributionServer> servers = new ArrayList<DistributionServer>() {{
            add(new DistributionServer(server1, 10.0));
            add(new DistributionServer(server2, 20.0));
        }};

        when(flavorRules.getDistributionServers()).thenReturn(servers);
        when(balancer.getCountOfHostsForPath(server1.getPath(), ServerLookupMode.DEFAULT)).thenReturn(5);
        when(balancer.getCountOfHostsForPath(server2.getPath(), ServerLookupMode.DEFAULT)).thenReturn(10);

        Assert.assertEquals(15, hostSelector.getCountOfHostsForDistribution());
    }

    @Test
    public void testGetPercentDeviationCountOfHostsForDistribution() throws Exception {
        //Test case if deviation equals 0
        final Server server1 = new Server("1","url1","");
        server1.setPath("path1");

        final Server server2 = new Server("2","url2","");
        server1.setPath("path2");

        Server defaultServer = new Server("default", "default", "");
        defaultServer.setPath("default");

        List<DistributionServer> servers = new ArrayList<DistributionServer>() {{
            add(new DistributionServer(server1, 10.0));
            add(new DistributionServer(server2, 20.0));
        }};

        when(flavorRules.getDistributionServers()).thenReturn(servers);
        when(flavorRules.getDefault()).thenReturn(defaultServer);
        when(balancer.getCountOfHostsForPath(server1.getPath(), ServerLookupMode.DEFAULT)).thenReturn(5);
        when(balancer.getCountOfHostsForPath(server2.getPath(), ServerLookupMode.DEFAULT)).thenReturn(10);
        when(balancer.getCountOfHostsForPath(defaultServer.getPath(), ServerLookupMode.DEFAULT)).thenReturn(35);

        Assert.assertEquals(0, hostSelector.getPercentDeviationCountOfHostsForDistribution());

        //Test case if deviation equals 30
        when(balancer.getCountOfHostsForPath(defaultServer.getPath(), ServerLookupMode.DEFAULT)).thenReturn(50);
        Assert.assertEquals(30, hostSelector.getPercentDeviationCountOfHostsForDistribution());

        //Test case if deviation is negative value have to return value = 0
        when(balancer.getCountOfHostsForPath(defaultServer.getPath(), ServerLookupMode.DEFAULT)).thenReturn(20);
        Assert.assertEquals(0, hostSelector.getPercentDeviationCountOfHostsForDistribution());

        //Test case if distribution isn't exist
        when(flavorRules.getDistributionServers()).thenReturn(null);
        Assert.assertEquals(0, hostSelector.getPercentDeviationCountOfHostsForDistribution());
    }

    @Test
    public void testIsServerInstanceInvalidBecauseNull() throws Exception {
        Assert.assertTrue(RedirectorEngine.HostSelector.isServerInstanceInvalid(null));
    }

    @Test
    public void testIsServerInstanceInvalidBecauseNoURL() throws Exception {
        Assert.assertTrue(RedirectorEngine.HostSelector.isServerInstanceInvalid(new InstanceInfo(new Server("", "", ""), null)));
        Assert.assertTrue(RedirectorEngine.HostSelector.isServerInstanceInvalid(new InstanceInfo(new Server("", "", ""), "")));
    }

    @Test
    public void testIsServerInstanceValid() throws Exception {
        Assert.assertFalse(RedirectorEngine.HostSelector.isServerInstanceInvalid(new InstanceInfo(new Server("", "", ""), "test")));
    }
}
