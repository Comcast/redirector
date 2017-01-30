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
        .factory('IndexedDBProvider', IndexedDBProvider);

    IndexedDBProvider.$inject = ['$q', 'WebServiceDataSource', 'IndexedDB_CONSTANTS'];

    function IndexedDBProvider($q, webServiceDataSource, entityCONST) {

        // OS - ObjectStore

        var db;
        var dbVersion = 3;
        var dbInitialized = false;
        var applications = [];
        var currentAppDataVersions = {};

        var READONLY = "readonly";
        var READ_WRITE = "readwrite";

        var APP_NAMES_VER = 'appsVersion';
        var NAMESPACE_VER = 'namespacesVersion';
        var NAMESPACE_DATA_NODE_VER = 'namespacesDataNodeVersion';
        var REDIRECTORCONFIG_VER = 'redirectorConfigVersion';

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

        var service = {
            getAppNames: getAppNames,
            getDB: getDB,
            initDataSource: initDataSource,
            getVersion: getVersion,
            constants: constants
        };

        initDataSource();

        return service;

        function constants() {
            return {
                NAMESPACE_DATA_NODE_VER: NAMESPACE_DATA_NODE_VER
            };
        }

//================================CODE RESPONSIBLE FOR INITIALIZATION AND DATA BASE CREATION==========================//

        function getAppNames() {
            return applications;
        }

        function getDB() {
            var deferred = $q.defer();

            initDataSource().
                then(function () {
                    deferred.resolve(db);
                }, function (error) {
                    deferred.reject(error)
                }
            );

            return deferred.promise;
        }

        function initDataSource() {

            var deferred = $q.defer();

            // 1. check if db has already been initialized
            if (dbInitialized) {
                deferred.resolve(true);
            }
            else {
                loadZookeeperSnapshot()
                    .then(function (data) {
                        // 3. now we are ready to initialize the database
                        initDataBase(data)
                            .then(function (data) {
                                deferred.resolve();
                            }, function (reason) {
                                deferred.reject(reason);
                            }
                        );
                    }, function (reason) {
                        deferred.reject(reason);
                    }
                );
            }

            return deferred.promise;
        }

        function loadZookeeperSnapshot() {
            var deferred = $q.defer();
            webServiceDataSource.loadZookeeperSnapshot()
                .then(function (zkSnapshot) {
                    applications = zkSnapshot.applicationsNames.appNames;
                    deferred.resolve(zkSnapshot);
                }, function (error) {
                    deferred.reject(error);
                });
            return deferred.promise;
        }

        function initCurrentDataVersions(applications) {
            var deferred = $q.defer();
            var snapshots = [];
            var promises = [];

            angular.forEach(applications, function (appName, index) {
                promises.push($q.when(getVersion(appName)));
            });
            promises.push($q.when(getVersion(APP_NAMES_VER)));
            promises.push($q.when(getVersion(NAMESPACE_VER)));
            promises.push($q.when(getVersion(NAMESPACE_DATA_NODE_VER)));
            promises.push($q.when(getVersion(REDIRECTORCONFIG_VER)));

            $q.all(promises)
                .then(function (results) {
                    angular.forEach(results, function (data, index) {
                        currentAppDataVersions[data.application] = data.version;
                    });
                    deferred.resolve();
                }, function (reason) {
                    deferred.reject(reason);
                }
            );

            return deferred.promise;
        }

        function getVersion(appName) {
            var deferred = $q.defer();

            if (angular.isDefined(db) && db.objectStoreNames.contains(VERSION)) {
                var OS = db.transaction(VERSION, READONLY).objectStore(VERSION);
                var request = OS.get(appName);

                request.onsuccess = function (event) {
                    var result = event.target.result;
                    var dataVersion = angular.isDefined(result) ? result.data : -2;
                    deferred.resolve({application: appName, version: dataVersion});
                };

                request.onerror = function (event) {
                    deferred.resolve({application: appName, version: -2});
                }
            }
            else {
                deferred.resolve({application: appName, version: -2});
            }

            return deferred.promise;
        }

        function initDataBase(data) {

            var deferred = $q.defer();

            if (dbInitialized) {
                deferred.resolve();
                return deferred.promise;
            }

            //var dopenRequest = window.indexedDB.deleteDatabase("redirector", dbVersion);
            var openRequest = window.indexedDB.open("redirector", dbVersion);

            openRequest.onerror = function (e) {
                console.log("error");
                deferred.reject(e);
            };

            openRequest.onupgradeneeded = function (event) {
                console.log("onupgradeneeded");
                db = event.target.result;
                initDBSchema(db);
            };

            openRequest.onsuccess = function (event) {
                console.log("onsuccess");
                db = event.target.result;

                initCurrentDataVersions(applications)
                    .then(function () {
                        initDB(db, data);
                    }, function () {
                    }
                );

                dbInitialized = true;
                deferred.resolve();
            };

            return deferred.promise;

        }

        function initDBSchema(db) {
            angular.forEach(entityCONST(), function (entityType, key) {
                switch (entityType) {
                    case STACKS:
                    case SERVERS:
                    case WHITELISTED:
                    case WHITELISTED_UPDATES:
                    case DISTRIBUTIONS:
                    case PENDINGCHANGES:
                    case STACK_BACKUP:
                    case DEFAULTPARAMS:
                    case VERSION:
                        if (!db.objectStoreNames.contains(entityType)) {
                            var OS = db.createObjectStore(entityType, {keyPath: 'appName'});
                        }
                        break;
                    case URL_RULES:
                    case PATH_RULES:
                    case TEMPLATES_URL:
                    case TEMPLATES_PATH:
                        if (!db.objectStoreNames.contains(entityType)) {
                            var OS = db.createObjectStore(entityType, {keyPath: ['appName', 'id']});
                            OS.createIndex('appNameIndex', 'appName', {unique: false});
                        }
                        break;
                    case NAMESPACE:
                    case REDIRECTORCONFIG:
                        if (!db.objectStoreNames.contains(entityType)) {
                            var OS = db.createObjectStore(entityType, {keyPath: 'name'});
                        }
                        break;
                    case APPLICATIONS:
                        if (!db.objectStoreNames.contains(APPLICATIONS)) {
                            var OS = db.createObjectStore(APPLICATIONS, {keyPath: 'applications'});
                        }
                        break;
                }
            });
        }

        function initDB(database, data) {
            angular.forEach(data.snapshots, function (snapshot, index) {
                var appName = snapshot.application;
                if (snapshot.version > currentAppDataVersions[appName]) {
                    angular.forEach(Object.keys(snapshot), function (entityType, index) {
                        switch (entityType) {
                            case PATH_RULES:
                            case TEMPLATES_PATH:
                            case URL_RULES:
                            case TEMPLATES_URL:
                                fillInRulesOS(database, entityType, appName, snapshot[entityType].if);
                                break;
                            case STACKS:
                            case PENDINGCHANGES:
                            case DISTRIBUTIONS:
                            case WHITELISTED:
                            case WHITELISTED_UPDATES:
                            case SERVERS:
                                fillInSimpleOS(database, entityType, appName, snapshot[entityType]);
                                break;
                            case DEFAULTPARAMS:
                                fillInDefaultParamsOS(database, appName, snapshot[entityType]);
                                break;
                        }
                    })
                }
                fillInSimpleOS(database, STACK_BACKUP, appName, snapshot[STACK_BACKUP]);
                fillInSimpleOS(database, VERSION, appName, snapshot[VERSION]);
            });

            if (data.namespaces.version > currentAppDataVersions[NAMESPACE_VER]) {
                fillInNamespacesOS(database, data.namespaces);
                fillInSimpleOS(database, VERSION, NAMESPACE_VER, data.namespaces.version);
                fillInSimpleOS(database, VERSION, NAMESPACE_DATA_NODE_VER, data.namespaces.dataNodeVersion);
            }

            if (data[APPLICATIONS].version > currentAppDataVersions[APP_NAMES_VER]) {
                fillInApplicationsOS(database, data[APPLICATIONS]);
                fillInSimpleOS(database, VERSION, APP_NAMES_VER, data[APPLICATIONS].version);
            }

            if (data[REDIRECTORCONFIG].version > currentAppDataVersions[REDIRECTORCONFIG_VER]) {
                fillInRedirectorConfigOS(database, data[REDIRECTORCONFIG]);
                fillInSimpleOS(database, VERSION, REDIRECTORCONFIG_VER, data[REDIRECTORCONFIG].version);
            }
        }

        function fillInRulesOS(database, entityType, appName, entitiesArray) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(entityType)) {
                clearRulesOSForApp(database, entityType, appName)
                    .then(function () {
                        OS = database.transaction(entityType, READ_WRITE).objectStore(entityType);
                        angular.forEach(entitiesArray, function (rule, index) {
                            OS.add({appName: appName, id: rule.id, data: rule});
                        });
                    }, function (error) {
                    }
                );
            }
            else {
                var error = "Failed to fill in the database for " + entityType + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }

        function clearRulesOSForApp(database, ruleType, appName) {
            var deferred = $q.defer();
            var OS = database.transaction(ruleType, READ_WRITE).objectStore(ruleType);
            var index = OS.index('appNameIndex');

            index.openCursor(appName).onsuccess = function (e) {

                var cursor = e.target.result;
                if (cursor) {
                    cursor.delete();
                    cursor.continue();
                }
                else {
                    deferred.resolve();
                }
            };
            return deferred.promise;
        }

        function fillInSimpleOS(database, entityType, appName, data) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(entityType)) {
                OS = database.transaction(entityType, READ_WRITE).objectStore(entityType);
                var deleteRequest = OS.delete(appName);
                deleteRequest.onsuccess = function (e) {
                    OS.add({appName: appName, data: data});
                }
            }
            else {
                var error = "Failed to fill in the database for " + entityType + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }

        function fillInApplicationsOS(database, data) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(APPLICATIONS)) {
                OS = database.transaction(APPLICATIONS, READ_WRITE).objectStore(APPLICATIONS);
                var deleteRequest = OS.delete(APPLICATIONS);
                deleteRequest.onsuccess = function (e) {
                    OS.add({applications: APPLICATIONS, data: data});
                }
            }
            else {
                var error = "Failed to fill in the database for " + APPLICATIONS + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }

        function fillInNamespacesOS(database, data) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(NAMESPACE)) {
                clearOS(database, NAMESPACE)
                    .then(function(){
                        OS = database.transaction(NAMESPACE, READ_WRITE).objectStore(NAMESPACE);
                        angular.forEach(data.namespace, function (namespace, index) {
                            OS.add({namespace: NAMESPACE, name: namespace.name, data: namespace});
                        });
                    }, function(){

                    }
                );
            }
            else {
                var error = "Failed to fill in the database for " + NAMESPACE + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }

        function fillInRedirectorConfigOS(database, data) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(REDIRECTORCONFIG)) {
                clearOS(database, REDIRECTORCONFIG)
                    .then(function(){
                        OS = database.transaction(REDIRECTORCONFIG, READ_WRITE).objectStore(REDIRECTORCONFIG);
                        OS.add({name: REDIRECTORCONFIG, data: data});
                    }, function(){
                    }
                );
            }
            else {
                var error = "Failed to fill in the database for " + NAMESPACE + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }

        function clearIndexedOS(database, entityType, indexName) {
            var deferred = $q.defer();
            var OS = database.transaction(entityType, READ_WRITE).objectStore(entityType);
            var index = OS.index(indexName);

            index.openCursor().onsuccess = function (e) {

                var cursor = e.target.result;
                if (cursor) {
                    cursor.delete();
                    cursor.continue();
                }
                else {
                    deferred.resolve();
                }
            };
            return deferred.promise;
        }

        function clearOS(database, entityType) {
            var deferred = $q.defer();
            var OS = database.transaction(entityType, READ_WRITE).objectStore(entityType);
            var cursor = OS.openCursor();

            cursor.onsuccess = function (e) {

                var cursor = e.target.result;
                if (cursor) {
                    cursor.delete();
                    cursor.continue();
                }
                else {
                    deferred.resolve();
                }
            };
            return deferred.promise;
        }

        function fillInDefaultParamsOS(database, appName, defaultParams) {
            var OS; // ObjectStore
            if (database.objectStoreNames.contains(DEFAULTPARAMS)) {
                OS = database.transaction(DEFAULTPARAMS, READ_WRITE).objectStore(DEFAULTPARAMS);
                var deleteRequest = OS.delete(appName);
                deleteRequest.onsuccess = function (e) {
                    OS.add({appName: appName, data: defaultParams});
                }

            }
            else {
                var error = "Failed to fill in the database for " + DEFAULTPARAMS + ". Probably database was not initialized properly.";
                log.error(error);
            }
        }
    }
})();
