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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.services')
        .factory('authService', authService);

    authService.$inject = ['$rootScope', 'currentApplication', 'utilsService', 'USER_PERMISSIONS', 'tokenService'];

    function authService($rootScope, currentApplication, utilsService, USER_PERMISSIONS, tokenService) {

        var service = {
            isAuthenticated: isAuthenticated,
            isAuthorized: isAuthorized,
            isAppAccessible: isAppAccessible,
            isDevProfile: isDevProfile
        };

        return service;

        function allPermissionsMatch (userPermissions, permissionToBeTested, permissionType) {
            if ((userPermissions.indexOf (permissionType + "-*") > 0) && permissionToBeTested.startsWith(permissionType)) {
                return true;
            }
            return false;
        }

        function isDevProfile() {
            var currentUser = tokenService.getGlobalCurrentUser();
            if (currentUser !== null &&
                currentUser.username !== null &&
                currentUser.username === "dev" &&
                currentUser.permissions !== null &&
                currentUser.permissions.indexOf(USER_PERMISSIONS().all) !== -1) {
                return true;
            } else {
                return false;
            }
        }

        function isAuthenticated() {
            return !!tokenService.getGlobalCurrentUser();
        }

        function isAuthorized(permissions) {
            var currentAppPermission = 'redirector-accessApp-' +
                (angular.isDefined($rootScope.currentApplication) ? $rootScope.currentApplication : currentApplication);
            permissions = utilsService.toArray(permissions);
            var allPermissionsPresent = true;
            var currentUser = tokenService.getGlobalCurrentUser();

            if (currentUser == null || currentUser.permissions == null) {
                return false;
            }

            for (var v in permissions) {
                if ((currentUser.permissions.indexOf(permissions[v]) === -1) &&
                    !allPermissionsMatch(currentUser.permissions, permissions[v], "read") &&
                    !allPermissionsMatch(currentUser.permissions, permissions[v], "write")) {
                    allPermissionsPresent = false;
                }
            }
            if ((currentUser.permissions.indexOf(currentAppPermission) === -1)) {
                allPermissionsPresent = false;
            }
            return isAuthenticated() && (currentUser.permissions.indexOf(USER_PERMISSIONS().all) !== -1 || allPermissionsPresent);
        }

        function isAppAccessible(appName) {
            var currentUser = tokenService.getGlobalCurrentUser();
            if (currentUser == null || currentUser.permissions == null) {
                return false;
            }
            return ((currentUser.permissions.indexOf('redirector-accessApp-' + appName) > -1) || (currentUser.permissions.indexOf(USER_PERMISSIONS().all) > -1));
        }
    }
})();
