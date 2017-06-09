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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */


/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedDetail', NamespacedDetail);

    NamespacedDetail.$inject = ['$scope','$log', '$state', '$stateParams', '$filter', '$modal', 'ngTableParams', 'STATES_CONSTANTS',
        'authService', 'USER_PERMISSIONS', 'LOCALSTORAGE_PAGE_NAMES', 'namespacedService', 'namespacedAlertsService',
        'importService'];

    function NamespacedDetail($scope, $log, $state, $stateParams, $filter, $modal, ngTableParams, STATES_CONSTANTS, authService,
                              USER_PERMISSIONS, LOCALSTORAGE_PAGE_NAMES, namespacedService, namespacedAlertsService, importService) {
        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.isAddState = false;
        vm.uiModelHolder = {
            value: ''
        };
        vm.name = '';

        vm.namespace = {};

        var duplicateData = [];

        vm.addValue = addValue;
        vm.delValue = delValue;
        vm.saveNamespace = saveNamespace;
        vm.isValueUnique = isValueUnique;
        vm.getFile = retrieveFile;

        vm.tableParamsHolder = {
            paramsCount : 10,
            pageNo : 1
        };

        init();

        function retrieveFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                convertValues(vm.name, result);
            }, function (reason) {
                $log.error('Reason: ' + reason);
                namespacedAlertsService.errorGetFile();
            });
        }

        function convertValues(namespaceName, result) {
            var values = result.match(/[^\r\n]+/g); //contains new lines todo: move to constants
            if (values !== '') {
                for (var i = 0; i < values.length; i++) {
                    if (!angular.equals(values[i].trim(), '')) {
                        addValueInBunch(namespaceName, values[i]);
                    }
                }
                generateTableData();
                vm.tableParams.reload();
            }
        }

        function initData() {
            vm.tableParams = new ngTableParams(
                {
                    page: vm.tableParamsHolder.pageNo,            // show first page
                    count: vm.tableParamsHolder.paramsCount,          // count per page
                    sorting: {
                        name: 'asc'     // initial sorting
                    }
                },
             {
                total: 0, // length of data
                getData: function ($defer, params) {
                    var orderedData = params.sorting() ? $filter('orderBy')(vm.dataForTable, params.orderBy()) : vm.dataForTable;
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
            $scope.$on('ngTableAfterReloadData' , function () {
                vm.tableParamsHolder.paramsCount = vm.tableParams.$params.count;
                vm.tableParamsHolder.pageNo = vm.tableParams.$params.page;
            });
            vm.tableParams.settings().$scope = $scope;
        }


        function generateTableData() {
            vm.dataForTable = [];
            if (angular.isDefined(vm.namespace)) {
                for (var i = 0; i < vm.namespace.valueSet.length; i++) {
                    if (vm.namespace.type == 'ENCODED') {
                        vm.dataForTable.push(
                            {
                                value: vm.namespace.valueSet[i].value,
                                encodedValue: vm.namespace.valueSet[i].encodedValue
                            }
                        );
                    } else {
                        vm.dataForTable.push(
                            {
                                value: vm.namespace.valueSet[i].value,
                                encodedValue: "list is not encoded"
                            }
                        );
                    }
                }
            }
        }

        function getNamespace() {
            namespacedService.getNamespaceFromDS(vm.name)
                .then(function (result) {
                    vm.namespace = result;
                    $log.info('Success: received ' + result.namespace.name+ ' namespaced list');
                    if (vm.namespace.type == undefined) {
                        vm.namespace.type = "TEXT";
                    }
                    generateTableData();
                    angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initData();
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
                }
            );
        }


        function addValue(namespaceName, namespaceValue) {
            namespacedService.addItem(vm.namespace, namespaceName, namespaceValue)
                .then(function (result) {
                    vm.uiModelHolder.value  = '';
                    $log.info('Added new value: ' + namespaceValue);
                    generateTableData();
                    vm.tableParams.reload();
                }, function (reason) {
                    $log.error('Failed: ' + reason.message);
                    namespacedAlertsService.duplicate(namespaceValue);
                });
        }

        function addValueInBunch(namespaceName, namespaceValue) {
            namespacedService.addItem(vm.namespace, namespaceName, namespaceValue);
        }


        function delValue(namespaceName, namespaceValue) {
            var promise = namespacedService.delItem(vm.namespace, namespaceName, namespaceValue);
            promise.then(function (result) {
                angular.extend(vm.namespace, result);
                $log.info('Deleted value: ' + namespaceValue);
                generateTableData();
                vm.tableParams.reload();
            }, function (reason) {
                $log.error('Failed: ' + reason.message.data);
                namespacedAlertsService.errorDelete(namespaceValue);
            });
        }

        function isValueUnique(value) {
            if (angular.isUndefined(value) || value === '') {
                return true;
            }
            return namespacedService.getValuePosition(vm.namespace.valueSet, {value: value, encodedValue: value});
        }

        function saveNamespaceWithoutDuplicates(namespace) {
            namespacedService.validateAndSave(namespace, namespace.name, vm.isAddState).then(function () {
                namespacedAlertsService.success(namespace.name);
                $log.info('Saved namespace: \"' + namespace.name + '\"');
                if (angular.isDefined($state.previousState)
                    && $state.previousState.name === STATES_CONSTANTS().namespacesSearch) {
                    $state.go($state.previousState.name, $state.previousParams);
                } else {
                    $state.go(STATES_CONSTANTS().namespacesShow);
                }
            }, function (reason) {
                //todo: get rid of it
                $log.error('Failed: ' + reason.message? reason.message: reason.data.message);
                namespacedAlertsService.errorPostWithMessage(reason.message? reason.message: reason.data.message);
            });
        }

        function resolveDuplicatesAndSaveNamespace(namespace, duplicates) {
            vm.namespaceCandidates = namespace;
            vm.namespacedDuplicateMap = angular.copy(duplicates.data);
            var modalInstance = $modal.open({
                templateUrl: '../uxData/scripts/namespaced/namespaced-modal.html',
                controller: 'NamespacedMerger as vm',
                resolve: {
                    'namespaceCandidates': function () {
                        return angular.copy(vm.namespaceCandidates);
                    },
                    'namespacedDuplicateMap': function () {
                        return angular.copy(vm.namespacedDuplicateMap);
                    },
                    'isImporting': function () {
                        return false;
                    }
                }
            }); // end modal.open

            modalInstance.result.then(function (result) {
                if (angular.isDefined($state.previousState) &&
                    $state.previousState.name === STATES_CONSTANTS().namespacesSearch) {
                    $state.go($state.previousState.name, $state.previousParams);
                } else {
                    $state.go(STATES_CONSTANTS().namespacesShow);
                }
            }, function () {
            });
        }

        function saveNamespace() {
            vm.namespacedDuplicateObject = {};
            namespacedService.validateDuplicates(vm.namespace).then(function (result) {
                saveNamespaceWithoutDuplicates(vm.namespace);
            }, function (result) {
                $log.warn(result.message);
                if (angular.isUndefined(result.data) || result.data.length == 0) {
                     namespacedAlertsService.errorPostWithMessage(result.message);
                } else {
                    resolveDuplicatesAndSaveNamespace(vm.namespace, result);
                }
            });
        }

        function init() {
            var id = $stateParams.id;
            vm.name = $stateParams.id;
            if (id === 'new' || angular.isUndefined(id)) {
                vm.isAddState = true;
                var type = 'TEXT';
                if (angular.isDefined($stateParams.type)) {
                    type = $stateParams.type;
                }
                //init
                    vm.namespace =
                        {
                            valueSet: [],
                            description: '',
                            name: '',
                            version: Math.round(new Date().getTime()),
                            type: type
                        };
                generateTableData();
                angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initData();

                vm.name = vm.namespace.name;
            } else {
                vm.name = id;
                vm.isAddState = false;
                getNamespace();
            }
        }
    }
})();
