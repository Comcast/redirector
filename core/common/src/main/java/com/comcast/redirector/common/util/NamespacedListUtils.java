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

package com.comcast.redirector.common.util;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.search.RuleEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NamespacedListUtils {
    private static final Logger log = LoggerFactory.getLogger(NamespacedListUtils.class);

    /**
     * Search for namespaced lists by namespaced list item.
     * @param searchValue value of namespaced list item
     * @param allNamespacedLists all namespaced lists available in the system
     * @param allRules rules in which search by certain namespaced list will be performed. A map serviceName => SelectServer
     * @return {@link NamespacedListSearchResult} instance representing namespaced lists including given item and rules depending on each found namespaced list
     */
    public static NamespacedListSearchResult searchNamespacedLists(NamespacedListValueForWS searchValue, Namespaces allNamespacedLists,
                                                                   Map<String, RulesWrapper> allRules) {
        NamespacedListSearchResult result = new NamespacedListSearchResult();
        List<NamespacedListEntity> foundNamespacedLists = new ArrayList<>();

        for (NamespacedList namespacedList : allNamespacedLists.getNamespaces()) {
            if (doesNamespacedListContainSearchValue(namespacedList, searchValue)||
                    StringUtils.containsIgnoreCase(namespacedList.getName(), searchValue.getValue()) ||
                    StringUtils.containsIgnoreCase(namespacedList.getDescription(), searchValue.getValue())) {
                NamespacedListEntity resultEntity = new NamespacedListEntity();

                resultEntity.setName(namespacedList.getName());
                resultEntity.setDescription(namespacedList.getDescription());
                resultEntity.setDependingFlavorRules(getDependentRules(EntityType.RULE.name(), namespacedList, allRules));
                resultEntity.setDependingTemplateFlavorRules(getDependentRules(EntityType.TEMPLATE_RULE.name(), namespacedList, allRules));
                resultEntity.setDependingUrlRules(getDependentRules(EntityType.URL_RULE.name(), namespacedList, allRules));
                resultEntity.setDependingTemplateUrlRules(getDependentRules(EntityType.TEMPLATE_URL_RULE.name(), namespacedList, allRules));
                resultEntity.setDependingDeciderRules(getDependentRules(EntityType.DECIDER_RULE.name(), namespacedList, allRules));

                foundNamespacedLists.add(resultEntity);
            }
        }

        result.setSearchItem(searchValue.getValue());
        result.setNamespacedLists(foundNamespacedLists);
        return result;
    }

    public static Collection<RuleEntity> getDependentRules(String ruleType, NamespacedList namespacedList, Map<String, RulesWrapper> allRules) {
        Collection<RuleEntity> rules = new LinkedHashSet<>();
        if (allRules == null) {
            return rules;
        }
        for (Map.Entry<String, RulesWrapper> rulesWrapperSet : allRules.entrySet()) {
            SelectServer selectServer = rulesWrapperSet.getValue().getRulesByType(ruleType);
            if (selectServer == null || selectServer.getItems() == null) {
                continue;
            }
            for (Expressions expression : selectServer.getItems()) {
                if (expression instanceof IfExpression) {
                    IfExpression ifExpression = (IfExpression) expression;
                    if (isExpressionDependsOnNamespacedList(namespacedList, ifExpression)) {
                        RuleEntity ruleEntity = new RuleEntity();
                        ruleEntity.setName(ifExpression.getId());
                        ruleEntity.setServiceName(rulesWrapperSet.getKey());
                        ruleEntity.setExpressions(RulesUtils.getReturn(ifExpression));
                        rules.add(ruleEntity);
                    }
                }
            }
        }
        return rules;
    }

    private static boolean isExpressionDependsOnNamespacedList(NamespacedList namespacedList, HasChildren expression) {
        Value searchItem = new Value(namespacedList.getName());
        List<Expressions> childExpressions = expression.getItems();

        if (childExpressions == null) {
            log.info("Incorrect expression {}", ((IfExpression)expression).getId());
            return false;
        }

        while (!childExpressions.isEmpty()) {
            List<Expressions> nextExpressions = new ArrayList<>();
            for (Expressions childExpression : childExpressions) {
                if (childExpression instanceof ContainsBase) {
                    ContainsBase containsExpression = (ContainsBase) childExpression;
                    if (containsExpression.getNamespacedLists() != null &&
                            containsExpression.getNamespacedLists().contains(searchItem)) {
                        return true;
                    }
                }
                if (childExpression instanceof HasChildren) {
                    nextExpressions.addAll(((HasChildren)childExpression).getItems());
                }
            }

            childExpressions = nextExpressions;
        }
        return false;
    }

    // collects all namespaces names of given rules
    public static Set<String> getAllNamespaceLists(Collection<HasChildren> rules) {
        Set<String> namespaceList = new HashSet<>();


        for (HasChildren rule : rules) {
            Collection<Expressions> rulesExpressions = rule.getItems();
            if (CollectionUtils.isNotEmpty(rulesExpressions)) {
                for (Expressions subRule : rulesExpressions) {
                    if (subRule instanceof HasChildren) {
                        namespaceList.addAll(getAllNamespaceLists(Arrays.asList((HasChildren)subRule)));
                    }
                    else if (subRule instanceof ContainsBase && ((ContainsBase)subRule).getType().equals("namespacedList")) {
                        for (Value namespace : ((ContainsBase)subRule).getNamespacedLists()) {
                            namespaceList.add(namespace.getValue());
                        }
                    }
                }
            }
            else if(rule instanceof IfExpression) {
                log.warn("Rule {} has empty expressions", ((IfExpression)rule).getId());
            }
        }
        return namespaceList;
    }

    private static boolean doesNamespacedListContainSearchValue(NamespacedList list, NamespacedListValueForWS namespacedListValueForWS) {
        if (NamespacedListType.ENCODED.equals(list.getType())) {
            for (NamespacedListValueForWS listValue : list.getValueSet()) {
                if (namespacedListValueForWS.getValue().equals(listValue.getValue()) ||
                        namespacedListValueForWS.getValue().equals(listValue.getEncodedValue())) {
                    return true;
                }
            }
        } else return list.getValueSet().contains(namespacedListValueForWS);

        return false;
    }
}
