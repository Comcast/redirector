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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.thucydides.steps.stacksmanagement;

import com.comcast.redirector.thucydides.pages.stacksmanagement.StacksManagementPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.steps.ScenarioSteps;

import static org.junit.Assert.assertEquals;

public class StacksManagementUserSteps extends ScenarioSteps {

    StacksManagementPage stacksManagementPage;

    @Step
    public StacksManagementUserSteps openPage() {
        stacksManagementPage.open();
        stacksManagementPage.waitFor(1).second();
        getDriver().navigate().refresh();
        stacksManagementPage.waitFor(1).second();
        return this;
    }

    @Step
    public StacksManagementUserSteps whitelistStack(String stackSwitchValue) {
        stacksManagementPage.whitelistStack(stackSwitchValue);
        return this;
    }

    @Step
    public StacksManagementUserSteps saveStacks() {
        stacksManagementPage.saveStacks();
        return this;
    }

    @Step
    public StacksManagementUserSteps refreshPage() {
        stacksManagementPage.getDriver().navigate().refresh();
        return this;
    }

    @Step
    public StacksManagementUserSteps verifyStackSwitchState(String stackName, String expectedState) {
        assertEquals(expectedState, stacksManagementPage.getStackSwitchText(stackName));
        return this;
    }

    @Step
    public StacksManagementUserSteps verifyActiveStackCount(Integer activeStackCount) {
        assertEquals(activeStackCount.toString(), stacksManagementPage.getActiveStackCount());
        return this;
    }

    @Step
    public StacksManagementUserSteps verifyInactiveStackCount(Integer inactiveStackCount) {
        assertEquals(inactiveStackCount.toString(), stacksManagementPage.getInactiveStackCount());
        return this;
    }

    @Step
    public StacksManagementUserSteps clickDeleteAllStacksButton() {
        stacksManagementPage.clickRemoveAllInactiveStacksButton();
        return this;
    }

    @Step
    public StacksManagementUserSteps clickExportAllStacks() {
        stacksManagementPage.clickExportButton();
        stacksManagementPage.clickExportAllLink();
        return this;
    }

    @Step
    public StacksManagementUserSteps editComment(String stackPath) {
        stacksManagementPage.find(By.id("stackComment_" + stackPath)).click();

        return this;
    }

    @Step
    public StacksManagementUserSteps verifyCommentContainsText(String stackPath, String text) {
        stacksManagementPage.find(By.id("stackComment_" + stackPath)).shouldContainText(text);

        return this;
    }

    @Step
    public StacksManagementUserSteps verifyCommentDoesNotContainText(String stackPath, String text) {
        stacksManagementPage.find(By.id("stackComment_" + stackPath)).shouldNotContainText(text);

        return this;
    }

    @Step
    public StacksManagementUserSteps typeTextToCommentInput(String text) {
        stacksManagementPage.find(By.jquery("textarea.editable-input:last")).type(text);

        return this;
    }

    @Step
    public StacksManagementUserSteps saveComment() {
        stacksManagementPage.find(By.jquery("button.saveComment:last")).click();

        return this;
    }

    @Step
    public StacksManagementUserSteps cancelCommentEdit() {
        stacksManagementPage.find(By.jquery("button.cancelComment:last")).click();

        return this;
    }
}
