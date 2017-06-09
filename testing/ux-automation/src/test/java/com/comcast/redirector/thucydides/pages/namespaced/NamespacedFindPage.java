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
 * @author Maxym Dolina (mdolina@productengine.com)
 */

package com.comcast.redirector.thucydides.pages.namespaced;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/namespaces/findByItem")
public class NamespacedFindPage extends PageObject {

    public NamespacedFindPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(id = "searchInput")
    private WebElementFacade searchInput;

    @FindBy(id = "searchButton")
    private WebElementFacade searchButton;

    @FindBy(css = "td.namespaced-list-name")
    private WebElementFacade namespacedListName;

    @FindBy(id = "show-flavor-rules")
    private WebElementFacade showFlavorRulesButton;

    @FindBy(css = "a.rule-name")
    private WebElementFacade ruleName;

    public void typeSearchNamespacedListValue(String searchValue) {
        searchInput.type(searchValue);
    }

    public void clickSearchButton() {
        searchButton.click();
    }

    public String getNamespacedListName() {
        return namespacedListName.getText();
    }

    public void clickShowFlavorRulesButton() {
        showFlavorRulesButton.click();
    }

    public String getRuleName() {
        return ruleName.getText();
    }
}
