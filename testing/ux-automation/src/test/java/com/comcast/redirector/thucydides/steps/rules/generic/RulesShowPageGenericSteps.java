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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.redirector.thucydides.steps.rules.generic;

import com.comcast.redirector.thucydides.pages.rules.generic.RulesShowPageObjects;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.findby.By;
import org.junit.Assert;

public abstract class RulesShowPageGenericSteps<T extends RulesShowPageGenericSteps> {

    private RulesShowPageObjects page;

    @Step
    public T editRule(String id) {
        page.clickEditButton(id);
        return getThis();
    }

    @Step
    public DiffViewSteps openDiffView(String id) {
        page.clickDiffViewButton(id);
        return new DiffViewSteps();
    }

    @Step
    public T deleteRule(String id) {
        page.clickDeleteButton(id);
        return getThis();
    }

    @Step
    public T verifyPermanentRuleExists(String ruleName) {
        page.waitFor(1).second();
        Assert.assertNotNull(page.getRulePanel(ruleName));
        return getThis();
    }

    @Step
    public T verifyPermanentRuleDoesNotExist(String ruleName) {
        page.waitFor(1).second();
        page.shouldNotBeVisible(By.id("#" + ruleName));

        return getThis();
    }

    @Step
    public T verifyPendingDeletedRuleIsShown(String ruleName) {
        page.waitFor(1).second();
        Assert.assertTrue(page.isRulePresent(ruleName));
        Assert.assertTrue("Rule Preview should have 'PENDING DELETE' look and feel", page.isRulePendingForDeletion(ruleName));
        return getThis();
    }

    @Step
    public T waitForPageToLoad() {
        page.waitForTextToDisappear("Loading...");
        return getThis();
    }

    @Step
    public T verifyPendingModifiedRuleIsShown(String ruleName) {
        page.waitFor(1).second();
        Assert.assertTrue(page.isRulePresent(ruleName));
        Assert.assertTrue("Rule Preview should have 'PENDING UPDATE' look and feel", page.isRulePendingForUpdate(ruleName));
        return getThis();
    }

    public class DiffViewSteps {

        public DiffViewSteps verifyParameterModifiedInPending(String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on right side of diff view ", page.hasHighLightedLineInPendingDiff(parameterModified));

            return this;
        }

        public DiffViewSteps verifyParameterModifiedInPending(String ruleName, String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on right side of diff view ", page.hasHighLightedLineInPendingDiff(ruleName, parameterModified));

            return this;
        }

        public DiffViewSteps verifyValueOfHighlightedLineInPending(String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on right side of diff view ", page.hasModifiedLineInPendingDiff(parameterModified));

            return this;
        }

        public DiffViewSteps verifyValueOfHighlightedLineInPending(String ruleName, String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on right side of diff view ", page.hasModifiedLineInPendingDiff(ruleName, parameterModified));

            return this;
        }

        public DiffViewSteps verifyObjectToBeChangedIsPresentInCurrent(String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on left side of diff view ", page.hasHighLightedLineInCurrentDiff(parameterModified));

            return this;
        }

        public DiffViewSteps verifyObjectToBeChangedIsPresentInCurrent(String ruleName, String parameterModified) {
            Assert.assertTrue("Expected " + parameterModified + " on left side of diff view ", page.hasHighLightedLineInCurrentDiff(ruleName, parameterModified));

            return this;
        }

        public DiffViewSteps verifyQueryPairAdded(String queryPair) {
            Assert.assertTrue("Expected " + queryPair + " on right side of diff view ", page.hasAddedLineInDiff(queryPair));

            return this;
        }

        public DiffViewSteps verifyQueryPairAdded(String ruleName, String queryPair) {
            Assert.assertTrue("Expected " + queryPair + " on right side of diff view ", page.hasAddedLineInDiff(ruleName, queryPair));

            return this;
        }
    }

    public abstract T getThis();
}
