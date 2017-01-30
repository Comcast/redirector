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

import com.comcast.redirector.thucydides.pages.rules.flavor.FlavorRulesAddPage;
import com.comcast.redirector.thucydides.pages.rules.generic.RulesShowPageObjects;
import com.comcast.redirector.thucydides.steps.rules.generic.RulesAddPageGenericSteps;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

import static com.comcast.redirector.thucydides.tests.CachedModeUxTestSuite.Constants.OFFLINE_MODE_DIALOG_TITLE;
import static com.comcast.redirector.thucydides.util.GenericTestUtils.waitForStacksToBePresentOnServer;

public class FlavorRulesAddPageSteps extends RulesAddPageGenericSteps<FlavorRulesAddPageSteps> {

    private FlavorRulesAddPage addPage;
    private RulesShowPageObjects rulesShowPageObjects;

    @Step
    public FlavorRulesAddPageSteps openPage() throws InterruptedException {
        waitForStacksToBePresentOnServer();
        addPage.open();

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps verifyPageOpened() {
        Assert.assertTrue("Flavor Rule Edit page should be opened", addPage.getPageHeader().startsWith(TestConstants.FLAVOR_RULE_ADD_PAGE_TITLE));
        addPage.waitFor(1).second();

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps addSimpleServer(String flavor) {
        addPage.addServer();
        addPage.selectServerEditMode("simple");
        addPage.selectSimpleServerPath(flavor);

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps clickServerRemove() {
        addPage.clickServerRemoveButton();

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps addAdvancedServer(String serverPath) {
        addPage.addServer();
        addPage.selectServerEditMode("advanced");
        addPage.setAdvancedServerPath(serverPath);

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps setAdvancedServer(String serverPath) {
        addPage.setAdvancedServerPath(serverPath);

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps verifyOfflineModeAlertShown() {
        Assert.assertEquals("Offline alert should be shown", OFFLINE_MODE_DIALOG_TITLE, addPage.getModalDialogTitle());

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps enterQueryPair(String key, String value) {
        addPage.enterQueryKey(key);
        addPage.enterQueryValue(value);

        return getThis();
    }

    @Step
    public FlavorRulesAddPageSteps clickAddQueryPair() {
        addPage.clickAddQueryPairButton();

        return getThis();
    }

    @Override
    public FlavorRulesAddPageSteps getThis() {
        return this;
    }
}
