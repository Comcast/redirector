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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.xre.redirector.test;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.balancer.serviceprovider.IAggregateServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.AggregateServiceProvider;
import junit.framework.Assert;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServiceProviderTest {
    private static final String FLAVOR = "FLAVOR";

    @Test
    public void testAggregateServiceProvider() {
        List<ServiceInstance<MetaData>> firstServiceList  = Arrays.asList(buildServiceInstance(0), buildServiceInstance(1), buildServiceInstance(2));
        List<ServiceInstance<MetaData>> secondServiceList = Arrays.asList(buildServiceInstance(3), buildServiceInstance(4), buildServiceInstance(5));
        List<ServiceInstance<MetaData>> thirdServiceList  = Arrays.asList(buildServiceInstance(6), buildServiceInstance(7), buildServiceInstance(8));

        List<IFilteredServiceProvider> serviceProviders = new ArrayList<>();
        serviceProviders.add(new TestServiceProvider(new RoundRobinStrategy(), firstServiceList, new XreStackPath("/po/poc1/" + FLAVOR + "/xreGuide")));
        serviceProviders.add(new TestServiceProvider(new RoundRobinStrategy(), secondServiceList, new XreStackPath("/po/poc2/" + FLAVOR + "/xreGuide")));
        serviceProviders.add(new TestServiceProvider(new RoundRobinStrategy(), thirdServiceList, new XreStackPath("/po/poc3/" + FLAVOR + "/xreGuide")));

        List<ServiceInstance> allServiceInstances = new ArrayList<>();
        allServiceInstances.addAll(firstServiceList);
        allServiceInstances.addAll(secondServiceList);
        allServiceInstances.addAll(thirdServiceList);

        IAggregateServiceProvider aggregateServiceProvider = new AggregateServiceProvider(
                new RoundRobinStrategy(), serviceProviders, FLAVOR);

        Assert.assertEquals(serviceProviders, aggregateServiceProvider.getAggregatedProviders());
        try {
            Assert.assertEquals(allServiceInstances, aggregateServiceProvider.getAllInstances());
        } catch (Exception ex) {
            Assert.fail("Unexpected exception " + ex.toString());
        }
        try {
            for (ServiceInstance serviceInstance : allServiceInstances) { // RoundRobin strategy order is expected to be preserved
                Assert.assertEquals(serviceInstance, aggregateServiceProvider.getInstance());
            }
        } catch (Exception ex) {
            Assert.fail("Unexpected exception " + ex.toString());
        }
    }

    @Test
    public void testAggregateServiceProviderUnsupported() {
        IFilteredServiceProvider aggregateServiceProvider = new AggregateServiceProvider(new RoundRobinStrategy(),
                Collections.<IFilteredServiceProvider>emptyList(), FLAVOR);

        try {
            aggregateServiceProvider.close();
            Assert.fail("UnsupportedOperationException expected");
        } catch (Exception ex) {
            Assert.assertTrue("UnsupportedOperationException expected", ex instanceof UnsupportedOperationException);
        }
    }

    private ServiceInstance<MetaData> buildServiceInstance(int id) {
        try {
            String strId = String.valueOf(id);
            return ServiceInstance.<MetaData>builder().payload(new MetaData()).name(strId).id(strId).build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
