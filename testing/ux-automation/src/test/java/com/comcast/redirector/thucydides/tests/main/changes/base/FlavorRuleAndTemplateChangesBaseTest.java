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

import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesAddPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesShowPageSteps;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Steps;
import org.junit.Test;

public class FlavorRuleAndTemplateChangesBaseTest extends ChangesBase {
    private static final String RULE_NAME = "authVersionRule";
    private static final String RULE_TEMPLATE_NAME = "ruleTemplateName";
    private static final String CONDITION_VALUE = "55";
    private static final String CONDITION_NAME = "authVersion";
    private static final String ADVANCED_SERVICE = "http://www";

    @Steps
    private FlavorRulesAddPageSteps flavorRulesAddPageSteps;

    @Steps
    private FlavorRulesShowPageSteps flavorRulesShowPageSteps;

    @Test
    public void approveFlavorRule() throws InterruptedException {
        createFlavorRule();

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        deleteFlavorRule(RULE_NAME);
        approveFlavorRule(RULE_NAME);
    }

    @Test
    public void approveFlavorRuleTemplate() throws InterruptedException {
        createFlavorRuleTemplate();

        changesPageSteps
                .openPage()
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, RULE_TEMPLATE_NAME);

        deleteFlavorRuleTemplate(RULE_TEMPLATE_NAME);
        approveFlavorRuleTemplate(RULE_TEMPLATE_NAME);
    }

    @Test
    public void cancelFlavorRuleTemplate() throws InterruptedException {
        createFlavorRuleTemplate();
        changesPageSteps
                .openPage()
                .cancelChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, RULE_TEMPLATE_NAME);
    }

    @Test
    public void cancelFlavorRule() throws InterruptedException {
        createFlavorRule();

        changesPageSteps
                .openPage()
                .cancelChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);
    }

    public void createFlavorRule() throws InterruptedException {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(RULE_NAME)
                .addCondition(CONDITION_NAME, "notEqual", CONDITION_VALUE)
                .addAdvancedServer(ADVANCED_SERVICE)
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleAdvancedServerIsShown(RULE_NAME, ADVANCED_SERVICE);
    }

    public void createFlavorRuleTemplate() throws InterruptedException {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(RULE_TEMPLATE_NAME)
                .addCondition(CONDITION_NAME, "notEqual", CONDITION_VALUE)
                .addAdvancedServer(ADVANCED_SERVICE)
                .saveRuleAsTemplate();
    }

    public void deleteFlavorRule(String ruleName) {
        flavorRulesShowPageSteps
                .openRulesPage()
                .deleteRule(ruleName);
        genericPageSteps.clickModalOkButton();
    }

    public void deleteFlavorRuleTemplate(String ruleName) throws InterruptedException {
        flavorRulesShowPageSteps
                .openTemplatesPage()
                .deleteRule(ruleName);
        genericPageSteps.clickModalOkButton();
    }

    public void approveFlavorRule(final String ruleName) {
        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, ruleName);
    }

    public void approveFlavorRuleTemplate(final String ruleName) {
        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, ruleName);
    }
}
