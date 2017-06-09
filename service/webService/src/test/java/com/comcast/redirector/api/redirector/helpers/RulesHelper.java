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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.common.RedirectorConstants;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class RulesHelper {

    private static final String RULES_SERVICE_PATH = RedirectorConstants.RULES_CONTROLLER_PATH;

    public static final String _RULE = "rule";
    public static final String _TEMPLATE_FLAVOR_RULE = "templateRule";

    public static IfExpression postRule(String serviceName, String ruleId, IfExpression rule, String mediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        return ServiceHelper.post(webTarget, rule, mediaType);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static IfExpression postRule(String serviceName, IfExpression rule) {
        return postRule(serviceName, rule.getId(), rule, MediaType.APPLICATION_JSON);
    }

    public static <T> T getRule(WebTarget target, String serviceName, String ruleId, String responseMediaType,
                                Class<T> responseClassType) {
        WebTarget webTarget = target.path(RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        return ServiceHelper.get(webTarget, responseMediaType, responseClassType);
    }

    /**
     * simple post - without checking response and specific mediaType
     */
    public static IfExpression getRule(WebTarget target, String serviceName, String ruleId) {
        WebTarget webTarget = target.path(RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, IfExpression.class);
    }

    public static SelectServer getAllRules(String serviceName, String responseMediaType) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(serviceName);
        return ServiceHelper.get(webTarget, responseMediaType, SelectServer.class);
    }

    /**
     * simple get - without checking specific mediaType
     */
    public static SelectServer getAllRules(String serviceName) {
        return getAllRules(serviceName, MediaType.APPLICATION_JSON);
    }

    public static void deleteRule(WebTarget target, String serviceName, String ruleId) {
        WebTarget webTarget = target.path(RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        ServiceHelper.delete(webTarget);
    }

    public static int deleteRuleWithResponce(WebTarget target, String serviceName, String ruleId) {
        WebTarget webTarget = target.path(RULES_SERVICE_PATH).path(serviceName).path(ruleId);
        return ServiceHelper.deleteWithResponce(webTarget);
    }

    public static void deleteAllRules(WebTarget target, String serviceName) {
        SelectServer rules = getAllRules(serviceName);
        if (rules.getItems() != null && !rules.getItems().isEmpty()) {
            for (IfExpression rule : rules.getItems()) {
                deleteRule(target, serviceName, rule.getId());
            }
        }
    }

    public static void makeRuleWithUpdatedStatus(WebTarget target, String serviceName, IfExpression rule,
                                                 IfExpression updatedRule) throws AssertionError {
        // post our rule
        postRule(serviceName, rule);

        // approve rule
        approvePendingChanges(target, serviceName, rule.getId());

        // post updated rule
        postRule(serviceName, updatedRule);
    }

    public static void makeRuleWithDeletedStatus(WebTarget target, String serviceName, IfExpression rule)
            throws AssertionError {
        // post our rule
        postRule(serviceName, rule);

        // approve rule
        approvePendingChanges(target, serviceName, rule.getId());

        // delete rule from service - create DELETE pending change
        deleteRule(target, serviceName, rule.getId());
    }
// ************************************* RULES PENDING CHANGES  *************************************** //

    public static void approvePendingChanges(WebTarget target, String serviceName, String ruleId) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_RULE).path(ruleId).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void approvePendingFlavorTemplateChanges(WebTarget target, String serviceName, String ruleId) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_TEMPLATE_FLAVOR_RULE).path(ruleId).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.approvePendingChanges(webTarget);
    }

    public static void cancelPendingChanges(WebTarget target, String serviceName, String ruleId) throws AssertionError {
        Integer currentChangeVersion = PendingChangesHelper.getCurrentChangeVersion(serviceName);
        WebTarget webTarget = target.path(PendingChangesHelper.PENDING_SERVICE_PATH).path(serviceName)
                .path(_RULE).path(ruleId).path(String.valueOf(currentChangeVersion));
        PendingChangesHelper.cancelPendingChanges(webTarget);
    }

    public static RuleIdsWrapper getRulesIds(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(serviceName).path("ids");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, RuleIdsWrapper.class);
    }

    public static IfExpression exportRules(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(serviceName).path("export");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, IfExpression.class);
    }

    public static IfExpression exportRule(String serviceName, String urlRuleId) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(serviceName).path("export").path(urlRuleId);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, IfExpression.class);
    }
}
