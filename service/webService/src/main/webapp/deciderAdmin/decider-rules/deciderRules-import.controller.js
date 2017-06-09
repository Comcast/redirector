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
        module('uxData.deciderRules').
        controller('DeciderRulesImportController', DeciderRulesImportController);

    DeciderRulesImportController.$inject = ['$stateParams', '$scope', '$log',
        'deciderRulesRequestService', 'rulesAlertsService', 'utilsService', 'authService', 'USER_PERMISSIONS', 'RULES_CONSTANTS',
        'deciderRulesBuilderService', 'rulesBuilderService', 'importService', 'messageService'];

    function DeciderRulesImportController($stateParams, $scope, $log, deciderRulesRequestService,
                                          rulesAlertsService, utilsService, authService, USER_PERMISSIONS, rulesCONST,
                                          deciderRulesBuilderService, rulesBuilderService, importService, messageService) {

        /* jshint validthis: true */
        var vm = this;

        var DECIDER_RULE = rulesCONST().RULE_TYPE.DECIDER;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.focusedRule = angular.isDefined($stateParams.ruleName) ? $stateParams.ruleName : '';

        vm.rulesFromFile = {};

        vm.loading = false;
        vm.getFile = getFile;
        vm.validateDuplicatesAndImportOneRule = validateDuplicatesAndImportOneRule;

        vm.isMapEmpty = utilsService.isMapEmpty;
        vm.importAll = importAll;

        /**
         * The 'open file' button handler
         * @param fileName
         */
        function getFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                $log.info('File ' + fileName + ' is read successfully');
                vm.loading = true;
                getRulesFromFile(result);
            }, function (reason) {
                $log.error('File is not read: ' + reason.message);
                rulesAlertsService.errorGetFile(reason.message);
            });
        }

        /**
         * validates and adds rules from an already read file
         * @param data
         */
        function getRulesFromFile(data) {
            try {
                $log.info('Trying to get rules from the following structure: ', data);
                var rulesCandidates = JSON.parse(data);

                if (angular.isDefined(rulesCandidates.id)) { // one rule
                    rulesCandidates = {'if': rulesCandidates};
                }
                vm.rulesFromFile = rulesBuilderService.unmarshallJSONRulesForPreview(
                    rulesCandidates,
                    undefined,
                    undefined,
                    DECIDER_RULE,
                    deciderRulesBuilderService);
                $log.info('Got rule(s) to import: ', vm.rulesFromFile);

                vm.loading = false;
                vm.title = 'Save rules by clicking \"Import\" button';
            } catch (e) {
                rulesAlertsService.genericError('can\'t parse file');
                $log.error('can\'t parse file');
                vm.loading = false;
            }
        }

        function importOneRule(ruleName) {
            deciderRulesRequestService.saveRule(vm.rulesFromFile[ruleName].rule, ruleName).then(
                function (result) {
                    delete(vm.rulesFromFile[ruleName]);
                    $log.info('Rule ' + ruleName + ' is imported')
                    rulesAlertsService.successImportRule(ruleName);
                },
                function (error) {
                    rulesAlertsService.genericError(error.message);
                    vm.rulesFromFile[ruleName].error = {
                        showError: true,
                        message: ('Error: ' + error.message)
                    };
                    $log.error('error importing rule ', error);
                }
            );
        }

        function validateDuplicatesAndImportOneRule(ruleName) {
            deciderRulesRequestService.getPartners().then(
                function (data) {
                    deciderRulesRequestService.getRuleIds().then(
                        function (ruleIds) {
                            if (validateRule(data, ruleIds, ruleName)) {
                                importOneRule(ruleName);
                            }
                        },
                        function (reason) {
                            rulesAlertsService.genericError(reason.message);
                            $log.error('Error while importing all rules: ', reason);
                        }
                    );
                },
                function (reason) {
                    rulesAlertsService.genericError(reason.message);
                    $log.error('Error while importing all rules: ', reason);
                }
            );
        }

        function validateRule(partners, ruleIds, ruleName) {
            if (angular.isDefined(partners) || angular.isDefined(ruleIds)) {
                if (angular.isDefined(ruleIds) && angular.isDefined(ruleIds.ids)) {
                    for (var ruleId in ruleIds.ids) {
                        if (ruleIds.ids.hasOwnProperty(ruleId) && (ruleId === ruleName)) {
                            rulesAlertsService.errorDuplicateCurrent(ruleName);
                            vm.rulesFromFile[ruleName].error = {
                                showError: true,
                                message: 'Error: the rule with this name already exists in current rules'
                            };
                            $log.error('Duplicates found ' + vm.rulesFromFile[ruleName].error.message);
                            return false;
                        }
                    }
                }
                var errorPartnerDoesNotExist = true;
                if (angular.isDefined(partners.partners) && angular.isDefined(partners.partners.partner) && angular.isArray(partners.partners.partner)) {
                    for (var i = 0; i < partners.partners.partner.length; i++) {
                        if (partners.partners.partner[i].id === vm.rulesFromFile[ruleName].rule.return.partner[0]) {
                            errorPartnerDoesNotExist = false;
                        }
                    }
                    if (errorPartnerDoesNotExist) {
                        rulesAlertsService.genericError(ruleName);
                        vm.rulesFromFile[ruleName].error = {
                            showError: true,
                            message: 'Error: partner does not exist'
                        };
                        $log.error('Partner does not exist ' + vm.rulesFromFile[ruleName].error.message);
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        function importAll() {
            $log.info('Importing all rules');
            deciderRulesRequestService.getPartners().then(
                function (data) {
                    deciderRulesRequestService.getRuleIds().then(
                        function (ruleIds) {
                            for (var ruleName in vm.rulesFromFile) {
                                if (vm.rulesFromFile.hasOwnProperty(ruleName) && validateRule(data, ruleIds, ruleName)) {
                                    importOneRule(ruleName);
                                }
                            }
                        },
                        function (reason) {
                            rulesAlertsService.genericError(reason.message);
                            $log.error('Error while importing all rules: ', reason);
                        }
                    );
                },
                function (reason) {
                    rulesAlertsService.genericError(reason.message);
                    $log.error('Error while importing all rules: ', reason);
                }
            );
        }

        messageService.onChangeApp($scope, function (message) {
            angular.copy({}, vm.rulesFromFile);
        });
    }
})();
