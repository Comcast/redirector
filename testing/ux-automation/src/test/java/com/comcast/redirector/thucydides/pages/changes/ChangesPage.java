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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.pages.changes;

import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.pages.PageObject;

@DefaultUrl("/changes")
public class ChangesPage extends PageObject {

    public void clickApproveAllChangesButton() {
        final String selector = "#approveAllChanges";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickCancelAllChangesButton() {
        final String selector = "#cancelAllChanges";
        waitForPresenceOf(selector);
        $(selector).waitUntilVisible().click();
    }

    public void clickTriggerReloadModelButton() {
        final String selector = "#triggerModelReload";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickDownloadCoreBackupButton() {
        final String selector = "#downloadCoreBackup";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickApproveDistributionChangesButton() {
        final String selector = "#approveDistributionChanges";
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void clickCancelDistributionChangesButton() {
        final String selector = "#cancelDistributionChanges";
        waitForPresenceOf(selector);
        $(selector).click();
    }


    public void clickExportDistributionChangesButton() {
        final String selector = "#exportDistributionChanges";
        waitForPresenceOf(selector);
        $(selector).click();
    }


    public void approveChange(final PendingChangeType changeType, final String ruleName) {
        final String selector = "#approve" + changeType.getCssName() + "_" + ruleName;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void cancelChange(final PendingChangeType changeType, final String ruleName) {
        final String selector = "#cancel" + changeType.getCssName() + "_" + ruleName;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void exportChange(final PendingChangeType changeType, final String ruleName) {
        final String selector = "#export" + changeType.getCssName() + "_" + ruleName;
        waitForPresenceOf(selector);
        $(selector).waitUntilPresent().click();
    }

    public void exportDistributionChange(final int idx) {
        final String selector = "#exportDistributionChange_" + idx;
        waitForPresenceOf(selector);
        $(selector).click();
    }

    public void isChangeNotPresent(final PendingChangeType changeType, final String ruleName) {
        if (findAll(By.id("approve" + changeType.getCssName() + "_" + ruleName)).size() != 0) {
            throw new RuntimeException(ruleName + " is present on page");
        }
    }

    public void isChangePresent(final PendingChangeType changeType, final String ruleName) {
        final String selector = "#approve" + changeType.getCssName() + "_" + ruleName;
        waitForPresenceOf(selector);
        $(selector);
    }

    public void isDistributionSectionNotPresent() {
        if (findAll(By.id("distributionBlock")).size() != 0) {
            throw new RuntimeException("DistributionBlock is present on page");
        }
    }

    public void isDistributionSectionPresent() {
        final String selector = "#distributionBlock";
        waitForPresenceOf(selector);
        $(selector);
    }
}
