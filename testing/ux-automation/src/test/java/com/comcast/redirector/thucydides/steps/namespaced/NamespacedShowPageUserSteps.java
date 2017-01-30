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
package com.comcast.redirector.thucydides.steps.namespaced;

import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.thucydides.pages.namespaced.NamespacedShowPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class NamespacedShowPageUserSteps extends ScenarioSteps {

    NamespacedShowPage showPage;

    @Step
    public NamespacedShowPageUserSteps openPage() {
        showPage.open();
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps setNameFilter(String name) {
        showPage.setNameFilter(name);
        showPage.waitForAnyTextToAppear(showPage.getFirstElementLink().getText(), name);

        return this;
    }

    @Step
    public NamespacedShowPageUserSteps confirmDeletionNamespaced() {
        showPage.confirmDeletionNamespaced();
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps editNamespaced(String name) {
        showPage.editNamespaced(name);
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps editFirstNamespaced() {
        showPage.editFirstNamespacedList();
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps deleteNamespaced(String name) {
        showPage.deleteNamespaced(name);
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps selectOneTypeSelector(NamespacedListType type) {
        showPage.waitFor(5).seconds();
        showPage.changeAllSelectorsButOne(type);
        return this;
    }

    @Step
    public NamespacedShowPageUserSteps resetTypeSelectorsState() {
        showPage.selectAllTypeSelectors();
        return this;
    }
}
