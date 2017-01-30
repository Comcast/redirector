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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.thucydides.pages.stacksmanagement;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/stacksManagement")
public class StacksManagementPage extends PageObject {

    public StacksManagementPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(id = "saveEntity")
    private WebElementFacade saveButton;

    @FindBy(id = "removeAllInactiveStacks")
    private WebElementFacade removeAllInactiveStacksButton;

    @FindBy(id = "inactiveStackCount")
    private WebElementFacade inactiveStackCount;

    @FindBy(id = "activeStackCount")
    private WebElementFacade activeStackCount;

    @FindBy(id = "exportAll")
    private WebElementFacade exportButton;

    @FindBy(css = "a.export-all-stacks")
    private WebElementFacade exportAllLink;

    public String getStackSwitchText(String stackName) {
        return find(By.id(stackName)).getText();
    }

    public void whitelistStack(String stackName) {
        waitForRenderedElementsToBePresent(By.id(stackName));
        find(By.id(stackName)).click();
    }

    public void saveStacks() {
        saveButton.click();
    }

    public void clickRemoveAllInactiveStacksButton() {
        removeAllInactiveStacksButton.click();
    }

    public String getInactiveStackCount() {
        return inactiveStackCount.getText();
    }

    public String getActiveStackCount() {
        return activeStackCount.getText();
    }

    public void clickExportButton() {
        exportButton.click();
    }

    public void clickExportAllLink() {
        exportAllLink.click();
    }
}
