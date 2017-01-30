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

(function() {
    'use strict';
angular.module('uxData.services')

.factory("messageService", ['$rootScope',
    function ($rootScope) {

        var CHANGE_APP = "changeApp";
        var EDIT_SERVER = "editServer";
        var EDIT_RULE = "editRule";
        var GET_SERVER_DATA = "getServerData";
        var RECEIVE_SERVER = "receiveSelectServer";
        var RECEIVE_DEF_SERVER = "receiveDefServer";
        var changeApp = function (name) {

            $rootScope.$broadcast(CHANGE_APP, {
                name: name
            });
        };

        var onChangeApp = function ($scope, handler) {
            $scope.$on(CHANGE_APP, function (event, message) {
                handler(message);
            });
        };

        var editPathRule = function(ruleId) {
            $rootScope.$broadcast(EDIT_RULE, {
                ruleId: ruleId
            });
        };

        var onEditPathRule = function(context, handler) {
            context.$on(EDIT_RULE, function (event, message) {
                handler(message);
            });
        };

        var editSelectServer = function (id) {

            $rootScope.$broadcast(EDIT_SERVER, {
                id: id
            });
        };

        var onEditSelectServer = function ($scope, handler) {
            $scope.$on(EDIT_SERVER, function (event, message) {
                handler(message);
            });
        };

        var receiveServer = function (data) {

            $rootScope.$broadcast(RECEIVE_SERVER, {
                data : data
            });
        };

        var onReceiveServer = function ($scope, handler) {
            $scope.$on(RECEIVE_SERVER, function (event, message) {
                handler(message);
            });
        };

        var defServerReceived = function (data) {

            $rootScope.$broadcast(RECEIVE_DEF_SERVER, {
                data : data
            });
        };

        var onDefServerReceived = function ($scope, handler) {
            $scope.$on(RECEIVE_DEF_SERVER, function (event, message) {
                handler(message);
            });
        };

        var sendServerData = function (data) {

            $rootScope.$broadcast(GET_SERVER_DATA, {
                data : data
            });
        };

        var onGetServerData = function ($scope, handler) {
            $scope.$on(GET_SERVER_DATA, function (event, message) {
                handler(message);
            });
        };


        return {
            changeApp: changeApp,
            onChangeApp: onChangeApp,
            editSelectServer: editSelectServer,
            onEditSelectServer: onEditSelectServer,
            editPathRule: editPathRule,
            onEditPathRule: onEditPathRule,
            receiveServer: receiveServer,
            onReceiveServer: onReceiveServer,
            defServerReceived: defServerReceived,
            onDefServerReceived: onDefServerReceived,
            sendServerData: sendServerData,
            onGetServerData: onGetServerData
        };
    }]);
})();
