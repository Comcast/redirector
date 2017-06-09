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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';

    angular.
        module('uxData.rules').
        controller('RulesImportController', RulesImportController);

    RulesImportController.$inject = ['$scope', 'RulesImportService',
        'utilsService', 'authService', 'USER_PERMISSIONS', 'RULES_CONSTANTS',
        'messageService'];

    function RulesImportController ($scope,RulesImportService,
                              utilsService, authService, USER_PERMISSIONS, rulesCONST,
                               messageService) {

        /* jshint validthis: true */
        var vm = this;

        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.rulesFromFile = {};

        vm.loading = false;
        vm.getFile = getFile;
        vm.validateDuplicatesAndImportOneRule = validateDuplicatesAndImportOneRule;

        vm.isMapEmpty = utilsService.isMapEmpty;
        vm.importAll = importAll;


        function getFile(fileName) {
            RulesImportService.getFile(fileName, PATH_RULE).then(
                function(data) {
                    vm.rulesFromFile = data;
                }
            );
        }

        function validateDuplicatesAndImportOneRule(data, ruleName) {
            RulesImportService.validateDuplicatesAndImportOneRule(data, ruleName, vm.rulesFromFile, PATH_RULE);
        }

        function importAll () {
            RulesImportService.importAll(vm.rulesFromFile, PATH_RULE);
        }

        messageService.onChangeApp($scope, function (message) {
            angular.copy({}, vm.rulesFromFile);
        });
    }
})();
