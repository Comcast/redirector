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


(function () {
    'use strict';

    angular
        .module('uxData.server')
        .controller('serverController', serverController);

    serverController.$inject = ['$rootScope', '$scope', 'serverService', 'utilsService', 'SERVER_CONSTANTS'];

    function serverController($rootScope, $scope, serverService, utils, CONST) {

        /* jshint validthis: true */
        var vm = this;

        var SIMPLE = CONST().EXP_EDIT_MODE.SIMPLE;
        var EMPTY_QUERY = {"entry" : []};

        vm.isSimpleMode = isSimpleMode;
        vm.isDisabled = isDisabled;
        vm.hasError = hasError;
        vm.urlChanged = urlChanged;
        vm.pathChanged = pathChanged;
        vm.getListOfPaths = getListOfPaths;
        vm.editModeChanged = editModeChanged;
        vm.whitelistedChanged = whitelistedChanged;
        vm.addNewQueryPair = addNewQueryPair;
        vm.removeQueryPair = removeQueryPair;
        vm.queryChanged = queryChanged;

        vm.serverValues = $scope.serverValues;
        vm.servicePaths = $scope.servicePaths;
        vm.validationData = $scope.validationData;
        vm.currentApp = $rootScope.currentApplication;

        vm.editMode = utils.isDefinedAndNotEmpty($scope.editMode) ? $scope.editMode : CONST().EXP_EDIT_MODE.SIMPLE;
        vm.disabled = utils.isDefinedAndNotEmpty($scope.disabled) ? $scope.disabled: false;
        vm.showLabels = utils.isDefinedAndNotEmpty($scope.showLabels) ? $scope.showLabels : true;
        vm.showFlavors = utils.isDefinedAndNotEmpty($scope.showFlavors) ? $scope.showFlavors : true;
        vm.showWhitelistedOnly = utils.isDefinedAndNotEmpty($scope.showWhitelistedOnly) ? $scope.showWhitelistedOnly : false;
        vm.onlySimpleMode = utils.isDefinedAndNotEmpty($scope.onlySimpleMode) ? $scope.onlySimpleMode : false;
        vm.showWhitelistedOption = utils.isDefinedAndNotEmpty($scope.showWhitelistedOption) ? $scope.showWhitelistedOption : false;
        vm.isNonWhitelisted = utils.isDefinedAndNotEmpty($scope.serverValues.isNonWhitelisted) ? $scope.serverValues.isNonWhitelisted : false;
        vm.path = utils.isDefinedAndNotEmpty($scope.serverValues.path) ? {NAME: $scope.serverValues.path} : {NAME: ''};
        vm.url = utils.isDefinedAndNotEmpty($scope.serverValues.url) && !angular.equals(vm.editMode, SIMPLE) ? $scope.serverValues.url : '';
        vm.query = utils.isEmptyObject($scope.serverValues.query) ? EMPTY_QUERY : $scope.serverValues.query;

        var currentServicePaths = {};
        var servicePathDirty = true;
        vm.listOfPaths = [];

        function isSimpleMode() {
            return angular.equals(vm.editMode, SIMPLE);
        }

        function getListOfPaths() {
            if (servicePathDirty || (!$.isEmptyObject(vm.servicePaths) && !angular.equals(vm.servicePaths, currentServicePaths))) {
                vm.listOfPaths = serverService.getListOfPaths(vm.servicePaths, vm.currentApp, $scope.showStacks, vm.showFlavors, $scope.showWhitelistedOnly);
                currentServicePaths = angular.copy(vm.servicePaths);
                servicePathDirty = false;
            }
            return vm.listOfPaths;
        }

        function pathChanged() {
            $scope.onPathChanged({path: vm.path.NAME});
        }

        function urlChanged() {
            $scope.onUrlChanged({url: vm.url});
        }

        function editModeChanged() {
            $scope.onEditModeChanged({mode: vm.editMode});
        }

        function whitelistedChanged() {
            $scope.onWhitelistedChanged({isNonWhitelisted: vm.isNonWhitelisted});
            servicePathDirty = true;
        }

        function isDisabled() {
            return vm.disabled;
        }

        function addNewQueryPair() {
            createEmptyQueryIfNull();
            vm.query.entry.push({"key" : "", "value" : ""});
            queryChanged();
        }

        function queryChanged() {
            $scope.onQueryChanged({query: vm.query});
        }

        function removeQueryPair(index) {
            vm.query.entry.splice(index, 1);
            nullifyEmptyQuery();
            queryChanged();
        }

        function createEmptyQueryIfNull() {
            if (utils.isEmptyObject(vm.query)) {
                vm.query = EMPTY_QUERY;
            }
        }

        function nullifyEmptyQuery() {
            if (!utils.isEmptyObject(vm.query)
                && (utils.isEmptyObject(vm.query.entry) || vm.query.entry.length == 0)) {
                vm.query = null;
            }
        }

        function hasError(error) {
            return angular.isDefined(error) && !$.isEmptyObject(error);
        }
    }
})();
