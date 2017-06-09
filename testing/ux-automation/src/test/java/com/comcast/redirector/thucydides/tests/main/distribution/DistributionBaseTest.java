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

package com.comcast.redirector.thucydides.tests.main.distribution;

import com.comcast.redirector.thucydides.steps.changes.ApproveChangesPageSteps;
import com.comcast.redirector.thucydides.steps.distribution.DistributionPageUserSteps;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Pending;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class DistributionBaseTest {
    private static final String DISTRIBUTION_PERCENT = "15";

    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @Steps
    private DistributionPageUserSteps distributionPageSteps;

    @Steps
    private ApproveChangesPageSteps approveChangesPageSteps;

    @Test
    public void _1_addDistribution() throws InterruptedException {
        distributionPageSteps
            .open()
            .launchAddDistributionDialog()
            .setPercent(DISTRIBUTION_PERCENT)
            .selectFlavor(getDistributionFlavor())
            .addDistribution()
            .saveDistribution()
            .verifySaved(getDistributionFlavor());

        approveChangesPageSteps.openPage().approveAll();
    }

    @Test
    public void _2_saveDefaultServer() throws InterruptedException {
        distributionPageSteps
            .open()
            .setDefaultServer(getDefaultFlavor())
            .saveDistribution();

        approveChangesPageSteps.openPage().approveAll();
    }

    @Pending
    public void _3_deleteDistribution() {
    }

    abstract String getDistributionFlavor();

    abstract String getDefaultFlavor();
}
