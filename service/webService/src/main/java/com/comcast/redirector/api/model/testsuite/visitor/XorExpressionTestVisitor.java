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

import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.XORExpression;
import com.comcast.redirector.api.model.testsuite.Parameter;

import java.util.Iterator;
import java.util.List;

@TestSuiteVisitor(forClass = XORExpression.class)
public class XorExpressionTestVisitor extends ExpressionWithChildrenTestVisitor<XORExpression> {
    @Override
    public void visit(XORExpression item) {
        super.visit(item);

        Iterator<Parameter> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = parameters.iterator().next();
            List<Value> firstItemValues = parameter.getValues();
            if (firstItemValues.size() > 0) {
                if (firstItemValues.get(0) != null && ! firstItemValues.get(0).getValue().isEmpty()) {
                    parameter.getValues().set(0, new Value(""));
                } else {
                    parameter.getValues().set(0, new Value("non-empty"));
                }
                break;
            }
        }
    }
}
