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

package com.comcast.apps.e2e.helpers;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.*;

public class ServicePathHelper {

    public static final String REDIRECTOR_CONFIG_PATH = "redirectorConfig";
    public static final String RULE_PATH = "rule";
    public static final String URL_RULE_PATH = "urlRule";

    private String serviceName;

    public ServicePathHelper() {
    }

    public ServicePathHelper(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRedirectorConfigServicePath() {
        return Stream.of(SETTINGS, REDIRECTOR_CONFIG_PATH).collect(Collectors.joining(DELIMETER));
    }

    public String getWhitelistedServicePath() {
        return Stream.of(WHITELISTED_CONTROLLER_PATH, serviceName).collect(Collectors.joining(DELIMETER));
    }

    public String getDefaultServerServicePath() {
        return Stream.of(SERVERS_CONTROLLER_PATH, serviceName, DEFAULT_SERVER_NAME).collect(Collectors.joining(DELIMETER));
    }

    public String getDistributionServicePath() {
        return Stream.of(DISTRIBUTION_CONTROLLER_PATH, serviceName).collect(Collectors.joining(DELIMETER));
    }

    public String getFlavorRulesServicePath(String ruleId) {
        return Stream.of(RULES_CONTROLLER_PATH, serviceName, ruleId).collect(Collectors.joining(DELIMETER));
    }

    public String getDefaultUrlParamsServicePath() {
        return Stream.of(URL_RULES_CONTROLLER_PATH, serviceName, PENDING_STATUS_URL_PARAMS, DEFAULT_URL_RULE)
            .collect(Collectors.joining(DELIMETER));
    }

    public String getUrlRulesServicePath(String urlRuleId) {
        return Stream.of(URL_RULES_CONTROLLER_PATH, serviceName, urlRuleId).collect(Collectors.joining(DELIMETER));
    }

    public String getPendingChangesServicePath() {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName).collect(Collectors.joining(DELIMETER));
    }

    public String getModelReloadServicePath() {
        return Stream.of(MODEL_RELOAD_PATH, serviceName).collect(Collectors.joining(DELIMETER));
    }

    public String getPendingChangesApproveServicePath(String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, PENDING_STATUS_APPROVE, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    String getPendingChangesApproveRuleServicePath(String ruleId, String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, RULE_PATH, ruleId, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    String getPendingChangesApproveUrlRuleServicePath(String urlRuleId, String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, URL_RULE_PATH, urlRuleId, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    String getPendingChangesApproveDefaultUrlParamsServicePath(String currentChangeVersion) {
        return Stream.of(URL_RULES_CONTROLLER_PATH, serviceName, PENDING_STATUS_URL_PARAMS, DEFAULT_URL_RULE, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    public String getPendingChangesApproveWhitelistedServicePath(String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, PENDING_STATUS_STACKMANAGEMENT, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    String getPendingChangesApproveDefaultServerServicePath(String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, SERVER_PATH, currentChangeVersion).collect(Collectors.joining(DELIMETER));
    }

    String getPendingChangesApproveDistributionServicePath(String currentChangeVersion) {
        return Stream.of(PENDING_CONTROLLER_PATH, serviceName, DISTRIBUTION_PATH, currentChangeVersion)
            .collect(Collectors.joining(DELIMETER));
    }

    public String getNamespacedListPostOneNamespacedServicePath(String namespacedListName) {
        return Stream.of(NAMESPACE_CONTROLLER_PATH, "addNewNamespaced", namespacedListName).collect(Collectors.joining(DELIMETER));
    }
}
