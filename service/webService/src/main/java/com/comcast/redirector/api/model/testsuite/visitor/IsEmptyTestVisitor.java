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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.api.model.IsEmpty;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.visitor.BaseTestSuiteExpressionVisitor;
import com.comcast.redirector.api.model.testsuite.visitor.TestSuiteVisitor;

import java.util.Collections;

@TestSuiteVisitor(forClass = IsEmpty.class)
public class IsEmptyTestVisitor extends BaseTestSuiteExpressionVisitor<IsEmpty> {

    @Override
    public void visit(IsEmpty item) {
        Parameter parameter = new Parameter();
        parameter.setName(item.getParam());
        Value value = isNegated() ? new Value("IsNotEmpty") : new Value();
        parameter.setValues(Collections.singletonList(value));
        addParameter(parameter);
    }
}
