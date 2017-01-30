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

package com.comcast.redirector.thucydides.tests.main.rules.flavor.rule;

import com.comcast.redirector.thucydides.tests.main.rules.flavor.rule.FlavorRulesBaseTest;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static com.comcast.redirector.thucydides.util.TestConstants.OFFLINE_UX_URL;
import static com.comcast.redirector.thucydides.tests.UxTestSuite.Constants.RULE_FLAVOR_OFFLINE;

@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlavorRulesTestOfflineMode extends FlavorRulesBaseTest {

    @ManagedPages(defaultUrl = OFFLINE_UX_URL)
    private Pages pages;

    @Override
    String getFlavor() {
        return RULE_FLAVOR_OFFLINE;
    }
}
