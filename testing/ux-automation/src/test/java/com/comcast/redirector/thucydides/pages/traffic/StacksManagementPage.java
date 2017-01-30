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
package com.comcast.redirector.thucydides.pages.traffic;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;


@DefaultUrl("/stacksManagement")
public class StacksManagementPage extends PageObject {

    public StacksManagementPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/div/form/table/thead/tr[2]/th[3]/div/div/div/input")
    private WebElementFacade stackField;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/div/form/table/tbody")
    private WebElementFacade stacksTable;

    @FindBy(xpath="html/body/div/div[2]/div/div/div/div/form/table/tbody/tr[*]/td[3]")
    private List<WebElement> stacks;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/div/form/table/tbody/tr[*]/td[2]/span")
    private List<WebElement> whitelistedCheckboxes;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/button")
    private WebElementFacade saveButton;


    private int getStackIndexByName(String path) {
        int index = 0;
        for (WebElement stack : stacks) {
            if (!stack.getText().equals(path)) {
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    public void checkWhitelisted(String path) {
        whitelistedCheckboxes.get(getStackIndexByName(path)).click();
        saveButton.click();
    }

    public boolean isWhitelisted(String path) {
        waitForPresenceOf("html/body/div[1]/div[2]/div/div/div/div/form/table/tbody/tr[*]/td[2]/span");
        return whitelistedCheckboxes.get(getStackIndexByName(path)).getText().equals("Yes");
    }
}
