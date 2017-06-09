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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.namespaced;


import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.thucydides.steps.namespaced.NamespacedAddEncodedPageUserSteps;
import com.comcast.redirector.thucydides.steps.namespaced.NamespacedAddPageUserSteps;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.runner.RunWith;

@RunWith(ThucydidesRunner.class)
public class NamespacedEncodedOfflineModeTest extends NamespacedBaseTest {

    @Steps
    NamespacedAddEncodedPageUserSteps namespacedAddEncodedPageUserSteps;

    @ManagedPages(defaultUrl = TestConstants.OFFLINE_UX_URL)
    public Pages pages;

    @Override
    String getSuffix() {
        return "_Encoded_Offline";
    }

    @Override
    String getValueSuffix() {
        return "_Encoded_Offline";
    }

    @Override
    NamespacedListType getSelectorForNamespacedListType() {
        return NamespacedListType.ENCODED;
    }

    @Override
    String getVisibleNamespacedListType() {
        return "ENCODED";
    }

    @Override
    protected NamespacedAddPageUserSteps getNamespacedAddSteps() {
        return namespacedAddEncodedPageUserSteps;
    }
}
