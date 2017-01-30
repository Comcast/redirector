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
        .module('uxData.changes')
        .factory('changesService', changesService);

    changesService.$inject = ['$rootScope', '$q', 'requestsService', 'changesRequestsService', 'rulesBuilderService',
        'pathRulesBuilderService', 'urlRulesBuilderService', 'distributionRequestService'];
    function changesService($rootScope, $q, requestsService, changesRequestsService, ruleBuilderService,
                            pathRulesBuilderService, urlRuleBuilderService, distributionRequestService) {

        var service = {
            loadPendingChanges: loadPendingChanges,
            generatePathRules: generatePathRules,
            generateUrlRules: generateUrlRules,
            generateWhitelistedData: generateWhitelistedData,
            getUrlParamsViewText: getUrlParamsViewText
        };
        return service;

        function getUrlParamsViewText(urlParams) {
            var urlParamsViewText = '';
            if (angular.isDefined(urlParams)) {
                urlParamsViewText = urlRuleBuilderService.getRuleReturnDiffText(urlParams);
                if (urlParamsViewText !== '') {
                    urlParamsViewText = '[' + urlParamsViewText + '\n]';
                }
            }
            return urlParamsViewText;
        }

        function loadDistributionPendingChanges() {
            var defer = $q.defer();
            changesRequestsService.getDistributionPendingPreview()
                .then(function (data) {
                    data.totalPercentage = calculateDistributionTotalPercentage(data);
                    defer.resolve({pendingDistribution: data});
                }, function (reason) {
                    defer.reject(reason);
                }
            );

            return defer.promise;
        }

        function loadDistribution() {
            var defer = $q.defer();
            distributionRequestService.getDistributions($rootScope.currentApplication)
                .then(function (data) {
                    data.totalPercentage = calculateDistributionTotalPercentage(data);
                    defer.resolve({currentDistribution: data});
                }, function (reason) {
                    defer.reject(reason);

                });
            return defer.promise;
        }

        function calculateDistributionTotalPercentage(distribution) {
            var total = 0;
            if (angular.isDefined(distribution)) {
                angular.forEach(distribution, function (value, key) {
                    total += angular.isUndefined(value.percent) ? 0 : value.percent;
                });
            }
            return parseFloat(total) || 0;
        }


        function loadWhitelisted() {
            var defer = $q.defer();
            requestsService.getWhitelisted($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve({whitelisted: data});
                }, function (reason) {
                    defer.reject(reason);
                });
            return defer.promise;
        }


        function loadPending() {
            var defer = $q.defer();
            changesRequestsService.getPendingChanges($rootScope.currentApplication)
                .then(function (data) {
                    defer.resolve({pending: data});
                }, function (reason) {
                    defer.reject(reason);

                });
            return defer.promise;
        }


        function generatePathRules(rules) {
            rules.entry.forEach(function (element) {
                var entry = element;

                if (angular.isDefined(entry.value.changedExpression)) {
                    var changedExpressionData = prepareExpressionData(entry.value.changedExpression, pathRulesBuilderService);
                    entry.value.changedExpression.ruleText = changedExpressionData.ruleText;
                    entry.value.changedExpression.return = changedExpressionData.returnStatement;
                }

                if (angular.isDefined(entry.value.currentExpression)) {
                    var currentExpressionData = prepareExpressionData(entry.value.currentExpression, pathRulesBuilderService);
                    entry.value.currentExpression.ruleText = currentExpressionData.ruleText;
                    entry.value.currentExpression.return = currentExpressionData.returnStatement;
                }
            });
            return rules;
        }

        function generateUrlRules(urlRules) {
            urlRules.entry.forEach(function (element) {
                var entry = element;

                if (angular.isDefined(entry.value.changedExpression)) {
                    var changedExpressionData = prepareUrlRuleExpressionData(entry.value.changedExpression, urlRuleBuilderService);
                    entry.value.changedExpression.ruleText = changedExpressionData.ruleText;
                    entry.value.changedExpression.returnUrlRule = changedExpressionData.returnStatement;
                }

                if (angular.isDefined(entry.value.currentExpression)) {
                    var currentExpressionData = prepareUrlRuleExpressionData(entry.value.currentExpression, urlRuleBuilderService);
                    entry.value.currentExpression.ruleText = currentExpressionData.ruleText;
                    entry.value.currentExpression.returnUrlRule = currentExpressionData.returnStatement;
                }
            });
            return urlRules;
        }

        function generateWhitelistedData(pendingWhitelisted, currentWhitelisted) {
            var result = {};
            result.whitelistCurrentDiff = '';
            result.whitelistChangedDiff = '';

            var changedWhitelistData = [];

            if (angular.isDefined(currentWhitelisted) && angular.isDefined(currentWhitelisted.paths) && angular.isArray(currentWhitelisted.paths.entry)) {
                for (var i = 0; i < currentWhitelisted.paths.entry.length; i++) {
                    if (currentWhitelisted.paths.entry[i].value.action !== 'DELETE') {
                        changedWhitelistData.push(currentWhitelisted.paths.entry[i].key);
                        result.whitelistCurrentDiff += currentWhitelisted.paths.entry[i].key + (i + 1 !== currentWhitelisted.paths.entry.length ? '\n' : '');
                    }
                }
            }

            pendingWhitelisted.entry.forEach(function (element) {
                var entry = element;

                if (angular.isDefined(entry.value.changedExpression)) {
                    if (entry.value.changeType === 'ADD') {
                        changedWhitelistData.push(entry.value.changedExpression.value);
                    }
                    if (entry.value.changeType === 'DELETE') {
                        //remove
                        var indexOf = changedWhitelistData.indexOf(entry.value.currentExpression.value);
                        if (indexOf !== -1) {
                            changedWhitelistData.splice(indexOf, 1);
                        }
                    }
                }
            });

            if (angular.isDefined(changedWhitelistData) && angular.isArray(changedWhitelistData)) {
                for (var y = 0; y < changedWhitelistData.length; y++) {
                    result.whitelistChangedDiff += changedWhitelistData[y] + (y + 1 !== changedWhitelistData.length ? '\n' : '');
                }
            }
            return result;
        }

        function prepareExpressionData(expression, ruleSpecificBuilderService) {

            var rule = ruleBuilderService.unmarshallJSONRule(expression, ruleSpecificBuilderService);

            return {
                ruleText: ruleBuilderService.getRuleDiffText(rule.expressions, rule.returnStatement, ruleSpecificBuilderService),
                returnStatement: rule.returnStatement
            };
        }

        function prepareUrlRuleExpressionData(expression, ruleSpecificBuilderService) {

            var rule = ruleBuilderService.unmarshallJSONRule(expression, ruleSpecificBuilderService);

            return {
                ruleText: ruleBuilderService.getRuleDiffText(rule.expressions, rule.returnStatement, ruleSpecificBuilderService),
                returnStatement: rule.returnStatement
            };
        }

        function loadPendingChanges() {
            $rootScope.isSaving = true;
            var result = {};
            var defer = $q.defer();
            var promises = [];
            promises.push($q.when(loadDistributionPendingChanges()));
            promises.push($q.when(loadDistribution()));
            promises.push($q.when(loadWhitelisted()));
            promises.push($q.when(loadPending()));

            $q.all(promises).then(
                function () {
                    angular.forEach(promises, function (promise, index) {
                        promise.then(function (data) {
                                angular.extend(result, data);
                            },
                            function (reason) {
                                $rootScope.isSaving = false;
                                defer.reject(reason);
                            });
                    });
                    $rootScope.isSaving = false;
                    defer.resolve(result);
                }, function (reason) {
                    $rootScope.isSaving = false;
                    defer.reject(reason);
                });
            return defer.promise;
        }
    }
})();
