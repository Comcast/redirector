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
import com.comcast.redirector.api.model.SingleParameterExpression;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;

import java.util.Collections;

public class SingleParameterExpressionTestVisitor<T extends SingleParameterExpression & IVisitable> extends BaseTestSuiteExpressionVisitor<T> {
    private IValueCalculationStrategy valueCalculationStrategy;

    public SingleParameterExpressionTestVisitor(IValueCalculationStrategy valueCalculationStrategy) {
        this.valueCalculationStrategy = valueCalculationStrategy;
    }

    @Override
    public void visit(T item) {
        Parameter parameter = new Parameter();
        parameter.setName(item.getParam());
        parameter.setValues(
            Collections.singletonList(
                new Value(
                    valueCalculationStrategy.getValueFrom(item))));

        addParameter(parameter);
    }
}
