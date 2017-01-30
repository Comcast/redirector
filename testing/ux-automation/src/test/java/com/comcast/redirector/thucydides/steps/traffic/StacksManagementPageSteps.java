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
package com.comcast.redirector.thucydides.steps.traffic;

import com.comcast.redirector.thucydides.pages.traffic.StacksManagementPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class StacksManagementPageSteps extends ScenarioSteps {

    StacksManagementPage stacksManagementPage;

    @Step
    public void isStacksManagementPage() {
        stacksManagementPage.open();
    }

    @Step
    public void checkWhitelisted(String path) {
        stacksManagementPage.checkWhitelisted(path);
    }

    @Step
    public boolean isWhitelisted(String path) {
        return stacksManagementPage.isWhitelisted(path);
    }

}
