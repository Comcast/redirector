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


(function() {
    'use strict';

    angular.
        module('uxData.deciderRules').
        controller('ShowDeciderRulesController', ShowDeciderRulesController);

    ShowDeciderRulesController.$inject = ['$state', '$stateParams', '$location', '$anchorScroll', 'STATES_CONSTANTS',
        'deciderRulesRequestService', 'RULES_CONSTANTS', 'rulesAlertsService', 'authService', 'USER_PERMISSIONS',
        'dialogs', 'utilsService', 'rulesBuilderService', 'deciderRulesBuilderService'];

    function ShowDeciderRulesController ($state,$stateParams, $location, $anchorScroll, STATES_CONSTANTS,
        deciderRulesRequestService, rulesCONST, rulesAlertsService, authService, USER_PERMISSIONS,
        dialogs, utilsService, rulesBuilderService, deciderRulesBuilderService) {

        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        var DECIDER_RULE = rulesCONST().RULE_TYPE.DECIDER;

        vm.loading = true;
        vm.rules = {};
        vm.editRule = editRule;
        vm.deleteRule = deleteRule;
        vm.confirmDeleteRule = confirmDeleteRule;
        vm.exportRule = deciderRulesRequestService.exportRule;
        vm.exportAllRules = deciderRulesRequestService.exportAllRules;
        vm.isMapEmpty = utilsService.isMapEmpty;

        vm.searchSort = {
            sortBy: "id",
            sortType: "asc",
            search: ""
        };

        vm.focusedRule = angular.isDefined($stateParams.ruleName) ? $stateParams.ruleName : '';

        init();

        function init() {
            deciderRulesRequestService.getAllRules()
                .then(function(result) {
                    vm.rules = rulesBuilderService.unmarshallJSONRulesForPreview(
                        result.rules,
                        result.pendingChanges,
                        result.servicePaths,
                        DECIDER_RULE,
                        deciderRulesBuilderService);
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
            $state.go(STATES_CONSTANTS().editDeciderRule, {ruleId: ruleId.toString()});
        }

        function deleteRule(ruleId) {
            deciderRulesRequestService.deleteRule(ruleId)
                .then(function() {
                    rulesAlertsService.successfullyDeleted('path rule: \'' + ruleId + '\'');
                    init();
                }, function(reason) {
                    rulesAlertsService.failedToDelete('path rule: \'' + ruleId + '/', reason);
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
    }

})();
