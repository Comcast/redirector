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
import com.comcast.redirector.ruleengine.model.Pattern;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class MatchesExpression extends BooleanExpression {
    private Value leftSide;
    private Pattern rightSide;

    @Override
    protected void init(Element element) {
        super.init(element);

        List<Element> children = getChildElements(element);
        if (children.size() != 2) {
            throw new IllegalStateException("Element " + element.getTagName() + " must have 2 children");
        }
        LanguageElement left = model.createLanguageElement(children.get(0));
        if (!(left instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + "'s first child must be a value or param.  Found: "
                    + children.get(0).getTagName());
        }
        LanguageElement right = model.createLanguageElement(children.get(1));
        if (!(right instanceof Pattern)) {
            throw new IllegalStateException("Element " + element.getTagName() + "'s second child must be a pattern.  Found: "
                    + children.get(1).getTagName());
        }
        leftSide = (Value) left;
        rightSide = (Pattern) right;
    }

    @Override
    public boolean evaluate(Map<String, String> params) {
        String value = leftSide.getStringValue(params);
        if (value == null) return false;
        return negotiate ? !value.matches(rightSide.getValue()) : value.matches(rightSide.getValue());
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        String name = getName() + " [ " + evaluate(params) + " ] \n";
        String leftSide = this.leftSide.toString(indent + printSpacing, params) + "\n";
        String rightSide = this.rightSide.toString(indent + printSpacing, params) + "\n";
        sb.append(doSpacing(name, indent));
        sb.append(doSpacing("{\n", indent));
        sb.append(leftSide);
        sb.append(rightSide);
        sb.append(doSpacing("}\n", indent));
        return sb.toString();
    }
}
