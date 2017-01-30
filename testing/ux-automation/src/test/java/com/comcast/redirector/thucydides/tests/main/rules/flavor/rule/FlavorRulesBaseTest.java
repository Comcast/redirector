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

package com.comcast.redirector.thucydides.tests.main.rules.flavor.rule;

import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.changes.ChangesPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesAddPageSteps;
import com.comcast.redirector.thucydides.steps.rules.flavor.FlavorRulesShowPageSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Pending;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

import static com.comcast.redirector.thucydides.util.GenericTestUtils.waitForStacksToBePresentOnServer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class FlavorRulesBaseTest {
    private static final String RULE_NAME = "authVersionRule";
    private static final String RULE_NAME_ADVANCED = "authVersionRuleAdvanced";
    private static final String PARAMETER_MODIFIED = "edit_test";
    private static final String SERVER_URL = "http://someHost:9966/somePath/";
    private static final String SERVER_URL_2 = "http://someHost:9977/somePath/";
    private static final String CONDITION_VALUE = "55";
    private static final String CONDITION_NAME = "authVersion";

    @Managed(uniqueSession = true)
    private WebDriver webdriver;

    @Steps
    private FlavorRulesAddPageSteps flavorRulesAddPageSteps;

    @Steps
    private FlavorRulesShowPageSteps flavorRulesShowPageSteps;

    @Steps
    private ChangesPageSteps changesPageSteps;

    @Steps
    private GenericPageSteps genericPageSteps;


    @Test
    public void _1_1_createFlavorRule() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(RULE_NAME)
                .addCondition(CONDITION_NAME, "notEqual", CONDITION_VALUE)
                .addSimpleServer(getFlavor())
                .clickAddQueryPair()
                .enterQueryPair("k1", "v1")
                .clickAddQueryPair()
                .enterQueryPair("k2", "v2")
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleIsShown(RULE_NAME, getFlavor(), "k1=v1&k2=v2");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME, getFlavor(), "k1=v1&k2=v2");
    }

    @Test
    public void _2_1_editFlavorRuleAndAddQueryPair() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .editRule(RULE_NAME);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .setParameterValue(PARAMETER_MODIFIED)
                .clickAddQueryPair()
                .enterQueryPair("k3", "v3")
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME)
                .openDiffView(RULE_NAME)
                .verifyParameterModifiedInPending(RULE_NAME, PARAMETER_MODIFIED)
                .verifyQueryPairAdded(RULE_NAME, "k3=v3");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME);
    }

    @Test
    public void _2_2_editFlavorRuleAndChangeCondition() {
        String pendingValue = "66";
        String pendingName = "newKey";

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .editRule(RULE_NAME);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .changeCondition(pendingName, "notEqual", pendingValue)
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME)
                .openDiffView(RULE_NAME)
                .verifyObjectToBeChangedIsPresentInCurrent(RULE_NAME, PARAMETER_MODIFIED)
                .verifyParameterModifiedInPending(RULE_NAME, pendingValue)
                .verifyParameterModifiedInPending(RULE_NAME, pendingName);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME);
    }

    @Test
    public void _3_1_deleteFlavorRule() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .deleteRule(RULE_NAME);

        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps
                .verifyPendingDeletedRuleIsShown(RULE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleDoesNotExist(RULE_NAME);
    }

    @Test
    public void _4_1_createFlavorRuleServerGroups() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(RULE_NAME)
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
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleIsShown(RULE_NAME, getFlavor(), "k1=v1&k2=v2")
                .verifyPendingAddedRuleIsShown(RULE_NAME, UxTestSuite.Constants.RULE_FLAVOR_2, "k3=v3&k4=v4");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME, getFlavor(), "k1=v1&k2=v2")
                .verifyPermanentRuleExists(RULE_NAME, UxTestSuite.Constants.RULE_FLAVOR_2, "k3=v3&k4=v4");
    }

    @Test
    public void _5_1_editFlavorRuleServerGroups() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .editRule(RULE_NAME);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .setParameterValue(PARAMETER_MODIFIED)
                .clickAddQueryPair()
                .enterQueryPair("k5", "v5")
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME)
                .openDiffView(RULE_NAME)
                .verifyParameterModifiedInPending(RULE_NAME, PARAMETER_MODIFIED)
                .verifyQueryPairAdded(RULE_NAME, "k5=v5");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME);
    }


    @Test
    public void _6_1_deleteOneServerFromTwoFlavorRuleServerGroups() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .editRule(RULE_NAME);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .clickServerRemove()
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME)
                .openDiffView(RULE_NAME)
                .verifyObjectToBeChangedIsPresentInCurrent(RULE_NAME, "SERVER_GROUP")
                .verifyValueOfHighlightedLineInPending(RULE_NAME, "RETURN");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME);
    }


    @Test
    public void _7_1_deleteFlavorRuleServerGroups() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME)
                .deleteRule(RULE_NAME);

        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps
                .verifyPendingDeletedRuleIsShown(RULE_NAME);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleDoesNotExist(RULE_NAME);
    }

    @Test
    public void _8_1_createFlavorRuleAdvancedServer() throws Exception {
        flavorRulesAddPageSteps
                .openPage()
                .verifyPageOpened()
                .setName(RULE_NAME_ADVANCED)
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
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingAddedRuleAdvancedServerIsShown(RULE_NAME_ADVANCED, SERVER_URL)
                .verifyPendingAddedRuleAdvancedServerIsShown(RULE_NAME_ADVANCED, SERVER_URL_2)
                .verifyPendingAddedRuleAdvancedServerContainsQueryPairsIsShown(RULE_NAME_ADVANCED, "k1=v1&k2=v2")
                .verifyPendingAddedRuleAdvancedServerContainsQueryPairsIsShown(RULE_NAME_ADVANCED, "k3=v3&k4=v4");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED);
    }

    @Pending
    //TODO: No Diff button is shown when queryPair added for Advanced server
    public void _8_2_editFlavorRuleAdvancedServerServerGroups() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED)
                .editRule(RULE_NAME_ADVANCED);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .setParameterValue(PARAMETER_MODIFIED)
                .clickAddQueryPair()
                .enterQueryPair("k5", "v5")
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME_ADVANCED)
                .openDiffView(RULE_NAME_ADVANCED)
                .verifyParameterModifiedInPending(RULE_NAME_ADVANCED, PARAMETER_MODIFIED)
                .verifyQueryPairAdded(RULE_NAME_ADVANCED, "k5=v5");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED);
    }

    @Test
    public void _9_1_deleteOneServerFromServerGroup_WhenItHasAdvancedServer() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED)
                .editRule(RULE_NAME_ADVANCED);

        flavorRulesAddPageSteps
                .verifyPageOpened()
                .clickServerRemove()
                .saveAsRule();

        flavorRulesShowPageSteps
                .waitForPageToLoad()
                .verifyPendingModifiedRuleIsShown(RULE_NAME_ADVANCED)
                .openDiffView(RULE_NAME_ADVANCED)
                .verifyObjectToBeChangedIsPresentInCurrent(RULE_NAME_ADVANCED, "SERVER_GROUP")
                .verifyValueOfHighlightedLineInPending(RULE_NAME_ADVANCED, "RETURN");

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED);
    }

    @Test
    public void _9_2_deleteFlavorRuleAdvancedServer() {
        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleExists(RULE_NAME_ADVANCED)
                .deleteRule(RULE_NAME_ADVANCED);

        genericPageSteps
                .clickModalOkButton();

        flavorRulesShowPageSteps.verifyPendingDeletedRuleIsShown(RULE_NAME_ADVANCED);

        changesPageSteps
                .openPage()
                .approveChange(PendingChangeType.FLAVOR_RULE, RULE_NAME_ADVANCED);

        flavorRulesShowPageSteps
                .openRulesPage()
                .verifyPermanentRuleDoesNotExist(RULE_NAME_ADVANCED);
    }

    abstract String getFlavor();
}
