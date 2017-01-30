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
        .factory('IndexedDBChanges', IndexedDBChanges);

    IndexedDBChanges.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBChanges($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource) {

        var STACKS = entityCONST().STACKS;
        var SERVERS = entityCONST().SERVERS;
        var URL_RULES = entityCONST().URLRULES;
        var PATHRULES = entityCONST().PATHRULES;
        var PATH_RULES = entityCONST().PATHRULES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var WHITELISTED = entityCONST().WHITELISTED;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var DEFAULTPARAMS = entityCONST().DEFAULTPARAMS;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var TEMPLATES_URL = entityCONST().TEMPLATEURLRULES;
        var TEMPLATES_PATH = entityCONST().TEMPLATEPATHRULES;
        var WHITELISTED_UPDATES = entityCONST().WHITELISTED_UPDATES;

        var service = {
            getPendingChanges: getPendingChanges,
            approveAll: approveAll,
            cancelAll: cancelAll,
            applyApprovedEntities: applyApprovedEntities
        };

        return service;

        function getPendingChanges(appName) {
            return simpleDR.get(PENDINGCHANGES, appName);
        }

        function cancelAll(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelAllPendingChangesOffline(appName, data.snapshot)
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

        function approveAll(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, DISTRIBUTION, SERVERS, WHITELISTED_UPDATES])
                .then(function (data) {
                    webServiceDataSource.approveAllPendingChangesOffline(appName, data)
                        .then(function (result) {
                            applyApprovedEntities(appName, result)
                                .then(function(){
                                    deferred.resolve();
                                }, function(error){
                                    deferred.reject();
                                }
                            );
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

        function applyApprovedEntities(appName, approvedEntities) {
            var deferred = $q.defer();
            var promises = [];
            angular.forEach(approvedEntities, function(value, entityType){
                switch (entityType) {
                    case PATH_RULES:
                    case URL_RULES:
                    case TEMPLATES_URL:
                    case TEMPLATES_PATH:
                        getApprovedRulesPromises(appName, value, entityType, promises);
                        break;
                    case DISTRIBUTION:
                    case SERVERS:
                    case STACKS:
                    case DEFAULTPARAMS:
                    case WHITELISTED:
                        getApprovedEntitiesPromises(appName, value, entityType, promises);
                }
            });

            promises.push($q.when(simpleDR.saveByAppName(PENDINGCHANGES, appName, approvedEntities.pendingChanges)));
            if (angular.isDefined(approvedEntities.whitelisted) && angular.isDefined(approvedEntities.whitelisted.entitiesToSave)) {
                promises.push($q.when(simpleDR.saveByAppName(STACKS, appName, approvedEntities.entitiesToUpdate.servicePaths[0])));
                promises.push($q.when(simpleDR.saveByAppName(WHITELISTED_UPDATES, appName, approvedEntities.entitiesToUpdate.whitelistedUpdates[0])));
            }

            $q.all(promises)
                .then(function(result){
                    deferred.resolve();
                }, function(error){
                    deferred.reject(error);
                }
            );
            return deferred.promise;

        }

        function getApprovedRulesPromises(appName, approvedRules, ruleType, promises) {
            if (angular.isDefined(approvedRules.entitiesToSave)) {
                angular.forEach(approvedRules.entitiesToSave.if, function(rule, index) {
                    promises.push($q.when(indexedDBCommon.saveApprovedEntity(appName, ruleType, rule, undefined, rule.id)));
                });
            }
            angular.forEach(approvedRules.entitiesToDelete, function(entity, index) {
                promises.push($q.when(indexedDBCommon.saveApprovedEntity(appName, ruleType, undefined, undefined, entity)));
            });
            return promises;
        }

        function getApprovedEntitiesPromises(appName, approvedEntities, entityType, promises) {
            angular.forEach(approvedEntities.entitiesToSave, function(entity, index) {
                switch (entityType) {
                    case DEFAULTPARAMS:
                        promises.push($q.when(indexedDBCommon.saveApprovedEntity(appName, entityType, {urlRule: entity[0]})));
                        break;
                    default:
                        promises.push($q.when(indexedDBCommon.saveApprovedEntity(appName, entityType, entity[0])));
                        break;
                }
            });
        }
    }
})();
