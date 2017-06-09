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
 */
package com.comcast.redirector.thucydides.pages.rules.url;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.WebDriver;

import static com.comcast.redirector.thucydides.util.GenericTestUtils.*;

@DefaultUrl("/urlRules/addNew/")
public class UrlRulesAddPage extends PageObject {

    public UrlRulesAddPage(WebDriver driver) {
        super(driver);
    }

    public void setUrn(String urn) {
        find(waitToBePresent(this, ".urlParams_urn_input:last")).type(urn);
    }

    public void setPort(String port) {
        find(waitToBePresent(this, ".url-params-port-input:last")).type(port);
    }

    public void selectProtocol(String protocol) {
        find(waitToBePresent(this, ".url-params-protocol-dropdown:last")).selectByValue(protocol);
    }

    public void selectIpVersion(String ipVersion) {
        find(waitToBePresent(this, ".url-params-ip-version-dropdown:last")).selectByValue(ipVersion);
    }

    public String getPageHeader() {
        return find(waitToBePresent(this, "h1")).getText();
    }
}
