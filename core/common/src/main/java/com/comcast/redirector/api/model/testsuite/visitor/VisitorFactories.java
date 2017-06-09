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
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.testsuite.Parameter;

import java.util.Collection;
import java.util.Collections;

public enum VisitorFactories {
    TEST_SUITE(
        new AbstractVisitorFactory(
            new NoOpVisitor(),
            new Class[] {TestSuiteVisitor.class},
            "com.comcast.redirector.api.model.testsuite.visitor"
        ){}),
    VALIDATION(
        new AbstractVisitorFactory(
            new NoOpVisitor(),
            new Class[] {ModelValidationVisitor.class},
            "com.comcast.redirector.api.model.validation.visitor"
        ){});

    private AbstractVisitorFactory factory;

    VisitorFactories(AbstractVisitorFactory factory) {
        this.factory = factory;
    }

    public AbstractVisitorFactory getFactory() {
        return factory;
    }

    private static class NoOpVisitor implements TestSuiteExpressionVisitor<IVisitable> {

        @Override
        public void visit(IVisitable item) {
            // no-op
        }

        @Override
        public void setNegated(boolean negated) {
        }

        @Override
        public Collection<Parameter> getParametersFromExpressions() {
            return Collections.emptySet();
        }

        @Override
        public TestSuiteVisitorState getTestSuiteVisitorState() {
            return null;
        }

        @Override
        public void setVisitorState(IVisitorState visitorState) {}
    }
}
