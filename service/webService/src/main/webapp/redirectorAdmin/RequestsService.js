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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.services')
        .factory('requestsService', RequestsService);


    RequestsService.$inject = ['$http', '$injector', '$location', '$q', '$state', '$rootScope', 'redirectorOfflineMode'];

    function RequestsService($http, $injector, $location, $q, $state, $rootScope, redirectorOfflineMode) {

        var dataSource;
        if (redirectorOfflineMode) {
            dataSource = $injector.get('IndexedDBDataSource');
        }
        else {
            dataSource = $injector.get('WebServiceDataSource');
        }

        var getData = function (url, headers) {
            var defer = $q.defer();
            $http({
                method: 'GET',
                url: url,
                headers: headers
            }).success(function (data, status, header) {
                defer.resolve(data);
            }).error(function (data, status, header) {
                if (status !== 401) {
                    defer.reject({data: data, status: status});
                }

                if (status !== 400 && data.indexOf('UnmarshalException') > 0) {
                    defer.resolve('');
                }
            });

            return defer.promise;
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
                    if (status !== 401) {
                        defer.reject({data: data, status: status});
                    }
                });
            return defer.promise;
        };

        var updateData = function (url, data, headers) {
            return $http({
                method: 'PUT',
                data: data,
                url: url,
                headers: headers
            })
                .success(function (data, status, header) {
                }).error(function (data, status, header) {
                   console.log(data);
                });
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
                    if (status !== 401) {
                        defer.reject({data: data, status: status});
                    }
                });
            return defer.promise;
        };

        var triggerModelReload = function (applicationName) {
            return saveData(getBaseApiUrl() + 'modelReload/' + applicationName, '');
        };

//============================ Namespaced Lists ======================================//
        var getNamespaces = function () {
            return dataSource.getNamespaces();
        };

        var getAllNamespacedListsWithoutValues = function () {
            return dataSource.getAllNamespacedListsWithoutValues();
        };

        var saveNamespaces = function (name, namespace) {
            return dataSource.saveNamespaces(name, namespace);
        };

        var saveNamespacesAutoResolve = function (name, namespace) {
            return dataSource.saveNamespacesAutoResolve(name, namespace);
        };

        var getNamespaceDuplicates = function (namespace) {
            return dataSource.getNamespaceDuplicates(namespace);
        };

        var getNamespacedListByName = function (name) {
            return dataSource.getNamespacedListByName(name);
        };

        var deleteNamespace = function (name) {
            return dataSource.deleteNamespace(name);
        };

        var bulkDeleteNamespacedValues = function (name, entitiesFromFile) {
            return dataSource.bulkDeleteNamespacedValues(name, entitiesFromFile);
        };

        var deleteNamespaceValue = function (name, value) {
            return dataSource.deleteNamespaceValue(name, value);
        };

        var deleteNamespaceValues = function (nsName, values) {
            return dataSource.deleteNamespaceValues(nsName, values);
        };

        var deleteEntitiesFromNamespacedLists = function (valuesByNames) {
            return dataSource.deleteEntitiesFromNamespacedLists(valuesByNames);
        };

        var exportNamespace = function (name) {
            window.open(getBaseApiUrl() + 'namespacedLists/export/' + name);
        };

        var exportAllNamespaces = function () {
            window.open(getBaseApiUrl() + 'namespacedLists/export');
        };

        var searchNamespacesByItem = function (item) {
            return dataSource.searchNamespacesByItem(item);
        };

        var sendServer = function (server, serverType, appName) {
            return saveData(getServerUrl(serverType, appName), server, {});
        };

        var exportServer = function (serverName) {
            window.open(getBaseApiUrl() + 'servers/export/' + $rootScope.currentApplication + '/' + serverName);
        };

        //============================ Default Server ======================================//
        var getServerUrl = function (serverType, appName) {
            return getBaseApiUrl() + 'servers/' + appName + '/' + serverType;
        };

        var getServer = function (serverType, appName) {
            return getData(getServerUrl(serverType, appName), {'Accept': 'application/json'});
        };

        var getDefaultUrlParams = function (appName) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName, {'Accept': 'application/json'});
        };

        var loadDefaultUrlParams = function (appName) {
            return dataSource.getDefaultUrlParams(appName);
        };

        var saveJSONDefaultUrlParam = function (appName, defaultUrlParams) {
            return dataSource.saveDefaultUrlParams(appName, defaultUrlParams);
        };

        var saveDefaultUrlParams = function (appName, urlParams) {
            return dataSource.saveDefaultUrlParams(appName, urlParams);
        };

        var getDefaultServer = function (appName) {
            return dataSource.getDefaultServer(appName);
        };

        var sendDefaultServer = function (defaultServer, appName) {
            return dataSource.sendDefaultServer(appName, defaultServer);
        };

        var getMultipleRulesDependingOnNamespaced = function(rules) {
            if (angular.isDefined(rules) && angular.isArray(rules)) {
                return dataSource.getMultipleRulesDependingOnNamespaced(rules);
            }
            else return null;
        };

//=============================  EXPORT MULTIPLE ENTITIES  ========================================//
        var exportDistributionsAlongWithServers = function () {
            window.open(getBaseApiUrl() + 'export/distribuionswithdefault/' + $rootScope.currentApplication);
        };
//=============================  RULES  ========================================//
        var saveRule = function (ruleXml, appName, ruleName) {
            var url = getBaseApiUrl() + 'rules/' + appName + '/' + ruleName + '/';
            return saveData(url, ruleXml, {'Content-Type': 'application/xml'});
        };  // TODO: pass rule as json

        var saveJSONRule = function (ruleJSON, appName, ruleName) {
            return dataSource.saveFlavorRule(ruleJSON, appName, ruleName);
        };

        var saveUrlRule = function (ruleXml, appName, ruleName) {
            var url = getBaseApiUrl() + 'urlRules/' + appName + '/' + ruleName + '/';
            return saveData(url, ruleXml, {'Content-Type': 'application/xml'});
        };

        var saveJSONUrlRule = function (jsonRule, appName, ruleName) {
            return dataSource.saveUrlRule(jsonRule, appName, ruleName);
        };

        var getRules = function (appName) {
            return getData(getBaseApiUrl() + 'rules/' + appName, {'Accept': 'application/xml'});
        };

        var getAllRules = function (appName) {
            return dataSource.getAllFlavorRules(appName);
        };

        var getAllRuleIds = function (appName) {
            return dataSource.getAllFlavorRuleIds(appName);
        };

        var getRule = function (appName, ruleId) {
            return dataSource.getFlavorRuleById(appName, ruleId);
        };
        
        var getUrlRules = function (appName) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName, {'Accept': 'application/xml'});
        };

        var getUrlRulesJSON = function (appName) {
            return dataSource.getAllUrlRules(appName);
        };

        var getAllUrlRuleIds = function (appName) {
            return dataSource.getAllUrlRuleIds(appName);
        };

        var getUrlRule = function (appName, ruleId) {
            return dataSource.getUrlRuleById(appName, ruleId);
        };

        var deleteRule = function (appName, ruleId) {
            return dataSource.deleteFlavorRule(appName, ruleId);
        };

        var deleteUrlRule = function (appName, ruleId) {
            return dataSource.deleteUrlRule(appName, ruleId);
        };

        var exportRule = function (appName, ruleName) {
            window.open(getBaseApiUrl() + 'rules/' + appName + '/export/' + ruleName);
        };

        var exportAllRules = function (appName) {
            window.open(getBaseApiUrl() + 'rules/' + appName + '/export');
        };

        var exportUrlRule = function (appName, ruleName) {
            window.open(getBaseApiUrl() + 'urlRules/' + appName + '/export/' + ruleName);
        };

        var exportAllURLRules = function (appName) {
            window.open(getBaseApiUrl() + 'urlRules/' + appName + '/export');
        };


//============================ SERVICE PATHS =======================================//
        var getServicePaths = function (appName) {
            return dataSource.getServicePaths(appName);
        };

        var exportAllServicePaths = function (appName) {
            window.open(getBaseApiUrl() + 'stacks/' + appName + '/export');
        };

//==================================================================================//
        var getBaseUrl = function () {
            var baseUrl = $location.absUrl();
            if (baseUrl.lastIndexOf('#/') !== -1) {
                baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('#/'));
            }
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));

            return baseUrl;
        };

        var getBaseApiUrl = function () {
            return getBaseUrl() + '/data/';
        };

//============================ BACKUPS =======================================//
        var triggerStacksBackup = function (appName) {
            var currUrl = getBaseApiUrl() + 'backups/' + appName + '/triggerStacksBackup';
            return saveData(currUrl, '', {});
        };

        var updateBackupUsageSchedule = function (xmlSchedule, appName) {
            var currUrl = getBaseApiUrl() + 'backups/' + appName + '/backupUsageSchedule';
            return saveData(currUrl, xmlSchedule, {'Content-Type': 'application/xml'});
        };

        var getBackupUsageSchedule = function (appName) {
            return getData(getBaseApiUrl() + 'backups/' + appName + '/backupUsageSchedule/', {'Content-Type': 'application/json'});
        };


//============================ WHITELISTED =======================================//
        var getWhitelisted = function (appName) {
            return dataSource.getWhitelisted(appName);
        };

        var  getStackComment = function (appName, path) {
            return dataSource.getStackComment(appName, path);
        };

        var  saveStackComment = function (comment, appName, path) {
            return dataSource.saveStackComment(comment, appName, path);
        };

        var sendWhitelisted = function (applicationName, wlist) {
            return dataSource.sendWhitelisted(applicationName, wlist);
        };

        var approveWhitelisted = function (appName, version) {
            return dataSource.approveWhitelisted(appName, version);
        };

        var validateAndApproveWhitelisted = function (appName, version) {
            return dataSource.validateAndApproveWhitelisted(appName, version);
        };

        var cancelWhitelisted = function (applicationName, version) {
            return dataSource.cancelWhitelisted(applicationName, version);
        };

        var approveCoreBackupWhitelisted = function (appName, version) {
            return dataSource.approveCoreBackupWhitelisted(appName, version)
        };

        var validateAndApproveCoreBackupWhitelisted = function (appName, version) {
            return dataSource.validateAndApproveCoreBackupWhitelisted(appName, version)
        };

        var cancelCoreBackupWhitelisted = function (applicationName, version) {
            return dataSource.cancelCoreBackupWhitelisted(applicationName, version)
        };

        var exportAllWhitelisted = function (appName, version) {
            window.open(getBaseApiUrl() + 'whitelist/export/' + appName);
        };

//====================== Applications ============================//
        var getApplicationNames = function () {
            return dataSource.getAllApplicationNames();
        };

        var createNodesIfAbsent = function (applicationName) {
            return getData(getBaseApiUrl() + 'redirectorService/createNodesIfNotPresent/' + applicationName, {'Accept': 'application/json'});
        };

//================================ Auth Info ===========================================//
        var getAuthInfo = function () {
            var result = null;
            $.ajax({
                url: getBaseApiUrl() + 'auth/authinfo',
                async: false,
                dataType: 'json',
                headers: {}
            }).done(function (data, status, header) {
                result = data;
            });

            return result;
        };

//================================ LOCATION CHANGES ====================================//
        var goToNamespacedListsSearchPage = function (searchPhrase) {
            $state.go(STATES_CONSTANTS().namespacesSearch, {serviceName: $rootScope.currentApplication, search: searchPhrase});
        };

        var goToEditRulePage = function (ruleName, serviceName) {
            $state.go(STATES_CONSTANTS().rulesEdit, {ruleId: ruleName, serviceName: serviceName});
        };

        var goToEditNamespacedListPage = function (namespacedListName) {
            $state.go(STATES_CONSTANTS().namespacesEdit, {name: namespacedListName});
        };


//================================ PENDING ====================================//
        function getFlavorRulesTemplates (appName) {
            return dataSource.getFlavorRulesTemplates(appName);
        }

        function getUrlRulesTemplates (appName) {
            return dataSource.getUrlRulesTemplates(appName);
        }

        function getIdsOfNewUnapprovedRules (appName, ruleType) {
            return dataSource.getIdsOfNewUnapprovedRules(appName, ruleType);
        }

        function approveFlavorRule(appName, ruleId, version) {
            return dataSource.approveFlavorRule(appName, ruleId, version);
        }

        function approveCoreBackupFlavorRule(appName, ruleId, version) {
            return dataSource.approveCoreBackupFlavorRule(appName, ruleId, version);
        }

        function approveUrlRule(appName, ruleId, version) {
            return dataSource.approveUrlRule(appName, ruleId, version);
        }

        function approveCoreBackupUrlRule(appName, ruleId, version) {
            return dataSource.approveCoreBackupUrlRule(appName, ruleId, version);
        }

        function cancelFlavorRule(appName, ruleId, version) {
            return dataSource.cancelFlavorRule(appName, ruleId, version);
        }

        function cancelCoreBackupFlavorRule(appName, ruleId, version) {
            return dataSource.cancelCoreBackupFlavorRule(appName, ruleId, version);
        }

        function cancelUrlRule(appName, ruleId, version) {
            return dataSource.cancelUrlRule(appName, ruleId, version);
        }

        function cancelCoreBackupUrlRule(appName, ruleId, version) {
            return dataSource.cancelCoreBackupUrlRule(appName, ruleId, version);
        }

        function cancelFlavorRuleTemplate(appName, ruleId, version) {
            return dataSource.cancelFlavorRuleTemplate(appName, ruleId, version);
        }

        function cancelCoreBackupFlavorRuleTemplate(appName, ruleId, version) {
            return dataSource.cancelCoreBackupFlavorRuleTemplate(appName, ruleId, version);
        }

        function cancelUrlRuleTemplate(appName, ruleId, version) {
            return dataSource.cancelUrlRuleTemplate(appName, ruleId, version);
        }

        function cancelCoreBackupUrlRuleTemplate(appName, ruleId, version) {
            return dataSource.cancelCoreBackupUrlRuleTemplate(appName, ruleId, version);
        }

        function getFlavorRuleTemplatesIds(appName) {
            return dataSource.getFlavorRuleTemplatesIds(appName);
        }

        function getUrlRuleTemplatesIds(appName) {
            return dataSource.getUrlRuleTemplatesIds(appName);
        }

        function saveTemplateFlavorRule(appName, rule, ruleId) {
            return dataSource.saveTemplateFlavorRule(appName, rule, ruleId);
        }

        function saveTemplateUrlRule(appName, rule, ruleId) {
            return dataSource.saveTemplateUrlRule(appName, rule, ruleId);
        }

        function deleteTemplateFlavorRule(appName, ruleId) {
            return dataSource.deleteTemplateFlavorRule(appName, ruleId);
        }

        function deleteTemplateUrlRule(appName, ruleId) {
            return dataSource.deleteTemplateUrlRule(appName, ruleId);
        }

        function approvePendingDefaultUrlParams(appName, version) {
            return dataSource.approvePendingDefaultUrlParams(appName, version);
        }

        function cancelPendingDefaultUrlParams(appName, version) {
            return dataSource.cancelPendingDefaultUrlParams(appName, version);
        }

        function approveCoreBackupPendingDefaultUrlParams(appName, version) {
            return dataSource.approveCoreBackupPendingDefaultUrlParams(appName, version);
        }

        function cancelCoreBackupPendingDefaultUrlParams(appName, version) {
            return dataSource.cancelCoreBackupPendingDefaultUrlParams(appName, version);
        }

        function approveTemplateUrlRule(appName, ruleId, version) {
            return dataSource.approveTemplateUrlRule(appName, ruleId, version);
        }

        function approveCoreBackupTemplateUrlRule(appName, ruleId, version) {
            return dataSource.approveCoreBackupTemplateUrlRule(appName, ruleId, version);
        }

        function approveTemplatePathRule(appName, ruleId, version) {
            return dataSource.approveTemplatePathRule(appName, ruleId, version);
        }

        function approveCoreBackupFlavorRuleTemplate(appName, ruleId, version) {
            return dataSource.approveCoreBackupFlavorRuleTemplate(appName, ruleId, version);
        }

        function approveAllPendingDistribution(appName, version) {
            return dataSource.approveAllPendingDistribution(appName, version);
        }

        function approveAllCoreBackupPendingDistribution(appName, version) {
            return dataSource.approveAllCoreBackupPendingDistribution(appName, version);
        }

        function getDistributionPendingPreview(appName) {
            return dataSource.getDistributionPendingPreview(appName);
        }

        var getPendingChangesJson = function (application) {
            return dataSource.getPendingChanges(application);
        };

        var getCoreBackupPendingChangesJson = function (application) {
            return dataSource.getCoreBackupPendingChangesJson(application);
        };

        var cancelAllPendingDistribution = function (applicationName, version) {
            return dataSource.cancelAllPendingDistribution(applicationName, version);
        };

        var cancelAllCoreBackupPendingDistribution = function (applicationName, version) {
            return dataSource.cancelAllCoreBackupPendingDistribution(applicationName, version);
        };

        var getCoreBackupDistributionPendingPreview = function (applicationName, version) {
            return dataSource.getCoreBackupDistributionPendingPreview(applicationName);
        };

        var approvePendingServer = function (appName, serverName, version) {
            return dataSource.approvePendingServer(appName, serverName, version);
        };

        var approveCoreBackupPendingServer = function (appName, serverName, version) {
            return dataSource.approveCoreBackupPendingServer(appName, serverName, version);
        };

        var cancelPendingServer = function (appName, serverName, version) {
            return dataSource.cancelPendingServer(appName, serverName, version);
        };

        var cancelCoreBackupPendingServer = function (appName, serverName, version) {
            return dataSource.cancelCoreBackupPendingServer(appName, serverName, version);
        };

        var getAllSnapshots = function () {
            return dataSource.getAllSnapshots();
        };

        var downloadCoreBackup = function(snapshots) {
            dataSource.downloadCoreBackup(snapshots);
        };

        var approveAllPendingChanges = function(appName, version) {
            return dataSource.approveAllPendingChanges(appName, version);
        };

        var validateAndApproveAllPendingChanges = function(appName, version) {
            return dataSource.validateAndApproveAllPendingChanges(appName, version);
        };

        var cancelAllPendingChanges = function(appName, version) {
            return dataSource.cancelAllPendingChanges(appName, version);
        };

        var approveAllCoreBackupPendingChanges = function(appName, version) {
            return dataSource.approveAllCoreBackupPendingChanges(appName, version);
        };

        var validateAndApproveAllCoreBackupPendingChanges = function(appName, version) {
            return dataSource.validateAndApproveAllCoreBackupPendingChanges(appName, version);
        };

        var cancelAllCoreBackupPendingChanges = function(appName, version) {
            return dataSource.cancelAllCoreBackupPendingChanges(appName, version);
        };

        var getNamespacesChangesStatus = function (item) {
            return saveData(getBaseApiUrl() + 'redirectorOffline/getAllNamespacesChanges', item, {'Content-Type': 'application/json'});
        }

        var cancelAllNamespacesChanges = function (item) {
            return saveData(getBaseApiUrl() + 'redirectorOffline/cancelAllNamespacesChanges', item, {'Content-Type': 'application/json'});
        }

        var cancelNamespacesChanges = function (item) {
            return saveData(getBaseApiUrl() + 'redirectorOffline/cancelNamespacesChanges', item, {'Content-Type': 'application/json'});
        }

        var approveAllNamespacesChanges = function (item) {
            return saveData(getBaseApiUrl() + 'redirectorOffline/approveAllNamespacesChanges', item, {'Content-Type': 'application/json'});
        }

        var approveNamespacesChanges = function (item) {
            return saveData(getBaseApiUrl() + 'redirectorOffline/approveNamespacesChanges', item, {'Content-Type': 'application/json'});
        }

        var getAllExistingApplications = function () {
            return dataSource.getAllExistingApplications();
        };
        
        var createDefaultModel = function (appName) {
            return dataSource.createDefaultModel(appName);
        };

        var atLeastOneValidModelExists = function () {
            return dataSource.atLeastOneValidModelExists();
        };
        
        var defaultModelConstructionDetails = function (appName) {
            return dataSource.defaultModelConstructionDetails(appName);
        };

        var validateApplication = function (appName) {
            return dataSource.validateApplication(appName);
        };
        
        return {
            getData: getData,
            saveData: saveData,
            updateData: updateData,
            deleteItem: deleteItem,
            getNamespaces: getNamespaces,
            getAllNamespacedListsWithoutValues: getAllNamespacedListsWithoutValues,
            saveNamespaces: saveNamespaces,
            saveNamespacesAutoResolve: saveNamespacesAutoResolve,
            deleteNamespace: deleteNamespace,
            bulkDeleteNamespacedValues: bulkDeleteNamespacedValues,
            exportNamespace: exportNamespace,
            exportAllNamespaces: exportAllNamespaces,
            searchNamespacesByItem: searchNamespacesByItem,
            getWhitelisted: getWhitelisted,
            //todo: naming is unclear (stack vs whitelisted)
            getStackComment: getStackComment,
            saveStackComment: saveStackComment,
            sendWhitelisted: sendWhitelisted,
            getDefaultServer: getDefaultServer,
            sendDefaultServer: sendDefaultServer,
            triggerStacksBackup: triggerStacksBackup,
            getServicePaths: getServicePaths,
            updateBackupUsageSchedule: updateBackupUsageSchedule,
            getBackupUsageSchedule: getBackupUsageSchedule,
            getBaseApiUrl: getBaseApiUrl,
            saveRule: saveRule,
            saveJSONRule: saveJSONRule,
            getRules: getRules,
            getAllRules: getAllRules,
            getRule: getRule,
            getAllRuleIds: getAllRuleIds,
            getUrlRulesJSON: getUrlRulesJSON,
            getAllUrlRuleIds: getAllUrlRuleIds,
            getUrlRule: getUrlRule,
            saveJSONUrlRule: saveJSONUrlRule,
            loadDefaultUrlParams: loadDefaultUrlParams,
            saveJSONDefaultUrlParam: saveJSONDefaultUrlParam,
            deleteRule: deleteRule,
            goToNamespacedListsSearchPage: goToNamespacedListsSearchPage,
            goToEditRulePage: goToEditRulePage,
            goToEditNamespacedListPage: goToEditNamespacedListPage,
            saveUrlRule: saveUrlRule,
            getUrlRules: getUrlRules,
            deleteUrlRule: deleteUrlRule,
            getDefaultUrlParams: getDefaultUrlParams,
            saveDefaultUrlParams: saveDefaultUrlParams,
            exportAllServicePaths: exportAllServicePaths,
            exportAllWhitelisted: exportAllWhitelisted,
            exportServer: exportServer,
            getAuthInfo: getAuthInfo,
            getNamespaceDuplicates: getNamespaceDuplicates,
            getNamespacedListByName: getNamespacedListByName,
            exportRule: exportRule,
            exportAllRules: exportAllRules,
            exportUrlRule: exportUrlRule,
            exportAllURLRules: exportAllURLRules,
            deleteNamespaceValue: deleteNamespaceValue,
            deleteNamespaceValues: deleteNamespaceValues,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists,
            getApplicationNames: getApplicationNames,
            createNodesIfAbsent: createNodesIfAbsent,
            triggerModelReload: triggerModelReload,
            getPendingChanges: getPendingChangesJson,
            getPendingChangesJson: getPendingChangesJson,
            getBaseUrl: getBaseUrl,
            exportDistributionsAlongWithServers: exportDistributionsAlongWithServers,
            getFlavorRulesTemplates: getFlavorRulesTemplates,
            getUrlRulesTemplates: getUrlRulesTemplates,
            getIdsOfNewUnapprovedRules: getIdsOfNewUnapprovedRules,
            approveFlavorRule: approveFlavorRule,
            approveUrlRule: approveUrlRule,
            cancelFlavorRule: cancelFlavorRule,
            cancelUrlRule: cancelUrlRule,
            cancelFlavorRuleTemplate: cancelFlavorRuleTemplate,
            cancelUrlRuleTemplate: cancelUrlRuleTemplate,
            getUrlRuleTemplatesIds: getUrlRuleTemplatesIds,
            getFlavorRuleTemplatesIds: getFlavorRuleTemplatesIds,
            saveTemplateFlavorRule: saveTemplateFlavorRule,
            saveTemplateUrlRule: saveTemplateUrlRule,
            deleteTemplateFlavorRule: deleteTemplateFlavorRule,
            deleteTemplateUrlRule: deleteTemplateUrlRule,
            approvePendingDefaultUrlParams: approvePendingDefaultUrlParams,
            cancelPendingDefaultUrlParams: cancelPendingDefaultUrlParams,
            approveTemplatePathRule: approveTemplatePathRule,
            approveTemplateUrlRule: approveTemplateUrlRule,
            approveAllPendingDistribution: approveAllPendingDistribution,
            getDistributionPendingPreview: getDistributionPendingPreview,
            cancelAllPendingDistribution: cancelAllPendingDistribution,
            approvePendingServer: approvePendingServer,
            cancelPendingServer: cancelPendingServer,
            cancelWhitelisted: cancelWhitelisted,
            approveWhitelisted: approveWhitelisted,
            validateAndApproveWhitelisted: validateAndApproveWhitelisted,
            getAllSnapshots: getAllSnapshots,
            approveAllPendingChanges: approveAllPendingChanges,
            validateAndApproveAllPendingChanges: validateAndApproveAllPendingChanges,
            cancelAllPendingChanges: cancelAllPendingChanges,
            downloadCoreBackup: downloadCoreBackup,
            getCoreBackupPendingChangesJson: getCoreBackupPendingChangesJson,

            approveCoreBackupFlavorRule: approveCoreBackupFlavorRule,
            cancelCoreBackupFlavorRule: cancelCoreBackupFlavorRule,

            approveCoreBackupUrlRule: approveCoreBackupUrlRule,
            cancelCoreBackupUrlRule: cancelCoreBackupUrlRule,

            approveCoreBackupFlavorRuleTemplate: approveCoreBackupFlavorRuleTemplate,
            cancelCoreBackupFlavorRuleTemplate: cancelCoreBackupFlavorRuleTemplate,

            approveCoreBackupTemplateUrlRule: approveCoreBackupTemplateUrlRule,
            cancelCoreBackupUrlRuleTemplate: cancelCoreBackupUrlRuleTemplate,

            approveCoreBackupPendingDefaultUrlParams: approveCoreBackupPendingDefaultUrlParams,
            cancelCoreBackupPendingDefaultUrlParams: cancelCoreBackupPendingDefaultUrlParams,

            approveAllCoreBackupPendingDistribution: approveAllCoreBackupPendingDistribution,
            cancelAllCoreBackupPendingDistribution: cancelAllCoreBackupPendingDistribution,
            getCoreBackupDistributionPendingPreview: getCoreBackupDistributionPendingPreview,

            approveCoreBackupPendingServer: approveCoreBackupPendingServer,
            cancelCoreBackupPendingServer: cancelCoreBackupPendingServer,

            approveCoreBackupWhitelisted: approveCoreBackupWhitelisted,
            validateAndApproveCoreBackupWhitelisted: validateAndApproveCoreBackupWhitelisted,
            cancelCoreBackupWhitelisted: cancelCoreBackupWhitelisted,

            approveAllCoreBackupPendingChanges: approveAllCoreBackupPendingChanges,
            validateAndApproveAllCoreBackupPendingChanges: validateAndApproveAllCoreBackupPendingChanges,
            cancelAllCoreBackupPendingChanges: cancelAllCoreBackupPendingChanges,
            
            getMultipleRulesDependingOnNamespaced: getMultipleRulesDependingOnNamespaced,

            getNamespacesChangesStatus: getNamespacesChangesStatus,
            approveNamespacesChanges: approveNamespacesChanges,
            approveAllNamespacesChanges: approveAllNamespacesChanges,
            cancelAllNamespacesChanges: cancelAllNamespacesChanges,
            cancelNamespacesChanges: cancelNamespacesChanges,
            getAllExistingApplications: getAllExistingApplications,
            createDefaultModel: createDefaultModel,
            atLeastOneValidModelExists: atLeastOneValidModelExists,
            defaultModelConstructionDetails: defaultModelConstructionDetails,
            validateApplication: validateApplication
        };
    }
})();
