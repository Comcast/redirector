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

package com.comcast.redirector.thucydides.steps.changes;

import com.comcast.redirector.thucydides.pages.changes.ChangesPage;
import com.comcast.redirector.thucydides.tests.main.changes.PendingChangeType;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.runner.RunWith;

@RunWith(ThucydidesRunner.class)
public class ChangesPageSteps extends ScenarioSteps {

    private ChangesPage page;

    public ChangesPage changesPage() {
        return getPages().currentPageAt(ChangesPage.class);
    }

    @Step
    public ChangesPageSteps clickApproveAllChangesButton() {
        page.clickApproveAllChangesButton();
        return this;
    }

    @Step
    public ChangesPageSteps clickCancelAllChangesButton() {
        page.clickCancelAllChangesButton();
        return this;
    }

    @Step
    public ChangesPageSteps clickTriggerReloadModelButton() {
        page.clickTriggerReloadModelButton();

        return this;
    }

    @Step
    public ChangesPageSteps clickDownloadCoreBackupButton() {
        page.clickDownloadCoreBackupButton();

        return this;
    }

    @Step
    public ChangesPageSteps clickApproveDistributionChangesButton() {
        page.clickApproveDistributionChangesButton();

        return this;

    }

    @Step
    public ChangesPageSteps clickCancelDistributionChangesButton() {
        page.clickCancelDistributionChangesButton();

        return this;
    }

    @Step
    public ChangesPageSteps clickExportDistributionChangesButton() {
        page.clickExportDistributionChangesButton();

        return this;
    }

    @Step
    public ChangesPageSteps refreshPage() {
        page.getDriver().navigate().refresh();

        return this;
    }

    @Step
    public ChangesPageSteps openPage() {
        page.open();
        page.waitFor(2).seconds();

        return this;
    }

    @Step
    public ChangesPageSteps approveChange(final PendingChangeType changeType, final String name) {
        waitFor(700).milliseconds();
        page.approveChange(changeType, name);

        return this;
    }

    @Step
    public ChangesPageSteps cancelChange(final PendingChangeType changeType, final String name) {
        waitFor(500).milliseconds();
        page.cancelChange(changeType, name);

        return this;
    }

    @Step
    public ChangesPageSteps exportChange(final PendingChangeType changeType, final String name) {
        page.exportChange(changeType, name);

        return this;
    }

    @Step
    public ChangesPageSteps exportDistributionChange(final int idx) {
        page.exportDistributionChange(idx);

        return this;
    }

    @Step
    public ChangesPageSteps isChangeNotPresent(final PendingChangeType changeType, final String ruleName) {
        waitFor(500).milliseconds();
        page.isChangeNotPresent(changeType, ruleName);

        return this;
    }

    @Step
    public ChangesPageSteps isChangePresent(final PendingChangeType changeType, final String ruleName) {
        page.isChangePresent(changeType, ruleName);

        return this;
    }

    @Step
    public ChangesPageSteps isDistributionSectionNotPresent() {
        page.isDistributionSectionNotPresent();

        return this;
    }

    @Step
    public ChangesPageSteps isDistributionSectionPresent() {
        page.isDistributionSectionPresent();

        return this;
    }
}
