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


angular.module('uxData').controller('applicationsCtrl', ['$scope', '$rootScope', '$sessionStorage', 'messageService', '$state', 'requestsService', 'authService', 'utilsService',
    'COMMON_CONSTANTS',
    function applicationsCtrl($scope, $rootScope, $sessionStorage, message, $state, requestsService, authService, utils, commonCONST) {
        $scope.applications = [];
        $scope.applications.push(commonCONST().APPLICATIONS.DEFAULT);
        if (angular.isUndefined($sessionStorage.currentApplication)) {
            $rootScope.currentApplication = $scope.applications[0];
        } else {
            $rootScope.currentApplication = $sessionStorage.currentApplication;
        }
        $scope.appsChooseHolder = {};
        $scope.appsChooseHolder.currentApplication = $rootScope.currentApplication;

        $scope.changeApplication = function (name) {
            $rootScope.currentApplication = name;
            message.changeApp(name);
        };

        angular.element(document).ready(function () {
            //get apps
            if (!$rootScope.isPageReloading) {
                requestsService.getApplicationNames().then(function (data, status, header) {
                    var apps = angular.isDefined(data.appNames) ? utils.toArray(data.appNames) : [];
                    angular.forEach(apps, function(app, index) {
                        if ((app !== commonCONST().APPLICATIONS.DEFAULT) && (app !== 'shell') && (authService.isAppAccessible(app))) { //APPDS-979 Xre Guide and Shell support requirement
                            $scope.applications.push(app);
                        }
                    });
                });
            }
        });

        message.onChangeApp($scope, function (message) {
            $rootScope.currentApplication = message.name;
            $sessionStorage.currentApplication = message.name;
            $scope.appsChooseHolder.currentApplication = message.name;
            requestsService.createNodesIfAbsent($rootScope.currentApplication);
        });
    }]);
