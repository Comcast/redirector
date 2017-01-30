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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.redirector.thucydides.pages.rules.generic;

import net.thucydides.core.pages.PageObject;

import static com.comcast.redirector.thucydides.util.GenericTestUtils.*;

public class RulesAddPageObjects extends PageObject {

    public void setName(String ruleName) {
        find(waitToBePresent(this, "#ruleName")).type(ruleName);
    }

    public void saveRule() {
        find(waitToBePresent(this, "#saveEntity")).click();
    }

    public void saveRuleAsTemplate() {
        find(waitToBePresent(this, "#saveEntityAsTemplate")).click();
        waitFor(1).second();
    }

    public void cancelSaveRule() {
        find(waitToBePresent(this, "#cancelEditEntity")).click();
    }

    public void addCondition() {
        find(waitToBePresent(this, "#addCondition")).click();
    }

    public void addOrCondition() {
        find(waitToBePresent(this, "#addOrCondition")).click();
    }

    public void addXorCondition() {
        find(waitToBePresent(this, "#addXorCondition")).click();
    }

    public void setParameterName(String parameterName) {
        find(waitToBePresent(this, ".expression-name-1:last")).type(parameterName);
    }

    public void selectCondition(String condition) {
        find(waitToBePresent(this, ".expression-condition:last")).selectByValue(condition);
    }

    public void setConditionValue(String conditionValue) {
        find(waitToBePresent(this, ".expression-value:last")).type(conditionValue);
    }

}
