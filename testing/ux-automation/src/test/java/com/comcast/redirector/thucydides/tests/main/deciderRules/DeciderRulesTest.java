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
package com.comcast.redirector.thucydides.tests.main.deciderRules;

import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.common.DeciderConstants;
import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.deciderRules.DeciderRulesAddSteps;
import com.comcast.redirector.thucydides.steps.deciderRules.DeciderRulesEditSteps;
import com.comcast.redirector.thucydides.steps.deciderRules.DeciderRulesShowSteps;
import com.comcast.redirector.thucydides.util.GenericTestUtils;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;

import static com.comcast.redirector.thucydides.tests.UxTestSuite.Constants.NAMESPACED_LIST;
import static com.comcast.redirector.thucydides.util.TestConstants.DATA_SERVICE_BASE_URL;

@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeciderRulesTest {

    private static final String PARTNER_ID = "0";
    private static final String DECIDER_RULE_NAME_1 = "DeciderRule_01";
    private static final String DECIDER_RULE_NAME_2 = "DeciderRule_02";
    private static final String JSON_Partner = "{\"partner\":[{\"id\":\"test\", \"properties\":{\"property\":[{\"name\":\"prop1\",\"value\":\"val1\"}]}}]}";

    private static final File dirForDownload = GenericTestUtils.createTempDir();

    @ManagedPages(defaultUrl = TestConstants.DECIDER_MAIN_UX_URL)
    private Pages pages;

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @Steps
    private DeciderRulesAddSteps deciderRulesAddSteps;

    @Steps
    private DeciderRulesShowSteps deciderRulesShowSteps;

    @Steps
    private DeciderRulesEditSteps deciderRulesEditSteps;

    @Steps
    private GenericPageSteps genericPageSteps;

    @BeforeClass
    public static void setup() {
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        WebTarget target = getWebTarget(DeciderConstants.DECIDER_PARTNERS_PATH);
        ServiceHelper.post(target, JSON_Partner, MediaType.APPLICATION_JSON);
    }

    @AfterClass
    public static void destroy() {
        WebTarget target = getWebTarget(DeciderConstants.DECIDER_PARTNERS_PATH).path("test");
        ServiceHelper.delete(target);
    }

    @Test
    public void _1_1_addDeciderRuleFirstRule() throws InterruptedException {
        deciderRulesAddSteps
                .openPage()
                .setName(DECIDER_RULE_NAME_1)
                .addCondition()
                .setParameterName("parameter")
                .selectCondition("notEqual")
                .setConditionValue("10")
                .selectPartner(PARTNER_ID)
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _1_2_addDeciderRuleSecondRule() throws InterruptedException {
        deciderRulesAddSteps
                .openPage()
                .setName(DECIDER_RULE_NAME_2)
                .addCondition()
                .setParameterName("parameter")
                .selectCondition("notEqual")
                .setConditionValue("10")
                .selectPartner(PARTNER_ID)
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _2_1_editDeciderRule() throws InterruptedException {
        deciderRulesShowSteps
                .openPage()
                .editDeciderRule(DECIDER_RULE_NAME_1);

        deciderRulesEditSteps
                .addCondition()
                .setParameterName("numericParameter")
                .selectCondition("greaterThan")
                .setConditionValue("11")
                .setConditionValueType("numeric")
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _2_2_editDeciderRuleChangedConditionType() throws InterruptedException {
        deciderRulesShowSteps
                .openPage()
                .editDeciderRule(DECIDER_RULE_NAME_2);

        deciderRulesEditSteps
                .setParameterName("numericParameter2")
                .selectCondition("lessOrEqual")
                .setConditionValue("One")
                .setConditionValueType("string")
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _2_3_editDeciderRuleAddNamespacedList() throws InterruptedException {
        deciderRulesShowSteps
                .openPage()
                .editDeciderRule(DECIDER_RULE_NAME_2);

        deciderRulesEditSteps
                .addCondition()
                .setParameterName("namespacedParameter")
                .selectCondition("containsnmsp")
                .selectNamespacedList(NAMESPACED_LIST)
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _2_4_editDeciderRuleRemoveExpression() throws InterruptedException {
        deciderRulesShowSteps
                .openPage()
                .editDeciderRule(DECIDER_RULE_NAME_2);

        deciderRulesEditSteps
                .removeExpression()
                .clickSaveButton()
                .verifySuccessToasterIsShown();
    }

    @Test
    public void _3_1_exportDeciderRule() throws Exception {
        deciderRulesShowSteps
                .openPage()
                .clickExportDeciderRule(DECIDER_RULE_NAME_1);

        genericPageSteps.checkSavedFile(dirForDownload, "exportedrules-byId-" + DECIDER_RULE_NAME_1 + ".json");
    }

    @Test
    public void _3_2_exportAllDeciderRules() throws Exception {
        deciderRulesShowSteps
                .openPage()
                .clickExportAllDeciderRule();

        genericPageSteps.checkSavedFile(dirForDownload, "exportedrules.json");
    }

    @Test
    public void _4_1_deleteDeciderRule() throws InterruptedException {
        deciderRulesShowSteps
                .openPage()
                .deleteDeciderRule(DECIDER_RULE_NAME_2)
                .verifySuccessToasterIsShown();

        deciderRulesShowSteps
                .openPage()
                .deleteDeciderRule(DECIDER_RULE_NAME_1)
                .verifySuccessToasterIsShown();
    }

    private static WebTarget getWebTarget(String path) {
        String pathsForApp = DATA_SERVICE_BASE_URL + path;
        HttpTestServerHelper.initWebTarget(pathsForApp);

        return HttpTestServerHelper.target();
    }
}
