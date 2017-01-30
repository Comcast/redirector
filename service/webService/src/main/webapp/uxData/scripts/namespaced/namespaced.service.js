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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.namespaced')
        .factory('namespacedService', namespacedService);

    namespacedService.$inject = ['$rootScope', '$window', 'requestsService', 'utilsService', '$q', 'COMMON_CONSTANTS', 'authService', '$state', 'RULES_CONSTANTS', 'messageService', 'REGEXP_CONSTANTS'];
    function namespacedService($rootScope, $window, requestsService, utilsService, $q, COMMON_CONSTANTS, authService, $state, RULES_CONSTANTS, messageService, REGEXP_CONSTANTS) {

        var service = {
            getNamespaces: getNamespaces,
            getNamespacesFromDS: getNamespacesFromDS,
            findPostionOfNamespaceByName: findPositionOfNamespaceByName,
            getNamespaceByName: getNamespaceByName,
            addItem: addNamespaceItem,
            delItem: deleteNamespaceItem,
            delItemFromDS: deleteNamespaceItemFromDS,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists,
            deleteListOfNamespaceItemsFromDS: deleteListOfNamespaceItemsFromDS,
            findValuesInTree: findValuesInNamespace,
            isNamespaceNameValid: isNamespaceNameValid,
            validateDuplicates: validateValueDuplicates,
            postNamespace: postNamespace,
            exportNamespace: exportNamespace,
            exportAll: exportAllNamespaces,
            deleteNamespace: deleteNamespace,
            validateAndSave: validateAndSave,
            getIndexOfDuplicateItemFromMapByKey: getIndexOfDuplicateItemFromMapByKey,
            searchNamespaces: searchNamespaces,
            getBaseUrl: getBaseUrl,
            goToFlavorRulePage: goToFlavorRulePage,
            goToTemplateFlavorRulePage: goToTemplateFlavorRulePage,
            getValuePosition: getValuePosition,
            getNamespaceFromDS: getNamespaceFromDS

        };
        return service;

        function getNamespaces() {
            var defer = $q.defer();
            requestsService.getNamespaces()
                .then(function (namespaces) {
                    if (utilsService.isNullOrUndefined(namespaces) || namespaces === '') {
                        defer.reject({'message': 'WS returned empty namespaced list data', status: 200});
                        return defer.promise;
                    } else {
                        defer.resolve(namespaces);
                    }
                }, function (reason) {
                    defer.reject({'message': reason});
                });
            return defer.promise;
        }

        function getNamespacesFromDS(baseNamespacesData) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.getAllNamespacedListsWithoutValues()
                .then(function (namespacesFromBackend) {
                    if (utilsService.isNullOrUndefined(namespacesFromBackend) || utilsService.isNullOrUndefined(namespacesFromBackend.namespace) || namespacesFromBackend.namespace === '') {
                        defer.reject({'message': 'WS returned empty namespaced list data', status: 200});
                        return defer.promise;
                    }

                    baseNamespacesData.namespace.splice(0, baseNamespacesData.namespace.length);
                    namespacesFromBackend.namespace = utilsService.toArray(namespacesFromBackend.namespace);
                    Array.prototype.push.apply(baseNamespacesData.namespace, namespacesFromBackend.namespace);
                    defer.resolve(baseNamespacesData);
                    $rootScope.isSaving = false;
                }, function (reason) {
                    defer.reject({'message': reason});
                    $rootScope.isSaving = false;
                });
            return defer.promise;
        }

        function getNamespaceFromDS(name) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.getNamespacedListByName(name)
                .then(function (namespaceObject) {
                    if (utilsService.isNullOrUndefined(namespaceObject) || utilsService.isNullOrUndefined(namespaceObject.valueSet)) {
                        defer.reject({'message': 'WS returned empty namespaced list data for \"' + name + '\"', status: 200});
                        return defer.promise;
                    }
                    namespaceObject.namespace = utilsService.toArray(namespaceObject.namespace);
                    defer.resolve(namespaceObject);
                    $rootScope.isSaving = false;
                }, function (reason) {
                    defer.reject({'message': reason});
                    $rootScope.isSaving = false;

                });
            return defer.promise;
        }

        function findPositionOfNamespaceByName(name, whereToSearch) {
            var result = {};
            result.position = null;
            if (angular.isDefined(whereToSearch)) {
                angular.forEach(whereToSearch, function (value, key) {
                    if (name === value.name) {
                        this.position = key;
                    }
                }, result);
            }
            return result.position;
        }

        function getNamespaceByName(name, whereToSearch) {
            if (angular.isDefined(whereToSearch)) {
                var pos = findPositionOfNamespaceByName(name, whereToSearch);
                if (angular.isUndefined(pos)) {
                    pos = 0;
                }
                return whereToSearch[pos];
            } else {
                return null;
            }
        }

        function goToFlavorRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'showFlavorRules', {ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.PATH_RULES_SHOW));
            }
        }

        function goToTemplateFlavorRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'templates', {name: RULES_CONSTANTS().TEMPLATES_TYPE.PATH, ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.TEMPLATE_PATH_RULES_SHOW));
            }
        }

        function redirectToState(serviceName, stateName, stateParams) {
            if (authService.isAppAccessible(serviceName)) {
                changeApplication(serviceName);
                $state.go(stateName, stateParams);
            } else {
                toastr.error('You don\'t have permissions for application: ' + serviceName, 'Error', {
                    closeButton: true,
                    timeOut: 5000
                });
            }
        }

        function redirectToHref(serviceName, showRulePageUrl) {
            if (authService.isAppAccessible(serviceName)) {
                changeApplication(serviceName);
                $window.location.href = showRulePageUrl;
            } else {
                toastr.error('You don\'t have permissions for application: ' + serviceName, 'Error', {
                    closeButton: true,
                    timeOut: 5000
                });
            }
        }

        function changeApplication(serviceName) {
            if (angular.isDefined(serviceName) && $rootScope.currentApplication !== serviceName &&
                serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                messageService.changeApp(serviceName);
            }
        }

        function getShowRulesPageUrl(serviceName, ruleName, hashPath) {
            var prodDevPath = isDevelopMode() ?
                (serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) ?
                    COMMON_CONSTANTS().DEVELOP_PATH.REDIRECTOR : COMMON_CONSTANTS().DEVELOP_PATH.DECIDER :
                (serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) ?
                    COMMON_CONSTANTS().PROD_PATH.REDIRECTOR : COMMON_CONSTANTS().PROD_PATH.DECIDER;
            return getBaseUrl() + '/' + prodDevPath + '/' + hashPath + ruleName + '#' + ruleName;
        }

        function isDevelopMode() {
            var developModePathsArray = [];
            developModePathsArray.push('/' + COMMON_CONSTANTS().DEVELOP_PATH.REDIRECTOR + '/'); // redirector
            developModePathsArray.push('/' + COMMON_CONSTANTS().DEVELOP_PATH.DECIDER + '/'); // decider
            return developModePathsArray.indexOf($window.location.href.replace($window.location.hash, '').replace(getBaseUrl(), '')) > 0;
        }

        function addNamespaceItem(namespace, name, value) {
            var defer = $q.defer();
            if (utilsService.isNullOrUndefined(name)) {
                defer.reject({'message': 'the item is empty or null or undefined'});
                return defer.promise;
            }

            if (utilsService.isNullOrUndefined(value) || value === '') {
                defer.reject({'message': 'the item is empty or null or undefined'});
                return defer.promise;
            }

            value = value.trim();

            if (utilsService.isNullOrUndefined(namespace)) {
                defer.reject({'message': 'the namespaced list is null or undefined'});
                return defer.promise;
            }

            if (utilsService.isNullOrUndefined(namespace)) {
                namespace = {
                    _name: name,
                    description: '',
                    valueSet: []
                };
            }
            if (!angular.isDefined(namespace.valueSet)) {
                namespace.valueSet = [];
            }
            if (namespace.valueSet.indexOf(value) < 0) {
                namespace.valueSet.push({value: value});
                defer.resolve(namespace);
                $rootScope.isSaving = false;
            } else {
                defer.reject({'message': 'the item \"' + value + '\" currently exists in list ' + namespace.name});
            }
            return defer.promise;
        }

        function deleteNamespaceItem(namespace, name, value) {
            var defer = $q.defer();
            if (utilsService.isNullOrUndefined(name)) {
                defer.reject({'message': 'the item is empty or null or undefined'});
                return defer.promise;
            }

            if (utilsService.isNullOrUndefined(value) || value === '') {
                defer.reject({'message': 'the item is empty or null or undefined'});
                return defer.promise;
            }

            if (utilsService.isNullOrUndefined(namespace)) {
                defer.reject({'message': 'the namespaced list array is null or undefined'});
                return defer.promise;
            }

            var positionToDelete = getValuePosition(namespace.valueSet, value);
            if (positionToDelete >= 0) {
                namespace.valueSet.splice(positionToDelete, 1);
                defer.resolve(namespace);
                $rootScope.isSaving = false;
            } else {
                defer.reject({'message': 'the item \"' + value.value + '\" is absent in list'});
            }
            return defer.promise;

        }

        function deleteNamespaceItemFromDS(name, value) {
            var defer = $q.defer();
            requestsService.deleteNamespaceValue(name, value).then(
                function (status) {
                    defer.resolve();
                },
                function (error) {
                    defer.reject({'message': error});
                });
            return defer.promise;
        }

        /**
         * Remove list of items from namespace
         * @param name
         * @param value array of values
         * @returns {*}
         */
        function deleteListOfNamespaceItemsFromDS(name, value) {
            var defer = $q.defer();
            if (!angular.isArray(value)) {
                defer.reject({'message': 'you try to remove list of values, but don\'t provide array'});
                return defer.promise;
            }

            requestsService.deleteNamespaceValues(name, value).then(
                function (status) {
                    defer.resolve();
                },
                function (error) {
                    defer.reject({'message': error});
                });
            return defer.promise;
        }

        /**
         * Remove list of items from namespace
         * @param name
         * @param value array of values
         * @returns {*}
         */
        function deleteEntitiesFromNamespacedLists(multipleValuesFromMultipleNamespacedList) {
            var defer = $q.defer();
            if (!angular.isArray(multipleValuesFromMultipleNamespacedList)) {
                defer.reject({'message': 'you are trying to remove list of values, but do not provide array'});
                return defer.promise;
            }

            requestsService.deleteEntitiesFromNamespacedLists(multipleValuesFromMultipleNamespacedList).then(
                function (status) {
                    defer.resolve();
                },
                function (error) {
                    defer.reject({'message': error});
                });
            return defer.promise;
        }

        function findValuesInNamespace(namespaces, name, value) {
            return getNamespaceByName(name, namespaces.namespace).value[namespaces.namespace[findPositionOfNamespaceByName(name, namespaces.namespace).indexOf(value)].value];
        }

        function validateValueDuplicates(namespace) {
            var defer = $q.defer();
            if (utilsService.isNullOrUndefined(namespace)) {
                defer.reject({'message': 'Namespace with name \'' + namespace.name + '\' is not exist'});
                return defer.promise;
            }

            var copiedNamespace = namespace;

            if (angular.isDefined(copiedNamespace.valueSet) && (copiedNamespace.valueSet.length === 0)) {
                delete (copiedNamespace.valueSet);
            }
            requestsService.getNamespaceDuplicates(copiedNamespace)
                .then(function (data) {
                    if (data.namespaceDuplicatesMap.entry.length > 0) {
                        defer.reject({
                            'message': 'duplicate items are found, please resolve them first',
                            'data': data.namespaceDuplicatesMap.entry
                        });
                        $rootScope.isSaving = false;
                    } if (data.containsNamespacedListsWithoutReadRights) {//if so, we cannot even see those duplicates
                        defer.reject({
                            'message': 'Found duplicates in lists you have no permission to read. Contact your   system administrator to resolve this issue',
                            'data': data.namespaceDuplicatesMap.entry
                        });
                        $rootScope.isSaving = false;
                    } else {
                        defer.resolve(data);
                    }
                }, function (reason) {
                    defer.reject({'message': reason.data.message});
                });
            return defer.promise;
        }

        function postNamespace(namespace, name) {
            var defer = $q.defer();

            if (angular.isDefined(namespace.valueSet) && (namespace.valueSet.length === 0)) {
                delete (namespace.valueSet);
            }
            requestsService.saveNamespaces(name, namespace)
                .then(function (status) {
                    $rootScope.isSaving = true;
                    defer.resolve();
                }, function (err) {
                    defer.reject(err);
                    $rootScope.isSaving = false;
                });
            return defer.promise;
        }

        function getIndexOfDuplicateItemFromMapByKey(duplicateMap, value) {
            var position = null;
            if (duplicateMap.length > 0) {
                for (var i = 0; i < duplicateMap.length; i++) {
                    if (duplicateMap[i].key === value) {
                        position = i;
                        break;
                    }
                }
                return position;
            } else {
                return null;
            }
        }


        function exportNamespace(namespaces, name) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            var item = getNamespaceByName(name, namespaces.namespace);
            if (utilsService.isNullOrUndefined(item)) {
                defer.reject({'message': 'Namespace \'' + name + '\' is not available'});
                return defer.promise;
            }
            requestsService.exportNamespace(name);
            defer.resolve();
            $rootScope.isSaving = false;
            return defer.promise;
        }

        function exportAllNamespaces() {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.exportAllNamespaces();
            defer.resolve();
            $rootScope.isSaving = false;
            return defer.promise;
        }


        function deleteNamespace(namespaces, name) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.deleteNamespace(name)
                .then(function () {
                    namespaces.namespace.splice(findPositionOfNamespaceByName(name, namespaces.namespace), 1);
                    defer.resolve(namespaces);
                }, function (reason) {
                    defer.reject(reason);
                });
            $rootScope.isSaving = false;
            return defer.promise;
        }

        /**
         * Check namespace name, should be only chars and numbers without special symbols
         * @param name
         * @returns {boolean}
         */
        function isNamespaceNameValid(name) {
            if (!REGEXP_CONSTANTS().alphaNumericalWithUnderscores.exec(name) || name === '') {
                $rootScope.isSaving = false;
                return false;
            }
            return true;
        }

        function validateAndSaveWithoutCheckDuplicates(namespace, name, autoResolve) {
            var defer = $q.defer();
            if (autoResolve) {
               requestsService.saveNamespacesAutoResolve(name, namespace)
                .then(function (status) {
                    $rootScope.isSaving = true;
                    defer.resolve();
                }, function (err) {
                    defer.reject(err);
                    $rootScope.isSaving = false;
                });
            } else {
                validateValueDuplicates(namespace)
                    .then(function () {
                        postNamespace(namespace, name)
                            .then(function () {
                                defer.resolve();
                            }, function (reason) {
                                defer.reject(reason);
                            });
                    }, function (reason) {
                        defer.reject(reason);
                    });
            }
            return defer.promise;
        }

        function validateAndSave(namespace, name, checkOverride, autoResolve) {
            $rootScope.isSaving = true;
            var defer = $q.defer();

            if (!isNamespaceNameValid(name)) {
                defer.reject({'message': 'Incorrect namespaced list name \"' + name + '\"'});
                return defer.promise;
            }

            if (checkOverride) {
                getNamespaceFromDS(name)
                    .then(function (data) {
                        defer.reject({'message': 'The namespaced list \"' + name + '\" already exists', status: 409});
                        return defer.promise;
                }, function (reason) {
                    validateAndSaveWithoutCheckDuplicates(namespace, name, autoResolve).then(function () {
                        defer.resolve();
                    }, function (reason) {
                        defer.reject(reason);
                        return defer.promise;
                    });
                });
            } else {
                validateAndSaveWithoutCheckDuplicates(namespace, name, autoResolve).then(function () {
                    defer.resolve();
                }, function (reason) {
                    defer.reject(reason);
                    return defer.promise;
                });
            }
            return defer.promise;
        }

        function searchNamespaces(name) {
            $rootScope.isSaving = true;
            var defer = $q.defer();
            requestsService.searchNamespacesByItem(name)
                .then(function (data) {
                    $rootScope.isSaving = false;
                    defer.resolve(data);
                }, function (reason) {
                    $rootScope.isSaving = false;
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function getBaseUrl() {
            return requestsService.getBaseUrl();
        }

        function getValuePosition (valueSet, value) {
            for (var i = 0; i < valueSet.length; i++) {
                if (valueSet[i].value == value.value) {
                    return i;
                }
            }
            return -1;
        }

    }
})();
