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
        .module('uxData.templates')
        .controller('Templates', Templates);

    Templates.$inject = ['$log', '$scope', '$stateParams', '$location', '$anchorScroll', 'dialogs', 'toastr', 'dialogs', 'authService',
        'USER_PERMISSIONS', 'templatesService', 'rulesService', 'urlRulesService', 'utilsService', 'RULES_CONSTANTS', 'messageService',
        'urlRulesBuilderService', 'pathRulesBuilderService', 'rulesBuilderService'];

    function Templates($log, $scope, $stateParams, $location, $anchorScroll, $dialogs, toastr, dialogs, authService,
        USER_PERMISSIONS, templatesService, rulesService, urlRulesService, utilsService, RULES_CONSTANTS, messageService,
        urlRulesBuilderService, pathRulesBuilderService, rulesBuilderService) {
        /* jshint validthis: true */
        var vm = this;


        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.templates = {};
        vm.name = '';
        vm.rules = {};
        vm.ruleType = 'rule';
        vm.templatePageName = '';
        vm.loading = true;

        vm.focusedRule = angular.isDefined($stateParams.ruleName) ? $stateParams.ruleName : '';

        vm.confirmDeleteTemplate= confirmDeleteTemplate;
        vm.isEmpty = utilsService.isMapEmpty;

        init();

        function init() {
            vm.name = $stateParams.name;
            var PATH = '';
            var ruleSpecificBuilderService;
            var rulesSpecificService;

            if (vm.name === RULES_CONSTANTS().TEMPLATES_TYPE.PATH) {
                PATH = RULES_CONSTANTS().TEMPLATES_TYPE.PATH;
                vm.ruleType = 'rule';
                vm.templatePageName = 'Flavor Rules';
                ruleSpecificBuilderService = pathRulesBuilderService;
                rulesSpecificService = rulesService;
            } else {
                PATH = RULES_CONSTANTS().TEMPLATES_TYPE.URL;
                vm.ruleType = 'urlRule';
                vm.templatePageName = 'URL Rules';
                ruleSpecificBuilderService = urlRulesBuilderService;
                rulesSpecificService = urlRulesService;
            }
            var ruleType =  vm.ruleType + "s";
            templatesService.getAll(vm.name).then(function (data) {
                rulesSpecificService.loadAllRules().then(function (rule) {
                    vm.rules = rulesBuilderService.unmarshallJSONTemplateRules(
                        ruleType,
                        data[vm.name],
                        data.pending,
                        data.servicePaths,
                        PATH,
                        ruleSpecificBuilderService,
                        rule);

                    //scroll to
                    if (vm.focusedRule !== '') {
                        $location.hash(vm.focusedRule);
                        $anchorScroll();
                    }
                    vm.loading = false;

                }, function (reason) {
                    var errorString = angular.isDefined(reason) ? reason.message : "error while getting url rules";
                    $log.error(errorString);
                    toastr.error(errorString, 'Error', {closeButton: true, timeOut: 3000});
                    vm.loading = false;
                });

            }, function (reason) {
                $log.error(reason.message);
                toastr.error(reason.message, 'Error', {closeButton: true, timeOut: 3000});
                vm.loading = false;
            });
        }

        function deleteTemplate(name, ruleId) {
            templatesService.deleteTemplate(name, ruleId)
                .then(function (data) {
                    toastr.success('Deleted template: \'' + ruleId + '\'', 'Success', {
                        closeButton: true,
                        timeOut: 3000
                    });
                    init();
                }, function (reason) {
                    var errorMsg = reason.data.message;
                    toastr.error('Template: \'' + ruleId + '\' failed to delete.' + errorMsg, 'Error', {closeButton: true, timeOut: 3000});
                });
        }

       function confirmDeleteTemplate(name, ruleId) {
            var msg = 'Are you sure you want to delete template \'' + ruleId + '\' ?';
            var dlg = dialogs.confirm(msg);
            dlg.result.then(function (btn) {
                    deleteTemplate(name, ruleId);
                },
                function () {}
            );
        }


        messageService.onChangeApp($scope, function (message) {
            init();
        });
    }

})();
