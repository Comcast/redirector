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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.settings;

import com.comcast.redirector.thucydides.steps.settings.SettingsPageUserSteps;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SettingsBaseTest {
    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @Steps
    private SettingsPageUserSteps settingsPageSteps;

    @Test
    public void _1_setSettings() throws InterruptedException {
        settingsPageSteps
            .openPage()
            .setMinHosts("55")
            .setAppMinHosts("55")
            .saveSettings();
    }

    @Test
    public void _2_editSettings() {
        settingsPageSteps
            .openPage()
            .setMinHosts("1")
            .setAppMinHosts("1")
            .saveSettings();
    }
}
