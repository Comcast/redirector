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

package com.comcast.redirector.api.model.validation.visitor.decider;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.appDecider.Partner;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.testsuite.visitor.TestSuiteVisitor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

@TestSuiteVisitor(forClass = Partner.class)
public class PartnerValidationVisitor extends BaseExpressionValidationVisitor<Partner> {
    @Override
    public void visit(Partner item) {
        if (StringUtils.isBlank(item.getId())) {
            getValidationState().pushError(ValidationState.ErrorType.PartnerIdMissing);
        }

        if (item.getProperties().size() < 1) {
            getValidationState().pushError(ValidationState.ErrorType.PartnerPropertiesMissing);
        }

        // validate that we are not deleting partner which is used in some of the rules
        Collection<IfExpression> rules = getValidationState().getRules();
        if (CollectionUtils.isNotEmpty(rules)) {
            StringBuilder errorMsg = new StringBuilder("Partner is used in ");
            boolean hasError = false;

            for (IfExpression rule : rules) {
                // only last IfExpression of a rule contains return.
                // if current IfExpression has another (nested) IfExpression there is no sense
                // to try to validate current return statement (it's null).
                // It's somewhere deeper in nested IfExpression...
                IfExpression lastIfExpression = getLastIfExpressionFromRule(rule);
                for (Expressions partnerIdValue : lastIfExpression.getReturn()) {
                    if (item.getId().equals(((Value) partnerIdValue).getValue())) {
                        errorMsg.append(rule.getId()).append(", ");
                        hasError = true;
                        break;
                    }
                }
            }

            if (hasError) {
                errorMsg.deleteCharAt(errorMsg.lastIndexOf(","));
                errorMsg.append("rule(s).");
                getValidationState().pushError(ValidationState.ErrorType.PartnerIsInUse, errorMsg.toString());
            }
        }
    }

    private IfExpression getLastIfExpressionFromRule(IfExpression rule) {
        Expressions lastIfExpression = rule;
        for (Expressions exp : rule.getItems()) {
            if (exp instanceof IfExpression) {
                lastIfExpression = exp;
            }
        }
        return (IfExpression) lastIfExpression;
    }
}
