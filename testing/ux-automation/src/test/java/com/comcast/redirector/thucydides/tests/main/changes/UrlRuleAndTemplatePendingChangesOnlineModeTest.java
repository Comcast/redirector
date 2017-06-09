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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.changes;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.factory.ExpressionFactory;
import com.comcast.redirector.api.model.factory.UrlRuleFactory;
import com.comcast.redirector.thucydides.tests.main.changes.base.UrlRuleAndTemplatePendingChangesBaseTest;
import com.comcast.redirector.thucydides.util.GenericTestUtils;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;

@RunWith(ThucydidesRunner.class)
public class UrlRuleAndTemplatePendingChangesOnlineModeTest extends UrlRuleAndTemplatePendingChangesBaseTest {

    @ManagedPages(defaultUrl = TestConstants.MAIN_UX_URL)
    public Pages pages;

    @Managed(uniqueSession = false)
    public WebDriver webdriver;

    @Test
    public void triggerReloadModel() throws InterruptedException {
        createAndSaveUrlRule(URL_RULE_NAME);
        createAndSaveUrlRule(URL_RULE_NAME + 2);

        changesPageSteps
                .openPage()
                .clickTriggerReloadModelButton();

        genericPageSteps
                .clickModalOkButton();

        changesPageSteps
                .refreshPage()
                .isChangePresent(PendingChangeType.URL_RULE, URL_RULE_NAME)
                .isChangePresent(PendingChangeType.URL_RULE, URL_RULE_NAME + 2);

        deleteUrlRule(URL_RULE_NAME);
        deleteUrlRule(URL_RULE_NAME + 2);
    }

    @Test
    public void exportUrlRuleChange() throws Exception {
        final File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));

        createAndSaveUrlRule(URL_RULE_NAME);

        changesPageSteps
                .openPage()
                .exportChange(PendingChangeType.URL_RULE, URL_RULE_NAME);

        genericPageSteps
                .checkSavedFile(dirForDownload,
                        "exportedpendingChangesStatus-byId-" + URL_RULE_NAME + "-forService-" + SERVICE_NAME + ".json");

        deleteUrlRule(URL_RULE_NAME);
    }

}
