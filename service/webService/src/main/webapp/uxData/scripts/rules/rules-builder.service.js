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
        .factory('rulesBuilderService', rulesBuilderService);

    rulesBuilderService.$inject = ['RULES_CONSTANTS', 'utilsService', 'rulesService', 'rulesUnmarshaller', 'rulesMarshaller',
        'COMMON_CONSTANTS', 'EXPRESSION_CONSTANTS'];

    function rulesBuilderService(rulesCONST, utils, rulesService, rulesUnmarshaller, rulesMarshaller, commonCONST, expCONST) {
        var service = {

            unmarshallJSONRule: unmarshallJSONRule,
            unmarshallJSONTemplateRules: unmarshallJSONTemplateRules,
            marshallRuleToJSON: marshallRuleToJSON,
            unmarshallJSONRulesForPreview: unmarshallJSONRulesForPreview,
            getRuleDiffText: getRuleDiffText,
            getRuleSimpleText: getRuleSimpleText
        };

        var ADD = commonCONST().PENDING_CHANGE_TYPE.ADD;
        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var OR = rulesCONST().OR;
        var XOR = rulesCONST().XOR;
        var LESS = expCONST().OPERATOR_TYPE.LESSTHAN;
        var LESSOREQUAL = expCONST().OPERATOR_TYPE.LESSOREQUAL;
        var GREATER = expCONST().OPERATOR_TYPE.GREATERTHAN;
        var GREATEROREQUAL = expCONST().OPERATOR_TYPE.GREATEROREQUAL;

        return service;

//================================== Public section ==================================//

        function unmarshallJSONRule(jsonRule, specificRuleBuilderService) {
            var rawRule = rulesUnmarshaller.unmarshallJsonRule(jsonRule);

            var rule = {};
            rule.expressions = rulesUnmarshaller.buildExpressionsObjects(rawRule.params);
            rule.returnStatement = specificRuleBuilderService.buildReturnStatement(rawRule.returnStatement);
            specificRuleBuilderService.completeRuleUnmarshalling(jsonRule, rawRule, rule);

            return rule;
        }

        function marshallRuleToJSON(exprs, returnObj, specificRuleBuilderService) {
            var expressions = prepareExpressionsForMarshalling(exprs);
            var returnStatement = specificRuleBuilderService.prepareReturnForMarshalling(returnObj);

            return rulesMarshaller.marshallRuleToJSON(expressions, returnStatement);
        }

        function unmarshallJSONRulesForPreview(jsonRules, pendingChanges, servicePaths, ruleType, ruleSpecificBuilderService) {
            var rulesMap = {};
            var ifArray = utils.toArray(jsonRules.if);
            angular.forEach(ifArray, function(rule, index) {
                rulesMap[rule.id] = getRuleObject(rule.id, rule, pendingChanges, servicePaths, ruleType, ruleSpecificBuilderService);
            });

            if (angular.isDefined(pendingChanges)) {
                angular.forEach(pendingChanges[ruleType].entry, function(entry, index) {
                    var change = entry.value;
                    if (angular.equals(change.changeType, ADD)) {
                        rulesMap[change.id] = getRuleObject(change.id, change.changedExpression, pendingChanges, servicePaths, ruleType, ruleSpecificBuilderService);
                    }
                });
            }
            return rulesMap;
        }

        function unmarshallJSONTemplateRules(ruleType, jsonTemplateRules, pendingChanges, servicePaths, path, ruleSpecificBuilderService, rules) {
            var rulesMap = {};
            var ifArray = utils.toArray(angular.isDefined(jsonTemplateRules) ? jsonTemplateRules.if : []);
            var allRulesWithTemplateName = getAllRuleWithTemplateName(rules[ruleType]);
            angular.forEach(ifArray, function (rule, index) {
                var ruleObject = getRuleObject(rule.id, rule, pendingChanges, servicePaths, path, ruleSpecificBuilderService);
                ruleObject.dependentRules = getAllDependentRules(ruleType, rule, allRulesWithTemplateName);
                rulesMap[rule.id] = ruleObject;
            });

            if (angular.isDefined(pendingChanges)) {
                angular.forEach(pendingChanges[path].entry, function (entry, index) {
                    var change = entry.value;
                    if (angular.equals(change.changeType, ADD)) {
                        rulesMap[change.id] = getRuleObject(change.id, change.changedExpression, pendingChanges, servicePaths, path, ruleSpecificBuilderService);
                    }
                });
            }
            return rulesMap;
        }

        function getAllRuleWithTemplateName(jsonRules) {
            var rules = [];
            var ifArray = utils.toArray(jsonRules.if);
            angular.forEach(ifArray, function (rule, index) {
                if (angular.isDefined(rule.templateName)) {
                    rules.push(rule);
                }
            });
            return rules;
        }

        function getAllDependentRules(ruleType, templateRules, allRules) {
            var dependentRules = '';
            for (var i = 0; i < allRules.length; i++) {
                if (allRules[i].templateName === templateRules.id) {
                    if (dependentRules === '') {
                        dependentRules = ruleType === 'rules' ? '(Depending Rules: ' : '(Depending Url Rules: ';
                    }
                    dependentRules = dependentRules + allRules[i].id + ', ';
                }
            }
            if (dependentRules != '') {
                dependentRules = dependentRules.substring(0, dependentRules.length - 2) + ')';
            }
            return dependentRules;
        }

        function getRuleObject(ruleId, jsonRule, pendingChanges, servicePaths, ruleType, ruleSpecificBuilderService) {
            var pendingRule = getPendingChangeByRuleId(ruleId, pendingChanges, ruleType);
            var mergedRule = jsonRule;

            if (angular.isDefined(pendingRule)) {
                mergedRule = angular.isDefined(pendingRule.value.changedExpression) ?
                    pendingRule.value.changedExpression : pendingRule.value.currentExpression;
            }
            var ruleObject = buildRuleObject(mergedRule, servicePaths, ruleSpecificBuilderService);
            ruleObject.data.id = ruleId;

            //APPDS-1417: current flavor should be displayed instead of pending in rule preview
            if (angular.isDefined(pendingRule) && angular.isDefined(pendingRule.value.changedExpression) && angular.isDefined(pendingRule.value.currentExpression)) {
                var ruleObjectFromCurrentRule = buildRuleObject(pendingRule.value.currentExpression, servicePaths, ruleSpecificBuilderService);
                ruleObject.data.path = ruleObjectFromCurrentRule.data.path;
            }
            addViewTextToRuleObject(ruleObject, pendingRule, servicePaths, ruleSpecificBuilderService);
            return ruleObject.data;
        }

        function getPendingChangeByRuleId(ruleId, pendingChanges, ruleType) {
            var change;

            if (!utils.isEmptyObject(pendingChanges)) {
                angular.forEach(pendingChanges[ruleType].entry, function(entry, index) {
                    if (angular.equals(entry.key, ruleId)) {
                        change = entry;
                    }
                });
            }
            return change;
        }

        function buildRuleObject(jsonRule, servicePaths, specificRuleBuilderService) {

            var rawRule = unmarshallJSONRule(jsonRule, specificRuleBuilderService);
            return specificRuleBuilderService.buildRuleObjectForPreview(rawRule, jsonRule, servicePaths);
        }

        function addViewTextToRuleObject(ruleObject, pendingRule, servicePaths, ruleSpecificBuilderService) {
            var xmlRuleMerged = ruleSpecificBuilderService.objectToXml(ruleObject);
            var simpleRuleMerged = getRuleSimpleText(ruleObject.params, ruleObject.returnStatement, ruleSpecificBuilderService);
            var diffRuleMergedText = getRuleDiffText(ruleObject.params, ruleObject.returnStatement, ruleSpecificBuilderService);

            if (angular.isDefined(pendingRule)) {
                switch (pendingRule.value.changeType) {
                    case commonCONST().PENDING_CHANGE_TYPE.ADD:
                        ruleObject.data.xmlRuleCurrent = xmlRuleMerged;
                        ruleObject.data.simpleRuleCurrent = simpleRuleMerged;
                        ruleObject.data.diffRuleChangedText = diffRuleMergedText;
                        break;
                    case commonCONST().PENDING_CHANGE_TYPE.DELETE:
                        ruleObject.data.xmlRuleCurrent = xmlRuleMerged;
                        ruleObject.data.simpleRuleCurrent = simpleRuleMerged;
                        ruleObject.data.diffRuleCurrentText = diffRuleMergedText;
                        break;
                    case commonCONST().PENDING_CHANGE_TYPE.UPDATE:
                        var currentRule = buildRuleObject(pendingRule.value.currentExpression, servicePaths, ruleSpecificBuilderService);
                        ruleObject.data.xmlRuleCurrent = utils.jsonObjToXml(currentRule.data.rule);
                        ruleObject.data.simpleRuleCurrent = getRuleSimpleText(currentRule.params, currentRule.returnStatement, ruleSpecificBuilderService);
                        ruleObject.data.diffRuleCurrentText = getRuleDiffText(currentRule.params, currentRule.returnStatement, ruleSpecificBuilderService);
                        ruleObject.data.diffRuleChangedText = diffRuleMergedText;
                        break;
                }
                ruleObject.data.changeType = pendingRule.value.changeType;
                ruleObject.data.hasChanges = true;
            } else {
                ruleObject.data.xmlRuleCurrent = xmlRuleMerged;
                ruleObject.data.simpleRuleCurrent = simpleRuleMerged;
                ruleObject.data.diffRuleCurrentText = diffRuleMergedText;
                ruleObject.data.diffRuleChangedText = diffRuleMergedText;
            }
        }

        function getRuleSimpleText(params, returnStatement, ruleSpecificBuilderService) {
            var paramsText = getRuleParamsSimpleText(params);
            var returnText = ruleSpecificBuilderService.getRuleReturnSimpleText(returnStatement);

            return paramsText + returnText;
        }

        function getRuleDiffText(params, returnStatement, ruleSpecificBuilderService) {
            var paramsText = getRuleParamsDiffText(params);
            var returnText = ruleSpecificBuilderService.getRuleReturnDiffText(returnStatement);

            return paramsText + returnText;
        }

        function getRuleParamsSimpleText(expressions) {
            var simpleParams = 'IF ';

            // iterate over each expression
            angular.forEach(expressions, function(expression, index) {
                simpleParams += expression.isBinary ? '(' : '';

                // iterate over LEFT and RIGHT operands of an expression
                angular.forEach(expression, function(operand, side) {
                    if (angular.equals(side, LEFT) || angular.equals(side, RIGHT)) {
                        var name = angular.isDefined(operand.expName) ? operand.expName : '';
                        var negation = operand.negation ? 'NOT ' : '';
                        var operator = negation + operand.expOperator;
                        var value = getValueText(operand.expValues);
                        var valueType = !utils.isEmptyString(operand.expType) ? ('(' + operand.expType.toUpperCase() + ')') : '';

                        simpleParams += '(' + name + ' ' + operator + ' '  + valueType + value + ')';
                        simpleParams += (expression.isBinary && angular.equals(side, LEFT)) ? (' ' + expression.type.toUpperCase() + ' ') : '';
                    }
                });
                simpleParams += expression.isBinary ? ')' : '';
                simpleParams += (index != expressions.length - 1) ? ' AND ' : '';
            });

            return simpleParams;
        }

        function getRuleParamsDiffText(expressions) {
            var ruleExpressionsText = 'IF\n\t';

            // iterate over each expression
            angular.forEach(expressions, function(expression, index) {
                ruleExpressionsText += expression.isBinary ? '(\n\t' : '';

                // iterate over LEFT and RIGHT operands of an expression
                angular.forEach(expression, function(operand, side) {
                    if (angular.equals(side, LEFT) || angular.equals(side, RIGHT)) {
                        var name = angular.isDefined(operand.expName) ? operand.expName : '';
                        var negation = operand.negation ? 'NOT ' : '';
                        var operator = negation + operand.expOperator;
                        var value = getValueText(operand.expValues);
                        var valueType = !utils.isEmptyString(operand.expType) ? '('+ operand.expType.toUpperCase() + ')' : '';

                        ruleExpressionsText += '\t(' + name + ' ' + operator + valueType + ' ' + value + ')';
                        ruleExpressionsText += (expression.isBinary && angular.equals(side, LEFT)) ? ('\n\t\t' + expression.type.toUpperCase() + '\n\t') : '';
                    }
                });
                ruleExpressionsText += expression.isBinary ? '\n\t)' : '';
                ruleExpressionsText += (index != expressions.length - 1) ? '\n\tAND\n\t' : '';
            });

            return ruleExpressionsText;
        }

        function getValueText(value) {
            return (angular.isArray(value) ? value.join(', ') : value);
        }

        function prepareExpressionsForMarshalling(expressions) {
            var exprs = [];

            angular.forEach(expressions, function (expr, index) {
                var expression = {};
                if (isBinary(expr.type)) {

                    expression[RIGHT] = {};
                    expression[RIGHT].paramName = expr[RIGHT].expName;
                    expression[RIGHT].expression = expr[RIGHT].expOperator;
                    expression[RIGHT].negation = expr[RIGHT].negation;
                    expression[RIGHT].paramValue = expr[RIGHT].expValues;

                    expression[LEFT] = {};
                    expression[LEFT].paramName = expr[LEFT].expName;
                    expression[LEFT].expression = expr[LEFT].expOperator;
                    expression[LEFT].negation = expr[LEFT].negation;
                    expression[LEFT].paramValue = expr[LEFT].expValues;

                    expression.isBinary = true;
                    expression.operatorName = expr.type.toLowerCase();

                    if (angular.equals(expr.type.toLowerCase(), OR)) {
                        expression.isOr = true;
                    }
                    else if (angular.equals(expr.type.toLowerCase(), XOR)) {
                        expression.isXor = true;
                    }

                    if (isRelationalOperator(expr[RIGHT].expOperator)) {
                        expression[RIGHT].type = expr[RIGHT].expType;
                    }

                    if (isRelationalOperator(expr[LEFT].expOperator)) {
                        expression[LEFT].type = expr[LEFT].expType;
                    }
                }
                else {
                    expression = {};
                    expression.paramName = expr[LEFT].expName;
                    expression.expression = expr[LEFT].expOperator;
                    expression.negation = expr[LEFT].negation;
                    expression.paramValue = expr[LEFT].expValues;

                    if (isRelationalOperator(expr[LEFT].expOperator)) {
                        expression.type = expr[LEFT].expType;
                    }
                }

                exprs.push(angular.copy(expression));
            });

            return exprs;
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
    }
})();
