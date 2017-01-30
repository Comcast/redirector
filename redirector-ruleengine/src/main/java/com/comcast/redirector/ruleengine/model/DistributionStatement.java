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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This statement distributes values depending on specified percent. Statement example:
 * <distribution>
 *   <rule>
 *     <percent>20</percent>
 *     <server>...</server>
 *   </rule>
 *   <rule>
 *     <percent>30</percent>
 *     <server>...</server>
 *   </rule>
 *   <server>...</server>
 * </distribution>
 *
 * Percent for last return is implicitly calculated.
 *
 * Note regarding float point calculation errors (which are used here for percents). Generally it's not correct
 * to use ==, <=, >= operators to compare float point values. But in our case that will work. Because percent value will
 * not contain too much numbers and we will only add values.
 */
public class DistributionStatement extends Statement {

    public static final String TAG_RULE = "rule";
    public static final String TAG_PERCENT = "percent";
    public static final String ATTRIBUTE_MAC = "mac";
    public static final String ATTRIBUTE_ACCOUNT_ID= "serviceAccountId";

    private Distribution distribution = new Distribution();
    private static Logger log = LoggerFactory.getLogger(DistributionStatement.class);

    @Override
    protected void init(Element element) {
        List<Element> distributionChildren = getChildElements(element);
        // every child element except the last
        for (int i = 0; i < distributionChildren.size() - 1; i++) {
            // validate tag name
            Element distributionChild = distributionChildren.get(i);
            if (!distributionChild.getTagName().equals(TAG_RULE)) {
                throw new IllegalStateException("distribution children elements except the last must be rule. " +
                        "found: " + distributionChild.getTagName());
            }
            // validate number of children
            List<Element> groupChildren = getChildElements(distributionChild);
            if (groupChildren.size() < 2) {
                throw new IllegalStateException("distribution group must contain elements: id, percent and server. " +
                        "found " + groupChildren .size() + " elements");
            }
            // validate and extract values
            double percent = validateAndExtractPercentValue(groupChildren.size() == 2 ? groupChildren.get(0) : groupChildren.get(1));
            ServerReturnStatement returnStatement = validateAndExtractReturnStatement(groupChildren.size() == 2 ? groupChildren.get(1) : groupChildren.get(2));
            // add new group
            distribution.addFirstOrIntermediateGroup(percent, returnStatement);
        }

        // the last child element
        Element distributionChild = distributionChildren.get(distributionChildren.size() - 1);
        // validate tag name
        if (!distributionChild.getTagName().equals(Model.TAG_SERVER)) {
            throw new IllegalStateException("distribution last child element must be server. found: " + distributionChild.getTagName());
        }
        ServerReturnStatement returnStatement = new ServerReturnStatement((Server) model.createLanguageElement(distributionChild));
        distribution.addLastGroup(returnStatement);
    }

    public Distribution getDistribution() {
        return distribution;
    }

    @Override
    public Object execute(Map<String, String> params) {
        return distribution.get(params).execute(params);
    }

    public Object executeDefaultStatement(Map<String, String> params) {
        return distribution.lastGroup.getReturnStatement().execute(params);
    }

    @Override
    public boolean returnFulfilled() {
        return true;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        return distribution.toString(indent, params);
    }

    private double validateAndExtractPercentValue(Element element) {
        double result;
        if (!element.getTagName().equals(TAG_PERCENT)) {
            throw new IllegalStateException("distribution group first child must be a percent expression. found: " + element.getTagName());
        }
        String strPercentValue = element.getTextContent().trim();
        try {
            result = Double.parseDouble(strPercentValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("distribution percent must be a number. found: " + strPercentValue);
        }
        if (result <= 0.0 || result >= 100.0) {
            throw new IllegalArgumentException("distribution percent must be greater than 0 and lesser than 100. found: " + element);
        }
        return result;
    }

    private ServerReturnStatement validateAndExtractReturnStatement(Element element) {
        if (!element.getTagName().equals(Model.TAG_SERVER)) {
            throw new IllegalStateException("distribution group second child must be a return statement. found: " + element.getTagName());
        }
        return new ServerReturnStatement((Server) model.createLanguageElement(element));
    }

    /**
     * Contains a list of percent + ServerReturnStatement pairs, returns ServerReturnStatement distributed with desired percentage.
     * How to use:
     * 1) Create
     * 2) Call {@link #addFirstOrIntermediateGroup(double, ServerReturnStatement)} to add every group except the last.
     *    May be called zero, once or multiple times.
     * 3) Call {@link #addLastGroup(ServerReturnStatement)} once to add the last group. After that you should not add any other groups.
     *    Method throws IllegalStateException if overall percent value is greater than 99.99.
     * 4) Call {@link #get(Map)} each time you need a {@link ServerReturnStatement}. They will be returned with distribution they was added.
     *
     * For {@link #addFirstOrIntermediateGroup(double, ServerReturnStatement)} percent must be lesser than 100 and greater than 0.
     * That is not validated in this class and delegated to other level.
     */
    public class Distribution {
        private List<Group> groups = new ArrayList<>();
        private Group lastGroup = null;
        private double lastUpperBound = 0.0; // for add*Group methods, not used for get

        public void addFirstOrIntermediateGroup(double percent, ServerReturnStatement returnStatement) {
            if (lastGroup != null) {
                throw new IllegalStateException("addLastGroup was called previously, illegal usage of class. see javadoc");
            }
            lastUpperBound += percent;
            returnStatement.setType(ReturnStatementType.DISTRIBUTION_RULE);
            groups.add(new Group(lastUpperBound, returnStatement));
        }

        public void addLastGroup(ServerReturnStatement returnStatement) throws IllegalStateException {
            if (lastGroup != null) {
                throw new IllegalStateException("addLastGroup was called previously, illegal usage of class. see javadoc");
            }
            if (lastUpperBound >= 100.0) {
                throw new IllegalStateException("before adding last element overall percents expected to be less than 100, actual value is " + lastUpperBound);
            }
            returnStatement.setType(ReturnStatementType.DEFAULT_SERVER);
            lastGroup = new Group(99.99, returnStatement);
        }

        public List<Group> getGroups() {
            return groups;
        }

        public ReturnStatement get(Map<String, String> params) {
            boolean hasValidAccountIdParam = params.containsKey(ATTRIBUTE_ACCOUNT_ID) && StringUtils.isNotBlank(params.get(ATTRIBUTE_ACCOUNT_ID));
            String calculationAttribute = hasValidAccountIdParam ? params.get(ATTRIBUTE_ACCOUNT_ID) : params.get(ATTRIBUTE_MAC);
            String calculationAttributeType = hasValidAccountIdParam ? "accountId" : "mac";
            if (calculationAttribute != null) {
                for (Group group : groups) {
                    if (fitsPercent(calculationAttribute, group.getUpperBound())) {
                        Server sg = (Server) group.getReturnStatement().value;
                        log.info("Distribution stickiness applied. " + calculationAttributeType + " = " + calculationAttribute + ", appliedDistribution = " + group.getUpperBound() + "%" + ", groupFlavor = " + sg.getPath());
                        return group.getReturnStatement();
                    }
                }
            }
            Server sg = (Server) lastGroup.getReturnStatement().value;
            log.info("Applied serverGroup = " + sg.getName() + ", " + calculationAttributeType + " = " + calculationAttribute + ", appliedPercentage = " + lastGroup.getUpperBound() + "%" + ", groupFlavor = " + sg.getPath());
            return lastGroup.getReturnStatement();
        }

        public String toString(int indent, Map<String, String> params) {
            StringBuilder sb = new StringBuilder();
            sb.append(doSpacing("distribution \n", indent));
            double prevUpperBound = 0;
            for (Group group : groups) {
                sb.append(doSpacing("percent " + (group.getUpperBound() - prevUpperBound) + "\n", indent + LanguageElement.printSpacing));
                sb.append(doSpacing("return", indent + LanguageElement.printSpacing));
                sb.append(group.getReturnStatement().toString(indent + LanguageElement.printSpacing, params));
                sb.append("\n");
                prevUpperBound = group.getUpperBound();
            }
            return sb.toString();
        }

        private boolean fitsPercent(String accountId, double percent) {
            HashCode hashcode = Hashing.sipHash24().hashString(accountId, Charsets.UTF_8);
            long percentHashLong = (long)(Long.MAX_VALUE / 10000 * (percent * 100));
            return (percentHashLong >= Math.abs(hashcode.asLong()));
        }
    }

    /**
     * Represents group (percent + server statement), but percent is replaced with lower and upper bounds.
     * Where upper bound explicitly exists while lower bound is an upper bound of previous group. Thus group can be properly
     * used only with other groups of the distribution. See {@link Distribution}
     *
     * server Statement must not be null and upperBound must be lesser than 100 and greater than 0. That is not validated
     * in this class and delegated to other level.
     */
    public static class Group {
        private double upperBound;
        private ServerReturnStatement returnStatement;

        public Group(double upperBound, ServerReturnStatement returnStatement) {
            this.upperBound = upperBound;
            this.returnStatement = returnStatement;
        }

        public double getUpperBound() {
            return upperBound;
        }

        public ReturnStatement getReturnStatement() {
            return returnStatement;
        }
    }
}
