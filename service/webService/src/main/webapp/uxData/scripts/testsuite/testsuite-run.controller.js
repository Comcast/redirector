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
        .controller('RunTestSuite', RunTestSuite);

    RunTestSuite.$inject = ['$rootScope', '$scope', '$timeout', '$log', '$state', '$stateParams', '$filter', 'dialogs',
        'ngTableParams', 'STATES_CONSTANTS', 'messageService', 'testsuiteService', 'testSuiteRequestService',
        'testsuiteAlertsService', 'validationServicetestsuite', 'utilsService'];

    /**
     * This is a controller which controls showing rule test page features
     */
    function RunTestSuite($rootScope, $scope, $timeout, $log, $state, $stateParams, $filter, $dialogs, ngTableParams,
                          STATES_CONSTANTS, messageService, testsuiteService, testSuiteRequestService, testsuiteAlertsService,
                          validationServicetestsuite, utilsService) {
        /* jshint validthis: true */
        var vm = this;

        vm.changeExpanded = changeExpanded;
        vm.findTestCaseByName = testsuiteService.findTestCaseByName;
        vm.findTestCaseIndexByName = testsuiteService.findTestCaseIndexByName;
        vm.generateDiffStringForTestResult = testsuiteService.generateDiffStringForTestResult;
        vm.goToShowPage = goToShowPage;
        vm.reRun = reRun;
        vm.testNamesToRunNext = testsuiteService.testNamesToRunNext;
        vm.testNamesToRun = testsuiteService.testNamesToRun;

        init();

        var CURRENT = 'CURRENT';
        var NEXT = 'NEXT';
        var PRETTY_PRINT_INDENT = 2;
        var TIMEOUT_500_MS = 500;

        /**
         * Main entry function
         */
        function init() {
            var runOne = angular.isDefined($stateParams.name) && $stateParams.name !== '';
            if (runOne) { //running one testcase
                initRunOne();
            } else {//running all testcases
                initRunAll();
            }
        }

        messageService.onChangeApp($scope, function (message) {
            init();
        });

        function initRunOne () {
            testSuiteRequestService.getTestCase($stateParams.name).then(
                function (data) {
                    $log.info('Got testcase ' + data.testName, data);
                    validationServicetestsuite.validateTestCase(data).then(//validating one case
                        function(){
                            vm.testCases = [data];
                            angular.isDefined(vm.tableParams) ? vm.tableParams.reload : initNgTableStructure();
                            runAllTestCasesRecursive();
                        },
                        function(reason) {
                            vm.testCases = [];
                            testsuiteAlertsService.errorValidateIncomingTest(reason);
                        });

                },
                function (reason) {
                    $log.error('Error getting testcase to run ', reason);
                    testsuiteAlertsService.errorGet(reason);
                }
            );
        }

        function initRunAll () {
            testsuiteService.getAndValidateAllTestCases().then(
                function (data) {
                    vm.testCases = data;
                    angular.isDefined(vm.tableParams) ? vm.tableParams.reload : initNgTableStructure();
                    $log.info('Got valid testcases ', data);
                    runAllTestCasesRecursive();
                },
                function (error) {
                    $log.error('Error getting test cases', error);
                }
            );
        }

        /**
         * Re-runs one test case, leaving all other ones untouched
         * @param testCase
         * @param mode
         * @returns {*}
         */
        function reRun(testCase, mode) {
            var dlg = $dialogs.wait(undefined, undefined, 0);

            $timeout(function () {
                $rootScope.$broadcast('dialogs.wait.progress', {'progress': 100});
            }, TIMEOUT_500_MS);
            testSuiteRequestService.runTestCase(testCase.testName, mode).then(
                 function (data) {
                     testCase.actual = data.actual;
                     testCase.logs = data.logs;
                     testCase.status = data.status;
                     validationServicetestsuite.validateTestCase(testCase).then(//validating one case
                            function(){
                                testCase.isResultExpanded = false;
                                $log.info('Test ran successfully while re-running', data);
                                testsuiteAlertsService.successRun(testCase.testName);
                                dlg.close();
                                vm.tableParams.reload();
                            },
                            function(reason) {
                                testsuiteAlertsService.errorValidateIncomingTest(reason);
                                $log.error('Test ', testCase, ' re-ran successfully, but there were validation errors. ', reason);
                                dlg.close();
                                vm.tableParams.reload();
                            });
                },
                function (error) {
                    $log.error('Test re-run was unsuccessful ', error);
                    addErrorLabel(testCase);
                    testsuiteAlertsService.errorRunTest(testCase.testName);
                    dlg.close();
                }
            );
        }

        /**
         * Inits view as ng-table
         */
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
                    var orderedData = params.sorting() ? $filter('orderBy')(vm.testCases, params.orderBy()) : vm.testCases;

                    var orderedDataNew = [];
                    angular.forEach(orderedData, function (testCase) {
                        for (var i = 0; i < vm.testNamesToRun.length; i++) {
                             if (testCase.testName === vm.testNamesToRun[i]){
                                 orderedDataNew.push(testCase);
                             }
                        }
                        testCase.expanded = false;
                        if (angular.isDefined(testCase.actual)) {//init view properties, which are not going to go to DS
                            testCase.expectedText = JSON.stringify(testsuiteService.arrayPropertiesToStrings(testCase.expected), null, PRETTY_PRINT_INDENT);
                            testCase.actualText = JSON.stringify(testsuiteService.arrayPropertiesToStrings(testCase.actual), null, PRETTY_PRINT_INDENT);
                        }
                    });

                    if (vm.testNamesToRun.length !=0) {
                        orderedData = orderedDataNew;
                    }
                    // use built-in angular filter
                    orderedData = params.filter() ? $filter('filter')(orderedData, params.filter()) : orderedData;

                    params.total(orderedData.length); // set total for recalc pagination
                    var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                    if (params.page() > totalPages) {
                        params.page(totalPages);
                    }
                    orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                    $defer.resolve(orderedData);
                }
            });
            //workaround for https://github.com/esvit/ng-table/issues/297
            vm.tableParams.settings().$scope = $scope;
        }


        function goToShowPage() {
            $state.go(STATES_CONSTANTS().testsuiteShow);
        }

        /**
         * Changes the displaying type of a given test case -- is it expanded or not
         * @param testCase
         */
        function changeExpanded(testCase) {
            testCase.expanded = !testCase.expanded;
        }

        function addErrorLabel (testcase) {
            testcase.error = true;
        }

        /**
         * Recursion invoker for running all tests
         */
        function runAllTestCasesRecursive() {
            $log.info('Attempting to run all tests');
            var testQueue = [];
            if (vm.testNamesToRun.length == 0) {
               testQueue = angular.copy(vm.testCases);
            } else {
                for (var i = 0; i < vm.testCases.length; i++) {
                    for (var j = 0; j < vm.testNamesToRun.length; j++) {
                        if (vm.testCases[i].testName === vm.testNamesToRun[j]) {
                            testQueue.push(vm.testCases[i]);
                        }
                    }
                }
            }
            runTestCasesRecursionBody(testQueue);
            vm.tableParams.reload();
        }

        /**
         * Recursion body for running all tests
         * @param testQueue
         */
        function runTestCasesRecursionBody(testQueue) {
            if (angular.isDefined(testQueue) && angular.isArray(testQueue) && angular.isDefined(testQueue[0])) {
                testSuiteRequestService.runTestCase(testQueue[0].testName,
                    (vm.testNamesToRunNext.indexOf(testQueue[0].testName) < 0) ? CURRENT : NEXT).
                    then(function (data) {//if ran successfully (either failed or passed)
                        $log.info('Test ran successfully', data);
                        vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)].actual = data.actual;
                        vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)].logs = data.logs;
                        vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)].status = data.status;
                        validationServicetestsuite.validateTestCase(vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)]).then(
                            function(validationSuccessMessage) {//validation passed
                                vm.tableParams.reload();
                                testQueue.splice(0, 1);
                                runTestCasesRecursionBody(testQueue);
                            },
                            function(reason) {//validation failed
                                vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)] = data;
                                vm.tableParams.reload();
                                $log.error('Cannot validate ran testcase for reason ', reason);
                                testQueue.splice(0, 1);
                                runTestCasesRecursionBody(testQueue);
                                testsuiteAlertsService.errorValidateIncomingTest(reason);
                            }
                        );
                    }, function (error) {//if run was in error
                        addErrorLabel(vm.testCases[vm.findTestCaseIndexByName(testQueue[0].testName, vm.testCases)]);
                        testsuiteAlertsService.errorRunTest(testQueue[0].testName);
                        testQueue.splice(0, 1);
                        $log.error('Test run error', error);
                        vm.tableParams.reload();
                        runTestCasesRecursionBody(testQueue);
                    });
            }
        }
    }
})();
