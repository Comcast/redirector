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
        .module('uxData.stacks')
        .controller('stacksManagement', stacksManagement);

    stacksManagement.$inject = ['$rootScope', '$scope', '$log', '$filter', '$timeout', 'toastr', 'dialogs', 'authService', 'USER_PERMISSIONS', 'stacksManagementService', 'ngTableParams', 'messageService', 'utilsService', 'LOCALSTORAGE_PAGE_NAMES', 'requestsService'];

    function stacksManagement($rootScope, $scope, $log, $filter, $timeout, toastr, dialogs, authService, USER_PERMISSIONS, stacksManagementService, ngTableParams, messageService, utils, LOCALSTORAGE_PAGE_NAMES, requestsService) {
        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.hasPermissions = utils.hasPermissions;

        vm.whitelistedStacks = [];
        vm.stacks = [];
        vm.stacksAndFlavors = {};

        vm.inactiveStacks = [];

        vm.saveStacks = saveStacks;
        vm.saveStackComment = saveStackComment;
        vm.exportAllStacks = stacksManagementService.exportAllStacks;
        vm.exportAllWhitelisted = stacksManagementService.exportAllWhitelisted;

        vm.tableParamsHolder = {
            paramsCount : 500,
            pageNo : 1
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
                total: 0, // length of data
                getData: function ($defer, params) {
                    var orderedData = params.sorting() ? $filter('orderBy')(vm.whitelistedStacks, params.orderBy()) : vm.whitelistedStacks;
                    // use build-in angular filter
                    orderedData = params.filter() ? $filter('filter')(orderedData, params.filter()) : orderedData;

                    params.total(orderedData.length); // set total for recalc pagination
                    var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                    if (params.page() > totalPages) {
                        params.page(totalPages);
                    }
                    orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                    const MAX_CHARACTERS_IN_FLAVOR = 25;
                    angular.forEach (orderedData, function (value) {
                        angular.forEach (value.flavors, function (flavor) {
                            if (flavor.name.length > MAX_CHARACTERS_IN_FLAVOR) {
                                flavor.name = flavor.name.substring(0, MAX_CHARACTERS_IN_FLAVOR) + "...";
                            }
                        });
                        requestsService.getStackComment($rootScope.currentApplication, value.path).then(
                            function(commentWrapper) {
                                value.comment = commentWrapper.comment;
                            }, function (error) {

                            }
                        );
                    });
                    $defer.resolve(orderedData);
                }
            });
            $scope.$on('ngTableAfterReloadData' , function () {
                vm.tableParamsHolder.paramsCount = vm.tableParams.$params.count;
                vm.tableParamsHolder.pageNo = vm.tableParams.$params.page;
            });
            //workaround for https://github.com/esvit/ng-table/issues/297
            vm.tableParams.settings().$scope = $scope;
        }

        function restoreTableParams() {
            if (angular.isDefined(vm.tableParams)) {
                vm.tableParams.$params.count = vm.tableParamsHolder.paramsCount;
                vm.tableParams.$params.page = vm.tableParamsHolder.pageNo;
            }
        }

        messageService.onChangeApp($scope, function (message) {
            clearData();
            init();
            restoreTableParams();
        });

        function saveStacks() {
            stacksManagementService.saveWhitelisted(vm.whitelistedStacks).then(function (data) {
                $log.info('The whitelisted stacks are saved');
                toastr.success('The whitelisted stacks are saved', 'Success', {closeButton: true, timeOut: 3000});
                clearData();
                getStacks();
            }, function (error) {
                toastr.error(error.data.message, 'Error', {closeButton: true, timeOut: 3000});
            });
        }


        function init() {
            getStacks();
        }

        function getStacks() {
            stacksManagementService.getStacks(vm.stacks, vm.stacksAndFlavors, vm.whitelistedStacks, vm.inactiveStacks).then(function (result) {
                angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initTableData();
            }, function (reason) {
                angular.isDefined(vm.tableParams) ? vm.tableParams.reload() : initTableData();
                toastr.error(reason.message, 'Error', {closeButton: true, timeOut: 3000});
            });
        }

        function clearData() {
            vm.whitelistedStacks.splice(0, vm.whitelistedStacks.length);
            vm.inactiveStacks.splice(0, vm.inactiveStacks.length);
            vm.stacks.splice(0, vm.stacks.length);
            vm.stacksAndFlavors = {};
            if (angular.isDefined(vm.tableParams)) {
                vm.tableParams.reload();
            }
        }

        function saveStackComment (comment, path) {
            var commentObject = {comment: comment};
            //todo: error handling (if any)
            requestsService.saveStackComment(commentObject, $rootScope.currentApplication, path);
            return 1;
        }
    }

})();
