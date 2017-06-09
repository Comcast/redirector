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

package com.comcast.redirector.thucydides.tests.main.stacksmanagement;

import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.changes.ApproveChangesPageSteps;
import com.comcast.redirector.thucydides.steps.stacksmanagement.StacksManagementUserSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import com.comcast.redirector.thucydides.util.GenericTestUtils;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

import java.io.File;

@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StacksManagementTest {

    @Steps
    StacksManagementUserSteps stacksManagementUserSteps;

    @Steps
    ApproveChangesPageSteps approveChangesPageSteps;

    @Steps
    GenericPageSteps genericSteps;

    static final String YES_STACK_SWITCH = "Yes";
    static final String DATA_CENTER = "PO";
    static final String STACK = "POC1";
    static final String IPV4 = "10.0.1.1";
    static final String IPV6 = "ff01::55";
    static final File dirForDownload = GenericTestUtils.createTempDir();

    @Managed(uniqueSession = true)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.MAIN_UX_URL)
    public Pages pages;

    @Before
    public void setup() {
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
    }

    @Test
    public void _1_whitelistStack1() throws Exception {
        stacksManagementUserSteps
                .openPage()
                .whitelistStack(UxTestSuite.Constants.STACK1)
                .saveStacks();

        approveChangesPageSteps.openPage()
                .approveWhitelisted();

        stacksManagementUserSteps.openPage()
                .verifyStackSwitchState(UxTestSuite.Constants.STACK1, YES_STACK_SWITCH);
    }

    @Test
    public void _3_exportStacks() throws Exception {
        stacksManagementUserSteps.refreshPage().clickExportAllStacks();

        genericSteps.checkSavedFile(dirForDownload, "exportedservices-forService-xreGuide.json");
    }

    @Test
    public void _4_verifyStackCommentIsEmpty() throws Exception {
        stacksManagementUserSteps.refreshPage().verifyCommentContainsText(UxTestSuite.Constants.STACK2, "Add comment");
    }

    @Test
    public void _5_editAndSaveStackComment() throws Exception {
        String commentText = "Some comment";
        stacksManagementUserSteps.refreshPage()
                .editComment(UxTestSuite.Constants.STACK2)
                .typeTextToCommentInput(commentText)
                .saveComment()
                .refreshPage()
                .verifyCommentContainsText(UxTestSuite.Constants.STACK2, commentText);
    }

    @Test
    public void _5_editAndCancelStackComment() throws Exception {
        String commentText = "Some other comment";
        stacksManagementUserSteps.refreshPage()
                .editComment(UxTestSuite.Constants.STACK2)
                .typeTextToCommentInput(commentText)
                .cancelCommentEdit()
                .refreshPage()
                .verifyCommentDoesNotContainText(UxTestSuite.Constants.STACK2, commentText);
    }
}
