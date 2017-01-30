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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.traffic;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.traffic.AdjustedTrafficInputParam;
import com.comcast.redirector.api.model.traffic.Traffic;
import com.comcast.redirector.api.model.traffic.TrafficInputParams;
import com.comcast.redirector.api.model.traffic.TrafficStatsItem;
import com.comcast.redirector.api.model.weightcalculator.AdjustedThreshold;
import com.comcast.redirector.api.model.weightcalculator.AdjustedWeights;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.api.redirector.service.weightcalculator.AdvancedTrafficCalculator;
import com.comcast.redirector.api.redirector.service.weightcalculator.SimpleTrafficCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class TrafficService implements ITrafficService {

    private static final float MAX_PERCENT_DISTRIBUTION = 100;

    private static final String TITLE_DEFAULT_SERVER = "Default server";

    private static final String TITLE_DISTRIBUTION = "Distribution";

    private static final String TITLE_OF = " of ";

    private static final String CHARACTER_PERCENT = "%";

    public static final String CHARACTER_SPACE = " ";

    @Autowired
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Server> currentDefaultServerEntityViewService;

    @Autowired
    private IEntityViewService<Server> nextDefaultServerEntityViewService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private AdvancedTrafficCalculator advancedTrafficCalculator;

    @Autowired
    private SimpleTrafficCalculator simpleTrafficCalculator;

    @Override
    public Traffic getTraffic(String serviceName, TrafficInputParams trafficInputParams) {

        Distribution distribution = getDistribution(serviceName, trafficInputParams.getDistributionMode());
        FlavorsNodesState flavorsNodesState = new FlavorsNodesState(serviceName, trafficInputParams.getHostsMode());

        Traffic traffic = new Traffic();

        traffic.setDefaultServerTraffic(calculateTrafficForDefaultServer(serviceName, distribution, trafficInputParams, flavorsNodesState));
        traffic.setDistributionRulesTraffic(calculateTrafficForDistributionRules(distribution, trafficInputParams, flavorsNodesState));

        return traffic;
    }

    private TrafficStatsItem calculateTrafficForDefaultServer(String serviceName, Distribution distribution,
                                                              TrafficInputParams inputParams,
                                                              FlavorsNodesState flavorsNodesState) {

        Server defaultServer = getDefaultServer(serviceName, inputParams.getDistributionMode());
        String flavor = defaultServer == null ? (distribution.getDefaultServer() == null ? "" : distribution.getDefaultServer().getPath()) : defaultServer.getPath();
        float percent = getPercentOfDefaultServer(distribution.getRules());

        TrafficStatsItem defaultTrafficStatsItem = calculateTrafficForHost(flavorsNodesState.getFlavorHostsCount(flavor),
                percent, inputParams, inputParams.getAdjustedTrafficInputParams(flavor));

        defaultTrafficStatsItem.setTitle(createTitle(TITLE_DEFAULT_SERVER, flavor, percent));
        defaultTrafficStatsItem.setFlavor(flavor);

        return defaultTrafficStatsItem;
    }

    private List<TrafficStatsItem> calculateTrafficForDistributionRules(Distribution distribution,
                                                                        TrafficInputParams trafficInputParams,
                                                                        FlavorsNodesState flavorsNodesState) {

        List<TrafficStatsItem> distributionRulesTraffic = new ArrayList<>(distribution.getRules().size());

        for (Rule rule : distribution.getRules()) {

            String flavor = rule.getServer().getPath();
            float percent = rule.getPercent();

            TrafficStatsItem trafficStatsItem = calculateTrafficForHost(flavorsNodesState.getFlavorHostsCount(flavor),
                    percent, trafficInputParams, trafficInputParams.getAdjustedTrafficInputParams(flavor));

            trafficStatsItem.setTitle(createTitle(TITLE_DISTRIBUTION, flavor, percent));
            trafficStatsItem.setFlavor(flavor);
            distributionRulesTraffic.add(trafficStatsItem);
        }

        return distributionRulesTraffic;
    }

    private TrafficStatsItem calculateTrafficForHost(long numberOfHostsOfFlavor, float percentOfTotalConnections,
                                                     TrafficInputParams inputParams, AdjustedTrafficInputParam adjustedInputParams) {

        TrafficStatsItem trafficStatsItem = new TrafficStatsItem();

        calculateSimpleTrafficForHost(trafficStatsItem, percentOfTotalConnections, inputParams, numberOfHostsOfFlavor);
        calculateAdvancedTrafficForHost(trafficStatsItem, inputParams, percentOfTotalConnections, adjustedInputParams, numberOfHostsOfFlavor);

        return trafficStatsItem;
    }

    private void calculateSimpleTrafficForHost(TrafficStatsItem trafficStatsItem, float percentOfTotalConnections,
                                               TrafficInputParams inputParams, long numberOfHostsOfFlavor) {

        long totalNumberOfConnectionsOfFlavor = simpleTrafficCalculator.calculateNumberOfConnections(inputParams.getTotalNumberConnections(), percentOfTotalConnections);
        trafficStatsItem.setNumberConnections(totalNumberOfConnectionsOfFlavor);

        trafficStatsItem.setTotalConnectionLimit(simpleTrafficCalculator.calculateTotalConnectionLimitOfHosts(inputParams.getConnectionThreshold(), numberOfHostsOfFlavor));
        trafficStatsItem.setConnectionsHostRatio(simpleTrafficCalculator.calculateNumberOfConnectionsPerHost(totalNumberOfConnectionsOfFlavor, numberOfHostsOfFlavor));
    }

    private void calculateAdvancedTrafficForHost(TrafficStatsItem trafficStatsItem, TrafficInputParams inputParams,
                                                 float percentOfTotalConnections, AdjustedTrafficInputParam adjustedInputParams,
                                                 long numberOfHostsOfFlavor) {

        if (adjustedInputParams != null && inputParams.getAdjustedTrafficCalculationMode() != null) {
            switch (inputParams.getAdjustedTrafficCalculationMode()) {
                case ADJUSTED_TRAFFIC:
                    trafficStatsItem.setAdjustedThreshold(calculateAdjustedTraffic(inputParams, adjustedInputParams, numberOfHostsOfFlavor, percentOfTotalConnections));
                    break;
                case ADJUSTED_WEIGHT:
                    trafficStatsItem.setAdjustedWeights(calculateAdjustedWeight(inputParams, adjustedInputParams, numberOfHostsOfFlavor, percentOfTotalConnections));
                    break;
            }
        }
    }

    private AdjustedThreshold calculateAdjustedTraffic(TrafficInputParams trafficInputParams,
                                                       AdjustedTrafficInputParam adjustedTrafficParams,
                                                       long numberOfHostsOfFlavor, float percent) {

        long totalConnectionsOfFlavor = advancedTrafficCalculator.calculateNumberOfConnections(trafficInputParams.getTotalNumberConnections(), percent);
        return advancedTrafficCalculator.calculateAdjustedTraffic(adjustedTrafficParams.getNumberOfHostsToAdjust(),
                totalConnectionsOfFlavor, numberOfHostsOfFlavor, adjustedTrafficParams.getDefaultWeight(),
                adjustedTrafficParams.getAdjustedWeight());
    }

    private AdjustedWeights calculateAdjustedWeight(TrafficInputParams trafficInputParams,
                                                    AdjustedTrafficInputParam adjustedTrafficParams,
                                                    long numberOfHostsOfFlavor, float percent) {

        long totalConnectionsOfFlavor = advancedTrafficCalculator.calculateNumberOfConnections(trafficInputParams.getTotalNumberConnections(), percent);
        return advancedTrafficCalculator.calculateAdjustedWeights(adjustedTrafficParams.getNumberOfHostsToAdjust(),
                adjustedTrafficParams.getAdjustedThreshold(), totalConnectionsOfFlavor,
                numberOfHostsOfFlavor, adjustedTrafficParams.getDefaultWeight());
    }

    private Distribution getDistribution(String serviceName, TrafficInputParams.DistributionMode distributionMode) {
        Distribution distributions = null;
        switch (distributionMode) {
            case CURRENT:
                distributions = currentDistributionEntityViewService.getEntity(serviceName);
                break;
            case NEXT:
                distributions = nextDistributionEntityViewService.getEntity(serviceName);
                if (distributions == null) {
                    distributions = currentDistributionEntityViewService.getEntity(serviceName);
                }
                break;
        }
        return distributions;
    }

    private Server getDefaultServer(String serviceName, TrafficInputParams.DistributionMode distributionMode) {
        Server server = null;
        switch (distributionMode) {
            case CURRENT:
                server = currentDefaultServerEntityViewService.getEntity(serviceName);
                break;
            case NEXT:
                server = nextDefaultServerEntityViewService.getEntity(serviceName);
                break;
        }
        return server;
    }

    private String createTitle(String title, String flavorName, float percent) {
        return title + CHARACTER_SPACE + percent + CHARACTER_PERCENT + TITLE_OF + flavorName;
    }

    private float getPercentOfDefaultServer(List<Rule> rules) {
        float percentDefaultServer = MAX_PERCENT_DISTRIBUTION;
        for (Rule rule : rules) {
            percentDefaultServer = percentDefaultServer - rule.getPercent();
        }
        return percentDefaultServer;
    }

    private class FlavorsNodesState {

        String serviceName;
        TrafficInputParams.HostsMode mode;

        private Map<String, Integer> flavorToNumberOfActiveWhitelistedNodes;
        private Map<String, Integer> flavorToNumberOfAllWhitelistedNodes;

        private FlavorsNodesState(String serviceName, TrafficInputParams.HostsMode mode) {
            this.serviceName = serviceName;
            this.mode = mode;
        }

        private Integer getFlavorHostsCount(String flavor) {
            switch (mode) {
                case ONLY_ACTIVE_WHITELISTED: {
                    if (flavorToNumberOfActiveWhitelistedNodes == null) {
                        flavorToNumberOfActiveWhitelistedNodes = new HashMap<>();
                        Set<PathItem> pathItems = stacksService.getActiveStacksAndFlavors(serviceName);
                        for (PathItem pathItem : pathItems) {
                            flavorToNumberOfActiveWhitelistedNodes.put(pathItem.getValue(), pathItem.getWhitelistedNodesCount());
                        }
                    }
                    return flavorToNumberOfActiveWhitelistedNodes.containsKey(flavor) ? flavorToNumberOfActiveWhitelistedNodes.get(flavor) : 0;
                }
                case ALL_WHITELISTED: {
                    if (flavorToNumberOfAllWhitelistedNodes == null) {
                        flavorToNumberOfAllWhitelistedNodes = new HashMap<>();
                        List<PathItem> pathItems = stacksService.getStacksForService(serviceName).getPaths(serviceName).getFlavors();
                        for (PathItem pathItem : pathItems) {
                            flavorToNumberOfAllWhitelistedNodes.put(pathItem.getValue(), pathItem.getWhitelistedNodesCount());
                        }
                    }
                    return flavorToNumberOfAllWhitelistedNodes.containsKey(flavor) ? flavorToNumberOfAllWhitelistedNodes.get(flavor) : 0;
                }
                default:
                    return 0;
            }
        }

    }
}
