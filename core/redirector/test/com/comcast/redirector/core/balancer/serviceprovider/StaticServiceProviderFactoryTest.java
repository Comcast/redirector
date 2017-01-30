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

package com.comcast.redirector.core.balancer.serviceprovider;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.FilterMode;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StaticServiceProviderFactoryTest {

    private IWhitelistPredicate whiteListedStacksManager;
    private IFilteredServiceProvider provider;

    @Before
    public void before() {
        whiteListedStacksManager = mock(IWhitelistPredicate.class);
        ZKConfig config = mock(ZKConfig.class);

        StaticServiceProviderFactory staticServiceProviderFactory = new StaticServiceProviderFactory(new RoundRobinStrategy<MetaData>(), config);
        when(config.getDefaultWeightOfTheNode()).thenReturn(5);
        when(config.getMaxWeightOfTheNode()).thenReturn(10);

        List<HostIPs> hosts = new ArrayList<>();
        hosts.add(new HostIPs("10.10.20.10", "2001:0db8:11a3:09d7:1f34:8a2e:07a0:765d"));
        hosts.add(new HostIPs("10.10.20.20", "2001:0db8:12a3:09d7:1f34:8a2e:07a0:765d"));

        provider = staticServiceProviderFactory.createServiceProvider(new XreStackPath("/PO/POC7/1.40/xreGuide"), hosts);
    }

    @Test
    public void testGetInstance() {
        ServiceInstance<MetaData> serviceProvider;
        Map<String, AtomicLong> result = new HashMap<>();

        for (int i = 0; i <= 10001; i++) {
            serviceProvider = provider.getInstance();

            if (result.containsKey(serviceProvider.getId())) {
                result.get(serviceProvider.getId()).incrementAndGet();
            } else {
                result.put(serviceProvider.getId(), new AtomicLong(0));
            }

        }

        long firstIp = result.get("10.10.20.10").get();
        Assert.assertTrue("10.10.20.10", firstIp >= 4990 && firstIp <= 5010);

        long secondIp = result.get("10.10.20.20").get();
        Assert.assertTrue("10.10.20.20", secondIp >= 4990 && secondIp <= 5010);

        Assert.assertEquals(2, provider.getAllUniqueInstances().size());
    }

    @Test
    public void testGetAllInstances() {
        Collection<ServiceInstance<MetaData>> instances = provider.getAllInstances();
        verifyCollection(instances);
        Assert.assertEquals(2, provider.getAllUniqueInstances().size());
    }

    @Test
    public void testGetFilteredInstance() {
        when(whiteListedStacksManager.isWhiteListed(eq("/PO/POC7"))).thenReturn(true);
        ServiceInstance<MetaData> serviceProvider = provider.getFilteredInstance(FilterMode.WHITELIST_ONLY, whiteListedStacksManager);

        Assert.assertNotNull(serviceProvider);
        assertThat(provider.getAllInstances(), hasItem(serviceProvider));

        when(whiteListedStacksManager.isWhiteListed(eq("/PO/POC7"))).thenReturn(false);
        serviceProvider = provider.getFilteredInstance(FilterMode.WHITELIST_ONLY, whiteListedStacksManager);
        Assert.assertNull(serviceProvider);
    }

    @Test
    public void testGetAllFilteredInstances() {
        when(whiteListedStacksManager.isWhiteListed(eq("/PO/POC7"))).thenReturn(true);
        Collection<ServiceInstance<MetaData>> instances = provider.getAllFilteredInstances(FilterMode.WHITELIST_ONLY, whiteListedStacksManager);
        verifyCollection(instances);
        Assert.assertEquals(2, provider.getAllUniqueInstances().size());

        when(whiteListedStacksManager.isWhiteListed(eq("/PO/POC7"))).thenReturn(false);
        Assert.assertEquals(0, provider.getAllFilteredInstances(FilterMode.WHITELIST_ONLY, whiteListedStacksManager).size());
    }

    private void verifyCollection(Collection<ServiceInstance<MetaData>> instances) {
        Assert.assertEquals(10, instances.size());
        ServiceInstance<MetaData> firstItem = (ServiceInstance<MetaData>) provider.getAllUniqueInstances().toArray()[0];
        ServiceInstance<MetaData> secondItem = (ServiceInstance<MetaData>) provider.getAllUniqueInstances().toArray()[1];
        assertThat(firstItem, isIn(instances));
        assertThat(secondItem, isIn(instances));
    }
}
