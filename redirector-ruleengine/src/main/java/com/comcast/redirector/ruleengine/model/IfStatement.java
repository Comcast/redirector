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
package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.model.expressions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// can be compared by priority
public class IfStatement extends Statement implements Comparable<IfStatement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);
    private static final Map<Class<? extends BooleanExpression>, Integer> PRIORITY_MAP = new HashMap<>();
    private static final int LOWEST_PRIORITY = Integer.MAX_VALUE; // higher numerical value means lower priority
    private static final String ID = "id";

    static {
        // lower numerical value means higher priority

        // relational
        PRIORITY_MAP.put(EqualsExpression.class, 10);
        PRIORITY_MAP.put(NotEqualExpression.class, 10);
        PRIORITY_MAP.put(GreaterThanExpression.class, 10);
        PRIORITY_MAP.put(GreaterOrEqualExpression.class, 10);
        PRIORITY_MAP.put(LessThanExpression.class, 10);
        PRIORITY_MAP.put(LessOrEqualExpression.class, 10);
        PRIORITY_MAP.put(IsEmptyExpression.class, 10);

        // logical
        PRIORITY_MAP.put(AndExpression.class, 100);
        PRIORITY_MAP.put(OrExpression.class, 100);
        PRIORITY_MAP.put(XorExpression.class, 100);

        // other
        PRIORITY_MAP.put(InIpRangeExpression.class, 1000);
        PRIORITY_MAP.put(ContainsExpression.class, 5000);
        PRIORITY_MAP.put(MatchesExpression.class, 25000);
        PRIORITY_MAP.put(Random.class, 50000);
        PRIORITY_MAP.put(Percent.class, 50000);
    }

    private int priority;
    private BooleanExpression expression;
    private Statement statement1;
    private Statement statement2;
    private String id;

    @Override
    protected void init(Element element) {
        // check for correct number of elements
        List<Element> children = getChildElements(element);
        if (!(children.size() == 2 || children.size() == 3)) {
            throw new IllegalStateException("if element must have exactly one boolean expression and either one or " +
                    "two statements.");
        }

        // ensure that the first element is a boolean expression
        LanguageElement expression = model.createLanguageElement(children.get(0));
        if (!(expression instanceof BooleanExpression)) {
            throw new IllegalStateException("1st element of if must be a boolean expression; found: "
                    + children.get(0).getTagName());
        }

        if (element.hasAttribute(IfStatement.ID))
            id = element.getAttribute(IfStatement.ID);

        this.expression = (BooleanExpression) expression;
        priority = getExpressionPriority(this.expression);

        // ensure the second element is a statement
        LanguageElement statement1 = model.createLanguageElement(children.get(1));
        if (!(statement1 instanceof Statement)) {
            throw new IllegalStateException("2nd element of if must be a statement; found: "
                    + children.get(1).getTagName());
        }
        this.statement1 = (Statement) statement1;

        // ensure the third element if present is also a statement
        if (children.size() == 3) {
            LanguageElement statement2 = model.createLanguageElement(children.get(2));
            if (!(statement2 instanceof Statement)) {
                throw new IllegalStateException("2nd element of if must be a statement; found: "
                        + children.get(2).getTagName());
            }
            this.statement2 = (Statement) statement2;
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public Object execute(Map<String, String> params) {
        boolean result = expression.evaluateExpression(params);
        if (result) {
            return statement1.execute(params);
        } else {
            if (statement2 != null) {
                return statement2.execute(params);
            }
        }
        return null;
    }

    @Override
    public boolean returnFulfilled() {
        if (statement2 == null) {
            return false;
        }
        return (statement1.returnFulfilled() && statement2.returnFulfilled());
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        String name;
        if (expression instanceof Random) {
            name = "if [ Random ] \n";
        } else {
            name = "if" + " [ results in" + execute(params) + " ] \n";
        }
        String expression = this.expression.toString(indent + LanguageElement.printSpacing, params) + "\n";
        sb.append(doSpacing(name, indent));
        sb.append(doSpacing("{\n", indent));
        sb.append(expression);
        sb.append(doSpacing("}\n", indent));
        return sb.toString();
    }

    @Override
    public int compareTo(IfStatement that) {
        return getComplexExpressionsPriority(this) - getComplexExpressionsPriority(that);
    }

    private int getComplexExpressionsPriority(IfStatement ifStatement) {
        int priority = (ifStatement.expression instanceof LogicalExpression)
                ? getComplexPriorityFromLogicalExpression((LogicalExpression) ifStatement.expression)
                : getExpressionPriority(ifStatement.expression);

        boolean isLastIfStatementInRule = ifStatement.statement1 instanceof ReturnStatement;
        if (isLastIfStatementInRule) {
            return priority;
        } else {
            return safeAddPriority(priority, getComplexExpressionsPriority((IfStatement) ifStatement.statement1));
        }
    }

    private int getComplexPriorityFromLogicalExpression(LogicalExpression logicalExpression) {
        int leftSidePriority = getExpressionPriority(logicalExpression.getLeftSide());
        int rightSidePriority = getExpressionPriority(logicalExpression.getRightSide());
        return safeAddPriority(leftSidePriority, rightSidePriority);
    }

    private int safeAddPriority(int left, int right) {
        return (left > LOWEST_PRIORITY - right) ? LOWEST_PRIORITY : left + right; // left and right >= 0
    }

    private int getExpressionPriority(BooleanExpression expression) {
        Class<? extends BooleanExpression> exprClass = expression.getClass();
        Integer priority = PRIORITY_MAP.get(exprClass);
        if (priority != null) {
            return priority;
        } else {
            LOGGER.error("Priority for " + exprClass.getSimpleName() + " not found. Set the lowest priority");
            return LOWEST_PRIORITY; // higher numerical value means lower priority
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfStatement that = (IfStatement) o;
        return Objects.equals(priority, that.priority) &&
                Objects.equals(expression, that.expression) &&
                Objects.equals(statement1, that.statement1) &&
                Objects.equals(statement2, that.statement2) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, expression, statement1, statement2, id);
    }
}
