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


(function () {
    'use strict';

    angular
        .module('uxData.deciderRules')
        .factory('deciderRulesRequestService', deciderRulesRequestService);

    deciderRulesRequestService.$inject = ['$rootScope', '$q', 'requestsService', 'utilsService'];


    function deciderRulesRequestService($rootScope, $q, requestsService, utils) {

        var service = {
            getAllRules: getAllRules,
            getRuleIds: getRuleIds,
            getRule: getRule,
            saveRule: saveRule,
            deleteRule: deleteRule,
            getPartners: getPartners,
            exportRule: requestsService.exportRule,
            exportAllRules: requestsService.exportAllRules
        };

        return service;


        function getAllRules() {
            var defer = $q.defer();
            requestsService.getRules($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve({rules: data});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function getRuleIds() {
            var defer = $q.defer();
            requestsService.getRuleIds()
                .then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve({ids: ruleIdsMap});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function getRule(ruleId) {
            var defer = $q.defer();
            requestsService.getRule(ruleId)
                .then(function (data) {
                    defer.resolve({rule: data});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function saveRule(rule, ruleId) {
            var defer = $q.defer();
            requestsService.saveRule(rule, ruleId)
                .then(function (data) {
                    defer.resolve({rule: data});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function deleteRule(ruleId) {
            var defer = $q.defer();
            requestsService.deleteRule('', ruleId)
                .then(function (data) {
                    defer.resolve();
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }

        function getPartners() {
            var defer = $q.defer();
            requestsService.getPartnersJSON()
                .then(function (data) {
                    defer.resolve({partners: data});
                }, function (reason) {
                    defer.reject({message: reason.data, status: reason.status});
                });
            return defer.promise;
        }
    }
})();

