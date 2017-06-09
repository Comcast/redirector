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
        .factory('IndexedDBDataSource', IndexedDBDataSource);

    IndexedDBDataSource.$inject = ["$q",'WebServiceDataSource', 'IndexedDB_CONSTANTS',
        'IndexedDBProvider', 'IndexedDBNamespaces', 'IndexedDBDistribution', 'IndexedDBWhitelisted', 'IndexedDBRules',
        'IndexedDBUrlRules', 'IndexedDBRulesTemplates', 'IndexedDBUrlRulesTemplates', 'IndexedDBDefaultUrlParams',
        'IndexedDBChanges', 'IndexedDBCommon', 'IndexedDBServicePaths', 'IndexedDBDefaultServer',
        'IndexedDBDistributionWithDefaultServer', 'IndexedDBSettings'];

    function IndexedDBDataSource($q, webServiceDataSource, entityCONST,
                                 indexedDBProvider, indexedDBNamespaces, indexedDBDistribution, indexedDBWhitelisted, indexedDBRules,
                                 indexedDBUrlRules, indexedDBRulesTemplates, indexedDBUrlRulesTemplates, indexedDBDefaultUrlParams,
                                 indexedDBChanges, indexedDBCommon, indexedDBServicePaths, indexedDBDefaultServer,
                                 IndexedDBDistributionWithDefaultServer, indexedDBSettings) {

        var URL_RULES = entityCONST().URLRULES;
        var PATH_RULES = entityCONST().PATHRULES;
        var DISTRIBUTIONS = entityCONST().DISTRIBUTION;
        var TEMPLATES_URL = entityCONST().TEMPLATEURLRULES;
        var TEMPLATES_PATH = entityCONST().TEMPLATEPATHRULES;

        var service = {

            getServicePaths: getServicePaths,

            // flavor rules
            getAllFlavorRules: getAllFlavorRules,
            getFlavorRuleById: getFlavorRuleById,
            getAllFlavorRuleIds: getAllFlavorRuleIds,
            saveFlavorRule: saveFlavorRule,
            deleteFlavorRule: deleteFlavorRule,
            approveFlavorRule: approveFlavorRule,
            cancelFlavorRule: cancelFlavorRule,

            // url rules
            getAllUrlRules: getAllUrlRules,
            getUrlRuleById: getUrlRuleById,
            getAllUrlRuleIds: getAllUrlRuleIds,
            saveUrlRule: saveUrlRule,
            deleteUrlRule: deleteUrlRule,
            approveUrlRule: approveUrlRule,
            cancelUrlRule: cancelUrlRule,

            // flavor templates
            getFlavorRulesTemplates: getFlavorRulesTemplates,
            getFlavorRuleTemplatesIds: getFlavorRuleTemplatesIds,
            saveTemplateFlavorRule: saveTemplateFlavorRule,
            deleteTemplateFlavorRule: deleteTemplateFlavorRule,
            cancelFlavorRuleTemplate: cancelFlavorRuleTemplate,

            // url templates
            getUrlRulesTemplates: getUrlRulesTemplates,
            getUrlRuleTemplatesIds: getUrlRuleTemplatesIds,
            saveTemplateUrlRule: saveTemplateUrlRule,
            deleteTemplateUrlRule: deleteTemplateUrlRule,
            approveTemplateUrlRule: approveTemplateUrlRule,
            cancelUrlRuleTemplate: cancelUrlRuleTemplate,

            // default url params
            getDefaultUrlParams: getDefaultUrlParams,
            saveDefaultUrlParams: saveDefaultUrlParams,
            approvePendingDefaultUrlParams: approvePendingDefaultUrlParams,
            cancelPendingDefaultUrlParams: cancelPendingDefaultUrlParams,
            approveTemplatePathRule: approveTemplatePathRule,

            // pending changes
            getPendingChanges: getPendingChanges,
            getIdsOfNewUnapprovedRules: getIdsOfNewUnapprovedRules,

            // distributions
            saveDistribution: saveDistribution,
            getDistribution: getDistribution,
            approveAllPendingDistribution: approveAllPendingDistribution,
            getDistributionPendingPreview: getDistributionPendingPreview,
            cancelAllPendingDistribution: cancelAllPendingDistribution,
            saveDistributionWithDefaultServer: saveDistributionWithDefaultServer,

            // default sever
            getDefaultServer: getDefaultServer,
            sendDefaultServer: sendDefaultServer,
            approvePendingServer: approvePendingServer,
            cancelPendingServer: cancelPendingServer,

            // whitelisted
            getWhitelisted: getWhitelisted,
            //todo: naming is unclear (stack vs whitelisted)
            getStackComment: getStackComment,
            saveStackComment: saveStackComment,
            sendWhitelisted: sendWhitelisted,
            approveWhitelisted: approveWhitelisted,
            cancelWhitelisted: cancelWhitelisted,

            // namespaces
            getNamespaces: getNamespaces,
            getAllNamespacedListsWithoutValues: getNamespaces,//because all ops are performed on front-end, there is no need of such optimisation
            getNamespacedListByName: getNamespacedListByName,
            saveNamespaces: saveNamespaces,
            deleteNamespace: deleteNamespace,
            deleteNamespaceValue: deleteNamespaceValue,
            deleteNamespaceValues: deleteNamespaceValues,
            getNamespaceDuplicates: getNamespaceDuplicates,
            searchNamespacesByItem: searchNamespacesByItem,
            getAllSnapshots: getAllSnapshots,
            getMultipleRulesDependingOnNamespaced: getMultipleRulesDependingOnNamespaced,
            bulkDeleteNamespacedValues: bulkDeleteNamespacedValues,

            // pending changes
            approveAllPendingChanges: approveAllPendingChanges,
            cancelAllPendingChanges: cancelAllPendingChanges,

            downloadCoreBackup: downloadCoreBackup,

            // redirector config
            getRedirectorConfig: getRedirectorConfig,
            saveRedirectorConfig: saveRedirectorConfig,

            // applications
            getAllApplicationNames: getAllApplicationNames,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists
        };

        indexedDBProvider.getDB()
            .then(function(database){
            }, function(error){
            }
        );

        return service;
//=========================================PUBLIC METHODS=============================================================//
        function getServicePaths(appName) {
            return indexedDBServicePaths.getServicePaths(appName);
        }

        function getAllFlavorRules(appName) {
            return indexedDBRules.getAllFlavorRules(appName);
        }

        function getAllUrlRules(appName) {
            return indexedDBUrlRules.getAllUrlRules(appName);
        }

        function getFlavorRuleById(appName, id) {
            return indexedDBRules.getFlavorRuleById(appName, id);
        }

        function getUrlRuleById(appName, id) {
            return indexedDBUrlRules.getUrlRuleById(appName, id);
        }

        function getFlavorRulesTemplates(appName) {
            return indexedDBRulesTemplates.getAllFlavorRuleTemplates(appName);
        }

        function getUrlRulesTemplates(appName) {
            return indexedDBUrlRulesTemplates.getAllUrlRuleTemplates(appName);
        }

        function getPendingChanges(appName) {
            return indexedDBChanges.getPendingChanges(appName);
        }

        function getAllFlavorRuleIds(appName) {
            return indexedDBCommon.getEntitiesIds(appName, PATH_RULES);
        }

        function getAllUrlRuleIds(appName) {
            return indexedDBCommon.getEntitiesIds(appName, URL_RULES);
        }

        function getFlavorRuleTemplatesIds(appName) {
            return indexedDBCommon.getEntitiesIds(appName, TEMPLATES_PATH);
        }

        function getUrlRuleTemplatesIds(appName) {
            return indexedDBCommon.getEntitiesIds(appName, TEMPLATES_URL);
        }

        function getIdsOfNewUnapprovedRules(appName, ruleType) {
            return indexedDBCommon.getIdsOfNewUnapprovedRules(appName, ruleType);
        }

        function getDefaultUrlParams(appName) {
            return indexedDBDefaultUrlParams.getDefaultUrlParams(appName);
        }

        function saveDefaultUrlParams(appName, urlParams) {
            return indexedDBDefaultUrlParams.saveDefaultUrlParams(appName, urlParams);
        }

        function saveFlavorRule(rule, appName, ruleId) {
            return indexedDBRules.saveFlavorRule(appName, rule, ruleId);
        }

        function saveTemplateFlavorRule(appName, rule, ruleId) {
            return indexedDBRulesTemplates.saveFlavorRuleTemplate(appName, rule, ruleId);
        }

        function saveTemplateUrlRule(appName, rule, ruleId) {
            return indexedDBUrlRulesTemplates.saveUrlRuleTemplate(appName, rule, ruleId);
        }

        function saveUrlRule(rule, appName, ruleId) {
            return indexedDBUrlRules.saveUrlRule(appName, rule, ruleId);
        }

        function deleteFlavorRule(appName, ruleId) {
            return indexedDBRules.deleteFlavorRule(appName, ruleId);
        }

        function deleteTemplateFlavorRule(appName, ruleId) {
            return indexedDBRulesTemplates.deleteFlavorRuleTemplate(appName, ruleId);
        }

        function deleteTemplateUrlRule(appName, ruleId) {
            return indexedDBUrlRulesTemplates.deleteUrlRuleTemplate(appName, ruleId);
        }

        function deleteUrlRule(appName, ruleId) {
            return indexedDBUrlRules.deleteUrlRule(appName, ruleId);
        }

        function approveFlavorRule(appName, ruleId) {
            return indexedDBRules.approveFlavorRule(appName, ruleId);
        }

        function approvePendingDefaultUrlParams(appName, version) {
            return indexedDBDefaultUrlParams.approveDefaultUrlParams(appName, 'default');
        }

        function approveUrlRule(appName, ruleId, version) {
            return indexedDBUrlRules.approveUrlRule(appName, ruleId);
        }

        function approveTemplateUrlRule(appName, ruleId, version) {
            return indexedDBUrlRulesTemplates.approveUrlRuleTemplate(appName, ruleId);
        }

        function approveTemplatePathRule(appName, ruleId, version) {
            return indexedDBRulesTemplates.approveFlavorRuleTemplate(appName, ruleId);
        }

        function cancelFlavorRule(appName, ruleId) {
            return indexedDBRules.cancelFlavorRule(appName, ruleId);
        }

        function cancelFlavorRuleTemplate(appName, ruleId, version) {
            return indexedDBRulesTemplates.cancelFlavorRuleTemplate(appName, ruleId);
        }

        function cancelUrlRuleTemplate(appName, ruleId, version) {
            return indexedDBUrlRulesTemplates.cancelUrlRuleTemplate(appName, ruleId);
        }

        function cancelUrlRule(appName, ruleId, version) {
            return indexedDBUrlRules.cancelUrlRule(appName, ruleId, version, URL_RULES);
        }

        function cancelPendingDefaultUrlParams(appName, version) {
            return indexedDBDefaultUrlParams.cancelDefaultUrlParams(appName, 'default');
        }

        function getDistribution(appName) {
            return indexedDBDistribution.getDistribution(appName);
        }

        function approveAllPendingDistribution(appName, version) {
            return indexedDBDistribution.approveDistribution(appName, DISTRIBUTIONS);
        }

        function getDefaultServer(appName) {
            return indexedDBDefaultServer.getDefaultServer(appName);
        }

        function sendDefaultServer(appName, defaultServer) {
            return indexedDBDefaultServer.saveDefaultServer(appName, defaultServer);
        }

        function approvePendingServer(appName, version) {
            return indexedDBDefaultServer.approveDefaultServer(appName);
        }

        function cancelPendingServer(appName, serverName, version) {
            return indexedDBDefaultServer.cancelDefaultServer(appName);
        }

        function getWhitelisted(appName) {
            return indexedDBWhitelisted.getWhitelisted(appName);
        }

        function getStackComment(appName, path) {
            var defer =  $q.defer();
            defer.resolve("Comments are not supported in offline mode");
            return defer.promise;
        }

        function saveStackComment(comment, appName, path) {
            var defer =  $q.defer();
            defer.resolve("Comments are not supported in offline mode");
            return defer.promise;
        }

        function approveAllPendingChanges(appName, version) {
            return indexedDBChanges.approveAll(appName);
        }

        function cancelAllPendingChanges(appName, version) {
            return indexedDBChanges.cancelAll(appName);
        }

        function sendWhitelisted(appName, whitelistToSave) {
            return indexedDBWhitelisted.saveWhitelisted(appName, whitelistToSave);
        }

        function approveWhitelisted(appName, version) {
            return indexedDBWhitelisted.approveWhitelisted(appName);
        }

        function cancelWhitelisted(appName, version) {
            return indexedDBWhitelisted.cancelWhitelisted(appName);
        }

        function getAllSnapshots() {
            return indexedDBCommon.getSnapshotListForAllApps();
        }

        function downloadCoreBackup(snapshots) {
            webServiceDataSource.downloadCoreBackup(snapshots)
        }

        function saveDistribution(appName, distributionToSave) {
            return indexedDBDistribution.saveDistribution(appName, distributionToSave);
        }

        function getDistributionPendingPreview(appName) {
            return indexedDBDistribution.getDistributionPendingPreview(appName);
        }

        function approveDistribution(appName) {
            return indexedDBDistribution.approveDistribution(appName);
        }

        function cancelAllPendingDistribution(appName, version) {
            return indexedDBDistribution.cancelAllPendingDistribution(appName);
        }

        function saveDistributionWithDefaultServer(appName, distributionWithDefaultServer) {
            return IndexedDBDistributionWithDefaultServer.saveDistribution(appName, distributionWithDefaultServer);
        }

        function getNamespaces() {
            return indexedDBNamespaces.getNamespaces();
        }

        function getNamespacedListByName(name) {
            return indexedDBNamespaces.getNamespacedListByName(name);
        }

        function saveNamespaces(name, namespace) {
            return indexedDBNamespaces.saveNamespaces(name, namespace);
        }

        function deleteNamespace(name) {
            return indexedDBNamespaces.deleteNamespace(name);
        }

        function deleteNamespaceValue(name, value) {
            return deleteNamespaceValues(name, value);
        }

        function deleteNamespaceValues(name, values) {
            return indexedDBNamespaces.deleteNamespaceValues(name, values);
        }

        function bulkDeleteNamespacedValues (name, entitiesFromFile) {
            return indexedDBNamespaces.bulkDeleteNamespacedValues(name, entitiesFromFile);
        }

        function getNamespaceDuplicates(newNamespace) {
            return indexedDBNamespaces.getNamespaceDuplicates(newNamespace);
        }

        function searchNamespacesByItem(item) {
            return indexedDBNamespaces.searchNamespacesByItem(item);
        }

        function getMultipleRulesDependingOnNamespaced(names) {
            return indexedDBNamespaces.getMultipleRulesDependingOnNamespaced(names);
        }

        function getRedirectorConfig () {
            return indexedDBSettings.getRedirectorConfig();
        }

        function saveRedirectorConfig (redirectorConfig) {
            return indexedDBSettings.saveRedirectorConfig(redirectorConfig);
        }

        function getAllApplicationNames () {
            var defer = $q.defer();
            try {
                indexedDBProvider.initDataSource().then (
                    function () {
                        defer.resolve({appNames: indexedDBProvider.getAppNames()});
                    }
                );
            } catch (e) {
                defer.reject(e);
            }
            return defer.promise;
        }

        function deleteEntitiesFromNamespacedLists (valuesByNames) {
            return indexedDBNamespaces.deleteEntitiesFromNamespacedLists(valuesByNames);
        }

    }
})();
