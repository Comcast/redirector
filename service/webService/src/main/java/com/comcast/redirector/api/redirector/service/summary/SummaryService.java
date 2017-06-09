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

package com.comcast.redirector.api.redirector.service.summary;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.RulesWrapper;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.ServerGroup;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.search.RuleEntity;
import com.comcast.redirector.api.model.summary.RowSummary;
import com.comcast.redirector.api.model.summary.Summary;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.common.util.NamespacedListUtils;
import com.comcast.redirector.dataaccess.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;


@Service
@Scope("prototype")
public class SummaryService implements ISummaryService {

    private static final String DECIMAL_PATTERN = "###.##";
    private static final String DEFAULT_SERVER = "default";
    private static final String CHARACTER_PERCENT = "%";
    private static final float MAX_PERCENT_DISTRIBUTION = 100;

    private String serviceName;

    private Map<String, Integer> countWhitelistedActiveNodes = new HashMap<>();

    @Autowired
    private IServerService serverService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IFlavorRulesService flavorRulesService;

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    private IStacksService stacksService;

    @Override
    public Summary getSummary(String serviceName, List<String> namespacedListNames) {
        this.serviceName = serviceName;
        Summary summary = new Summary();
        List<Rule> rules = getRules();
        setCountWhitelistedActiveNodes(getActiveStacksAndFlavors());
        setDefaultServer(summary, getDefaultServer(), rules);
        setDistributions(summary, rules);
        setSummaryRules(summary, namespacedListNames);
        return summary;
    }

    private void setCountWhitelistedActiveNodes(Set<PathItem> pathItems) {
        for(PathItem pathItem : pathItems) {
            addCountWhitelistedActiveNodes(pathItem);
        }
    }

    private void addCountWhitelistedActiveNodes(PathItem pathItem) {
        countWhitelistedActiveNodes.put(pathItem.getValue(), pathItem.getWhitelistedNodesCount());
    }

    private void setDefaultServer(Summary summary, Server server, List<Rule> rules) {
        RowSummary defaultServer = new RowSummary();
        defaultServer.setDistribution(getPercentDefaultServer(rules));

        if (server != null) {
            defaultServer.setRelease(server.getPath());
            defaultServer.setNode(countWhitelistedActiveNodes.get(server.getPath()));
        }

        summary.setDefaultServer(defaultServer);
    }

    private void setDistributions(Summary summary, List<Rule> rules) {
        List<RowSummary> distributions = new ArrayList<>(rules.size());
        for (Rule rule : rules) {
            RowSummary distribution = new RowSummary();
            distribution.setRelease(rule.getServer().getPath());
            distribution.setDistribution(rule.getPercent() + CHARACTER_PERCENT);
            distribution.setNode(countWhitelistedActiveNodes.get(rule.getServer().getPath()));
            distributions.add(distribution);
        }
        summary.setDistributions(distributions);
    }

    private void setSummaryRules(Summary summary, List<String> namespacedListNames) {
        Map<String, RowSummary> rules = new HashMap<>();
        for (String namespacedListName : namespacedListNames) {
            NamespacedList namespacedList = namespacedListsService.getNamespacedListByName(namespacedListName);
            if (namespacedList != null) {
                Collection<RuleEntity> ruleEntities = NamespacedListUtils.getDependentRules(EntityType.RULE.name(), namespacedList, getAllRules());
                for (RuleEntity ruleEntity : ruleEntities) {
                    for (Expressions expressions : ruleEntity.getExpressions()) {
                        addRuleRows(rules, expressions, namespacedListName);
                    }
                }
            }
        }
        summary.setRules(new HashSet<>(rules.values()));
    }

    private String getPercentDefaultServer(List<Rule> rules) {
        float percentDefaultServer = MAX_PERCENT_DISTRIBUTION;
        for (Rule rule : rules) {
            percentDefaultServer = percentDefaultServer - rule.getPercent();
        }
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_PATTERN);
        return decimalFormat.format(percentDefaultServer) + CHARACTER_PERCENT;
    }

    private Set<PathItem> getActiveStacksAndFlavors() {
        return stacksService.getActiveStacksAndFlavors(serviceName);
    }

    private List<Rule> getRules() {
        return distributionService.getDistribution(serviceName).getRules();
    }

    private Server getDefaultServer() {
        return serverService.getServer(serviceName);
    }

    private Map<String, RulesWrapper> getAllRules() {
        Map<String, RulesWrapper> allRules = new HashMap<>();
        RulesWrapper rulesWrapper = new RulesWrapper();
        rulesWrapper.addRules(EntityType.RULE.name(), flavorRulesService.getRules(serviceName));
        allRules.put(serviceName, rulesWrapper);
        return allRules;
    }

    private void addRuleRows(Map<String, RowSummary> rules, Expressions expressions, String namespacedListName) {
        if (expressions instanceof Server) {
            String path = ((Server) expressions).getPath();
            addRule(rules, path, namespacedListName);
        } else if (expressions instanceof ServerGroup) {
            for (Server server : ((ServerGroup) expressions).getServers()) {
                String path = server.getPath();
                addRule(rules, path, namespacedListName);
            }
        }
    }

    private void addRule(Map<String, RowSummary> rules, String path, String namespacedListName) {
        if (path != null) {
            RowSummary rowSummary = rules.get(path);
            if (rowSummary == null) {
                Set<String> namespacedListNames = new HashSet<>();
                namespacedListNames.add(namespacedListName);
                rules.put(path, createAndGetRowSummary(path, namespacedListNames));
            } else {
                Set<String> namespacedListNames = rowSummary.getNamespacedListNames();
                namespacedListNames.add(namespacedListName);
                rules.put(path, createAndGetRowSummary(path, namespacedListNames));
            }
        }
    }


    private RowSummary createAndGetRowSummary(String path, Set<String> namespacedListNames) {
        RowSummary ruleRow = new RowSummary();
        ruleRow.setRelease(path);
        ruleRow.setNamespacedListNames(namespacedListNames);
        ruleRow.setNode(countWhitelistedActiveNodes.get(path));
        return ruleRow;
    }
}
