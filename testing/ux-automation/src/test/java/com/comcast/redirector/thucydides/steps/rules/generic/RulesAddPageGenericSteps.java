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

package com.comcast.redirector.thucydides.steps.rules.generic;

import com.comcast.redirector.thucydides.pages.rules.generic.RulesAddPageObjects;
import net.thucydides.core.annotations.Step;

public abstract class RulesAddPageGenericSteps<T extends RulesAddPageGenericSteps> {

    RulesAddPageObjects page;

    @Step
    public T setName(String name) {
        page.setName(name);

        return getThis();
    }

    @Step
    public T addCondition(String parameterName, String condition, String value) {
        page.addCondition();
        changeCondition(parameterName, condition, value);

        return getThis();
    }

    @Step
    public T cancelSaveRule() {
        page.cancelSaveRule();

        return getThis();
    }

    @Step
    public T saveAsRule() {
        page.saveRule();
        return getThis();
    }

    @Step
    public T saveRuleAsTemplate() {
        page.saveRuleAsTemplate();
        return getThis();
    }

    @Step
    public T setParameterValue(String value) {
        page.setConditionValue(value);

        return getThis();
    }

    @Step
    public T changeCondition(String parameterName, String condition, String value) {
        page.setParameterName(parameterName);
        page.selectCondition(condition);
        page.setConditionValue(value);

        return getThis();
    }

    public abstract T getThis();

}
