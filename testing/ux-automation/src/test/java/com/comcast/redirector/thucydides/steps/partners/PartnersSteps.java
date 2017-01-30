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

package com.comcast.redirector.thucydides.steps.partners;

import com.comcast.redirector.thucydides.pages.partners.PartnersPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class PartnersSteps extends ScenarioSteps {
    private PartnersPage page;

    @Step
    public PartnersSteps openPage() {
        page.open();
        return this;
    }

    @Step
    public PartnersSteps refreshPage() {
        page.getDriver().navigate().refresh();
        return this;
    }

    @Step
    public PartnersSteps clickAddPartnerButton() {
        page.clickAddPartnerButton();
        return this;
    }

    @Step
    public PartnersSteps setId(String value) {
        page.setId(value);
        return this;
    }

    @Step
    public PartnersSteps setName(String value) {
        page.setName(value);
        return this;
    }

    @Step
    public PartnersSteps setValue(String value) {
        page.setValue(value);
        return this;
    }

    @Step
    public PartnersSteps clickAddPropertyButton() {
        page.clickAddPropertyButton();
        return this;
    }

    @Step
    public PartnersSteps clickSaveButton() {
        page.clickSaveButton();
        return this;
    }

    @Step
    public PartnersSteps isPropertyExist(String key, String value) {
        page.isPropertyExist(key, value);
        return this;
    }

    @Step
    public PartnersSteps clickEditPartnerButton(String value) {
        page.clickEditPartnerButton(value);
        return this;
    }

    @Step
    public PartnersSteps clickDeletePartnerButton(String value) {
        page.clickDeletePartnerButton(value);
        return this;
    }

    @Step
    public PartnersSteps removePropertyById(String key) {
        page.removePropertyById(key);
        return this;
    }

    @Step
    public PartnersSteps clickExportPartnerButton(String value) {
        page.clickExportPartnerButton(value);
        return this;
    }


    @Step
    public PartnersSteps clickExportAllPartnerButton() {
        page.clickExportAllPartnerButton();
        return this;
    }
}
