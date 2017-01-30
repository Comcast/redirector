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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.factory;

import com.comcast.redirector.api.model.*;

import java.util.Arrays;
import java.util.List;

public class ExpressionFactory {

    public static <T extends SingleParameterExpression> T newSingleParamExpression(final Class<T> expClass,
            final String paramName, final String paramValue) throws IllegalAccessException, InstantiationException {

        final T expression = expClass.newInstance();
        expression.setParam(paramName);
        expression.setValue(paramValue);

        return expression;
    }

    public static <T extends ContainsBase> T newMultiValueExpression(final Class<T> expClass, final String paramName,
                                                                     final List<Value> values) throws IllegalAccessException, InstantiationException {

        return newMultiValueExpression(expClass, paramName, values, "");
    }

    public static <T extends ContainsBase> T newMultiValueExpression(final Class<T> expClass, final String paramName,
                                                                     final List<Value> values, final String type) throws IllegalAccessException, InstantiationException {

        final T expression = expClass.newInstance();
        expression.setParam(paramName);
        expression.setType(type);
        if ("namespacedList".equals(type)) {
            expression.setNamespacedLists(values);
        }
        else {
            expression.setValues(values);
        }

        return expression;
    }

    public static <T extends TypedSingleParameterExpression> T newSingleTypedParamExpression(final Class<T> expClazz,
            final String paramName, final String paramValue, final Expressions.ValueType valueType)
            throws IllegalAccessException, InstantiationException {

        final T expression = expClazz.newInstance();
        expression.setParam(paramName);
        expression.setValue(paramValue);
        expression.setType(valueType.name());

        return expression;
    }

    public static OrExpression newOrExpression(final Expressions leftOperand, final Expressions rightOperand) {
        final OrExpression orExpression = new OrExpression();
        orExpression.setItems(Arrays.asList(leftOperand, rightOperand));

        return orExpression;
    }

    public static XORExpression newXorExpression(final Expressions leftOperand, final Expressions rightOperand) {
        final XORExpression xorExpression = new XORExpression();
        xorExpression.setItems(Arrays.asList(leftOperand, rightOperand));

        return xorExpression;
    }

}
