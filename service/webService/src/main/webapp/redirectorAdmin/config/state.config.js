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
                $state.go(STATES_CONSTANTS().showFlavorRules);
            }
        });
        $stateProvider
            .state(STATES_CONSTANTS().showFlavorRules, {
                controller: 'rulesController',
                controllerAs: 'vm',
                url: '/flavorRules/showAll/:ruleName',
                templateUrl: '../uxData/scripts/rules/rules.html',
                data: {
                    permissions: [USER_PERMISSIONS().readRules]
                }
            })
            .state(STATES_CONSTANTS().editFlavorRule, {
                controller: 'RulesEditController',
                controllerAs: 'vm',
                url: '/flavorRules/edit/:ruleId/:serviceName',
                templateUrl: '../uxData/scripts/rules/rules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeRules]
                }
            })
            .state(STATES_CONSTANTS().addNewFlavorRule, {
                controller: 'RulesEditController',
                controllerAs: 'vm',
                url: '/flavorRules/addNew/:serviceName',
                templateUrl: '../uxData/scripts/rules/rules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeRules]
                }
            }).state(STATES_CONSTANTS().showURLRules, {
                controller: 'urlRulesShowController',
                controllerAs: 'vm',
                url: '/urlRules/showAll/:ruleName',
                templateUrl: '../uxData/scripts/url-rules/urlRules-show.html',
                data: {
                    permissions: [USER_PERMISSIONS().readUrlRules]
                }
            }).state(STATES_CONSTANTS().importFlavorRules, {
                controller: 'RulesImportController',
                controllerAs: 'vm',
                url: '/flavorRules/import',
                templateUrl: '../uxData/scripts/rules/rules-import.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeRules]
                }
             })
             .state(STATES_CONSTANTS().editURLRule, {
                controller: 'urlRulesEditController',
                controllerAs: 'vm',
                url: '/urlRules/edit/:ruleId/:serviceName',
                templateUrl: '../uxData/scripts/url-rules/urlRules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeUrlRules]
                }
            })
            .state(STATES_CONSTANTS().addNewURLRule, {
                controller: 'urlRulesEditController',
                controllerAs: 'vm',
                url: '/urlRules/addNew/:serviceName',
                templateUrl: '../uxData/scripts/url-rules/urlRules-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeUrlRules]
                }
            })
            .state(STATES_CONSTANTS().importURLRules, {
                controller: 'UrlRulesImportController',
                controllerAs: 'vm',
                url: '/urlRules/import',
                templateUrl: '../uxData/scripts/url-rules/url-rules-import.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeUrlRules]
                }
            })
            .state(STATES_CONSTANTS().distribution, {
                controller: 'DistributionController',
                controllerAs: 'vm',
                url: '/distribution',
                templateUrl: '../uxData/scripts/distribution/distribution.html',
                data: {
                    permissions: [USER_PERMISSIONS().readDistributions]
                }
            })
            .state(STATES_CONSTANTS().backup, {
                controller: 'BackupCtrl',
                url: '/backup',
                templateUrl: '../uxData/views/backup.html',
                data: {
                    permissions: [USER_PERMISSIONS().readBackups]
                }
            })
            .state(STATES_CONSTANTS().stacksManagement, {
                controller: 'stacksManagement',
                controllerAs: 'vm',
                url: '/stacksManagement',
                templateUrl: '../uxData/scripts/stacks-management/stacksManagement.html',
                data: {
                    permissions: [USER_PERMISSIONS().readStacks]
                }
            })
            .state(STATES_CONSTANTS().summary, {
                controller: 'summaryController',
                controllerAs: 'vm',
                url: '/summary',
                templateUrl: '../uxData/scripts/summary/summary.html',
                data: {
                    permissions: [USER_PERMISSIONS().readStacks]
                }
            })
            .state(STATES_CONSTANTS().changes, {
                controller: 'Changes',
                controllerAs: 'vm',
                url: '/changes',
                templateUrl: '../uxData/scripts/changes/changes.html',
                data: {
                    permissions: [USER_PERMISSIONS().readChanges]
                }
            })
            .state(STATES_CONSTANTS().settings, {
                controller: 'settingsController',
                controllerAs: 'vm',
                url: '/settings',
                templateUrl: '../uxData/scripts/settings/settings.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeSettings]
                }
            })
            .state(STATES_CONSTANTS().changesOffline, {
                controller: 'changesOffline',
                controllerAs: 'vm',
                url: '/CoreBackupChanges',
                templateUrl: '../uxData/scripts/changesOffline/changesOffline.html',
                data: {
                    permissions: [USER_PERMISSIONS().readChanges]
                }
            })
            .state(STATES_CONSTANTS().testsuiteShow, {
                controller: 'ShowTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/show',
                templateUrl: '../uxData/scripts/testsuite/testsuite-show.html',
                data: {
                    permissions: [USER_PERMISSIONS().readTestSuite]
                }
            })
            .state(STATES_CONSTANTS().testsuiteShowAuto, {
                controller: 'ShowAutoTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/showauto',
                templateUrl: '../uxData/scripts/testsuite/testsuite-showauto.html',
                data: {
                    permissions: [USER_PERMISSIONS().readTestSuite]
                }
            })
            .state(STATES_CONSTANTS().testsuiteAdd, {
                controller: 'EditTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/edit',
                templateUrl: '../uxData/scripts/testsuite/testsuite-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeTestSuite]
                }
            })
            .state(STATES_CONSTANTS().testsuiteEdit, {
                controller: 'EditTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/edit/:name',
                templateUrl: '../uxData/scripts/testsuite/testsuite-edit.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeTestSuite]
                }
            })
            .state(STATES_CONSTANTS().testsuiteRun, {
                controller: 'RunTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/run/:name',
                templateUrl: '../uxData/scripts/testsuite/testsuite-run.html',
                data: {
                    permissions: [USER_PERMISSIONS().readTestSuite]
                }
            })
            .state(STATES_CONSTANTS().testsuiteImport, {
                controller: 'ImportTestSuite',
                controllerAs: 'vm',
                url: '/testsuite/import',
                templateUrl: '../uxData/scripts/testsuite/testsuite-import.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeTestSuite]
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
                    type:'IP'
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
                    type:'ENCODED'
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


        $stateProvider
            .state(STATES_CONSTANTS().templates, {
                controller: 'Templates',
                controllerAs: 'vm',
                url: '/templates/:name/:ruleName',
                templateUrl: '../uxData/scripts/templates/templates.html',
                data: {
                    permissions: [USER_PERMISSIONS().readRules]
                }});

        $stateProvider
            .state(STATES_CONSTANTS().modelInitializer, {
                controller: 'modelInitializerController',
                controllerAs: 'vm',
                url: '/modelInitializer',
                templateUrl: '../uxData/scripts/model-initializer/model-initializer.html',
                data: {
                    permissions: [USER_PERMISSIONS().writeModelInitializer]
                }});

    }
})();
