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
        .module('uxData.changes')
        .factory('changesRequestsService', changesRequestsService);

    changesRequestsService.$inject = ['$rootScope', '$q', 'requestsService', 'utilsService'];

    function changesRequestsService($rootScope, $q, requestsService, utils) {


        var service = {
            getPendingChanges: requestsService.getPendingChanges,
            getPendingChangesJson: getPendingChangesJson,
            getPendingChangesJsonByEntityType: getPendingChangesJsonByEntityType,
            getUnapprovedRulesIds: getUnapprovedRulesIds,
            cancelPendingRule: cancelPendingRule,
            cancelTemplatePendingRule: cancelTemplatePendingRule,
            cancelTemplatePendingUrlRule: cancelTemplatePendingUrlRule,
            cancelPendingServer: cancelPendingServer,
            approvePendingRule: approvePendingRule,
            approveTemplatePendingRule: approveTemplatePendingRule,
            approveTemplatePendingUrlRule: approveTemplatePendingUrlRule,
            approvePendingServer: approvePendingServer,
            approveAllPendingDistribution: approveAllPendingDistribution,
            approvePendingChanges: approvePendingChanges,
            validateAndApprovePendingChanges: validateAndApprovePendingChanges,
            cancelAllPendingChanges: cancelAllPendingChanges,
            cancelAllPendingDistribution: cancelAllPendingDistribution,
            getDistributionPendingPreview: getDistributionPendingPreview,
            cancelPendingStackManagement: cancelPendingStackManagement,
            approvePendingStackManagement: approvePendingStackManagement,
            validateAndApprovePendingStackManagement: validateAndApprovePendingStackManagement,
            approvePendingUrlRule: approvePendingUrlRule,
            cancelPendingUrlRule: cancelPendingUrlRule,
            approvePendingUrlParams: approvePendingUrlParams,
            cancelPendingUrlParams: cancelPendingUrlParams,
            exportAllPending: exportAllPending,
            downloadCoreBackup: downloadCoreBackup,
            exportPending: exportPending,
            exportPendingDistributions: exportPendingDistributions,
            exportPendingStackManagement: exportPendingStackManagement,
            triggerModelReload: triggerModelReload,
            triggerStacksReload: triggerStacksReload
        };

        return service;

        function getPendingChangesJson() {
            return requestsService.getPendingChangesJson($rootScope.currentApplication);
        }


        function triggerModelReload() {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'modelReload/' + $rootScope.currentApplication, '');
        }

        function triggerStacksReload() {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'stacksReload/' + $rootScope.currentApplication, '');
        }

        //====================== PENDING CHANGES ============================//

        function getPendingChangesJsonByEntityType (changesType, changeId) {
            var defer = $q.defer();
            requestsService.getPendingChangesJson($rootScope.currentApplication)
                .then(function(data) {
                    var response = {};
                    response.result = {};
                    response.result[changeId] = utils.getPendingChangesObjectById(data, changesType, changeId);
                    defer.resolve(response);
                }, function(reason) {
                    defer.reject({message: reason});
                }
            );
            return defer.promise;
        }

        function cancelPendingRule(ruleId, version) {
            return requestsService.cancelFlavorRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelTemplatePendingRule(ruleId, version) {
            return requestsService.cancelFlavorRuleTemplate($rootScope.currentApplication, ruleId, version);
        }

        function approvePendingRule(ruleId, version) {
            return requestsService.approveFlavorRule($rootScope.currentApplication, ruleId, version);
        }

        function approveTemplatePendingRule(ruleId, version) {
            return requestsService.approveTemplatePathRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelPendingServer(serverName, version) {
            return requestsService.cancelPendingServer($rootScope.currentApplication, serverName, version);
        }

        function approvePendingServer(serverName, version) {
            return requestsService.approvePendingServer($rootScope.currentApplication, serverName, version);
        }

        function cancelPendingStackManagement(version) {
            return requestsService.cancelWhitelisted($rootScope.currentApplication, version);
        }

        function approvePendingStackManagement(version) {
            return requestsService.approveWhitelisted($rootScope.currentApplication, version);
        }

        function validateAndApprovePendingStackManagement(version) {
            return requestsService.validateAndApproveWhitelisted($rootScope.currentApplication, version);
        }

        function approvePendingUrlRule(ruleId, version) {
            return requestsService.approveUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function approveTemplatePendingUrlRule(ruleId, version) {
            return requestsService.approveTemplateUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelPendingUrlRule(ruleId, version) {
            return requestsService.cancelUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelTemplatePendingUrlRule(ruleId, version) {
            return requestsService.cancelUrlRuleTemplate($rootScope.currentApplication, ruleId, version);
        }

        function approvePendingUrlParams(version) {
            return requestsService.approvePendingDefaultUrlParams($rootScope.currentApplication, version);
        }

        function cancelPendingUrlParams(version) {
            return requestsService.cancelPendingDefaultUrlParams($rootScope.currentApplication, version);
        }


        function approvePendingChanges(version) {
            return requestsService.approveAllPendingChanges($rootScope.currentApplication, version)
        }

        function validateAndApprovePendingChanges(version) {
            return requestsService.validateAndApproveAllPendingChanges($rootScope.currentApplication, version)
        }

        function cancelAllPendingChanges(version) {
            return requestsService.cancelAllPendingChanges($rootScope.currentApplication, version)
        }

        function cancelAllPendingDistribution(version) {
            return requestsService.cancelAllPendingDistribution($rootScope.currentApplication, version);
        }

        function approveAllPendingDistribution(version) {
            return requestsService.approveAllPendingDistribution($rootScope.currentApplication, version);
        }

        function getDistributionPendingPreview() {
            return requestsService.getDistributionPendingPreview($rootScope.currentApplication);
        }

        function getUnapprovedRulesIds(ruleType) {
            return requestsService.getIdsOfNewUnapprovedRules($rootScope.currentApplication, ruleType);
        }

        function downloadCoreBackup() {
            requestsService.getAllSnapshots()
                .then(function(snapshots){
                    requestsService.downloadCoreBackup(snapshots);
                }, function(error){
                }
            );
        }

        function exportAllPending() {
            window.open(requestsService.getBaseApiUrl() + 'changes/' + $rootScope.currentApplication + '/export', {'Accept': 'application/xml'});
        }

        function exportPending(ruleType, changeId) {
            window.open(requestsService.getBaseApiUrl() + 'changes/' + $rootScope.currentApplication + '/export/' + ruleType + '/' + changeId, {'Accept': 'application/xml'});
        }

        function exportPendingDistributions() {
            window.open(requestsService.getBaseApiUrl() + 'changes/' + $rootScope.currentApplication + '/export/distributions/', {'Accept': 'application/xml'});
        }

        function exportPendingStackManagement() {
            window.open(requestsService.getBaseApiUrl() + 'changes/' + $rootScope.currentApplication + '/export/stackmanagement/', {'Accept': 'application/xml'});
        }

        function deletePendingChange(cUrl) {
            return requestsService.deleteItem(cUrl, {});
        }

        function approvePendingChange(cUrl) {
            return requestsService.saveData(cUrl, '', {});
        }
    }
})();
