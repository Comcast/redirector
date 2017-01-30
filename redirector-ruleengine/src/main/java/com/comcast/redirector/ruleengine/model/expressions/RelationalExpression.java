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
import com.comcast.redirector.ruleengine.model.Parameter;
import com.google.common.primitives.UnsignedBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import sun.net.util.IPAddressUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RelationalExpression extends BooleanExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationalExpression.class);
    private static final String TYPE = "type";
    private static final String CASE_SENSITIVE = "caseSensitive";

    protected Value leftSide;
    protected Value rightSide;
    private CompareType compareType = CompareType.NONE;
    private boolean evaluateFromLeftToRight = true;
    private boolean caseSensitive = false;

    @Override
    protected void init(Element element) {
        super.init(element);

        try {
            String s = element.getAttribute(TYPE);
            if (s != null && !s.isEmpty()) {
                compareType = CompareType.valueOf(s.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("type attribute must be one of " +
                    Arrays.toString(CompareType.values()).toLowerCase() + " in " + element.getNodeName());
        }
        String s = element.getAttribute(CASE_SENSITIVE);
        if (s != null && !s.isEmpty()) {
            if (s.equalsIgnoreCase("true")) {
                caseSensitive = true;
            } else if (s.equalsIgnoreCase("false")) {
                caseSensitive = false;
            } else {
                throw new IllegalArgumentException("if specified, caseSensitive attribute must be true or false. " +
                        "found: " + s);
            }
        }
        List<Element> children = getChildElements(element);
        if (children.size() != 2) {
            throw new IllegalStateException("Element " + element.getTagName() + " must have 2 children");
        }
        LanguageElement left = model.createLanguageElement(children.get(0));
        if (!(left instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + " has a child that is not a value or " +
                    "param: " + children.get(0).getTagName());
        }
        LanguageElement right = model.createLanguageElement(children.get(1));
        if (!(right instanceof Value)) {
            throw new IllegalStateException("Element " + element.getTagName() + " has a child that is not a value or " +
                    "param: " + children.get(1).getTagName());
        }
        leftSide = (Value) left;
        rightSide = (Value) right;
        if (leftSide instanceof Parameter && rightSide instanceof com.comcast.redirector.ruleengine.model.Value) {
            /*
             * Since a Parameter's getNumericValue() requires parsing a String,
			 * if the right side is a Value, it has already been parsed and thus
			 * it's getNumericValue() returns quicker
			 */
            evaluateFromLeftToRight = false;
        }
    }

    @Override
    public boolean evaluate(Map<String, String> params) {
        if (compareType == CompareType.VERSION) {
            return evaluateVersion(params);
        } else if ((compareType == CompareType.NUMERIC || compareType == CompareType.NONE) && (evaluateFromLeftToRight ?
                (leftSide.isNumericValue(params) && rightSide.isNumericValue(params)) :
                (rightSide.isNumericValue(params) && leftSide.isNumericValue(params)))) {
            return evaluate(leftSide.getNumericValue(params), rightSide.getNumericValue(params));
        } else if (compareType == CompareType.IPV6) {
            return evaluateIPv6(params);
        }
        return evaluate(leftSide.getStringValue(params), rightSide.getStringValue(params), caseSensitive);
    }

    private boolean evaluate(String s1, String s2, boolean caseSensitive) {
        if (s1 == null || s2 == null) return false;
        if (!caseSensitive) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        return evaluate(s1, s2);
    }

    private boolean evaluateVersion(Map<String, String> params) {
        String left = leftSide.getStringValue(params);
        String right = rightSide.getStringValue(params);
        if (left == null || right == null) return false;
        String[] leftTokens = left.isEmpty() || IPAddressUtil.isIPv6LiteralAddress(left)
                ? new String[]{}
                : left.split("\\.");
        String[] rightTokens = right.isEmpty() || IPAddressUtil.isIPv6LiteralAddress(right)
                ? new String[]{}
                : right.split("\\.");
        int largest = Math.max(leftTokens.length, rightTokens.length);
        int[] leftNumbers = new int[largest];
        int[] rightNumbers = new int[largest];
        for (int i = 0; i < largest; i++) {
            leftNumbers[i] = i < leftTokens.length ? getIntValue(leftTokens[i]) : 0;
            rightNumbers[i] = i < rightTokens.length ? getIntValue(rightTokens[i]) : 0;
        }
        return evaluate(leftNumbers, rightNumbers);
    }

    private boolean evaluateIPv6(Map<String, String> params) {
        String left = leftSide.getStringValue(params);
        String right = rightSide.getStringValue(params);
        if (left == null || right == null) return false;
        return evaluate(
                IPAddressUtil.isIPv6LiteralAddress(left) ? IPAddressUtil.textToNumericFormatV6(left) : new byte[]{},
                IPAddressUtil.isIPv6LiteralAddress(right) ? IPAddressUtil.textToNumericFormatV6(right) : new byte[]{});
    }

    protected static int compareArrays(int[] i1, int[] i2) {
        for (int i = 0; i < i1.length; i++) {
            int result = i1[i] - i2[i];
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    protected static int compareArrays(byte[] b1, byte[] b2) {
        return UnsignedBytes.lexicographicalComparator().compare(b1, b2);
    }

    private int getIntValue(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            LOGGER.error("Encountered part of a version that was not an int: " + str, e);
            return 0;
        }
    }

    public Value getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(Value leftSide) {
        this.leftSide = leftSide;
    }

    public Value getRightSide() {
        return rightSide;
    }

    public void setRightSide(Value rightSide) {
        this.rightSide = rightSide;
    }

    public abstract boolean evaluate(double d1, double d2);

    public abstract boolean evaluate(String s1, String s2);

    public abstract boolean evaluate(int[] i1, int[] i2);

    public abstract boolean evaluate(byte[] b1, byte[] b2);

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

    private enum CompareType {
        NONE,
        STRING,
        NUMERIC,
        VERSION,
        IPV6
    }
}
