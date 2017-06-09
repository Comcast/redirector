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
package com.comcast.redirector.thucydides.pages.rules.flavor.rule;

import com.comcast.redirector.thucydides.pages.rules.generic.RulesShowPageObjects;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.WebDriver;



@DefaultUrl("/flavorRules/showAll/")
public class FlavorRulesShowPage extends PageObject {

    private RulesShowPageObjects rulesShowPageObjects;

    public FlavorRulesShowPage(WebDriver driver) {
        super(driver);
    }

    public boolean isRuleReturningFlavor(String ruleName, String flavor) {
        return rulesShowPageObjects.getSimpleRuleText(ruleName).contains("path: " + flavor);
    }

    public boolean isRuleReturningServerPath(String ruleName, String serverPath) {
        return rulesShowPageObjects.getSimpleRuleText(ruleName).contains("url: " + serverPath);
    }

    public boolean isRuleReturningQueryPairs(String ruleName, String queryPairs) {
        return rulesShowPageObjects.getSimpleRuleText(ruleName).contains("query: " + queryPairs);
    }
}
