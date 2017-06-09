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
 */
(function () {
    'use strict';

    angular
        .module('uxData.modelInitializer')
        .controller('modelInitializerController', ModelInitializerController);

    ModelInitializerController.$inject = ['$scope', '$filter', '$state', '$rootScope', 'authService',
        'USER_PERMISSIONS', 'messageService', 'dialogs', 'toastr', 'ngTableParams', 'modelInitializerService', 'STATES_CONSTANTS'];

    function ModelInitializerController($scope, $filter, $state, $rootScope, authService, USER_PERMISSIONS, messageService, dialogs, toastr, ngTableParams, modelInitializerService, STATES_CONSTANTS) {
        var vm = this;
        vm.models = [];
        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        
        vm.createDefaultModel = createDefaultModel;
        const COUNT = 10;

        vm.jumpOnNewApplication = function jumpOnNewApplication(appName) {
            messageService.changeApp(appName);
            $rootScope.currentApplication = appName;
            $state.go(STATES_CONSTANTS().showFlavorRules);
        };

        init();

        function init() {
            initData();
        }

        function initData() {
            vm.tableParams = new ngTableParams({
                page: 1,           
                count: COUNT,
                sorting: {
                    active: 'asc'
                }
            }, {
                total: 0, // length of data
                getData: function ($defer, params) {
                    modelInitializerService.getAllExistingApplications().then(function (result) {
                        vm.models = result.modelStates;
                        var orderedData = params.sorting() ? $filter('orderBy')(vm.models, params.orderBy()) : vm.models;

                        orderedData = params.filter() ?
                            $filter('filter')(orderedData, params.filter()) : orderedData;

                        params.total(orderedData.length);
                        var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                        if (params.page() > totalPages) {
                            params.page(totalPages);
                        }

                        params.settings({counts: vm.models.length > COUNT ? [10, 25, 50] : []});
                        orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                        $defer.resolve(orderedData);
                    }, function () {

                    });
                }
            });
            vm.tableParams.settings().$scope = $scope;
        }

        function createDefaultModel(applicationName) {
            modelInitializerService.validateApplication(applicationName).then(function (data) {
                if (data == 'true') {
                    modelInitializerService.defaultModelConstructionDetails(applicationName).then(function (data) {
                        var defaultRoute = data.defaultRouteComposition;
                        var defaultUrlParts = data.defaultUrlPartsComposition;
                        var defaultStack = data.firstAvailableWhitelisted;
                        var constractionDetails = data.urlForRedirection;

                        var dlg = dialogs.confirm('Create new redirection rule', 'Are you sure you want to create new redirection rule?<br/> ' +
                            '<span> Connection will be redirected to ' + constractionDetails + '</span>' + '<br/><br/>' +
                            'The redirection rule will be created with next parameters:<br/>' +
                            '<ol><li> Default Distribution Route: ' + defaultRoute + '</li>' +
                            '<li> Default UrlParts: ' + defaultUrlParts + '</li>' +
                            '<li> Default Whitelisted: ' + defaultStack + '</li></ol>');

                        dlg.result.then(function (btn) {
                            modelInitializerService.createDefaultModel(applicationName)
                                .then(function (data) {
                                    toastr.success('The application: \"' + applicationName + '\" is enabled', 'Success', {
                                        closeButton: true,
                                        timeOut: 3000
                                    });

                                    vm.tableParams.reload();
                                }, function (reason) {
                                    toastr.error(reason.message.message, 'Error', {closeButton: true, timeOut: 6000});
                                });
                        });
                    });
                } else {
                    vm.tableParams.reload();
                    toastr.warning("Could not create the new Redirection rule. Try to do it manually", 'Warning', {closeButton: true, timeOut: 6000});
                }
            }, function (error) {
                toastr.error(reason.message.message, 'Error', {closeButton: true, timeOut: 6000});
            });
        }
    }
})();
