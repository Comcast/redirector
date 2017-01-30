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

import com.comcast.redirector.api.model.ContainsBase;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ContainsBaseExpressionValidationVisitor<T extends ContainsBase> extends BaseExpressionValidationVisitor<T> {

    @Override
    public void visit(ContainsBase item) {

        if (StringUtils.isBlank(item.getParam())) {
            getValidationState().pushError(ValidationState.ErrorType.MissedParamName);
        }

        if ((item.getValues() == null && item.getNamespacedLists() == null) ||
                (item.getValues() != null && item.getValues().isEmpty()) ||
                (item.getValues() != null && areValuesEmpty(item.getValues())) ||
                (item.getNamespacedLists() != null && item.getNamespacedLists().isEmpty()) ||
                (item.getNamespacedLists() != null && areNamespacesListValuesEmpty(item.getNamespacedLists()))) {

            getValidationState().pushError(ValidationState.ErrorType.MissedParamValue);
        }
    }

    protected boolean areValuesEmpty(List<Value> values) {

        for (Value value : values) {
            if (StringUtils.isBlank(value.getValue())) {
                return true;
            }
        }

        return false;
    }

    protected boolean areNamespacesListValuesEmpty(List<Value> namespacedLists) {

        for (Value value : namespacedLists) {
            if (StringUtils.isBlank(value.getValue())) {
                return true;
            }
        }

        return false;
    }
}
