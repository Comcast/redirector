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
        .module('uxData.rules')
        .factory('rulesRequestService', rulesRequestService);

    rulesRequestService.$inject = ['$rootScope', '$q', 'requestsService', 'utilsService'];


    function rulesRequestService($rootScope, $q, requestsService, utils) {

        var service = {
            getAllRules: getAllRules,
            getRule: getRule,
            getAllRuleIds: getAllRuleIds,
            getPendingChanges: getPendingChanges,
            getServicePaths: getServicePaths,
            saveRule: saveRule,
            deleteRule: deleteRule,
            exportAllRules: exportAllRules
        };

        return service;


        function getServicePaths() {
            var defer = $q.defer();
            requestsService.getServicePaths($rootScope.currentApplication)
                .then(function (data) {
                    var servicePaths = {};
                    if (angular.isDefined(data.paths) && angular.isDefined(data.paths[0])) {
                        servicePaths = data.paths[0];
                    }
                    defer.resolve(servicePaths);
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function getPendingChanges() {
            var defer = $q.defer();
            requestsService.getPendingChanges($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve(data);
                }, function (reason) {
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function getAllRules() {
            var defer = $q.defer();
            requestsService.getAllRules($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve(data);
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function getRule(ruleId) {

            var defer = $q.defer();
            requestsService.getRule($rootScope.currentApplication, ruleId)
                .then(function(data) {
                    defer.resolve(data);
                }, function(reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });

            return defer.promise;
        }

        function getAllRuleIds() {
            var defer = $q.defer();
            requestsService.getAllRuleIds($rootScope.currentApplication)
                .then(function(data) {
                    defer.resolve(data);
                }, function(reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });

            return defer.promise;
        }

        function saveRule(jsonRule, ruleName) {
            var defer = $q.defer();
            requestsService.saveJSONRule(jsonRule, $rootScope.currentApplication, ruleName)
                .then(function () {
                    defer.resolve();
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function deleteRule(ruleName) {
            var defer = $q.defer();
            requestsService.deleteRule($rootScope.currentApplication, ruleName)
                .then(function () {
                    defer.resolve();
                }, function (reason) {
                    //todo: get rid of such syntax
                    defer.reject({message: reason});
                }
            );
            return defer.promise;
        }

        function exportAllRules () {
            requestsService.exportAllRules($rootScope.currentApplication);
        }
    }
})();
