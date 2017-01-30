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


/**
 * @author: Alexander Pletnev
 * Date: 4/24/14
 */


angular.module('uxData').controller('serverCtrl', ['$scope', '$rootScope', '$filter', 'messageService', 'requestsService', 'utilsService',
    function SelectServerCtrl($scope, $rootScope, $filter, messageService, requests, utils) {

        $scope.message = 'Server';

        $scope.data = {};

        $scope.data.xreGuide = {};
        $scope.data.xreGuide.path = [];

        $scope.data.xreApp = {};
        $scope.data.xreApp.path = [];

        $scope.currentPath = '';
        $scope.currentApp = $rootScope.currentApplication;

        var servicePathsToCompare = {};
        $scope.result = [];

        $scope.isCurrentPathDirty = false;
        $scope.isCurrentPathValid = false;

        messageService.onChangeApp($scope, function (message) {
            if (!$scope.disabled) {
                $scope.currentApp = message.name;
            }
        });

        messageService.onEditSelectServer($scope, function (message) {
            if ($scope.id == message.id) {
                $scope.disabled = !$scope.disabled;
            }
        });

        $scope.getCurrentApp = function () {
            return $scope.currentApp;
        };

        messageService.onGetServerData($scope, function (data) {
            $scope.servicePaths = data.data;
        });

        $scope.isDisabled = function () {
            return $scope.disabled;
        };

        $scope.isSimple = function () {
            if (!angular.isDefined($scope.editMode) || angular.equals($scope.editMode, 'simple')) {
                return true;
            }
            return false;
        };

        $scope.updateData = function () {
            if($scope.isSimple()) {
                $scope.values.url = utils.createUrl();
            }
            $scope.values.path = $scope.currentPath;
        };

        $scope.setData = function () {
            /*var url = $scope.values.url;*/
            if($scope.isSimple()) {
                $scope.currentPath = $scope.values.path;
                //for angularjs select
                $scope._currentPath = {NAME: $scope.currentPath};
            }
        };

        $scope.urlChanged = function () {
            $scope.isCurrentParthDirty = true;
        };

        /**
         * Return list of flavors and paths
         */
        $scope.getListOfPaths = function () {
            var result = [];

            if (!$.isEmptyObject($scope.servicePaths) && !angular.equals($scope.servicePaths, servicePathsToCompare)) {

                if (angular.isDefined($scope.getCurrentApp())) {
//                    if (angular.isUndefined(servicePathForApp)) return [];
                    if (!angular.isArray($scope.servicePaths.stack)) {
                        $scope.servicePaths.stack = [$scope.servicePaths.stack];
                    }

                    if (!angular.isArray($scope.servicePaths.flavor)) {
                        $scope.servicePaths.flavor = [$scope.servicePaths.flavor];
                    }

                    if (angular.isUndefined($scope.showStacks) || (angular.isDefined($scope.showStacks) && $scope.showStacks == true)) {
                        angular.forEach($scope.servicePaths.stack, function (value, key) {
                            var nodes = ($scope.showWhitelistedCount) ? parseInt(value.nodesWhitelisted) : parseInt(value.nodes);
                            this.push(
                                {
                                    GROUP: 'Stacks',
                                    NAME: value.value,
                                    VALUE: nodes > 0 ? value.value + " (" + nodes + ")" : value.value + " (inactive)",
                                    ACTIVE: nodes > 0 ? true : false
                                }
                            );
                        }, result);
                    }
                    if (angular.isUndefined($scope.showFlavors) || (angular.isDefined($scope.showFlavors) && $scope.showFlavors == true)) {
                        angular.forEach($scope.servicePaths.flavor, function (value, key) {
                            var nodes = ($scope.showWhitelistedCount) ? parseInt(value.nodesWhitelisted) : parseInt(value.nodes);
                            if (nodes > 0) {
                                this.push(
                                    {
                                        GROUP: 'Flavors',
                                        NAME: value.value,
                                        NUMBER_VALUE: parseFloat(value.value),
                                        VALUE: nodes > 0 ? value.value + " (" + nodes + ")" : value.value + " (inactive)",
                                        ACTIVE: nodes > 0 ? true : false
                                    });
                            }
                        }, result);
                    }
                }
                servicePathsToCompare = angular.copy($scope.servicePaths);
                $scope.result = result;
            }

            var showFlavors = (angular.isUndefined($scope.showFlavors) || (angular.isDefined($scope.showFlavors) && $scope.showFlavors == true));
            var showStacks = (angular.isUndefined($scope.showStacks) || (angular.isDefined($scope.showStacks) && $scope.showStacks == true));
            /*if (showFlavors && !showStacks) {
                var orderBy = $filter('orderBy');
                $scope.result = orderBy($scope.result, 'NUMBER_VALUE' , false);
            }*/
            return $scope.result;
        };

        $scope.pathChanged = function() {
            $scope.currentPath = $scope._currentPath.NAME;
            $scope.serverChanged();
        };

        $scope.serverChanged = function() {
            $scope.updateData();
            $scope.onChanged();
            $scope.validateServer();
        };

        $scope.validateServer = function () {
            $scope.updateData();
            $scope.isCurrentPathDirty = true;
            $scope.isCurrentPathValid = false;

            var isValidationDataDefined = !$.isEmptyObject($scope.validationData);
            var hasGeneralError = isValidationDataDefined ? !$.isEmptyObject($scope.validationData.generalErrorMsg) : false;
            if ($scope.isSimple()) {
                $scope.isCurrentPathValid = $scope.currentPath !== '';
            }
            else if (angular.isDefined($scope.values.url) && $scope.values.url !== ''){
                var genericRegexp = /^([a-z]+){1}(:\/\/)([\w]+)([:]*)([0-9]*)[\/]*([\w]*)/g;
                if (genericRegexp.exec($scope.values.url)) {
                    $scope.isCurrentPathValid = true;
                }
            }

            return $scope.isCurrentPathValid && !hasGeneralError;
        };
    }]);
