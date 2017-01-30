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
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.xrestack.ServicePaths;

import java.util.Collection;
import java.util.Map;

public interface IFlavorRulesService {
    Collection<IfExpression> getRules(String serviceName);
    Map<String, IfExpression> getRulesInMap(String serviceName);
    SelectServer getAllRules(String serviceName);
    Collection<String> getRuleIdsByServiceName(String serviceName);
    IfExpression getRule(String serviceName, String ruleId);
    Map<String, PendingChange> getRule(PendingChangesStatus pendingChangesStatus);
    void deleteRule(String serviceName, String ruleId);
    OperationResult deleteRule(String serviceName, String ruleId, ApplicationStatusMode mode);
    void saveRule(IfExpression rule, String serviceName);
    OperationResult saveRule(String serviceName, IfExpression rule, ApplicationStatusMode mode);
    EntityType getEntityType();

    OperationResult deleteTemplateRule(String appName, String ruleId);
}
