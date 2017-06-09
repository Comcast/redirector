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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */


(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('IndexedDBDistributionWithDefaultServer', IndexedDBDistributionWithDefaultServer);

    IndexedDBDistributionWithDefaultServer.$inject = ['$q', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBDistributionWithDefaultServer($q, DataRequesterSimple, IndexedDB_CONSTANTS, indexedDBCommon, webServiceDataSource) {

        var STACKS = IndexedDB_CONSTANTS().STACKS;
        var DISTRIBUTIONS = IndexedDB_CONSTANTS().DISTRIBUTION;
        var PENDING_CHANGES = IndexedDB_CONSTANTS().PENDINGCHANGES;
        var SERVERS = IndexedDB_CONSTANTS().SERVERS;

        var service = {
            saveDistribution: saveDistribution
        };

        return service;

        function saveDistribution(appName, distribution) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [DISTRIBUTIONS, PENDING_CHANGES, STACKS, SERVERS])
                .then(function (data) {
                    data.snapshot.entityToSave = {distribution: angular.fromJson(distribution).distribution, "server": angular.fromJson(distribution).defaultServer};
                    webServiceDataSource.saveDistributionWithDefaultServerOffline(appName, data.snapshot)
                        .then(function (result) {
                            DataRequesterSimple.saveByAppName(PENDING_CHANGES, appName, result.pendingChanges)
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
