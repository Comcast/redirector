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

import com.comcast.redirector.api.model.LessOrEqualExpression;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.common.util.CommonUtils;

@ModelValidationVisitor(forClass = LessOrEqualExpression.class)
public class LessThenOrEqualExpressionValidationVisitor extends RelationalExpressionValidationVisitor<LessOrEqualExpression> {
    @Override
    public void visit(LessOrEqualExpression item) {
        super.visit(item);
        getValidationState().validateDuplicates(CommonUtils.getOperatorName(getClass()), item.getParam(), ValidationState.ErrorType.DuplicatedLessOrEqualParam);
    }
}
