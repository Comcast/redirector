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

package com.comcast.redirector.thucydides.steps.namespaced;

import com.comcast.redirector.thucydides.pages.namespaced.NamespacedFindPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

import static org.junit.Assert.assertEquals;

public class NamespacedFindPageUserSteps extends ScenarioSteps {

    NamespacedFindPage findPage;

    @Step
    public NamespacedFindPageUserSteps openPage() {
        findPage.open();
        return this;
    }

    @Step
    public NamespacedFindPageUserSteps typeSearchValue(String namespacedListName) {
        findPage.typeSearchNamespacedListValue(namespacedListName);
        return this;
    }

    @Step
    public NamespacedFindPageUserSteps clickSearchButton() {
        findPage.clickSearchButton();
        return this;
    }

    @Step
    public NamespacedFindPageUserSteps verifyNamespacedListName(String namespaceName) {
        assertEquals(namespaceName, findPage.getNamespacedListName());
        return this;
    }

    @Step
    public NamespacedFindPageUserSteps clickShowFlavorRulesButton() {
        findPage.clickShowFlavorRulesButton();
        return this;
    }

    @Step
    public NamespacedFindPageUserSteps verifyRuleName(String ruleName) {
        assertEquals(ruleName, findPage.getRuleName());
        return this;
    }
}
