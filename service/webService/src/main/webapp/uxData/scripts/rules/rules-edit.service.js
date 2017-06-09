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
    angular
        .module('uxData.rules')
        .factory('rulesEditService', rulesEditService);

    rulesEditService.$inject = ['RULES_CONSTANTS'];

    function rulesEditService(rulesCONST) {
        var service = {
            createExpression: createExpression,
            createBinaryExpression: createBinaryExpression,
            isBinary: isBinary,
            isRelationalOperator: isRelationalOperator
        };
        return service;

//================================== Public section ==================================//

        function createExpression(name, type, operator, values, side, negation) {
            var expr = {};
            expr.type = '';
            expr[side] = {};
            expr[side].expName = name;
            expr[side].expOperator = operator;
            expr[side].negation = negation;
            expr[side].expValues = values;
            expr[side].expType = type;

            return expr;
        }

        function createBinaryExpression(leftOperand, rightOperand, exprType) {
            angular.extend(leftOperand, rightOperand);
            leftOperand.type = exprType;
            return leftOperand;
        }

        function isBinary(expressionType) {
            return expressionType === rulesCONST().OR || expressionType === rulesCONST().XOR;
        }

        function isRelationalOperator(expression) {
            return expression === 'lessThan' ||
                expression === 'lessOrEqual' ||
                expression === 'greaterThan' ||
                expression === 'greaterOrEqual';
        }

    }
})();
