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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */


/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.summary')
        .controller('summaryController', summaryController);

    summaryController.$inject = ['$scope', '$log', '$filter', 'summaryService', 'namespacedService', 'toastr', 'ngTableParams', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'namespacedAlertsService', 'SUMMARY_CONSTANTS','$state','STATES_CONSTANTS'];

    function summaryController($scope, $log, $filter, summaryService, namespacedService, toastr, ngTableParams, messageService, LOCALSTORAGE_PAGE_NAMES, namespacedAlertsService, summaryCONST, $state, STATES_CONSTANTS) {
        /* jshint validthis: true */
        var vm = this;
        var NAMESPACED_DEFAULT_NAMES = [summaryCONST().NAMESPACED_DEFAULT_NAMES.INTERNAL_MAINTENANCE, summaryCONST().NAMESPACED_DEFAULT_NAMES.INTERNAL_FEATURE, summaryCONST().NAMESPACED_DEFAULT_NAMES.PRODUCTION_PREVIEW_LIST_2, summaryCONST().NAMESPACED_DEFAULT_NAMES.PRODUCTION_PREVIEW_LIST];
        vm.loading = true;
        vm.summary = [];
        vm.namespaces = {};
        vm.namespaces.namespace = [];
        vm.selectedNamespaces = [];
        vm.reloadSummary = reloadSummary;
        vm.hasReloadSummary = hasReloadSummary;
        vm.distributionPage = goToDistributionPage;
        vm.editNamespaces = goToNamespacesEditPage;
        vm.getSelectedNamespaces = getSelectedNamespaces;
        vm.tableParamsHolder = {
            paramsCount: 500,
            pageNo: 1
        };

        init();

        function initTableData() {
            vm.tableParams = new ngTableParams({
                page: vm.tableParamsHolder.pageNo,          // show first page
                count: vm.tableParamsHolder.paramsCount,    // count per page
                sorting: {
                    pos: 'asc'
                }
            }, {
                counts: [],
                total: 0, // length of data
                getData: function ($defer, params) {
                    var orderedData = params.sorting() ? $filter('orderBy')(vm.summary, params.orderBy()) : vm.summary;
                    // use build-in angular filter
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
            $scope.$on('ngTableAfterReloadData', function () {
                vm.tableParamsHolder.paramsCount = vm.tableParams.$params.count;
                vm.tableParamsHolder.pageNo = vm.tableParams.$params.page;
            });
            vm.tableParams.settings().$scope = $scope;
        }

        function restoreTableParams() {
            if (angular.isDefined(vm.tableParams)) {
                vm.tableParams.$params.count = vm.tableParamsHolder.paramsCount;
                vm.tableParams.$params.page = vm.tableParamsHolder.pageNo;
            }
        }

        messageService.onChangeApp($scope, function () {
            clearData();
            init();
            restoreTableParams();
        });

        function reloadSummary() {
            vm.loading = true;
            clearData();
            init();
            restoreTableParams();
        }

        function hasReloadSummary() {
            if (vm.selectedNamespaces.length > 0) {
                return false;
            } else {
                return true;
            }
        }

        function init() {
            getAllNamespacesAndSummary();
        }

        function clearData() {
            vm.summary.splice(0, vm.summary.length);
            if (angular.isDefined(vm.tableParams)) {
                vm.tableParams.reload();
            }
        }

        function getAllNamespacesAndSummary() {
            var namespaces = {};
            namespaces.namespace = [];
            namespacedService.getNamespacesFromDS(namespaces)
                .then(function (result) {
                    $log.info('Success: received ' + result.namespace.length + ' item(s)');
                    for (var i = 0; i < namespaces.namespace.length; i++) {
                        vm.namespaces.namespace[i] = namespaces.namespace[i].name;
                    }
                    if (vm.selectedNamespaces.length === 0) {
                        vm.selectedNamespaces = NAMESPACED_DEFAULT_NAMES;
                    }
                    summaryService.getSummary(vm.summary, getSelectedNamespaces())
                        .then(function () {
                            vm.loading = false;
                            angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initTableData();
                        }, function (reason) {
                            vm.loading = false;
                            angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initTableData();
                            toastr.error(reason.message, 'Error', {closeButton: true, timeOut: 3000});
                        });

                },
                function (reason) {
                    if (reason.status === 200) {
                        $log.warn('Warn: ' + reason.message);
                        vm.loading = false;
                   } else {
                        vm.loading = false;
                        $log.error('Failed: ' + reason.message);
                        if (reason.status !== 404) {
                            namespacedAlertsService.errorGet();
                        }
                    }
                }
            );
        }

        function getSelectedNamespaces() {
            var namespacedListNames = '';
            for (var i = 0; i < vm.selectedNamespaces.length; i++) {
                namespacedListNames = namespacedListNames + ',' + vm.selectedNamespaces[i];
            }
            return namespacedListNames.substring(1, namespacedListNames.length);
        }

        function goToNamespacesEditPage(name) {
            console.log(name)
            $state.go(STATES_CONSTANTS().namespacesEdit, {id: name});
        }

        function goToDistributionPage() {
            $state.go(STATES_CONSTANTS().distribution);
        }
    }
})();
