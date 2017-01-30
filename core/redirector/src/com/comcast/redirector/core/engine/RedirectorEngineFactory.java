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
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.balancer.Balancer;
import com.comcast.redirector.core.balancer.IBalancer;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhitelistPredicate;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.rules.FlavorRuleSet;
import com.comcast.redirector.core.engine.rules.URLRuleSet;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.function.Predicate;

public class RedirectorEngineFactory implements IRedirectorEngineFactory {
    public enum BackupMode {IN_MEMORY, FILE_SYSTEM}

    private IServiceProviderManagerFactory serviceProviderManagerFactory;
    private Serializer serializer;
    private ZKConfig config;
    private Predicate<String> isStaticDiscoveryNeededForApp;

    public RedirectorEngineFactory(IServiceProviderManagerFactory serviceProviderManagerFactory) {
        this.serviceProviderManagerFactory = serviceProviderManagerFactory;
    }

    @Autowired(required = false)
    public void setConfig(ZKConfig config) {
        this.config = config;
    }

    @Autowired(required = false)
    public void setIsStaticDiscoveryNeededForApp(Predicate<String> isStaticDiscoveryNeededForApp) {
        this.isStaticDiscoveryNeededForApp = isStaticDiscoveryNeededForApp;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public IRedirectorEngine newRedirectorEngine(String appName, Model flavorRules, URLRuleModel urlRules, WhiteList whiteList, Set<StackData> stacks) {
        return newRedirectorEngine(appName, flavorRules, urlRules, whiteList, stacks, RedirectorConstants.NO_MODEL_NODE_VERSION);
    }

    @Override
    public IRedirectorEngine newRedirectorEngine(String appName, Model flavorRules, URLRuleModel urlRules, WhiteList whiteList, Set<StackData> allStacks, int modelVersion) {
        ILoggable.ISessionLog sessionLog = config.isEndToEndModeEnabled() ? new RedirectorEngine.SessionLog() : null;
        IServiceProviderManager serviceProviderManager = (isStaticDiscoveryNeededForApp.test(appName))
            ? serviceProviderManagerFactory.newStaticServiceProviderManager(allStacks)
            : serviceProviderManagerFactory.newDynamicServiceProviderManager();

        return newRedirectorEngine(appName, flavorRules, urlRules, whiteList, serviceProviderManager, sessionLog, modelVersion);
    }

    private IRedirectorEngine newRedirectorEngine(String appName, Model flavorRules, URLRuleModel urlRules, WhiteList whiteList,
                                                  IServiceProviderManager serviceProviderManager, ILoggable.ISessionLog sessionLog, int modelVersion) {

        IBalancer balancer = new Balancer(appName, serviceProviderManager, new WhitelistPredicate(whiteList), modelVersion);

        return new RedirectorEngine(
            appName,
            balancer,
            new FlavorRuleSet(flavorRules),
            new URLRuleSet.Builder()
                .setModel(urlRules)
                .setFallbackIPProtocolVersion(config.getFallbackIPProtocolVersion())
                .setFallbackPort(config.getFallbackPort())
                .setFallbackProtocol(config.getFallbackProtocol())
                .setFallbackUrn(config.getFallbackUrn())
                .build(),
            sessionLog,
            modelVersion);
    }

    // TODO: this method is needed only for Web Service. Should we move it there?
    @Override
    public IRedirectorEngine newRedirectorEngine(String appName,
                                                 SelectServer flavorRules,
                                                 URLRules urlRules,
                                                 Whitelisted whitelists,
                                                 NamespacedListRepository namespacedLists,
                                                 Set<StackData> stacks,
                                                 ILoggable.ISessionLog sessionLog) {
        if (serializer == null) {
            throw new IllegalStateException("RedirectorEngineFactory should be initialized with serializer before using this method");
        }

        ModelTranslationService modelTranslationService = new ModelTranslationService(serializer);

        Model flavorRuleSet = modelTranslationService.translateFlavorRules(flavorRules, namespacedLists);
        URLRuleModel urlRuleSet = modelTranslationService.translateUrlRules(urlRules, namespacedLists);
        WhiteList whiteList = modelTranslationService.translateWhitelistedStacks(whitelists);

        IServiceProviderManager serviceProviderManager = serviceProviderManagerFactory.newStaticServiceProviderManager(stacks);

        return newRedirectorEngine(appName, flavorRuleSet, urlRuleSet, whiteList, serviceProviderManager, sessionLog, RedirectorConstants.NO_MODEL_NODE_VERSION);
    }

    @Override
    public IRedirectorEngine.IHostSelector newHostSelector(String appName, IServiceProviderManager serviceProviderManager, WhiteList whiteList, Model flavorRulesModel) {
        IBalancer balancer = new Balancer(appName, serviceProviderManager, new WhitelistPredicate(whiteList), RedirectorConstants.NO_MODEL_NODE_VERSION);
        return new RedirectorEngine.HostSelector.Builder().setAppName(appName).setBalancer(balancer).setFlavorRules(new FlavorRuleSet(flavorRulesModel)).build();
    }
}
