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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

(function () {
    'use strict';

    angular.
        module('uxData.settings').
        controller('settingsController', settingsController);

    settingsController.$inject = ['$scope', 'authService', 'USER_PERMISSIONS', 'utilsService', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'settingsService', 'toastr', 'dialogs'];

    function settingsController($scope, authService, USER_PERMISSIONS, utilsService, messageService, LOCALSTORAGE_PAGE_NAMES, settingsService, toastr, dialogs) {

        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.hasPermissions = utilsService.hasPermissions;
        vm.isSaving = false;
        vm.redirectorConfig = {
            minHosts: 0,
            appMinHosts: 0
            /*
            defaultWeightOfTheNode: 0 //TODO: APPDS-1860 uncomement for UI changes
            */
        };
        vm.saveSettings = saveSettings;

        initialize();

        messageService.onChangeApp($scope, function (message) {
            initialize();
        });

        function initialize() {
            loadRedirectorConfig();
        }

        function loadRedirectorConfig() {
            settingsService.getRedirectorConfig()
                .then(function (data) {
                    vm.redirectorConfig = angular.copy(data);
                }, function (reason) {
                    toastr.error('Can\'t load redirectorConfig', 'Error', {closeButton: true, timeOut: 3000});
                });
        }

        function saveSettings() {
            vm.isSaving = true;
            settingsService.saveRedirectorConfig(vm.redirectorConfig)
                .then(function (status) {
                    toastr.success('The settings have been successfully modified', 'Success', {
                        closeButton: true,
                        timeOut: 3000
                    });
                    vm.isSaving = false;
                }, function (error) {
                    vm.responseErrorMsg = error.data.message;
                    toastr.error('Can\'t save settings', 'Error', {closeButton: true, timeOut: 3000});
                    dialogs.error('Failed to save settings', vm.responseErrorMsg);
                    vm.isSaving = false;
                });
        }
    }
})();
