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
package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.model.LanguageElement;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.Map;

public abstract class BooleanExpression extends LanguageElement {
    private static final String NEGATION = "negation";

    protected boolean negotiate = false;

    @Override
    protected void init(Element element) {
        String attrValue = element.getAttribute(NEGATION);
        boolean isValidBoolean = attrValue.equalsIgnoreCase("true") || attrValue.equalsIgnoreCase("false");

        if (StringUtils.isNotBlank(attrValue) && isValidBoolean) {
            negotiate = attrValue.equalsIgnoreCase("true");
        } else if (StringUtils.isNotBlank(attrValue) && !isValidBoolean) {
            String error = String.format("if specified, '%s' attribute must be true or false. found: %s",
                    NEGATION, String.valueOf(attrValue));
            throw new IllegalArgumentException(error);
        }
    }

    protected abstract boolean evaluate(Map<String, String> params);

    public boolean evaluateExpression(Map<String, String> params) {
        return evaluate(params);
    }
}
