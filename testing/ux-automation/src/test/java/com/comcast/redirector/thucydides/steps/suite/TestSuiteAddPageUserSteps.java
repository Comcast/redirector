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
 * @author {authorPlaceHolder}
 */

package com.comcast.redirector.thucydides.steps.suite;

import com.comcast.redirector.thucydides.pages.suite.TestSuiteAddPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class TestSuiteAddPageUserSteps extends ScenarioSteps {

    TestSuiteAddPage page;

    @Step
    public TestSuiteAddPageUserSteps openPage() {
        page.open();
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps addParameter(String name, String value) {
        page.setParameterName(name);
        page.setParameterValue(value);
        page.clickParameterValueAddButton();
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setName(String name) {
        page.setName(name);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps clickAddParameterButton() {
        page.addParameterButtonClick();
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setRuleName(String name) {
        page.setRuleName(name);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps saveTestCase() {
        page.saveButtonClick();
        page.waitFor(500).milliseconds();
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setProtocol(String protocol) {
        page.setProtocol(protocol);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setIpVersion(int i) {
        page.setIpVersion(i);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setPort(String port) {
        page.setPort(port);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setUrn(String urn) {
        page.setUrn(urn);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setStack(String stack) {
        page.setStack(stack);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setFlavor(String flavor) {
        page.setFlavor(flavor);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps setResponseType(int i) {
        page.setResponseType(i);
        return this;
    }

    @Step
    public TestSuiteAddPageUserSteps addUrlRuleName(String name) {
        page.setUrlRule(name);
        page.setAddUrlRuleButtonClick();
        return this;
    }
}
