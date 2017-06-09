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
package com.comcast.redirector.thucydides.steps.deciderRules;

import com.comcast.redirector.thucydides.pages.deciderRules.DeciderRulesAddPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

import java.util.concurrent.TimeUnit;

public class DeciderRulesAddSteps extends ScenarioSteps {
    private DeciderRulesAddPage page;

    @Step
    public DeciderRulesAddSteps openPage() {
        page.open();
        return this;
    }

    @Step
    public DeciderRulesAddSteps refreshPage() {
        page.getDriver().navigate().refresh();
        return this;
    }

    @Step
    public DeciderRulesAddSteps addCondition() {
        page.addCondition();
        return this;
    }

    @Step
    public DeciderRulesAddSteps addOrCondition() {
        page.addOrCondition();
        return this;
    }

    @Step
    public DeciderRulesAddSteps addXorCondition() {
        page.addXorCondition();
        return this;
    }

    @Step
    public DeciderRulesAddSteps setName(String value) {
        page.setName(value);
        return this;
    }

    @Step
    public DeciderRulesAddSteps setParameterName(String value) {
        page.setParameterName(value);
        return this;
    }

    public DeciderRulesAddSteps selectCondition(String value) {
        page.selectCondition(value);
        return this;
    }

    public DeciderRulesAddSteps setConditionValue(String value) {
        page.setConditionValue(value);
        return this;
    }

    public DeciderRulesAddSteps setConditionValueType(String valueType) {
        page.selectCondition(valueType);
        return this;
    }

    public DeciderRulesAddSteps selectPartner(String value) {
        page.selectPartner(value);
        return this;
    }

    @Step
    public DeciderRulesAddSteps clickSaveButton() {
        page.clickSaveButton();
        return this;
    }

    @Step
    public DeciderRulesAddSteps verifySuccessToasterIsShown() {
        page.verifySuccessToasterIsShown();
        return this;
    }
}
