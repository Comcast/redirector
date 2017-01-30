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
import com.comcast.redirector.ruleengine.model.Value;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rseamo200
 * Date: 11/5/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class Random extends BooleanExpression {
    private double percentage;

    @Override
    public boolean evaluate(Map<String, String> params) {
        return Math.random() * 100 < percentage;
    }

    @Override
    protected void init(Element element) {
        super.init(element);

        List<Element> children = getChildElements(element);
        if (children.size() != 1)
        {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag");
        }

        LanguageElement valueElement = model.createLanguageElement(children.get(0));
        if (! (valueElement instanceof Value))
        {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag");
        }

        Value value = (Value) valueElement;

        if (!value.isNumericValue(null))
        {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag " +
                    "that contains a double value between 0 and 100");
        }

        percentage = value.getNumericValue(null);

        if (percentage < 0 || percentage > 100)
        {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag " +
                    "that contains a double value between 0 and 100");
        }
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        return getName() + " [ random < " + percentage + " ] \n";
    }
}
