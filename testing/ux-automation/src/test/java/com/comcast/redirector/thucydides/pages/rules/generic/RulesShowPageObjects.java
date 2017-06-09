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

package com.comcast.redirector.thucydides.pages.rules.generic;

import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.WebElement;

import static com.comcast.redirector.thucydides.util.GenericTestUtils.*;


public class RulesShowPageObjects extends PageObject {

    public void clickEditButton(String ruleId) {
        find(waitToBePresent(this, "#" + ruleId + " button.edit-rule-button")).click();
    }

    public void clickDiffViewButton(String ruleId) {
        find(waitToBePresent(this, "#" + ruleId + " button.diff-view-button")).click();
    }

    public void clickDeleteButton(String ruleId) {
        find(waitToBePresent(this, "#" + ruleId + " button.delete-rule-button")).click();
    }

    private String getRulePanelClass(String ruleName) {
        return getRulePanel(ruleName).getAttribute("class");
    }

    public boolean isRulePendingForAddition(String ruleName) {
        return getRulePanelClass(ruleName).contains(RulePendingStates.ADD.getCssClass());
    }

    public boolean isRulePendingForUpdate(String ruleName) {
        return getRulePanelClass(ruleName).contains(RulePendingStates.UPDATE.getCssClass());
    }

    public boolean isRulePendingForDeletion(String ruleName) {
        return getRulePanelClass(ruleName).contains(RulePendingStates.DELETE.getCssClass());
    }

    public WebElement getRulePanel(String name) {
         return find(waitToBePresent(this, "#" + name));
    }

    public boolean isRulePresent(String name) {
        return getRulePanel(name) != null;
    }

    public String getSimpleRuleText(String ruleName) {
        return getRulePanel(ruleName).findElement(By.cssSelector("div.rule-simple-view")).getText();
    }

    public boolean hasModifiedLineInPendingDiff(String removedLine) {
        return findAll(waitToBePresent(this, "td.changes-diff-view-column:last span.modified"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(removedLine::equals)
                .count() == 1;
    }

    public boolean hasModifiedLineInPendingDiff(String ruleId, String removedLine) {
        return findAll(waitToBePresent(this, "#" + ruleId + " td.changes-diff-view-column:last span.modified"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(removedLine::equals)
                .count() == 1;
    }

    public boolean hasHighLightedLineInCurrentDiff(String lineToRemove) {
        return findAll(waitToBePresent(this, "td.changes-diff-view-column:first span.modified-light"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(lineToRemove::equals)
                .count() == 1;
    }

    public boolean hasHighLightedLineInCurrentDiff(String ruleId, String lineToRemove) {
        return findAll(waitToBePresent(this, "#" + ruleId + " td.changes-diff-view-column:first span.modified-light"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(lineToRemove::equals)
                .count() == 1;
    }

    public boolean hasHighLightedLineInPendingDiff(String parameterModified) {
        return findAll(waitToBePresent(this, "td.changes-diff-view-column:last span.modified-light"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(parameterModified::equals)
                .count() == 1;
    }

    public boolean hasHighLightedLineInPendingDiff(String ruleId, String parameterModified) {
        return findAll(waitToBePresent(this, "#" + ruleId + " td.changes-diff-view-column:last span.modified-light"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(parameterModified::equals)
                .count() == 1;
    }

    public boolean hasAddedLineInDiff(String parameterModified) {
        return findAll(waitToBePresent(this, "td.changes-diff-view-column:last span.inserted"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(parameterModified::equals)
                .count() == 1;
    }

    public boolean hasAddedLineInDiff(String ruleId, String parameterModified) {
        return findAll(waitToBePresent(this, "#" + ruleId + " td.changes-diff-view-column:last span.inserted"))
                .stream()
                .map(webElement -> webElement.getText().trim())
                .filter(parameterModified::equals)
                .count() == 1;
    }

    private enum RulePendingStates {
        ADD("panel-success"),
        UPDATE("panel-info"),
        DELETE("panel-danger");

        private String cssClass;

        RulePendingStates(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }
    }
}
