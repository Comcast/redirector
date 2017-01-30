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
 */
package com.comcast.redirector.thucydides.tests.main.modelInitializer;

import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.modelInitializer.ModelInitializerSteps;
import com.comcast.redirector.thucydides.steps.settings.SettingsPageUserSteps;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelInitializerTest {

    @ManagedPages(defaultUrl = TestConstants.MAIN_UX_URL)
    public Pages pages;

    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @Steps
    public GenericPageSteps genericPageSteps;

    @Steps
    private ModelInitializerSteps modelInitializerSteps;

    @Test
    public void _1_startRedirection() throws InterruptedException {

        modelInitializerSteps
                .openPage()
                .createNewModelForRedirect();
        genericPageSteps
                .clickModalOkButton();

    }
}
