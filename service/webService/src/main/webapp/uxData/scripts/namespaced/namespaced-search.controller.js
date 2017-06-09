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



/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedSearcher', NamespacedSearcher);

    NamespacedSearcher.$inject = ['$rootScope', '$scope', '$log', '$state', '$stateParams', '$window', 'toastr',
        'STATES_CONSTANTS', 'COMMON_CONSTANTS', 'RULES_CONSTANTS', 'authService', 'messageService', 'namespacedService', 'namespacedAlertsService'];
    function NamespacedSearcher($rootScope, $scope, $log, $state, $stateParams, $window, toastr, STATES_CONSTANTS,
                                COMMON_CONSTANTS, RULES_CONSTANTS, authService, messageService, namespacedService, namespacedAlertsService) {
        /* jshint validthis: true */
        var vm = this;
        vm.search = null;
        vm.searchResult = {};
        vm.isSearching = false;
        vm.changeServiceNameInUrlOnAppChanged = true;

        vm.goToSearchPage = goToSearchPage;
        vm.isSearchResultExists = isSearchResultExists;
        vm.goToEditPage = goToEditPage;
        vm.goToFlavorRulePage = goToFlavorRulePage;
        vm.goToTemplateFlavorRulePage = goToTemplateFlavorRulePage;
        vm.goToUrlRulePage = goToUrlRulePage;
        vm.goToTemplateUrlRulePage = goToTemplateUrlRulePage;
        vm.goToDeciderRulePage = goToDeciderRulePage;
        vm.isSearchResultsEmpty = isSearchResultsEmpty;
        vm.isSearchDataBlank = isSearchDataBlank;
        vm.searchNamespacesByItem = searchNamespacesByItem;

        init();

        function goToSearchPage() {
            $state.go(STATES_CONSTANTS().namespacesSearch, {serviceName: $rootScope.currentApplication, search: vm.search}, {reload: true});
        }

        function goToFlavorRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'showFlavorRules', {ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.PATH_RULES_SHOW));
            }
        }

        function goToTemplateFlavorRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'templates', {name: RULES_CONSTANTS().TEMPLATES_TYPE.PATH, ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.TEMPLATE_PATH_RULES_SHOW));
            }
        }

        function goToUrlRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'showURLRules', {ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.URL_RULES_SHOW));
            }
        }

        function goToTemplateUrlRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'templates', {name: RULES_CONSTANTS().TEMPLATES_TYPE.URL, ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.TEMPLATE_URL_RULES_SHOW));
            }
        }

        function goToDeciderRulePage(ruleName, serviceName) {
            if ($rootScope.currentApplication === COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                redirectToState(serviceName, 'showDeciderRules', {ruleName: ruleName});
            } else {
                redirectToHref(serviceName,
                    getShowRulesPageUrl(serviceName, ruleName, RULES_CONSTANTS().RULES_HASH_PATH.DECIDER_RULES_SHOW));
            }
        }

        function redirectToState(serviceName, stateName, stateParams) {
            if (authService.isAppAccessible(serviceName)) {
                changeApplication(serviceName, false);
                $state.go(stateName, stateParams);
            } else {
                toastr.error('You don\'t have permissions for application: ' + serviceName, 'Error', {
                    closeButton: true,
                    timeOut: 5000
                });
            }
        }

        function redirectToHref(serviceName, showRulePageUrl) {
            if (authService.isAppAccessible(serviceName)) {
                changeApplication(serviceName, false);
                $window.location.href = showRulePageUrl;
            } else {
                toastr.error('You don\'t have permissions for application: ' + serviceName, 'Error', {
                    closeButton: true,
                    timeOut: 5000
                });
            }
        }

        function getShowRulesPageUrl(serviceName, ruleName, hashPath) {
            var prodDevPath = isDevelopMode() ?
                (serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) ?
                    COMMON_CONSTANTS().DEVELOP_PATH.REDIRECTOR : COMMON_CONSTANTS().DEVELOP_PATH.DECIDER :
                (serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) ?
                    COMMON_CONSTANTS().PROD_PATH.REDIRECTOR : COMMON_CONSTANTS().PROD_PATH.DECIDER;
            return namespacedService.getBaseUrl() + '/' + prodDevPath + '/' + hashPath + ruleName + '#' + ruleName;
        }

        function isDevelopMode() {
            var developModePathsArray = [];
            developModePathsArray.push('/' + COMMON_CONSTANTS().DEVELOP_PATH.REDIRECTOR + '/'); // redirector
            developModePathsArray.push('/' + COMMON_CONSTANTS().DEVELOP_PATH.DECIDER + '/'); // decider
            return developModePathsArray.indexOf($window.location.href.replace($window.location.hash, '').replace(namespacedService.getBaseUrl(), '')) > 0;
        }

        function changeApplication(serviceName, changeServiceNameInUrlOnAppChanged) {
            vm.changeServiceNameInUrlOnAppChanged = changeServiceNameInUrlOnAppChanged;
            if (angular.isDefined(serviceName) && $rootScope.currentApplication !== serviceName &&
                serviceName !== COMMON_CONSTANTS().APPLICATIONS.DECIDER) {
                messageService.changeApp(serviceName);
            }
        }

        function goToEditPage(namespacedListName) {
            $state.go(STATES_CONSTANTS().namespacesEdit, {id: namespacedListName});
        }

        function searchNamespacesByItem(namespaceName) {
            namespacedService.searchNamespaces(namespaceName)
                .then(function (data) {
                    vm.searchResult = data;
                }, function (reason) {
                    $log.error('Reason: ' + reason.data.message);
                    namespacedAlertsService.errorSearch(reason.data.message);
                });
        }

        function isSearchResultExists() {
            return !vm.isSearching && angular.isDefined($stateParams.search) &&
                angular.isDefined(vm.searchResult.namespacedLists) &&
                vm.searchResult.namespacedLists.length > 0;
        }

        function isSearchDataBlank() {
            return !vm.isSearching && !angular.isDefined($stateParams.search);
        }

        function isSearchResultsEmpty() {
            return !vm.isSearching && angular.isDefined($stateParams.search) &&
                angular.isDefined(vm.searchResult.namespacedLists) &&
                vm.searchResult.namespacedLists.length === 0;
        }

        function init() {
            if (angular.isDefined($stateParams.serviceName)) {
                changeApplication($stateParams.serviceName, true);
            }
            vm.search = angular.isDefined($stateParams.search) ? angular.copy($stateParams.search) : '';
            if (vm.search.length !== 0) {
                searchNamespacesByItem(vm.search);
            }
        }

        messageService.onChangeApp($scope, function (message) {
            if (vm.changeServiceNameInUrlOnAppChanged) {
                goToSearchPage();
            }
        });

    }
})();
