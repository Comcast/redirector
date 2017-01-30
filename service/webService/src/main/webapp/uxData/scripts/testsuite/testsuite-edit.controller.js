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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';

    angular
        .module('uxData.testsuite')
        .controller('EditTestSuite', EditTestSuite);

    EditTestSuite.$inject = ['$scope', 'dialogs', '$log', '$rootScope', '$state', '$stateParams', 'authService',
        'USER_PERMISSIONS', 'STATES_CONSTANTS', 'testsuiteService', 'validationServicetestsuite', 'testsuiteAlertsService',
        'stacksManagementService', 'TYPEAHEAD_DATA', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'utilsService'];

    /**
     * This is a controller which controls editing rule test page features
     */
    function EditTestSuite($scope, $dialogs, $log, $rootScope, $state, $stateParams, authService, USER_PERMISSIONS,
                           STATES_CONSTANTS, testsuiteService, validationServicetestsuite, testsuiteAlertsService,
                           stacksManagementService, TYPEAHEAD_DATA, messageService, LOCALSTORAGE_PAGE_NAMES, utilsService) {
        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.addValue = addValue;
        vm.addParameter = addParameter;
        vm.currentTestCase = {
            parameters: {
                parameter: []
            },
            expected: {appliedUrlRules:[]},
            testName: '',
            application: $rootScope.currentApplication
        };//need to have a constant address for localstorage

        vm.uiModelHolder = {
            valueToAddToCurrentParam: ''
        };

        vm.currentParam = {
            name: '',
            values: []
        };
        vm.deleteParameter = deleteParameter;
        vm.deleteValue = deleteValue;
        vm.isCurrentTestCaseValid = true;
        vm.saveTestCase = saveTestCase;
        vm.validation = validationServicetestsuite;
        vm.cancelSaving = cancelSaving;

        vm.isEditing = false;
        vm.isParameterUnique = isParameterUnique;
        vm.isValueUnique = isValueUnique;

        /**
         * All typeahead data that is used in add testcase page
         * @type {{stacks: string[], flavors: string[], ruleNames: string[], urns: (*|result.urns), protocols: (*|result.protocols)}}
         */
        vm.typeaheadData = {
            stacks: [],
            flavors: [],
            ruleNames: [],//toDo: replace stubs after rules refactoring
            urns: TYPEAHEAD_DATA().urns,
            protocols: TYPEAHEAD_DATA().protocols
        };

        vm.uiModelHolder.valueToAddToCurrentParam = '';

        function cancelSaving(testCase) {
            var dlg = $dialogs.confirm('Cancel confirmation', 'Are you sure you want to drop changes for test case: ' + testCase.testName);
            dlg.result.then(function (btn) {
                $log.info('cancelled saving test case', testCase);
                $state.go(STATES_CONSTANTS().testsuiteShow);
            });
        }

        init();

        /**
         * Main entry point
         */
        function init() {
            var name = $stateParams.name;
            loadStacks();
            loadRules();
            if (!utilsService.isEmptyString(name)) {
                vm.isEditing = true;
                testsuiteService.getAndValidateOneTestCase(name).then(
                    function (data) {
                        angular.copy(data, vm.currentTestCase);
                        if (!angular.isDefined(vm.currentTestCase.expected.appliedUrlRules)) {
                            vm.currentTestCase.expected.appliedUrlRules = [];
                        }
                    },
                    function (reason) {
                        testsuiteAlertsService.errorGet(reason);
                    }
                );
            }
        }

        function clearCurrentParameter() {
            vm.currentParam.name = '';
            vm.currentParam.values.splice(0, vm.currentParam.values.length);
        }

        /**
         * Adds parameter along with its values to the model ({@link currentTestCase}), clearing the view.
         */
        function addParameter() {
            vm.currentTestCase.parameters.parameter.push(angular.copy(vm.currentParam));
            clearCurrentParameter();
            $scope.add_testcase.parameterName.$pristine = true;
            $scope.add_testcase.parameterValue.$pristine = true;
        }

        function isParameterUnique (parameterName) {
            if (angular.isDefined(vm.currentTestCase.parameters)) {
                return findParameterIndexByParameterName(parameterName, vm.currentTestCase.parameters.parameter) < 0;
            }
        }

        /**
         * Adds a value to the current parameter ({@link currentParam}), clearing its view
         */
        function addValue(value) {
            if (validationServicetestsuite.isParameterValueValid(value)) {
                vm.currentParam.values.push(value);
                $log.debug('Added value ', value);
                vm.uiModelHolder.valueToAddToCurrentParam = '';
                $scope.add_testcase.parameterValue.$pristine = true;
            } else {
                $log.error('Attempt to add invalid value ' + value);
            }
        }

        function isValueUnique (value) {
            return vm.currentParam.values.indexOf(value) < 0;
        }

        function deleteValue(valueName) {
            vm.currentParam.values.splice(vm.currentParam.values.indexOf(valueName), 1);
            $log.debug('Value ' + valueName +' is deleted');
        }

        function deleteParameter(parameterName) {
            vm.currentTestCase.parameters.parameter.splice(findParameterIndexByParameterName(parameterName, vm.currentTestCase.parameters.parameter), 1);
            $log.debug('Parameter ' + parameterName +' is deleted');
        }

        function findParameterIndexByParameterName(parameterName, parameters) {
            if (angular.isUndefined (parameters)) {
                parameters = [];
            }
            for (var i = 0; i < parameters.length; i++) {
                if (parameters[i].name === parameterName) {
                    return i;
                }
            }
            return -1;
        }

        function saveTestCase(testCase) {
            testsuiteService.validateAndSaveTestCase(testCase, vm.isEditing).then(function (data) {
                $log.info('Saved test case ', data);
                $state.go(STATES_CONSTANTS().testsuiteShow);
            }, function (error) {
                $log.error('Error saving test case ', error.message);
            });

        }

        function loadRules() {
            //toDo: when rules refactoring will be finished
        }

        messageService.onChangeApp($scope, function (message) {
            if (vm.isEditing) {
                $state.go(STATES_CONSTANTS().testsuiteAdd);
            }
            loadStacks();
            loadRules();
        });

        function loadStacks() {
            var stacksForMgmtService = [];
            var stacksAndFlavorsForMgmtService = [];
            vm.typeaheadData.stacks.splice(0, vm.typeaheadData.stacks.length);
            vm.typeaheadData.flavors.splice(0, vm.typeaheadData.flavors.length);
            stacksManagementService.getServicePathsFromDS([], stacksForMgmtService, stacksAndFlavorsForMgmtService).then(
                function (data) {
                    vm.typeaheadData.stacks = stacksForMgmtService;
                    angular.forEach(stacksForMgmtService, function (stackName) {
                        var stackWithFlavors = stacksAndFlavorsForMgmtService[stackName];
                        angular.forEach(stackWithFlavors.flavors, function (flavor) {
                            if (vm.typeaheadData.flavors.indexOf(flavor.name) === -1) {
                                vm.typeaheadData.flavors.push(flavor.name);
                            }
                        });
                    });
                    $log.info('Got stacks ', vm.typeaheadData.stacks, ' and flavors ', vm.typeaheadData.flavors);
                },
                function (error) {
                    $log.error('Cannot get stacks', error);
                });
        }

    }
})();
