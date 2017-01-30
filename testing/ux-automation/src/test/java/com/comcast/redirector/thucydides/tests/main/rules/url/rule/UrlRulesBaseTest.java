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

package com.comcast.redirector.thucydides.tests.main.rules.url.rule;

import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.changes.ChangesPageSteps;
import com.comcast.redirector.thucydides.steps.rules.url.UrlRulesAddPageUserSteps;
import com.comcast.redirector.thucydides.steps.rules.url.UrlRulesShowPageUserSteps;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

import static com.comcast.redirector.thucydides.tests.UxTestSuite.Constants.URL_RULE_FOR_EDIT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class UrlRulesBaseTest {
    private static final String RULE_NAME = "urlRuleTest";
    private static final String PARAMETER_MODIFIED = "edit_test";

    @Managed(uniqueSession = true)
    private WebDriver webdriver;

    @Steps
    private UrlRulesAddPageUserSteps urlRulesAddPageSteps;

    @Steps
    private UrlRulesShowPageUserSteps urlRulesShowPageSteps;

    @Steps
    private ChangesPageSteps changesPageSteps;

    @Steps
    private GenericPageSteps genericPageSteps;

    @Test
    public void _1_createUrlRule() {
        urlRulesAddPageSteps
                .openPage()
                .setName(RULE_NAME)
                .addCondition("test", "equals", "test")
                .setUrlParams("xre", "5555", "test", "0")
                .saveAsRule();

        urlRulesShowPageSteps.
                waitForPageToLoad()
                .verifyPendingAddedRuleIsShown(RULE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE, RULE_NAME);

        urlRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME);
    }

    @Test
    public void _2_editUrlRule() {
        urlRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(URL_RULE_FOR_EDIT)
                .editRule(URL_RULE_FOR_EDIT);

        String parameterModified = PARAMETER_MODIFIED + System.currentTimeMillis();
        urlRulesAddPageSteps
                .verifyPageOpened()
                .setParameterValue(parameterModified)
                .saveAsRule();

        urlRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(URL_RULE_FOR_EDIT);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE, URL_RULE_FOR_EDIT);

        urlRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(URL_RULE_FOR_EDIT);
    }

    @Test
    public void _3_deleteUrlRule() {
        urlRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .deleteRule(RULE_NAME);

        genericPageSteps
                .clickModalOkButton();

        urlRulesShowPageSteps
                .verifyPendingDeletedRuleIsShown(RULE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE, RULE_NAME);

        urlRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleDoesNotExist(RULE_NAME);
    }
}
