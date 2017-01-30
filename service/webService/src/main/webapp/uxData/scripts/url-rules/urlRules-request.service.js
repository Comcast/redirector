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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


(function() {
    'use strict';

    angular
        .module('uxData.urlRules')
        .factory('urlRulesRequestService', urlRulesRequestService);

    urlRulesRequestService.$inject = ['$rootScope', '$q', '$log', 'requestsService', 'utilsService'];


    function urlRulesRequestService($rootScope, $q, $log, requestsService, utils) {

        var service = {
            getUrlRule: getUrlRule,
            getAllUrlRules: getAllUrlRules,
            deleteUrlRule: deleteUrlRule,
            getAllApprovedUrlRuleIds: getAllApprovedUrlRuleIds,
            saveUrlRule: saveUrlRule,
            loadDefaultUrlParams: loadDefaultUrlParams,
            saveDefaultUrlParams: saveDefaultUrlParams,
            exportAllUrlRules: exportAllUrlRules
        };

        return service;

        function loadDefaultUrlParams() {

            var defer = $q.defer();
            requestsService.loadDefaultUrlParams($rootScope.currentApplication)
                .then(function(data) {
                    defer.resolve({result: data});
                }, function(reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });

            return defer.promise;
        }

        function getUrlRule(ruleId) {

            var defer = $q.defer();
            requestsService.getUrlRule($rootScope.currentApplication, ruleId)
                .then(function(data) {
                    defer.resolve(data);
                }, function(reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });

            return defer.promise;
        }

        function getAllUrlRules() {
            var defer = $q.defer();
            requestsService.getUrlRulesJSON($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve({urlRules: data});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                }
            );
            return defer.promise;
        }

        function getAllApprovedUrlRuleIds() {
            var defer = $q.defer();
            requestsService.getAllUrlRuleIds($rootScope.currentApplication)
                .then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve(ruleIdsMap);
                }, function (reason) {
                    //todo: get rid of such syntax
                    defer.reject({message: reason});
                    $log.error('Failed to load existing approved url rule names. ' + reason);
                }
            );
            return defer.promise;
        }

        function deleteUrlRule(ruleId) {
            var defer = $q.defer();
            requestsService.deleteUrlRule($rootScope.currentApplication, ruleId)
                .then(function () {
                    defer.resolve();
                }, function (reason) {
                    //todo: get rid of such syntax
                    defer.reject({message: reason});
                }
            );
            return defer.promise;
        }

        function saveUrlRule(jsonRule, ruleName) {
            var defer = $q.defer();
            requestsService.saveJSONUrlRule(jsonRule, $rootScope.currentApplication, ruleName)
                .then(function () {
                    defer.resolve();
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function saveDefaultUrlParams(defaultUrlParams) {
            var defer = $q.defer();
            requestsService.saveJSONDefaultUrlParam($rootScope.currentApplication, defaultUrlParams)
                .then(function () {
                    defer.resolve();
                }, function (reason) {
                    //todo: get rid of such syntax
                    defer.reject({message: reason});
                }
            );
            return defer.promise;
        }

        function exportAllUrlRules () {
            requestsService.exportAllURLRules($rootScope.currentApplication);
        }
    }

})();
