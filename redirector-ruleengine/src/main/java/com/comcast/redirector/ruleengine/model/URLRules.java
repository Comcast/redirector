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

package com.comcast.redirector.ruleengine.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class URLRules extends LanguageElement {
    private final static Logger logger = LoggerFactory.getLogger(URLRules.class);

    private List<IfStatement> _ifStatements = new ArrayList<>();
    private Statement _defaultStatement;

    @Override
    protected void init(Element element) {
        List<Element> children = getChildElements(element);

        if (children.size() == 0) {
            throw new IllegalStateException("ruleConditions must contain one or more if statements.");
        }

        for (Element child : children) {

            LanguageElement object = null;
            try {
                object = model.createLanguageElement(child);
            } catch (Exception e) {
                logger.error("Failed to build rule \"{}\" id = {}, skipping it. {}", child.getTagName(), child.getAttribute("id"), e.getMessage());
            }

            if (object == null || !(object instanceof Statement)) {
                logger.error("ruleConditions may contain if and default section. InvalidTag tag: {}", element.getTagName());
                continue;
            }

            if (object instanceof IfStatement) {
                _ifStatements.add((IfStatement) object);
            } else if (object instanceof UrlRuleDefaultStatement) {
                if (_defaultStatement != null) {
                    throw new IllegalStateException("ruleCondition may contain only one default statement.");
                }
                _defaultStatement = (Statement) object;
            } else {
                logger.error("ruleConditions must containts if conditions or default section");
            }
        }
        Collections.sort(_ifStatements); // IfStatement implements Comparable and can be sorted by priority
    }

    public List<IfStatement> getIfStatements() {
        return _ifStatements;
    }

    public void setIfStatements(List<IfStatement> ifStatements) {
        _ifStatements = ifStatements;
    }

    public Statement getDefaultStatement() {
        return _defaultStatement;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {

        return "";
    }
}
