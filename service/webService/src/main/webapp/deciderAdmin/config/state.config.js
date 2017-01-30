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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */


(function () {
    'use strict';

    angular
        .module('uxData.config')
        .config(state);

    state.$inject = ['$stateProvider', '$urlRouterProvider', 'USER_PERMISSIONS', 'STATES_CONSTANTS'];

    function state($stateProvider, $urlRouterProvider, USER_PERMISSIONS, STATES_CONSTANTS) {

        $urlRouterProvider.otherwise( function($injector, $location) {
            var $rootScope = $injector.get('$rootScope');
            if (!$rootScope.isPageReloading) {
                var $state = $injector.get('$state');
                $state.go(STATES_CONSTANTS().showDeciderRules);
            }
        });

        $stateProvider
            .state(STATES_CONSTANTS().addDeciderRule, {
                controller: 'DeciderRulesEditController',
                controllerAs: 'vm',
                url: '/addNewDeciderRule',
                templateUrl: '../deciderAdmin/decider-rules/deciderRules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().readDeciderRules]
                }
            })
            .state(STATES_CONSTANTS().editDeciderRule, {
                controller: 'DeciderRulesEditController',
                controllerAs: 'vm',
                url: '/editDeciderRule/edit/:ruleId',
                templateUrl: '../deciderAdmin/decider-rules/deciderRules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().readDeciderRules]
                }
            })
            .state(STATES_CONSTANTS().showDeciderRules, {
                controller: 'ShowDeciderRulesController',
                controllerAs: 'vm',
                url: '/showDeciderRules/:ruleName',
                templateUrl: '../deciderAdmin/decider-rules/deciderRules-show.html',
                data: {
                    permissions: [USER_PERMISSIONS().readDeciderRules]
                }
            })
            .state(STATES_CONSTANTS().importDeciderRules, {
                controller: 'DeciderRulesImportController',
                controllerAs: 'vm',
                url: '/importDeciderRules',
                templateUrl: '../deciderAdmin/decider-rules/deciderRules-import.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeDeciderRules]
                }
            })            
            .state(STATES_CONSTANTS().partners, {
                controller: 'Partners',
                controllerAs: 'vm',
                url: '/partners',
                templateUrl: '../uxData/scripts/partners/partners.html',
                data: {
                    permissions: [USER_PERMISSIONS().readPartners]
                }
            })
            .state(STATES_CONSTANTS().error, {
                templateUrl: '../uxData/views/error.html'
            });


        $stateProvider
            .state(STATES_CONSTANTS().namespacesAdd, {
                controller: 'NamespacedDetail',
                controllerAs: 'vm',
                url: '/namespaces/add',
                templateUrl: '../uxData/scripts/namespaced/namespaced-edit.html',
                data: {
                    permissions: []
                }
            }).state(STATES_CONSTANTS().namespacesAddIp, {
                controller: 'NamespacedDetail',
                controllerAs: 'vm',
                url: '/namespaces/addIp',
                templateUrl: '../uxData/scripts/namespaced/namespaced-edit.html',
                params: {
                    type: 'IP'
                },
                data: {
                    permissions: []
                }
            }).state(STATES_CONSTANTS().namespacesAddEncoded, {
                controller: 'NamespacedDetail',
                controllerAs: 'vm',
                url: '/namespaces/addEncoded',
                templateUrl: '../uxData/scripts/namespaced/namespaced-edit.html',
                params: {
                    type: 'ENCODED'
                },
                data: {
                    permissions: []
                }
            })
            .state(STATES_CONSTANTS().namespacesImport, {
                controller: 'NamespacedImport',
                controllerAs: 'vm',
                url: '/namespaces/import',
                templateUrl: '../uxData/scripts/namespaced/namespaced-import.html',
                data: {
                    permissions: []
                }
            })
            .state(STATES_CONSTANTS().namespacesShow, {
                controller: 'NamespacedList',
                controllerAs: 'vm',
                url: '/namespaces/show',
                templateUrl: '../uxData/scripts/namespaced/namespaced-list.html',
                data: {
                    permissions: []
                }
            })
            .state(STATES_CONSTANTS().namespacesEdit, {
                controller: 'NamespacedDetail',
                controllerAs: 'vm',
                url: '/namespaces/edit/:id',
                templateUrl: '../uxData/scripts/namespaced/namespaced-edit.html',
                data: {
                    permissions: []
                }
            })
            .state(STATES_CONSTANTS().namespacesFindByItem, {
                controller: 'NamespacedSearcher',
                controllerAs: 'vm',
                url: '/namespaces/findByItem',
                templateUrl: '../uxData/scripts/namespaced/namespaced-search.html',
                data: {
                    permissions: []
                }
            })
            .state(STATES_CONSTANTS().namespacesSearch, {
                controller: 'NamespacedSearcher',
                controllerAs: 'vm',
                url: '/namespaces/findByItem/:serviceName/:search',
                templateUrl: '../uxData/scripts/namespaced/namespaced-search.html',
                data: {
                    permissions: []
                }
            });
    }
})();
