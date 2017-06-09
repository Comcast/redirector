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
 * but this service is responsible for CRUD operations only for indexed data.
 * currently we store indexes only for all types of rules.
 *
 * other modules (like IndexedDBRules) should not work directly with database but should use
 * CRUD requests of this module
 *
 */

(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('DataRequesterIndexed', DataRequesterIndexed);

    DataRequesterIndexed.$inject = ['$q', 'IndexedDBProvider'];

    function DataRequesterIndexed($q, indexedDBProvider) {

        var READONLY = "readonly";
        var READ_WRITE = "readwrite";

        var service = {
            get: get,
            getAll: getAll,
            saveByIdAndAppName: saveByIdAndAppName,
            deleteByAppNameAndType: deleteByAppNameAndType
        };

        return service;

        function get(appName, entityType, entityId) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                    var request = OS.get([appName, entityId]);

                    request.onsuccess = function (event) {
                        var result = event.target.result;
                        deferred.resolve(angular.isDefined(result) ? result.data : "");
                    }
                }, function (error) {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function getAll(entityType, appName) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READONLY).objectStore(entityType);
                    var index = OS.index('appNameIndex');

                    var result = [];
                    index.openCursor(appName).onsuccess = function (e) {

                        var cursor = e.target.result;
                        if (cursor) {
                            result.push(cursor.value.data);
                            cursor.continue();
                        }
                        else {
                            deferred.resolve(result);
                        }
                    }
                }, function (error) {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function saveByIdAndAppName(appName, entity, entityType, entityId) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {
                    var OS = db.transaction(entityType, READ_WRITE).objectStore(entityType);
                    var request = OS.put({appName: appName, id: entityId, data: entity});

                    request.onsuccess = function (event) {
                        deferred.resolve();
                    };
                    request.onerror = function (event) {
                        deferred.reject();
                    };
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function deleteByAppNameAndType(appName, entityType, entityId) {
            var deferred = $q.defer();

            indexedDBProvider.getDB()
                .then(function (db) {

                    var OS = db.transaction(entityType, READ_WRITE).objectStore(entityType);
                    var request = OS.delete([appName, entityId]);

                    request.onsuccess = function (event) {
                        deferred.resolve();
                    };
                    request.onerror = function (event) {
                        deferred.reject();
                    };
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }
    }
})();
