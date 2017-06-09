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

package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.Value;
import com.comcast.redirector.ruleengine.model.LanguageElement;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class IsEmptyExpression extends BooleanExpression {
    private Value stringToCheck;

    @Override
    public boolean evaluate(Map<String, String> params) {
        String expression = stringToCheck.getStringValue(params);
        return negotiate ? StringUtils.isNotBlank(expression) : StringUtils.isBlank(expression);
    }

    @Override
    protected void init(Element element) {
        super.init(element);

        List<Element> children = getChildElements(element);
        if (children.size() != 1) {
            throw new IllegalStateException("Element " + element.getTagName() + " must have 1 child");
        }
        LanguageElement toCheck = model.createLanguageElement(children.get(0));
        if (!(toCheck instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + " has a child that is not a value or " +
                    "param: " + children.get(0).getTagName());
        }
        stringToCheck = (Value) toCheck;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        String name = getName() + " [ " + evaluate(params) + " ] \n";
        String stringToCheckString = stringToCheck.toString(indent + printSpacing, params) + "\n";
        sb.append(doSpacing(name, indent));
        sb.append(doSpacing("{\n", indent));
        sb.append(stringToCheckString);
        sb.append(doSpacing("}\n", indent));
        return sb.toString();
    }
}
