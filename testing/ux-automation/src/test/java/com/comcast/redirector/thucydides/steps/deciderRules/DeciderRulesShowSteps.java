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
package com.comcast.redirector.thucydides.steps.deciderRules;

import com.comcast.redirector.thucydides.pages.GenericPageObjects;
import com.comcast.redirector.thucydides.pages.deciderRules.DeciderRulesShowPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;


public class DeciderRulesShowSteps extends ScenarioSteps {
    private DeciderRulesShowPage page;
    private GenericPageObjects genericPage;

    @Step
    public DeciderRulesShowSteps openPage() {
        page.open();
        return this;
    }

    @Step
    public DeciderRulesShowSteps refreshPage() {
        page.getDriver().navigate().refresh();
        return this;
    }

    @Step
    public DeciderRulesShowSteps editDeciderRule(String value) {
        page.clickEditDeciderRulesButton(value);
        return this;
    }

    @Step
    public DeciderRulesShowSteps deleteDeciderRule(String value) {
        page.clickDeleteDeciderRulesButton(value);
        genericPage.clickModalOkButton();
        return this;
    }

    @Step
    public DeciderRulesShowSteps verifySuccessToasterIsShown() {
        page.verifySuccessToasterIsShown();
        return this;
    }

    @Step
    public DeciderRulesShowSteps clickExportDeciderRule(String value) {
        page.clickExportDeciderRule(value);
        return this;
    }

    @Step
    public DeciderRulesShowSteps clickExportAllDeciderRule() {
        page.clickExportAllDeciderRules();
        return this;
    }
}
