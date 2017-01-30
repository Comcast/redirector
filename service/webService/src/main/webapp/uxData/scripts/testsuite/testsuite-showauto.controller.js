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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

(function () {
    'use strict';

    angular
        .module('uxData.testsuite')
        .controller('ShowAutoTestSuite', ShowAutoTestSuite);

    ShowAutoTestSuite.$inject = ['$log', '$scope', '$filter', 'toastr', 'ngTableParams', 'USER_PERMISSIONS',
        'testSuiteRequestService', 'testsuiteService', 'messageService'];

    /**
     * This is a controller which controls showing rule test page features
     */
    function ShowAutoTestSuite($log, $scope, $filter,toastr, ngTableParams, USER_PERMISSIONS,
                           testSuiteRequestService, testsuiteService, messageService) {
        /* jshint validthis: true */
        var PRETTY_PRINT_INDENT = 2;

        var vm = this;

        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.MODES = {
            CURRENT  : 'CURRENT',
            NEXT : 'NEXT',
            EXTERNAL_REST : 'EXTERNAL_REST'
        };
        vm.STATUS = {
            PASSED : 'PASSED',
            FAILED : 'FAILED'
        };

        vm.filterSuiteData = filterSuiteData;
        vm.exportTestCase = testSuiteRequestService.exportTestCase;
        vm.exportTestCases = testSuiteRequestService.exportTestCases;
        vm.getSimpleViewForTestCase = testsuiteService.getSimpleViewForTestCase;
        vm.getXmlViewForTestCase = testsuiteService.getXmlViewForTestCase;
        vm.generateDiffStringForTestResult = testsuiteService.generateDiffStringForTestResult;
        vm.changeExpanded = changeExpanded;
        vm.reRunTestCases = loadData;
        vm.ngTableData = [];
        vm.switchMode = switchMode;
        vm.searchCriteria = "";
        vm.mode = vm.MODES.CURRENT;
        vm.externalEndpointUrl = "";

        vm.testCasesWithResult = [];
        vm.loading = false;
        vm.validateUrl = validateUrl;
        vm.isCurrentPathValid = true;

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

            var mode = vm.mode;
            if (mode == vm.MODES.EXTERNAL_REST) {
                mode += "?baseURL=" + vm.externalEndpointUrl;
            }

            vm.testCasesWithResult = [];

            testsuiteService.getAllAutoTestCases(mode).then(
                function (data) {
                    $log.info('Got generated test cases and results: ', data);
                    vm.testCasesWithResult = data;
                    if (angular.isArray(vm.testCasesWithResult) && vm.testCasesWithResult.length > 0) {
                        angular.forEach(vm.testCasesWithResult, function (testCaseWithResult) {//init additional view properties
                            testCaseWithResult.testcase.showSimpleView = true;
                            testCaseWithResult.expected = testCaseWithResult.testcase.expected;

                            var copyOfActualResult = angular.copy(testCaseWithResult.actual);
                            if (testCaseWithResult.status === vm.STATUS.PASSED) {
                                /**
                                 * for test to pass appliedUrlRules arrays don't have to be equal, but they must be equal in order
                                 * for diff to be shown as successful
                                 */
                                copyOfActualResult.appliedUrlRules = testCaseWithResult.testcase.expected.appliedUrlRules;
                            }
                            testCaseWithResult.expectedText = JSON.stringify(testsuiteService.arrayPropertiesToStrings(testCaseWithResult.expected), null, PRETTY_PRINT_INDENT);
                            testCaseWithResult.actualText = JSON.stringify(testsuiteService.arrayPropertiesToStrings(copyOfActualResult), null, PRETTY_PRINT_INDENT);
                        });
                    } else {
                        vm.loading = false;
                    }
                    angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initNgTableStructure();
                },
                function (error) {
                    toastr.error(error.data, 'Error', {closeButton: true, timeOut: 3000});
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
                    if (angular.isArray(vm.testCasesWithResult) && vm.testCasesWithResult.length > 0) {
                        var orderedData = params.sorting() ? $filter('orderBy')(vm.testCasesWithResult, params.orderBy()) : vm.testCasesWithResult;


                        // use built-in angular filter
                        orderedData = params.filter() ? $filter('filter')(orderedData, params.filter()) : orderedData;

                        if (angular.isDefined(orderedData)) {
                            params.total(orderedData.length); // set total for recalc pagination
                            var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                            if (params.page() > totalPages) {
                                params.page(totalPages);
                            }
                            orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                            vm.loading = false;
                            $defer.resolve(orderedData);
                        } else {
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

        messageService.onChangeApp($scope, function (message) {
            loadData();
        });

        function switchMode(testCase) {
            testCase.showSimpleView = !testCase.showSimpleView;
        }

        function changeExpanded(testCase) {
            testCase.expanded = !testCase.expanded;
        }

        function validateUrl() {
            vm.isCurrentPathValid = false;
            if (angular.isDefined(vm.externalEndpointUrl) && vm.externalEndpointUrl !== '') {
                var genericRegexp = /^([a-z]+)(:\/\/)([\w]+)([:]*)([0-9]*)[\/]*([\w]*)/g;
                if (genericRegexp.exec(vm.externalEndpointUrl)) {
                    vm.isCurrentPathValid = true;
                }
            }
        }
    }
})();
