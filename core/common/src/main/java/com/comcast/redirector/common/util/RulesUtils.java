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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.common.util;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class RulesUtils {

    public static SelectServer buildSelectServer(Collection<IfExpression> rules, Distribution distribution) {
        SelectServer selectServer = new SelectServer();

        if (CollectionUtils.isNotEmpty(rules)) {
            selectServer.setItems(rules);
        }

        if (distribution != null) {
            selectServer.setDistribution(distribution);
        }

        return selectServer;
    }

    public static URLRules buildURLRules(Collection<IfExpression> rules, Default defaultUrlRule) {
        URLRules urlRules = new URLRules();

        if (CollectionUtils.isNotEmpty(rules)) {
            urlRules.setItems(rules);
        }

        if (defaultUrlRule != null) {
            urlRules.setDefaultStatement(defaultUrlRule);
        }

        return urlRules;
    }

    public static Map<String, IfExpression> mapFromCollection(Collection<IfExpression> expressions) {
        Map<String, IfExpression> expressionsMap = new LinkedHashMap<>();
        if (expressions != null) {
            for (IfExpression expression : expressions) {
                expressionsMap.put(expression.getId(), expression);
            }
        }

        return expressionsMap;
    }

    public static List<Expressions> getReturn(IfExpression ifExpression) {
        IfExpression current = ifExpression;
        while (current.getReturn() == null) {
            int size = current.getItems().size();
            if (size > 0 && (current.getItems().get(size - 1) instanceof IfExpression)) {
                current = (IfExpression)current.getItems().get(size - 1);
            } else {
                break;
            }
        }

        return current.getReturn();
    }

}
