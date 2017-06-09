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

import com.comcast.redirector.ruleengine.Value;
import com.comcast.redirector.ruleengine.model.LanguageElement;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.IpAddressInitException;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InIpRangeExpression extends BooleanExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(InIpRangeExpression.class);

    private Value leftSide;
    private RightSide rightSide;

    @Override
    protected void init(Element element) {
        super.init(element);

        List<Element> children = getChildElements(element);
        if (children.size() > 3) {
            throw new IllegalStateException("Element " + element.getTagName() + " must not have more than 3 children");
        }
        LanguageElement left = model.createLanguageElement(children.get(0));
        if (!(left instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + "'s first child must be a value or param.  Found: "
                    + children.get(0).getTagName());
        }

        leftSide = (Value) left;
        rightSide = NamespacedListHelper.createRightSide(children, model, Model.TAG_IN_IP_RANGE);

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

    public boolean evaluate(Map<String, String> params) {
        String value = leftSide.getStringValue(params);
        if (value == null) return false;
        value = value.toLowerCase();
        try {
            IpAddress ipAddress = new IpAddress(value);
            return negotiate ? !evaluateInIpRange(ipAddress) : evaluateInIpRange(ipAddress);
        } catch (IpAddressInitException e) {
            LOGGER.error("Bad ip address while redirecting : {}", e.getMessage());
            return false;
        }
    }

    private boolean evaluateInIpRange(IpAddress ipAddress) throws IpAddressInitException {
        IpAddress existIpAddress;
        for (String ipAddressString : rightSide.getValues().getValues()) {
            existIpAddress = new IpAddress(ipAddressString);
            if (existIpAddress.isInRange(ipAddress)) {
                return true;
            }
        }
        NamespacedListRepository namespacedListsHolder = model.getNamespacedListHolder();
        for (String namespacedListName : rightSide.getNamespacedList().getValues()) {
            Set<IpAddress> namespacedListValues = namespacedListsHolder.getIpAddressesFromNamespacedList(namespacedListName);
            if (namespacedListValues != null) {
                for (IpAddress ipAddressFromList : namespacedListValues) {
                    if (ipAddressFromList.isInRange(ipAddress)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
