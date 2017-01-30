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
package com.comcast.redirector.thucydides.pages.rules.flavor;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.WebDriver;

import static com.comcast.redirector.thucydides.util.GenericTestUtils.*;

@DefaultUrl("/flavorRules/addNew/")
public class FlavorRulesAddPage extends PageObject {

    public FlavorRulesAddPage(WebDriver driver) {
        super(driver);
    }

    public void addServer() {
        find(waitToBePresent(this, "#addServer")).click();
    }

    public void selectServerEditMode(String serverEditMode) {
        find(waitToBePresent(this, ".server-edit-mode:last")).selectByValue(serverEditMode);
    }

    public void selectSimpleServerPath(String serverPath) {
        find(waitToBePresent(this, ".server-path-input:last")).selectByValue(serverPath);
    }

    public void setAdvancedServerPath(String serverPath) {
        find(waitToBePresent(this, ".server-url-input:last")).type(serverPath);
    }

    public String getModalDialogTitle() {
        return find(waitToBePresent(this, ".modal-title")).getText();
    }

    public String getPageHeader() {
        return find(waitToBePresent(this, "h1")).getText();
    }

    public void clickAddQueryPairButton() {
        find(waitToBePresent(this, ".add-query-pair:last")).click();
    }

    public void clickServerRemoveButton() {
        find(waitToBePresent(this, ".btn-server-delete:last")).click();
    }

    public void enterQueryKey(String key) {
        find(waitToBePresent(this, "input.query-key:last")).type(key);
    }

    public void enterQueryValue(String value) {
        find(waitToBePresent(this, ".query-value:last")).type(value);
    }
}
