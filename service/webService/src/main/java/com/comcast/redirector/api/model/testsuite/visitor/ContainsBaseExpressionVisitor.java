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

import com.comcast.redirector.api.model.ContainsBase;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.visitor.BaseTestSuiteExpressionVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class ContainsBaseExpressionVisitor<T extends ContainsBase> extends BaseTestSuiteExpressionVisitor<T> {
    @Override
    public void visit(T item) {
        List<Value> values = item.getValues();
        List<Value> namespacedLists = item.getNamespacedLists();

        Parameter parameter = new Parameter();
        parameter.setName(item.getParam());

        if (! isNegated()) {
            if (values != null && values.size() > 0) {
                parameter.getValues().addAll(obtainParameterValuesFromPlainValues(values));
            }

            if (namespacedLists != null && namespacedLists.size() > 0) {
                parameter.getValues().addAll(obtainParameterValuesFromNamespacedLists(namespacedLists));
            }
        } else {
            parameter.setValues(getValuesForNegation());
        }

        addParameter(parameter);
    }

    protected List<Value> obtainParameterValuesFromPlainValues(List<Value> values) {
        return Collections.singletonList(obtainParameterValue(values.get(0).getValue()));
    }

    protected List<Value> obtainParameterValuesFromNamespacedLists(List<Value> namespacedLists) {
        for (Value list : namespacedLists) {
            Set<String> listValues = getTestSuiteVisitorState().namespacedListValuesByName(list.getValue());
            if (listValues.size() != 0) {
                return Collections.singletonList(obtainParameterValue(listValues.iterator().next()));
            }
        }

        return Collections.emptyList();
    }

    protected Value obtainParameterValue(String input) {
        return new Value(input);
    }

    protected abstract List<Value> getValuesForNegation();
}
