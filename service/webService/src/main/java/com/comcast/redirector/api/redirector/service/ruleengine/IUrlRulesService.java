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

package com.comcast.redirector.api.redirector.service.ruleengine;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;

import java.util.Collection;
import java.util.Map;

public interface IUrlRulesService {
    Collection<IfExpression> getUrlRules(String appName);
    Map<String, IfExpression> getAllRulesInMap(String appName);
    URLRules getAllRules(String appName);
    Collection<String> getUrlRuleIdsByServiceName(String appName);
    IfExpression getUrlRule(String appName, String ruleId);
    Map<String, PendingChange> getUrlRules(PendingChangesStatus pendingChangesStatus);
    void deleteUrlRule(String appName, String ruleId);

    OperationResult deleteUrlRule(String appName, String ruleId, ApplicationStatusMode mode);

    OperationResult deleteTemplateUrlRule(String appName, String ruleId);

    void saveUrlRule(String appName, IfExpression rule, String ruleId);
    OperationResult saveUrlRule(String appName, IfExpression pendingRule, ApplicationStatusMode mode);
    EntityType getEntityType();
}
