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

import com.comcast.redirector.api.model.Matches;
import com.comcast.redirector.api.model.Value;

@TestSuiteVisitor(forClass = Matches.class)
public class MatchesTestVisitor extends SingleParameterExpressionTestVisitor<Matches> {
    public MatchesTestVisitor() {
        super(ValueCalculationStrategies.MATCHES.getStrategy());
    }

    @Override
    public void visit(Matches item) {
        super.visit(item);

        if (isNegated()) {
            for (Value v : parameters.iterator().next().getValues()) {
                v.setValue("NOT" + v.getValue());
            }
        }
    }
}
