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


(function(){
    'use strict';
    angular.module('uxData.services')
        .factory('IndexedDBCommon', IndexedDBCommon);

    IndexedDBCommon.$inject = ['$q', 'IndexedDB_CONSTANTS', 'IndexedDBProvider', 'DataRequesterSimple', 'DataRequesterIndexed',
        'utilsService', 'WebServiceDataSource'];

    function IndexedDBCommon($q, entityCONST, indexedDBProvider, simpleDR, indexedDR, utils, webServiceDataSource) {

        var STACKS = entityCONST().STACKS;
        var SERVERS = entityCONST().SERVERS;
        var VERSION = entityCONST().VERSION;
        var URL_RULES = entityCONST().URLRULES;
        var NAMESPACE = entityCONST().NAMESPACE;
        var PATH_RULES = entityCONST().PATHRULES;
        var WHITELISTED = entityCONST().WHITELISTED;
        var WHITELISTED_UPDATES = entityCONST().WHITELISTED_UPDATES;
        var STACK_BACKUP = entityCONST().STACKBACKUP;
        var APPLICATIONS = entityCONST().APPLICATIONS;
        var DISTRIBUTIONS = entityCONST().DISTRIBUTION;
        var TEMPLATES_URL = entityCONST().TEMPLATEURLRULES;
        var DEFAULTPARAMS = entityCONST().DEFAULTPARAMS;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;
        var TEMPLATES_PATH = entityCONST().TEMPLATEPATHRULES;
        var REDIRECTORCONFIG = entityCONST().REDIRECTORCONFIG;

        var READONLY = "readonly";
        var READ_WRITE = "readwrite";
        
        var NS_DATA_NODE_VERSION_FIELD = 'dataNodeVersion';

        var service = {
            getSnapshotList: getSnapshotList,
            getSnapshotListForAllApps: getSnapshotListForAllApps,
            getSnapshot: getSnapshot,
            getEntityByAppNameAndType: getEntityByAppNameAndType,
            getEntityByAppNameTypeAndId: getEntityByAppNameTypeAndId,
            getEntitiesIds: getEntitiesIds,
            getIdsOfNewUnapprovedRules: getIdsOfNewUnapprovedRules,
            saveApprovedEntity: saveApprovedEntity
        };

        return service;

        function getSnapshotListForAllApps() {
            return getSnapshotList(indexedDBProvider.getAppNames());
        }

        /**
         * @param appNames - array of application names.
         * @param entitiesArray - defines entities which each snapshot will include (see 'getSnapshot' method)
         * @returns snapshotList. resulting snapshotList will contain snapshots for applications specified in appNames
         */
        function getSnapshotList(appNames, entitiesArray) {
            var deferred = $q.defer();
            var snapshotList = {snapshots: [], applicationsNames: {}, namespaces: {namespace: []}, redirectorConfig: {}};
            var promises = [];

            if (utils.isEmptyObject(appNames)) {
                appNames = indexedDBProvider.getAppNames();
            }

            if (utils.isEmptyObject(entitiesArray)) {
                entitiesArray = utils.valuesArray(entityCONST());
            }

            angular.forEach(appNames, function(appName, index) {
                promises.push($q.when(getSnapshot(appName, entitiesArray)));
            });
            promises.push($q.when(getEntityByAppNameAndType(APPLICATIONS, APPLICATIONS)));
            promises.push($q.when(getEntityByAppNameAndType(NAMESPACE, NAMESPACE)));
            promises.push($q.when(getEntityByAppNameAndType(REDIRECTORCONFIG, REDIRECTORCONFIG)));

            $q.all(promises)
                .then(function (results) {
                    angular.forEach(results, function (data, index) {
                        if (angular.isDefined(data.snapshot)) {
                            snapshotList.snapshots.push(data.snapshot);
                        }else if (angular.isDefined(data[NAMESPACE])) {
                            snapshotList.namespaces = data;
                        }else if (angular.isDefined(data[APPLICATIONS])) {
                            snapshotList.applicationsNames = data[APPLICATIONS];
                        }else if (angular.isDefined(data[REDIRECTORCONFIG])) {
                            snapshotList.redirectorConfig = data[REDIRECTORCONFIG];
                        }
                    });
                    deferred.resolve(snapshotList);
                }, function (reason) {
                    deferred.reject(reason);
                }
            );
            return deferred.promise;
        }

        /**
         *
         * @param appName - application name
         * @param entitiesArray - this is an array of IndexedDB_CONSTANTS which identifies which entities to include in
         *                        snapshot. For example to search namespace by name or value data service only needs
         *                        IndexedDB_CONSTANTS.PATHRULES,
         *                        IndexedDB_CONSTANTS.URLRULES,
         *                        IndexedDB_CONSTANTS.TEMPLATEPATHRULES,
         *                        IndexedDB_CONSTANTS.TEMPLATEURLRULES,
         * @returns
         */
        function getSnapshot(appName, entitiesArray){
            var deferred = $q.defer();
            var snapshot = {application: appName};
            var promises = [];

            angular.forEach(entitiesArray, function (entityType, key) {
                if (!angular.equals(NAMESPACE, entityType) && !angular.equals(APPLICATIONS, entityType)) {
                    promises.push($q.when(getEntityByAppNameAndType(appName, entityType)));
                }
            });

            $q.all(promises)
                .then(function (results) {
                    angular.forEach(results, function (data, index) {
                        if (angular.isDefined(data.urlParams)){
                            data.urlParams = {urlRule: data.urlParams};
                        }
                        angular.extend(snapshot, data);
                    });
                    deferred.resolve({snapshot: snapshot});
                }, function (reason) {
                    deferred.reject(reason);
                }
            );

            return deferred.promise;
        }

        /**
         *
         * @param appName - optional, for entities like NAMESPACES which are one for all applications appName may be blank
         * @param entityType
         * @returns {deferred.promise|{then}}
         */
        function getEntityByAppNameAndType(appName, entityType) {
            var deferred = $q.defer();
            switch (entityType) {
                case PATH_RULES:
                case URL_RULES:
                case TEMPLATES_URL:
                case TEMPLATES_PATH:
                    indexedDR.getAll(entityType, appName)
                        .then(function (data) {
                            var result = {};
                            result[entityType] = {if: data};
                            deferred.resolve(result);
                        }, function (error) {
                            deferred.reject(error);
                        });
                    break;
                case DEFAULTPARAMS:
                    simpleDR.get(entityType, appName)
                        .then(function (data) {
                            var result = {};
                            result[entityType] = data.urlRule;
                            deferred.resolve(result);
                        }, function (error) {
                            deferred.reject(error);
                        });
                    break;
                case DISTRIBUTIONS:
                case WHITELISTED:
                case WHITELISTED_UPDATES:
                case STACKS:
                case STACK_BACKUP:
                case SERVERS:
                case VERSION:
                case PENDINGCHANGES:
                case APPLICATIONS:
                    simpleDR.get(entityType, appName)
                        .then(function (data) {
                            var result = {};
                            result[entityType] = data;
                            deferred.resolve(result);
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                    break;
                case REDIRECTORCONFIG:
                    simpleDR.get(entityType, REDIRECTORCONFIG)
                        .then(function (data) {
                            var result = {};
                            result[entityType] = data;
                            deferred.resolve(result);
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                    break;
                case NAMESPACE:
                    simpleDR.getAll(entityType)
                        .then(function(data) {
                            var result = {};
                            result[entityType] = data;
                            indexedDBProvider
                                .getVersion(indexedDBProvider.constants().NAMESPACE_DATA_NODE_VER)
                                .then(function(data) {
                                    result[NS_DATA_NODE_VERSION_FIELD] = data.version;
                                    deferred.resolve(result);
                                }, function (error) {
                                    deferred.reject(error);
                                });
                        }, function(error) {
                            deferred.reject(error);
                        }
                    );
                    break;
            }
            return deferred.promise;
        }

        function getEntitiesIds(appName, entityType) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                    var index = OS.index('appNameIndex');

                    var ids = {id: []};
                    index.openCursor(appName).onsuccess = function (event) {
                        var cursor = event.target.result;
                        if (cursor) {
                            ids.id.push(cursor.value.id);
                            cursor.continue();
                        }
                        else {
                            deferred.resolve(ids);
                        }
                    }
                }, function (error) {
                    deferred.reject(error);
                });

            return deferred.promise;
        }

        function getIdsOfNewUnapprovedRules(appName, ruleType) {
            var deferred = $q.defer();

            getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.getIdsOfNewUnapprovedRulesOffline(ruleType, data.snapshot)
                        .then(function (result) {
                            deferred.resolve(result);
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

        function getEntityByAppNameTypeAndId(appName, entityType, id) {
            var deferred = $q.defer();
            switch (entityType) {
                case PATH_RULES:
                case URL_RULES:
                case TEMPLATES_URL:
                case TEMPLATES_PATH:
                    indexedDR.get(appName, entityType, id)
                        .then(function (data) {
                            var result = {};
                            result[entityType] = {if: data};
                            deferred.resolve(result);
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                    break;
                case NAMESPACE:
                case REDIRECTORCONFIG:
                    simpleDR.get(entityType, id)
                        .then(function(data) {
                            var result = {};
                            result[entityType] = data;
                            deferred.resolve(result);
                        }, function(error) {
                            deferred.reject(error);
                        }
                    );
                    break;
                default:
                    deferred.resolve(null);
            }
            return deferred.promise;
        }

        function saveApprovedEntity(appName, entityType, entity, pendingChanges, entityId) {
            switch (entityType) {
                case PATH_RULES:
                case URL_RULES:
                case TEMPLATES_URL:
                case TEMPLATES_PATH:
                    if (angular.isDefined(pendingChanges)) {
                        return saveApprovedIndexedEntity(appName, entityType, entity, pendingChanges, entityId);
                    }
                    else {
                        return saveOnlyApprovedIndexedEntity(appName, entityType, entity, entityId);
                    }
                    break;
                default:
                    if (angular.isDefined(pendingChanges)) {
                        return saveApprovedSimpleEntity(appName, entityType, entity, pendingChanges);
                    }
                    else {
                        return saveOnlyApprovedSimpleEntity(appName, entityType, entity);
                    }
                    break;
            }
        }

        function saveApprovedSimpleEntity(appName, entityType, entity, pendingChanges) {
            var deferred = $q.defer();
            simpleDR.saveByAppName(PENDINGCHANGES, appName, pendingChanges)
                .then(function(){
                    simpleDR.saveByAppName(entityType, appName, entity)
                        .then(function(){
                            deferred.resolve();
                        }, function(error){
                            deferred.reject(error);
                        }
                    );
                }, function(error){
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function saveOnlyApprovedSimpleEntity(appName, entityType, entity) {
            var deferred = $q.defer();
            simpleDR.saveByAppName(entityType, appName, entity)
                .then(function(){
                    deferred.resolve();
                }, function(error){
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function saveApprovedIndexedEntity(appName, entityType, entity, pendingChanges, entityId) {
            var deferred = $q.defer();
            simpleDR.saveByAppName(PENDINGCHANGES, appName, pendingChanges)
                .then(function(){
                    if (angular.isDefined(entity)) {
                        indexedDR.saveByIdAndAppName(appName, entity, entityType, entityId)
                            .then(function(){
                                deferred.resolve();
                            }, function(error){
                                deferred.reject(error);
                            }
                        );
                    } else {
                        indexedDR.deleteByAppNameAndType(appName, entityType, entityId)
                            .then(function(){
                                deferred.resolve();
                            }, function(error){
                                deferred.reject(error);
                            }
                        );
                    }
                }, function(error){
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function saveOnlyApprovedIndexedEntity(appName, entityType, entity, entityId) {
            var deferred = $q.defer();
            if (angular.isDefined(entity)) {
                indexedDR.saveByIdAndAppName(appName, entity, entityType, entityId)
                    .then(function(){
                        deferred.resolve();
                    }, function(error){
                        deferred.reject(error);
                    }
                );
            } else {
                indexedDR.deleteByAppNameAndType(appName, entityType, entityId)
                    .then(function(){
                        deferred.resolve();
                    }, function(error){
                        deferred.reject(error);
                    }
                );
            }
            return deferred.promise;
        }
    }
})();
