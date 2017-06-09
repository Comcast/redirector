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


(function() {
    'use strict';

    angular
        .module('uxData.expression')
        .factory('expressionService', expressionService);

    expressionService.$inject = ['EXPRESSION_CONSTANTS', 'RULES_CONSTANTS'];

    function expressionService (expCONST, rulesCONST) {

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;

        var service = {
            isRelationalOperator: isRelationalOperator,
            isValueTypeNamespace: isValueTypeNamespace,
            isOperatorInIpRange: isOperatorInIpRange,
            isMultivalueOperatorSelected: isMultivalueOperatorSelected,
            updateSelectedNamespaces: updateSelectedNamespaces,
            getNamespaceNames: getNamespaceNames,
            getOppositeSide: getOppositeSide
        };

        return service;

        function updateSelectedNamespaces(side, namespaces, expressions) {

            if (angular.isUndefined(namespaces[side])) {
                return;
            }

            angular.forEach(namespaces[side], function (namespace, nsIndex) {
                var alreadySelected = false;
                angular.forEach(expressions[side].expValues, function(value, index) {
                    if (!alreadySelected) {
                        alreadySelected = angular.equals(namespace.name, value.name);
                        namespace.alreadySelected = alreadySelected;
                    }
                });

                if (!alreadySelected && angular.equals(expressions[LEFT].expOperator, expressions[RIGHT].expOperator)) {
                    angular.forEach(expressions[getOppositeSide(side)].expValues, function(value, index) {
                        if (!alreadySelected) {
                            alreadySelected = angular.equals(namespace.name, value.name);
                            namespace.alreadySelected = alreadySelected;
                        }
                    });
                }
            });
        }

        function getOppositeSide(side) {
            return angular.equals(side, LEFT) ? RIGHT : LEFT;
        }

        function getNamespaceNames(expr) {
            var namespaceNames = [];
            for (var i =0; i < expr.expValues.length; i++) {
                if ( (typeof expr.expValues[i] === 'function') || (typeof expr.expValues[i] === 'object') ) {
                    namespaceNames.push(expr.expValues[i].name);
                } else {
                    namespaceNames.push(expr.expValues[i]);
                }
            }
            return namespaceNames;
        }

        function isMultivalueOperatorSelected (operandSide, expressions, operators) {
            return angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.CONTAINS) ||
                angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.CONTAINS_NMSP) ||
                angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.IN_IP_RANGE) ||
                angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP);
        }

        function isRelationalOperator (expression) {
            return angular.equals(expression.expOperator, expCONST().OPERATOR_TYPE.LESSTHAN) ||
                angular.equals(expression.expOperator, expCONST().OPERATOR_TYPE.LESSOREQUAL) ||
                angular.equals(expression.expOperator, expCONST().OPERATOR_TYPE.GREATERTHAN) ||
                angular.equals(expression.expOperator, expCONST().OPERATOR_TYPE.GREATEROREQUAL);
        }

        function isValueTypeNamespace (expressions, operandSide) {
                return angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.CONTAINS_NMSP) ||
                angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP);
        }

        function isOperatorInIpRange (expressions, operandSide) {
                return angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.IN_IP_RANGE) ||
                angular.equals(expressions[operandSide].expOperator, expCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP);
        }
    }
})();
