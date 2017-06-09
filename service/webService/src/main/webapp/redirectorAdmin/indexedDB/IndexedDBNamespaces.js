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
        .factory('IndexedDBNamespaces', IndexedDBNamespaces);

    IndexedDBNamespaces.$inject = ['$q', 'WebServiceDataSource', 'IndexedDBProvider', 'IndexedDBCommon', 'IndexedDB_CONSTANTS',
        'DataRequesterSimple', 'utilsService'];

    function IndexedDBNamespaces($q, webServiceDataSource, indexedDBProvider, indexedDBCommon, entityCONST, simpleDR, utils) {

        var URL_RULES = entityCONST().URLRULES;
        var PATH_RULES = entityCONST().PATHRULES;
        var TEMPLATES_URL = entityCONST().TEMPLATEURLRULES;
        var TEMPLATES_PATH = entityCONST().TEMPLATEPATHRULES;
        var NAMESPACE = entityCONST().NAMESPACE;

        var service = {
            getNamespaces: getNamespaces,
            getNamespacedListByName: getNamespacedListByName,
            saveNamespaces: saveNamespaces,
            searchNamespacesByItem: searchNamespacesByItem,
            getNamespaceDuplicates: getNamespaceDuplicates,
            deleteNamespace: deleteNamespace,
            deleteNamespaceValues: deleteNamespaceValues,
            bulkDeleteNamespacedValues: bulkDeleteNamespacedValues,
            getMultipleRulesDependingOnNamespaced: getMultipleRulesDependingOnNamespaced,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists
        };

        return service;

        function getNamespaces() {
            return indexedDBCommon.getEntityByAppNameAndType('', NAMESPACE);
        }

        function getNamespacedListByName(name) {
            return simpleDR.get(NAMESPACE, name);
        }

        function saveNamespaces(name, namespace) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshotList([])
                .then(function (snapshotList) {
                    snapshotList.namespaced_list = namespace;
                    webServiceDataSource.validateNamespace(snapshotList)
                        .then(function (namespace) {
                            simpleDR.saveByEntityName(NAMESPACE, name, namespace)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error)
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

        function bulkDeleteNamespacedValues (name, entitiesFromFile) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshotList([NAMESPACE])
                .then(function (snapshotList) {
                    snapshotList.namespacedEntities = {entities: entitiesFromFile};
                    webServiceDataSource.bulkDeleteNamespacedValuesOffline(name, snapshotList)
                        .then(function (result) {
                            simpleDR.saveByEntityName(NAMESPACE, name, result.namespaced_list)
                                .then(function () {
                                    deferred.resolve(result.methodResponce);
                                }, function (error) {
                                    deferred.reject(error)
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

        function deleteNamespace(name) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshotList('', [PATH_RULES, URL_RULES, TEMPLATES_PATH, TEMPLATES_URL]).
                then(function (snapshotList) {
                    webServiceDataSource.deleteNamespaceOffline(name, snapshotList).
                        then(function () {
                            simpleDR.deleteByName(NAMESPACE, name).
                                then(function () {
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

        /**
         * @param item - namespace name or value of namespace
         * @returns all namespaces with names matching the item and/or namespaces which values contain the item
         */
        function searchNamespacesByItem(item) {
            var deferred = $q.defer();
            indexedDBProvider.initDataSource().
                then(function () {
                    var appNames = indexedDBProvider.getAppNames();
                    var entitiesArray = [URL_RULES, PATH_RULES, TEMPLATES_PATH, TEMPLATES_URL];
                    indexedDBCommon.getSnapshotList(appNames, entitiesArray)
                        .then(function (snapshotList) {
                            webServiceDataSource.searchNamespacesByItemOffline(item, snapshotList)
                                .then(function (searchResult) {
                                    deferred.resolve(searchResult);
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            );
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function (error) {
                    deferred.reject(error)
                }
            );

            return deferred.promise;
        }

        /**
         * @param item - namespace name or value of namespace
         * @returns all namespaces with names matching the item and/or namespaces which values contain the item
         */
        function getMultipleRulesDependingOnNamespaced(names) {
            var deferred = $q.defer();
            indexedDBProvider.initDataSource().
                then(function () {
                    var appNames = indexedDBProvider.getAppNames();
                    var entitiesArray = [PATH_RULES, TEMPLATES_PATH];
                    indexedDBCommon.getSnapshotList(appNames, entitiesArray)
                        .then(function (snapshotList) {
                            webServiceDataSource.getMultipleRulesDependingOnNamespacedOffline(names, snapshotList)
                                .then(function (searchResult) {
                                    deferred.resolve(searchResult);
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            );
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function (error) {
                    deferred.reject(error)
                }
            );

            return deferred.promise;
        }

        /**
         * Namespace should contain unique values across all other namespaces
         * @param newNamespace - namespace we are going to save
         */
        function getNamespaceDuplicates(newNamespace) {
            var deferred = $q.defer();

            indexedDBCommon.getEntityByAppNameAndType('', NAMESPACE)
                .then(function (currentNamespaces) {
                    var namespaces = {namespace: currentNamespaces.namespace, newNamespace: newNamespace};
                    webServiceDataSource.getNamespaceDuplicatesOffline(namespaces)
                        .then(function (namespaceDuplicates) {
                            deferred.resolve(namespaceDuplicates);
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

        function deleteNamespaceValues(name, values) {
            var deferred = $q.defer();

            simpleDR.get(NAMESPACE, name).
                then(function (namespace) {
                    webServiceDataSource.deleteNamespaceValuesOffline(name, values, namespace)
                        .then(function (updatedNamespace) {
                            simpleDR.saveByEntityName(NAMESPACE, name, updatedNamespace)
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

        function deleteEntitiesFromNamespacedLists(namespacesToValuesToDelete) {
            var deferred = $q.defer();
            getNamespacedValuesToDeleteByName(namespacesToValuesToDelete) //get list
                .then(function (result) {
                    webServiceDataSource.deleteEntitiesFromNamespacedListsOffline(result)
                        .then(function (returnList) {
                            saveListOfNamespacedList(returnList.namespace)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                });
                        }, function (error) {
                            deferred.reject(error);
                        });
                }, function (error) {
                    deferred.reject(error);
                });
            return deferred.promise;
        }

        function getNamespacedValuesToDeleteByName(namespacesToValuesToDelete) {
            var deferred = $q.defer();
            var result = [];
            var namespacesList = getNamespacedListsNames(namespacesToValuesToDelete);

            simpleDR.getAllByArrayKey(NAMESPACE, namespacesList).
            then(function (namespaces) {
                angular.forEach(namespacesToValuesToDelete, function(namespace, index) {
                    var namespacedValuesToDeleteByName = {};
                    namespacedValuesToDeleteByName['name'] = angular.copy(namespace.name);
                    namespacedValuesToDeleteByName['valuesToDelete'] = angular.copy(namespace.valuesToDelete);
                    namespacedValuesToDeleteByName['currentNamespacedList'] = angular.copy(namespaces[namespace.name]);
                    result.push(namespacedValuesToDeleteByName);
                });
                deferred.resolve(result);
            }, function (error) {
                deferred.reject(error);
            });

            return deferred.promise;
        }

        function getNamespacedListsNames(namespacesToValuesToDelete) {
            var nsNames = [];
            angular.forEach(namespacesToValuesToDelete, function(namespace, index) {
                nsNames.push(namespace.name);
            });
            return nsNames;
        }

        function saveListOfNamespacedList(list) {
            var result = {};
            var defer = $q.defer();
            var isSaving = true;

            var returnListNamespacedValues = [];
            var promises = [];

            if (list.length > 0) {
                angular.forEach(list, function (entity, index) {
                    promises.push($q.when(simpleDR.saveByEntityName(NAMESPACE, entity.name, entity)));
                });

                $q.all(promises).then(
                    function () {
                        angular.forEach(promises, function (promise, index) {
                            promise.then(function (data) {
                                    angular.extend(result, data);
                                },
                                function (reason) {
                                    isSaving = false;
                                    defer.reject(reason);
                                });
                        });
                        isSaving = false;
                        defer.resolve(result);
                    }, function (reason) {
                        isSaving = false;
                        defer.reject(reason);
                    });

                return defer.promise;
            }

            defer.reject("NamespacedList is not allowed.'");
            return defer.promise;
        }
    }
})();
