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

import com.comcast.redirector.thucydides.util.tables.Table;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@DefaultUrl("/testsuite/show")
public class TestSuiteShowPage extends PageObject {

    public TestSuiteShowPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath="/html/body/div/div[2]/div/div/div/div/div/table/tbody")
    private WebElement table;

    @FindBy(xpath="/html/body/div[1]/div/div/div[2]")
    private WebElement successMessageText;

    @FindBy(xpath="html/body/div[3]/div/div/div[3]/button[1]")
    private WebElementFacade confirmDeletionButton;

    @FindBy(xpath="html/body/div[3]/div/div/div[3]/button[2]")
    private WebElementFacade declineDeletionButton;

    public Table getTestSuiteTable() {
        return new Table(table);
    }

    public String getSuccessMessageText() {
        return successMessageText.getText();
    }

    public void confirmDeletion() {
        confirmDeletionButton.click();
    }

    public void editTestCase(String testCaseName) {
        getTestSuiteTable().getEditButton(testCaseName).click();
    }

    public void clickActionButton(String testCaseName) {
        getTestSuiteTable().getActionsButton(testCaseName).click();
    }

    public void clickDeleteButton(String testCaseName) {
        getTestSuiteTable().getDeleteButton(testCaseName).click();
    }
}
