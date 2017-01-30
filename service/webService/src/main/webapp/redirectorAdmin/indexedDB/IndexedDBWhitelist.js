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


(function(){
    'use strict';
    angular.module('uxData.services')
        .factory('IndexedDBWhitelisted', IndexedDBWhitelisted);

    IndexedDBWhitelisted.$inject = ['$q', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBWhitelisted($q, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource) {

        var STACKS = entityCONST().STACKS;
        var WHITELISTED = entityCONST().WHITELISTED;
        var WHITELISTED_UPDATES = entityCONST().WHITELISTED_UPDATES;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var PATHRULES = entityCONST().PATHRULES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var SERVERS = entityCONST().SERVERS;

        var service = {
            getWhitelisted: getWhitelisted,
            saveWhitelisted: saveWhitelisted,
            approveWhitelisted: approveWhitelisted,
            cancelWhitelisted: cancelWhitelisted

        };

        return service;

        function getWhitelisted(appName) {
            return simpleDR.get(WHITELISTED_UPDATES, appName);
        }

        function saveWhitelisted(appName, whitelisted) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [WHITELISTED, PENDINGCHANGES, DISTRIBUTION, STACKS, SERVERS])
                .then(function (data) {
                    data.snapshot.entityToSave = {whitelisted: angular.fromJson(whitelisted)};
                    webServiceDataSource.saveWhitelistedOffline(appName, data.snapshot)
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
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function approveWhitelisted(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, DISTRIBUTION, SERVERS, WHITELISTED_UPDATES])
                .then(function (data) {
                    webServiceDataSource.approveWhitelistedOffline(appName, data)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, WHITELISTED, result.whitelisted, result.pendingChanges)
                                .then(function () {
                                    simpleDR.saveByAppName(STACKS, appName, result.entitiesToUpdate.servicePaths[0])
                                        .then(function () {
                                            simpleDR.saveByAppName(WHITELISTED_UPDATES, appName, result.entitiesToUpdate.whitelistedStackUpdates[0])
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
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            );
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

        function cancelWhitelisted(appName){
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelWhitelistedOffline(appName, data.snapshot)
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
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }
    }
})();
