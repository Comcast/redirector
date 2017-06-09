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
        .factory('IndexedDBDefaultServer', IndexedDBDefaultServer);

    IndexedDBDefaultServer.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBDefaultServer($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource) {

        var STACKS = entityCONST().STACKS;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var SERVERS = entityCONST().SERVERS;
        var WHITELISTED = entityCONST().WHITELISTED;
        var PATHRULES = entityCONST().PATHRULES;

        var service = {
            getDefaultServer: getDefaultServer,
            saveDefaultServer: saveDefaultServer,
            approveDefaultServer: approveDefaultServer,
            cancelDefaultServer: cancelDefaultServer
        };

        return service;

        function getDefaultServer(appName) {
            return simpleDR.get(SERVERS, appName);
        }

        function saveDefaultServer(appName, defaultServer) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [SERVERS, PENDINGCHANGES, STACKS])
                .then(function (data) {
                    data.snapshot.entityToSave = {server: angular.fromJson(defaultServer)};
                    webServiceDataSource.sendDefaultServerOffline(appName, data.snapshot)
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

        function approveDefaultServer(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, DISTRIBUTION, SERVERS])
                .then(function (data) {
                    webServiceDataSource.approvePendingDefaultServerOffline(appName, data)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, SERVERS, result.server, result.pendingChanges, 'default')
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

        function cancelDefaultServer(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelPendingDefaultServerOffline(appName, data.snapshot)
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
