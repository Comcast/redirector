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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.validation.visitor.redirector;

import com.comcast.redirector.api.model.ExpressionVisitor;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.OrExpression;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;

@ModelValidationVisitor(forClass = OrExpression.class)
public class OrExpressionValidationVisitor extends BaseExpressionValidationVisitor<OrExpression> {
    @Override
    public void visit(OrExpression item) {
        for (Expressions exp : item.getItems()) {
            if (exp instanceof IVisitable) {
                IVisitable visitable = (IVisitable)exp;
                ExpressionVisitor<IVisitable> visitor = VisitorFactories.VALIDATION.getFactory().get(visitable.getClass(), getValidationState());
                visitable.accept(visitor);
            }
        }
    }
}
