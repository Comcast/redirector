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
 */
package com.comcast.redirector.core.engine;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static com.comcast.redirector.core.engine.RedirectorEngine.SessionLog;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedirectorEngineFactoryTest {

    private IServiceProviderManagerFactory serviceProviderManagerFactory;
    private ZKConfig zkConfig;
    private RedirectorEngineFactory engineFactory;
    private String appName = "xreGuide";
    private Model flavorRules;
    private URLRuleModel urlRules;
    private WhiteList whiteList;
    Set<StackData> stacks;

    @Before
    public void setUp() {
        serviceProviderManagerFactory = mock(IServiceProviderManagerFactory.class);
        StackData stackData = mock(StackData.class);
        zkConfig = mock(ZKConfig.class);
        flavorRules = mock(Model.class);
        urlRules = mock(URLRuleModel.class);
        whiteList = mock(WhiteList.class);
        stacks = Collections.singleton(stackData);

        engineFactory = new RedirectorEngineFactory(serviceProviderManagerFactory);
        engineFactory.setIsStaticDiscoveryNeededForApp(app -> true);
        engineFactory.setConfig(zkConfig);
    }

    @Test
    public void createRedirectorEngineTestInDirectMode() {
        when(zkConfig.isEndToEndModeEnabled()).thenReturn(false);

        RedirectorEngine redirectorEngine = (RedirectorEngine) engineFactory.newRedirectorEngine(appName, flavorRules, urlRules, whiteList, stacks);
        Assert.assertEquals(null, redirectorEngine.getLog().pollAll());
    }

    @Test
    public void createRedirectorEngineTestInEndToEndMode() {
        when(zkConfig.isEndToEndModeEnabled()).thenReturn(true);

        RedirectorEngine redirectorEngine = (RedirectorEngine) engineFactory.newRedirectorEngine(appName, flavorRules, urlRules, whiteList, stacks);

        SessionLog log = (SessionLog) redirectorEngine.getLog();
        verifyEndToEndMode(log);

    }

    private void verifyEndToEndMode(SessionLog log) {
        Assert.assertNotEquals(null, log.pollAll());
        Assert.assertNotEquals(null, log.pollAll().getSessions());
        Assert.assertEquals(0, log.pollAll().getSessions().size());
    }
}
