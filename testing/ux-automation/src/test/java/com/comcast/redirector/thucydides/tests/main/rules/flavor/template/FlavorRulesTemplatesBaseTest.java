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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.rules.flavor.template;

import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.changes.ChangesPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesAddPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesShowPageSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class FlavorRulesTemplatesBaseTest {
    private static final String TEMPLATE_NAME = "authVersionTemplate";
    private static final String TEMPLATE_NAME_ADVANCED = "authVersionTemplateAdvanced";
    private static final String PARAMETER_MODIFIED = "edit_test";
    private static final String SERVER_URL = "http://someHost:9966/somePath/";
    private static final String SERVER_URL_2 = "http://someHost:9977/somePath/";
    private static final String CONDITION_VALUE = "55";
    private static final String CONDITION_NAME = "authVersion";

    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @Steps
    private FlavorRulesAddPageSteps flavorRulesAddPageSteps;

    @Steps
    private FlavorRulesShowPageSteps flavorRulesShowPageSteps;

    @Steps
    private ChangesPageSteps changesPageSteps;

    @Steps
    private GenericPageSteps genericPageSteps;


    @Test
    public void _1_1_createFlavorRuleTemplate() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(TEMPLATE_NAME)
                .addCondition(CONDITION_NAME, "notEqual", CONDITION_VALUE)
                .addSimpleServer(getFlavor())
                .clickAddQueryPair()
                .enterQueryPair("k1", "v1")
                .clickAddQueryPair()
                .enterQueryPair("k2", "v2")
                .saveRuleAsTemplate();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleIsShown(TEMPLATE_NAME, getFlavor(), "k1=v1&k2=v2");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME, getFlavor(), "k1=v1&k2=v2");
    }

    @Test
    public void _2_1_deleteFlavorRuleTemplate() {
        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME)
                .deleteRule(TEMPLATE_NAME);

        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingDeletedRuleIsShown(TEMPLATE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleDoesNotExist(TEMPLATE_NAME);
    }

    @Test
    public void _3_1_createFlavorRuleTemplateServerGroups() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(TEMPLATE_NAME)
                .addCondition(CONDITION_NAME, "notEqual", "55")
                .addSimpleServer(getFlavor())
                .clickAddQueryPair()
                .enterQueryPair("k1", "v1")
                .clickAddQueryPair()
                .enterQueryPair("k2", "v2")
                .addSimpleServer(UxTestSuite.Constants.RULE_FLAVOR_2)
                .clickAddQueryPair()
                .enterQueryPair("k3", "v3")
                .clickAddQueryPair()
                .enterQueryPair("k4", "v4")
                .saveRuleAsTemplate();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleIsShown(TEMPLATE_NAME, getFlavor(), "k1=v1&k2=v2")
                .verifyPendingAddedRuleIsShown(TEMPLATE_NAME, UxTestSuite.Constants.RULE_FLAVOR_2, "k3=v3&k4=v4");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME, getFlavor(), "k1=v1&k2=v2")
                .verifyPermanentRuleExists(TEMPLATE_NAME, UxTestSuite.Constants.RULE_FLAVOR_2, "k3=v3&k4=v4");
    }

    @Test
    public void _4_1_deleteFlavorRuleTemplateServerGroups() {
        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME)
                .deleteRule(TEMPLATE_NAME);

        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps
                .verifyPendingDeletedRuleIsShown(TEMPLATE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleDoesNotExist(TEMPLATE_NAME);
    }

    @Test
    public void _5_1_createFlavorRuleTemplateAdvancedServer() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(TEMPLATE_NAME_ADVANCED)
                .addCondition(CONDITION_NAME, "notEqual", "55")
                .addAdvancedServer(SERVER_URL)
                .clickAddQueryPair()
                .enterQueryPair("k1", "v1")
                .clickAddQueryPair()
                .enterQueryPair("k2", "v2")
                .addAdvancedServer(SERVER_URL_2)
                .clickAddQueryPair()
                .enterQueryPair("k3", "v3")
                .clickAddQueryPair()
                .enterQueryPair("k4", "v4")
                .saveRuleAsTemplate();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleAdvancedServerIsShown(TEMPLATE_NAME_ADVANCED, SERVER_URL)
                .verifyPendingAddedRuleAdvancedServerIsShown(TEMPLATE_NAME_ADVANCED, SERVER_URL_2);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME_ADVANCED);
    }

    @Test
    public void _6_1_deleteFlavorRuleTemplateAdvancedServer() {
        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleExists(TEMPLATE_NAME_ADVANCED)
                .deleteRule(TEMPLATE_NAME_ADVANCED);
        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps
                .verifyPendingDeletedRuleIsShown(TEMPLATE_NAME_ADVANCED);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE_TEMPLATE, TEMPLATE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openTemplatesPage()
                .verifyPermanentRuleDoesNotExist(TEMPLATE_NAME_ADVANCED);
    }

    abstract String getFlavor();
}
