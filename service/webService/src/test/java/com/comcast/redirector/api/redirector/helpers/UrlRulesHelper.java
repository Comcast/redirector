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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class UrlRulesHelper {

    private static final String URL_RULES_SERVICE_PATH = RedirectorConstants.URL_RULES_CONTROLLER_PATH;

    public static final String _URL_RULE = "urlRule";
    public static final String _URL_PARAMS = "urlParams";
    public static final String _DEFAULT = "default";

    public static IfExpression postUrlRule(String serviceName, String urlRuleId, IfExpression urlRule, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path(urlRuleId);
        return ServiceHelper.post(webTarget, urlRule, mediaType);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static IfExpression postUrlRule(String serviceName, IfExpression urlRule) {
        return postUrlRule(serviceName, urlRule.getId(), urlRule, MediaType.APPLICATION_JSON);
    }

    public static URLRules getAllUrlRules(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, URLRules.class);
    }

    /**
     * simple get - without checking specific mediaType
     */
    public static URLRules getAllUrlRules(String serviceName) {
        return getAllUrlRules(serviceName, MediaType.APPLICATION_JSON);
    }

    public static RuleIdsWrapper getUrlRulesIds(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path("ids");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, RuleIdsWrapper.class);
    }


    public static UrlRule getUrlRule(String serviceName, String urlRuleId, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path(urlRuleId);
        return ServiceHelper.get(webTarget, responseMediaType, UrlRule.class);
    }

    public static IfExpression getUrlRuleExpression(String serviceName, String urlRuleId, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path(urlRuleId);
        return ServiceHelper.get(webTarget, responseMediaType, IfExpression.class);
    }

    public static Default getUrlParamsRule(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName)
                .path("defaultUrlParams");
        return ServiceHelper.get(webTarget, responseMediaType, Default.class);
    }

    public static void deleteUrlRule(String serviceName, String ruleId) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        ServiceHelper.delete(webTarget);
    }

    public static void deleteAllUrlRules(String serviceName) {
        URLRules rules = getAllUrlRules(serviceName, MediaType.APPLICATION_JSON);
        if (rules.getItems() != null && !rules.getItems().isEmpty()) {
            for (IfExpression rule : rules.getItems()) {
                deleteUrlRule(serviceName, rule.getId());
            }
        }
    }

    public static UrlRule postUrlParams(String serviceName, UrlRule urlParams, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName)
                .path(_URL_PARAMS).path(_DEFAULT);
        return ServiceHelper.post(webTarget, urlParams, mediaType);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static UrlRule postUrlParams(String serviceName, UrlRule urlParams) {
        return postUrlParams(serviceName, urlParams, MediaType.APPLICATION_JSON);
    }

// ************************************* URL RULES PENDING CHANGES  *************************************** //

    public static void makeUrlRuleWithUpdatedStatus(WebTarget target, String serviceName, IfExpression urlRule,
                                              IfExpression updatedUrlRule) throws AssertionError {
        // post our urlRule
        postUrlRule(serviceName, urlRule);

        // approve urlRule
        approveUrlRulePendingChanges(target, serviceName, urlRule.getId());

        // post updated urlRule
        postUrlRule(serviceName, updatedUrlRule);
    }

    public static void makeUrlRuleWithDeletedStatus(WebTarget target, String serviceName, IfExpression urlRule)
            throws AssertionError {
        // post our urlRule
        postUrlRule(serviceName, urlRule);

        // approve urlRule
        approveUrlRulePendingChanges(target, serviceName, urlRule.getId());

        // delete urlRule from service - create DELETE pending change
        deleteUrlRule(serviceName, urlRule.getId());
    }

    public static void approveUrlRulePendingChanges(WebTarget target, String serviceName, String ruleId)
            throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_URL_RULE).path(ruleId).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelUrlRulePendingChanges(WebTarget target, String serviceName, String ruleId)
            throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_URL_RULE).path(ruleId).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

    public static void approveUrlParamsPendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_URL_PARAMS).path(_DEFAULT).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelUrlParamsPendingChanges(WebTarget target, String serviceName) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_URL_PARAMS).path(_DEFAULT).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

    public static IfExpression exportUrlRules(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path("export");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, IfExpression.class);
    }

    public static IfExpression exportUrlRule(String serviceName, String urlRuleId) {
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(serviceName).path("export").path(urlRuleId);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, IfExpression.class);
    }
}
