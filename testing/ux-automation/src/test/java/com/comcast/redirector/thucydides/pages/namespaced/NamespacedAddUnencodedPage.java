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

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/namespaces/add")
public class NamespacedAddUnencodedPage extends PageObject {

    final String DELETE_FROM_CANDIDATE_CLASS_PREFIX = "deleteFromCandidate_";

    final String DELETE_FROM_EXISTING_CLASS_PREFIX = "deleteFromExisting_";

    public NamespacedAddUnencodedPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "continueButton")
    private WebElementFacade duplicatesModalContinueButton;

    @FindBy(id = "editNamespacedListTypeSpan")
    private WebElementFacade typeSpan;

    @FindBy(id = "namespacedListName")
    private WebElementFacade nameField;

    @FindBy(id = "namespacedListDescription")
    private WebElementFacade descriptionField;

    @FindBy(id = "valueToAdd")
    private WebElementFacade valueField;

    @FindBy(id = "addValueButton")
    private WebElementFacade addValueButton;

    @FindBy(id = "saveEntity")
    private WebElementFacade saveButton;

    @FindBy(id = "toast-container")
    private WebElementFacade toaster;

    public void setName(String namespacedName) {
        nameField.type(namespacedName);
    }

    public void setDescription(String namespacedDescription) {
        descriptionField.type(namespacedDescription);
    }

    public void setValue(String namespacedValue) {
        valueField.type(namespacedValue);
    }

    public void addValue() {
        addValueButton.click();
    }

    public void saveNamespaced() {
        saveButton.click();
    }

    public void verifySuccessToasterIsShown() {
        toaster.waitUntilVisible();
        toaster.shouldContainText("Success");
    }

    public void closePage() {
        getDriver().close();
    }

    public void verifyErrorToasterIsShown() {
        toaster.waitUntilVisible();
        toaster.shouldContainText("Error");
    }

    public void verifyPageHasListType(String type) {
        typeSpan.waitUntilVisible();
        typeSpan.shouldContainText(type);
    }

    public void deleteValueFromExistingList (String value) {
        find(By.id(DELETE_FROM_EXISTING_CLASS_PREFIX + value)).click();
    }

    public void deleteValueFromCandidateList (String value) {
        find(By.id(DELETE_FROM_CANDIDATE_CLASS_PREFIX + value)).click();
    }

    public void clickContinueFromDuplicatesModalButton () {
        duplicatesModalContinueButton.waitUntilEnabled();
        waitFor(500).milliseconds();
        duplicatesModalContinueButton.click();
    }

    public void verifyDuplicateValueIsNoLongerPresent (String value) {
        if (findAll(By.id("duplicateValue_" + value)).size() >= 1) {
            Assert.fail("Duplicate value is still visible");
        };
    }
}
