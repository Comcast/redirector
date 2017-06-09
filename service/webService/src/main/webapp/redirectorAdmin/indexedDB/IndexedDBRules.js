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
    angular.module('uxData.services')
        .factory('IndexedDBRules', IndexedDBRules);

    IndexedDBRules.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource', 'utilsService'];

    function IndexedDBRules($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource, utils) {

        var STACKS = entityCONST().STACKS;
        var PATHRULES = entityCONST().PATHRULES;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var SERVERS = entityCONST().SERVERS;
        var WHITELISTED = entityCONST().WHITELISTED;

        var service = {
            getAllFlavorRules: getAllFlavorRules,
            getFlavorRuleById: getFlavorRuleById,

            saveFlavorRule: saveFlavorRule,
            approveFlavorRule: approveFlavorRule,
            deleteFlavorRule: deleteFlavorRule,
            cancelFlavorRule: cancelFlavorRule
        };

        return service;

        function getAllFlavorRules(appName) {
            var deferred = $q.defer();

            indexedDR.getAll(PATHRULES, appName)
                .then(function(rules){
                    deferred.resolve({if: rules});
                }, function(error){
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function getFlavorRuleById(appName, id) {
            var deferred = $q.defer();
            indexedDR.get(appName, PATHRULES, id)
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

        function saveFlavorRule(appName, rule, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, STACKS, PATHRULES])
                .then(function (data) {
                    data.snapshot.entityToSave = {if: angular.fromJson(rule)};
                    webServiceDataSource.saveFlavorRuleOffline(appName, ruleId, data.snapshot)
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

        function deleteFlavorRule(appName, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, PATHRULES])
                .then(function (data) {
                    webServiceDataSource.deleteFlavorRuleOffline(appName, ruleId, data.snapshot)
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

        function approveFlavorRule(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, DISTRIBUTION, SERVERS])
                .then(function (data) {
                    webServiceDataSource.approveFlavorRuleOffline(appName, ruleId, data)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, PATHRULES, result.if, result.pendingChanges, ruleId)
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

        function cancelFlavorRule(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelFlavorRuleOffline(appName, ruleId, data.snapshot)
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
