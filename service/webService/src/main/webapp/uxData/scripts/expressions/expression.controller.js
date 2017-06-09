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

(function () {
    'use strict';

    angular.
        module('uxData.expression').
        controller('expressionController', expressionController);

    expressionController.$inject = ['$scope', '$q',
        'expressionBuilderService', 'expressionService', 'RULES_CONSTANTS', 'EXPRESSION_CONSTANTS', 'utilsService', 'namespacedService', 'namespacedAlertsService'];

    function expressionController($scope, $q, expBuilder, expService, rulesCONST, expCONST, utils, namespacedService, namespacedAlertsService) {

        /* jshint validthis: true */
        var vm = this;

        vm.nameChanged = nameChanged;
        vm.operatorChanged = operatorChanged;
        vm.valueChanged = valueChanged;
        vm.valueTypeChanged = valueTypeChanged;
        vm.negationChanged = negationChanged;
        vm.addNewValue = addNewValue;
        vm.removeValue = removeValue;
        vm.onNamespaceChanged = onNamespaceChanged;
        vm.hasError = hasError;
        vm.isRelationalOperator = expService.isRelationalOperator;
        vm.isValueTypeNamespace = isValueTypeNamespace;
        vm.isNegationApplicable = isNegationApplicable;
        vm.validationData = $scope.validationData;

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;

        vm.namespaces = {};
        vm.namespaces[LEFT] = [];
        vm.namespaces[RIGHT] = [];

        vm.expr = {};

        vm.expCONST = expCONST();

        initExpressionType();

        vm.operators = expBuilder.buildOperators($scope.expressionType);
        vm.valueType = expBuilder.buildValueTypes();

        vm.expr[LEFT] = {};
        vm.expr[RIGHT] = {};
        vm.expr[LEFT].isMultivalueOperatorSelected = expService.isMultivalueOperatorSelected(LEFT, vm.expr, vm.operators);
        vm.expr[RIGHT].isMultivalueOperatorSelected = expService.isMultivalueOperatorSelected(RIGHT, vm.expr, vm.operators);
        vm.expr[LEFT].negation = false;
        vm.expr[RIGHT].negation = false;

        initExpressionsData();

        function initExpressionType() {
            vm.expr.isBinary = false;
            vm.expr.Type = '';
            if (utils.isDefinedAndNotEmpty($scope.data.type)){
                vm.expr.Type = $scope.data.type;
                vm.expr.isBinary = angular.equals($scope.data.type, rulesCONST().OR) || angular.equals($scope.data.type, rulesCONST().XOR);
            }
            vm.expr.disabled = $scope.data.disabled;
        }

        function initExpressionsData() {
            if (utils.isDefinedAndNotEmpty($scope.data)) {
                initOperand(LEFT);
                if (vm.expr.isBinary) {
                    initOperand(RIGHT);
                }
            }
        }

        function initOperand(side) {
            if (angular.isDefined($scope.data) && !$.isEmptyObject($scope.data)) {
                vm.expr[side].expName = $scope.data[side].expName;
                vm.expr[side].expValues = angular.copy($scope.data[side].expValues);
                vm.expr[side].isMultivalueOperatorSelected = expService.isMultivalueOperatorSelected(side, vm.expr, vm.operators);
                vm.expr[side].expType = $scope.data[side].expType;
                vm.expr[side].negation = angular.isDefined($scope.data[side].negation)? angular.copy($scope.data[side].negation) : false;

                if ($scope.data[side].expOperator === expCONST().OPERATOR_TYPE.CONTAINS &&
                    $scope.data[side].expType !== expCONST().OPERATOR_TYPE.NAMESPACED_LIST) {
                    // Simple CONTAINS is depricated now - change to CONTAINS_NMSP with empty values list on edit rule
                    vm.expr[side].expOperator = vm.operators[expCONST().OPERATOR_TYPE.CONTAINS_NMSP].value;
                    vm.expr[side].expValues = [''];
                } else if ($scope.data[side].expType !== expCONST().OPERATOR_TYPE.NAMESPACED_LIST) {
                    vm.expr[side].expOperator = $scope.data[side].expOperator;
                } else {
                    angular.forEach(vm.expr[side].expValues, function (value, index) {
                        vm.expr[side].expValues[index] = {name: value};
                    });
                    if (($scope.data[side].expOperator === expCONST().OPERATOR_TYPE.IN_IP_RANGE) || ($scope.data[side].expOperator === expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP)) {
                        vm.expr[side].expOperator = vm.operators[expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP].value;
                    } else {
                        vm.expr[side].expOperator = vm.operators[expCONST().OPERATOR_TYPE.CONTAINS_NMSP].value;
                    }
                    getNamespaces(side);
                }
                vm.expr[side].isMultivalueOperatorSelected = expService.isMultivalueOperatorSelected(side, vm.expr, vm.operators);

                if (expService.isRelationalOperator($scope.data[side])) {
                    vm.expr[side].valueType = $scope.data[side].expType;
                }
            }
            expressionChanged(side);
        }

        function isNegationApplicable(side) {
            var operator = vm.expr[side].expOperator;
            return angular.equals(operator, expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP) ||
                angular.equals(operator, expCONST().OPERATOR_TYPE.CONTAINS_NMSP) ||
                angular.equals(operator, expCONST().OPERATOR_TYPE.IN_IP_RANGE) ||
                angular.equals(operator, expCONST().OPERATOR_TYPE.CONTAINS) ||
                angular.equals(operator, expCONST().OPERATOR_TYPE.MATCHES) ||
                angular.equals(operator, expCONST().OPERATOR_TYPE.ISEMPTY);
        }

        function getNamespaces(side) {
            var defer = $q.defer();
             namespacedService.getNamespacesFromDS({namespace: []})
                .then(function (namespaces) {
                    vm.namespaces[side] = namespaces.namespace;
                    updateNamespaces(side);
                    defer.resolve();
                },
                function (reason) {
                    vm.namespaces[side] = [];
                    vm.expr[side].expValues = [''];
                    namespacedAlertsService.errorGet();
                    defer.resolve();
                }
            );
            return defer.promise;
        }

        function nameChanged(side) {
            var expr = vm.expr[side];
            $scope.onNameChanged({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: 0,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function valueChanged(side, index) {
            var expr = vm.expr[side];
            $scope.onValueChanged({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: index,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function valueTypeChanged(side) {
            var expr = vm.expr[side];
            $scope.onValueTypeChanged({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: expr.expValues,
                changedValueIndex: 0,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function operatorChanged(side) {
            vm.expr[side].expValues = [''];
            vm.expr[side].isMultivalueOperatorSelected = expService.isMultivalueOperatorSelected(side, vm.expr, vm.operators);
            updateNamespaces(side);
            if (expService.isValueTypeNamespace(vm.expr, side)) {
                getNamespaces(side);
            }

            var expr = vm.expr[side];
            if (expService.isOperatorInIpRange(vm.expr, side)) {
                expr.expName = expCONST().DEFAULT_TITLE_NAME.IN_IP_RANGE; //use constant name
            }

            $scope.onOperatorChanged({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: [''],
                changedValueIndex: 0,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function negationChanged(side) {
            var expr = vm.expr[side];
            $scope.onNegationChanged({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: 0,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function addNewValue(side, index) {
            vm.expr[side].expValues.push('');
            var expr = vm.expr[side];
            $scope.onValueAdded({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: index,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });
        }

        function removeValue(side, index) {
            vm.expr[side].expValues.splice(index, 1);
            updateNamespaces(side);

            var expr = vm.expr[side];
            $scope.onValueRemoved({
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: index,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            });

        }

        function updateNamespaces(side) {
            var oppositeSide = expService.getOppositeSide(side);
            if (isValueTypeNamespace(side) || isValueTypeNamespace(oppositeSide)) {
                expService.updateSelectedNamespaces(side, vm.namespaces, vm.expr);
                expService.updateSelectedNamespaces(oppositeSide, vm.namespaces, vm.expr);
            }
        }

        function onNamespaceChanged(side, changedValueIndex) {
            updateNamespaces(side);
            vm.valueChanged(side, changedValueIndex);
        }

        function expressionChanged(side) {
            var expr = vm.expr[side];
            var result = {
                side: side,
                newName: expr.expName,
                newOperator: expr.expOperator,
                newValues: isValueTypeNamespace(side) ? expService.getNamespaceNames(expr) : expr.expValues,
                changedValueIndex: 0,
                newValueType: expr.valueType,
                newNegationValue: isNegationApplicable(side) ? expr.negation : undefined
            };

            if (expService.isValueTypeNamespace(vm.expr, side) && angular.isDefined(vm.namespaces[side]) && !utils.isMapEmpty(vm.namespaces[side])) {
                getNamespaces(side).then(function() {
                    //update alreadySelected items
                    angular.forEach(vm.namespaces[side], function(namespace, index) {
                        if (namespace.name === result.newValues[0]) {
                           namespace.alreadySelected = true;
                        }
                    });
                    $scope.onExpressionChanged(result);
                });
            } else {
                $scope.onExpressionChanged(result);
            }
        }

        function isValueTypeNamespace(side) {
            return expService.isValueTypeNamespace(vm.expr, side);
        }

        function hasError(error) {
            return utils.isDefinedAndNotEmpty(error);
        }
    }
})();
