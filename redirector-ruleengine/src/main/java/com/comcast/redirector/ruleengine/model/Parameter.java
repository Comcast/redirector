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
package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.Value;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.Map;

public class Parameter extends LanguageElement implements Value {
    public String parameterName;

    @Override
    protected void init(Element element) {
        String text = element.getTextContent().trim();
        if (StringUtils.isEmpty(text)) {
            throw new IllegalStateException("parameter element has no value");
        }
        parameterName = text;
    }


    public String getStringValue(Map<String, String> params) {
        return params.get(parameterName);
    }

    public double getNumericValue(Map<String, String> params) {
        try {
            return Double.parseDouble(params.get(parameterName));
        } catch (Exception e) {
        }
        return 0;
    }

    public boolean isNumericValue(Map<String, String> params) {
        try {
            Double.parseDouble(params.get(parameterName));
            return true;
        } catch (Exception e) {
        }
        return false;
    }


    @Override
    public String toString(int indent, Map<String, String> params) {
        return doSpacing("parameter:" + parameterName + "[" + getStringValue(params) + "]", indent);
    }
}
