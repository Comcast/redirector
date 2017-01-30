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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.validation.visitor.redirector;

import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;

@ModelValidationVisitor(forClass = Rule.class)
public class RuleValidationVisitor extends BaseExpressionValidationVisitor<Rule> {
    @Override
    public void visit(Rule item) {
        Server server = item.getServer();
        if (server instanceof IVisitable) {
            IVisitable visitable = item.getServer();
            visitable.accept(VisitorFactories.VALIDATION.getFactory().get(visitable.getClass(), getValidationState()));
        }

        if (item.getPercent() < 0 || item.getPercent() > 100) {
            getValidationState().pushError(ValidationState.ErrorType.RuleDistributionPercentageInvalid);
        }
    }
}
