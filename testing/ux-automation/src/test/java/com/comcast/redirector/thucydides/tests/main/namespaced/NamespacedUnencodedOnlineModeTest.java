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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.namespaced;

import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.runner.RunWith;

@RunWith(ThucydidesRunner.class)
public class NamespacedUnencodedOnlineModeTest extends NamespacedBaseTest {
    @ManagedPages(defaultUrl = TestConstants.MAIN_UX_URL)
    public Pages pages;

    @Override
    String getSuffix() {
        return "_Unencoded_Online";
    }

    @Override
    String getValueSuffix() {
        return "_Unencoded_Online";
    }

    @Override
    NamespacedListType getSelectorForNamespacedListType() {
        return NamespacedListType.TEXT;
    }

    @Override
    String getVisibleNamespacedListType() {
        return "TEXT";
    }
}
