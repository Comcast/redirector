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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.defaultserver')
        .controller('DefaultServer', DefaultServer);

    DefaultServer.$inject = ['$scope', '$rootScope', 'messageService', 'requestsService', 'utilsService', 'mediator', 'authService', 'USER_PERMISSIONS', 'toastr', 'dialogs'];

    function DefaultServer($scope, $rootScope, messageService, requests, utils, mediator, authService, USER_PERMISSIONS, toastr, dialogs) {

        var vm = this;

        vm.serverHolder = $scope.serverHolder;

        vm.defaultServerToggle = false; //collapse on start
        vm.currentApplication = $rootScope.currentApplication;
        vm.changes = {};
        vm.serverHolder.defaultServer = {};
        vm.serverBeforeEdit = {};
        vm.isSavingDefaultServer = false;
        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.entityName = 'server';
        vm.responseErrorMsg = '';

        //*********Diff variables;
        vm.serverHolder.defaultCurrentText = '';
        vm.serverHolder.defaultPendingText = '';
        vm.exportAll = exportAll;
        vm.changeDefaultServer = changeDefaultServer;
        vm.returnOnlyNonSelectedPaths = returnOnlyNonSelectedPaths;
        vm.serverHolder.returnOnlyNonSelectedPathsForDefault = returnOnlyNonSelectedPaths;

        vm.nonSelectedPaths = [];

        loadData();

        function loadData() {
            loadServicePaths();
            requests.getPendingChangesJson($rootScope.currentApplication)
                .then(function (data) {
                    vm.changes = data;
                    getDefaultServer($rootScope.currentApplication);
                });
        }

        function returnOnlyNonSelectedPaths() {
            var returnedPaths = angular.copy(vm.servicePaths);
            if (vm.servicePaths === undefined || vm.servicePaths.flavor === undefined) {
                return [];
            }
            angular.forEach(vm.servicePaths.flavor, function(path, index) {
                for (var i = 0; i < vm.serverHolder.distribution.displayableRules.length; i++) {
                    if (path.value === vm.serverHolder.distribution.displayableRules[i].server.path) {
                        returnedPaths.flavor.splice(getFlavorIndexByValue(returnedPaths.flavor, path.value), 1);
                    }
                }
            });
            vm.nonSelectedPaths = returnedPaths;
        }

        function getFlavorIndexByValue (flavors, value) {
            for (var i = 0; i < flavors.length; i++ ){
                if (flavors[i].value === value) {
                    return i;
                }
            }
        }

        function getServerFromData(data, pendingChangesServer, serverName) {
            var changedExpression = null;
            var hasChanges = false;
            var changeType = '';
            var url = utils.createUrl();
            if (angular.isDefined(pendingChangesServer)) {
                hasChanges = true;
                changeType = pendingChangesServer.changeType;
                changedExpression = pendingChangesServer.currentExpression;
                url = angular.copy(changedExpression.url);
            }
            return {
                name: serverName + ' Server',
                url: url,
                secureUrl: '',
                path: angular.copy(changedExpression != null ? changedExpression.path : data.path),
                description: serverName + ' Server route',
                editMode: url.indexOf('host') > -1 ? 'simple' : 'advanced',
                hasChanges: hasChanges,
                changeType: changeType
            };
        }

        function getDefaultServer() {
            requests.getDefaultServer($rootScope.currentApplication)
                .then(function (data) {
                    var pendingChangesServer = utils.getPendingChangesObjectById(vm.changes, 'servers', 'default');
                    if (angular.isDefined(pendingChangesServer)) {
                        vm.serverHolder.defaultPendingText = pendingChangesServer.changedExpression.path;
                        vm.serverHolder.defaultCurrentText = pendingChangesServer.currentExpression.path;
                    }
                    vm.serverHolder.defaultServer = getServerFromData(data, pendingChangesServer, 'Default');
                    vm.serverBeforeEdit = angular.copy(vm.serverHolder.defaultServer);
                }, function (reason) {
                    toastr.error('Can\'t get Default Server', 'Error', {closeButton: true, timeOut: 3000});
                });
        }

        function exportAll() {
            requests.exportDistributionsAlongWithServers();
        }

        function loadServicePaths() {
            requests.getServicePaths($rootScope.currentApplication)
                .then(function (data) {
                    if (typeof data.paths !== 'undefined') {
                        vm.servicePaths = data.paths[0];
                        returnOnlyNonSelectedPaths();
                    } else {
                        vm.servicePaths = [];
                    }
                }, function (reason) {
                    toastr.error('Can\'t get servicePath', 'Error', {closeButton: true, timeOut: 3000});
                });
        }

        function changeDefaultServer() {
            vm.serverHolder.defaultCurrentText = vm.serverBeforeEdit.path;
            vm.serverHolder.defaultPendingText = vm.serverHolder.defaultServer.path;
            vm.serverHolder.inputs.saved = false;

        }


        messageService.onChangeApp($scope, function (message) {
            vm.serverHolder.defaultCurrentText = '';
            vm.serverHolder.defaultPendingText = '';
            vm.changes = {};
            vm.serverHolder.defaultServer = {};
            loadData();
        });

        $scope.$watch(vm.serverHolder.distribution,
            function (scope) {
                returnOnlyNonSelectedPaths();
        });
    }
})();
