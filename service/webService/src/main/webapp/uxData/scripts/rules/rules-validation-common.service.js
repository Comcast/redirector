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

    angular
        .module('uxData.rules')
        .factory('RulesCommonValidationService', RulesCommonValidationService);

    RulesCommonValidationService.$inject = ['utilsService', 'rulesEditService', 'EXPRESSION_CONSTANTS',
        'REGEXP_CONSTANTS', 'SERVER_CONSTANTS', 'RULES_CONSTANTS', 'rulesAlertsService'];

    function RulesCommonValidationService(utils, rulesEditService, expCONST, regexpCONST, serverCONST, rulesCONST, rulesAlertsService) {

        var service = {
            validateName: validateName,
            validateValue: validateValue,
            validateExprName: validateExprName,
            validateExprValue: validateExprValue,
            validateRuleName: validateRuleName,
            validateRuleNameBase: validateRuleNameBase,
            validateRule: validateRule,
            validatePercentExpressions: validatePercentExpressions,
            hasError: hasError
        };

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var ISEMPTY = expCONST().OPERATOR_TYPE.ISEMPTY;
        var RANDOM = expCONST().OPERATOR_TYPE.RANDOM;
        var PERCENT = expCONST().OPERATOR_TYPE.PERCENT;
        var IN_IP_RANGE = expCONST().OPERATOR_TYPE.IN_IP_RANGE;
        var NAMESPACED_LIST = expCONST().OPERATOR_TYPE.NAMESPACED_LIST;

        var IPV6 = expCONST().VALUE_TYPE.IPV6;
        var NUMERIC = expCONST().VALUE_TYPE.NUMERIC;
        var VERSION = expCONST().VALUE_TYPE.VERSION;

        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var ADVANCED = serverCONST().EXP_EDIT_MODE.ADVANCED;

        return service;


        function validateName(name, operator) {

            var result = {isValid: true, errorMsg: ''};

            if (utils.isEmptyString(name) && !angular.equals(operator, PERCENT) && !angular.equals(operator, RANDOM)) {

                result.errorMsg = 'Expression name is required';
                result.isValid = false;
            }

            return result;
        }

        function validateValue(value, ignoreEmptyValue, operator, valueType, expType) {

            var result = {isValid: true, errorMsg: ''};

            if (!value || !utils.isDefinedAndNotEmpty(value)) {
                if (ignoreEmptyValue === false) {
                    result.errorMsg = 'Expression value is required';
                    result.isValid = false;
                }
            }
            else if (rulesEditService.isRelationalOperator(operator)) {
                if (angular.equals(valueType, NUMERIC) &&
                    (!regexpCONST().numericPattern.test(value) || angular.equals(value, '-0') || angular.equals(value, '-0.0'))) {
                    result.errorMsg = 'value should contain only numbers';
                    result.isValid = false;

                } else if (angular.equals(valueType, VERSION) && !regexpCONST().versionPattern.test(value)) {
                    result.errorMsg = 'value should be a version / IPv4 address (alphabet/numbers/hyphens separated by dots)';
                    result.isValid = false;

                } else if (angular.equals(valueType, IPV6) && !(new v6.Address(value)).isValid()) {
                    result.errorMsg = 'value should be a valid IPv6 address';
                    result.isValid = false;
                }
            }
            else if (angular.equals(operator, PERCENT)) {
                if (isNaN(value)) {
                    result.errorMsg = 'Invalid percent format';
                    result.isValid = false;
                }
                else if (!isNaN(value) && (value <= 0 || value > 100)) {
                    result.errorMsg = 'Invalid range';
                    result.isValid = false;
                }
            }
            else if (angular.equals(operator, RANDOM)) {
                var decimalPercentRegexp = /^(?:100|\d{1,2})(?:\.\d{1,2})?$/;
                var valueValid = angular.isDefined(value) ? value.match(decimalPercentRegexp) : false;
                if (!valueValid) {
                    result.errorMsg = 'value should be from 0 to 100';
                    result.isValid = false;
                }
            }
            else if (angular.equals(operator, IN_IP_RANGE) && !angular.equals(valueType, NAMESPACED_LIST)) {
                var ipv6Valid = new v6.Address(value).isValid();
                var ipv4Valid = new v4.Address(value).isValid();

                if (!ipv4Valid && !ipv6Valid) {
                    result.errorMsg = 'value should be a valid IPv4/IPv6 or range address';
                    result.isValid = false;
                }
            }

            return result;
        }

        function validateRuleNameBase(ruleName, existingNames, editMode) {

            var result = {isValid: true, errorMsg: ''};

            if (!utils.isDefinedAndNotEmpty(ruleName) || !regexpCONST().alphaNumericalWithUnderscores.test(ruleName)) {
                result.errorMsg = 'Rule name is required and must contain only word characters, i.e. letters, numbers and _';
                result.isValid = false;
            }
            else if (editMode === false && angular.isDefined(existingNames[ruleName])) {
                result.errorMsg = 'Rule or Template \'' + ruleName + '\' already exists.';
                result.isValid = false;
            }

            return result;
        }

        function hasError(error) {
            return utils.isDefinedAndNotEmpty(error);
        }

//====================================================================================================================//

        function validateRule(vm, ruleSpecificValidationService, skipReturnValueValidation) {
            var isRuleNameValid = validateRuleName(vm);
            var areExpressionsValid = validateExpressions(vm);
            var areServersValid = skipReturnValueValidation ? true : ruleSpecificValidationService.validateReturnValue(vm);

            return isRuleNameValid && areExpressionsValid && areServersValid;
        }

        function validateRuleName(vm) {
            var validationResult = validateRuleNameBase(vm.uiModelHolder.ruleName, angular.extend(vm.existingRuleIDs, vm.existingTemplatesIDs), vm.uiModelHolder.editMode);

            if (validationResult.isValid === true) {
                vm.ruleNameError = '';
            } else {
                vm.ruleNameError = validationResult.errorMsg;
            }
            return validationResult.isValid;
        }

        function validateExpressions(vm) {

            if (vm.uiModelHolder.rawExpressions.length === 0) {
                rulesAlertsService.failedToSave('rule', 'Rule must contain at least one expression');
                return false;
            }

            var areNamesValid = true;
            var areValuesValid = true;
            var arePercentExpsValid = true;
            var percentExprCount = 0;
            angular.forEach(vm.uiModelHolder.rawExpressions, function (expression, index) {
                angular.forEach(expression, function (operand, side) {
                    if (side === LEFT || side === RIGHT) {
                        // 1. validate names
                        if (!validateExprName(expression[side], index, side, vm)) {
                            areNamesValid = false;
                        }

                        // 2. validate values
                        if (!validateExprValues(expression[side], index, side, vm)) {
                            areValuesValid = false;
                        }

                        // 3. validate that there are no more than one percent operator in rule and
                        //    OR/XOR do not contain percent operator
                        percentExprCount = operand.expOperator === 'percent' ? (++percentExprCount) : percentExprCount;
                        if (!validatePercentExpression(percentExprCount, index, operand.expOperator, side, vm)) {
                            arePercentExpsValid = false;
                        }
                    }
                });
            });

            return areNamesValid && areValuesValid && arePercentExpsValid;
        }

        // method validates that there are no more than one percent operator in rule and
        // OR/XOR do not contain percent operator
        function validatePercentExpressions(vm) {
            var percentExprCount = 0;
            angular.forEach(vm.uiModelHolder.rawExpressions, function (operands, index) {
                angular.forEach(operands, function (value, side) {
                    if (side === LEFT || side === RIGHT) {
                        percentExprCount = value.expOperator === 'percent' ? (++percentExprCount) : percentExprCount;
                        validatePercentExpression(percentExprCount, index, value.expOperator, side, vm);
                    }
                });
            });
        }

        function validatePercentExpression(currentPercentExprsCount, index, operator, side, vm) {

            var isValid = true;

            var currentGeneralErrorMsg = vm.exprValidationData[index][side]['generalErrorMsg'];
            if (currentGeneralErrorMsg && utils.stringContains(currentGeneralErrorMsg, 'percent')) {
                vm.exprValidationData[index][side]['generalErrorMsg'] = '';
            }

            if (operator === 'percent') {
                if (vm.uiModelHolder.rawExpressions[index].type === 'OR' || vm.uiModelHolder.rawExpressions[index].type === 'XOR') {
                    vm.exprValidationData[index][side]['generalErrorMsg'] =
                        'Binary ' + vm.uiModelHolder.rawExpressions[index].type + ' expression should not contain expression with \'percent\' operand';
                    isValid = false;
                }
                else if (currentPercentExprsCount > 1) {
                    vm.exprValidationData[index][side]['generalErrorMsg'] = 'Rule already contains percent expression';
                    isValid = false;
                }
            }
            return isValid;
        }

        function validateExprName(expression, expIndex, operandSide, vm) {
            var validationResult = validateName(expression.expName, expression.expOperator);

            if (validationResult.isValid === true) {
                vm.exprValidationData[expIndex][operandSide]['nameErrorMsg'] = '';
            }else {
                vm.exprValidationData[expIndex][operandSide]['nameErrorMsg'] = validationResult.errorMsg;
            }

            return validationResult.isValid;
        }

        function validateExprValue(value, expression, expIndex, operandSide, valueIndex, vm) {
            var ignoreEmptyValue = angular.equals(expression.expOperator, ISEMPTY);
            var validationResult = validateValue(value, ignoreEmptyValue, expression.expOperator, expression.expType, expression.type);

            if (validationResult.isValid === true) {
                vm.exprValidationData[expIndex][operandSide]['valueErrorMsg'][valueIndex] = '';
            }
            else {
                vm.exprValidationData[expIndex][operandSide]['valueErrorMsg'][valueIndex] = validationResult.errorMsg;
            }

            return validationResult.isValid;
        }

        function validateExprValues(expression, expIndex, operandSide, vm) {
            var valuesAreValid = true;
            angular.forEach(expression.expValues, function (value, valueIndex) {
                if (!validateExprValue(value, expression, expIndex, operandSide, valueIndex, vm)) {
                    valuesAreValid = false;
                }
            });
            return valuesAreValid;
        }
    }
})();
