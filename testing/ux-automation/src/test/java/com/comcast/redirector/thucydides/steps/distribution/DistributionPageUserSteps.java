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
 */
package com.comcast.redirector.thucydides.steps.distribution;

import com.comcast.redirector.thucydides.pages.distribution.DistributionPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import org.junit.Assert;

public class DistributionPageUserSteps extends ScenarioSteps {

    DistributionPage page;

    @Step
    public DistributionPageUserSteps open() throws InterruptedException {
        page.openPage();
        page.waitFor(500);
        getDriver().navigate().refresh();
        page.waitFor(1000);
        return this;
    }

    @Step
    public DistributionPageUserSteps launchAddDistributionDialog() {
        page.waitFor(5).seconds();
        page.clickAddDistributionButton();
        return this;
    }

    @Step
    public DistributionPageUserSteps setPercent(String percent) {
        page.setPercent(percent);
        return this;
    }

    @Step
    public DistributionPageUserSteps selectFlavor(String flavor) {
        page.selectDistribution(flavor);
        return this;
    }

    @Step
    public DistributionPageUserSteps addDistribution() {
        page.clickSaveDistributionButton();
        return this;
    }

    @Step
    public DistributionPageUserSteps saveDistribution() {
        page.saveDistribution();
        return this;
    }

    @Step
    public DistributionPageUserSteps setDefaultServer(String flavor) {
        page.selectDefaultServer(flavor);
        return this;
    }

    @Step
    public DistributionPageUserSteps verifySaved(String flavor) {
        Assert.assertTrue(page.isDistributionPresent(flavor));
        return this;
    }

    @Step
    public DistributionPageUserSteps deleteDistribution(final int idx) {
        page.deleteDistribution(idx);

        return this;
    }

}
