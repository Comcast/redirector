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
        module('uxData.urlRules').
        controller('urlRulesEditController', urlRulesEditController);

    urlRulesEditController.$inject = ['$state', '$stateParams', '$rootScope', '$scope', 'STATES_CONSTANTS',
        'RULES_CONSTANTS', 'EXPRESSION_CONSTANTS', 'rulesEditService', 'rulesBuilderService', 'RulesCommonValidationService',
        'authService', 'USER_PERMISSIONS', 'utilsService', 'rulesAlertsService', 'urlRulesService',
        'urlRulesValidationService', 'urlRulesRequestService', 'templatesService', 'LOCALSTORAGE_PAGE_NAMES',
        'urlRulesBuilderService', 'messageService'];

    function urlRulesEditController($state, $stateParams, $rootScope, $scope,
                                    STATES_CONSTANTS, rulesCONST, expCONST, rulesEditService,
                                    rulesBuilderService, rulesCommonVS, authService,
                                    USER_PERMISSIONS, utils, rulesAlertsService, urlRulesService, urlRulesVS,
                                    urlRulesRequestService, templatesService, LOCALSTORAGE_PAGE_NAMES,
                                    urlRulesBuilderService, messageService) {


        /* jshint validthis: true */
        var vm = this;

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var EDIT_STATE = LOCALSTORAGE_PAGE_NAMES().urlRulesAdd;
        var ADD_STATE = LOCALSTORAGE_PAGE_NAMES().urlRulesEdit;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.hasPermissions = utils.hasPermissions;

        var beforeEditRule = {};

        // note that exprValidationData should be synchronized with rawExpressions
        // if value added/removed from rawExpression this change should be reflected in exprValidationData
        vm.exprValidationData = []; // {left: {duplicates: {}, nameErrorMsg: '', valueErrorMsg: '', generalErrorMsg: ''}, right: {same as left}}
        vm.urlParamsValidationData = {};

        vm.existingRuleIDs = {};
        vm.existingTemplatesIDs = {};

        vm.approvedDefaultUrlParams ={};
        vm.defaultUrlParams = {hasChanges: false};
        vm.defaultUrlParamsChanges = {};
        vm.defaultUrlParamsValidationData = {};
        vm.defaultUrlParamsToggle = false;
        vm.isSavingDefaultServer = false;

        vm.uiModelHolder = {
            ruleName: angular.isDefined($stateParams.ruleId) ? $stateParams.ruleId : '',
            currentTemplate: { },
            editMode: angular.isDefined($stateParams.ruleId), // there can be two states: adding new rule and editing rule
            currentTemplateName: '',
            rawExpressions: [],
            urlParams: {},
            disableUrlParams: false
        };

        vm.ruleNameError = '';

        vm.saveAsTemplate = saveAsTemplate;
        vm.addEmptyExpression = addEmptyExpression;
        vm.removeExpression = removeExpression;
        vm.cancelEditRule = cancelEditRule;
        vm.validateRuleName = validateRuleName;
        vm.hasError = utils.isDefinedAndNotEmpty;
        vm.updateExpressions = updateExpressions;
        vm.onExprNameChanged = onExprNameChanged;
        vm.onExprOperatorChanged = onExprOperatorChanged;
        vm.onExprValueAdded = onExprValueAdded;
        vm.onExprValueRemoved = onExprValueRemoved;
        vm.onExprValueChanged = onExprValueChanged;
        vm.onUrlParamsChanged = onUrlParamsChanged;
        vm.onDefaultUrlParamsChanged = onDefaultUrlParamsChanged;
        vm.saveDefaultUrlParams = saveDefaultUrlParams;
        vm.getUrlParamsDiffViewText = getUrlParamsDiffViewText;
        vm.onTemplateChanged = onTemplateChanged;

        vm.saveRule = saveRule;

        messageService.onChangeApp($scope, function () {
            cancelEditRule();
            initialize();
        });

        initialize();

        function initialize() {

            loadDefaultUrlParams();
            loadAllRuleIds();
            loadTemplatesIds();

            if (vm.uiModelHolder.editMode) {
                urlRulesService.loadRuleAndTemplates(vm.uiModelHolder.ruleName)
                    .then(function (result) {
                        vm.templateRules = result.templates;
                        //TODO: BACKWARD COMPATIBILITY
                        if (angular.isDefined(result.rule.if) && angular.isArray(result.rule.if)) {
                            result.rule = angular.copy(result.rule.if[0]);
                        }
                        initFormWithRule(result.rule, vm.uiModelHolder.ruleName);
                    }, function (reason) {
                        rulesAlertsService.failedToLoadData('rule data', reason);
                    }
                );
            }
            else {
                urlRulesService.loadRulesAndTemplates()
                    .then(function (result) {
                        vm.templateRules = result.templates;
                    }, function (reason) {
                        rulesAlertsService.failedToLoadData('rule data', reason);
                    }
                );
            }
        }

        function initFormWithRule(rule, ruleId) {
            clearModel();
            var ruleExpressionsAndUrlParams = rulesBuilderService.unmarshallJSONRule(rule, urlRulesBuilderService);

            if (utils.isDefinedAndNotEmpty(rule.templateName)) {

                // remove all expressions and servers of template
                var templateExpressionsAndUrlParams = rulesBuilderService.unmarshallJSONRule(vm.templateRules[rule.templateName], urlRulesBuilderService);
                angular.extend(ruleExpressionsAndUrlParams, filterOutExpressionsOfTemplate(ruleExpressionsAndUrlParams, templateExpressionsAndUrlParams));

                // init form without template
                initFormFromBuilder(ruleExpressionsAndUrlParams, ruleId);

                // now set template
                vm.uiModelHolder.currentTemplateName = rule.templateName;
                setTemplate();
            }
            else {
                initFormFromBuilder(ruleExpressionsAndUrlParams, ruleId);
            }
            createRuleSnapshotBeforeEdit();
        }

        function setTemplate() {
            // 1. remove current template, because only one template can be used in rule
            removeTemplate();
            // 2. and insert new one
            addTemplate();
        }

        // this is a 'private' helper of setTemplate()
        function addTemplate() {
            var templateExpressionsAndUrlParams = rulesBuilderService.unmarshallJSONRule(vm.templateRules[vm.uiModelHolder.currentTemplateName], urlRulesBuilderService);

            angular.forEach(templateExpressionsAndUrlParams.expressions.reverse(), function (templateExpression, index) {
                addExpression(templateExpression, true);
                vm.uiModelHolder.rawExpressions[0].disabled = true;
            });

            angular.copy(templateExpressionsAndUrlParams.returnStatement, vm.uiModelHolder.urlParams);
            vm.uiModelHolder.disableUrlParams = true;
            urlRulesVS.validateReturnValue(vm);
        }

        function removeTemplate() {

            // 1. remove expressions of template
            var index = 0;
            while (index < vm.uiModelHolder.rawExpressions.length) {
                var expression = vm.uiModelHolder.rawExpressions[index];
                if (expression.disabled) {
                    removeExpression(index);
                } else {
                    index++;
                }
            }

            // 2. urlRule can have only one url param, so do not need to remove it.
            // Just enable to allow edit.
            vm.uiModelHolder.disableUrlParams = false;
        }

        function filterOutExpressionsOfTemplate(allExpressions, templateExpressions) {
            var onlyRuleExpressionsAndUrlParams = {};
            onlyRuleExpressionsAndUrlParams.expressions = [];
            onlyRuleExpressionsAndUrlParams.returnStatement = templateExpressions.returnStatement;

            angular.forEach(allExpressions.expressions, function (ruleExpr, index) {
                var exprOfTemplate = false;
                angular.forEach(templateExpressions.expressions, function (templateExpr, index) {
                    if (angular.equals(templateExpr, ruleExpr)) {
                        exprOfTemplate = true;
                    }
                });
                if (!exprOfTemplate) {
                    onlyRuleExpressionsAndUrlParams.expressions.push(ruleExpr);
                }
            });

            return onlyRuleExpressionsAndUrlParams;
        }

        function onTemplateChanged() {
            if (utils.isDefinedAndNotEmpty(vm.uiModelHolder.currentTemplateName)) {
                setTemplate();
            }
            else {
                removeTemplate();
            }
        }

        function loadDefaultUrlParams() {
            urlRulesService.loadDefaultUrlParams()
                .then(function (data) {
                    angular.copy(data.urlRule, vm.defaultUrlParams);
                    angular.copy(data.urlRule, vm.approvedDefaultUrlParams);
                    if (utils.isDefinedAndNotEmpty(data.default)) {
                        angular.copy(data.default, vm.defaultUrlParamsChanges);
                        vm.defaultUrlParams.hasChanges = true;
                    }
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('rules names');
                }
            );
        }

        function loadAllRuleIds() {
            urlRulesService.loadAllUrlRuleIds(rulesCONST().RULE_TYPE.URL)
                .then(function (data) {
                    vm.existingRuleIDs = angular.copy(data);
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('rules names');
                }
            );
        }

        function loadTemplatesIds() {
            templatesService.loadAllTemplateIds(rulesCONST().TEMPLATES_TYPE.URL).then(function (data) {
                vm.existingTemplatesIDs = angular.copy(data);
            }, function() {
                rulesAlertsService.failedToLoadData('templatesIds');
            });
        }

        function initFormFromBuilder(expressionsAndUrlParams, ruleId) {
            vm.uiModelHolder.ruleName = ruleId;
            vm.uiModelHolder.editMode = true;
            angular.copy(expressionsAndUrlParams.expressions, vm.uiModelHolder.rawExpressions);
            angular.copy(expressionsAndUrlParams.returnStatement, vm.uiModelHolder.urlParams);
            initValidationData();
        }

        function createRuleSnapshotBeforeEdit() {
            beforeEditRule.id = vm.uiModelHolder.ruleName;
            beforeEditRule.jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, vm.uiModelHolder.urlParams, urlRulesBuilderService);
        }


        function initValidationData() {
            // now for each expression and server validation data should be initialized
            angular.forEach(vm.uiModelHolder.rawExpressions, function (expr, index) {
                vm.exprValidationData[index] = {};
                vm.exprValidationData[index][LEFT] = {duplicates: {}, valueErrorMsg: []};
                if (!utils.isEmptyString(expr.type)) {
                    vm.exprValidationData[index][RIGHT] = {duplicates: {}, valueErrorMsg: []};
                }
            });
            angular.copy({}, vm.urlParamsValidationData);
        }

        function addEmptyExpression(expressionType) {
            var expr = rulesEditService.createExpression('', '', expCONST().OPERATOR_TYPE.EQUAL, [''], LEFT);
            if (rulesEditService.isBinary(expressionType)) {
                var rightOperand = rulesEditService.createExpression('', '', expCONST().OPERATOR_TYPE.EQUAL, [''], RIGHT);
                expr = rulesEditService.createBinaryExpression(expr, rightOperand, expressionType);
            }
            addExpression(expr, false);
        }

        function addExpression(expression, pushToTheFront) {
            var validationExprObj = {};
            var expr = rulesEditService.createExpression(expression[LEFT].expName, expression[LEFT].expType, expression[LEFT].expOperator, expression[LEFT].expValues, LEFT);
            validationExprObj[LEFT] = {valueErrorMsg: []};

            if (rulesEditService.isBinary(expression.type)) {
                var rightOperand = rulesEditService.createExpression(expression[RIGHT].expName, expression[RIGHT].expType, expression[RIGHT].expOperator, expression[RIGHT].expValues, RIGHT);
                expr = rulesEditService.createBinaryExpression(expr, rightOperand, expression.type);
                validationExprObj[RIGHT] = {valueErrorMsg: []};
            }

            if (pushToTheFront) {
                vm.exprValidationData.unshift(validationExprObj);
                vm.uiModelHolder.rawExpressions.unshift(expr);
            }
            else {
                vm.exprValidationData.push(validationExprObj);
                vm.uiModelHolder.rawExpressions.push(expr);
            }
        }

        function removeExpression(index) {
            vm.uiModelHolder.rawExpressions.splice(index, 1);
            vm.exprValidationData.splice(index, 1);
            rulesCommonVS.validatePercentExpressions(vm);
        }

        function cancelEditRule() {
            clearModel();
            if (vm.uiModelHolder.editMode) {
                vm.uiModelHolder.editMode = false;
                if (angular.isDefined($state.previousState) && $state.previousState.name === STATES_CONSTANTS().showURLRules) {
                    $state.go($state.previousState.name, $state.previousParams);
                } else {
                    $state.go(STATES_CONSTANTS().addNewURLRule, {
                        ruleId: vm.uiModelHolder.ruleName,
                        serviceName: $rootScope.currentApplication
                    });
                }
            }
        }

        function clearModel() {
            vm.uiModelHolder.currentTemplate = {};
            vm.uiModelHolder.rawExpressions.splice(0, vm.uiModelHolder.rawExpressions.length);
            vm.uiModelHolder.ruleName = '';
            vm.uiModelHolder.currentTemplateName = '';
            vm.ruleNameError = '';
            angular.copy({}, vm.uiModelHolder.urlParams);
            angular.copy({}, vm.urlParamsValidationData);
            vm.exprValidationData.splice(0, vm.exprValidationData.length);
            vm.isSaving = false;
            vm.uiModelHolder.disableUrlParams= false;
        }

//=====================================Callbacks for expression and server changes====================================//

        function onExprNameChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType);
            rulesCommonVS.validateExprName(vm.uiModelHolder.rawExpressions[index][side], index, side, vm);
        }

        function onExprValueChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType);
            rulesCommonVS.validateExprValue(newValues[changedValueIndex], vm.uiModelHolder.rawExpressions[index][side], index, side, changedValueIndex, vm);
        }

        function onExprOperatorChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType);
            vm.exprValidationData[index][side].valueErrorMsg = [''];
            rulesCommonVS.validateExprName(vm.uiModelHolder.rawExpressions[index][side], index, side, vm);
            rulesCommonVS.validatePercentExpressions(vm);
        }

        function onExprValueAdded(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.push('');
        }

        function onExprValueRemoved(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.splice(changedValueIndex, 1);
        }

        function updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType) {
            vm.uiModelHolder.rawExpressions[index][side].expName = newName;
            vm.uiModelHolder.rawExpressions[index][side].expOperator = newOperator;
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.uiModelHolder.rawExpressions[index][side].expType = newValueType;
        }

        function onUrlParamsChanged() {
            urlRulesVS.validateReturnValue(vm);
        }

        function onDefaultUrlParamsChanged() {
            urlRulesVS.validateDefaultReturnValue(vm);
        }

//====================================================================================================================//


        function getUrlParamsDiffViewText (urlParams) {
            var urlParamsViewText = '';
            if (angular.isDefined(urlParams)) {
                urlParamsViewText = urlRulesBuilderService.getRuleReturnDiffText(urlParams);
                if (urlParamsViewText !== '') {
                    urlParamsViewText = '[' + urlParamsViewText + '\n]';
                }
            }
            return urlParamsViewText;
        }

        function saveDefaultUrlParams() {
            var areUrlParamsValid = urlRulesVS.validateDefaultReturnValue(vm);
            var hasChanges = !urlRulesService.areUrlParamsEquals(vm.approvedDefaultUrlParams, vm.defaultUrlParams);

            if (areUrlParamsValid && hasChanges) {
                vm.isSavingDefaultServer = true;
                urlRulesRequestService.saveDefaultUrlParams(vm.defaultUrlParams)
                    .then(function (data) {
                        vm.isSavingDefaultServer = false;
                        loadDefaultUrlParams();
                    }, function (error) {
                        vm.isSavingDefaultServer = false;
                        rulesAlertsService.failedToSave('Default URL Parameters: ', angular.isDefined(error.message.data.message) ? error.message.data.message : error.message);
                    }
                );
            }
            else if (!areUrlParamsValid){
                rulesAlertsService.failedToSave('Default Url Params', 'due to validation errors');
            }
            else if (!hasChanges) {
                rulesAlertsService.hasNotChangedWarning('Default Url Params');
            }
        }

        function saveRule() {

            var isRuleValid = rulesCommonVS.validateRule(vm, urlRulesVS);
            if (isRuleValid) {
                var jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, vm.uiModelHolder.urlParams, urlRulesBuilderService);

                if (angular.equals(beforeEditRule.id, vm.uiModelHolder.ruleName) && angular.equals(beforeEditRule.jsonRule, jsonRule)) {
                    rulesAlertsService.hasNotChangedWarning('URL Rule');
                } else {

                    if (vm.uiModelHolder.currentTemplateName !== '') {
                        jsonRule.if[0].templateName = vm.uiModelHolder.currentTemplateName;
                    } else {
                        if (angular.isDefined(jsonRule.if[0].templateName)) {
                            delete jsonRule.if[0].templateName;
                        }
                    }

                    jsonRule.if[0].id = vm.uiModelHolder.ruleName;
                    urlRulesRequestService.saveUrlRule(jsonRule.if[0], vm.uiModelHolder.ruleName)
                        .then(function(data) {
                            rulesAlertsService.successfullySaved(' \'' + vm.uiModelHolder.ruleName + '\' URL Rule.');
                            $state.go(STATES_CONSTANTS().showURLRules, {ruleName: vm.uiModelHolder.ruleName});
                        }, function(error) {
                            rulesAlertsService.failedToSave('URL Rule: \'' + vm.uiModelHolder.ruleName + '\'', angular.isDefined(error.data.message) ? error.data.message : error.message);
                            vm.isSaving = false;
                        }
                    );
                }
            }
            else {
                rulesAlertsService.failedToSave('rule', 'Due to validation errors.');
            }
        }


        function saveAsTemplate() {
            var isRuleValid = rulesCommonVS.validateRule(vm, urlRulesVS);

            if (isRuleValid) {
                var jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, vm.uiModelHolder.urlParams, urlRulesBuilderService);
                jsonRule.if[0].id = vm.uiModelHolder.ruleName;
                templatesService.saveTemplate(rulesCONST().TEMPLATES_TYPE.URL, jsonRule.if[0], vm.uiModelHolder.ruleName)
                    .then(function (data) {
                        rulesAlertsService.successfullySaved(' \'' + vm.uiModelHolder.ruleName + '\' template.');
                        $state.go(STATES_CONSTANTS().templates, {name: rulesCONST().TEMPLATES_TYPE.URL, ruleName: vm.uiModelHolder.ruleName});
                    }, function (error) {
                        rulesAlertsService.failedToSave('Template: \'' + vm.uiModelHolder.ruleName + '\'', angular.isDefined(error.message.data.message) ? error.message.data.message : error.message);
                        vm.isSaving = false;
                    }
                );
            } else {
                rulesAlertsService.failedToSave('Template', 'Due to validation errors.');
            }
        }

        function validateRuleName() {
            rulesCommonVS.validateRuleName(vm);
        }
    }

})();
