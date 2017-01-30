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
package com.comcast.redirector.thucydides.pages.distribution;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


@DefaultUrl("/distribution")
public class DistributionPage extends PageObject {

    private static final Logger log = LoggerFactory.getLogger(DistributionPage.class);

    public DistributionPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath="html/body/div/div[2]/div/div/div/form/div/div[2]/default-server/div/div/div/div[3]/div/fieldset/div[2]/div[2]/select")
    private WebElementFacade defaultServerSelect;

    @FindBy(css = "button#saveEntity")
    private WebElementFacade saveButton;

    @FindBy(css = "button#addItem")
    private WebElementFacade addItemButton;

    @FindBy(xpath="html/body/div[3]/div/div/div[2]/form/div/div[1]/span/input")
    private WebElementFacade percentField;

    @FindBy(xpath=".//*[@id='server']/fieldset/div[2]/div[2]/select")
    private WebElementFacade distributionSelect;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div")
    private WebElementFacade distributionChange;

    @FindBy(css = "button#saveDistribution")
    private WebElementFacade saveDistributionButton;

    public void setDefaultServer(String string) {
        Select select = new Select(defaultServerSelect);
        select.selectByValue(string);
    }

    public void saveDistribution() {
        waitFor(5).seconds();
        waitForButton(saveButton).click();
    }

    public void selectDefaultServer(String flavor) {
        waitFor(2).seconds();
        defaultServerSelect.selectByValue(flavor);
    }

    public void selectDistribution(String flavor) {
        distributionSelect.selectByValue(flavor);
    }

    public void setPercent(String percent) {
        percentField.type(percent);
    }

    public void openPage() {
        open();
        waitForTextToDisappear("Loading...");
    }

    public void clickSaveDistributionButton() {
        waitForButton(saveDistributionButton).click();
    }

    public void clickAddDistributionButton() {
        waitForButton(addItemButton).click();
    }

    public boolean isDistributionPresent(String flavor) {
        return $("#rule_" + flavor).waitUntilPresent().isPresent();
    }

    private WebElement waitForButton(WebElementFacade buttonFacade) {
        WebElement button = buttonFacade.withTimeoutOf(10, TimeUnit.SECONDS).waitUntilEnabled();

        if (!button.isEnabled()) {
            button = buttonFacade.withTimeoutOf(10, TimeUnit.SECONDS).waitUntilEnabled().waitUntilVisible();
        }

        return button;
    }

    public void deleteDistribution(final int idx) {
        final String selector = "#deleteDistribution_" + idx;
        waitForPresenceOf(selector);
        $(selector).click();
    }

}
