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
        .factory('IndexedDBDefaultUrlParams', IndexedDBDefaultUrlParams);

    IndexedDBDefaultUrlParams.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBDefaultUrlParams($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource) {

        var DEFAULTPARAMS = entityCONST().DEFAULTPARAMS;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;

        var service = {
            getDefaultUrlParams: getDefaultUrlParams,
            saveDefaultUrlParams: saveDefaultUrlParams,
            approveDefaultUrlParams: approveDefaultUrlParams,
            cancelDefaultUrlParams: cancelDefaultUrlParams
        };

        return service;

        function getDefaultUrlParams(appName) {
            return simpleDR.get(DEFAULTPARAMS, appName);
        }

        function saveDefaultUrlParams(appName, urlParams) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [DEFAULTPARAMS, PENDINGCHANGES])
                .then(function (data) {
                    data.snapshot.entityToSave = {default: {urlRule: angular.fromJson(urlParams)}};
                    webServiceDataSource.saveDefaultUrlParamsOffline(appName, data.snapshot)
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

        function approveDefaultUrlParams(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.approvePendingDefaultUrlParamsOffline(appName, data.snapshot)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, DEFAULTPARAMS, {urlRule: result.urlRule}, result.pendingChanges, ruleId)
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

        function cancelDefaultUrlParams(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelPendingDefaultUrlParamsOffline(appName, data.snapshot)
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
