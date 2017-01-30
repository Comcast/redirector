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
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;


@DefaultUrl("/changes")
public class PendingChangesPage extends PageObject {

    public PendingChangesPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(id="approveAllChanges")
    private WebElementFacade approveAllButton;

    @FindBy(id="cancelAllChanges")
    private WebElementFacade cancelAllButton;

    @FindBy(id="triggerModelReload")
    private WebElementFacade triggerModel;

    @FindBy(id="approveWhitelistedStacks")
    private WebElementFacade approveWhitelistedButton;

    @FindBy(css="button.approve-flavor-rule")
    private List<WebElement> approveRuleButtons;

    @FindBy(css="button.approve-url-rule")
    private List<WebElement> approveUrlRuleButtons;

    @FindBy(css="div.flavor-rule-panel")
    private WebElementFacade flavorRulePanel;

    @FindBy(css="div.url-rule-panel")
    private WebElementFacade urlRulePanel;

    public void approveAll() {approveAllButton.click(); }

    public void cancelAll() {
        cancelAllButton.click();
    }

    public void triggerModel() {
        triggerModel.click();
    }

    public int getChangeIndexByName(List<WebElementFacade> changes, String name) {
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
        flavorRulePanel.waitUntilVisible();
        approveRuleButtons.get(getChangeIndexByName(findAll("div.flavor-rule-changes"), name)).click();
    }

    public void approveUrlRuleChange(String name) {
        urlRulePanel.waitUntilVisible();
        approveUrlRuleButtons.get(getChangeIndexByName(findAll("div.url-rule-changes"), name)).click();
    }

}
