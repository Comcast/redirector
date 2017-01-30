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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;

import java.util.Collections;
import java.util.Set;

class ZookeeperRedirectorEnvLoader implements RedirectorTestSuiteService.IRedirectorEnvLoader {
    private String serviceName;
    private IEntityViewService<SelectServer> flavorRulesEntityViewService;
    private IEntityViewService<URLRules> urlRulesEntityViewService;
    private IEntityViewService<Whitelisted> whitelistedEntityViewService;
    private IEntityViewService<Distribution> distributionEntityViewService;
    private IEntityViewService<Server> defaultServerEntityViewService;
    private IStacksService stacksService;
    private INamespacedListsService namespacedListsService;

    public ZookeeperRedirectorEnvLoader(String serviceName,
                                        IEntityViewService<SelectServer> flavorRulesEntityViewService,
                                        IEntityViewService<URLRules> urlRulesEntityViewService,
                                        IEntityViewService<Whitelisted> whitelistedEntityViewService,
                                        IEntityViewService<Distribution> distributionEntityViewService,
                                        IEntityViewService<Server> defaultServerEntityViewService,
                                        IStacksService stacksService,
                                        INamespacedListsService namespacedListsService) {
        this.serviceName = serviceName;
        this.flavorRulesEntityViewService = flavorRulesEntityViewService;
        this.urlRulesEntityViewService = urlRulesEntityViewService;
        this.whitelistedEntityViewService = whitelistedEntityViewService;
        this.distributionEntityViewService = distributionEntityViewService;
        this.defaultServerEntityViewService = defaultServerEntityViewService;
        this.stacksService = stacksService;
        this.namespacedListsService = namespacedListsService;
    }

    @Override
    public NamespacedListsBatch getNamespacedListsBatch() {
        return RedirectorTestSuiteService.getNamespacedListsBatch(namespacedListsService.getAllNamespacedLists());
    }

    @Override
    public Set<StackData> getStacks() {
        Set<StackData> stacks = stacksService.getAllStacksAndHosts(serviceName);
        return (stacks != null) ? stacks : Collections.<StackData>emptySet();
    }

    @Override
    public SelectServer getFlavorRules() {
        SelectServer rules = flavorRulesEntityViewService.getEntity(serviceName);
        Server defaultServer = defaultServerEntityViewService.getEntity(serviceName);
        Distribution distribution = distributionEntityViewService.getEntity(serviceName);
        if (distribution != null) {
            distribution.setDefaultServer(defaultServer);
        }
        rules.setDistribution(distribution);
        return rules;
    }

    @Override
    public URLRules getUrlRules() {
        return urlRulesEntityViewService.getEntity(serviceName);
    }

    @Override
    public Whitelisted getWhitelists() {
        return whitelistedEntityViewService.getEntity(serviceName);
    }
}
