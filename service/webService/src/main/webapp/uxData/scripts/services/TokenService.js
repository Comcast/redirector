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
 * @author Oleksandr Baturynskyi (obaturynskyi@productengine.com)
 */


(function() {
    'use strict';
angular.module('uxData.services')
    .factory("tokenService", ['localStorageService', '$window', '$rootScope', 'redirectorOfflineMode',
    function TokenService (localStorageService, $window, $rootScope, redirectorOfflineMode) {
        var saveTokenAndReloadPage = function (requestsService) {

            $rootScope.isPageReloading = false;

            var paramsStr = angular.copy($window.location.search);
            if (angular.isDefined(paramsStr) && paramsStr != "") {
                var splitItem = paramsStr.split('=');
                if (splitItem[0] == "?token") {
                    $rootScope.isPageReloading = true;

                    var hash = "";

                    if($window.location.hash != null){
                        hash = $window.location.hash;
                    }

                    var currentHash = getGlobalValue('currentHash');
                    if(currentHash != null && angular.isDefined(currentHash) && currentHash != "") {
                        hash = currentHash;
                        removeGlobalValue('currentHash');
                    }

                    var query = $window.location.href.indexOf('?');
                    var url  = $window.location.href.substring(0, query);
                    url = url + hash;

                    // $window.location.href didn't work in Chrome until setTimeout with 0 duration was added
                    setTimeout(function(){$window.location.href = url;},0);
                    return true;
                }
            }

            var user = redirectorOfflineMode ? {username: 'dev', permissions : 'permitAll'} : requestsService.getAuthInfo();
            if (user != null) {
                setGlobalCurrentUser(user);
                $rootScope.currentUser = user;
            }

            return false;
        };

        var removeGlobalValue = function (key) {
            var oldPrefix = localStorageService.getPrefix();
            localStorageService.setPrefix("");
            localStorageService.remove(key);
            localStorageService.setPrefix(oldPrefix);
        };

        var setGlobalValue = function (key, value) {
            var oldPrefix = localStorageService.getPrefix();
            localStorageService.setPrefix("");
            localStorageService.set(key, value);
            localStorageService.setPrefix(oldPrefix);
        };

        var getGlobalValue = function (key) {
            var oldPrefix = localStorageService.getPrefix();
            localStorageService.setPrefix("");
            var value = localStorageService.get(key);
            localStorageService.setPrefix(oldPrefix);
            return value;
        };

        var getGlobalCurrentUser = function () {
            return getGlobalValue(redirectorOfflineMode ? 'offlineUser' : 'currentUser');
        };

        var setGlobalCurrentUser = function (value) {
            setGlobalValue(redirectorOfflineMode ? 'offlineUser' : 'currentUser', value);
        };

        var removeGlobalUser = function () {
            removeGlobalValue('currentUser');
        };

        var setGlobalHash = function (value) {
            setGlobalValue('currentHash', value);
        };

        var getHashFromPageAndSaveItGlobal = function () {
            setGlobalHash($window.location.hash);
        };

        return {
            removeGlobalValue: removeGlobalValue,
            getGlobalCurrentUser: getGlobalCurrentUser,
            setGlobalCurrentUser: setGlobalCurrentUser,
            removeGlobalUser: removeGlobalUser,
            saveTokenAndReloadPage: saveTokenAndReloadPage,
            setGlobalValue: setGlobalValue,
            getGlobalValue: getGlobalValue,
            setGlobalHash: setGlobalHash,
            getHashFromPageAndSaveItGlobal: getHashFromPageAndSaveItGlobal
        }
    }]);
})();
