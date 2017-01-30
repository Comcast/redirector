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

package com.comcast.redirector.thucydides.tests.main.changes;

import com.comcast.redirector.thucydides.tests.main.changes.base.FlavorRuleAndTemplateChangesBaseTest;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(ThucydidesRunner.class)
public class FlavorRuleAndTemplateChangesOfflineModeTest extends FlavorRuleAndTemplateChangesBaseTest {

    @ManagedPages(defaultUrl = TestConstants.OFFLINE_UX_URL)
    public Pages pages;

    @Managed(uniqueSession = false)
    public WebDriver webdriver;

}
