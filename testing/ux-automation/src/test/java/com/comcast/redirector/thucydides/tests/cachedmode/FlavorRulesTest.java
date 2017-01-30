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

package com.comcast.redirector.thucydides.tests.cachedmode;

import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesAddPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesShowPageSteps;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static com.comcast.redirector.thucydides.tests.CachedModeUxTestSuite.Constants.FLAVOR_RULE_ID;

@RunWith(ThucydidesRunner.class)
public class FlavorRulesTest {
    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "http://localhost:10540/redirectorWebService/admin/#")
    public Pages pages;

    @Steps
    private FlavorRulesShowPageSteps flavorRulesShowPageSteps;

    @Steps
    private FlavorRulesAddPageSteps flavorRulesAddPageSteps;

    @Test
    public void openFlavorRuleForEdit() throws Exception {
        openEditRulePageOrFail(FLAVOR_RULE_ID);
    }

    @Test
    public void cancelFlavorRule() throws Exception {
        openEditRulePageOrFail(FLAVOR_RULE_ID);
        flavorRulesAddPageSteps.cancelSaveRule();
        flavorRulesShowPageSteps.verifyPermanentRuleExists(FLAVOR_RULE_ID);
    }

    @Test
    public void saveFlavorRule_ShowsServiceUnavailableAlert() throws Exception {
        openEditRulePageOrFail(FLAVOR_RULE_ID);
        flavorRulesAddPageSteps
                .setParameterValue("anyValue")
                .saveAsRule()
                .verifyOfflineModeAlertShown();
    }

    private void openEditRulePageOrFail(String ruleId) {
        flavorRulesShowPageSteps
            .openRulesPage()
            .editRule(ruleId);

        flavorRulesAddPageSteps.verifyPageOpened();
    }
}
