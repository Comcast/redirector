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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.changes.base;

import com.comcast.redirector.thucydides.steps.rules.url.UrlRulesAddPageUserSteps;
import com.comcast.redirector.thucydides.steps.rules.url.UrlRulesShowPageUserSteps;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class UrlRuleAndTemplatePendingChangesBaseTest extends ChangesBase {

    @Steps
    private UrlRulesAddPageUserSteps urlRulesAddPageUserSteps;

    @Steps
    private UrlRulesShowPageUserSteps urlRulesShowPageUserSteps;

    public static final String URL_RULE_NAME = "testRuleName";
    public static final String URL_RULE_TEMPLATE_NAME = "testRuleTemplateName";

    @Test
    public void approveAllChanges() {
        createAndSaveUrlRule(URL_RULE_NAME);
        createAndSaveUrlRule(URL_RULE_NAME + 2);

        changesPageSteps
                .openPage()
                .clickApproveAllChangesButton();
        genericPageSteps
                .clickModalOkButton();

        changesPageSteps
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME + 2);

        deleteUrlRule(URL_RULE_NAME);
        approveUrlRule(URL_RULE_NAME);
        deleteUrlRule(URL_RULE_NAME + 2);
        approveUrlRule(URL_RULE_NAME + 2);
    }

    @Test
    public void cancelAllChanges() {
        createAndSaveUrlRule(URL_RULE_NAME);
        createAndSaveUrlRule(URL_RULE_NAME + 2);

        changesPageSteps
                .openPage()
                .clickCancelAllChangesButton();

        genericPageSteps
                .clickModalOkButton();

        changesPageSteps
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME + 2);
    }

    @Test
    public void approveUrlRuleChange() {
        createAndSaveUrlRule(URL_RULE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE, URL_RULE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME);

        deleteUrlRule(URL_RULE_NAME);
        approveUrlRule(URL_RULE_NAME);
    }

    @Test
    public void approveUrlRuleTemplateChange() {
        createAndSaveUrlRuleTemplate(URL_RULE_TEMPLATE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE_TEMPLATE, URL_RULE_TEMPLATE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE_TEMPLATE, URL_RULE_TEMPLATE_NAME);

        deleteUrlRuleTemplate(URL_RULE_TEMPLATE_NAME);
        approveUrlRuleTemplate(URL_RULE_TEMPLATE_NAME);
    }

    @Test
    public void cancelUrlRuleChange() {
        createAndSaveUrlRule(URL_RULE_NAME);

        changesPageSteps
                .openPage()
                .cancelChange(PendingChangeType.URL_RULE, URL_RULE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE, URL_RULE_NAME);
    }

    @Test
    public void cancelUrlRuleTemplateChange() {
        createAndSaveUrlRuleTemplate(URL_RULE_TEMPLATE_NAME);

        changesPageSteps
                .openPage()
                .cancelChange(PendingChangeType.URL_RULE_TEMPLATE, URL_RULE_TEMPLATE_NAME)
                .isChangeNotPresent(PendingChangeType.URL_RULE_TEMPLATE, URL_RULE_TEMPLATE_NAME);
    }


    public void createAndSaveUrlRule(String ruleName) {
        urlRulesAddPageUserSteps
                .openPage()
                .setName(ruleName)
                .addCondition("1", "equals", "1")
                .setUrlParams("xre", "5555", "test", "0")
                .saveAsRule();
        urlRulesShowPageUserSteps
                .verifyPendingAddedRuleIsShown(ruleName);
    }

    public void createAndSaveUrlRuleTemplate(String ruleName) {
        urlRulesAddPageUserSteps
                .openPage()
                .setName(ruleName)
                .addCondition("1", "equals", "1")
                .setUrlParams("xre", "5555", "test", "0")
                .saveRuleAsTemplate();
    }

    public void deleteUrlRule(String ruleName) {
        urlRulesShowPageUserSteps
                .openRulesPage()
                .deleteRule(ruleName);
        genericPageSteps.clickModalOkButton();
    }


    public void deleteUrlRuleTemplate(String ruleName) {
        urlRulesShowPageUserSteps
                .openTemplatesPage()
                .deleteRule(ruleName);
        genericPageSteps.clickModalOkButton();
    }


    public void approveUrlRule(final String ruleName) {
        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE, ruleName);
    }

    public void approveUrlRuleTemplate(final String ruleName) {
        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.URL_RULE_TEMPLATE, ruleName);
    }

}
