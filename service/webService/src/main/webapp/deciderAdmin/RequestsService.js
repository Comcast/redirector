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


(function() {
    'use strict';
    angular.module('uxData.services')
        .factory('requestsService', RequestsService);

    RequestsService.$inject = ['$http', '$location', '$q', '$rootScope', '$state', 'STATES_CONSTANTS'];

    function RequestsService($http, $location, $q, $rootScope, $state, STATES_CONSTANTS) {
        var getData = function (url, headers) {
            var defer = $q.defer();
            $http({
                method: 'GET',
                url: url,
                headers: headers
            }).success(function (data, status, header) {
                defer.resolve(data);
            }).error(function (data, status, header) {
                if (status !== 401) defer.reject({data: data, status: status});
            });

            return defer.promise;
        };

        var getPartners = function () {
            return getData(getDeciderBaseApiUrl() + 'partners', {
                'Content-Type': 'application/json',
                'Accept': 'application/json, text/json, */*;'
            });
        };

        var savePartners = function (applicationName, partners) {
            var defer = $q.defer();
            $http({
                method: 'POST',
                data: partners,
                headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
                url: getDeciderBaseApiUrl() + 'partners'
            }).success(function (data, status, header) {
                defer.resolve(data);
            }).error(function (data, status, header) {
                if (status !== 401) defer.reject(data);
            });

            return defer.promise;
        };

        var addOrUpdatePartner = function (partner) {
            return updateData(getDeciderBaseApiUrl() + 'partners', partner, {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            });
        };

        var deletePartner = function (partnerId) {
            return deleteItem(getDeciderBaseApiUrl() + 'partners/' + partnerId, {'Accept': 'application/json'});
        };


//=============================  RULES  ========================================//
        var saveRule = function (ruleJSON, ruleName) {
            var url = getDeciderBaseApiUrl() + 'deciderRules' + '/' + ruleName;
            var defer = $q.defer();
            $http({
                method: 'POST',
                data: ruleJSON,
                url: url,
                headers: {'Content-Type': 'application/json'}
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                })
                .error(function (data, status, header) {
                    if (status !== 401) defer.reject(data);
                });
            return defer.promise;
        };

        var getRules = function () {
            return getData(getDeciderBaseApiUrl() + 'deciderRules', {'Accept': 'application/json'});
        };

        var getRule = function (ruleId) {
            return getData(getDeciderBaseApiUrl() + 'deciderRules/' + ruleId , {'Accept': 'application/json'});
        };

        var getRuleIds = function () {
            return getData(getDeciderBaseApiUrl() + 'deciderRules/ids', {'Accept': 'application/json'});
        };

        var deleteRule = function (appName, ruleId) {
            var currUrl = getDeciderBaseApiUrl() + 'deciderRules/' + ruleId;

            var defer = $q.defer();
            $http({
                method: 'DELETE',
                url: currUrl,
                headers: {}
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                })
                .error(function (data, status, header) {
                    if (status !== 401) defer.reject(data);
                });
            return defer.promise;
        };

        var getExportPartners = function () {
            window.open(getDeciderBaseApiUrl() + 'partners/export');
        };

        var getExportPartner = function (partnerId) {
            window.open(getDeciderBaseApiUrl() + 'partners/export/' + partnerId);
        };

        var getPartners = function () {
            return getData(getDeciderBaseApiUrl() + 'partners', {'Accept': 'application/xml'});
        };

        var getPartnersJSON = function () {
            return getData(getDeciderBaseApiUrl() + 'partners', {'Accept': 'application/json'});
        };

        var getBaseUrl = function () {
            var baseUrl = $location.absUrl();
            if (baseUrl.lastIndexOf('#/') !== -1) {
                baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('#/'));
            }
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));

            return baseUrl;
        };

        var getDeciderBaseApiUrl = function () {
            return getBaseUrl() + '/data/decider/';
        };

        var getRedirectorBaseApiUrl = function () {
            return getBaseUrl() + '/data/';
        };

        var getServicePaths = function (appName) {
            var success = function (test) {
                test.call(this, {paths: ''}, '');
            };
            var error = function () {
            };
            return {
                success: success,
                error: error
            }
        };

//=============================This is TEMP copy-paste from redirectorRequestService==//
//============================ Namespaced Lists ======================================//
        var getNamespaces = function () {
            return getData(getRedirectorBaseApiUrl() + 'namespacedLists/getAllNamespacedLists', {'Accept': 'application/json'});
        };

        function getAllNamespacedListsWithoutValues () {
            return getData(getRedirectorBaseApiUrl() + 'namespacedLists/getAllNamespacedListsWithoutValues', {'Accept': 'application/json'});
        }

        var saveNamespaces = function (name, namespace) {
            return saveData(getRedirectorBaseApiUrl() + 'namespacedLists/addNewNamespaced/' + name, namespace, {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            });
        };

        var getNamespaceDuplicates = function (namespace) {
            return saveData(getRedirectorBaseApiUrl() + 'namespacedLists/duplicates', namespace, {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            });
        };

        var getNamespacedListByName = function (name) {
            return getData(getRedirectorBaseApiUrl() + 'namespacedLists/getOne/' + name, {'Accept': 'application/json'});
        };

        var deleteNamespace = function (name) {
            return deleteItem(getRedirectorBaseApiUrl() + 'namespacedLists/' + name, {'Accept': 'application/json'});
        };

        var deleteNamespaceValue = function (name, value) {
            return deleteItem(getRedirectorBaseApiUrl() + 'namespacedLists/' + name + '/' + encodeURIComponent(value), {'Accept': 'application/json'});
        };

        var exportNamespace = function (name) {
            window.open(getRedirectorBaseApiUrl() + 'namespacedLists/export/' + name);
        };

        var exportAllNamespaces = function (namespace) {
            window.open(getRedirectorBaseApiUrl() + 'namespacedLists/export');
        };

        var searchNamespacesByItem = function (item) {
            return getData(getRedirectorBaseApiUrl() + 'namespacedLists/search/' + encodeURIComponent(item), {});
        };

        var getMultipleRulesDependingOnNamespaced = function(rules) {
            if (angular.isDefined(rules) && angular.isArray(rules)) {
                var url = getRedirectorBaseApiUrl() + 'namespacedLists/dependingRulesMultiple/';
                for (var i = 0; i < rules.length; i++) {
                    url += ((i === rules.length - 1) ? rules[i] : rules[i] + ',');
                }
                return getData(url, {'Accept': 'application/json'});
            }
            else return null;
        };

        var bulkDeleteNamespacedValues = function (name, entitiesFromFile) {
            return saveData(getRedirectorBaseApiUrl() + 'namespacedLists/deleteNamespacedEntities/' + name, {entities: entitiesFromFile}, {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            });
        };

        var saveData = function (url, data, headers) {
            var defer = $q.defer();
            $http({
                method: 'POST',
                data: data,
                url: url,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                }).error(function (data, status, header) {
                    if (status !== 401) defer.reject(data);
                });
            return defer.promise;
        };

        var updateData = function (url, data, headers) {
            var defer = $q.defer();
            $http({
                method: 'PUT',
                data: data,
                url: url,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                }).error(function (data, status, header) {
                    if (status !== 401) defer.reject(data);
                });
            return defer.promise;
        };

        var deleteItem = function (url, headers, aData) {
            var defer = $q.defer();
            $http({
                method: 'DELETE',
                url: url,
                data: aData,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                })
                .error(function (data, status, header) {
                    if (status !== 401) defer.reject(data);
                });
            return defer.promise;
        };


         var goToNamespacedListsSearchPage = function (searchPhrase) {
            $state.go(STATES_CONSTANTS().namespacesSearch, {serviceName: $rootScope.currentApplication, search: searchPhrase});
         };


        var goToEditRulePage = function (ruleName, serviceName) {
            $state.go(STATES_CONSTANTS().rulesEdit, {ruleId: ruleName, serviceName: serviceName});
        };

        var goToEditNamespacedListPage = function (namespacedListName) {
            $state.go(STATES_CONSTANTS().namespacesEdit, {name: namespacedListName});
        };

//=============================End of TEMP copy-paste from redirectorRequestService=====//
//================================ Auth Info ===========================================//
        var getAuthInfo = function () {
            var result = null;
            $.ajax({
                url: getBaseUrl() + '/data/auth/authinfo',
                async: false,
                dataType: 'json',
                headers: {}
            }).done(function (data, status, header) {
                result = data;
            });

            return result;
        };

        var deleteNamespaceValues = function (name, values) {
            if (angular.isArray(values)) {
                var url = getBaseUrl() + '/data/namespacedLists/' + name + '/';
                for (var i = 0; i < values.length; i++) {
                    url += encodeURIComponent(values[i]);
                    url += (i === values.length - 1) ? '' : ',';
                }
                return deleteItem(url, {'Accept': 'application/json'});
            } else {
                return deleteNamespaceValue(name, values);
            }
        };

        function deleteEntitiesFromNamespacedLists (valuesByNames) {
            var url = getBaseUrl() + '/data/namespacedLists/deleteEntitiesFromNamespacedLists';
            return saveData(url, valuesByNames, {'Content-Type': 'application/json','Accept': 'application/json'});
        }

        var exportRule = function (name) {
            window.open(getDeciderBaseApiUrl() + 'deciderRules/export/' + name);
        };

        var exportAllRules = function () {
            window.open(getDeciderBaseApiUrl() + 'deciderRules/export/');
        };

        return {
            getPartners: getPartners,
            getPartnersJSON: getPartnersJSON,
            getExportPartners: getExportPartners,
            getExportPartner: getExportPartner,
            savePartners: savePartners,
            addOrUpdatePartner: addOrUpdatePartner,
            deletePartner: deletePartner,
            saveRule: saveRule,
            getRules: getRules,
            getRule: getRule,
            getRuleIds: getRuleIds,
            deleteRule: deleteRule,
            getServicePaths: getServicePaths,
            getAuthInfo: getAuthInfo,
            getNamespaces: getNamespaces,
            getAllNamespacedListsWithoutValues: getAllNamespacedListsWithoutValues,
            saveNamespaces: saveNamespaces,
            deleteNamespace: deleteNamespace,
            exportNamespace: exportNamespace,
            exportAllNamespaces: exportAllNamespaces,
            searchNamespacesByItem: searchNamespacesByItem,
            getNamespaceDuplicates: getNamespaceDuplicates,
            getNamespacedListByName: getNamespacedListByName,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists,
            deleteNamespaceValue: deleteNamespaceValue,
            goToNamespacedListsSearchPage: goToNamespacedListsSearchPage,
            goToEditRulePage: goToEditRulePage,
            goToEditNamespacedListPage: goToEditNamespacedListPage,
            deleteNamespaceValues: deleteNamespaceValues,
            getMultipleRulesDependingOnNamespaced: getMultipleRulesDependingOnNamespaced,
            bulkDeleteNamespacedValues: bulkDeleteNamespacedValues,
            exportRule: exportRule,
            exportAllRules: exportAllRules,
            getBaseUrl: getBaseUrl
        };
    }
})();
