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

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OperationContextHolder {
    private static ThreadLocal<OperationContext> currentContext = new ThreadLocal<>();

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    @Qualifier("urlRulesService")
    private IUrlRulesService urlRulesService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IWhiteListService whiteListService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    public void buildContext(String serviceName) {
        createContext(changesStatusService, serviceName);
    }

    public void buildContext(IChangesStatusService changesStatusService, String serviceName) {
        createContext(changesStatusService, serviceName);
    }

    public void buildContextFromOfflineSnapshot(String serviceName, Snapshot snapshot) {
        PendingChangesStatus pendingChangesStatus = snapshot.getPendingChanges();
        SelectServer flavorRules = snapshot.getFlavorRules();
        Namespaces namespaces = namespacedListsService.getAllNamespacedLists();
        Distribution distribution = snapshot.getDistribution();
        Whitelisted whitelist = snapshot.getWhitelist();
        Server server = snapshot.getServers();
        URLRules urlRules = snapshot.getUrlRules();
        urlRules.setDefaultStatement(snapshot.getDefaultUrlParams());
        ServicePaths servicePaths = snapshot.getServicePaths();
        Set<PathItem> activeStacksAndFlavors = stacksService.getActiveStacksAndFlavors(serviceName, servicePaths);
        URLRules templateUrlRules = snapshot.getTemplateUrlRules();
        SelectServer templatesPathRules = snapshot.getTemplatePathRules();
        WhitelistedStackUpdates whitelistedStackUpdates = snapshot.getWhitelistedStackUpdates();

        OperationContext context = new OperationContext(serviceName, pendingChangesStatus, flavorRules, distribution, whitelist, namespaces, server, urlRules, servicePaths, activeStacksAndFlavors, templateUrlRules, templatesPathRules, whitelistedStackUpdates);

        currentContext.set(context);
    }

    public static OperationContext getCurrentContext() {
        return currentContext.get();
    }

    private void createContext(IChangesStatusService changesStatusService, String serviceName) {
        PendingChangesStatus pendingChangesStatus = changesStatusService.getPendingChangesStatus(serviceName);
        SelectServer flavorRules = flavorRulesService.getAllRules(serviceName);
        Namespaces namespaces = namespacedListsService.getAllNamespacedLists();
        Distribution distribution = distributionService.getDistribution(serviceName);
        Whitelisted whitelist = whiteListService.getWhitelistedStacks(serviceName);
        Server server = serverService.getServer(serviceName);
        URLRules urlRules = urlRulesService.getAllRules(serviceName);
        ServicePaths servicePaths = stacksService.getStacksForService(serviceName);
        Set<PathItem> activeStacksAndFlavors = stacksService.getActiveStacksAndFlavors(serviceName);
        URLRules templateUrlRules = null;
        SelectServer templatesPathRules = null;
        WhitelistedStackUpdates whitelistedStackUpdates = whiteListStackUpdateService.getWhitelistedStacksUpdates(serviceName);

        OperationContext context = new OperationContext(serviceName, pendingChangesStatus, flavorRules, distribution, whitelist, namespaces, server, urlRules, servicePaths, activeStacksAndFlavors, templateUrlRules, templatesPathRules, whitelistedStackUpdates);

        currentContext.set(context);
    }

    public static class OperationContext {
        private String serviceName;
        private PendingChangesStatus pendingChangesStatus;
        private SelectServer flavorRules;
        private Distribution distribution;
        private Whitelisted whitelist;
        private Namespaces namespacedLists;
        private Server server;
        private URLRules urlRules;
        private ServicePaths servicePaths;
        private Set<PathItem> activeStacksAndFlavors;
        private URLRules templateUrlRules;
        private SelectServer templatePathRules;
        private WhitelistedStackUpdates whitelistedStackUpdates;

        private OperationContext(String serviceName,
                                 PendingChangesStatus pendingChangesStatus,
                                 SelectServer flavorRules,
                                 Distribution distribution,
                                 Whitelisted whitelist,
                                 Namespaces namespacedLists,
                                 Server server,
                                 URLRules urlRules,
                                 ServicePaths servicePaths,
                                 Set<PathItem> activeStacksAndFlavors,
                                 URLRules templateUrlRules,
                                 SelectServer templatesPathRules,
                                 WhitelistedStackUpdates whitelistedStackUpdates) {
            this.serviceName = serviceName;
            this.pendingChangesStatus = pendingChangesStatus;
            this.flavorRules = flavorRules;
            this.distribution = distribution;
            this.whitelist = whitelist;
            this.namespacedLists = namespacedLists;
            this.server = server;
            this.urlRules = urlRules;
            this.servicePaths = servicePaths;
            this.activeStacksAndFlavors = activeStacksAndFlavors;
            this.templateUrlRules = templateUrlRules;
            this.templatePathRules = templatesPathRules;
            this.whitelistedStackUpdates = whitelistedStackUpdates;
        }

        public String getServiceName() {
            return serviceName;
        }

        public PendingChangesStatus getPendingChangesStatus() {
            return pendingChangesStatus;
        }

        public SelectServer getFlavorRules() {
            return flavorRules;
        }

        public IfExpression getFlavorRule(String ruleName) {
            return flavorRules.getRule(ruleName);
        }

        public Distribution getDistribution() {
            return distribution;
        }

        public Whitelisted getWhitelist() {
            return whitelist;
        }

        public Namespaces getNamespacedLists() {
            return namespacedLists;
        }

        public Server getServer() {
            return server;
        }

        public URLRules getUrlRules() {
            return urlRules;
        }

        public IfExpression getUrlRule(String ruleName) {
            return urlRules.getUrlRule(ruleName);
        }

        public ServicePaths getServicePaths() {
            return servicePaths;
        }

        public Set<PathItem> getActiveStacksAndFlavors() {
            return activeStacksAndFlavors;
        }

        public URLRules getTemplateUrlRules(){ return templateUrlRules;}

        public SelectServer getTemplatePathRules() {
            return templatePathRules;
        }

        public WhitelistedStackUpdates getWhitelistedStackUpdates() {return  whitelistedStackUpdates;}
    }
}
