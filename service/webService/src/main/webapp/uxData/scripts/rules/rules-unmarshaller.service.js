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
        .module('uxData.services')
        .factory('rulesUnmarshaller', rulesUnmarshaller);

    rulesUnmarshaller.$inject = ['RULES_CONSTANTS', 'SERVER_CONSTANTS', 'utilsService', 'EXPRESSION_CONSTANTS'];

    function rulesUnmarshaller(rulesCONST, serverCONST, utils, exprCONST) {
        var service = {
            unmarshallJsonRule: unmarshallParamsAndReturnStatement,
            buildExpressionsObjects: buildExpressionsObjects
        };

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var OR = rulesCONST().OR;
        var XOR = rulesCONST().XOR;
        var HOST = rulesCONST().HOST;
        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;
        var URL_RULE = rulesCONST().RULE_TYPE.URL;
        var DECIDER_RULE = rulesCONST().RULE_TYPE.DECIDER;
        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var ADVANCED = serverCONST().EXP_EDIT_MODE.ADVANCED;

        return service;



//======================================= PUBLIC SECTION =============================================================//
        function unmarshallParamsAndReturnStatement(jsonRule) {

            var result = {params: [], returnStatement: {}};

            if (jsonRule.if) {
                $.merge(result.params, unmarshallParams(jsonRule));
                var currResult = unmarshallParamsAndReturnStatement(jsonRule.if[0]);
                $.merge(result.params, currResult.params);
                result.returnStatement = currResult.returnStatement;
            }
            else if (jsonRule.return) {
                $.merge(result.params, unmarshallParams(jsonRule));
                result.returnStatement = unmarshallReturnStatement(jsonRule.return);
            }
            return result;
        }

//======================================= PRIVATE HELPERS ============================================================//
        function unmarshallParams(statement) {
            var params = [];
            if (statement.equals) {
                params.push.apply(params, unmarshallParam(statement, 'equals'));
            }
            if (statement.notEqual) {
                params.push.apply(params, unmarshallParam(statement, 'notEqual'));
            }
            if (statement.matches) {
                params.push.apply(params, unmarshallParam(statement, 'matches'));
            }
            if (statement.greaterThan) {
                params.push.apply(params, unmarshallParam(statement, 'greaterThan'));
            }
            if (statement.lessThan) {
                params.push.apply(params, unmarshallParam(statement, 'lessThan'));
            }
            if (statement.greaterOrEqual) {
                params.push.apply(params, unmarshallParam(statement, 'greaterOrEqual'));
            }
            if (statement.lessOrEqual) {
                params.push.apply(params, unmarshallParam(statement, 'lessOrEqual'));
            }
            if (statement.contains) {
                params.push.apply(params, unmarshallParam(statement, 'contains'));
            }
            if (statement.inIpRange) {
                params.push.apply(params, unmarshallParam(statement, 'inIpRange'));
            }
            if (statement.isEmpty) {
                params.push.apply(params, unmarshallParam(statement, 'isEmpty'));
            }
            if (statement.random) {
                params.push.apply(params, unmarshallParam(statement, 'random'));
            }
            if (statement.percent) {
                params.push.apply(params, unmarshallParam(statement, 'percent'));
            }

            if (statement.and) {
                return unmarshallParams(statement.and[0]);
            }
            if (statement.or) {
                var orParam = {};
                orParam.operatorName = 'or';
                orParam.isBinary = true;
                orParam.isOr = true;
                var result = unmarshallParams(statement.or[0]);
                orParam[LEFT] = result[0];
                orParam[RIGHT] = result[1];
                params.push(orParam);
            }
            if (statement.xor) {
                var xorParam = {};
                xorParam.operatorName = 'xor';
                xorParam.isBinary = true;
                xorParam.isXOR = true;
                var result = unmarshallParams(statement.xor[0]);
                xorParam[LEFT] = result[0];
                xorParam[RIGHT] = result[1];
                params.push(xorParam);
            }
            return params;
        }

        function unmarshallParam(statement, expression) {
            var params = [];
            if (!angular.isArray(statement[expression])) {
                statement[expression] = [statement[expression]];
            }
            for (var i = 0; i < statement[expression].length; i++) {
                var parameter = {};
                parameter.name = statement[expression][i].param;
                parameter.type = statement[expression][i].type;
                parameter.expression = expression;
                if (isContainsNamespacedLists(parameter.type, parameter.expression)) {
                    parameter.expression = exprCONST().OPERATOR_TYPE.CONTAINS_NMSP;
                } else if (isInIpRangeNamespacedLists(parameter.type, parameter.expression)) {
                    parameter.expression = exprCONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP;
                }
                parameter.negation = statement[expression][i].negation;
                if (statement[expression][i].pattern) {
                    parameter.value = statement[expression][i].pattern;
                } else if (statement[expression][i].values) {
                    parameter.value = statement[expression][i].values.value;
                } else if (statement[expression][i].namespacedList) {
                    parameter.value = statement[expression][i].namespacedList.value;
                } else {
                    parameter.value = statement[expression][i].value;
                }
                if (!angular.isArray(parameter.value)) {
                    parameter.value = [parameter.value];
                }
                params.push(parameter);
            }
            return params;
        }

        function isContainsNamespacedLists(type, expression) {
            return angular.equals(type, exprCONST().OPERATOR_TYPE.NAMESPACED_LIST) && angular.equals(expression, exprCONST().OPERATOR_TYPE.CONTAINS);
        }

        function isInIpRangeNamespacedLists(type, expression) {
            return angular.equals(type, exprCONST().OPERATOR_TYPE.NAMESPACED_LIST) && angular.equals(expression,exprCONST().OPERATOR_TYPE.IN_IP_RANGE);
        }

        function unmarshallReturnStatement(returnStatement) {
            var result = {};

            if (returnStatement.urlRule) {
                if (angular.isArray(returnStatement.urlRule)) {
                    returnStatement.urlRule = returnStatement.urlRule[0];
                }
                if (returnStatement.urlRule.urn) {
                    result.urn = returnStatement.urlRule.urn;
                }
                if (returnStatement.urlRule.port) {
                    result.port = returnStatement.urlRule.port;
                }
                if (returnStatement.urlRule.protocol) {
                    result.protocol = returnStatement.urlRule.protocol;
                }
                if (returnStatement.urlRule.ipProtocolVersion) {
                    result.ipProtocolVersion = returnStatement.urlRule.ipProtocolVersion;
                }
            }
            else if (returnStatement.partner) {
                result.partner = returnStatement.partner[0];
            }
            else {
                var servers = returnStatement.serverGroup ? returnStatement.serverGroup[0].server : returnStatement.server;
                result.servers = [];

                for (var i = 0; i < servers.length; i++) {
                    var ret = {};
                    ret.path = servers[i].path;
                    ret.serverName = servers[i].name;
                    ret.description = servers[i].description;
                    ret.url = servers[i].url;
                    ret.query = servers[i].query;
                    ret.isNonWhitelisted = servers[i].isNonWhitelisted === 'true' || servers[i].isNonWhitelisted === true;

                    result.servers.push(ret);
                }

                if (angular.isDefined(returnStatement.serverGroup) && returnStatement.serverGroup[0]) {
                    result.countDownTime = returnStatement.serverGroup[0].countDownTime;
                    result.enablePrivate = returnStatement.serverGroup[0].enablePrivate;
                }
            }

            return result;
        }

//====== HELPERS FOR CONSTRUCTING INTERNAL REPRESENTATION OBJECTS FROM UNMARSHALLED OBJECTS ==========================//

        function buildExpressionsObjects(params) {
            var expressions = [];
            angular.forEach(params, function (param, index) {
                var expressionObj = {};
                if (param.isBinary) {
                    var leftOperand = buildExpression(param[LEFT].name, param[LEFT].type, param[LEFT].expression, param[LEFT].value, LEFT, param[LEFT].negation);
                    var rightOperand = buildExpression(param[RIGHT].name, param[RIGHT].type, param[RIGHT].expression, param[RIGHT].value, RIGHT, param[RIGHT].negation);

                    expressionObj = buildBinaryExpression(leftOperand, rightOperand, param.isOr ? OR : XOR);
                }
                else {
                    expressionObj = buildExpression(param.name, param.type, param.expression, param.value, LEFT, param.negation);
                }
                expressions.push(angular.copy(expressionObj));
            });
            return expressions;
        }

        function buildExpression(name, type, operator, values, side, negation) {
            var expr = {};
            expr.type = '';
            expr[side] = {};
            expr[side].expName = name;
            expr[side].expOperator = operator;
            expr[side].expValues = values;
            expr[side].expType = type;
            expr[side].negation = negation;

            return expr;
        }

        function buildBinaryExpression(leftOperand, rightOperand, exprType) {
            angular.extend(leftOperand, rightOperand);
            leftOperand.type = exprType;
            leftOperand.isBinary = true;
            return leftOperand;
        }
    }
})();
