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

import com.comcast.redirector.ruleengine.model.expressions.*;
import com.comcast.redirector.ruleengine.model.expressions.Random;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.w3c.dom.Element;

import java.util.*;

public abstract class AbstractModel {
    public static final Map<String, Class<? extends LanguageElement>> TAG_MAP = new TreeMap<String, Class<? extends LanguageElement>>();

    public static final String TAG_IF = "if";
    public static final String TAG_RETURN = "return";
    public static final String TAG_DISTRIBUTION = "distribution";
    public static final String TAG_AND = "and";
    public static final String TAG_OR = "or";
    public static final String TAG_EXCLUSIVE_OR = "xor";
    public static final String TAG_EQUAL = "equals";
    public static final String TAG_NOT_EQUAL = "notEqual";
    public static final String TAG_GREATER_THAN = "greaterThan";
    public static final String TAG_LESS_THAN = "lessThan";
    public static final String TAG_GREATER_THAN_OR_EQUAL_TO = "greaterOrEqual";
    public static final String TAG_LESS_THAN_OR_EQUAL_TO = "lessOrEqual";
    public static final String TAG_MATCHES = "matches";
    public static final String TAG_VALUE = "value";
    public static final String TAG_PARAMETER = "param";
    public static final String TAG_VALUES = "values";
    public static final String TAG_NAMESPACED_LIST = "namespacedList";
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_CONTAINS = "contains";
    public static final String TAG_IN_IP_RANGE = "inIpRange";
    public static final String TAG_RANDOM = "random";
    public static final String TAG_SELECT_SERVER = "selectServer";
    public static final String TAG_SERVER = "server";
    public static final String TAG_SERVER_GROUP = "serverGroup";
    public static final String TAG_IS_EMPTY = "isEmpty";

    private NamespacedListRepository namespacedListHolder;

    static {
        TAG_MAP.put(TAG_IF, IfStatement.class);
        TAG_MAP.put(TAG_RETURN, ReturnStatement.class);
        TAG_MAP.put(TAG_DISTRIBUTION, DistributionStatement.class);
        TAG_MAP.put(TAG_EQUAL, EqualsExpression.class);
        TAG_MAP.put(TAG_NOT_EQUAL, NotEqualExpression.class);
        TAG_MAP.put(TAG_AND, AndExpression.class);
        TAG_MAP.put(TAG_OR, OrExpression.class);
        TAG_MAP.put(TAG_EXCLUSIVE_OR, XorExpression.class);
        TAG_MAP.put(TAG_GREATER_THAN, GreaterThanExpression.class);
        TAG_MAP.put(TAG_LESS_THAN, LessThanExpression.class);
        TAG_MAP.put(TAG_GREATER_THAN_OR_EQUAL_TO, GreaterOrEqualExpression.class);
        TAG_MAP.put(TAG_LESS_THAN_OR_EQUAL_TO, LessOrEqualExpression.class);
        TAG_MAP.put(TAG_MATCHES, MatchesExpression.class);
        TAG_MAP.put(TAG_VALUE, Value.class);
        TAG_MAP.put(TAG_PARAMETER, Parameter.class);
        TAG_MAP.put(TAG_VALUES, Values.class);
        TAG_MAP.put(TAG_NAMESPACED_LIST, NamespacedList.class);
        TAG_MAP.put(TAG_PATTERN, Pattern.class);
        TAG_MAP.put(TAG_CONTAINS, ContainsExpression.class);
        TAG_MAP.put(TAG_IN_IP_RANGE, InIpRangeExpression.class);
        TAG_MAP.put(TAG_RANDOM, Random.class);
        TAG_MAP.put(TAG_SELECT_SERVER, SelectServer.class);
        TAG_MAP.put(TAG_SERVER, Server.class);
        TAG_MAP.put(TAG_SERVER_GROUP, ServerGroup.class);
        TAG_MAP.put(TAG_IS_EMPTY, IsEmptyExpression.class);
    }

    public AbstractModel(NamespacedListRepository namespacedListHolder) {
        this.namespacedListHolder = namespacedListHolder;
    }

    public LanguageElement createLanguageElement(Element element) {
        LanguageElement result;
        Class<? extends LanguageElement> clazz = TAG_MAP.get(element.getTagName());
        if (clazz == null) {
            throw new IllegalStateException("Unknown Tag: " + element.getTagName());
        }
        try {
            result = clazz.newInstance();
            result.init(this, element);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public abstract Object execute(Map<String, String> params);

    public abstract Object executeDefault(Map<String, String> params);

    public abstract List<DistributionServer> getDistribution();

    public NamespacedListRepository getNamespacedListHolder() {
        return namespacedListHolder;
    }
}
