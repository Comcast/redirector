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
package com.comcast.redirector.thucydides.pages.settings;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/settings")
public class SettingsPage extends PageObject {

    public SettingsPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/div[2]/div[2]/form/div/div[1]/input")
    private WebElementFacade minHosts;

    @FindBy(xpath="html/body/div[1]/div[2]/div/div/div/div[2]/div[2]/form/div/div[2]/input")
    private WebElementFacade appMinHosts;

    @FindBy(css = "button#saveEntity")
    private WebElementFacade saveButton;

    @FindBy(id = "toast-message")
    private WebElementFacade toaster;

    public void setMinHosts(String minHostsValue) {
        minHosts.type(minHostsValue);
    }

    public void setAppMinHosts(String appMinHostsValue) {
        appMinHosts.type(appMinHostsValue);
    }

    public void saveSettings() {saveButton.click(); }
}
