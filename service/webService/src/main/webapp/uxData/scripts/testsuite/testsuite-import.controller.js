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
        .controller('ImportTestSuite', importTestSuite);

    importTestSuite.$inject = ['$log',
        '$filter', 'ngTableParams', 'testsuiteService', 'testsuiteAlertsService', 'importService', 'validationServicetestsuite'];

    /**
     * This is a controller which controls importing rule test page features
     */
    function importTestSuite($log, $filter, ngTableParams, testsuiteService, testsuiteAlertsService, importService, validationServicetestsuite) {
        /* jshint validthis: true */
        var vm = this;

        vm.changeExpanded = changeExpanded;
        vm.findTestCaseByName = testsuiteService.findTestCaseByName;
        vm.getSimpleViewForTestCase = testsuiteService.getSimpleViewForTestCase;
        vm.getFile = getFile;
        vm.importOne = importOne;
        vm.importAll = importAll;
        vm.loadTestCases = testsuiteService.loadTestCases;
        vm.title = 'Choose a file to import';
        vm.testCasesFromFile = [];


        var findTestCaseIndexByName = testsuiteService.findTestCaseIndexByName;

        /**
         * The 'open file' button handler
         * @param fileName
         */
        function getFile(fileName) {
            vm.showFooter = false;
            importService.openFile(fileName, null, this).then(function (result) {
                $log.info('File ' +  fileName + ' is read successfully');
                getTestCasesFromFile(result);
            }, function (reason) {
                $log.error('File is not read: ' + reason.message);
                testsuiteAlertsService.errorGetFile();
            });
        }

        /**
         * validates and adds test cases from an already read file
         * @param data
         */
        function getTestCasesFromFile(data) {
            $log.info('Trying to get test cases from the following structure: ', data);
            var testCaseCandidate = JSON.parse(data);
            if (angular.isDefined(testCaseCandidate.testCase)) { //multiple test cases
                var testCasesAfterValidation = [];
                angular.forEach(testCaseCandidate.testCase, function(value) {
                    validationServicetestsuite.validateTestCase(value).then(
                        function(validationSuccessMessage) {
                            testCasesAfterValidation.push(value);
                        },
                        function(validationError) {
                            $log.error('Cannot validate test case ', value, 'for reason ', validationError);
                            testsuiteAlertsService.errorValidateIncomingTest(validationError);
                            vm.title = 'There were validation errors. Please check the file validity. Only valid cases are shown';
                        }
                    );
                });
                vm.testCasesFromFile = testCasesAfterValidation;
                $log.info('Got valid testcases: ', vm.testCasesFromFile);
            } else { // one test case
                validationServicetestsuite.validateTestCase(testCaseCandidate).then(
                    function(validationSuccessMessage) {
                        vm.testCasesFromFile = [testCaseCandidate];
                        $log.info('Got one test case to import: ', vm.testCasesFromFile);
                    },
                    function(reason) {
                        $log.error('Cannot validate test case ', data, 'for reason ', reason);
                        testsuiteAlertsService.errorValidateIncomingTest(reason);
                        vm.title = 'There was a validation error. Please check the file validity';
                    }
                );
            }
            vm.title = 'Save test cases by clicking \"Import\" button';
            if (angular.isDefined (vm.tableParams)) {
                vm.tableParams.reload();
            } else {
                initNgTableStructure();
            }

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
                    var orderedData = params.sorting() ? $filter('orderBy')(vm.testCasesFromFile, params.orderBy()) : vm.testCasesFromFile;

                    if (orderedData.length > 0) {
                        angular.forEach(orderedData, function (testCase) {//init additional view properties
                            testCase.expanded = false;
                        });
                        // use built-in angular filter
                        orderedData = params.filter() ? $filter('filter')(orderedData, params.filter()) : orderedData;

                        params.total(orderedData.length); // set total for recalc pagination
                        var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                        if (params.page() > totalPages) {
                            params.page(totalPages);
                        }
                        orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                        $defer.resolve(orderedData);
                    } else {
                        $defer.reject([]);
                    }
                }
            });
        }

        /**
         * A link to the service's {@link testsuiteService.validateAndSaveTestCase}
         * @param testCase
         */
        function importOne(testCase) {
            $log.info('Trying to import one testcase: ', testCase);
            testsuiteService.validateAndSaveTestCase(testCase).then(function (data) {
                $log.info('Testcase is imported: ', testCase);
                vm.testCasesFromFile.splice(findTestCaseIndexByName(testCase.testName, vm.testCasesFromFile), 1);
                vm.tableParams.reload();
                if (vm.testCasesFromFile.length === 0) {
                    vm.showFooter = true;
                }
            }, function (error) {
                testsuiteAlertsService.errorImportTest(error);
            });
        }

        function importAll() {
            $log.info('Importing all test cases');
            var testQueue = angular.copy(vm.testCasesFromFile);
            importTestCasesRecursionBody(testQueue);
        }

        function importTestCasesRecursionBody(testQueue) {
            if (angular.isDefined(testQueue) && angular.isDefined(testQueue[0])) {
                testsuiteService.validateAndSaveTestCase(testQueue[0]).
                    then(function (data) {
                        vm.testCasesFromFile.splice(findTestCaseIndexByName(testQueue[0].testName, vm.testCasesFromFile), 1);
                        vm.tableParams.reload();
                        testsuiteAlertsService.successImportTest(testQueue[0].testName);
                        testQueue.splice(0, 1);
                        importTestCasesRecursionBody(testQueue);
                        if (vm.testCasesFromFile.length === 0) {
                            vm.showFooter = true;
                        }
                    }, function (error) {
                        $log.error('Test case ', testQueue[0], ' is NOT imported :', error);
                        testsuiteAlertsService.errorImportTest(testQueue[0].testName);
                        testQueue.splice(0, 1);
                        importTestCasesRecursionBody(testQueue);
                        if (vm.testCasesFromFile.length === 0) {
                            vm.showFooter = true;
                        }
                    });
            }
        }

        /**
         * Changes the {@link testCase.expanded} property, which handles whether to show the expanded view or not
         * @param testCase
         */
        function changeExpanded(testCase) {
            testCase.expanded = !testCase.expanded;
        }

    }
})();
