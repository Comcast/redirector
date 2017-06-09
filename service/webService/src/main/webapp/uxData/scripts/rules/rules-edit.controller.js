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
        module('uxData.rules').
        controller('RulesEditController', RulesEditController);

    RulesEditController.$inject = ['$state', '$q', '$stateParams', '$rootScope', '$scope', 'STATES_CONSTANTS',
        'RULES_CONSTANTS', 'EXPRESSION_CONSTANTS', 'rulesService', 'rulesRequestService', 'rulesEditService', 'rulesBuilderService', 'RulesCommonValidationService',
        'rulesValidationService', 'authService', 'USER_PERMISSIONS', 'utilsService', 'SERVER_CONSTANTS', 'rulesAlertsService', 'templatesService', 'LOCALSTORAGE_PAGE_NAMES',
        'pathRulesBuilderService', 'messageService'];

    function RulesEditController($state, $q, $stateParams, $rootScope, $scope, STATES_CONSTANTS,
                                 rulesCONST, expCONST, rulesService, rulesRequestService, rulesEditService, rulesBuilderService, rulesCommonVS, pathRulesVS, authService,
                                 USER_PERMISSIONS, utils, serverCONST, rulesAlertsService, templatesService, LOCALSTORAGE_PAGE_NAMES, pathRulesBuilderService, messageService) {

        /* jshint validthis: true */
        var vm = this;

        var LEFT = rulesCONST().LEFT;
        var RIGHT = rulesCONST().RIGHT;
        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;
        var ISEMPTY = expCONST().OPERATOR_TYPE.ISEMPTY;
        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var EDIT_STATE = LOCALSTORAGE_PAGE_NAMES().rulesEdit;
        var ADD_STATE = LOCALSTORAGE_PAGE_NAMES().rulesAdd;
        var beforeEditRule = {};

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.hasPermissions = utils.hasPermissions;

        vm.existingRuleIDs = {};
        vm.existingTemplatesIDs = {};

        // note that exprValidationData should be synchronized with rawExpressions
        // if value added/removed from rawExpression this change should be reflected in exprValidationData
        vm.exprValidationData = []; // {left: {duplicates: {}, nameErrorMsg: '', valueErrorMsg: '', generalErrorMsg: ''}, right: {same as left}}
        vm.serverValidationData = [];

        vm.uiModelHolder = {
            ruleName: angular.isDefined($stateParams.ruleId) ? $stateParams.ruleId : '',
            currentTemplate: {},
            editMode: angular.isDefined($stateParams.ruleId),// there can be two states: adding new rule and editing rule
            rawExpressions: [],
            servers: [],
            countDownTime: 15,
            enablePrivate: true,
            currentTemplateName: ''
        };

        vm.servicePaths = {};
        vm.ruleNameError = '';
        vm.isSaving = false;

        vm.templateRules = {};

        vm.saveRule = saveRule;
        vm.saveAsTemplate = saveAsTemplate;
        vm.addEmptyServer = addEmptyServer;
        vm.removeServer = removeServer;
        vm.addExpression = addExpression;
        vm.addEmptyExpression = addEmptyExpression;
        vm.removeExpression = removeExpression;
        vm.updateExpressions = updateExpressions;
        vm.cancelEditRule = cancelEditRule;
        vm.onExprNameChanged = onExprNameChanged;
        vm.onExprOperatorChanged = onExprOperatorChanged;
        vm.onExprValueAdded = onExprValueAdded;
        vm.onExprValueRemoved = onExprValueRemoved;
        vm.onExprValueChanged = onExprValueChanged;
        vm.onExprValueTypeChanged = onExprValueTypeChanged;
        vm.onServerPathChanged = onServerPathChanged;
        vm.onServerUrlChanged = onServerUrlChanged;
        vm.onServerQueryChanged = onServerQueryChanged;
        vm.onServerEditModeChanged = onServerEditModeChanged;
        vm.onServerWhitelistedChanged = onServerWhitelistedChanged;
        vm.validateRuleName = validateRuleName;
        vm.onTemplateChanged = onTemplateChanged;
        vm.hasError = utils.isDefinedAndNotEmpty;

        initialize();

        messageService.onChangeApp($scope, function (message) {
            cancelEditRule();
            vm.servicePaths = {};
            vm.uiModelHolder.servers = [];
            vm.serverValidationData = [];
            initialize();
        });

        function initialize() {

            rulesService.loadServicePaths()
                .then(function (data) {
                    angular.extend(vm.servicePaths, data.servicePaths);
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('service paths');
                }
            );

            loadAllRuleIds();
            loadTemplatesIds();

            if (vm.uiModelHolder.editMode) {
                rulesService.loadRuleAndTemplates(vm.uiModelHolder.ruleName)
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
                rulesService.loadRulesAndTemplates()
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
            var ruleExpressionsAndServers = rulesBuilderService.unmarshallJSONRule(rule, pathRulesBuilderService);

            if (utils.isDefinedAndNotEmpty(rule.templateName)) {

                // remove all expressions of template
                var templateExpressions = rulesBuilderService.unmarshallJSONRule(vm.templateRules[rule.templateName], pathRulesBuilderService);
                angular.extend(ruleExpressionsAndServers, getOnlyRuleExpressions(ruleExpressionsAndServers, templateExpressions));

                // init form without template
                initFormFromBuilder(ruleExpressionsAndServers, ruleId);

                // now set template
                vm.uiModelHolder.currentTemplateName = rule.templateName;
                setTemplate();
            }
            else {
                initFormFromBuilder(ruleExpressionsAndServers, ruleId);
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
            var templateExpressionsAndServers = rulesBuilderService.unmarshallJSONRule(vm.templateRules[vm.uiModelHolder.currentTemplateName], pathRulesBuilderService);

            angular.forEach(templateExpressionsAndServers.expressions.reverse(), function (templateExpression, index) {
                addExpression(templateExpression, true);
                vm.uiModelHolder.rawExpressions[0].disabled = true;
            });
        }

        function removeTemplate() {
            var index = 0;
            while (index < vm.uiModelHolder.rawExpressions.length) {
                var expression = vm.uiModelHolder.rawExpressions[index];
                if (expression.disabled) {
                    removeExpression(index);
                } else {
                    index++;
                }
            }
        }

        function initFormFromBuilder(expressionsAndServers, ruleId) {
            vm.uiModelHolder.ruleName = ruleId;
            vm.uiModelHolder.editMode = true;
            vm.uiModelHolder.rawExpressions = angular.copy(expressionsAndServers.expressions);
            vm.uiModelHolder.servers = angular.copy(expressionsAndServers.returnStatement.servers);
            vm.uiModelHolder.countDownTime = expressionsAndServers.countDownTime;
            vm.uiModelHolder.enablePrivate = angular.equals(expressionsAndServers.enablePrivate, 'true');

            initValidationData();
        }

        function createRuleSnapshotBeforeEdit() {
            beforeEditRule.id = vm.uiModelHolder.ruleName;
            var returnObject = {
                servers: vm.uiModelHolder.servers,
                enablePrivate: vm.uiModelHolder.enablePrivate,
                countDownTime: vm.uiModelHolder.countDownTime,
                ruleName: vm.uiModelHolder.ruleName
            };
            beforeEditRule.jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, returnObject, pathRulesBuilderService);
        }

        function getOnlyRuleExpressions(allExpressions, templateExpressions) {
            var onlyRuleExpressionsAndServers = {};
            onlyRuleExpressionsAndServers.expressions = [];

            angular.forEach(allExpressions.expressions, function (ruleExpr, index) {
                var exprOfTemplate = false;
                angular.forEach(templateExpressions.expressions, function (templateExpr, index) {
                    if (angular.equals(templateExpr, ruleExpr)) {
                        exprOfTemplate = true;
                    }
                });
                if (!exprOfTemplate) {
                    onlyRuleExpressionsAndServers.expressions.push(ruleExpr);
                }
            });

            return onlyRuleExpressionsAndServers;
        }

        function onTemplateChanged() {
            if (utils.isDefinedAndNotEmpty(vm.uiModelHolder.currentTemplateName)) {
                setTemplate();
            }
            else {
                removeTemplate();
            }
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

            angular.forEach(vm.uiModelHolder.servers, function (server, index) {
                vm.serverValidationData.push({});
            });
        }

        function loadAllRuleIds() {
            rulesService.loadAllRuleIds(rulesCONST().RULE_TYPE.PATH)
                .then(function (data) {
                    vm.existingRuleIDs = angular.copy(data);
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('rules names');
                });
        }

        function loadTemplatesIds() {
            templatesService.loadAllTemplateIds(rulesCONST().TEMPLATES_TYPE.PATH).then(function (data) {
                vm.existingTemplatesIDs = angular.copy(data);
            }, function () {
                rulesAlertsService.failedToLoadData('templatesIds');
            });
        }

//=====================================Callbacks for expression and server changes====================================//

        function addEmptyServer() {
            var server = {editMode: serverCONST().EXP_EDIT_MODE.SIMPLE, path: '', isNonWhitelisted: false};
            addServer(server, false)
        }

        function addServer(server, pushToTheFront) {
            if (!angular.isDefined(vm.uiModelHolder.countDownTime) || isNaN(vm.uiModelHolder.countDownTime)) {
                vm.uiModelHolder.countDownTime = 15;
                vm.uiModelHolder.enablePrivate = true;
            }

            if (pushToTheFront) {
                vm.uiModelHolder.servers.unshift(server);
                vm.serverValidationData.unshift({});
            }
            else {
                vm.uiModelHolder.servers.push(server);
                vm.serverValidationData.push({});
            }
        }

        function removeServer(index) {
            vm.uiModelHolder.servers.splice(index, 1);
            vm.serverValidationData.splice(index, 1);
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
            var expr = rulesEditService.createExpression(expression[LEFT].expName, expression[LEFT].expType, expression[LEFT].expOperator, expression[LEFT].expValues, LEFT, expression[LEFT].negation);
            validationExprObj[LEFT] = {duplicates: {}, valueErrorMsg: []};

            if (rulesEditService.isBinary(expression.type)) {
                var rightOperand = rulesEditService.createExpression(expression[RIGHT].expName, expression[RIGHT].expType, expression[RIGHT].expOperator, expression[RIGHT].expValues, RIGHT, expression[RIGHT].negation);
                expr = rulesEditService.createBinaryExpression(expr, rightOperand, expression.type);
                validationExprObj[RIGHT] = {duplicates: {}, valueErrorMsg: []};
            }

            if (pushToTheFront) {
                vm.exprValidationData.unshift(validationExprObj);
                vm.uiModelHolder.rawExpressions.unshift(expr);
            }
            else {
                vm.exprValidationData.push(validationExprObj);
                vm.uiModelHolder.rawExpressions.push(expr);
            }
            pathRulesVS.validateDuplicatesOfAllExpressions(vm.uiModelHolder.rawExpressions, vm.exprValidationData);
        }

        function removeExpression(index) {
            vm.uiModelHolder.rawExpressions.splice(index, 1);
            vm.exprValidationData.splice(index, 1);
            pathRulesVS.revalidateDuplicatesOnRemove(index, vm.uiModelHolder.rawExpressions, vm.exprValidationData);
        }

        function cancelEditRule() {
            clearModel();
            vm.uiModelHolder.currentTemplate = {};
            if (vm.uiModelHolder.editMode) {
                vm.uiModelHolder.editMode = false;
                if (angular.isDefined($state.previousState) && $state.previousState.name === STATES_CONSTANTS().showFlavorRules) {
                    $state.go($state.previousState.name, $state.previousParams);
                } else {
                    $state.go(STATES_CONSTANTS().addNewFlavorRule, {
                        ruleId: vm.uiModelHolder.ruleName,
                        serviceName: $rootScope.currentApplication
                    });
                }
            }
        }

        function clearModel() {
            vm.uiModelHolder.currentTemplateName = '';
            vm.uiModelHolder.rawExpressions.splice(0, vm.uiModelHolder.rawExpressions.length);
            vm.uiModelHolder.ruleName = '';
            vm.ruleNameError = '';
            vm.uiModelHolder.servers.splice(0, vm.uiModelHolder.servers.length);
            vm.uiModelHolder.countDownTime = 15;
            vm.uiModelHolder.enablePrivate = true;
            vm.exprValidationData.splice(0, vm.exprValidationData.length);
            vm.serverValidationData.splice(0, vm.serverValidationData.length);
            vm.serverValidationData.push({});
            vm.isSaving = false;
        }

//=====================================Callbacks for expression and server changes====================================//

        function onExprNameChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            pathRulesVS.validateDuplicatedRelationalExpressions(vm.uiModelHolder.rawExpressions, vm.exprValidationData, index, side);
            rulesCommonVS.validateExprName(vm.uiModelHolder.rawExpressions[index][side], index, side, vm);
        }

        function onExprValueChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            rulesCommonVS.validateExprValue(newValues[changedValueIndex], vm.uiModelHolder.rawExpressions[index][side], index, side, changedValueIndex, vm);
        }

        function onExprOperatorChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            vm.exprValidationData[index][side].valueErrorMsg = [''];
            rulesCommonVS.validateExprName(vm.uiModelHolder.rawExpressions[index][side], index, side, vm);
            pathRulesVS.validateDuplicatedRelationalExpressions(vm.uiModelHolder.rawExpressions, vm.exprValidationData, index, side);
        }

        function onExprValueTypeChanged(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue);
            rulesCommonVS.validateExprValue(newValues[changedValueIndex], vm.uiModelHolder.rawExpressions[index][side], index, side, changedValueIndex, vm);
        }

        function onExprValueAdded(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.push('');
        }

        function onExprValueRemoved(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.exprValidationData[index][side].valueErrorMsg.splice(changedValueIndex, 1);
        }

        function updateExpressions(index, side, newName, newOperator, newValues, changedValueIndex, newValueType, newNegationValue) {
            vm.uiModelHolder.rawExpressions[index][side].expName = newName;
            vm.uiModelHolder.rawExpressions[index][side].expOperator = newOperator;
            vm.uiModelHolder.rawExpressions[index][side].negation = newNegationValue;
            vm.uiModelHolder.rawExpressions[index][side].expValues = newValues;
            vm.uiModelHolder.rawExpressions[index][side].expType = newValueType;
        }

        function onServerPathChanged(index, path) {
            vm.uiModelHolder.servers[index].path = path;
            pathRulesVS.validateServer(index, vm);
        }

        function onServerUrlChanged(index, url) {
            vm.uiModelHolder.servers[index].url = url;
            pathRulesVS.validateServer(index, vm);
        }

        function onServerQueryChanged(index, query) {
            vm.uiModelHolder.servers[index].query = query;
        }

        function onServerEditModeChanged(index, editMode) {
            vm.uiModelHolder.servers[index].editMode = editMode;
        }

        function onServerWhitelistedChanged(index, isNonWhitelisted) {
            vm.uiModelHolder.servers[index].isNonWhitelisted = isNonWhitelisted;
        }

        function updateServersUrls() {
            angular.forEach(vm.uiModelHolder.servers, function (server, index) {
                if (angular.equals(server.editMode, SIMPLE)) {
                    server.url = utils.createUrl();
                }
            });
        }

//====================================================================================================================//

        function saveRule() {

            updateServersUrls();

            var isRuleValid = rulesCommonVS.validateRule(vm, pathRulesVS);

            if (isRuleValid) {
                var returnObject = {
                    servers: vm.uiModelHolder.servers,
                    enablePrivate: vm.uiModelHolder.enablePrivate,
                    countDownTime: vm.uiModelHolder.countDownTime,
                    ruleName: vm.uiModelHolder.ruleName
                };
                var jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, returnObject, pathRulesBuilderService);

                if (angular.equals(beforeEditRule.id, vm.uiModelHolder.ruleName) && angular.equals(beforeEditRule.jsonRule, jsonRule)) {
                    rulesAlertsService.hasNotChangedWarning('Flavor Rule');
                }
                else {

                    if (vm.uiModelHolder.currentTemplateName !== '') {
                        jsonRule.if[0].templateName = vm.uiModelHolder.currentTemplateName;
                    } else {
                        if (angular.isDefined(jsonRule.if[0].templateName)) {
                            delete jsonRule.if[0].templateName;
                        }
                    }

                    jsonRule.if[0].id = vm.uiModelHolder.ruleName;
                    rulesRequestService.saveRule(jsonRule.if[0], vm.uiModelHolder.ruleName)
                        .then(function (data) {
                            rulesAlertsService.successfullySaved(' \'' + vm.uiModelHolder.ruleName + '\' rule.');
                            $state.go(STATES_CONSTANTS().showFlavorRules, {ruleName: vm.uiModelHolder.ruleName});
                        }, function (error) {
                            rulesAlertsService.genericError(error.data.message);
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
            updateServersUrls();
            if (angular.isUndefined(vm.uiModelHolder.currentTemplateName)) {
                vm.uiModelHolder.currentTemplateName = '';
            }
            var isRuleValid = rulesCommonVS.validateRule(vm, pathRulesVS, true);

            if (isRuleValid) {
                var returnObject = {
                    servers: vm.uiModelHolder.servers,
                    enablePrivate: vm.uiModelHolder.enablePrivate,
                    countDownTime: vm.uiModelHolder.countDownTime,
                    ruleName: vm.uiModelHolder.ruleName
                };
                var jsonRule = rulesBuilderService.marshallRuleToJSON(vm.uiModelHolder.rawExpressions, returnObject, pathRulesBuilderService);
                jsonRule.if[0].id = vm.uiModelHolder.ruleName;
                templatesService.saveTemplate(rulesCONST().TEMPLATES_TYPE.PATH, jsonRule.if[0], vm.uiModelHolder.ruleName)
                    .then(function (data) {
                        rulesAlertsService.successfullySaved(' \'' + vm.uiModelHolder.ruleName + '\' template.');
                        $state.go(STATES_CONSTANTS().templates, {
                            name: rulesCONST().TEMPLATES_TYPE.PATH,
                            ruleName: vm.uiModelHolder.ruleName
                        });
                    }, function (error) {
                        rulesAlertsService.failedToSave('Template: \'' + vm.uiModelHolder.ruleName + '\'', angular.isDefined(error.data.message) ? error.data.message : error.message);
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
