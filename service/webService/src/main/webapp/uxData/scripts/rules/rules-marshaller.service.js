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
        .module('uxData.services')
        .factory('rulesMarshaller', rulesMarshaller);

    rulesMarshaller.$inject = ['RULES_CONSTANTS', 'SERVER_CONSTANTS', 'EXPRESSION_CONSTANTS', 'utilsService'];

    function rulesMarshaller(rulesCONST, serverCONST, expCONST, utils) {
        var service = {
            marshallRuleToJSON: marshallRule
        };


        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var OR = rulesCONST().OR;
        var XOR = rulesCONST().XOR;
        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;
        var URL_RULE = rulesCONST().RULE_TYPE.URL;
        var DECIDER_RULE = rulesCONST().RULE_TYPE.DECIDER;
        var LESS = expCONST().OPERATOR_TYPE.LESSTHAN;
        var LESSOREQUAL = expCONST().OPERATOR_TYPE.LESSOREQUAL;
        var GREATER = expCONST().OPERATOR_TYPE.GREATERTHAN;
        var GREATEROREQUAL = expCONST().OPERATOR_TYPE.GREATEROREQUAL;

        return service;
//======================================= PUBLIC SECTION =============================================================//

        function marshallRule(expressions, returnStatement) {
            return createRule(expressions, 0, returnStatement);
        }

//======================================= PRIVATE HELPERS ============================================================//

        function createRule(params, currIndex, returnStatement) {

            var exp = null;
            if ((params.length - currIndex) === 0) {
                return returnStatement;
            } else if ((params.length - currIndex) === 1) {
                if (params[currIndex].isBinary) {
                    exp = buildBinaryExpFromParam(params[currIndex], params[currIndex].operatorName);
                    return ifStatement(exp, returnStatement);
                } else {
                    exp = getExp(params[currIndex].expression, params[currIndex].paramName, params[currIndex].paramValue, params[currIndex]);
                    return ifStatement(exp, returnStatement);
                }
            } else {
                var exp1 = null;
                var exp2 = null;
                if (params[currIndex].isBinary) {
                    exp = buildBinaryExpFromParam(params[currIndex], params[currIndex].operatorName);
                    currIndex++;
                    return ifStatement(exp, createRule(params, currIndex, returnStatement));
                } else {
                    exp1 = getExp(params[currIndex].expression, params[currIndex].paramName, params[currIndex].paramValue, params[currIndex]);

                    currIndex++;
                    if (params[currIndex].isBinary) {
                        return ifStatement(exp1, createRule(params, currIndex, returnStatement));
                    }

                    exp2 = getExp(params[currIndex].expression, params[currIndex].paramName, params[currIndex].paramValue, params[currIndex]);
                    exp = getExp('and', exp1, exp2);
                    currIndex++;
                    return ifStatement(exp, createRule(params, currIndex, returnStatement));
                }
            }
        }

        function isBinary(expressionType) {
            return angular.equals(expressionType, OR) || angular.equals(expressionType, XOR);
        }

        function isRelationalOperator(expression) {
            return angular.equals(expression, LESS) ||
                angular.equals(expression, LESSOREQUAL) ||
                angular.equals(expression, GREATEROREQUAL) ||
                angular.equals(expression, GREATER);
        }

        function buildBinaryExpFromParam(param, operatorName) {
            var exp1 = getExp(param[LEFT].expression, param[LEFT].paramName, param[LEFT].paramValue, param[LEFT]);
            var exp2 = getExp(param[RIGHT].expression, param[RIGHT].paramName, param[RIGHT].paramValue, param[RIGHT]);
            return getExp(operatorName, exp1, exp2);
        }

        function getExp(expressionName, paramName, paramValue, param) {
            switch (expressionName.toLowerCase()) {
                case 'equals':
                case 'notequal':
                {
                    return simpleBinaryExp(expressionName, paramName, paramValue, param);
                }
                case 'greaterorequal':
                case 'lessorequal':
                case 'lessthan':
                case 'greaterthan':
                {
                    return simpleTypedBinaryExp(expressionName, paramName, paramValue, param);
                }
                case 'contains':
                {
                    return containsExp(paramName, paramValue, param);
                }
                case 'containsnmsp':
                {
                    return containsNamespaceExp(paramName, paramValue, param);
                }
                case 'matches':
                {
                    return matchesExp(paramName, paramValue, param);
                }
                case 'and':
                {
                    return andExp(paramName, paramValue);
                }
                case 'or':
                {
                    return orExp(paramName, paramValue);
                }
                case 'xor':
                {
                    return xorExp(paramName, paramValue);
                }
                case 'isempty':
                {
                    return isEmptyExp(paramName, param);
                }
                case 'percent':
                {
                    return percentExp(paramValue, param);
                }
                case 'random':
                {
                    return randomExp(paramValue, param);
                }
                case 'iniprangelist':
                {
                    return inIpRangeListExp(paramName, paramValue, param);
                }
                case 'iniprange':
                {
                    return inIpRangeExp(paramName, paramValue, param);
                }
            }
        }

        function ifStatement(expression, statement) {
            var expName = Object.keys(expression)[0]; //get expression name (e.g. 'equals', 'isEmpty')

            var statName = Object.keys(statement)[0];

            var ifStat = {};//assemble whole expression
            var ifStatInternals = {};
            ifStatInternals[expName] = expression[expName];
            ifStatInternals[statName] = statement[statName];
            ifStat.if = [ifStatInternals];
            return ifStat;
        }

        function simpleBinaryExp(expName, paramName, value, param) {
            var result = {};
            result[expName] = [{
                'param': paramName,
                'value': value[0]
            }];
            return result;
        }

        function simpleTypedBinaryExp(expName, paramName, value, param) {
            var result = {};
            result[expName] = [{
                'param': paramName,
                'value': value[0],
                'type': (angular.isDefined(param.type) ? param.type : 'none')
            }];
            return result;
        }

        function matchesExp(paramName, pattern, param) {
            return {
                'matches': [{
                    'negation': param.negation,
                    'param': paramName,
                    'pattern': pattern
                }]
            };
        }

        function containsExp(paramName, values, param) {
            return {
                'contains': [{
                    'negation': param.negation,
                    'param': paramName,
                    'values': {
                        'value': values // should be an array
                    }
                }]
            };
        }

        function containsNamespaceExp(paramName, values, param) {
            return {
                'contains': [{
                    'negation': param.negation,
                    'type': 'namespacedList',
                    'param': paramName,
                    'namespacedList': {
                        value: values
                    }
                }]
            };
        }

        function inIpRangeExp(paramName, values, param) {
            return {
                'inIpRange': [{
                    'negation': param.negation,
                    'param': paramName,
                    'values': {
                        'value': values // should be an array
                    }
                }]
            };
        }

        function inIpRangeListExp(paramName, values, param) {
            return {
                'inIpRange': [{
                    'negation': param.negation,
                    'type': 'namespacedList',
                    'param': paramName,
                    'namespacedList': {
                        value: values
                    }
                }]
            };
        }

        function greaterOrEqualExp(paramName, value, param) {
            return {
                'greaterOrEqual': [{
                    'negation': param.negation,
                    'param': paramName,
                    'value': value
                }]
            };
        }

        function andExp(expression1, expression2) {
            return complexBinaryExp('and', expression1, expression2);
        }

        function orExp(expression1, expression2) {
            return complexBinaryExp('or', expression1, expression2);
        }

        function xorExp(expression1, expression2) {
            return complexBinaryExp('xor', expression1, expression2);
        }

        function complexBinaryExp(name, expression1, expression2) {
            var exp1Name = Object.keys(expression1)[0];
            var exp2Name = Object.keys(expression2)[0];
            var result = {};
            if (exp1Name === exp2Name) {
                var joinedExpression = {};
                joinedExpression[exp1Name] = [
                    expression1[exp1Name][0],
                    expression2[exp2Name][0]
                ];
                result[name] = [joinedExpression];
                return result;
            } else {
                var expOperands = {};
                expOperands[exp1Name] = expression1[exp1Name];
                expOperands[exp2Name] = expression2[exp2Name];
                result[name] = [expOperands];
                return result;
            }
            return result;
        }

        function isEmptyExp(expression, param) {
            return {
                'isEmpty': [{
                    'negation': param.negation,
                    'param': expression
                }]
            };
        }

        function percentExp(value, param) {
            return {
                'percent': [{
                    'negation': param.negation,
                    'value': value
                }]
            };
        }

        function randomExp(value, param) {
            return {
                'random': [{
                    'negation': param.negation,
                    'value': value
                }]
            };
        }
    }
})();
