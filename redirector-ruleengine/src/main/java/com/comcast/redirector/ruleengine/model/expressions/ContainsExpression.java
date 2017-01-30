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

import com.comcast.redirector.ruleengine.Value;
import com.comcast.redirector.ruleengine.model.LanguageElement;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainsExpression extends BooleanExpression {
    private static final String CASE_SENSITIVE = "caseSensitive";

    private Value leftSide;
    private RightSide rightSide;
    private boolean caseSensitive = false;

    @Override
    protected void init(Element element) {
        super.init(element);
        List<Element> children = getChildElements(element);
        if (children.size() > 3) {
            throw new IllegalStateException("Element " + element.getTagName() + " must not have more than 3 children");
        }
        LanguageElement left = model.createLanguageElement(children.get(0));
        if (!(left instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + "'s first child must be a value or " +
                    "param.  Found: " + children.get(0).getTagName());
        }
        rightSide = NamespacedListHelper.createRightSide(children, model, model.TAG_CONTAINS);
        String s = element.getAttribute(CASE_SENSITIVE);
        if (s != null && !s.isEmpty()) {
            if (s.equalsIgnoreCase("true")) {
                caseSensitive = true;
            } else if (s.equalsIgnoreCase("false")) {
                caseSensitive = false;
            } else {
                throw new IllegalArgumentException("if specified, caseSensitive attribute must be true or false. " +
                        "found: " + s);
            }
        }
        leftSide = (Value) left;
    }

    @Override
    public boolean evaluate(Map<String, String> params) {
        String value = leftSide.getStringValue(params);
        if (value == null) return false;
        boolean result = false;
        if (evaluateValues(value)) {
            result = true;
        } else if (evaluateNamespacedList(value)) {
            result = true;
        }
        return negotiate ? !result : result;
    }

    private boolean evaluateNamespacedList(String value) {
        NamespacedListRepository namespacedListsHolder = model.getNamespacedListHolder();

        for (String namespacedList : rightSide.getNamespacedList().getValues()) {
            Set<String> namespacedListValues = namespacedListsHolder.getNamespacedListValues(namespacedList);
            if (!caseSensitive) {
                if (containsIgnoreCase(namespacedListValues, value)) {
                    return true;
                }
            } else {
                if (namespacedListValues.contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean evaluateValues(String value) {
        if (!caseSensitive) {
            if (containsIgnoreCase(rightSide.getValues().getValues(), value)) return true;
        } else {
            if (rightSide.getValues().contains(value)) return true;
        }
        return false;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        String name = getName() + " [ " + evaluate(params) + " ] \n";
        String leftSide = this.leftSide.toString(indent + printSpacing, params) + "\n";
        String rightSide = this.rightSide.toString(indent + printSpacing) + "\n";
        sb.append(doSpacing(name, indent));
        sb.append(doSpacing("{\n", indent));
        sb.append(leftSide);
        sb.append(rightSide);
        sb.append(doSpacing("}\n", indent));
        return sb.toString();
    }

    public boolean containsIgnoreCase(Set<String> namespacedListValues, String value) {
        Iterator<String> it = namespacedListValues.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(value))
                return true;
        }
        return false;
    }
}
