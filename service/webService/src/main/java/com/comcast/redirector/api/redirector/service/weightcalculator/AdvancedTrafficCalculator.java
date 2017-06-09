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

package com.comcast.redirector.api.redirector.service.weightcalculator;

import com.comcast.redirector.api.model.weightcalculator.AdjustedThreshold;
import com.comcast.redirector.api.model.weightcalculator.AdjustedWeights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdvancedTrafficCalculator extends BaseTrafficCalculator {

    private static final Logger log = LoggerFactory.getLogger(AdvancedTrafficCalculator.class);

    public AdjustedWeights calculateAdjustedWeights(
            double weightedHosts,
            double adjustedThreshold,
            double totalConnections,
            double totalHosts,
            double defaultWeight) {

        if (weightedHosts > totalHosts) {
            weightedHosts = totalHosts;
        }

        AdjustedWeights adjustedWeights = new WeightCalculator(weightedHosts, totalHosts, totalConnections, defaultWeight, adjustedThreshold).calculate();
        logAdjustedWeightsCalculations(adjustedWeights, weightedHosts, adjustedThreshold, totalConnections, totalHosts, defaultWeight);
        return adjustedWeights;
    }

    public AdjustedThreshold calculateAdjustedTraffic(
            double weightedHosts,
            double totalConnections,
            double totalHosts,
            double defaultWeight,
            double adjustedWeight) {

        AdjustedThreshold adjustedThreshold =
            new ThresholdCalculator(weightedHosts, totalHosts, totalConnections, defaultWeight, adjustedWeight)
                .calculate();
        logAdjustedTrafficCalculations(adjustedThreshold, weightedHosts, totalConnections, totalHosts, defaultWeight, adjustedWeight);
        return adjustedThreshold;
    }

    private static class WeightCalculator {
        private double totalConnections;
        private double adjustedThreshold;
        private double defaultWeight;
        private double weightedHosts, totalHosts;

        private double adjustedWeight;

        public WeightCalculator(double weightedHosts, double totalHosts, double totalConnections, double defaultWeight, double adjustedThreshold) {
            if (weightedHosts > totalHosts) {
                weightedHosts = totalHosts;
            }
            this.totalConnections = totalConnections;
            this.adjustedThreshold = adjustedThreshold;
            this.defaultWeight = defaultWeight;
            this.weightedHosts = weightedHosts;
            this.totalHosts = totalHosts;
        }

        public AdjustedWeights calculate() {
            calculateAdjustedWeight(adjustedThreshold);

            AdjustedThreshold adjustedThreshold =
                new ThresholdCalculator(weightedHosts, totalHosts, totalConnections, defaultWeight, adjustedWeight).calculate();

            return new AdjustedWeights(adjustedWeight, defaultWeight,
                adjustedThreshold.getAdjustedWeightedHostsTraffic(), adjustedThreshold.getAdjustedWeightedHostPossibleTraffic(),
                adjustedThreshold.getDefaultWeightedHostsTraffic(), adjustedThreshold.getDefaultWeightedHostPossibleTraffic());
        }

        private void calculateAdjustedWeight(double adjustedThreshold) {
            adjustedWeight = Math.round(defaultWeight * (adjustedThreshold / 100 + 1));
            if (adjustedWeight < 0) {
                defaultWeight -= adjustedWeight;
                adjustedWeight = 0;
            }
        }
    }

    private static class ThresholdCalculator {
        private double totalConnections;
        private double defaultWeight, adjustedWeight;
        private double weightedHosts, nonWeightedHosts;

        private double effectiveWeightedHosts, effectiveTotalHosts;

        private double weightedConnectionsBucket, nonWeightedConnectionsBucket;
        private double weightedConnectionsPerHost, nonWeightedConnectionsPerHost;

        public ThresholdCalculator(double weightedHosts, double totalHosts, double totalConnections, double defaultWeight, double adjustedWeight) {
            if (weightedHosts > totalHosts) {
                weightedHosts = totalHosts;
            }
            this.weightedHosts = weightedHosts;
            this.totalConnections = totalConnections;
            this.defaultWeight = defaultWeight;
            this.adjustedWeight = adjustedWeight;
            this.nonWeightedHosts = totalHosts - weightedHosts;
        }

        public AdjustedThreshold calculate() {
            findTotalInstancesBasedOnWeight();
            calculateTrafficBuckets();
            calculateTrafficPerHost();
            int adjustedThreshold = calculateTrafficAdjustment();

            return new AdjustedThreshold(
                adjustedThreshold,
                (long)weightedConnectionsBucket,
                (long)weightedConnectionsPerHost,
                (long)nonWeightedConnectionsBucket,
                (long)nonWeightedConnectionsPerHost);
        }

        private void findTotalInstancesBasedOnWeight() {
            effectiveWeightedHosts = weightedHosts * adjustedWeight;
            double effectiveNonWeightedHosts = nonWeightedHosts * defaultWeight;
            effectiveTotalHosts = effectiveWeightedHosts + effectiveNonWeightedHosts;
        }

        private void calculateTrafficBuckets() {
            double weightedProportion = effectiveWeightedHosts / effectiveTotalHosts;

            weightedConnectionsBucket = totalConnections * weightedProportion;
            nonWeightedConnectionsBucket = totalConnections - weightedConnectionsBucket;
        }

        private void calculateTrafficPerHost() {
            weightedConnectionsPerHost = weightedConnectionsBucket / weightedHosts;
            nonWeightedConnectionsPerHost = nonWeightedConnectionsBucket / nonWeightedHosts;
        }

        private int calculateTrafficAdjustment() {
            return (int)Math.round(((weightedConnectionsPerHost / nonWeightedConnectionsPerHost) - 1)*100);
        }
    }

    private void logAdjustedWeightsCalculations(AdjustedWeights adjustedWeights,
                                                double adjustedWeightHosts,
                                                double adjustedTraffic,
                                                double totalConnections,
                                                double totalHosts,
                                                double defaultWeight) {

        StringBuilder inputs = new StringBuilder();
        inputs.append("\n\t").append("weightedHosts: ").append(adjustedWeightHosts);
        inputs.append("\n\t").append("adjustedTraffic: ").append(adjustedTraffic).append("%");
        inputs.append("\n\t").append("totalConnections: ").append(totalConnections);
        inputs.append("\n\t").append("totalHosts: ").append(totalHosts);
        inputs.append("\n\t").append("defaultWeight: ").append(defaultWeight);

        log.info("Calculating adjusted weights. \nInput params: {}\n Result:\n {}", inputs, adjustedWeights);
    }

    private void logAdjustedTrafficCalculations(AdjustedThreshold adjustedThreshold,
                                                double adjustedWeightHosts,
                                                double totalConnections,
                                                double totalHosts,
                                                double defaultWeight,
                                                double adjustedWeight) {

        StringBuilder inputs = new StringBuilder();
        inputs.append("\n\t").append("weightedHosts: ").append(adjustedWeightHosts);
        inputs.append("\n\t").append("totalConnections: ").append(totalConnections);
        inputs.append("\n\t").append("totalHosts: ").append(totalHosts);
        inputs.append("\n\t").append("defaultWeight: ").append(defaultWeight);
        inputs.append("\n\t").append("adjustedWeight: ").append(adjustedWeight);

        log.info("Calculating adjusted traffic. \nInput params: {}\n Result:\n {}", inputs, adjustedThreshold);
    }
}
