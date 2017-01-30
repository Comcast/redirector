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
 * @author Maxym Dolina (mdolina@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.stacksmanagement;

import com.comcast.redirector.thucydides.steps.changes.ApproveChangesPageSteps;
import com.comcast.redirector.thucydides.steps.stacksmanagement.StacksManagementUserSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
public class StacksManagementTestOffline {

    @Managed(uniqueSession = true)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.OFFLINE_UX_URL)
    public Pages pages;

    @Steps
    StacksManagementUserSteps stacksManagementUserSteps;

    @Steps
    ApproveChangesPageSteps approveChangesPageSteps;

    @Test
    public void _1_whitelistStack2() throws Exception {
        stacksManagementUserSteps
                .openPage()
                .whitelistStack(UxTestSuite.Constants.STACK2)
                .saveStacks();

        approveChangesPageSteps.openPage()
                .approveWhitelisted();

        stacksManagementUserSteps.openPage()
                .verifyStackSwitchState(UxTestSuite.Constants.STACK2, StacksManagementTest.YES_STACK_SWITCH);
    }
}
