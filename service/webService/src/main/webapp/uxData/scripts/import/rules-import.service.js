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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';

    angular.
        module('uxData.services').
        factory('RulesImportService', RulesImportService);

    RulesImportService.$inject = ['$log', '$rootScope', '$q', 'rulesService', 'urlRulesService', 'templatesService',
        'rulesRequestService', 'rulesAlertsService', 'RULES_CONSTANTS',
        'pathRulesBuilderService', 'rulesBuilderService', 'importService', 'urlRulesBuilderService', 'urlRulesRequestService'];

    function RulesImportService ($log, $rootScope, $q, rulesService, urlRulesService, templatesService, rulesRequestService,
                                    rulesAlertsService, rulesCONST,
                                    pathRulesBuilderService, rulesBuilderService, importService, urlRulesBuilderService, urlRulesRequestService) {

        var PATH_RULE = rulesCONST().RULE_TYPE.PATH;
        var URL_RULE = rulesCONST().RULE_TYPE.URL;
        /* jshint validthis: true */
        return {
          getFile: getFile,
          validateDuplicatesAndImportOneRule: validateDuplicatesAndImportOneRule,
          importAll: importAll
        };


        /**
         * The 'open file' button handler
         * @param fileName
         * @param ruleType
         */
        function getFile(fileName, ruleType) {
            var defer = $q.defer();
            importService.openFile(fileName, null, $rootScope).then(function (result) {
                $log.info('File ' +  fileName + ' is read successfully');
                getRulesFromFile(result, ruleType).then(
                    function(result) {
                        defer.resolve(result);
                    },
                    function (error) {
                        defer.reject(error);
                    }
                );
            }, function (reason) {
                $log.error('File is not read: ' + reason.message);
                rulesAlertsService.errorGetFile(reason.message);
                defer.reject(reason);
            });
            return defer.promise;
        }

        /**
         * validates and adds rules from an already read file
         * @param data
         * @param ruleType
         */
        function getRulesFromFile(data, ruleType) {
            var defer = $q.defer();
            rulesService.loadServicePaths().then(function (stacks) {
                    try {
                        stacks = stacks.servicePaths;
                        $log.info('Trying to get rules from the following structure: ', data);
                        var rulesCandidates = JSON.parse(data);
                        if (angular.isDefined(rulesCandidates.id)) {
                            rulesCandidates = {'if': rulesCandidates};
                        }

                        defer.resolve(rulesBuilderService.unmarshallJSONRulesForPreview(
                            rulesCandidates,
                            undefined,
                            ruleType === PATH_RULE ? stacks : null,
                            ruleType,
                            ruleType === PATH_RULE ? pathRulesBuilderService : urlRulesBuilderService));
                    } catch (e) {
                        defer.reject();
                        rulesAlertsService.genericError('can\'t parse file');
                        $log.error('can\'t parse file', e);
                    }
                },
                function (error) {
                    defer.reject();
                    $log.error('Error loading stacks');
                });
            return defer.promise;
        }

        function importOneRule (ruleName, rulesFromFile, ruleType) {
            var defer = $q.defer();
            var saveFunction = ruleType === PATH_RULE ? rulesRequestService.saveRule : urlRulesRequestService.saveUrlRule;
            saveFunction(rulesFromFile[ruleName].rule, ruleName).then(
                function(result) {
                    delete(rulesFromFile[ruleName]);
                    $log.info('Rule ' + ruleName + ' is imported');
                    rulesAlertsService.successImportRule(ruleName);
                    defer.resolve()
                },
                function(error) {
                    rulesAlertsService.genericError(angular.isDefined(error.data.message) ? error.data.message : error.message);
                    rulesFromFile[ruleName].error = {
                        showError: true,
                        message: ('Error: ' + angular.isDefined(error.data.message) ? error.data.message : error.message)
                    };
                    $log.error('error importing rule ', error.data.message);
                    defer.reject(error);
                }
            );
            return defer.promise;
        }

        function importOneTemplate (ruleName, rulesFromFile, ruleType) {
            var defer = $q.defer();
            templatesService.saveTemplate(ruleType === PATH_RULE ? rulesCONST().TEMPLATES_TYPE.PATH : rulesCONST().TEMPLATES_TYPE.URL, rulesFromFile[ruleName].rule, ruleName).then(
                function(result) {
                    delete(rulesFromFile[ruleName]);
                    $log.info('Rule ' + ruleName + ' is imported as template');
                    rulesAlertsService.successImportRule(ruleName);
                    defer.resolve()
                },
                function(error) {
                    rulesAlertsService.genericError(angular.isDefined(error.message.data) ? error.message.data : error.message);
                    rulesFromFile[ruleName].error = {
                        showError: true,
                        message: ('Error: ' + error.message.data)
                    };
                    $log.error('Error importing rule as template', error);
                    defer.reject(error);
                }
            );
            return defer.promise;
        }

        function validateDuplicatesAndImportOneRule (ruleName, asTemplate, rulesFromFile, ruleType) {
            var loadRulesFunction = ruleType === PATH_RULE ? rulesService.loadRulesPathsChanges : urlRulesService.loadUrlRulesPathsChanges;
            loadRulesFunction().then(
                function(data) {
                    if (validateDuplicates(data, ruleName, rulesFromFile, ruleType)) {
                        if (asTemplate) {
                            importOneTemplate(ruleName, rulesFromFile, ruleType);
                        } else {
                            importOneRule(ruleName, rulesFromFile, ruleType);
                        }
                    }
                },
                function(reason) {
                    rulesAlertsService.genericError(reason.message);
                    rulesFromFile[ruleName].error = {
                        showError: true,
                        message: ('Error: ' + reason.message.data)
                    };
                    $log.error('Error validating rule', error);
                }
            );
        }

        function validateDuplicates(data, ruleName, rulesFromFile, ruleType) {
            if (angular.isDefined(data)) {
                var rules = ruleType == PATH_RULE ? data.rules : data.urlRules;
                if (angular.isDefined(rules) && angular.isDefined(rules.if) && angular.isArray(rules.if)) {
                    for (var i = 0; i < rules.if.length; i++) {
                        if (rules.if[i].id === ruleName) {
                            rulesAlertsService.errorDuplicateCurrent(ruleName);
                            rulesFromFile[ruleName].error = {
                                showError: true,
                                message: 'Error: the rule with this name already exists in current rules'
                            };
                            $log.error('Duplicates found ' + rulesFromFile[ruleName].error.message);
                            return false;
                        }
                    }
                }
                var changes = ruleType == PATH_RULE ? data.pendingChanges.pathRules : data.pendingChanges.urlRules;
                if (angular.isDefined(changes)  &&
                    angular.isDefined(changes.entry) && angular.isArray(changes.entry)) {
                    for (var i = 0; i < changes.entry.length; i++) {
                        if (changes.entry[i].key === ruleName) {
                            rulesAlertsService.errorDuplicatePending(ruleName);
                            rulesFromFile[ruleName].error = {
                                showError: true,
                                message: 'Error: the rule with this name already exists in pending rules'
                            };
                            $log.error('Duplicates found ' + rulesFromFile[ruleName].error.message);
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        function importAll(rulesFromFile, ruleType) {
            $log.info('Importing all rules');
            var loadRulesFunction = ruleType === PATH_RULE ? rulesService.loadRulesPathsChanges : urlRulesService.loadUrlRulesPathsChanges;
            loadRulesFunction().then(
                function (data) {
                    var ruleNameQueue = [];
                    for (var ruleName in rulesFromFile) {
                        if (rulesFromFile.hasOwnProperty(ruleName)) {
                            ruleNameQueue.push(ruleName);
                        }
                    }
                    importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);
                },
                function (reason) {
                    rulesAlertsService.genericError(reason.message);
                    $log.error('Error while importing all rules: ', reason);
                }
            );
        }

        function importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue) {
            if (ruleNameQueue.length == 0) {
                return;
            }

            var ruleName = ruleNameQueue[0];
            if (rulesFromFile.hasOwnProperty(ruleName) && validateDuplicates(data, ruleName, rulesFromFile, ruleType)) {
                if (rulesFromFile[ruleName].asTemplate) {
                    importOneTemplate(ruleName, rulesFromFile, ruleType).then(
                        function (result) {
                            ruleNameQueue.splice(0,1);
                            importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);
                        },
                        function (error) {
                            ruleNameQueue.splice(0,1);
                            importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);
                        }
                    );
                } else {
                    importOneRule(ruleName, rulesFromFile, ruleType).then(
                        function (result) {
                            ruleNameQueue.splice(0,1);
                            importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);

                        },
                        function (error) {
                            ruleNameQueue.splice(0,1);
                            importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);
                        }
                    );
                }
            } else {
                ruleNameQueue.splice(0,1);
                importAllRecursive(rulesFromFile, data, ruleType, ruleNameQueue);
            }
        }
    }
})();
