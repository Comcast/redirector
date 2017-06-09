/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
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

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.helpers.ServicePathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.apps.e2e.utils.CommonUtils;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

public class SetUpEnvManager implements ISetUpEnvManager {
    private static final Logger log = LoggerFactory.getLogger(SetUpEnvManager.class);

    private final String serviceName;
    private final Context context;
    private final ServicePathHelper servicePathHelper;
    private final ServiceHelper serviceHelper;

    public SetUpEnvManager(Context context, ServiceHelper serviceHelper) {
        this.serviceName = context.getServiceName();
        this.servicePathHelper = context.getServicePathHelper();
        this.context = context;
        this.serviceHelper = serviceHelper;
    }

    public void setUp() throws Exception {
        setupStacksWhiteList();
        CommonUtils.waitForHostCachesToUpdate();
        triggerModelReload();
        waitNextStep();

        setupModel();
        CommonUtils.waitForHostCachesToUpdate();
        approveModel();
        waitNextStep();
    }

    private void setupModel() throws Exception {
        setupDistribution();
        setupDefaultServer();
        setupFlavorRules();

        setupUrlRules();
        setupDefaultUrlParams();
    }

    private void waitNextStep() throws InterruptedException {
        CommonUtils.waitForHostCachesToUpdate();
        context.getZookeeperModelReloadDispatcher().waitNextStep();
    }

    private void setupStacksWhiteList() {
        Whitelisted whitelisted = context.getWhitelisted();
        try {
            // TODO: refactor to have 1 all lines in method be of one level of abstraction
            String whitelistedServicePath = servicePathHelper.getWhitelistedServicePath();
            serviceHelper.post(whitelistedServicePath, whitelisted, MediaType.APPLICATION_JSON);
            log.info("Successfully saved whitelisted for '{}' application.", serviceName);
            approveWhitelistedPendingChanges();
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to save whitelisted for '%s' application.", serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void setupDefaultServer() {
        Server defaultServer = context.getDefaultServer();

        try {
            String defaultServerServicePath = servicePathHelper.getDefaultServerServicePath();
            serviceHelper.post(defaultServerServicePath, defaultServer, MediaType.APPLICATION_JSON);
            log.info("Successfully saved defaultServer for '{}' application.", serviceName);
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to save server. Name ='%s', path ='%s' for %s application.", defaultServer.getName(), defaultServer.getPath(), serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void setupDistribution() {
        Distribution distribution = context.getDistribution();

        try {
            String distributionServicePath = servicePathHelper.getDistributionServicePath();
            serviceHelper.post(distributionServicePath, distribution, MediaType.APPLICATION_JSON);
            log.info("Successfully saved distribution for '{}' application.", serviceName);
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to save distribution for '%s' application.", serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void setupFlavorRules() {
        for (IfExpression rule : context.getFlavorRules()) {
            String ruleId = rule.getId();
            String flavorRuleServicePath = servicePathHelper.getFlavorRulesServicePath(ruleId);
            try {
                serviceHelper.post(flavorRuleServicePath, rule, MediaType.APPLICATION_JSON);
                log.info("Successfully saved flavor rule. Rule name '{}' for '{}' application.", ruleId, serviceName);
            } catch (WebApplicationException wae) {
                String error = String.format("Failed to save flavor rule. Rule name ='%s' for %s application.", ruleId, serviceName);
                log.error(error, wae);
                throw new WebApplicationException(error, wae.getResponse().getStatus());
            }
        }
    }

    private void setupDefaultUrlParams() {
        UrlRule urlRule = context.getDefaultUrlRule();
        try {
            String urlDefaultParamsServicePath = servicePathHelper.getDefaultUrlParamsServicePath();
            serviceHelper.post(urlDefaultParamsServicePath, urlRule, MediaType.APPLICATION_JSON);
            log.info("Successfully saved default url params for '{}' application.", serviceName);
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to save default url params for '%s' application.", serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void setupUrlRules() {
        for (IfExpression urlRule : context.getUrlRules().getItems()) {
            String urlRuleId = urlRule.getId();
            String urlRulesServicePath = servicePathHelper.getUrlRulesServicePath(urlRuleId);
            try {
                serviceHelper.post(urlRulesServicePath, urlRule, MediaType.APPLICATION_JSON);
                log.info("Successfully saved url rule. Url Rule name ='{}' for '{}' application.", urlRuleId, serviceName);
            } catch (WebApplicationException wae) {
                String error = String.format("Failed to save url rule. Url rule name ='%s' for '%s' application.", urlRuleId, serviceName);
                log.error(error, wae);
                // TODO: runtime exceptions here
                throw new WebApplicationException(error, wae.getResponse().getStatus());
            }
        }
    }

    private void approveWhitelistedPendingChanges() {
        try {
            String currentChangeVersion = getCurrentChangeVersion();
            String approveWhitelistedPendingChangesServicePath = servicePathHelper.getPendingChangesApproveWhitelistedServicePath(currentChangeVersion);
            approvePendingChanges(approveWhitelistedPendingChangesServicePath);
            log.info("Successfully approve whitelisted pending changes for '{}' application.", serviceName);
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to approve whitelisted pending changes for '%s' application.", serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void triggerModelReload() {
        serviceHelper.post(servicePathHelper.getModelReloadServicePath(), null, MediaType.APPLICATION_JSON);
    }

    private void approvePendingChanges(String pendingChangesServicePath) {
        serviceHelper.post(pendingChangesServicePath, null, MediaType.APPLICATION_JSON);
    }

    private String getCurrentChangeVersion() {
        String pendingChangesServicePath = servicePathHelper.getPendingChangesServicePath();
        PendingChangesStatus currentChanges = serviceHelper.get(pendingChangesServicePath, PendingChangesStatus.class, MediaType.APPLICATION_JSON);
        Integer currentChangeVersion = currentChanges.getVersion();
        return String.valueOf(currentChangeVersion);
    }

    private void approveModel() {
        try {
            String currentChangeVersion = getCurrentChangeVersion();
            String approvePendingChangesServicePath = servicePathHelper.getPendingChangesApproveServicePath(currentChangeVersion);
            serviceHelper.post(approvePendingChangesServicePath, null, MediaType.APPLICATION_JSON);
            log.info("Successfully approve all pending changes for '{}' application.", serviceName);
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to approve all pending changes for '%s' application.", serviceName);
            log.error(error, wae);
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }
}
