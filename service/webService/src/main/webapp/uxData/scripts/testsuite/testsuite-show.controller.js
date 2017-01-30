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
        .controller('ShowTestSuite', ShowTestSuite);

    ShowTestSuite.$inject = ['$log', '$state', '$scope', '$filter', 'dialogs', 'ngTableParams', 'authService', 'USER_PERMISSIONS',
        'STATES_CONSTANTS', 'testSuiteRequestService', 'testsuiteService', 'testsuiteAlertsService', 'utilsService', 'messageService'];

    /**
     * This is a controller which controls showing rule test page features
     */
    function ShowTestSuite($log, $state, $scope, $filter, $dialogs, ngTableParams, authService, USER_PERMISSIONS,
                           STATES_CONSTANTS, testSuiteRequestService, testsuiteService, testsuiteAlertsService, utilsService, messageService) {
        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.changeRunNextForTestCase = changeRunNextForTestCase;
        vm.toggleRunForTestCase = toggleRunForTestCase;
        vm.deleteTestCase = deleteTestCase;
        vm.editTestCase = editTestCase;
        vm.exportTestCase = testSuiteRequestService.exportTestCase;
        vm.exportTestCases = testSuiteRequestService.exportTestCases;
        vm.getSimpleViewForTestCase = testsuiteService.getSimpleViewForTestCase;
        vm.getXmlViewForTestCase = testsuiteService.getXmlViewForTestCase;
        vm.ngTableData = [];
        vm.runOne = runOne;
        vm.runAll = runAll;
        vm.switchMode = switchMode;
        vm.selectAll = selectAll;
        vm.deselectAll = deselectAll;
        vm.allSelected = false;
        vm.searchCriteria = "";
        vm.filterSuiteData = filterSuiteData;

        vm.testNamesToRunNext = testsuiteService.testNamesToRunNext;
        vm.testNamesToRun = testsuiteService.testNamesToRun;
        vm.testCases = [];
        vm.loading = false;

        vm.search = {
            $: ''
        };

        function filterSuiteData() {
            vm.tableParams.$params.filter = vm.search;
            vm.tableParams.reload();
        }

        loadData();

        /**
         * Main entry point
         */
        function loadData() {
            vm.loading = true;
            vm.allSelected = false;
            vm.testNamesToRunNext.splice(0, vm.testNamesToRunNext.length);
            vm.testNamesToRun.splice(0, vm.testNamesToRun.length);

            testsuiteService.getAndValidateAllTestCases().then(
                function (data) {
                    $log.info('Got test cases: ', data);
                    vm.testCases = data;
                    if (angular.isArray(vm.testCases) && vm.testCases.length > 0) {
                        angular.forEach(vm.testCases, function (testCase) {//init additional view properties
                            testCase.run = false;
                            testCase.showSimpleView = true;
                        });
                    }
                    angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initNgTableStructure();
                    vm.loading = false;
                },
                function (error) {
                    $log.error('Error while getting all test cases', error);
                    vm.loading = false;
                }
            );
        }

        function initNgTableStructure() {
            vm.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 10,          // count per page
                sorting: {
                    name: 'asc'     // initial sorting
                }
            }, {
                total: 0, // length of data
                getData: function ($defer, params) {
                    if (angular.isArray(vm.testCases) && vm.testCases.length > 0) {
                        mergeTestCasesWithRunProperties (vm.tableParams.data, vm.testCases);
                        updateTestNamesToRun();
                        var orderedData = params.sorting() ? $filter('orderBy')(vm.testCases, params.orderBy()) : vm.testCases;


                        // use built-in angular filter
                        orderedData = params.filter() ? $filter('filter')(orderedData, params.filter()) : orderedData;

                        if (angular.isDefined(orderedData)) {
                            params.total(orderedData.length); // set total for recalc pagination
                            var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                            if (params.page() > totalPages) {
                                params.page(totalPages);
                            }
                            orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                            vm.allSelected = isAllSelected(orderedData);
                            vm.loading = false;
                            $defer.resolve(orderedData);
                        } else {
                            vm.loading = false;
                            $defer.resolve([]);
                        }
                    } else {
                        $defer.resolve([]);
                    }
                }
                /**/
            });
            //workaround for https://github.com/esvit/ng-table/issues/297
            vm.tableParams.settings().$scope = $scope;
        }

        function mergeTestCasesWithRunProperties (testCasesFromTable, baseTestCases) {
            if (angular.isUndefined(testCasesFromTable)) {
                return;
            }
            for (var i = 0; i < baseTestCases.length; i++) {
                for (var j = 0; j < testCasesFromTable.length; j++) {
                    if (baseTestCases[i].testName === testCasesFromTable[j].testName) {
                        baseTestCases[i].run = testCasesFromTable[j].run;
                    }
                }
            }
        }

        function deleteTestCase(testName) {
            var dlg = $dialogs.confirm('Delete confirmation', 'Are you sure you want to Delete test case: ' + testName);
            dlg.result.then(function (btn) {
                testsuiteService.deleteTestCaseByName(testName)
                    .then(function (data) {
                        testsuiteAlertsService.successDelete(testName);
                        vm.testCases.splice(testsuiteService.findTestCaseIndexByName(testName, vm.testCases), 1);
                        vm.tableParams.reload();
                        updateTestNamesToRun();
                    }, function (reason) {
                        $dialogs.notify('Failed to delete test case: ' + testName, reason.data.message);
                        vm.isSaving = false;
                    }, function (btn) {
                        //click cancel
                    });
            });
        }

        messageService.onChangeApp($scope, function (message) {
            loadData();
        });

        function editTestCase(testCase) {
            $log.info('going to edit testCase', testCase);
            $state.go(STATES_CONSTANTS().testsuiteEdit, {name: testCase.testName});
        }

        function runOne(testCase) {
            $state.go(STATES_CONSTANTS().testsuiteRun, {name: testCase.testName});
        }

        function changeRunNextForTestCase(testName, runNext) {
            if (runNext) {
                vm.testNamesToRunNext.splice(vm.testNamesToRunNext.indexOf(testName), 1);
            } else {
                vm.testNamesToRunNext.push(testName);
            }
        }

        function selectAll() {
            vm.allSelected = true;
            vm.testNamesToRun.splice(0, vm.testNamesToRun.length);

            var filteredTestCases = $filter('filter')(vm.tableParams.data, vm.search.$);

            /*for (var i = 0; i < vm.testCases.length; i++) {
                vm.testCases[i].run = false;
            }*/

            for (var i = 0; i < filteredTestCases.length; i++) {
                filteredTestCases[i].run = true;
                vm.testNamesToRun.push(filteredTestCases[i].testName);
            }
        }

        function deselectAll() {
            vm.allSelected = false;
            vm.testNamesToRun.splice(0, vm.testNamesToRun.length);
            for (var i = 0; i < vm.tableParams.data.length; i++) {
                vm.tableParams.data[i].run = false;
            }
            mergeTestCasesWithRunProperties(vm.tableParams.data, vm.testCases);
            updateTestNamesToRun();
        }

        function toggleRunForTestCase(testName, runNext) {
            vm.allSelected = isAllSelected(vm.tableParams.data);
            if (!runNext) {
                vm.testNamesToRun.splice(vm.testNamesToRun.indexOf(testName), 1);
                updateTestNamesToRun();
            } else {
                vm.testNamesToRun.push(testName);
            }
        }

        function isAllSelected (data) {
            for (var i = 0; i < data.length; i++) {
                if (!data[i].run) {
                    return false;
                }
            }
            return true;
        }

        function switchMode(testCase) {
            testCase.showSimpleView = !testCase.showSimpleView;
        }

        function updateTestNamesToRun() {
            vm.testNamesToRun.splice(0, vm.testNamesToRun.length);
            for (var i = 0 ; i < vm.testCases.length; i++) {
                if (vm.testCases[i].run === true) {
                   vm.testNamesToRun.push(vm.testCases[i].testName);
                }
            }
        }

        function runAll() {
            vm.testNamesToRun.splice(0, vm.testNamesToRun.length);
            updateTestNamesToRun();
            $state.go(STATES_CONSTANTS().testsuiteRun);
        }

    }
})();
