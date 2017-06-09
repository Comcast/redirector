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
        .module('uxData.rules')
        .factory('rulesService', rulesService);

    rulesService.$inject = ['$rootScope', '$q', '$log',
        'rulesRequestService', 'rulesAlertsService', 'utilsService', 'RULES_CONSTANTS',
        'SERVER_CONSTANTS', 'COMMON_CONSTANTS', 'changesRequestsService', 'templatesService'];

    function rulesService($rootScope, $q, $log,
                          rulesRequestService,  rulesAlertsService, utils, rulesCONST, serverCONST, commonCONST,
                          changesRequestsService, templatesService) {

        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;

        var service = {
            loadRulesAndTemplates: loadRulesAndTemplates,
            loadRuleAndTemplates: loadRuleAndTemplates,
            loadRulesPathsChanges: loadRulesPathsChanges,
            loadServicePaths: loadServicePaths,
            loadPendingChanges: loadPendingChanges,
            loadAllRules: loadAllRules,
            loadAllRuleIds: loadAllRuleIds,
            getAllNotApprovedNewRulesIds: getAllNotApprovedNewRulesIds,
            loadRule: loadRule,
            areServersEqual: areServersEqual
        };

        return service;

//===================================== public  methods =====================================//

        function loadRulesAndTemplates() {
            var defer = $q.defer();
            var result = {};
            var promises = [];

            promises.push($q.when(loadAllRules()));
            promises.push($q.when(loadAllTemplates()));

            $q.all(promises)
                .then(function (results) {
                    angular.forEach(results, function(data, index) {
                        angular.extend(result, data);
                    });
                    defer.resolve(result)
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function loadRuleAndTemplates(ruleId) {
            var defer = $q.defer();
            var result = {};
            var promises = [];

            promises.push($q.when(loadRule(ruleId)));
            promises.push($q.when(loadAllTemplates()));

            $q.all(promises)
                .then(function (results) {
                    angular.forEach(results, function(data, index) {
                        angular.extend(result, data);
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function loadRulesPathsChanges() {
            var defer = $q.defer();
            var result = {};
            var promises = [];
            promises.push($q.when(loadServicePaths()));
            promises.push($q.when(loadPendingChanges()));
            promises.push($q.when(loadAllRules()));

            $q.all(promises).then(
                function (results) {
                    angular.forEach(results, function(data, index) {
                        angular.extend(result, data);
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function loadAllRuleIds(ruleType) {
            var result = {};
            var defer = $q.defer();
            var promises = [];
            promises.push($q.when(getAllApprovedRuleIds()));
            promises.push($q.when(getAllNotApprovedNewRulesIds(ruleType)));

            $q.all(promises).then(
                function (results) {
                    angular.forEach(results, function (data, index) {
                        angular.extend(result, data);
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject(reason);
                    $log.error('Failed to load rules IDs');

                });
            return defer.promise;
        }

        function getAllApprovedRuleIds() {
            var defer = $q.defer();
            rulesRequestService.getAllRuleIds()
                .then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve(ruleIdsMap);
                }, function (reason) {
                    defer.reject({message: reason});
                    $log.error('Failed to load existing approved rule names. ' + reason);
                }
            );
            return defer.promise;
        }

        function getAllNotApprovedNewRulesIds(ruleType) {
            var defer = $q.defer();
            changesRequestsService.getUnapprovedRulesIds(ruleType)
                .then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve(ruleIdsMap);
                }, function (reason) {
                    defer.reject({message: reason});
                    $log.error('Failed to load existing rule names of pending changes. ' + reason);
                }
            );
            return defer.promise;
        }

        function loadRule(ruleId) {
            var defer = $q.defer();
            rulesRequestService.getRule(ruleId)
                .then(function (rule) {
                    defer.resolve({rule: {if: [rule]}});
                }, function (reason) {
                    changesRequestsService.getPendingChangesJson($rootScope.currentApplication)
                        .then(function (pendingChanges) {
                                var pendingRule = utils.getPendingChangesObjectById(pendingChanges, PATH_RULE, ruleId);
                                defer.resolve({rule: {if: [pendingRule.changedExpression]}});
                            }, function (reason) {
                                defer.reject({message: reason});
                                $log.error('Failed to load rule ' + ruleId + '. ' + reason);
                            }
                        );
                }
            );
            return defer.promise;
        }

        function loadServicePaths() {
            var defer = $q.defer();
            rulesRequestService.getServicePaths()
                .then(function (result) {
                    defer.resolve({servicePaths: result});
                    $log.info('Service paths successfully loaded');
                }, function (reason) {
                    defer.reject(reason);
                    $log.error('Failed to load service paths: ' + reason.message);
                });
            return defer.promise;
        }

//===================================== private methods =====================================//

        function loadPendingChanges() {
            var defer = $q.defer();
            rulesRequestService.getPendingChanges($rootScope.currentApplication)
                .then(function (result) {
                    defer.resolve({pendingChanges: result});
                }, function (reason) {
                    defer.reject();
                    $log.error('Failed to load pending changes for rules: ' + reason.message);
                });
            return defer.promise;
        }

        function loadAllRules() {
            var defer = $q.defer();
            rulesRequestService.getAllRules($rootScope.currentApplication)
                .then(function (result) {
                    defer.resolve({rules: result});
                }, function (reason) {
                    defer.reject();
                    $log.error('Failed to load rules: ' + reason.message);
                    if (reason.status !== 404) {
                        rulesAlertsService.failedToLoadData('rules');
                    }
                });
            return defer.promise;
        }

        function loadAllTemplates() {
            var defer = $q.defer();
            templatesService.loadTemplateRule()
                .then(function (data) {
                    var templatesMap = {};
                    angular.forEach(data.templatePathRules.if, function(template, index) {
                        templatesMap[template.id] = template;
                    });
                    defer.resolve({templates: templatesMap});
                }, function (reason) {
                    rulesAlertsService.failedToLoadData('templates');
                    defer.reject();
                }
            );
            return defer.promise;
        }

        function areServersEqual (server1, server2) {
            var areServersEqual = false;

            if (angular.equals(server1.editMode, server2.editMode)) {
                areServersEqual = angular.equals(server1.editMode, SIMPLE) ?
                    angular.equals(server1.path, server2.path) : angular.equals(server1.url, server2.url);
            }

            return areServersEqual;
        }
    }
})();


