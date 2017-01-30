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


(function () {
    'use strict';

    angular.
        module('uxData.deciderRules').
        controller('DeciderRulesEditController', DeciderRulesEditController);

    DeciderRulesEditController.$inject = ['$state', '$stateParams', 'STATES_CONSTANTS',
        'deciderRulesRequestService', 'RULES_CONSTANTS', 'EXPRESSION_CONSTANTS',
        'rulesEditService', 'rulesBuilderService', 'RulesCommonValidationService', 'rulesValidationService', 'authService',
        'USER_PERMISSIONS', 'utilsService', 'SERVER_CONSTANTS', 'rulesAlertsService', 'deciderRulesBuilderService'];

    function DeciderRulesEditController($state, $stateParams, STATES_CONSTANTS,
         deciderRulesRequestService, rulesCONST, expCONST,
         rulesEditService, rulesBuilderService, rulesCommonVS, pathRulesVS, authService,
         USER_PERMISSIONS, utils, serverCONST, rulesAlertsService, deciderRulesBuilderService) {

        /* jshint validthis: true */
        var vm = this;

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;
        var ISEMPTY = expCONST().OPERATOR_TYPE.ISEMPTY;
        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var beforeEditRule = {};

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.existingRuleIDs = {};

        // note that exprValidationData should be synchronized with rawExpressions
        // if value added/removed from rawExpression this change should be reflected in exprValidationData
        vm.exprValidationData = []; // {left: {duplicates: {}, nameErrorMsg: '', valueErrorMsg: '', generalErrorMsg: ''}, right: {same as left}}
        vm.partnerErrorMsg = '';
        vm.rawExpressions = [];

        vm.partners = [];
        vm.selectedPartner = '';
        vm.ruleName = angular.isDefined($stateParams.ruleId) ? $stateParams.ruleId : '';
        vm.editMode = angular.isDefined($stateParams.ruleId); // there can be two states: adding new rule and editing rule
        vm.ruleNameError = '';
        vm.isSaving = false;

        vm.saveRule = saveRule;
        vm.addExpression = addExpression;
        vm.removeExpression = removeExpression;
        vm.updateExpressions = updateExpressions;
        vm.cancelEditRule = cancelEditRule;
        vm.onExprNameChanged = onExprNameChanged;
        vm.onExprOperatorChanged = onExprOperatorChanged;
        vm.onExprValueAdded = onExprValueAdded;
        vm.onExprValueRemoved = onExprValueRemoved;
        vm.onExprValueChanged = onExprValueChanged;
        vm.onExprValueTypeChanged = onExprValueTypeChanged;
        vm.validateRuleName = validateRuleName;
        vm.validatePartner = validatePartner;
        vm.hasError = hasError;

        initialize();

        function initialize() {

            loadAllRuleIds();

            deciderRulesRequestService.getPartners()
                .then(function (data) {
                    angular.forEach(data.partners.partner, function (partner, index) {
                        vm.partners.push(partner.id);
                    });
                    onEditMode();
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('decider partners', reason.message);
                    onEditMode();
                }
            );
        }

        function onEditMode() {
            if (vm.editMode) {
                deciderRulesRequestService.getRule(vm.ruleName)
                    .then(function (data) {
                        fillInTheFormWithRule(data.rule, vm.ruleName);
                    }, function (reason) {
                        rulesAlertsService.failedToLoadData('rule: ' + vm.ruleName, reason.message);
                    }
                );
            }
        }

        function loadAllRuleIds() {
            deciderRulesRequestService.getRuleIds()
                .then(function (data) {
                    angular.copy(data.ids, vm.existingRuleIDs);
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('rules data');
                }
            );
        }

        function fillInTheFormWithRule(rule, ruleId) {
            clearModel();

            vm.ruleName = ruleId;
            vm.editMode = true;

            // build expressions and server objects from json rule
            var expressionsAndPartner = rulesBuilderService.unmarshallJSONRule(rule, deciderRulesBuilderService);
            angular.copy(expressionsAndPartner.expressions, vm.rawExpressions);
            vm.selectedPartner = expressionsAndPartner.returnStatement;

            initValidationData();

            beforeEditRule.id = vm.ruleName;
            var returnObject = {partner: vm.selectedPartner, ruleName: vm.ruleName};
            beforeEditRule.jsonRule = rulesBuilderService.marshallRuleToJSON(vm.rawExpressions, returnObject, deciderRulesBuilderService);
        }

        function initValidationData() {
            // now for each expression and server validation data should be initialized
            angular.forEach(vm.rawExpressions, function (expr, index) {
                vm.exprValidationData[index] = {};
                vm.exprValidationData[index][LEFT] = {duplicates: {}, valueErrorMsg: []};
                if (!utils.isEmptyString(expr.type)) {
                    vm.exprValidationData[index][RIGHT] = {duplicates: {}, valueErrorMsg: []};
                }
            });

            vm.partnerErrorMsg = '';
        }

//=====================================Callbacks for expression and server changes====================================//
        function addExpression(expressionType) {
            var validationExprObj = {};
            var expr = rulesEditService.createExpression('', '', expCONST().OPERATOR_TYPE.EQUAL, [''], LEFT);
            validationExprObj[LEFT] = {duplicates: {}, valueErrorMsg: []};

            if (rulesEditService.isBinary(expressionType)) {
                var rightOperand = rulesEditService.createExpression('', '', expCONST().OPERATOR_TYPE.EQUAL, [''], RIGHT);
                expr = rulesEditService.createBinaryExpression(expr, rightOperand, expressionType);
                validationExprObj[RIGHT] = {duplicates: {}, valueErrorMsg: []};
            }
            vm.exprValidationData.push(validationExprObj);
            vm.rawExpressions.push(expr);
        }

        function removeExpression(index) {
            vm.rawExpressions.splice(index, 1);
            vm.exprValidationData.splice(index, 1);
            pathRulesVS.revalidateDuplicatesOnRemove(index, vm.rawExpressions, vm.exprValidationData);
        }

        function cancelEditRule() {
            clearModel();
            if (angular.isDefined($state.previousState) && $state.previousState.name === STATES_CONSTANTS().showDeciderRules) {
                $state.go($state.previousState.name, $state.previousParams);
            } else {
                $state.go(STATES_CONSTANTS().addDeciderRule);
            }
        }

        function clearModel() {
            vm.rawExpressions.splice(0, vm.rawExpressions.length);
            vm.ruleName = '';
            vm.ruleNameError = '';
            vm.editMode = false;
            vm.selectedPartner = '';
            vm.exprValidationData.splice(0, vm.exprValidationData.length);
            vm.partnerErrorMsg = '';
            vm.isSaving = false;
        }

//=====================================Callbacks for expression and server changes====================================//

        function onExprNameChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            pathRulesVS.validateDuplicatedRelationalExpressions(vm.rawExpressions, vm.exprValidationData, index, side);
            validateName(vm.rawExpressions[index][side], index, side);
        }

        function onExprValueChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            validateValue(newValues[changedValueIndex], vm.rawExpressions[index][side], index, side, changedValueIndex);
        }

        function onExprOperatorChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            vm.exprValidationData[index][side].valueErrorMsg = [''];
            validateName(vm.rawExpressions[index][side], index, side);
            pathRulesVS.validateDuplicatedRelationalExpressions(vm.rawExpressions, vm.exprValidationData, index, side);
        }

        function onExprValueTypeChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            validateValue(newValues[changedValueIndex], vm.rawExpressions[index][side], index, side, changedValueIndex);
        }

        function onExprValueAdded(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            vm.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.push('');
        }

        function onExprValueRemoved(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            vm.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.splice(changedValueIndex, 1);
        }

        function updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            vm.rawExpressions[index][side].expName = newName;
            vm.rawExpressions[index][side].expOperator = newOperator;
            vm.rawExpressions[index][side].negation = newNegationValue;
            vm.rawExpressions[index][side].expValues = newValues;
            vm.rawExpressions[index][side].expType = newValueType;
        }

//====================================================================================================================//

        function saveRule() {

            var isRuleValid = validateRule();

            if (isRuleValid) {
                var returnObject = {partner: vm.selectedPartner, ruleName: vm.ruleName};
                var jsonRule = rulesBuilderService.marshallRuleToJSON(vm.rawExpressions, returnObject, deciderRulesBuilderService);

                if (angular.equals(beforeEditRule.id, vm.ruleName) && angular.equals(beforeEditRule.jsonRule, jsonRule)) {
                    rulesAlertsService.hasNotChangedWarning('Decider Rule');
                }
                else {
                    deciderRulesRequestService.saveRule(jsonRule.if[0], vm.ruleName)
                        .then(function (data) {
                            rulesAlertsService.successfullySaved(' \'' + vm.ruleName + '\' rule.');
                            $state.go(STATES_CONSTANTS().showDeciderRules, {ruleName:vm.ruleName});
                        }, function (reason) {
                            rulesAlertsService.failedToSave('rule: \'' + vm.ruleName + '\'', reason.message);
                            vm.isSaving = false;
                        }
                    );
                }
            }
            else {
                rulesAlertsService.failedToSave('rule', 'Due to validation errors.');
            }
        }

        function validateRule() {
            var isRuleNameValid = validateRuleName();
            var areExpressionsValid = validateExpressions();
            var areServersValid = validatePartner();

            return isRuleNameValid && areExpressionsValid && areServersValid;
        }

        function validateRuleName() {
            var validationResult = rulesCommonVS.validateRuleNameBase(vm.ruleName, vm.existingRuleIDs, vm.editMode);

            if (validationResult.isValid === true) {
                vm.ruleNameError = '';
            } else {
                vm.ruleNameError = validationResult.errorMsg;
            }
            return validationResult.isValid;
        }

        function validateExpressions() {
            if (vm.rawExpressions.length === 0) {
                rulesAlertsService.failedToSave('decider rule', 'Rule must contain at least one expression');
                return false;
            }
            var areNamesValid = true;
            var areValuesValid = true;
            angular.forEach(vm.rawExpressions, function (expression, index) {
                angular.forEach(expression, function (operand, side) {
                    if (side === LEFT || side === RIGHT) {
                        // 1. validate names
                        if (!validateName(expression[side], index, side)) {
                            areNamesValid = false;
                        }

                        // 2. validate values
                        if (!validateValues(expression[side], index, side)) {
                            areValuesValid = false;
                        }
                    }
                });
            });

            return areNamesValid && areValuesValid;
        }

        function validatePartner() {
            var partnerIsValid = true;
            vm.partnerErrorMsg = '';
            if (!utils.isDefinedAndNotEmpty(vm.selectedPartner)) {
                vm.partnerErrorMsg = 'Partner is required';
                partnerIsValid = false;
            }
            return partnerIsValid;
        }

        function validateName(expression, expIndex, operandSide) {
            var validationResult = rulesCommonVS.validateName(expression.expName, expression.expOperator);

            if (validationResult.isValid === true) {
                vm.exprValidationData[expIndex][operandSide]['nameErrorMsg'] = '';
            } else {
                vm.exprValidationData[expIndex][operandSide]['nameErrorMsg'] = validationResult.errorMsg;
            }

            return validationResult.isValid;
        }

        function validateValue(value, expression, expIndex, operandSide, valueIndex) {
            var ignoreEmptyValue = angular.equals(expression.expOperator, ISEMPTY);
            var validationResult = rulesCommonVS.validateValue(value, ignoreEmptyValue, expression.expOperator, expression.expType, expression.type);

            if (validationResult.isValid === true) {
                vm.exprValidationData[expIndex][operandSide]['valueErrorMsg'][valueIndex] = '';
            }
            else {
                vm.exprValidationData[expIndex][operandSide]['valueErrorMsg'][valueIndex] = validationResult.errorMsg;
            }

            return validationResult.isValid;
        }

        function validateValues(expression, expIndex, operandSide) {
            var valuesAreValid = true;
            angular.forEach(expression.expValues, function (value, valueIndex) {
                if (!validateValue(value, expression, expIndex, operandSide, valueIndex)) {
                    valuesAreValid = false;
                }
            });
            return valuesAreValid;
        }

        function hasError(error) {
            return angular.isDefined(error) && !$.isEmptyObject(error);
        }
    }
})();

