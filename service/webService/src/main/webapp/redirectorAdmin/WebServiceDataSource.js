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


(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('WebServiceDataSource', WebServiceDataSource);

    WebServiceDataSource.$inject = ['$http', '$location', '$q', 'utilsService', '$state', '$rootScope', 'tokenService'];

    function WebServiceDataSource($http, $location, $q, utils, $state, $rootScope, tokenService) {

        var service = {

            loadApplications: loadApplications,
            loadModelSnapshot: loadModelSnapshot,
            loadNamespacedListsSnapshot: loadNamespacedListsSnapshot,
            loadZookeeperSnapshot: loadZookeeperSnapshot,

            getServicePaths: getServicePaths,

            // flavor rules
            getAllFlavorRules: getAllFlavorRules,
            getFlavorRuleById: getFlavorRuleById,
            getAllFlavorRuleIds: getAllFlavorRuleIds,
            saveFlavorRule: saveFlavorRule,
            deleteFlavorRule: deleteFlavorRule,
            approveFlavorRule: approveFlavorRule,
            cancelFlavorRule: cancelFlavorRule,
            // flavor rules offline
            saveFlavorRuleOffline: saveFlavorRuleOffline,
            deleteFlavorRuleOffline: deleteFlavorRuleOffline,
            approveFlavorRuleOffline: approveFlavorRuleOffline,
            cancelFlavorRuleOffline: cancelFlavorRuleOffline,

            // url rules
            getAllUrlRules: getAllUrlRules,
            getUrlRuleById: getUrlRuleById,
            getAllUrlRuleIds: getAllUrlRuleIds,
            saveUrlRule: saveUrlRule,
            deleteUrlRule: deleteUrlRule,
            approveUrlRule: approveUrlRule,
            cancelUrlRule: cancelUrlRule,
            // url rules offline
            saveUrlRuleOffline: saveUrlRuleOffline,
            deleteUrlRuleOffline: deleteUrlRuleOffline,
            approveUrlRuleOffline: approveUrlRuleOffline,
            cancelUrlRuleOffline: cancelUrlRuleOffline,

            // flavor templates
            getFlavorRulesTemplates: getFlavorRulesTemplates,
            getFlavorRuleTemplatesIds: getFlavorRuleTemplatesIds,
            saveTemplateFlavorRule: saveTemplateFlavorRule,
            deleteTemplateFlavorRule: deleteTemplateFlavorRule,
            cancelFlavorRuleTemplate: cancelFlavorRuleTemplate,
            // flavor templates offline
            saveFlavorRuleTemplateOffline: saveFlavorRuleTemplateOffline,
            deleteFlavorRuleTemplateOffline: deleteFlavorRuleTemplateOffline,
            approveFlavorRuleTemplateOffline: approveFlavorRuleTemplateOffline,
            cancelFlavorRuleTemplateOffline: cancelFlavorRuleTemplateOffline,

            // url templates
            getUrlRulesTemplates: getUrlRulesTemplates,
            getUrlRuleTemplatesIds: getUrlRuleTemplatesIds,
            saveTemplateUrlRule: saveTemplateUrlRule,
            deleteTemplateUrlRule: deleteTemplateUrlRule,
            approveTemplateUrlRule: approveTemplateUrlRule,
            cancelUrlRuleTemplate: cancelUrlRuleTemplate,
            // url templates offline
            saveTemplateUrlRuleOffline: saveTemplateUrlRuleOffline,
            deleteTemplateUrlRuleOffline: deleteTemplateUrlRuleOffline,
            approveTemplateUrlRuleOffline: approveTemplateUrlRuleOffline,
            cancelUrlRuleTemplateOffline: cancelUrlRuleTemplateOffline,

            // default url params
            getDefaultUrlParams: getDefaultUrlParams,
            saveDefaultUrlParams: saveDefaultUrlParams,
            approvePendingDefaultUrlParams: approvePendingDefaultUrlParams,
            cancelPendingDefaultUrlParams: cancelPendingDefaultUrlParams,
            approveTemplatePathRule: approveTemplatePathRule,
            // default url params offline
            saveDefaultUrlParamsOffline: saveDefaultUrlParamsOffline,
            approvePendingDefaultUrlParamsOffline: approvePendingDefaultUrlParamsOffline,
            cancelPendingDefaultUrlParamsOffline: cancelPendingDefaultUrlParamsOffline,

            getPendingChanges: getPendingChanges,
            getIdsOfNewUnapprovedRules: getIdsOfNewUnapprovedRules,
            getIdsOfNewUnapprovedRulesOffline: getIdsOfNewUnapprovedRulesOffline,

            getNamespacesChanges: getNamespacesChanges,

            // distributions
            saveDistribution: saveDistribution,
            getDistribution: getDistribution,
            approveAllPendingDistribution: approveAllPendingDistribution,
            getDistributionPendingPreview: getDistributionPendingPreview,
            cancelAllPendingDistribution: cancelAllPendingDistribution,
            saveDistributionWithDefaultServer: saveDistributionWithDefaultServer,
            // distribution offline
            saveDistributionOffline: saveDistributionOffline,
            saveDistributionWithDefaultServerOffline: saveDistributionWithDefaultServerOffline,
            approveDistributionOffline: approveDistributionOffline,
            cancelDistributionOffline: cancelDistributionOffline,
            getDistributionPendingPreviewOffline: getDistributionPendingPreviewOffline,

            // default sever
            getDefaultServer: getDefaultServer,
            sendDefaultServer: sendDefaultServer,
            approvePendingServer: approvePendingServer,
            cancelPendingServer: cancelPendingServer,
            // default sever offline
            sendDefaultServerOffline: sendDefaultServerOffline,
            approvePendingDefaultServerOffline: approvePendingDefaultServerOffline,
            cancelPendingDefaultServerOffline: cancelPendingDefaultServerOffline,

            // whitelisted
            getWhitelisted: getWhitelisted,
            //todo: naming is unclear (stack vs whitelisted)
            getStackComment: getStackComment,
            saveStackComment: saveStackComment,
            sendWhitelisted: sendWhitelisted,
            approveWhitelisted: approveWhitelisted,
            validateAndApproveWhitelisted: validateAndApproveWhitelisted,
            cancelWhitelisted: cancelWhitelisted,
            //whitelisted offline
            saveWhitelistedOffline: saveWhitelistedOffline,
            approveWhitelistedOffline: approveWhitelistedOffline,
            cancelWhitelistedOffline: cancelWhitelistedOffline,

            // namespaces
            getNamespaces: getNamespaces,
            getAllNamespacedListsWithoutValues: getAllNamespacedListsWithoutValues,
            getNamespacedListByName: getNamespacedListByName,
            saveNamespaces: saveNamespaces,
            saveNamespacesAutoResolve: saveNamespacesAutoResolve,
            deleteNamespace: deleteNamespace,
            deleteNamespaceValue: deleteNamespaceValue,
            deleteNamespaceValues: deleteNamespaceValues,
            deleteEntitiesFromNamespacedLists: deleteEntitiesFromNamespacedLists,
            bulkDeleteNamespacedValues: bulkDeleteNamespacedValues,
            getNamespaceDuplicates: getNamespaceDuplicates,
            searchNamespacesByItem: searchNamespacesByItem,
            getMultipleRulesDependingOnNamespaced: getMultipleRulesDependingOnNamespaced,
            // namespaces offline
            searchNamespacesByItemOffline: searchNamespacesByItemOffline,
            getNamespaceDuplicatesOffline: getNamespaceDuplicatesOffline,
            deleteNamespaceOffline: deleteNamespaceOffline,
            deleteNamespaceValueOffline: deleteNamespaceValueOffline,
            deleteNamespaceValuesOffline: deleteNamespaceValuesOffline,
            bulkDeleteNamespacedValuesOffline: bulkDeleteNamespacedValuesOffline,
            validateNamespace: validateNamespace,
            getMultipleRulesDependingOnNamespacedOffline: getMultipleRulesDependingOnNamespacedOffline,

            // upload XRE Backup
            downloadCoreBackup: downloadCoreBackup,

            // pending changes
            approveAllPendingChanges: approveAllPendingChanges,
            validateAndApproveAllPendingChanges: validateAndApproveAllPendingChanges,
            cancelAllPendingChanges: cancelAllPendingChanges,
            // pending changes offline
            approveAllPendingChangesOffline: approveAllPendingChangesOffline,
            cancelAllPendingChangesOffline: cancelAllPendingChangesOffline,

            // redirector config
            getRedirectorConfig: getRedirectorConfig,
            saveRedirectorConfig: saveRedirectorConfig,


            // XRE Backup Changes
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

            //applications
            getAllApplicationNames: getAllApplicationNames,
            deleteEntitiesFromNamespacedListsOffline: deleteEntitiesFromNamespacedListsOffline,
            
            getAllExistingApplications: getAllExistingApplications,
            createDefaultModel: createDefaultModel,
            atLeastOneValidModelExists: atLeastOneValidModelExists,
            defaultModelConstructionDetails: defaultModelConstructionDetails,
            validateApplication: validateApplication
        };

        return service;
//=======================================PUBLIC METHODS===============================================================//

        function loadApplications() {
            return getData(getBaseApiUrl() + 'redirectorOffline/applications');
        }

        function loadModelSnapshot(application) {
            return getData(getBaseApiUrl() + 'redirectorOffline/' + application + '/modelSnapshot');
        }

        function loadNamespacedListsSnapshot() {
            return getData(getBaseApiUrl() + 'redirectorOffline/namespaceSnapshot');
        }

        function loadZookeeperSnapshot() {
            return getData(getBaseApiUrl() + 'redirectorOffline/allSnapshots');
        }

        function downloadCoreBackup(snapshots) {
            openInNewWindow ('POST', getBaseApiUrl() + 'redirectorOffline/downloadCoreBackup', angular.toJson(snapshots), 'newWin');
        }

        function openInNewWindow (verb, url, data, target) {
            var form = window.document.createElement("form");
            form.action = url;
            form.method = verb;
            form.enctype = 'application/x-www-form-urlencoded';
            form.target = target || "_self";

            var input = document.createElement("textarea");
            input.name = 'snapshots';
            input.value = data;

            form.appendChild(input);
            form.style.display = 'none';
            document.body.appendChild(form);
            form.submit();
        }

        function getServicePaths(appName) {
            return getData(getBaseApiUrl() + 'stacks/' + appName);
        }

        function getAllFlavorRules(appName) {
            return getData(getBaseApiUrl() + 'rules/' + appName)
        }

        function getAllUrlRules(appName) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName);
        }

        function getFlavorRuleById(appName, ruleId) {
            return getData(getBaseApiUrl() + 'rules/' + appName + '/' + ruleId);
        }

        function getUrlRuleById(appName, ruleId) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName + '/' + ruleId);
        }

        function getFlavorRulesTemplates(appName) {
            return getData(getBaseApiUrl() + 'templates/rules/' + appName);
        }

        function getFlavorRuleTemplatesIds(appName) {
            return getData(getBaseApiUrl() + 'templates/rules/' + appName + '/ids');
        }

        function saveTemplateFlavorRule(appName, rule, ruleId) {
            return saveData(getBaseApiUrl() + 'templates/rules/' + appName + '/' + ruleId + '/', rule);
        }

        function saveTemplateUrlRule(appName, rule, ruleId) {
            return saveData(getBaseApiUrl() + 'templates/urlRules/' + appName + '/' + ruleId + '/', rule);
        }

        function getUrlRulesTemplates(appName) {
            return getData(getBaseApiUrl() + 'templates/urlRules/' + appName);
        }

        function getUrlRuleTemplatesIds(appName) {
            return getData(getBaseApiUrl() + 'templates/urlRules/' + appName + '/ids');
        }

        function getPendingChanges(appName) {
            return getData(getBaseApiUrl() + 'changes/' + appName);
        }

        function getAllFlavorRuleIds(appName) {
            return getData(getBaseApiUrl() + 'rules/' + appName + '/ids');
        }

        function getIdsOfNewUnapprovedRules(appName, ruleType) {
            return getData(getBaseApiUrl() + 'changes/' + appName + '/newRuleIds/' + ruleType);
        }

        function getIdsOfNewUnapprovedRulesOffline(ruleType, snapshot) {
                    return saveData(getBaseApiUrl() + 'changesOffline/newRuleIds/' + ruleType, snapshot);
        }

        function getNamespacesChanges() {
            return getData(getBaseApiUrl() + 'changesNamespaces/');
        }

        function getDefaultUrlParams(appName) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName + '/defaultUrlParams');
        }

        function saveDefaultUrlParams(appName, urlParams) {
            return saveData(getBaseApiUrl() + 'urlRules/' + appName + '/urlParams/default/', urlParams);
        }

        function saveDefaultUrlParamsOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'urlRulesOffline/' + appName + '/urlParams/default/', snapshot);
        }

        function approvePendingDefaultUrlParamsOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/urlParams/default/approve', snapshot);
        }

        function cancelPendingDefaultUrlParamsOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/urlParams/default/cancel', snapshot);
        }

        function approvePendingDefaultUrlParams(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/urlParams/default' + '/' + version);
        }

        function cancelPendingDefaultUrlParams(appName, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/urlParams/default' + '/' + version);
        }

        function saveFlavorRule(rule, appName, ruleName) {
            return saveData(getBaseApiUrl() + 'rules/' + appName + '/' + ruleName + '/', rule);
        }

        function saveFlavorRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'rulesOffline/' + appName + '/' + ruleName + '/add', snapshot);
        }

        function deleteFlavorRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'rulesOffline/' + appName + '/' + ruleName + '/delete', snapshot);
        }

        function approveFlavorRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/flavorrule/' + ruleName + '/approve', snapshot);
        }

        function cancelFlavorRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/flavorrule/' + ruleName + '/cancel', snapshot);
        }

        function saveUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'urlRulesOffline/' + appName + '/' + ruleName + '/add', snapshot);
        }

        function deleteUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'urlRulesOffline/' + appName + '/' + ruleName + '/delete', snapshot);
        }

        function approveUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/urlrule/' + ruleName + '/approve', snapshot);
        }

        function cancelUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/urlrule/' + ruleName + '/cancel', snapshot);
        }

        function saveFlavorRuleTemplateOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'templates/rulesOffline/' + appName + '/' + ruleName + '/add', snapshot);
        }

        function deleteFlavorRuleTemplateOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'templates/rulesOffline/' + appName + '/' + ruleName + '/delete', snapshot);
        }

        function approveFlavorRuleTemplateOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/templateRule/' + ruleName + '/approve', snapshot);
        }

        function cancelFlavorRuleTemplateOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/templateRule/' + ruleName + '/cancel', snapshot);
        }

        function saveTemplateUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'templates/urlRulesOffline/' + appName + '/' + ruleName + '/add', snapshot);
        }

        function deleteTemplateUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'templates/urlRulesOffline/' + appName + '/' + ruleName + '/delete', snapshot);
        }

        function approveTemplateUrlRuleOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/templateUrlRule/' + ruleName + '/approve', snapshot);
        }

        function cancelUrlRuleTemplateOffline(appName, ruleName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/templateUrlRule/' + ruleName + '/cancel', snapshot);
        }

        function saveUrlRule(rule, appName, ruleName) {
            return saveData(getBaseApiUrl() + 'urlRules/' + appName + '/' + ruleName + '/', rule);
        }

        function saveDistribution(appName, distribution) {
            return saveData(getBaseApiUrl() + 'distributions/' + appName, distribution);
        }

        function getDistribution(appName) {
            return getData(getBaseApiUrl() + 'distributions/' + appName);
        }

        function approveAllPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/approveWithoutValidation/' + version);
        }

        function validateAndApproveAllPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/approve/' + version);
        }

        function cancelAllPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/cancel/' + version);
        }

        function approveAllPendingChangesOffline(appName, data) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/approve/', data);
        }

        function cancelAllPendingChangesOffline(appName, data) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/cancel/', data);
        }

        function approveAllPendingDistribution(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/distribution/' + version);
        }

        function getDistributionPendingPreview(appName) {
            return getData(getBaseApiUrl() + 'changes/' + appName + '/preview/distribution/');
        }

        function getDistributionPendingPreviewOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/preview/distribution/', snapshot);
        }

        function deleteFlavorRule(appName, ruleId) {
            return deleteItem(getBaseApiUrl() + 'rules/' + appName + '/' + ruleId);
        }

        function deleteTemplateFlavorRule(appName, ruleId) {
            return deleteItem(getBaseApiUrl() + 'templates/rules/' + appName + '/' + ruleId);
        }

        function cancelFlavorRuleTemplate(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/templateRule/' + ruleId + '/' + version);
        }

        function cancelUrlRuleTemplate(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/templateUrlRule/' + ruleId + '/' + version);
        }

        function deleteTemplateUrlRule(appName, ruleId) {
            return deleteItem(getBaseApiUrl() + 'templates/urlRules/' + appName + '/' + ruleId);
        }

        function approveTemplatePathRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/templateRule/' + ruleId + '/' + version);
        }

        function approveTemplateUrlRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/templateUrlRule/' + ruleId + '/' + version);
        }

        function deleteUrlRule(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'urlRules/' + appName + '/' + ruleId);
        }

        function approveFlavorRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/rule/' + ruleId + '/' + version);
        }

        function approveUrlRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/urlRule/' + ruleId + '/' + version);
        }

        function cancelFlavorRule(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/rule/' + ruleId + '/' + version);
        }

        function cancelUrlRule(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/urlRule/' + ruleId + '/' + version);
        }

        function cancelAllPendingDistribution(appName, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/distribution/' + version);
        }

        function saveDistributionWithDefaultServer(appName, distributionWithDefault) {
             return saveData(getBaseApiUrl() + 'export/distribuionswithdefault/save/' + appName, distributionWithDefault);
        }

        function saveDistributionOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'distributionsOffline/' + appName, snapshot);
        }

        function saveDistributionWithDefaultServerOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'exportOffline/distribuionswithdefault/save/' + appName, snapshot);
        }

        function approveDistributionOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/distribution/approve', snapshot);
        }

        function cancelDistributionOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/distribution/cancel', snapshot);
        }

        function getAllUrlRuleIds(appName) {
            return getData(getBaseApiUrl() + 'urlRules/' + appName + '/ids');
        }


        function getServer(serverType, appName) {
            return getData(getServerUrl(serverType, appName));
        }

        function sendServer(server, serverType, appName) {
            return saveData(getServerUrl(serverType, appName), server);
        }

        function getDefaultServer(appName) {
            return getServer('default', appName);
        }

        function sendDefaultServer(appName, defaultServer) {
            return sendServer(defaultServer, 'default', appName);
        }

        function sendDefaultServerOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'serversOffline/' + appName + '/default/', snapshot);
        }

        function approvePendingDefaultServerOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/server/default/approve', snapshot);
        }

        function cancelPendingDefaultServerOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/server/default/cancel', snapshot);
        }

        function sendDefaultUrlParamsOffline(appName, defaultServer) {
            return saveData(defaultServer, 'default', appName);
        }

        function approvePendingServer(appName, serverName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/server/' + serverName + '/' + version, '', {})
        }

        function cancelPendingServer(appName, serverName, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/server/' + serverName + '/' + version);
        }

        function getWhitelisted(appName) {
            return getData(getBaseApiUrl() + 'whitelistWithTimestamps/' + appName);
        }

        function getStackComment(appName, path) {
            return getData(getBaseApiUrl() + 'stackComments/getOne/' + appName + "?path=" + path);
        }

         function saveStackComment(comment, appName, path) {
            return saveData(getBaseApiUrl() + 'stackComments/post/' + appName + "?path=" + path, comment);
        }

        function sendWhitelisted(appName, whitelist) {
            return saveData(getBaseApiUrl() + 'whitelist/' + appName, whitelist);
        }

        function approveWhitelisted(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/stackmanagement/' + version);
        }

        function validateAndApproveWhitelisted(appName, version) {
            return saveData(getBaseApiUrl() + 'changes/' + appName + '/stackmanagement/validate/' + version);
        }

        function cancelWhitelisted(appName, version) {
            return deleteItem(getBaseApiUrl() + 'changes/' + appName + '/stackmanagement/' + version);
        }

        function saveWhitelistedOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'whitelistOffline/' + appName, snapshot);
        }

        function approveWhitelistedOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/stackmanagement/approve', snapshot);
        }

        function cancelWhitelistedOffline(appName, snapshot) {
            return saveData(getBaseApiUrl() + 'changesOffline/' + appName + '/stackmanagement/cancel', snapshot);
        }

        function getNamespaces() {
            return getData(getBaseApiUrl() + 'namespacedLists/getAllNamespacedLists');
        }

        function getAllNamespacedListsWithoutValues () {
            return getData(getBaseApiUrl() + 'namespacedLists/getAllNamespacedListsWithoutValues');
        }

        function saveNamespaces(name, namespace) {
            return saveData(getBaseApiUrl() + 'namespacedLists/addNewNamespaced/' + name, namespace);
        }

        function saveNamespacesAutoResolve(name, namespace) {
            return saveData(getBaseApiUrl() + 'namespacedLists/addNewNamespaced/' + name + "?autoResolve=true", namespace);
        }

        function deleteNamespace(name) {
            return deleteItem(getBaseApiUrl() + 'namespacedLists/' + name);
        }

        function getNamespacedListByName(name) {
            return getData(getBaseApiUrl() + 'namespacedLists/getOne/' + name);
        }

        function deleteNamespaceValue(name, value) {
            return deleteItem(getBaseApiUrl() + 'namespacedLists/' + name + '/' + encodeURIComponent(value));
        }

        function bulkDeleteNamespacedValues (name, entitiesFromFile) {
            return saveData(getBaseApiUrl() + 'namespacedLists/deleteNamespacedEntities/' + name, {entities: entitiesFromFile});
        }

        function deleteNamespaceOffline(name, snapshotList) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/' + name, snapshotList);
        }

        function deleteNamespaceValueOffline(name, value, namespace) {
            return deleteItem(getBaseApiUrl() + 'namespacedListsOffline/' + name + '/' + encodeURIComponent(value), namespace);
        }

        function bulkDeleteNamespacedValuesOffline (name, snapshotList) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/deleteNamespacedEntities/' + name, snapshotList);
        }

        function deleteNamespaceValues(name, values) {
            if (angular.isArray(values)) {
                var url = getBaseApiUrl() + 'namespacedLists/' + name + '/';
                for (var i = 0; i < values.length; i++) {
                    url += encodeURIComponent(values[i]);
                    url += (i === values.length - 1) ? '' : ',';
                }
                return deleteItem(url);
            } else {
                return deleteNamespaceValue(name, values);
            }
        }

        function deleteEntitiesFromNamespacedLists (valuesByNames) {
            var url = getBaseApiUrl() + 'namespacedLists/deleteEntitiesFromNamespacedLists';
            return saveData(url, valuesByNames);
        }

        function deleteNamespaceValuesOffline(name, values, namespace) {
            if (angular.isArray(values)) {
                var url = getBaseApiUrl() + 'namespacedListsOffline/' + name + '/';
                for (var i = 0; i < values.length; i++) {
                    url += encodeURIComponent(values[i]);
                    url += (i === values.length - 1) ? '' : ',';
                }
                return deleteItem(url, namespace);
            } else {
                return deleteNamespaceValueOffline(name, values);
            }
        }

        function getNamespaceDuplicates(namespace) {
            return saveData(getBaseApiUrl() + 'namespacedLists/duplicates', namespace);
        }

        function validateNamespace(namespace) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/validate', namespace);
        }

        function getNamespaceDuplicatesOffline(namespace) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/duplicates', namespace);
        }

        function searchNamespacesByItem(item) {
            return getData(getBaseApiUrl() + 'namespacedLists/search/' + item);
        }

        function getMultipleRulesDependingOnNamespaced(namespaced) {
            var url = getBaseApiUrl() + 'namespacedLists/dependingRulesMultiple/';
            for (var i = 0; i < namespaced.length; i++) {
                url += ((i === namespaced.length - 1) ? namespaced[i] : namespaced[i] + ',');
            }
            return getData(url);
        }

        function getMultipleRulesDependingOnNamespacedOffline(namespacedNames, snapshotsList) {
            var url = getBaseApiUrl() + 'namespacedListsOffline/dependingRulesMultiple/';
            for (var i = 0; i < namespacedNames.length; i++) {
                url += ((i === namespacedNames.length - 1) ? namespacedNames[i] : namespacedNames[i] + ',');
            }
            return saveData(url, snapshotsList);
        }
        
        function searchNamespacesByItemOffline(item, snapshotsList) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/search/' + item, snapshotsList);
        }

        function getRedirectorConfig () {
            return getData(getBaseApiUrl() + 'settings/redirectorConfig');
        }

        function saveRedirectorConfig (redirectorConfig) {
            return saveData(getBaseApiUrl() + 'settings/redirectorConfig', redirectorConfig);
        }

        function getCoreBackupPendingChangesJson(appName) {
            return getData(getBaseApiUrl() + 'coreBackupChanges/' + appName);
        }

        function approveCoreBackupFlavorRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/rule/' + ruleId + '/' + version);
        }

        function cancelCoreBackupFlavorRule(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/rule/' + ruleId + '/' + version);
        }

        function approveCoreBackupUrlRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/urlRule/' + ruleId + '/' + version);
        }

        function cancelCoreBackupUrlRule(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/urlRule/' + ruleId + '/' + version);
        }

        function approveCoreBackupFlavorRuleTemplate(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/templateRule/' + ruleId + '/' + version);
        }

        function cancelCoreBackupFlavorRuleTemplate(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/templateRule/' + ruleId + '/' + version);
        }

        function approveCoreBackupTemplateUrlRule(appName, ruleId, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/templateUrlRule/' + ruleId + '/' + version);
        }

        function cancelCoreBackupUrlRuleTemplate(appName, ruleId, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/templateUrlRule/' + ruleId + '/' + version);
        }

        function approveCoreBackupPendingDefaultUrlParams(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/urlParams/default' + '/' + version);
        }

        function cancelCoreBackupPendingDefaultUrlParams(appName, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/urlParams/default' + '/' + version);
        }

        function approveAllCoreBackupPendingDistribution(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/distribution/' + version);
        }

        function cancelAllCoreBackupPendingDistribution(appName, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/distribution/' + version);
        }

        function getCoreBackupDistributionPendingPreview(appName, version) {
            return getData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/preview/distribution/');
        }

        function approveCoreBackupPendingServer(appName, serverName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/server/' + serverName + '/' + version, '', {})
        }

        function cancelCoreBackupPendingServer(appName, serverName, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/server/' + serverName + '/' + version);
        }

        function approveCoreBackupWhitelisted(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/stackmanagement/' + version);
        }

        function validateAndApproveCoreBackupWhitelisted(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/stackmanagement/validate/' + version);
        }

        function cancelCoreBackupWhitelisted(appName, version) {
            return deleteItem(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/stackmanagement/' + version);
        }

        function approveAllCoreBackupPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/approveWithoutValidation/' + version);
        }

        function validateAndApproveAllCoreBackupPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/approve/' + version);
        }

        function cancelAllCoreBackupPendingChanges(appName, version) {
            return saveData(getBaseApiUrl() + 'coreBackupChanges/' + appName + '/cancel/' + version);
        }

        function  getAllApplicationNames () {
            return getData(getBaseApiUrl() + 'redirectorService/applicationNames');
        }

        function deleteEntitiesFromNamespacedListsOffline (valuesByNames) {
            return saveData(getBaseApiUrl() + 'namespacedListsOffline/deleteEntitiesFromNamespacedLists', valuesByNames);
        }

        function getAllExistingApplications() {
            return getData(getBaseApiUrl() + "redirectorInitModel")
        }

        function createDefaultModel(appName) {
            return saveData(getBaseApiUrl() + "redirectorInitModel/" + appName )
        }

        function atLeastOneValidModelExists() {
            return getData(getBaseApiUrl() + "redirectorInitModel/validModelExists/")
        }

        function defaultModelConstructionDetails (appName) {
            return getData(getBaseApiUrl() + "redirectorInitModel/defaultModelConstructionDetails/" + appName);
        }

        function validateApplication (appName) {
            return getData(getBaseApiUrl() + "redirectorInitModel/" + appName + "/validate");
        }
        
//=======================================PRIVATE METHODS==============================================================//
        function getData(url) {
            return getData(url, {});
        }

        function getData(url, headers) {
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
                data = angular.isDefined(data.message) ? data.message : data;
                if (status !== 400 && data.indexOf('UnmarshalException') > 0) {
                    defer.resolve('');
                }
            });

            return defer.promise;
        }

        function saveData(url) {
            return saveData(url, '', {});
        }

        function saveData(url, data) {
            return saveData(url, data, {});
        }

        function saveData(url, data, headers) {
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
        }

        function updateData(url) {
            return updateData(url, '', {})
        }

        function updateData(url, data) {
            return updateData(url, data, {})
        }

        function updateData(url, data, headers) {
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
                    if (status !== 401) {
                        defer.reject({data: data, status: status});
                    }
                });
            return defer.promise;
        }

        function deleteItem(url) {
            return deleteItem(url, {}, '');
        }

        function deleteItem(url, aData) {
            return deleteItem(url, {}, aData);
        }

        function deleteItem(url, headers, aData) {
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
        }

        function getServerUrl(serverType, appName) {
            return getBaseApiUrl() + 'servers/' + appName + '/' + serverType;
        }

        function getBaseUrl() {
            var baseUrl = $location.absUrl();
            if (baseUrl.lastIndexOf('#/') !== -1) {
                baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('#/'));
            }
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));
            baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf('/'));

            return baseUrl;
        }

        function getBaseApiUrl() {
            return getBaseUrl() + '/data/';
        }

    }
})();
