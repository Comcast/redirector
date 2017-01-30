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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.redirectortestsuite;


import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndToEndService implements IEndToEndService {

    @Autowired
    private IRedirectorEngineFactory redirectorEngineFactory;

    @Autowired
    private IEntityViewService<SelectServer> currentFlavorRulesEntityViewService;

    @Autowired
    private IEntityViewService<URLRules> currentUrlRulesEntityViewService;

    @Autowired
    private IEntityViewService<Whitelisted> currentWhitelistedEntityViewService;

    @Autowired
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Server> currentDefaultServerEntityViewService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Override
    public RedirectorTestCaseList getRedirectorTestCases(String serviceName) {
        ZookeeperRedirectorEnvLoader redirectorEnvLoader = new ZookeeperRedirectorEnvLoader(serviceName, currentFlavorRulesEntityViewService, currentUrlRulesEntityViewService, currentWhitelistedEntityViewService, currentDistributionEntityViewService, currentDefaultServerEntityViewService, stacksService, namespacedListsService);
        AutoTestRunner testRunner = new AutoTestRunner(
                redirectorEnvLoader,
                serviceName,
                redirectorEngineFactory);
        List<RedirectorTestCase> testCases = testRunner.createTestCases();
        RedirectorTestCaseList redirectorTestCaseList = new RedirectorTestCaseList();
        redirectorTestCaseList.setRedirectorTestCases(testCases);

        return redirectorTestCaseList;
    }
}
