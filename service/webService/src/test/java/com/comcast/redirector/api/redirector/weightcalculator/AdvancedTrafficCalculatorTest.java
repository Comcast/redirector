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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.weightcalculator;

import com.comcast.redirector.api.model.weightcalculator.AdjustedThreshold;
import com.comcast.redirector.api.model.weightcalculator.AdjustedWeights;
import com.comcast.redirector.api.redirector.service.weightcalculator.AdvancedTrafficCalculator;
import org.junit.Assert;
import org.junit.Test;

public class AdvancedTrafficCalculatorTest {

    private AdvancedTrafficCalculator calculator = new AdvancedTrafficCalculator();

    @Test
    public void adjustedWeight_10_with_0_TH_test() {
        long AT = 10;   // increase to 10%
        long TC = 100;  // 100 total connections
        long AWH = 3;
        long TH = 0;
        long DW = 10;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(11, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(10, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_10_percent_100_TC_test() {
        long AT = 10;   // increase to 10%
        long TC = 100;  // 100 total connections
        long AWH = 3;
        long TH = 10;
        long DW = 10;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(11, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(10, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_50_percent_100_TC_test() {
        long AT = 50;   // increase to 50%
        long TC = 100;  // 100 total connections
        long AWH = 3;
        long TH = 10;
        long DW = 10;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(15, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(10, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_50_percent_10M_TC_test() {
        long AT = 50;       // increase to 50%
        long TC = 10000000; // 10M of total connections
        long AWH = 200;
        long TH = 1000;
        long DW = 15;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(23, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(15, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_decrease150_percent_10M_TC_test() {
        long AT = -150;       // 150% decrease (must give 0 weight)
        long TC = 10000000; // 10M of total connections
        long AWH = 200;
        long TH = 1000;
        long DW = 15;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(0, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(22, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_50_percent_10M_TC_5000_TH_test() {
        long AT = 50; // increase to 50%
        long TC = 10000000;
        long AWH = 500;
        long TH = 5000;
        long DW = 15;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(23, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(15, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedWeight_decrease50_percent_1M_TC_200_TH_200_AH_test() {
        long AT = -50; // 50% decrease
        long TC = 1000000;
        long AWH = 200;
        long TH = 200;
        long DW = 5;

        AdjustedWeights adjustedWeights = calculator.calculateAdjustedWeights(AWH, AT, TC, TH, DW);
        Assert.assertEquals(3, adjustedWeights.getAdjustedWeight(), 0);
        Assert.assertEquals(5, adjustedWeights.getDefaultWeight(), 0);
    }

    @Test
    public void adjustedThresholdTest_DW15_AW11() {
        long TC = 100;  // 100 total connections
        long AWH = 3;
        long TH = 10;
        long DW = 10;
        long AW = 11;

        AdjustedThreshold adjustedWeights = calculator.calculateAdjustedTraffic(AWH, TC, TH, DW, AW);
        Assert.assertEquals(10, adjustedWeights.getAdjustedThreshold(), 0);
    }

    @Test
    public void adjustedThresholdTest_DW15_AW15() {
        long TC = 100;  // 100 total connections
        long AWH = 3;
        long TH = 10;
        long DW = 10;
        long AW = 15;

        AdjustedThreshold adjustedWeights = calculator.calculateAdjustedTraffic(AWH, TC, TH, DW, AW);
        Assert.assertEquals(50, adjustedWeights.getAdjustedThreshold(), 0);
    }

    @Test
    public void adjustedThresholdTest_DW15_AW23() {
        long TC = 10000000; // 10M of total connections
        long AWH = 200;
        long TH = 1000;
        long DW = 15;
        long AW = 23;

        AdjustedThreshold adjustedWeights = calculator.calculateAdjustedTraffic(AWH, TC, TH, DW, AW);
        Assert.assertEquals(53, adjustedWeights.getAdjustedThreshold(), 0);
    }

    @Test
    public void adjustedThreshold_1OK_TC_5_TH_4_AWH_test() {
        long TC = 10000;
        long AWH = 4;
        long TH = 5;
        long DW = 10;
        long AW = 14;

        AdjustedThreshold adjustedWeights = calculator.calculateAdjustedTraffic(AWH, TC, TH, DW, AW);
        Assert.assertEquals(40, adjustedWeights.getAdjustedThreshold(), 0);
        Assert.assertEquals(8484, adjustedWeights.getAdjustedWeightedHostsTraffic(), 0);
    }
}
