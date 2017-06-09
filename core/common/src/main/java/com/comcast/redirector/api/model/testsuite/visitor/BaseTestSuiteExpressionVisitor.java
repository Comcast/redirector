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

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.IVisitorState;
import com.comcast.redirector.api.model.testsuite.Parameter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseTestSuiteExpressionVisitor<T extends IVisitable> implements TestSuiteExpressionVisitor<T> {
    protected final Set<Parameter> parameters = new HashSet<>();
    private Boolean negated = null;
    private TestSuiteVisitorState visitorState = null;

    @Override
    public Collection<Parameter> getParametersFromExpressions() {
        return Collections.unmodifiableCollection(parameters);
    }

    protected final void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    protected final void addParameters(Collection<Parameter> parameters) {
        this.parameters.addAll(parameters);
    }

    public final boolean isNegated() {
        return negated;
    }

    public final void setNegated(boolean negated) {
        if (this.negated == null) {
            this.negated = negated;
        } else {
            throw new UnsupportedOperationException("can't set negated field more than once");
        }
    }

    @Override
    public void setVisitorState(IVisitorState visitorState) {
        if (visitorState instanceof TestSuiteVisitorState) {
            this.visitorState = (TestSuiteVisitorState) visitorState;
        } else {
            throw new IllegalArgumentException("Wrong type of visitor state: " + visitorState.getClass().getName());
        }
    }

    @Override
    public TestSuiteVisitorState getTestSuiteVisitorState() {
        return visitorState;
    }
}
