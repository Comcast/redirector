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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * This statement provide default values for urlRule tag, in case if some of conditions doesn't provide some value:
 * <default>
 *    <urlRule>
 *       <urn></urn>
 *       <port></port>
 *       <protocol></protocol>
 *       <ipProtocolVersion></ipProtocolVersion>
 *    </urlRule>
 * </default>
 */
public class UrlRuleDefaultStatement extends Statement {

    public static final String TAG_RULE = "urlRule";
    public static final String TAG_URN = "urn";
    public static final String TAG_PORT = "port";
    public static final String TAG_PROTOCOL = "protocol";
    public static final String TAG_IP_PROTOCOL_VERSION = "ipProtocolVersion";

    private UrlParams defaultItem = new UrlParams();
    private static Logger log = LoggerFactory.getLogger(UrlRuleDefaultStatement.class);

    @Override
    protected void init(Element element) {
        List<Element> defaultChildren = getChildElements(element);
        Element defaultChild = defaultChildren.get(0); //get first item
        if (!defaultChild.getTagName().equals(TAG_RULE)) {
            throw new IllegalStateException("defaultItem children elements except the last must be url rule. " +
                    "found: " + defaultChild.getTagName());
        }
        // validate number of children
        List<Element> groupChildren = getChildElements(defaultChild);
        if (groupChildren.size() != 4) {
            throw new IllegalStateException("defaultItem must contain four elements: urn, port, protocol and ipProtocolVersion. " +
                    "found " + groupChildren.size() + " elements");
        }
        // validate and extract values
        for (Element child : groupChildren) {
            if (child.getTagName().equals(TAG_URN)) {
                defaultItem.setUrn(extractStringValue(child));
            } else if (child.getTagName().equals(TAG_PORT)) {
                defaultItem.setPort(extractIntValue(child));
            } else if (child.getTagName().equals(TAG_PROTOCOL)) {
                defaultItem.setProtocol(extractStringValue(child));
            } else if (child.getTagName().equals(TAG_IP_PROTOCOL_VERSION)) {
                defaultItem.setIPProtocolVersion(extractIntValue(child));
            }
        }
    }

    @Override
    public Object execute(Map<String, String> params) {
        return defaultItem;
    }

    @Override
    public boolean returnFulfilled() {
        return true;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        return defaultItem.toString(indent, params);
    }

    private String extractStringValue(Element element) {
        return element.getTextContent().trim();
    }

    private int extractIntValue(Element element) {
        return Integer.parseInt(element.getTextContent().trim());
    }
}
