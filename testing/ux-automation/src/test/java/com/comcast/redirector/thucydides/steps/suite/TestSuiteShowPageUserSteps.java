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
 */
package com.comcast.redirector.thucydides.steps.suite;

import com.comcast.redirector.thucydides.pages.suite.TestSuiteAddPage;
import com.comcast.redirector.thucydides.pages.suite.TestSuiteShowPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import org.junit.Assert;

public class TestSuiteShowPageUserSteps extends ScenarioSteps {

    TestSuiteShowPage showPage;
    TestSuiteAddPage addPage;

    @Step
    public TestSuiteShowPageUserSteps openPage() {
        showPage.open();
        return this;
    }

    @Step
    public TestSuiteShowPageUserSteps verifySuccessMessageShown(String testCaseName) {
        Assert.assertTrue(showPage.getSuccessMessageText().contains(testCaseName));
        return this;
    }

    @Step
    public TestSuiteShowPageUserSteps editTestSuiteByName(String testCaseName) {
        showPage.editTestCase(testCaseName);

        addPage.setParameterName("param_1_edited");
        addPage.setParameterValue("value_1_edited");
        addPage.clickParameterValueAddButton();
        addPage.addParameterButtonClick();
        addPage.saveButtonClick();

        verifySuccessMessageShown(testCaseName);

        return this;
    }

    @Step
    public TestSuiteShowPageUserSteps editTestSuiteByNameRemovingTwoUrlParams(String testCaseName) {
        showPage.editTestCase(testCaseName);

        addPage.removeUrlRuleByNumberInList(0);
        addPage.removeUrlRuleByNumberInList(0);
        addPage.saveButtonClick();

        verifySuccessMessageShown(testCaseName);

        return this;
    }

    @Step
    public TestSuiteShowPageUserSteps deleteTestSuiteByName(String testCaseName) {
        showPage.clickActionButton(testCaseName);
        showPage.clickDeleteButton(testCaseName);
        showPage.confirmDeletion();

        return this;
    }
}
