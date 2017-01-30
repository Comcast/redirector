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

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Mockito.mock;

@Ignore
public class DynamicServiceProviderManagerTest {
    private MockServiceProviderFactory serviceProviderFactory;
    private TestServiceProviderManager testee;

    @Before
    public void setUp() throws Exception {
        serviceProviderFactory = new MockServiceProviderFactory();


        testee = new TestServiceProviderManager();
        testee.switchToDynamicMode(serviceProviderFactory);
    }

    @Test
    public void testWithAddedStacksOnCleanServiceProviders() throws Exception {
        Set<XreStackPath> addedStacks = new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone1/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor2/app1"));
        }};
        Set<XreStackPath> deletedStacks = Collections.emptySet();

        testee.applyStacksChanges(addedStacks, deletedStacks);

        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor1", "app1"));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor2", "app1"));
        verifyStack("/dc/zone1/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor2/app1", testee.getAllStackServiceProviders());
    }

    @Test
    public void testWithAddedAndDeletedStacksOnCleanServiceProviders() throws Exception {
        Set<XreStackPath> addedStacks = new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone1/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor2/app1"));
        }};
        Set<XreStackPath> deletedStacks = new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone1/flavor1/app1"));
            add(new XreStackPath("/dc/zone3/flavor2/app1"));
        }};

        testee.applyStacksChanges(addedStacks, deletedStacks);

        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor1", "app1"));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor2", "app1"));
        verifyStack("/dc/zone1/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor2/app1", testee.getAllStackServiceProviders());
    }

    @Test
    public void testWithAddedAndDeletedStacksOnFilledServiceProviders() throws Exception {
        testee.applyStacksChanges(new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone3/flavor2/app1"));
            add(new XreStackPath("/dc/zone4/flavor1/app1"));
            add(new XreStackPath("/dc/zone5/flavor3/app1"));
        }}, Collections.emptySet());

        Set<XreStackPath> addedStacks = new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone1/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor1/app1"));
            add(new XreStackPath("/dc/zone2/flavor2/app1"));
        }};
        Set<XreStackPath> deletedStacks = new LinkedHashSet<XreStackPath>() {{
            add(new XreStackPath("/dc/zone1/flavor1/app1"));
            add(new XreStackPath("/dc/zone3/flavor2/app1"));
        }};

        testee.applyStacksChanges(addedStacks, deletedStacks);

        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor1", "app1"));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor3", "app1"));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp("flavor2", "app1"));
        verifyStack("/dc/zone4/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone5/flavor3/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone1/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor1/app1", testee.getAllStackServiceProviders());
        verifyStack("/dc/zone2/flavor2/app1", testee.getAllStackServiceProviders());
    }

    private void verifyStack(String expectedPath, List<IFilteredServiceProvider> result) {
        assertThat(expectedPath, isIn(result.stream().map(provider -> provider.getStack().getPath()).collect(Collectors.toList())));
    }

    private static class MockServiceProviderFactory implements DynamicServiceProviderFactory {

        @Override
        public IFilteredServiceProvider createServiceProvider(XreStackPath servicePath) {
            MockFilteredServiceProvider provider = mock(MockFilteredServiceProvider.class, Mockito.CALLS_REAL_METHODS);
            provider.setStack(servicePath);
            return provider;
        }
    }

    private abstract static class MockFilteredServiceProvider implements IFilteredServiceProvider {
        private XreStackPath stack;

        public void setStack(XreStackPath stack) {
            this.stack = stack;
        }

        @Override
        public XreStackPath getStack() {
            return stack;
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static class TestServiceProviderManager extends AbstractServiceProviderManager {

        TestServiceProviderManager() {
            super(new RoundRobinStrategy<>(), new Config());
        }
    }
}
