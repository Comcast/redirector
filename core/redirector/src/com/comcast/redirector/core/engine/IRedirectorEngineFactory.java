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

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;

import java.util.Set;

public interface IRedirectorEngineFactory {
    /**
     * Creates {@link IRedirectorEngine} instance for given application based on xreredirector-common model classes and current stacks
     *
     * @param appName application name e.g. pandora or xreGuide
     * @param flavorRules flavor rules
     * @param urlRules url rules
     * @param whiteList whitelist
     * @param stacks currently available stacks with hosts
     * @return {@link IRedirectorEngine} instance able to redirect
     */
    IRedirectorEngine newRedirectorEngine(String appName, Model flavorRules, URLRuleModel urlRules, WhiteList whiteList, Set<StackData> stacks);

    /**
     * Creates {@link IRedirectorEngine} instance for given application based on redirector-common model classes and current stacks
     *
     * @param appName application name e.g. pandora or xreGuide
     * @param flavorRules {@link SelectServer} instance representing url rules
     * @param urlRules {@link URLRules} instance representing url rules
     * @param whitelists whitelist
     * @param namespacedListsHolder namespaced lists
     * @param stacks currently available stacks with hosts
     * @return {@link IRedirectorEngine} instance able to redirect
     */
    IRedirectorEngine newRedirectorEngine(String appName, SelectServer flavorRules, URLRules urlRules,
                                          Whitelisted whitelists,
                                          NamespacedListRepository namespacedListsHolder,
                                          Set<StackData> stacks, ILoggable.ISessionLog sessionLog);

    /**
     * Creates {@link IRedirectorEngine.IHostSelector} instance for given application name, {@link IServiceProviderManager} and flavor rules.
     * Given instance is able to get number of hosts currently available for default server and distribution etc.
     *
     * @param appName application name e.g. pandora or xreGuide
     * @param serviceProviderManager {@link IServiceProviderManager} instance able to give service provider for given path
     * @param flavorRulesModel flavor rules able to return server by given context
     * @return {@link IRedirectorEngine.IHostSelector} instance for given application name, {@link IServiceProviderManager} and flavor rules.
     */
    IRedirectorEngine.IHostSelector newHostSelector(String appName, IServiceProviderManager serviceProviderManager, WhiteList whiteList, Model flavorRulesModel);

    IRedirectorEngine newRedirectorEngine(String appName, Model model, URLRuleModel urlRuleModel, WhiteList whiteList, Set<StackData> allStacks, int modelVersion);
}
