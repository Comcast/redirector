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
package com.comcast.redirector.thucydides.pages.changes;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;


@DefaultUrl("/changes")
public class ApproveChangesPage extends PageObject {

    public ApproveChangesPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[2]/button[1]")
    private WebElementFacade approveAllButton;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[2]/button[2]")
    private WebElementFacade cancelAllButton;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[2]/button[3]")
    private WebElementFacade triggerModel;

    @FindBy(xpath="html/body/div[3]/div/div/div[3]/button[1]")
    private WebElementFacade confirmApproveButton;

    @FindBy(xpath="html/body/div[3]/div/div/div[3]/button[2]")
    private WebElementFacade declineApproveButton;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[4]/div[1]/button[1]")
    private WebElementFacade approveWhitelistedButton;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[5]/div[2]/div/div[*]/div[1]")
    private List<WebElement> changesRules;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div[7]/div[2]/div/div[*]/div[1]")
    private List<WebElement> changesUrlRules;

    public void approveAll() {approveAllButton.click(); }

    public void cancelAll() {
        cancelAllButton.click();
    }

    public void triggerModel() {
        triggerModel.click();
    }

    public void confirmApprove(){
        confirmApproveButton.click();
    }

    public int getChangeIndexByName(List<WebElement> changes, String name) {
        int index = 0;
        for (WebElement change : changes) {
            if (change.getText().contains(name)) {
                break;
            }
            index++;
            continue;
        }
        return index;
    }

    public void approveWhitelistedChange() {
        approveWhitelistedButton.click();
    }

    public void approveRuleChange(String name) {
        find(By.id("approveRule_" + name)).click();
    }

    public void approveUrlRuleChange(String name) {
        find(By.id("approveUrlRule_" + name)).click();
    }

}
