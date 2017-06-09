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

package com.comcast.redirector.api.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RulesWrapper {

    // RuleType -> SelectServer
    Map<String, SelectServer> rules = new HashMap<>();

    public RulesWrapper() {
    }

    public RulesWrapper(Map<String, SelectServer> rules) {
        this.rules = rules;
    }

    public RulesWrapper(String rulesType, SelectServer rules) {
        this.rules.put(rulesType, rules);
    }

    public RulesWrapper(String rulesType, Collection<IfExpression> rules) {
        if (rules != null) {
            SelectServer selectServer = new SelectServer();
            selectServer.setItems(rules);
            this.rules.put(rulesType, selectServer);
        }
    }

    public Map<String, SelectServer> getRules() {
        return rules;
    }

    public void setRules(Map<String, SelectServer> rules) {
        this.rules = rules;
    }

    public void addRules(String rulesType, Collection<IfExpression> newRules) {
        if (newRules != null) {
            if (rules.containsKey(rulesType)) {
                SelectServer selectServer = rules.get(rulesType);
                selectServer.getItems().addAll(newRules);
            } else {
                SelectServer selectServer = new SelectServer();
                selectServer.setItems(newRules);
                rules.put(rulesType, selectServer);
            }
        }
    }

    public SelectServer getRulesByType(String rulesType) {
        return rulesType == null ? null : rules.get(rulesType);
    }

}
