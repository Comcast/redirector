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
        controller('urlRulesShowController', urlRulesShowController);

    urlRulesShowController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$location', '$anchorScroll',
        'STATES_CONSTANTS', 'authService', 'USER_PERMISSIONS', 'utilsService', 'urlRulesService', 'urlRulesRequestService',
        'rulesAlertsService', 'RULES_CONSTANTS', 'messageService', 'dialogs', 'urlRulesBuilderService', 'rulesBuilderService',
        'urlRulesValidationService', 'LOCALSTORAGE_PAGE_NAMES'];

    function urlRulesShowController ($rootScope, $scope, $state, $stateParams, $location, $anchorScroll,
        STATES_CONSTANTS, authService, USER_PERMISSIONS, utils, urlRulesService, urlRulesRequestService,
        rulesAlertsService, rulesCONST, messageService, dialogs, urlRulesBuilderService, rulesBuilderService, urlRulesVS, LOCALSTORAGE_PAGE_NAMES) {

        /* jshint validthis: true */
        var vm = this;

        var URL_RULE = rulesCONST().RULE_TYPE.URL;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.hasPermissions = utils.hasPermissions;
        vm.loading = true;

        vm.focusedRule = angular.isDefined($stateParams.ruleName) ? $stateParams.ruleName : '';

        vm.approvedDefaultUrlParams ={};
        vm.defaultUrlParams = {hasChanges: false};
        vm.defaultUrlParamsChanges = {};
        vm.defaultUrlParamsValidationData = {};
        vm.defaultUrlParamsToggle = false;
        vm.isSavingDefaultServer = false;

        vm.urlRules = {};
        vm.editUrlRule = editUrlRule;
        vm.deleteUrlRule = deleteUrlRule;
        vm.isMapEmpty = utils.isMapEmpty;
        vm.confirmDeleteRule = confirmDeleteRule;
        vm.onDefaultUrlParamsChanged = onDefaultUrlParamsChanged;
        vm.saveDefaultUrlParams = saveDefaultUrlParams;
        vm.getUrlParamsDiffViewText = getUrlParamsDiffViewText;

        vm.searchSort = {
            sortBy: "id",
            sortType: "asc",
            search: ""
        };

        vm.exportAllURLRules = urlRulesRequestService.exportAllUrlRules;
        init();

        function init() {
            loadDefaultUrlParams();
            urlRulesService.loadUrlRulesPathsChanges()
                .then(function(result) {
                    vm.urlRules = rulesBuilderService.unmarshallJSONRulesForPreview(
                        result.urlRules,
                        result.pendingChanges,
                        result.servicePaths,
                        URL_RULE, urlRulesBuilderService);

                    vm.loading = false;
                    //scroll to
                    if (vm.focusedRule !== '') {
                        $location.hash(vm.focusedRule);
                        $anchorScroll();
                    }
                }, function(reason) {
                    vm.loading = false;
                    rulesAlertsService.failedToLoadData('urlRules data');
                }
            );
        }

        function editUrlRule(ruleId) {
            $state.go(STATES_CONSTANTS().editURLRule, {ruleId: ruleId.toString(), serviceName: $rootScope.currentApplication});
        }

        function deleteUrlRule(ruleId) {
            urlRulesRequestService.deleteUrlRule(ruleId)
                .then(function() {
                    rulesAlertsService.successfullyDeleted('URL Rule: \'' + ruleId + '\'');
                    init();
                }, function(reason) {
                    rulesAlertsService.failedToDelete('URL Rule: \'' + ruleId + '/', angular.isDefined(reason.message) ? reason.message : reason);
                }
            );
        }

        function confirmDeleteRule(ruleId) {
            var msg = 'Are you sure you want to delete rule \'' + ruleId + '\' ?';
            var dlg = dialogs.confirm(msg);
            dlg.result.then(function (btn) {

                deleteUrlRule(ruleId);
                },
                function () {}
            );
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

        function onDefaultUrlParamsChanged() {
            urlRulesVS.validateDefaultReturnValue(vm);
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
                    }, function (reason) {
                        vm.isSavingDefaultServer = false;
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

        messageService.onChangeApp($scope, function () {
            init();
        });
    }
})();
