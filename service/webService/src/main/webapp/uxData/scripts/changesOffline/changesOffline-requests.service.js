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
    angular
        .module('uxData.changesOffline')
        .factory('changesOfflineRequestsService', changesOfflineRequestsService);

    changesOfflineRequestsService.$inject = ['$rootScope', '$q', 'requestsService', 'utilsService'];

    function changesOfflineRequestsService($rootScope, $q, requestsService, utils) {


        var service = {
            getCoreBackupUploadUrl: getCoreBackupUploadUrl,
            getPendingChanges: requestsService.getCoreBackupPendingChangesJson,
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
            approveNamespacesChanges: approveNamespacesChanges,
            approveAllNamespacesChanges: approveAllNamespacesChanges,
            cancelAllNamespacesChanges: cancelAllNamespacesChanges,
            cancelNamespacesChanges: cancelNamespacesChanges,
            hasPendingChanges: hasPendingChanges,
            hasChanges: hasChanges
        };

        return service;

        function hasChanges(pendingData) {
           return requestsService.saveData(requestsService.getBaseApiUrl() + 'redirectorOffline/' + $rootScope.currentApplication + '/hasOfflineChanges', pendingData, {'Content-Type': 'application/json', 'Accept': 'text/plain'});
         }

        function hasPendingChanges() {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'redirectorOffline/' + $rootScope.currentApplication + '/hasOfflinePendingChanges', {'Content-Type': 'application/json', 'Accept': 'text/plain'});
        }

        function getCoreBackupUploadUrl() {
            return requestsService.getBaseApiUrl() + 'redirectorOffline/' + $rootScope.currentApplication + '/getOfflinePendingChanges';
        }

        function getPendingChangesJson() {
            return requestsService.getCoreBackupPendingChangesJson($rootScope.currentApplication);
        }


        function triggerModelReload() {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'modelReload/' + $rootScope.currentApplication, '');
        }

        //====================== PENDING CHANGES ============================//

        function getPendingChangesJsonByEntityType (changesType, changeId) {
            var defer = $q.defer();
            requestsService.getCoreBackupPendingChangesJson($rootScope.currentApplication)
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
            return requestsService.cancelCoreBackupFlavorRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelTemplatePendingRule(ruleId, version) {
            return requestsService.cancelCoreBackupFlavorRuleTemplate($rootScope.currentApplication, ruleId, version);
        }

        function approvePendingRule(ruleId, version) {
            return requestsService.approveCoreBackupFlavorRule($rootScope.currentApplication, ruleId, version);
        }

        function approveTemplatePendingRule(ruleId, version) {
            return requestsService.approveCoreBackupFlavorRuleTemplate($rootScope.currentApplication, ruleId, version);
        }

        function cancelPendingServer(serverName, version) {
            return requestsService.cancelCoreBackupPendingServer($rootScope.currentApplication, serverName, version);
        }

        function approvePendingServer(serverName, version) {
            return requestsService.approveCoreBackupPendingServer($rootScope.currentApplication, serverName, version);
        }

        function cancelPendingStackManagement(version) {
            return requestsService.cancelCoreBackupWhitelisted($rootScope.currentApplication, version);
        }

        function approvePendingStackManagement(version) {
            return requestsService.approveCoreBackupWhitelisted($rootScope.currentApplication, version);
        }

        function validateAndApprovePendingStackManagement(version) {
            return requestsService.validateAndApproveCoreBackupWhitelisted($rootScope.currentApplication, version);
        }

        function approvePendingUrlRule(ruleId, version) {
            return requestsService.approveCoreBackupUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function approveTemplatePendingUrlRule(ruleId, version) {
            return requestsService.approveCoreBackupTemplateUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelPendingUrlRule(ruleId, version) {
            return requestsService.cancelCoreBackupUrlRule($rootScope.currentApplication, ruleId, version);
        }

        function cancelTemplatePendingUrlRule(ruleId, version) {
            return requestsService.cancelCoreBackupUrlRuleTemplate($rootScope.currentApplication, ruleId, version);
        }

        function approvePendingUrlParams(version) {
            return requestsService.approveCoreBackupPendingDefaultUrlParams($rootScope.currentApplication, version);
        }

        function cancelPendingUrlParams(version) {
            return requestsService.cancelCoreBackupPendingDefaultUrlParams($rootScope.currentApplication, version);
        }

        function approvePendingChanges(version) {
            return requestsService.approveAllCoreBackupPendingChanges($rootScope.currentApplication, version)
        }

        function validateAndApprovePendingChanges(version) {
            return requestsService.validateAndApproveAllCoreBackupPendingChanges($rootScope.currentApplication, version)
        }

        function cancelAllPendingChanges(version) {
            return requestsService.cancelAllCoreBackupPendingChanges($rootScope.currentApplication, version)
        }

        function cancelAllPendingDistribution(version) {
            return requestsService.cancelAllCoreBackupPendingDistribution($rootScope.currentApplication, version);
        }

        function approveAllPendingDistribution(version) {
            return requestsService.approveAllCoreBackupPendingDistribution($rootScope.currentApplication, version);
        }

        function getDistributionPendingPreview() {
            return requestsService.getCoreBackupDistributionPendingPreview($rootScope.currentApplication);
        }

        function getUnapprovedRulesIds(ruleType) {
            return requestsService.getIdsOfNewUnapprovedRules($rootScope.currentApplication, ruleType);
        }

        function approveAllNamespacesChanges(namespaceLists) {
            return requestsService.approveAllNamespacesChanges(namespaceLists);
        }

        function approveNamespacesChanges(item) {
            return requestsService.approveNamespacesChanges(item);
        }

        function cancelAllNamespacesChanges() {
            return requestsService.cancelAllNamespacesChanges();
        }

        function cancelNamespacesChanges(item) {
            return requestsService.cancelNamespacesChanges(item);
        }
    }
})();
