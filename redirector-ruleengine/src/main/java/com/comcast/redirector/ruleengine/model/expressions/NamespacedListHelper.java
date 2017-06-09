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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.model.AbstractModel;
import com.comcast.redirector.ruleengine.model.LanguageElement;
import com.comcast.redirector.ruleengine.model.NamespacedList;
import com.comcast.redirector.ruleengine.model.Values;
import com.google.common.collect.Iterables;
import org.w3c.dom.Element;

import java.util.List;

public class NamespacedListHelper {
    public static RightSide createRightSide(List<Element> elements, AbstractModel model, String tag) {
        RightSide rightSide = new RightSide();
        //miss the first element
        for (Element element : Iterables.skip(elements, 1)) {
            LanguageElement input = model.createLanguageElement(element);
            if (input instanceof NamespacedList) {
                rightSide.setNamespacedList((NamespacedList) input);
            } else if (input instanceof Values) {
                rightSide.setValues((Values) input);

            } else {
                throw new IllegalStateException("Element " + tag + "'s second or third child must be a values or namespacedList.  Found: "
                        + element.getTagName());
            }
        }
        return rightSide;
    }
}
