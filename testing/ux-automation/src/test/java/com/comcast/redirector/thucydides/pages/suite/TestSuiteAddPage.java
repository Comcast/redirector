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
package com.comcast.redirector.thucydides.pages.suite;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

@DefaultUrl("/testsuite/edit")
public class TestSuiteAddPage extends PageObject {

    public TestSuiteAddPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "input#testCaseName")
    private WebElementFacade testCaseNameField;

    @FindBy(css = "input#parameterName")
    private WebElementFacade parametersNameFiled;

    @FindBy(css = "input#parameterValue")
    private WebElementFacade parametersValueField;

    @FindBy(css = "button#addParameterValue")
    private WebElementFacade parametersAddButton;

    @FindBy(css = "div#appParameter")
    private WebElementFacade addParameterButton;

    @FindBy(css = "input#rule")
    private WebElementFacade ruleNameField;

    @FindBy(css = "input#protocol")
    private WebElementFacade protocolEditField;

    @FindBy(css = "select#ipVersion")
    private WebElementFacade ipVersionDropDown;

    @FindBy(css = "input#port")
    private WebElementFacade portEditField;

    @FindBy(css = "input#urn")
    private WebElementFacade urnEditField;

    @FindBy(css = "input#stack")
    private WebElementFacade stackEditField;

    @FindBy(css = "input#flavor")
    private WebElementFacade flavorEditField;

    @FindBy(css = "select#responceType")
    private WebElementFacade responseTypeDropDown;

    @FindBy(css = "button#saveEntity")
    private WebElementFacade testCaseSaveButton;

    @FindBy(css = "input#urlrule")
    private WebElementFacade addUrlRuleInput;

    @FindBy(css = "button#addUrlRule")
    private WebElementFacade addUrlRuleButton;

    public void setName(String testCaseName) {
        testCaseNameField.type(testCaseName);
    }

    public void setParameterName(String testParameter) {
        parametersNameFiled.type(testParameter);
    }

    public void setParameterValue(String testValue) {
        parametersValueField.type(testValue);
    }

    public void clickParameterValueAddButton() {
        parametersAddButton.click();
    }

    public void addParameterButtonClick() {
        addParameterButton.click();
    }

    public void setRuleName(String ruleName) {
        ruleNameField.type(ruleName);
    }

    public void saveButtonClick() {
        testCaseSaveButton.click();
    }

    public void setProtocol(String protocol) {
        protocolEditField.type(protocol);
    }

    public void setIpVersion(int i) {
        Select select = new Select(ipVersionDropDown);
        select.selectByIndex(i);
    }

    public void setPort(String port) {
        portEditField.type(port);
    }

    public void setUrn(String urn) {
        urnEditField.type(urn);
    }

    public void setStack(String stack) {
        stackEditField.type(stack);
    }

    public void setFlavor(String flavor) {
        flavorEditField.type(flavor);
    }

    public void setResponseType(int i) {
        Select select = new Select(responseTypeDropDown);
        select.selectByIndex(i);
    }

    public void removeUrlRuleByNumberInList (int number) {
        find(By.id("removeUrlRule_" + number)).click();
    }

    public void setUrlRule (String rule) {
        addUrlRuleInput.type(rule);
    }

    public void setAddUrlRuleButtonClick () {
        addUrlRuleButton.click();
    }
}
