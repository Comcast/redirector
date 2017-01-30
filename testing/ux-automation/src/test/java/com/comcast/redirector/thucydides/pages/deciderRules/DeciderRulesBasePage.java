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
package com.comcast.redirector.thucydides.pages.deciderRules;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;

public class DeciderRulesBasePage extends PageObject {

    @FindBy(id = "toast-container")
    private WebElementFacade toaster;

    @FindBy(id = "rulePartner")
    private WebElementFacade rulePartner;

    @FindBy(id = "ruleName")
    private WebElementFacade ruleName;

    @FindBy(id = "addCondition")
    private WebElementFacade addCondition;

    @FindBy(id = "addOrCondition")
    private WebElementFacade addOrCondition;

    @FindBy(id = "addXorCondition")
    private WebElementFacade addXorCondition;

    @FindBy(id = "saveEntity")
    private WebElementFacade saveEntity;

    @FindBy(jquery = ".expression-name-1:last")
    private WebElementFacade parameterName;

    @FindBy(jquery = ".expression-condition:last")
    private WebElementFacade condition;

    @FindBy(jquery = ".expression-value:last")
    private WebElementFacade conditionValue;

    @FindBy(jquery = ".expression-value-type:last")
    private WebElementFacade conditionValueType;

    @FindBy(jquery = ".expression-value-2:last")
    private WebElementFacade selectNamespacedList;

    public void setName(String value) {
        ruleName.type(value);
    }

    public void addCondition() {
        addCondition.click();
    }

    public void addOrCondition() {
        addOrCondition.click();
    }

    public void addXorCondition() {
        addXorCondition.click();
    }

    public void clickSaveButton() {
        saveEntity.click();
    }

    public void selectPartner(String value) {
        rulePartner.selectByValue(value);
    }

    public void setParameterName(String value) {
        parameterName.type(value);
    }

    public void selectCondition(String value) {
        condition.selectByValue(value);
    }

    public void setConditionValue(String value) {
        conditionValue.type(value);
    }

    public void selectConditionValueType(String value) {
        conditionValueType.selectByValue(value);
    }

    public void selectNamespacedList (String value) {
        selectNamespacedList.selectByValue(value);
    }

    public void verifySuccessToasterIsShown() {
        toaster.waitUntilVisible();
        toaster.shouldContainText("Success");
    }
}
