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

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.TypedSingleParameterExpression;
import com.comcast.redirector.api.model.VisitableExpression;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.common.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;

public class RelationalExpressionValidationVisitor<T extends VisitableExpression & TypedSingleParameterExpression> extends BaseExpressionValidationVisitor<T> {
    @Override
    public void visit(T item) {
        if (StringUtils.isBlank(item.getParam())) {
            getValidationState().pushError(ValidationState.ErrorType.MissedParamName);
        }

        if (StringUtils.isBlank(item.getValue())) {
            getValidationState().pushError(ValidationState.ErrorType.MissedParamValue);
        }
        else {
            item.setValue(CommonUtils.formatRelationalValue(item.getValue(), Expressions.ValueType.valueOf(item.getType().toUpperCase())));
            getValidationState().validateValue(item.getValue(), Expressions.ValueType.valueOf(item.getType().toUpperCase()));
        }
    }
}
