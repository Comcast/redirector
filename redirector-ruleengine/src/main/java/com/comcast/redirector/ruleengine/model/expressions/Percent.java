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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.model.LanguageElement;
import com.comcast.redirector.ruleengine.model.Value;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class Percent extends BooleanExpression {
    public static final String ATTRIBUTE_MAC = "mac";
    public static final String ATTRIBUTE_ACCOUNT_ID = "serviceAccountId";

    private double percentage;

    @Override
    public boolean evaluate(Map<String, String> params) {
        boolean hasValidAccountIdParam = params.containsKey(ATTRIBUTE_ACCOUNT_ID)
                && StringUtils.isNotBlank(params.get(ATTRIBUTE_ACCOUNT_ID));
        String calculationAttribute = hasValidAccountIdParam
                ? params.get(ATTRIBUTE_ACCOUNT_ID)
                : params.get(ATTRIBUTE_MAC);
        if (calculationAttribute != null) {
            if (fitsPercent(calculationAttribute, percentage)) {
                return true;
            }
        }
        return false;
    }

    private boolean fitsPercent(String calculationAttribute, double percent) {
        HashCode hashcode = Hashing.sipHash24().hashString(calculationAttribute, Charsets.UTF_8);
        long percentHashLong = (long) (Long.MAX_VALUE / 10000 * (percent * 100));
        return (percentHashLong >= Math.abs(hashcode.asLong()));
    }

    @Override
    protected void init(Element element) {
        super.init(element);

        List<Element> children = getChildElements(element);
        if (children.size() != 1) {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag");
        }

        LanguageElement valueElement = model.createLanguageElement(children.get(0));
        if (!(valueElement instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag");
        }

        Value value = (Value) valueElement;

        if (!value.isNumericValue(null)) {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag " +
                    "that contains a double value between 0 and 100");
        }

        percentage = value.getNumericValue(null);

        if (percentage < 0 || percentage > 100) {
            throw new IllegalStateException("Element " + element.getTagName() + " must define a <value> child tag " +
                    "that contains a double value between 0 and 100");
        }
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        return getName() + " [ random < " + percentage + " ] \n";
    }
}
