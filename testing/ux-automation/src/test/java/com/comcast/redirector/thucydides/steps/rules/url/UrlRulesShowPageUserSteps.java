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
package com.comcast.redirector.thucydides.steps.rules.url;

import com.comcast.redirector.thucydides.pages.rules.generic.RulesShowPageObjects;
import com.comcast.redirector.thucydides.pages.rules.url.rule.UrlRulesShowPage;
import com.comcast.redirector.thucydides.pages.rules.url.template.UrlRulesTemplatesShowPage;
import com.comcast.redirector.thucydides.steps.rules.generic.RulesShowPageGenericSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import org.junit.Assert;

public class UrlRulesShowPageUserSteps extends RulesShowPageGenericSteps<UrlRulesShowPageUserSteps> {

    UrlRulesShowPage urlRulesShowPage;
    RulesShowPageObjects rulesShowPageObjects;
    UrlRulesTemplatesShowPage urlRulesTemplatesShowPage;

    @Step
    public UrlRulesShowPageUserSteps openRulesPage() {
        urlRulesShowPage.open();
        return getThis();
    }

    @Step
    public UrlRulesShowPageUserSteps openTemplatesPage() {
        urlRulesTemplatesShowPage.open();
        urlRulesTemplatesShowPage.waitFor(500).milliseconds();
        return getThis();
    }

    @Step
    public UrlRulesShowPageUserSteps verifyPendingAddedRuleIsShown(String ruleName) {
        Assert.assertTrue(rulesShowPageObjects.isRulePresent(ruleName));
        Assert.assertTrue("Rule Preview should have 'PENDING ADD' look and feel", rulesShowPageObjects.isRulePendingForAddition(ruleName));

        return getThis();
    }

    @Step
    public UrlRulesShowPageUserSteps verifyPendingModifiedRuleIsShown(String ruleName) {
        Assert.assertTrue(rulesShowPageObjects.isRulePresent(ruleName));
        Assert.assertTrue("Rule Preview should have 'PENDING UPDATE' look and feel", rulesShowPageObjects.isRulePendingForUpdate(ruleName));

        return getThis();
    }

    @Override
    public UrlRulesShowPageUserSteps getThis() {
        return this;
    }
}
