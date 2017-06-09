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

package com.comcast.redirector.core.balancer;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.redirector.core.engine.FilterMode;
import com.comcast.redirector.core.engine.ServerLookupMode;
import com.comcast.redirector.ruleengine.model.Server;
import org.junit.Assert;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class BalancerTest {
    private static final String FILTERED_IPV4 = "FILTERED_IPV4";
    private static final String FILTERED_IPV6 = "FILTERED_IPV6";
    private static final String NON_FILTERED_IPV4 = "NON_FILTERED_IPV4";
    private static final String NON_FILTERED_IPV6 = "NON_FILTERED_IPV6";

    private Server server;
    private IFilteredServiceProvider stackServiceProvider;
    private IFilteredServiceProvider flavorServiceProvider;
    private IWhitelistPredicate mockWhitelist;

    private Balancer balancer;

    @Before
    public void setUp() throws Exception {
        MetaData nonFilteredPayload = new MetaData();
        nonFilteredPayload.setParameters(new HashMap<String, String>() {{
            put(IpProtocolVersion.IPV4.getId(), NON_FILTERED_IPV4);
            put(IpProtocolVersion.IPV6.getId(), NON_FILTERED_IPV6);
        }});

        MetaData filteredPayload = new MetaData();
        filteredPayload.setParameters(new HashMap<String, String>() {{
            put(IpProtocolVersion.IPV4.getId(), FILTERED_IPV4);
            put(IpProtocolVersion.IPV6.getId(), FILTERED_IPV6);
        }});

        ServiceInstance<MetaData> nonFilteredStackInstance = ServiceInstance.<MetaData>builder().id("nonfiltered").name("nonfiltered").payload(nonFilteredPayload).build();
        ServiceInstance<MetaData> filteredStackInstance = ServiceInstance.<MetaData>builder().id("filtered").name("filtered").payload(filteredPayload).build();

        ServiceInstance<MetaData> nonFilteredFlavorInstance = ServiceInstance.<MetaData>builder().id("nonfilteredflavor").name("nonfilteredflavor").payload(nonFilteredPayload).build();
        ServiceInstance<MetaData> filteredFlavorInstance = ServiceInstance.<MetaData>builder().id("filteredflavor").name("filteredflavor").payload(filteredPayload).build();

        server = new Server("test", "{protocol}://{host}:{port}/{urn}", "");
        IServiceProviderManager mockServiceProviderManager = mock(IServiceProviderManager.class);
        mockWhitelist = mock(IWhitelistPredicate.class);
        stackServiceProvider = mock(IFilteredServiceProvider.class);
        flavorServiceProvider = mock(IFilteredServiceProvider.class);

        when(mockServiceProviderManager.getServiceProviderForFlavorAndApp(anyString(), anyString())).thenReturn(flavorServiceProvider);
        when(mockServiceProviderManager.getStackServiceProvider(any(StackData.class))).thenReturn(stackServiceProvider);
        when(stackServiceProvider.getInstance()).thenReturn(nonFilteredStackInstance);
        when(stackServiceProvider.getFilteredInstance(FilterMode.WHITELIST_ONLY, mockWhitelist)).thenReturn(filteredStackInstance);
        when(flavorServiceProvider.getInstance()).thenReturn(nonFilteredFlavorInstance);
        when(flavorServiceProvider.getFilteredInstance(FilterMode.WHITELIST_ONLY, mockWhitelist)).thenReturn(filteredFlavorInstance);

        balancer = new Balancer("testApp", mockServiceProviderManager, mockWhitelist, RedirectorConstants.NO_MODEL_NODE_VERSION);
    }

    @Test
    public void testGetServiceInstanceForFlavorApplyFilter() throws Exception {
        server.setPath("1.43");

        InstanceInfo instanceInfo = balancer.getServiceInstance(server, ServerLookupMode.DEFAULT);

        Assert.assertFalse(instanceInfo.isStackBased().booleanValue());
        Assert.assertEquals(FILTERED_IPV4, instanceInfo.getServerIp());
        Assert.assertEquals(FILTERED_IPV6, instanceInfo.getServerIpV6());
    }

    @Test
    public void testGetServiceInstanceForFlavorNoFilter() throws Exception {
        server.setPath("1.43");

        InstanceInfo instanceInfo = balancer.getServiceInstance(server, ServerLookupMode.NON_FILTERED);

        Assert.assertFalse(instanceInfo.isStackBased().booleanValue());
        Assert.assertEquals(NON_FILTERED_IPV4, instanceInfo.getServerIp());
        Assert.assertEquals(NON_FILTERED_IPV6, instanceInfo.getServerIpV6());
    }

    @Test
    public void testGetServiceInstanceForStackApplyFilter() throws Exception {
        server.setPath("/PO/POC7/1.43");

        InstanceInfo instanceInfo = balancer.getServiceInstance(server, ServerLookupMode.DEFAULT);

        Assert.assertTrue(instanceInfo.isStackBased().booleanValue());
        Assert.assertEquals("/PO/POC7", instanceInfo.getStack());
        Assert.assertEquals(FILTERED_IPV4, instanceInfo.getServerIp());
        Assert.assertEquals(FILTERED_IPV6, instanceInfo.getServerIpV6());
    }

    @Test
    public void testGetServiceInstanceForStackNoFilter() throws Exception {
        server.setPath("/PO/POC7/1.43");

        InstanceInfo instanceInfo = balancer.getServiceInstance(server, ServerLookupMode.NON_FILTERED);

        Assert.assertTrue(instanceInfo.isStackBased().booleanValue());
        Assert.assertEquals(NON_FILTERED_IPV4, instanceInfo.getServerIp());
        Assert.assertEquals(NON_FILTERED_IPV6, instanceInfo.getServerIpV6());
    }

    @Test
    public void testGetHostsCountForFlavorApplyFilter() throws Exception {
        Collection<ServiceInstance<MetaData>> allFlavorFilteredInstances = new ArrayList<>();
        allFlavorFilteredInstances.add(getServiceInstance("ipv4"));

        when(flavorServiceProvider.getAllFilteredInstances(FilterMode.WHITELIST_ONLY, mockWhitelist)).thenReturn(allFlavorFilteredInstances);
        Assert.assertEquals(1, balancer.getCountOfHostsForPath("1.43", ServerLookupMode.DEFAULT));
    }

    @Test
    public void testGetHostsCountForFlavorNoFilter() throws Exception {
        Collection<ServiceInstance<MetaData>> allFlavorInstances = new ArrayList<>();
        allFlavorInstances.add(getServiceInstance("ipv4"));
        allFlavorInstances.add(getServiceInstance("ipv43"));

        when(flavorServiceProvider.getAllInstances()).thenReturn(allFlavorInstances);
        Assert.assertEquals(2, balancer.getCountOfHostsForPath("1.43", ServerLookupMode.NON_FILTERED));
    }

    @Test
    public void testGetHostsCountForStackApplyFilter() throws Exception {
        Collection<ServiceInstance<MetaData>> allStackFilteredInstances = new ArrayList<>();
        allStackFilteredInstances.add(getServiceInstance("ipv4"));
        allStackFilteredInstances.add(getServiceInstance("ipv43"));
        allStackFilteredInstances.add(getServiceInstance("ipv44"));

        when(stackServiceProvider.getAllFilteredInstances(FilterMode.WHITELIST_ONLY, mockWhitelist)).thenReturn(allStackFilteredInstances);

        Assert.assertEquals(3, balancer.getCountOfHostsForPath("/PO/POC7/1.43", ServerLookupMode.DEFAULT));
    }

    @Test
    public void testGetHostsCountForStackNoFilter() throws Exception {
        Collection<ServiceInstance<MetaData>> allStackInstances = new ArrayList<>();
        allStackInstances.add(getServiceInstance("ipv4"));
        allStackInstances.add(getServiceInstance("ipv42"));
        allStackInstances.add(getServiceInstance("ipv43"));
        allStackInstances.add(getServiceInstance("ipv44"));
        allStackInstances.add(getServiceInstance("ipv45"));

        when(stackServiceProvider.getAllInstances()).thenReturn(allStackInstances);

        Assert.assertEquals(5, balancer.getCountOfHostsForPath("/PO/POC7/1.43", ServerLookupMode.NON_FILTERED));
    }

    private static ServiceInstance<MetaData> getServiceInstance(String ipAddress) throws Exception {
        return ServiceInstance.<MetaData>builder().id(ipAddress).address(ipAddress).name("xreGuide")
            .payload(new MetaData(UUID.randomUUID(), "listenAddress", 0, "xreGuide")).build();
    }
}
