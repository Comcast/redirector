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
        controller('rulesController', rulesController);

    rulesController.$inject = ['$rootScope', '$scope', '$state', '$stateParams', '$location', '$anchorScroll',
        'STATES_CONSTANTS', 'rulesService', 'rulesRequestService', 'rulesAlertsService', 'utilsService',
        'authService', 'USER_PERMISSIONS', 'RULES_CONSTANTS', 'messageService', 'dialogs', 'pathRulesBuilderService',
        'rulesBuilderService', 'LOCALSTORAGE_PAGE_NAMES'];

    function rulesController ($rootScope, $scope, $state, $stateParams, $location, $anchorScroll, STATES_CONSTANTS,
                              rulesService, rulesRequestService, rulesAlertsService, utilsService, authService,
                              USER_PERMISSIONS, rulesCONST, messageService, dialogs, pathRulesBuilderService,
                              rulesBuilderService, LOCALSTORAGE_PAGE_NAMES) {

        /* jshint validthis: true */
        var vm = this;

        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.focusedRule = angular.isDefined($stateParams.ruleName) ? $stateParams.ruleName : '';

        vm.rules = {};
        vm.editRule = editRule;
        vm.deleteRule = deleteRule;
        vm.confirmDeleteRule = confirmDeleteRule;
        vm.loading = true;

        vm.exportAllRules = rulesRequestService.exportAllRules;

        vm.isMapEmpty = utilsService.isMapEmpty;

        vm.searchSort = {
            sortBy: "id",
            sortType: "asc",
            search: ""
        };

        init();

        function init() {
            rulesService.loadRulesPathsChanges()
                .then(function(result) {
                    vm.rules = rulesBuilderService.unmarshallJSONRulesForPreview(
                        result.rules,
                        result.pendingChanges,
                        result.servicePaths,
                        PATH_RULE,
                        pathRulesBuilderService);

                    vm.loading = false;
                    //scroll to
                    if (vm.focusedRule !== '') {
                        $location.hash(vm.focusedRule);
                        $anchorScroll();
                    }
                }, function(reason) {
                    vm.loading = false;
                    rulesAlertsService.failedToLoadData('rules data');
                }
            );

        }

        function editRule(ruleId) {
            $state.go(STATES_CONSTANTS().editFlavorRule, {ruleId: ruleId.toString(), serviceName: $rootScope.currentApplication});
        }

        function deleteRule(ruleId) {
            rulesRequestService.deleteRule(ruleId)
                .then(function() {
                    rulesAlertsService.successfullyDeleted('path rule: \'' + ruleId + '\'');
                    init();
                }, function(reason) {
                    rulesAlertsService.failedToDelete('path rule: \'' + ruleId + '/', angular.isDefined(reason.message) ? reason.message : reason);
                });
        }

        function confirmDeleteRule(ruleId) {
            var msg = 'Are you sure you want to delete rule \'' + ruleId + '\' ?';
            var dlg = dialogs.confirm(msg);
            dlg.result.then(function (btn) {
                    deleteRule(ruleId);
                },
                function () {}
            );
        }

        messageService.onChangeApp($scope, function () {
            init();
        });
    }
})();
