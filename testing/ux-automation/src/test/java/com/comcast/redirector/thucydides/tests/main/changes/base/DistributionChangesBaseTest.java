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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.changes.base;

import com.comcast.redirector.thucydides.steps.distribution.DistributionPageUserSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DistributionChangesBaseTest extends ChangesBase {

    @Steps
    private DistributionPageUserSteps distributionPageSteps;


    @Test
    public void _1_approveDistributionRule() throws InterruptedException {
        createDistribution(UxTestSuite.Constants.DEFAULT_FLAVOR);

        changesPageSteps
                .openPage();

        changesPageSteps
                .clickApproveDistributionChangesButton()
                .isDistributionSectionNotPresent();

        deleteDistribution(1);
        approveDistributionChanges();
    }

    @Test
    public void _2_cancelDistributionRule() throws InterruptedException {
        createDistribution(UxTestSuite.Constants.DEFAULT_FLAVOR);
        changesPageSteps
                .openPage();

        changesPageSteps
                .clickCancelDistributionChangesButton()
                .isDistributionSectionNotPresent();
    }

    public void createDistribution(final String flavor) throws InterruptedException {
        distributionPageSteps
                .open()
                .launchAddDistributionDialog()
                .setPercent("15")
                .selectFlavor(flavor)
                .addDistribution()
                .saveDistribution();
    }

    public void deleteDistribution(final int idx) throws InterruptedException {
        distributionPageSteps
                .open()
                .deleteDistribution(idx);
        genericPageSteps.clickModalOkButton();
        distributionPageSteps.saveDistribution();
    }

    public void approveDistributionChanges() throws InterruptedException {
        changesPageSteps
                .openPage();
        changesPageSteps
                .clickApproveDistributionChangesButton();
    }
}
