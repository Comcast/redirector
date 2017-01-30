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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.redirector.thucydides.steps.rules.flavor;

import com.comcast.redirector.thucydides.pages.rules.flavor.rule.FlavorRulesShowPage;
import com.comcast.redirector.thucydides.pages.rules.flavor.template.FlavorRulesTemplatesShowPage;
import com.comcast.redirector.thucydides.pages.rules.generic.RulesShowPageObjects;
import com.comcast.redirector.thucydides.steps.rules.generic.RulesShowPageGenericSteps;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

public class FlavorRulesShowPageSteps extends RulesShowPageGenericSteps<FlavorRulesShowPageSteps> {

    RulesShowPageObjects rulesShowPageObjects;
    FlavorRulesShowPage rulesShowPage;
    FlavorRulesTemplatesShowPage rulesTemplatesShowPage;

    @Step
    public FlavorRulesShowPageSteps openRulesPage() {
        rulesShowPage.open();
        rulesShowPage.waitFor(500).milliseconds();

        return getThis();
    }

    @Step
    public FlavorRulesShowPageSteps openTemplatesPage() {
        rulesTemplatesShowPage.open();

        return getThis();
    }

    @Step
    public FlavorRulesShowPageSteps verifyPermanentRuleExists(String ruleName, String flavor, String queryPairs) {
        Assert.assertTrue(rulesShowPage.isRuleReturningFlavor(ruleName, flavor));
        Assert.assertTrue(rulesShowPage.isRuleReturningQueryPairs(ruleName, queryPairs));

        return getThis();
    }

    @Step
    public FlavorRulesShowPageSteps verifyPendingAddedRuleIsShown(String ruleName, String flavor, String queryPairs) {
        Assert.assertTrue(rulesShowPageObjects.isRulePresent(ruleName));
        Assert.assertTrue(rulesShowPage.isRuleReturningFlavor(ruleName, flavor));
        Assert.assertTrue(rulesShowPage.isRuleReturningQueryPairs(ruleName, queryPairs));
        Assert.assertTrue("Rule Preview should have 'PENDING ADD' look and feel", rulesShowPageObjects.isRulePendingForAddition(ruleName));

        return getThis();
    }

    @Step
    public FlavorRulesShowPageSteps verifyPendingAddedRuleAdvancedServerIsShown(String ruleName, String serverPath) {
        rulesShowPage.waitFor(1).second();
        Assert.assertTrue(rulesShowPageObjects.isRulePresent(ruleName));
        Assert.assertTrue(rulesShowPage.isRuleReturningServerPath(ruleName, serverPath));
        Assert.assertTrue("Rule Preview should have 'PENDING ADD' look and feel", rulesShowPageObjects.isRulePendingForAddition(ruleName));
        return getThis();
    }

    @Step
    public FlavorRulesShowPageSteps verifyPendingAddedRuleAdvancedServerContainsQueryPairsIsShown(String ruleName, String queryPairs) {
        Assert.assertTrue(rulesShowPage.isRuleReturningQueryPairs(ruleName, queryPairs));
        return getThis();
    }

    @Override
    public FlavorRulesShowPageSteps getThis() {
        return this;
    }
}
