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
    angular.module('uxData.services')
        .factory('IndexedDBUrlRules', IndexedDBUrlRules);

    IndexedDBUrlRules.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource', 'utilsService'];

    function IndexedDBUrlRules($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource, utils) {

        var STACKS = entityCONST().STACKS;
        var URLRULES = entityCONST().URLRULES;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var PATHRULES = entityCONST().PATHRULES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var SERVERS = entityCONST().SERVERS;
        var WHITELISTED = entityCONST().WHITELISTED;

        var service = {
            getAllUrlRules: getAllUrlRules,
            getUrlRuleById: getUrlRuleById,
            saveUrlRule: saveUrlRule,
            approveUrlRule: approveUrlRule,
            deleteUrlRule: deleteUrlRule,
            cancelUrlRule: cancelUrlRule
        };

        return service;

        function getAllUrlRules(appName) {
            var deferred = $q.defer();

            indexedDR.getAll(URLRULES, appName)
                .then(function(rules){
                    deferred.resolve({if: rules});
                }, function(error){
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function getUrlRuleById(appName, id) {
            var deferred = $q.defer();
            indexedDR.get(appName, URLRULES, id)
                .then(function (rule) {
                    if (utils.isDefinedAndNotEmpty(rule)) {
                        deferred.resolve(rule);
                    } else {
                        deferred.reject("rule not found in indexDB");
                    }
                }, function (error) {
                    deferred.reject(error);
                });
            return deferred.promise;
        }

        function saveUrlRule(appName, rule, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, STACKS, URLRULES])
                .then(function (data) {
                    data.snapshot.entityToSave = {if: angular.fromJson(rule)};
                    webServiceDataSource.saveUrlRuleOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            );
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function () {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function deleteUrlRule(appName, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, URLRULES])
                .then(function (data) {
                    webServiceDataSource.deleteUrlRuleOffline(appName, ruleId, data.snapshot)
                        .then(function(result){
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function(){
                                    deferred.resolve();
                                }, function(error){
                                    deferred.reject(error);
                                }
                            );
                        }, function(error) {
                            deferred.reject(error);
                        }
                    );
                }, function () {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function approveUrlRule(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, URLRULES, DISTRIBUTION, SERVERS])
                .then(function (data) {
                    webServiceDataSource.approveUrlRuleOffline(appName, ruleId, data)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, URLRULES, result.if, result.pendingChanges, ruleId)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            )
                        }, function (error) {
                            deferred.reject(error);
                        }
                    )
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function cancelUrlRule(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelUrlRuleOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            )
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }
    }

})();
