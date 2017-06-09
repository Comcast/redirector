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

import com.comcast.redirector.api.model.Random;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;

@ModelValidationVisitor(forClass = Random.class)
public class RandomExpressionValidationVisitor extends BaseExpressionValidationVisitor<Random> {
    @Override
    public void visit(Random item) {

        int intValue = 0;
        try {
            intValue = Integer.parseInt(item.getValue());
        } catch (NumberFormatException ex) {
            getValidationState().pushError(ValidationState.ErrorType.RandomRangeError);
            return;
        }

        if (intValue <= 0 || intValue > 100) {
            getValidationState().pushError(ValidationState.ErrorType.RandomRangeError);
        }
    }
}
