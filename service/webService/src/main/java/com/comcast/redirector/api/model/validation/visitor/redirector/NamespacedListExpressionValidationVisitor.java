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

import com.comcast.redirector.api.model.HasChildren;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.common.util.IpAddressValidator;
import com.comcast.redirector.common.util.NamespacedListUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

@ModelValidationVisitor(forClass = NamespacedList.class)
public class NamespacedListExpressionValidationVisitor extends BaseExpressionValidationVisitor<NamespacedList> {
    @Override
    public void visit(NamespacedList item) {
        if (StringUtils.isBlank(item.getName())) {
            getValidationState().pushError(ValidationState.ErrorType.NamespacedListNameIsMissed);
        }

        if (!item.getName().matches("([a-zA-Z0-9_])+") || item.getName().equals("undefined")) {
            getValidationState().pushError(ValidationState.ErrorType.NamespacedListNameIsInvalid);
        }

        Function <NamespacedListValueForWS, Boolean> validateValueFunction;

        if (item.getType() == null) {
            item.setType(NamespacedListType.TEXT);
        }

        switch (item.getType()) {
            case IP: {
                validateValueFunction = NamespacedListExpressionValidationVisitor::validateIp;
                break;
            }
            case ENCODED: {
                validateValueFunction = this::validateEncoded;
                break;
            }
            case TEXT:
            default: {
                item.setType(NamespacedListType.TEXT);
                validateValueFunction = this::validateText;
                break;
            }
        }

        for (NamespacedListValueForWS value : item.getValueSet()) {
            if (!validateValueFunction.apply(value)) {
                getValidationState().pushError(ValidationState.ErrorType.NamespacedListValueIsInvalid);
                break;
            }
        }

        // validate that we are not deleting namespace which is used in some of the rules
        Collection<IfExpression> rules = getValidationState().getRules();
        if (CollectionUtils.isNotEmpty(rules)) {

            StringBuilder errorMsg = new StringBuilder("Namespaced list is used in ");
            boolean hasError = false;

            for (IfExpression expr : rules) {

                if (NamespacedListUtils.getAllNamespaceLists(Arrays.asList((HasChildren) expr)).contains(item.getName())){
                    errorMsg.append((expr).getId()).append(", ");
                    hasError = true;
                }
            }

            if (hasError) {
                errorMsg.deleteCharAt(errorMsg.lastIndexOf(","));
                errorMsg.append("rule(s).");
                getValidationState().pushError(ValidationState.ErrorType.NamespacedlistIsInUse, errorMsg.toString());
            }
        }

    }

    //todo: is static due to IP migration. revisit after it is done.
    public static boolean validateIp(NamespacedListValueForWS value) {
        return IpAddressValidator.isValidIpString(value.getValue());
    }

    private boolean validateEncoded(NamespacedListValueForWS value) {
        return validateText(value);
    }

    private boolean validateText(NamespacedListValueForWS value) {
        if (StringUtils.isBlank(value.getValue())) {
            getValidationState().pushError(ValidationState.ErrorType.NamespacedListContainsEmptyValue);
            return false;
        }
        return true;
    }
}
