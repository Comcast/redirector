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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.pages.partners;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;
import org.junit.Assert;


@DefaultUrl("/partners")
public class PartnersPage extends PageObject {
    public void clickAddPartnerButton() {
        final String selector = "#addPartner";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void setId(String value) {
        final String selector = "#inputId";
        waitForPresenceOf(selector);
        $(selector).type(value);
    }

    public void setName(String value) {
        final String selector = "#inputPropertyName";
        waitForPresenceOf(selector);
        $(selector).type(value);
    }

    public void setValue(String value) {
        final String selector = "#inputPropertyValue";
        waitForPresenceOf(selector);
        $(selector).type(value);
    }

    public void clickAddPropertyButton() {
        final String selector = "#addProperty";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickSaveButton() {
        final String selector = "#saveEntity";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void isPropertyExist(String key, String value) {
        final String keySelector = "#property_" + key + " .property-key";
        final String valueSelector = "#property_" + key + " .property-value";
        waitForPresenceOf(keySelector);
        waitForPresenceOf(valueSelector);
        $(keySelector).waitUntilPresent();
        Assert.assertTrue($(valueSelector).getText().equals(value));
    }

    public void clickEditPartnerButton(String value) {
        clickActionsDropdown(value);

        final String selector = "#editPartner_" + value;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickDeletePartnerButton(String value) {
        clickActionsDropdown(value);

        String selector = "#deletePartner_" + value;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickExportPartnerButton(String value) {
        clickActionsDropdown(value);

        final String selector = "#exportPartner_" + value;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickExportAllPartnerButton() {
        waitABit(5000);
        final String selector = "#exportAll";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void removePropertyById(String key) {
        String selector = "#property_" + key + " .property-remove";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    private void clickActionsDropdown(String value) {
        final String selector = "#actionsDropdown_" + value;
        waitABit(5000); // toaster time
        waitForPresenceOf(selector);
        $(selector).click();
    }

}
