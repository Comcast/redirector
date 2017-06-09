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
 */
package com.comcast.redirector.ruleengine.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SelectServer extends LanguageElement {
    private final static Logger LOGGER = LoggerFactory.getLogger(SelectServer.class);

    private List<IfStatement> ifStatements = new ArrayList<>();
    private Statement distributionStatement; // DistributionStatement

    @Override
    protected void init(Element element) {
        List<Element> children = getChildElements(element);

        if (children.size() == 0) {
            throw new IllegalStateException("selectServer must contain one or more if statements and one return or " +
                    "distribution statement.");
        }

        for (Element child : children) {
            LanguageElement object = null;
            try {
                object = model.createLanguageElement(child);
            } catch (Exception e) {
                LOGGER.error("Failed to build rule id = {}, skipping it. {}", child.getAttribute("id"), e.getMessage());
            }

            if (object == null || !(object instanceof Statement)) {
                LOGGER.error("selectServer may only contain if, return or distribution statements. InvalidTag tag: {}",
                        element.getTagName());
                continue;
            }

            if (object instanceof IfStatement) {
                ifStatements.add((IfStatement) object);
            } else if (object instanceof DistributionStatement) {
                if (distributionStatement != null) {
                    throw new IllegalStateException("selectServer may only contain one distribution statement.");
                }
                distributionStatement = (Statement) object;
            }
        }
        Collections.sort(ifStatements); // IfStatement implements Comparable and can be sorted by priority
    }

    public List<IfStatement> getIfStatements() {
        return ifStatements;
    }

    public void setIfStatements(List<IfStatement> ifStatements) {
        this.ifStatements = ifStatements;
    }

    public Statement getDistributionStatement() {
        return distributionStatement;
    }

    public void setDistributionStatement(Statement distributionStatement) {
        this.distributionStatement = distributionStatement;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        return "";
    }
}
