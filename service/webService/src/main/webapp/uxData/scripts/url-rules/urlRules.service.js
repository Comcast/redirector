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


(function() {
    'use strict';
    angular
        .module('uxData.urlRules')
        .factory('urlRulesService', urlRulesService);

    urlRulesService.$inject = ['$rootScope', '$q', '$log',
        'urlRulesRequestService', 'rulesService', 'utilsService', 'changesRequestsService', 'RULES_CONSTANTS', 'COMMON_CONSTANTS',
        'templatesService', 'rulesAlertsService'];

    function urlRulesService($rootScope, $q, $log,
                             urlRulesRequestService, rulesService, utils, changesRequestsService, rulesCONST, commonCONST,
                             templatesService, rulesAlertsService) {

        var service = {
            loadRulesAndTemplates: loadRulesAndTemplates,
            loadRuleAndTemplates: loadRuleAndTemplates,
            loadUrlRulesPathsChanges: loadUrlRulesPathsChanges,
            loadAllUrlRuleIds: loadAllUrlRuleIds,
            loadDefaultUrlParams: loadDefaultUrlParams,
            areUrlParamsEquals: areUrlParamsEquals,
            loadAllRules: loadAllRules
        };

        var URL_RULE = rulesCONST().RULE_TYPE.URL;
        var URL_PARAMS = commonCONST().PENDING_CHANGES_ENTITY_TYPE.URL_PARAMS;
        var DEFAULT_URL_PARAMS = commonCONST().PENDING_CHANGES_ENTITY_TYPE.URL_PARAMS;

        return service;

        function loadRulesAndTemplates() {
            var defer = $q.defer();
            var result = {};
            var promises = [];

            promises.push($q.when(urlRulesRequestService.getAllUrlRules()));
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

            promises.push($q.when(loadUrlRule(ruleId)));
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

        function loadAllTemplates() {
            var defer = $q.defer();
            templatesService.loadTemplateUrlRule()
                .then(function (data) {
                    var templatesMap = {};
                    angular.forEach(data.templateUrlPathRules.if, function(template, index) {
                        templatesMap[template.id] = template;
                    });
                    defer.resolve({templates: templatesMap});
            }, function (reason) {
                rulesAlertsService.failedToLoadData('URL Templates', reason);
                defer.reject();
            });
            return defer.promise;
        }

        function loadUrlRulesPathsChanges() {
            var defer = $q.defer();
            var result = {};
            var promises = [];
            promises.push($q.when(rulesService.loadServicePaths()));
            promises.push($q.when(rulesService.loadPendingChanges()));
            promises.push($q.when(urlRulesRequestService.getAllUrlRules()));

            $q.all(promises).then(
                function (results) {
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

        function loadAllUrlRuleIds(ruleType) {
            var result = {};
            var defer = $q.defer();
            var promises = [];
            promises.push($q.when(urlRulesRequestService.getAllApprovedUrlRuleIds()));
            promises.push($q.when(rulesService.getAllNotApprovedNewRulesIds(ruleType)));

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

        function loadUrlRule(ruleId) {
            var defer = $q.defer();
            urlRulesRequestService.getUrlRule(ruleId)
                .then(function (rule) {
                        defer.resolve({rule: {if: [rule]}});
                    }, function (reason) {
                        changesRequestsService.getPendingChangesJson($rootScope.currentApplication)
                            .then(function (pendingChanges) {
                                    var pendingRule = utils.getPendingChangesObjectById(pendingChanges, URL_RULE, ruleId);
                                    defer.resolve({rule: {if: [pendingRule.changedExpression]}});
                                }, function (reason) {
                                    defer.reject({message: reason});
                                    $log.error('Failed to load url rule ' + ruleId + '. ' + reason);
                                }
                            );
                    }
                );
            return defer.promise;
        }

        function loadDefaultUrlParams() {
            var defer = $q.defer();
            var result = {};
            var promises = [];
            promises.push($q.when(urlRulesRequestService.loadDefaultUrlParams()));
            promises.push($q.when(changesRequestsService.getPendingChangesJsonByEntityType(URL_PARAMS, 'default')));

            $q.all(promises).then(
                function (results) {
                    angular.forEach(results, function(data, index) {
                        angular.extend(result, data.result);
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function loadAllRules() {
            var defer = $q.defer();
            urlRulesRequestService.getAllUrlRules($rootScope.currentApplication)
                .then(function (result) {
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject();
                    $log.error('Failed to load url rules: ' + reason.message);
                    if (reason.status !== 404) {
                        rulesAlertsService.failedToLoadData('url rules');
                    }
                });
            return defer.promise;
        }

        function areUrlParamsEquals(urlParams1, urlParams2) {
            return angular.equals(urlParams1.protocol , urlParams2.protocol) &&
                angular.equals(urlParams1.port , urlParams2.port) &&
                angular.equals(urlParams1.urn , urlParams2.urn) &&
                angular.equals(urlParams1.ipProtocolVersion , urlParams2.ipProtocolVersion);
        }
    }
})();
