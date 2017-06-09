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
package com.comcast.redirector.thucydides.steps.namespaced;

import com.comcast.redirector.thucydides.pages.namespaced.NamespacedAddUnencodedPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class NamespacedAddPageUserSteps extends ScenarioSteps {

    NamespacedAddUnencodedPage namespacedAddUnencodedPage;

    @Step
    public NamespacedAddPageUserSteps openPage() {
        getPage().open();
        return this;
    }

    @Step
    public NamespacedAddPageUserSteps setName(String name) {
        getPage().setName(name);
        return this;
    }

    @Step
    public NamespacedAddPageUserSteps setDescription(String description) {
        getPage().setDescription(description);
        return this;
    }

    @Step
    public NamespacedAddPageUserSteps addValue(String value) {
        getPage().setValue(value);
        getPage().addValue();

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps saveNamespaced() {
        getPage().saveNamespaced();
        getPage().verifySuccessToasterIsShown();

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps tryToSaveNamespaced() {
        getPage().saveNamespaced();

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps saveNamespacedAndVerifyErrorIsShown() {
        getPage().saveNamespaced();
        getPage().verifyErrorToasterIsShown();

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps deleteDuplicateFromExistingList(String value) {
        getPage().deleteValueFromExistingList(value);
        getPage().verifyDuplicateValueIsNoLongerPresent(value);

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps deleteDuplicateFromCandidateList(String value) {
        getPage().waitFor(1).second();
        getPage().deleteValueFromCandidateList(value);
        getPage().verifyDuplicateValueIsNoLongerPresent(value);

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps saveNamespacedListFromDuplicatesModal() {
        getPage().clickContinueFromDuplicatesModalButton();
        getPage().waitFor(200).milliseconds();
        getPage().verifySuccessToasterIsShown();

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps verifyPageHasListType(String type) {
        getPage().verifyPageHasListType(type);

        return this;
    }

    @Step
    public NamespacedAddPageUserSteps closePage() {
        getPage().closePage();

        return this;
    }

    NamespacedAddUnencodedPage getPage () {
        return namespacedAddUnencodedPage;
    }
}
