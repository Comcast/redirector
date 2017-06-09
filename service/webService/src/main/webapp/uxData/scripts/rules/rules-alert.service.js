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

(function () {
    'use strict';
    angular
        .module('uxData.rules')
        .factory('rulesAlertsService', rulesAlertsService);


    rulesAlertsService.$inject = ['toastr', 'utilsService'];

    function rulesAlertsService($toastr, utils) {


        var service = {
            failedToLoadData: failedToLoadData,
            failedToSave: failedToSave,
            successfullySaved: successfullySaved,
            successfullyDeleted: successfullyDeleted,
            failedToDelete: failedToDelete,
            hasNotChangedWarning: hasNotChangedWarning,
            errorGetFile: errorGetFile,
            successImportRule: successImportRule,
            errorDuplicateCurrent: errorDuplicateCurrent,
            errorDuplicatePending: errorDuplicatePending,
            genericError: genericError

        };
        return service;


        function failedToLoadData(entity, reason) {
            var errorMsg = 'Failed to load \' ' + entity + ' \'';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successfullySaved(entity) {
            var message = 'Saved ' + entity;

            $toastr.success(message, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successfullyDeleted(entity) {
            var message = 'Deleted ' + entity;

            $toastr.success(message, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function failedToSave(entity, reason) {
            var errorMsg = 'Failed to save ' + entity +'.';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function failedToDelete(entity, reason) {
            var errorMsg = 'Failed to delete ' + entity +'.';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function hasNotChangedWarning(entity) {
            var message = 'Make some changes before saving the ' + entity;
            $toastr.warning(message, 'Warning', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorGetFile (reason) {
            var errorMsg = 'Failed to load file';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorDuplicateCurrent (reason) {
            var errorMsg = 'Error: rule duplicates a current one';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorDuplicatePending (reason) {
            var errorMsg = 'Error: rule duplicates a pending one';
            if (!utils.isEmptyString(reason)) {
                errorMsg += ' ' + reason;
            }
            $toastr.error(errorMsg, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function genericError (reason) {
            $toastr.error('An error occured: ' + reason, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successImportRule (entity) {
            var message = 'Imported rule ' + entity;

            $toastr.success(message, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }
    }
})();
