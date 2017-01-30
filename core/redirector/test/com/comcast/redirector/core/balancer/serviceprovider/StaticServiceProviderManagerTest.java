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

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

public class StaticServiceProviderManagerTest {
    private StaticServiceProviderManager testee;

    @Test
    public void testRefresh() {
        final String STACK_1 = "/PO/POC7/1.45/xreGuide";
        final String STACK_2 = "/BR/BRC7/1.45/xreGuide";
        final String STACK_3 = "/PO/POC8/1.49/xreGuide";
        final String STACK_4 = "/BR/BRC7/1.49/xreGuide";
        final String FLAVOR_1 = "1.45";
        final String FLAVOR_2 = "1.49";
        final String APP = "xreGuide";

        testee = new StaticServiceProviderManager(new HashSet<StackData>() {{
            add(new StackData(STACK_1));
            add(new StackData(STACK_2));
            add(new StackData(STACK_3));
            add(new StackData(STACK_4));
        }}, new Config(), new RoundRobinStrategy<>());

        Assert.assertNotNull(testee.getStackServiceProvider(new StackData(STACK_1)));
        Assert.assertNotNull(testee.getStackServiceProvider(new StackData(STACK_2)));
        Assert.assertNotNull(testee.getStackServiceProvider(new StackData(STACK_3)));
        Assert.assertNotNull(testee.getStackServiceProvider(new StackData(STACK_4)));
        Assert.assertNull(testee.getStackServiceProvider(new StackData("/PO/POC7/1.55/xreGuide")));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp(FLAVOR_1, APP ));
        Assert.assertNotNull(testee.getServiceProviderForFlavorAndApp(FLAVOR_2, APP ));
    }
}
