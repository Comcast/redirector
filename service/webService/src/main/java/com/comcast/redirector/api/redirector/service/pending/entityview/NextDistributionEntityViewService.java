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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NextDistributionEntityViewService implements IEntityViewService<Distribution> {
    private ApprovedDistributionHelper helper = new ApprovedDistributionHelper();

    @Autowired
    private IDistributionService distributionService;

    private IChangesStatusService changesStatusService;


    @Override
    public Distribution getEntity(String serviceName) {
        PendingChangesStatus pendingChangesStatus = changesStatusService.getPendingChangesStatus(serviceName);
        return getDistributionPendingPreview(serviceName, pendingChangesStatus);
    }

    @Override
    public Distribution getEntity(PendingChangesStatus pendingChangesStatus, Distribution currentDistribution) {
        return helper.getDistribution(currentDistribution, pendingChangesStatus);
    }

    private Distribution getDistributionPendingPreview(String serviceName,
                                                       PendingChangesStatus pendingChangesStatus) {
        return helper.getDistribution(
                distributionService.getDistribution(serviceName), pendingChangesStatus);
    }

    private static class ApprovedDistributionHelper {
        Distribution getDistribution(Distribution distribution, PendingChangesStatus pendingChangesStatus) {
            Map<Integer, Rule> ruleMap = getIdToRuleMap(distribution.getRules());
            mergePendingChangesIntoRuleMap(pendingChangesStatus.getDistributions(), ruleMap);
            distribution.setRules(getOrderedRules(ruleMap));
            return distribution;
        }

        private void mergePendingChangesIntoRuleMap(Map<String, PendingChange> pendingChanges, Map<Integer, Rule> toMap) {
            for (Map.Entry<String, PendingChange> pendingChange : pendingChanges.entrySet()) {
                Rule changedRule = (Rule) pendingChange.getValue().getChangedExpression();
                switch (pendingChange.getValue().getChangeType()) {
                    case UPDATE:
                    case ADD:
                        toMap.put(changedRule.getId(), changedRule);
                        break;
                    case DELETE:
                        changedRule = (Rule) pendingChange.getValue().getCurrentExpression();
                        toMap.remove(changedRule.getId());
                        break;
                }
            }
        }

        private Map<Integer, Rule> getIdToRuleMap(List<Rule> rules) {
            Map<Integer, Rule> rulesMap = new LinkedHashMap<>();
            for (Rule rule : rules) {
                rulesMap.put(rule.getId(), rule);
            }

            return rulesMap;
        }

        private List<Rule> getOrderedRules(Map<Integer, Rule> ruleMap) {
            List<Rule> rules = new ArrayList<>(ruleMap.values());
            for (int i = 0; i < rules.size(); i++) {
                rules.get(i).setId(i);
            }

            return rules;
        }
    }

    void setDistributionService(IDistributionService distributionService) {
        this.distributionService = distributionService;
    }

    public void setChangesStatusService(IChangesStatusService changesStatusService) {
        this.changesStatusService = changesStatusService;
    }
}
