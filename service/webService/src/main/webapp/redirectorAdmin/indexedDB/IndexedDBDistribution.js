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
        .factory('IndexedDBDistribution', IndexedDBDistribution);

    IndexedDBDistribution.$inject = ['$q', '$log', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource', 'IndexedDBChanges'];

    function IndexedDBDistribution($q, $log, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource, indexedDBChanges) {

        var STACKS = entityCONST().STACKS;
        var DISTRIBUTIONS = entityCONST().DISTRIBUTION;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var PATHRULES = entityCONST().PATHRULES;
        var STACKBACKUP = entityCONST().STACKBACKUP;
        var DISTRIBUTION = entityCONST().DISTRIBUTION;
        var SERVERS = entityCONST().SERVERS;
        var WHITELISTED = entityCONST().WHITELISTED;

        var service = {
            getDistribution: getDistribution,
            getDistributionPendingPreview: getDistributionPendingPreview,
            saveDistribution: saveDistribution,
            approveDistribution: approveDistribution,
            cancelAllPendingDistribution: cancelAllPendingDistribution
        };

        return service;

        function getDistribution(appName) {
            return simpleDR.get(DISTRIBUTIONS, appName);
        }

        function getDistributionPendingPreview(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [DISTRIBUTIONS, PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.getDistributionPendingPreviewOffline(appName, data.snapshot)
                        .then(function (result) {
                            deferred.resolve(result);
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

        function saveDistribution(appName, distribution) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [DISTRIBUTIONS, PENDINGCHANGES, STACKS])
                .then(function (data) {
                    data.snapshot.entityToSave = {distribution: angular.fromJson(distribution)};
                    webServiceDataSource.saveDistributionOffline(appName, data.snapshot)
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

        function approveDistribution(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshotList([appName], [PENDINGCHANGES, WHITELISTED, STACKS, STACKBACKUP, PATHRULES, DISTRIBUTION, SERVERS])
                .then(function (data) {
                    webServiceDataSource.approveDistributionOffline(appName, data)
                        .then(function (result) {
                            indexedDBChanges.applyApprovedEntities(appName, result)
                                .then(function () {
                                    $log.info('Approved entities:', result);
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

        function cancelAllPendingDistribution(appName) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelDistributionOffline(appName, data.snapshot)
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
