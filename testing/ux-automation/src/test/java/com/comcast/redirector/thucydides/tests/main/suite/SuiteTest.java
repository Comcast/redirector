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

package com.comcast.redirector.thucydides.tests.main.suite;

import com.comcast.redirector.thucydides.steps.suite.TestSuiteAddPageUserSteps;
import com.comcast.redirector.thucydides.steps.suite.TestSuiteShowPageUserSteps;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SuiteTest {

    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.MAIN_UX_URL)
    public Pages pages;

    @Steps
    private TestSuiteAddPageUserSteps testsuiteAddSteps;

    @Steps
    private TestSuiteShowPageUserSteps testsuiteShowSteps;

    @Test
    public void _1_createTestCase() {
        testsuiteAddSteps
            .openPage()
            .setName("test_1")
            .addParameter("param_1", "value_1")
            .clickAddParameterButton()
            .setRuleName("rule_1")
            .saveTestCase();

        testsuiteShowSteps.verifySuccessMessageShown("test_1");
    }

    @Test
    public void _2_createTestCaseWithFewValues() {
        testsuiteAddSteps
            .openPage()
            .setName("test_2")
            .addParameter("param_1", "value_1")
            .addParameter("param_2", "value_2")
            .addParameter("param_3", "value_3")
            .clickAddParameterButton()
            .setRuleName("rule_2")
            .saveTestCase();

        testsuiteShowSteps.verifySuccessMessageShown("test_2");
    }

    @Test
    public void _4_createTestCaseWithAllExpectedParameters() {
        testsuiteAddSteps
            .openPage()
            .setName("test_3")
            .addParameter("param_3", "value_3")
            .clickAddParameterButton()
            .setRuleName("rule_3")
            .setProtocol("http")
            .setIpVersion(1)
            .setPort("22")
            .setUrn("shell")
            .setStack("/PO/POC5")
            .setFlavor("1.61")
            .addUrlRuleName("urlRule_1")
            .addUrlRuleName("urlRule_2")
            .addUrlRuleName("urlRule_3")
            .setResponseType(1)
            .saveTestCase();

        testsuiteShowSteps.verifySuccessMessageShown("test_3");
    }

    @Test
    public void _5_editFirstTestCase() {
        testsuiteShowSteps
            .openPage()
            .editTestSuiteByName("test_1");
    }

    @Test
    public void _6_editTestCaseRemovingUrlRules() {
        testsuiteShowSteps
                .openPage()
                .editTestSuiteByNameRemovingTwoUrlParams("test_3");
    }

    @Test
    public void _7_deleteAllTestCases() {
        testsuiteShowSteps
            .openPage()
            .deleteTestSuiteByName("test_1")
            .deleteTestSuiteByName("test_2")
            .deleteTestSuiteByName("test_3");
    }
}
