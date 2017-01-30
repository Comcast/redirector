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

package com.comcast.redirector.api.redirector.service.pending.helper;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.google.common.collect.Sets;

import java.util.*;

public class PendingDistributionDiffHelper {
    public static Map<String, PendingChange> getDistributionsDiff(Distribution pending, Distribution current) {
        Map<String, PendingChange> diff = new TreeMap<>();
        Set<Rule> allPendingRules = pending.getRules() != null ? Sets.newLinkedHashSet(pending.getRules()) : new LinkedHashSet<Rule>();
        Set<Rule> allCurrentRules = current.getRules() != null ? Sets.newLinkedHashSet(current.getRules()) : new LinkedHashSet<Rule>();

        Set<Rule> added = getAddedRules(allPendingRules, allCurrentRules);
        Set<Rule> deleted = getDeletedRules(allPendingRules, allCurrentRules);
        Set<Rule> updated = getUpdatedRules(allPendingRules, allCurrentRules);

        putAddedRulesToDiff(diff, added);
        putDeletedRulesToDiff(diff, deleted);
        putUpdatedRulesToDiff(diff, updated, allCurrentRules);

        return diff;
    }

    private static Set<Rule> getAddedRules(Set<Rule> pending, Set<Rule> current) {
        Set<Rule> added = new LinkedHashSet<>();
        Sets.difference(pending, current).copyInto(added);
        return added;
    }

    private static Set<Rule> getDeletedRules(Set<Rule> pending, Set<Rule> current) {
        Set<Rule> deleted = new LinkedHashSet<>();
        Sets.difference(current, pending).copyInto(deleted);
        return deleted;
    }

    private static Set<Rule> getUpdatedRules(Set<Rule> pending, Set<Rule> current) {
        Set<Rule> updated = new LinkedHashSet<>();
        Sets.intersection(pending, current).copyInto(updated);

        Set<Rule> normalizedPending = getNormalizedPendingRules(pending);
        Iterator<Rule> updatedIterator = updated.iterator();
        while (updatedIterator.hasNext()) {
            Rule rule = updatedIterator.next();
            Rule currentRule = getRuleFromSet(current, rule);
            Rule pendingRule = getRuleFromSet(normalizedPending, rule);
            if (pendingRule == null ||
                    (currentRule.getServer().equals(pendingRule.getServer()) && currentRule.getPercent() == pendingRule.getPercent())) {
                updatedIterator.remove();
            }
        }
        return updated;
    }

    private static void putAddedRulesToDiff(Map<String, PendingChange> diff, Set<Rule> rules) {
        for (Rule rule : rules) {
            putRuleToDiff(diff, Integer.toString(rule.getId()), rule, null, ActionType.ADD);
        }
    }

    private static void putDeletedRulesToDiff(Map<String, PendingChange> diff, Set<Rule> rules) {
        for (Rule rule : rules) {
            putRuleToDiff(diff, Integer.toString(rule.getId()), null, rule, ActionType.DELETE);
        }
    }

    private static void putUpdatedRulesToDiff(Map<String, PendingChange> diff, Set<Rule> updated, Set<Rule> allCurrentRules) {
        for (Rule rule : updated) {
            putRuleToDiff(diff, Integer.toString(rule.getId()), rule, getRuleFromSet(allCurrentRules, rule), ActionType.UPDATE);
        }
    }

    private static Rule getRuleFromSet(Set<Rule> rules, Rule rule) {
        for (Rule ruleInSet: rules) {
            if (rule.equals(ruleInSet)) {
                return ruleInSet;
            }
        }
        return null;
    }

    private static void putRuleToDiff(Map<String, PendingChange> diff, String id, Rule changed, Rule current, ActionType action) {
        diff.put(id, new PendingChange(id, action, changed, current));
    }

    private static Set<Rule> getNormalizedPendingRules(Set<Rule> pendingSet) {
        Rule[] pendingRules = pendingSet.toArray(new Rule[pendingSet.size()]);
        Set<Rule> result = new LinkedHashSet<>();
        // let's normalize array - mark deleted rules as null (pendingRules is ordered already by id)
        int index = 0;
        for (Rule rule : pendingRules) {
            int id = rule.getId();
            for (int y = index; y < id; y++) {
                result.add(null);
            }
            result.add(rule);
            index = id + 1;
        }
        return result;
    }
}
