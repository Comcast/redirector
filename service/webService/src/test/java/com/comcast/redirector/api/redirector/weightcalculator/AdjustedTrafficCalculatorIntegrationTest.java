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

package com.comcast.redirector.api.redirector.weightcalculator;

import com.comcast.redirector.api.model.traffic.AdjustedTrafficInputParam;
import com.comcast.redirector.api.model.traffic.AdjustedTrafficInputParams;
import com.comcast.redirector.api.model.traffic.Traffic;
import com.comcast.redirector.api.model.traffic.TrafficInputParams;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import it.context.ContextBuilder;
import it.context.HostsBuilder;
import it.context.TestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.fail;

public class AdjustedTrafficCalculatorIntegrationTest {

    private static final String DEFAULT_FLAVOR = "zone2_traffic";
    private static final String FLAVOR_1 = "zone1_traffic";
    private static final String PATH_POC1 = "/PO/POC1";

    private static final int TOTAL_HOSTS_IN_DISTRIBUTION_FLAVOR_20 = 20;
    private static final int TOTAL_HOSTS_IN_DEFAULT_FLAVOR_40 = 40;

    private static final int TICK_TIME = 100;
    private static final int MAX_WAIT_TIME = 2000;

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void calculateAdjustedTraffic1() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new AdjustedTrafficCalculatorSteps(serviceName)
                .createDefaultServerInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(DEFAULT_FLAVOR)
                                .withNumberOfHostsToAdjust(20)
                                .withAdjustedWeight(15)
                                .withDefaultWeight(11)
                                .build())
                .createDistributionRulesInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(FLAVOR_1)
                                .withNumberOfHostsToAdjust(10)
                                .withAdjustedWeight(14)
                                .withDefaultWeight(11)
                                .build())
                .createTrafficInputParams()
                    .withTotalNumberConnections(4000)
                    .withConnectionThreshold(200)
                    .withAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode.ADJUSTED_TRAFFIC)
                    .buildTrafficInputParams()
                .calculationApplied()
                .verifyTrafficCalulationForDefaultServerIsNotEmpty()
                .verifyDefaultServerTrafficNumberConnections(3952)
                .verifyDefaultServerTrafficTotalConnectionLimit(8000)
                .verifyDefaultServerTrafficConnectionsHostRatio(98)
                .verifyDefaultServerTrafficIsAdjustedThresholdNotNull()
                .verifyDefaultServerTrafficAdjustedWeightedHostPossibleTraffic(114)
                .verifyDefaultServerTrafficDefaultWeightedHostPossibleTraffic(83)
                .verifyDefaultServerTrafficAdjustedThreshold(36)
                .verifyDefaultServerTrafficAdjustedWeightedHostsTraffic(2280)
                .verifyDefaultServerTrafficDefaultWeightedHostsTraffic(1672)
                .verifyTrafficCalulationForDistributionRulesIsNotEmpty()
                .verifyDistributionRulesTrafficNumberConnections(48)
                .verifyDistributionRulesTrafficTotalConnectionLimit(4000)
                .verifyDistributionRulesTrafficConnectionsHostRatio(2)
                .verifyDistributionRulesTrafficIsAdjustedThresholdNotNull()
                .verifyDistributionRulesTrafficAdjustedWeightedHostPossibleTraffic(2)
                .verifyDistributionRulesTrafficDefaultWeightedHostPossibleTraffic(2)
                .verifyDistributionRulesTrafficAdjustedThreshold(27)
                .verifyDistributionRulesTrafficAdjustedWeightedHostsTraffic(26)
                .verifyDistributionRulesTrafficDefaultWeightedHostsTraffic(21);
    }

    @Test
    public void calculateAdjustedTraffic2() throws Exception {

        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new AdjustedTrafficCalculatorSteps(serviceName)
                .createDefaultServerInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(DEFAULT_FLAVOR)
                                .withNumberOfHostsToAdjust(20)
                                .withAdjustedWeight(11)
                                .withDefaultWeight(10)
                                .build())
                .createDistributionRulesInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(FLAVOR_1)
                                .withNumberOfHostsToAdjust(10)
                                .withAdjustedWeight(12)
                                .withDefaultWeight(9)
                                .build())
                .createTrafficInputParams()
                    .withTotalNumberConnections(1000000)
                    .withConnectionThreshold(200)
                    .withAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode.ADJUSTED_TRAFFIC)
                    .buildTrafficInputParams()
                .calculationApplied()
                .verifyTrafficCalulationForDefaultServerIsNotEmpty()
                .verifyDefaultServerTrafficNumberConnections(988000)
                .verifyDefaultServerTrafficTotalConnectionLimit(8000)
                .verifyDefaultServerTrafficConnectionsHostRatio(24700)
                .verifyDefaultServerTrafficIsAdjustedThresholdNotNull()
                .verifyDefaultServerTrafficAdjustedWeightedHostPossibleTraffic(25876)
                .verifyDefaultServerTrafficDefaultWeightedHostPossibleTraffic(23523)
                .verifyDefaultServerTrafficAdjustedThreshold(10)
                .verifyDefaultServerTrafficAdjustedWeightedHostsTraffic(517523)
                .verifyDefaultServerTrafficDefaultWeightedHostsTraffic(470476)
                .verifyTrafficCalulationForDistributionRulesIsNotEmpty()
                .verifyDistributionRulesTrafficNumberConnections(12000)
                .verifyDistributionRulesTrafficTotalConnectionLimit(4000)
                .verifyDistributionRulesTrafficConnectionsHostRatio(600)
                .verifyDistributionRulesTrafficIsAdjustedThresholdNotNull()
                .verifyDistributionRulesTrafficAdjustedWeightedHostPossibleTraffic(685)
                .verifyDistributionRulesTrafficDefaultWeightedHostPossibleTraffic(514)
                .verifyDistributionRulesTrafficAdjustedThreshold(33)
                .verifyDistributionRulesTrafficAdjustedWeightedHostsTraffic(6857)
                .verifyDistributionRulesTrafficDefaultWeightedHostsTraffic(5142);
    }

    @Test
    public void calculateAdjustedWeight1() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new AdjustedTrafficCalculatorSteps(serviceName)
                .createDefaultServerInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(DEFAULT_FLAVOR)
                                .withNumberOfHostsToAdjust(20)
                                .withAdjustedThreshold(33)
                                .withDefaultWeight(10)
                                .build())
                .createDistributionRulesInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(FLAVOR_1)
                                .withNumberOfHostsToAdjust(10)
                                .withAdjustedThreshold(25)
                                .withDefaultWeight(60)
                                .build())
                .createTrafficInputParams()
                    .withTotalNumberConnections(1000000)
                    .withConnectionThreshold(200)
                    .withAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode.ADJUSTED_WEIGHT)
                    .buildTrafficInputParams()
                .calculationApplied()
                .verifyTrafficCalulationForDefaultServerIsNotEmpty()
                .verifyDefaultServerTrafficNumberConnections(988000)
                .verifyDefaultServerTrafficTotalConnectionLimit(8000)
                .verifyDefaultServerTrafficConnectionsHostRatio(24700)
                .verifyDefaultServerTrafficIsAdjustedWeightsNotNull()
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(27921)
                .verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(21478)
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeight(13)
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(558434)
                .verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(429565)
                .verifyTrafficCalulationForDistributionRulesIsNotEmpty()
                .verifyDistributionRulesTrafficNumberConnections(12000)
                .verifyDistributionRulesTrafficTotalConnectionLimit(4000)
                .verifyDistributionRulesTrafficConnectionsHostRatio(600)
                .verifyDistributionRulesTrafficIsAdjustedWeightedNotNull()
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(666)
                .verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(533)
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeight(75)
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(6666)
                .verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(5333);
    }

    @Test
    public void calculateAdjustedWeight2() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new AdjustedTrafficCalculatorSteps(serviceName)
                .createDefaultServerInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(DEFAULT_FLAVOR)
                                .withNumberOfHostsToAdjust(50)
                                .withAdjustedThreshold(60)
                                .withDefaultWeight(5)
                                .build())
                .createDistributionRulesInputParams(
                        new AdjustedTrafficInputParamBuilder().withFlavor(FLAVOR_1)
                                .withNumberOfHostsToAdjust(25)
                                .withAdjustedThreshold(50)
                                .withDefaultWeight(12)
                                .build())
                .createTrafficInputParams()
                    .withTotalNumberConnections(4000)
                    .withConnectionThreshold(200)
                    .withAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode.ADJUSTED_WEIGHT)
                    .buildTrafficInputParams()
                .calculationApplied()
                .verifyTrafficCalulationForDefaultServerIsNotEmpty()
                .verifyDefaultServerTrafficNumberConnections(3952)
                .verifyDefaultServerTrafficTotalConnectionLimit(8000)
                .verifyDefaultServerTrafficConnectionsHostRatio(98)
                .verifyDefaultServerTrafficIsAdjustedWeightsNotNull()
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(98)
                .verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(0)
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeight(8)
                .verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(3952)
                .verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(0)
                .verifyTrafficCalulationForDistributionRulesIsNotEmpty()
                .verifyDistributionRulesTrafficNumberConnections(48)
                .verifyDistributionRulesTrafficTotalConnectionLimit(4000)
                .verifyDistributionRulesTrafficConnectionsHostRatio(2)
                .verifyDistributionRulesTrafficIsAdjustedWeightedNotNull()
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(2)
                .verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(0)
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeight(18)
                .verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(48)
                .verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(0);
    }

    private class AdjustedTrafficInputParamBuilder {

        String flavor = "";
        long numberOfHostsToAdjust;
        long adjustedThreshold;
        long adjustedWeight;
        long defaultWeight;

        AdjustedTrafficInputParamBuilder withFlavor(String flavor) {
            this.flavor = flavor;
            return this;
        }

        AdjustedTrafficInputParamBuilder withNumberOfHostsToAdjust(long numberOfHostsToAdjust) {
            this.numberOfHostsToAdjust = numberOfHostsToAdjust;
            return this;
        }

        AdjustedTrafficInputParamBuilder withAdjustedThreshold(long adjustedThreshold) {
            this.adjustedThreshold = adjustedThreshold;
            return this;
        }

        AdjustedTrafficInputParamBuilder withAdjustedWeight(long adjustedWeight) {
            this.adjustedWeight = adjustedWeight;
            return this;
        }

        AdjustedTrafficInputParamBuilder withDefaultWeight(long defaultWeight) {
            this.defaultWeight = defaultWeight;
            return this;
        }

        AdjustedTrafficInputParam build() {
            AdjustedTrafficInputParam adjustedTrafficInputParam = new AdjustedTrafficInputParam();
            adjustedTrafficInputParam.setFlavor(flavor);
            adjustedTrafficInputParam.setNumberOfHostsToAdjust(numberOfHostsToAdjust);
            adjustedTrafficInputParam.setAdjustedThreshold(adjustedThreshold);
            adjustedTrafficInputParam.setAdjustedWeight(adjustedWeight);
            adjustedTrafficInputParam.setDefaultWeight(defaultWeight);
            return adjustedTrafficInputParam;
        }
    }

    private class AdjustedTrafficCalculatorSteps {
        private String serviceName;
        private AdjustedTrafficInputParam defaultInputParam;
        private AdjustedTrafficInputParam distributionRulesInputParams;
        private TrafficInputParams trafficInputparams;
        private Traffic traffic;

        AdjustedTrafficCalculatorSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        AdjustedTrafficCalculatorSteps createDefaultServerInputParams(AdjustedTrafficInputParam adjustedTrafficInputParam) {
            this.defaultInputParam = adjustedTrafficInputParam;
            return this;
        }

        AdjustedTrafficCalculatorSteps createDistributionRulesInputParams(AdjustedTrafficInputParam adjustedTrafficInputParam) {
            this.distributionRulesInputParams = adjustedTrafficInputParam;
            return this;
        }

        TrafficInputParamsBuilder createTrafficInputParams() {
            return new TrafficInputParamsBuilder();
        }

        AdjustedTrafficCalculatorSteps calculationApplied() {
            traffic = apiFacade.calculatedTraffic(trafficInputparams, serviceName);
            return this;
        }

        // check the results
        AdjustedTrafficCalculatorSteps verifyTrafficCalulationForDefaultServerIsNotEmpty() {
            Assert.assertNotNull(traffic);
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficNumberConnections(long expectedNumberConnections) {
            Assert.assertEquals(expectedNumberConnections, traffic.getDefaultServerTraffic().getNumberConnections());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficTotalConnectionLimit(long expectedTotalConnectionLimit) {
            Assert.assertEquals(expectedTotalConnectionLimit, traffic.getDefaultServerTraffic().getTotalConnectionLimit());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficConnectionsHostRatio(long expectedConnectionsHostRatio) {
            Assert.assertEquals(expectedConnectionsHostRatio, traffic.getDefaultServerTraffic().getConnectionsHostRatio());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficIsAdjustedThresholdNotNull() {
            Assert.assertNotNull(traffic.getDefaultServerTraffic().getAdjustedThreshold());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficAdjustedWeightedHostPossibleTraffic(long expectedAdjustedWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostPossibleTraffic, traffic.getDefaultServerTraffic().getAdjustedThreshold().getAdjustedWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficDefaultWeightedHostPossibleTraffic(long expectedDefaultWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostPossibleTraffic, traffic.getDefaultServerTraffic().getAdjustedThreshold().getDefaultWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficAdjustedThreshold(long expectedAdjustedThreshold) {
            Assert.assertEquals(expectedAdjustedThreshold, traffic.getDefaultServerTraffic().getAdjustedThreshold().getAdjustedThreshold(), 0);
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficAdjustedWeightedHostsTraffic(long expectedAdjustedWeightedHostsTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostsTraffic, traffic.getDefaultServerTraffic().getAdjustedThreshold().getAdjustedWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficDefaultWeightedHostsTraffic(long expectedDefaultWeightedHostsTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostsTraffic, traffic.getDefaultServerTraffic().getAdjustedThreshold().getDefaultWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyTrafficCalulationForDistributionRulesIsNotEmpty() {
            Assert.assertNotNull(traffic.getDistributionRulesTraffic());
            Assert.assertNotNull(traffic.getDistributionRulesTraffic().get(0));
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficNumberConnections(long expectedNumberConnections) {
            Assert.assertEquals(expectedNumberConnections, traffic.getDistributionRulesTraffic().get(0).getNumberConnections());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficTotalConnectionLimit(long expectedTotalConnectionLimit) {
            Assert.assertEquals(expectedTotalConnectionLimit, traffic.getDistributionRulesTraffic().get(0).getTotalConnectionLimit());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficConnectionsHostRatio(long expectedConnectionsHostRatio) {
            Assert.assertEquals(expectedConnectionsHostRatio, traffic.getDistributionRulesTraffic().get(0).getConnectionsHostRatio());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficIsAdjustedThresholdNotNull() {
            Assert.assertNotNull(traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficAdjustedWeightedHostPossibleTraffic(long expectedAdjustedWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostPossibleTraffic, traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold().getAdjustedWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficDefaultWeightedHostPossibleTraffic(long expectedDefaultWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostPossibleTraffic, traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold().getDefaultWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficAdjustedThreshold(long expectedAdjustedThreshold) {
            Assert.assertEquals(expectedAdjustedThreshold, traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold().getAdjustedThreshold(), 0);
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficAdjustedWeightedHostsTraffic(long expectedAdjustedWeightedHostsTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostsTraffic, traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold().getAdjustedWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficDefaultWeightedHostsTraffic(long expectedDefaultWeightedHostsTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostsTraffic, traffic.getDistributionRulesTraffic().get(0).getAdjustedThreshold().getDefaultWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficIsAdjustedWeightsNotNull() {
            Assert.assertNotNull(traffic.getDefaultServerTraffic().getAdjustedWeights());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(long expectedAdjustedWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostPossibleTraffic, traffic.getDefaultServerTraffic().getAdjustedWeights().getAdjustedWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(long expectedDefaultWeightedHostPossibleTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostPossibleTraffic, traffic.getDefaultServerTraffic().getAdjustedWeights().getDefaultWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeight(long expectedAdjustedWeight) {
            Assert.assertEquals(expectedAdjustedWeight, traffic.getDefaultServerTraffic().getAdjustedWeights().getAdjustedWeight(), 0);
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(long expectedAdjustedWeightedHostsTraffic) {
            Assert.assertEquals(expectedAdjustedWeightedHostsTraffic, traffic.getDefaultServerTraffic().getAdjustedWeights().getAdjustedWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDefaultServerTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(long expectedDefaultWeightedHostsTraffic) {
            Assert.assertEquals(expectedDefaultWeightedHostsTraffic, traffic.getDefaultServerTraffic().getAdjustedWeights().getDefaultWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficIsAdjustedWeightedNotNull() {
            Assert.assertNotNull(traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostPossibleTraffic(int i) {
            Assert.assertEquals(i, traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights().getAdjustedWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostPossibleTraffic(int i) {
            Assert.assertEquals(i, traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights().getDefaultWeightedHostPossibleTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeight(int i) {
            Assert.assertEquals(i, traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights().getAdjustedWeight(), 0);
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficForAdjustedWeightsAdjustedWeightedHostsTraffic(int i) {
            Assert.assertEquals(i, traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights().getAdjustedWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps verifyDistributionRulesTrafficForAdjustedWeightsDefaultWeightedHostsTraffic(int i) {
            Assert.assertEquals(i, traffic.getDistributionRulesTraffic().get(0).getAdjustedWeights().getDefaultWeightedHostsTraffic());
            return this;
        }

        AdjustedTrafficCalculatorSteps setTrafficInputparams(TrafficInputParams trafficInputparams) {
            this.trafficInputparams = trafficInputparams;
            return this;
        }

        private class TrafficInputParamsBuilder {

            private TrafficInputParams trafficInputparams = new TrafficInputParams();
            private long totalNumberConnections;
            private long connectionThreshold;
            private AdjustedTrafficInputParams.AdjustedTrafficCalculationMode adjustedTrafficCalculationMode;
            private AdjustedTrafficInputParams adjustedInputParams;

            TrafficInputParamsBuilder withTotalNumberConnections(long totalNumberConnections) {
                this.totalNumberConnections = totalNumberConnections;
                return this;
            }

            TrafficInputParamsBuilder withConnectionThreshold(long connectionThreshold) {
                this.connectionThreshold = connectionThreshold;
                return this;
            }

            TrafficInputParamsBuilder withAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode adjustedTrafficCalculationMode) {
                this.adjustedTrafficCalculationMode = adjustedTrafficCalculationMode;
                return this;
            }

            AdjustedTrafficCalculatorSteps buildTrafficInputParams() {
                adjustedInputParams = new AdjustedTrafficInputParams();
                adjustedInputParams.setAdjustedTrafficCalculationMode(adjustedTrafficCalculationMode);
                adjustedInputParams.setDefaultServer(defaultInputParam);
                adjustedInputParams.setDistribution(Collections.singletonList(distributionRulesInputParams));

                trafficInputparams.setTotalNumberConnections(totalNumberConnections);
                trafficInputparams.setConnectionThreshold(connectionThreshold);
                trafficInputparams.setAdjustedTrafficInputParams(adjustedInputParams);
                trafficInputparams.setAdjustedTrafficCalculationMode(adjustedTrafficCalculationMode);
                return AdjustedTrafficCalculatorSteps.this.setTrafficInputparams(trafficInputparams);
            }
        }
    }

    private void setupEnv(String serviceName) throws Exception {
        List<String> listAddressesForDefaultFlavor = createListOfIPv4(TOTAL_HOSTS_IN_DEFAULT_FLAVOR_40);
        List<String> listAddressesForDistributionFlavor = createListOfIPv4(TOTAL_HOSTS_IN_DISTRIBUTION_FLAVOR_20);

        HostsBuilder contextBuilder = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor(DEFAULT_FLAVOR)
                .withWhitelist(PATH_POC1)
                .withDefaultUrlParams().urn("any").protocol("any").port("0").ipv("4")
                .withDistribution().percent("1.2f").flavor(FLAVOR_1)
                .withHosts();

        listAddressesForDefaultFlavor.forEach(ipv4->
                contextBuilder.stack(PATH_POC1).flavor(DEFAULT_FLAVOR).currentApp().ipv4(ipv4).ipv6("ipv6"));

        listAddressesForDistributionFlavor.forEach(ipv4->
                contextBuilder.stack(PATH_POC1).flavor(FLAVOR_1).currentApp().ipv4(ipv4).ipv6("ipv6"));

        TestContext context = contextBuilder.build();
        testHelperBuilder(context).setupEnv();

        waitDistributionSetUp(MAX_WAIT_TIME, 1, serviceName);
        waitAllStacksSetUp(MAX_WAIT_TIME, 1, serviceName);
    }

    private static List<String> createListOfIPv4(int numberOfHosts) {
        List<String> hosts = new ArrayList<>();
        int octet3 = 0;
        int octet4 = 0;

        String IPv4;
        while (octet3 < 256 && hosts.size() < numberOfHosts){
            IPv4 = "10.10." + octet3 + "." + octet4;
            hosts.add(IPv4);
            while ((++octet4) < 256 && hosts.size() < numberOfHosts){
                IPv4 = "10.10." + octet3 + "." + octet4;
                hosts.add(IPv4);
            }
            octet4 = 0;
            octet3++;
        }
        return hosts;
    }

    private void waitDistributionSetUp (int waitTime, int distributionNumber, String serviceName) throws InterruptedException {
        long lastTestTime = System.currentTimeMillis() + waitTime;
        int count = 0;
        while(count < distributionNumber) {
            if (lastTestTime < System.currentTimeMillis()) {
                fail("Couldn't registry distribution for running test");
            }
            count = apiFacade.getDistributionForService(serviceName).getRules().size();
            Thread.sleep(TICK_TIME);
        }
    }

    private void waitAllStacksSetUp (int waitTime, int hostsNumber, String serviceName) throws InterruptedException {
        long lastTestTime = System.currentTimeMillis() + waitTime;
        int count = 0;
        while(count < hostsNumber) {
            if (lastTestTime < System.currentTimeMillis()) {
                fail("Couldn't registry distribution for running test");
            }
            count = apiFacade.getAddressByStackForService(serviceName, PATH_POC1).getHostIPsList().size();
            Thread.sleep(TICK_TIME);
        }
    }
}
