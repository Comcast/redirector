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

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.ExpressionVisitor;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

@ModelValidationVisitor(forClass = IfExpression.class)
public class IfExpressionValidationVisitor extends BaseExpressionValidationVisitor<IfExpression> {

    @Override
    public void visit(IfExpression expression) {

        ValidationState state = getValidationState();

        // 1. validate rule name
        // only first IfExpression contains rule name.
        if (!state.hasParam("isRuleNameValidated")) {
            validateRuleName(expression, state);
            state.setParam("isRuleNameValidated", true);
            state.setParam("ruleName", expression.getId());
            // it makes sense to perform this validation only once and not for each nested IfExpression
            validateTemplateDeletion(expression);
        }

        // 2. Validate if template do not depend on another template
        if ((state.getEntityType() == EntityType.TEMPLATE_RULE ||
                state.getEntityType() == EntityType.TEMPLATE_URL_RULE) &&
                StringUtils.isNotBlank(expression.getTemplateDependencyName())) {
            state.pushError(ValidationState.ErrorType.TemplateDependsOnTemplate);
        }

        boolean lastIfExpression = true; // only last IfExpression of a rule contains return server.
                                         // if current IfExpression has another (nested) IfExpression there is no sense
                                         // to try to validate current return statement (it's null).
                                         // It's somewhere deeper in nested IfExpression...

        // 3. validate items
        if (CollectionUtils.isEmpty(expression.getItems())) {
            state.pushError(ValidationState.ErrorType.EmptyExpressions);
        }
        else {
            try {
                for (Expressions exp : expression.getItems()) {
                    if (exp instanceof IVisitable) {
                        IVisitable visitable = (IVisitable)exp;
                        ExpressionVisitor<IVisitable> visitor = VisitorFactories.VALIDATION.getFactory().get(visitable.getClass(), state);
                        visitable.accept(visitor);
                    }
                    if (exp instanceof IfExpression) {
                        lastIfExpression = false;
                    }
                }
            } catch (ClassCastException e) {
                state.pushError(ValidationState.ErrorType.ExpressionCouldNotBeDeserialized);
                return;
            }
        }

        // 4. validate server/serverGroup
        if (state.getActionType() != ValidationState.ActionType.DELETE) {
            if (lastIfExpression && state.getEntityType() != EntityType.TEMPLATE_RULE && (CollectionUtils.isEmpty(expression.getReturn()))) {
                state.pushError(ValidationState.ErrorType.ServerIsMissed);
            }
            if (state.getEntityType() == EntityType.TEMPLATE_RULE && expression.getReturn() == null) {
                state.pushError(ValidationState.ErrorType.ServerIsMissed);
            }
            else if (lastIfExpression && expression.getReturn() != null) {
                for (Expressions exp : expression.getReturn()) {
                    if (exp instanceof IVisitable) {
                        IVisitable visitable = (IVisitable)exp;
                        ExpressionVisitor<IVisitable> visitor = VisitorFactories.VALIDATION.getFactory().get(visitable.getClass(), state);
                        visitable.accept(visitor);
                    }
                }
            }
        }
    }

    private void validateRuleName(IfExpression expression, ValidationState state) {
        if (StringUtils.isBlank(expression.getId())) {
            state.pushError(ValidationState.ErrorType.MissedRuleName);
        }

        if (!expression.getId().matches("([a-zA-Z0-9_])+") || expression.getId().equals("undefined")) {
            state.pushError(ValidationState.ErrorType.InvalidRuleName);
        }
    }

    // method checks that template we are going to remove is not used
    private void validateTemplateDeletion(IfExpression templateToDelete) {
        if (getValidationState().getActionType() == ValidationState.ActionType.DELETE &&
                (getValidationState().getEntityType() == EntityType.TEMPLATE_RULE || getValidationState().getEntityType() == EntityType.TEMPLATE_URL_RULE)) {
            StringBuilder errorMsg;
            if (EntityType.TEMPLATE_RULE == getValidationState().getEntityType()) {
                errorMsg = new StringBuilder("Template is used in rule(s): ");
            } else {
                errorMsg = new StringBuilder("Template is used in url rule(s): ");
            }
            boolean hasError = false;
            Collection<IfExpression> rules = getValidationState().getRules();
            for (IfExpression rule : rules) {
                if (rule.getTemplateDependencyName() != null && rule.getTemplateDependencyName().equals(templateToDelete.getId())) {
                    errorMsg.append(rule.getId()).append(", ");
                    hasError = true;
                }
            }
            if (hasError) {
                errorMsg.deleteCharAt(errorMsg.lastIndexOf(","));
                getValidationState().pushError(ValidationState.ErrorType.TemplateIsUsed, errorMsg.toString());
            }
        }
    }

}
