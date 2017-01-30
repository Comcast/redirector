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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.IVisitorState;
import com.comcast.redirector.api.model.NegationSupport;
import com.comcast.redirector.api.model.testsuite.Parameter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class TestSuiteExpressionVisitors {
    public static Collection<Parameter> expressionToParameters(Expressions expression) {
        return expressionToParameters(expression, null);
    }

    public static Collection<Parameter> expressionToParameters(Expressions expression, Function<String, Set<String>> namespacedListValuesByName) {
        if (expression instanceof IVisitable) {
            IVisitable visitable = (IVisitable) expression;
            IVisitorState visitorState = new TestSuiteVisitorState(namespacedListValuesByName);
            TestSuiteExpressionVisitor<IVisitable> visitor =
                (TestSuiteExpressionVisitor<IVisitable>) VisitorFactories.TEST_SUITE.getFactory().get(visitable.getClass(), visitorState);
            if (expression instanceof NegationSupport) {
                visitor.setNegated(((NegationSupport)expression).isNegated());
            }
            visitable.accept(visitor);

            return visitor.getParametersFromExpressions();
        } else {
            return Collections.emptySet();
        }
    }

}
