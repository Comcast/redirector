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
package com.comcast.redirector.thucydides.pages.namespaced;

import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@DefaultUrl("/namespaces/show")
public class NamespacedShowPage extends PageObject {

    public NamespacedShowPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "table#showNamespacedLists")
    private WebElement allNamespacedLists;

    @FindBy(id = "showText")
    private WebElementFacade showUnencodedSelector;

    @FindBy(id = "showIp")
    private WebElementFacade showIpSelector;

    @FindBy(id = "showEncoded")
    private WebElementFacade showEncodedSelector;

    @FindBy(name = "name")
    private WebElementFacade nameFilterField;

    @FindBy(xpath = "html/body/div[1]/div[2]/div/div/div/div/table/tbody/tr[1]/td[1]/a")
    private WebElementFacade firstElementLink;

    @FindBy(xpath = "html/body/div[3]/div/div/div[3]/button[1]")
    private WebElementFacade confirmDeletionButton;

    public void setNameFilter(String name) {
        nameFilterField.type(name);
        waitFor(1).seconds();
    }

    public void editNamespaced(String name) {
        allNamespacedLists.findElement(By.cssSelector("#" + name + " button.edit-entity-button")).click();
    }

    public void editFirstNamespacedList() {
        firstElementLink.click();
    }

    public void deleteNamespaced(String name) {
        allNamespacedLists.findElement(By.cssSelector("#" + name + " button.edit-entity-dropdown")).click();
        allNamespacedLists.findElement(By.cssSelector("#" + name + " a.delete-entity-button")).click();
    }

    public void confirmDeletionNamespaced() {
        confirmDeletionButton.waitUntilVisible();
        confirmDeletionButton.click();
    }

    public WebElementFacade getFirstElementLink() {
        return firstElementLink;
    }

    public void changeAllSelectorsButOne(NamespacedListType type) {
        Boolean changeUnencoded = !type.equals(NamespacedListType.TEXT);
        Boolean changeIp = !type.equals(NamespacedListType.IP);
        Boolean changeEncoded = !type.equals(NamespacedListType.ENCODED);

        if(changeIp) {
            changeSelectorState(showIpSelector);
        }
        if(changeUnencoded) {
            changeSelectorState(showUnencodedSelector);
        }
        if(changeEncoded) {
            changeSelectorState(showEncodedSelector);
        }
        waitFor(1).seconds();
    }

    public void selectAllTypeSelectors () {
        changeSelectorState(showIpSelector);
        changeSelectorState(showUnencodedSelector);
        changeSelectorState(showEncodedSelector);
    }

    private void changeSelectorState(WebElementFacade selector) {
            WebElementFacade selectorClickable = selector.find(By.className("toggle"))
                    .find(By.className("toggle-group"))
                    .find(By.className("toggle-handle"));
            selectorClickable.waitUntilVisible();
            selectorClickable.click();
    }

}
