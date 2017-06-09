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



/**
 * this service is responsible for preforming CRUD low level requests directly to indexed database
 *
 * but this service is responsible for CRUD operations only for non indexed data (like IndexedDBNamespaces, IndexedDBDistributions, ...).
 * currently we store indexes only for all types of rules.
 *
 * other modules (like IndexedDBNamespaces, IndexedDBDistributions, ...) should not work directly with database but should use
 * CRUD requests of this module
 *
 */
(function(){
    'use strict';
    angular.module('uxData.services')
        .factory('DataRequesterSimple', DataRequesterSimple);

    DataRequesterSimple.$inject = ['$q', 'IndexedDBProvider'];

    function DataRequesterSimple($q, indexedDBProvider) {

        var READONLY = "readonly";
        var READ_WRITE = "readwrite";

        var service = {
            get: get,
            getAll: getAll,
            saveByEntityName: saveByEntityName,
            saveByAppName: saveByAppName,
            deleteByName: deleteByName,
            getAllByArrayKey: getAllByArrayKey
        };

        return service;

        function get(entityType, key) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function(db){
                    var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                    var request = OS.get(key);

                    request.onsuccess = function (event) {
                        var result = event.target.result;
                        deferred.resolve(angular.isDefined(result) ? result.data : '');
                    }
                }, function(error){
                }
            );
            return deferred.promise;
        }

        function getAll(entityType) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function(db){
                    var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                    var cursor = OS.openCursor();

                    var result = [];
                    cursor.onsuccess = function(e) {
                        var cursor = e.target.result;
                        if (cursor) {
                            result.push(cursor.value.data);
                            cursor.continue();
                        }
                        else {
                            deferred.resolve(result);
                        }
                    };

                    cursor.onerror = function(e) {
                        deferred.reject(e);
                    };

                }, function(error){
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function getAllByArrayKey(entityType, arrayKey) {
            var deferred = $q.defer();
            var result = [];

            indexedDBProvider.getDB()
                .then(function(db){
                        var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                        var cursor = OS.openCursor();

                        var result = {};
                        cursor.onsuccess = function(e) {
                            var cursor = e.target.result;
                            if (cursor) {
                                if (arrayKey.indexOf(cursor.key) !== -1) {
                                    result[cursor.key] = cursor.value.data;
                                }
                                cursor.continue();
                            }
                            else {
                                deferred.resolve(result);
                            }
                        };

                        cursor.onerror = function(e) {
                            deferred.reject(e);
                        };

                    }, function(error){
                        deferred.reject(error);
                    }
                );
            return deferred.promise;
        }


        function saveByAppName(entityType, appName, entity) {
            var objectToSave = {appName: appName, data: entity};
            return save(entityType, objectToSave);
        }

        function saveByEntityName(entityType, name, entity) {
            var objectToSave = {name: name, data: entity};
            return save(entityType, objectToSave);
        }

        function save(entityType, objectToSave) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READ_WRITE).objectStore(entityType);
                    objectToSave[entityType] = entityType;
                    var putRequest = OS.put(objectToSave);

                    putRequest.onsuccess = function () {
                        deferred.resolve();
                    };

                    putRequest.onerror = function (e) {
                        deferred.reject(e);
                    };

                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;

        }

        function deleteByName(entityType, name) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READ_WRITE).objectStore(entityType);
                    var deleteRequest = OS.delete(name);

                    deleteRequest.onsuccess = function () {
                        deferred.resolve();
                    };

                    deleteRequest.onerror = function (e) {
                        deferred.reject(e);
                    };

                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }
    }
})();
