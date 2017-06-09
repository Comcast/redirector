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

import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.appDecider.Partner;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.dataaccess.EntityType;

import java.util.Collection;
import java.util.Set;

/**
 * Facade for model validation subsystem
 */
public class ModelValidationFacade {

    public static void validateFlavorRule(IfExpression rule, OperationContextHolder.OperationContext operationContext, EntityType entityType) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(
            operationContext.getActiveStacksAndFlavors(),
            operationContext.getPendingChangesStatus(),
            entityType);

        rule.accept(VisitorFactories.VALIDATION.getFactory().get(rule.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateUrlRule(IfExpression rule, PendingChangesStatus pendingChangesStatus, EntityType entityType) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(pendingChangesStatus, entityType);
        rule.accept(VisitorFactories.VALIDATION.getFactory().get(rule.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateUrlParams(UrlRule urlParams, EntityType entityType) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(entityType);
        urlParams.accept(VisitorFactories.VALIDATION.getFactory().get(urlParams.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateRuleTemplateDeletion(IfExpression template, Collection<IfExpression> expressions, EntityType entityType, ValidationState.ActionType actionType) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(expressions, entityType, actionType);
        template.accept(VisitorFactories.VALIDATION.getFactory().get(template.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateDistribution(Distribution distribution, OperationContextHolder.OperationContext operationContext) throws ExpressionValidationException{
        ValidationState validationState =
            new ValidationState(
                operationContext.getActiveStacksAndFlavors(),
                operationContext.getDistribution(),
                operationContext.getPendingChangesStatus(),
                operationContext.getServer());

        distribution.accept(
            VisitorFactories.VALIDATION.getFactory().get(distribution.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateWhitelistedStacks(Whitelisted newWhitelist, OperationContextHolder.OperationContext operationContext) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(operationContext.getWhitelist());
        validationState.setDefaultServer(operationContext.getServer());
        validationState.setServicePaths(operationContext.getServicePaths());
        validationState.setActivePaths(operationContext.getActiveStacksAndFlavors());
        validationState.setServiceName(operationContext.getServiceName());

        newWhitelist.accept(VisitorFactories.VALIDATION.getFactory().get(newWhitelist.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateNamespacedList(NamespacedList namespacedList, Collection<IfExpression> rules) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(rules);
        namespacedList.accept(VisitorFactories.VALIDATION.getFactory().get(namespacedList.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateNamespacedList(NamespacedList namespacedList) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState();
        namespacedList.accept(VisitorFactories.VALIDATION.getFactory().get(namespacedList.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validateServer(Server server, OperationContextHolder.OperationContext operationContext) throws ExpressionValidationException {

        ValidationState validationState = new ValidationState(
                operationContext.getActiveStacksAndFlavors(),
                operationContext.getPendingChangesStatus(),
                EntityType.SERVER,
                operationContext.getDistribution());
        server.accept(VisitorFactories.VALIDATION.getFactory().get(server.getClass(), validationState));
        validationState.isExpressionValid();
    }

    public static void validatePartner(Partner partner, Collection<IfExpression> rules) throws ExpressionValidationException {
        ValidationState validationState = new ValidationState(rules);
        partner.accept(VisitorFactories.VALIDATION.getFactory().get(partner.getClass(), validationState));
        validationState.isExpressionValid();
    }
}
