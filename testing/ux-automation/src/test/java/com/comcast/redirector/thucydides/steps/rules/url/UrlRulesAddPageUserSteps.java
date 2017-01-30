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
package com.comcast.redirector.thucydides.steps.rules.url;

import com.comcast.redirector.thucydides.pages.rules.url.UrlRulesAddPage;
import com.comcast.redirector.thucydides.steps.rules.generic.RulesAddPageGenericSteps;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

import static com.comcast.redirector.thucydides.util.TestConstants.URL_RULE_ADD_PAGE_TITLE;

public class UrlRulesAddPageUserSteps extends RulesAddPageGenericSteps<UrlRulesAddPageUserSteps> {

    UrlRulesAddPage page;

    @Step
    public UrlRulesAddPageUserSteps openPage() {
        page.open();

        return this;
    }

    @Step
    public UrlRulesAddPageUserSteps setUrlParams(String protocol, String port, String urn, String ipVersion) {
        page.selectProtocol(protocol);
        page.setPort(port);
        page.setUrn(urn);
        page.selectIpVersion(ipVersion);

        return getThis();
    }

    @Step
    public UrlRulesAddPageUserSteps verifyPageOpened() {
        Assert.assertTrue("Flavor Rule Edit page should be opened", page.getPageHeader().startsWith(URL_RULE_ADD_PAGE_TITLE));

        return getThis();
    }

    @Override
    public UrlRulesAddPageUserSteps getThis() {
        return this;
    }
}
