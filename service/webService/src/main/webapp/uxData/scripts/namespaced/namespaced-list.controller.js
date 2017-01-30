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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */


/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedList', NamespacedList);

    NamespacedList.$inject = ['$rootScope', '$scope', '$log', '$state', '$q', '$filter', '$modal', '$sessionStorage', 'dialogs', 'ngTableParams',
        'authService', 'USER_PERMISSIONS', 'STATES_CONSTANTS', 'namespacedService', 'namespacedAlertsService', 'COMMON_CONSTANTS', 'requestsService', 'redirectorOfflineMode'];

    function NamespacedList($rootScope, $scope, $log, $state, $q, $filter, $modal, $sessionStorage, $dialogs, ngTableParams, authService,
                            USER_PERMISSIONS, STATES_CONSTANTS, namespacedService, namespacedAlertsService, COMMON_CONSTANTS, requestsService, redirectorOfflineMode) {
        /* jshint validthis: true */
        var vm = this;

        //hook storage to the scope so no listeners should be added
        vm.sessionStorage = $sessionStorage;

        vm.redirectorOfflineMode = redirectorOfflineMode;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.namespaces = {};
        vm.namespaces.namespace = [];

        //CURRENT DATA
        vm.exportAll = exportAllNamespaces;
        vm.edit = goToEditPage;
        vm.delete = deleteNamespace;
        vm.export = exportNamespace;
        vm.findRules = findRulesForNamespace;
        vm.deleteEntities = deleteEntities;

        vm.batchSizeToGetDependingRules = 10;

        vm.goToFlavorRulePage = namespacedService.goToFlavorRulePage;
        vm.goToTemplateFlavorRulePage = namespacedService.goToTemplateFlavorRulePage;

        /**
         * This is a map (NS list type -> show it or not)
         */
        vm.showNamespaced = {
            "IP": true,
            "ENCODED": true,
            "TEXT": true
        };

        init();

        //is not a big issue since the object is small
        $scope.$watch("vm.showNamespaced", function () {
            vm.tableParams.reload();
        }, true);

        function init() {
            //init session storage for filters
            if (angular.isUndefined(vm.sessionStorage.namespacedListFilter)) {
                vm.sessionStorage.namespacedListFilter = vm.showNamespaced;
            } else {
                vm.showNamespaced = vm.sessionStorage.namespacedListFilter;
            }
            initData();
        }

        function initData() {
            vm.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 10,          // count per page
                sorting: {
                    name: 'asc'     // initial sorting
                }
            }, {
                total: 0, // length of data
                getData: function ($defer, params) {
                    getNamespaces().then(function (result) {
                        vm.namespaces.namespace = result;
                        var orderedData = params.sorting() ? $filter('orderBy')(result, params.orderBy()) : result;

                        // use build-in angular filter
                        orderedData = params.filter() ?
                            $filter('filter')(orderedData, params.filter()) : orderedData;

                        params.total(orderedData.length); // set total for recalc pagination
                        var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                        if (params.page() > totalPages) {
                            params.page(totalPages);
                        }
                        orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                        $defer.resolve(orderedData);
                        getDependingRulesRecursive(orderedData, vm.batchSizeToGetDependingRules);
                    }, function () {

                    });
                }
            });
            vm.tableParams.settings().$scope = $scope;
        }

        function getNamespaces() {
            var defer = $q.defer();
            namespacedService.getNamespacesFromDS(vm.namespaces)
                .then(function (result) {
                    $log.info('Success: NamespaceDS provide ' + result.namespace.length + ' item(s)', result);

                    var dataForTable = [];
                    for (var i = 0; i < result.namespace.length; i++) {
                        if (vm.showNamespaced[result.namespace[i].type]) {//apply by type filter
                            dataForTable.push(
                                {
                                    'name': result.namespace[i].name,
                                    'description': result.namespace[i].description,
                                    'valueCount': redirectorOfflineMode ? result.namespace[i].valueSet.length :
                                        result.namespace[i].valueCount,
                                    'hiddenFromUI': false
                                }
                            );
                        }
                    }
                    defer.resolve(dataForTable);
                },
                function (reason) {
                    if (reason.status === 200) {
                        $log.warn('Warn: ' + reason.message);
                    } else {
                        $log.error('Failed: ' + reason.message);
                        if (reason.status !== 404) {
                            namespacedAlertsService.errorGet();
                        }
                    }
                    defer.reject();
                }
            );
            return defer.promise;
        }

        function getDependingRulesRecursive(data, recursionCount) {
            $log.info('Attempting to get all depending rules');
            var testQueue = [];
            for (var i = 0; i < data.length; i++) {
                testQueue.push(data[i].name);
            }
            getDependingRulesRecursionBody(testQueue, recursionCount);
        }

        function getDependingRulesRecursionBody(testQueue, recursionCount) {
            if (angular.isDefined(testQueue) && angular.isArray(testQueue) && angular.isDefined(testQueue[0])) {
                var namespacedToGetRules = testQueue.slice(0, recursionCount);
                requestsService.getMultipleRulesDependingOnNamespaced(namespacedToGetRules).
                    then(function (data) {
                        $log.info('Depending rules received successfully', data);
                        for (var i = 0; i < data.namespacedLists.length; i++) {
                            var namespace = namespacedService.getNamespaceByName(data.namespacedLists[i].name, vm.tableParams.data);
                            if (angular.isDefined(namespace)) {
                                var namespaceWithDependingRules = namespacedService.getNamespaceByName(data.namespacedLists[i].name, data.namespacedLists);
                                namespace.dependingRules = namespaceWithDependingRules.dependingFlavorRules;
                                namespace.dependingTemplateRules = namespaceWithDependingRules.dependingTemplateFlavorRules;
                            }
                        }
                        testQueue.splice(0, recursionCount);
                        getDependingRulesRecursionBody(testQueue, recursionCount);
                    }, function (error) {//if get was in error
                        testQueue.splice(0, recursionCount);
                        $log.error('Error while getting namespaced', error);
                        getDependingRulesRecursionBody(testQueue, recursionCount);
                    });
            }
        }

        /** LIST **/
        function exportAllNamespaces() {
            namespacedService.exportAll()
                .then(function () {
                    $log.info('Success export all (' + vm.namespaces.namespace.length + ') namespaces');
                }, function (reason) {
                    $log.error('Failed: ' + reason);
                });
        }

        function goToEditPage(name) {
            $state.go(STATES_CONSTANTS().namespacesEdit, {id: name});
        }

        function deleteNamespace(name) {
            var dlg = $dialogs.confirm('Delete confirmation', 'Are you sure you want to Delete namespaced list: ' + name);
            dlg.result.then(function (btn) {
                var namespace = vm.tableParams.data[namespacedService.findPostionOfNamespaceByName(name, vm.tableParams.data)];
                namespace.hiddenFromUI = true;
                namespacedAlertsService.willTryDelete(name);
                namespacedService.deleteNamespace(vm.namespaces, name)
                    .then(function (data) {
                        namespacedAlertsService.successDelete(name);
                        $log.warn('Deleted namespace item: ' + name);
                        vm.tableParams.reload();
                    }, function (reason) {
                        $log.error('Failed: ' + reason.data);
                        namespace.hiddenFromUI = false;
                        if (reason.status != 403) { // we show dialog for 403 in the interceptor
                            $dialogs.notify('Failed to delete namespaced list: ' + name, reason.data.message);
                        }
                        vm.isSaving = false;
                    }, function (btn) {
                        //click cancel
                    });
            });
        }

        function exportNamespace(name) {
            namespacedService.exportNamespace(vm.namespaces, name)
                .then(function () {
                    $log.info('Success export namespace \"' + name + '\"');
                }, function (reason) {
                    $log.error('Failed: ' + reason);
                });
        }

        function findRulesForNamespace(name) {
            $state.go(STATES_CONSTANTS().namespacesSearch, {serviceName: $rootScope.currentApplication, search: name});
        }

        function deleteEntities (namespacedListName) {
            console.log(namespacedListName);
            $modal.open({
                templateUrl: '../uxData/scripts/namespaced/namespaced-delete-entities.modal.html',
                controller: 'NamespacedDeleteEntities as vm',
                resolve: {
                    'namespacedName': function () {return namespacedListName}
                },
                size: 'lg'//large size
            }); // end modal.open
        }
    }
})();
