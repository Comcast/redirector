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
        .module('uxData.namespaced')
        .factory('namespacedAlertsService', namespacedAlertsService);


    namespacedAlertsService.$inject = ['toastr'];

    function namespacedAlertsService($toastr) {


        var service = {
            success: successSave,
            duplicate: duplicateValue,
            errorPost: errorSave,
            willTryDelete: willTryDelete,
            errorPostWithMessage: errorPostWithMessage,
            errorGet: errorGetItems,
            errorDelete: errorDeleteItem,
            errorGetFile: errorGetFile,
            errorParse: errorParseFile,
            successDelete: successDelete,
            alreadyExists: alreadyExists,
            errorSearch: errorSearch
        };
        return service;


        function successSave(name) {
            var res = 'Data saved';
            if (angular.isDefined(name)) {
                res += ' - ' + name;
            }
            $toastr.success(res, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function duplicateValue(value) {
            $toastr.error('The \"' + value + '\" exists already.', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorSave() {
            $toastr.error('Can\'t save items', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorPostWithMessage(message) {
            $toastr.error('Can\'t save items: ' + message, 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function willTryDelete(name) {
            var res = 'Will try to delete namespaced list';
            if (angular.isDefined(name)) {
                res += ' - ' + name;
            }
            $toastr.info(res, 'Info', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successDelete(name) {
            var res = 'Deleted namespaced list';
            if (angular.isDefined(name)) {
                res += ' - ' + name;
            }
            $toastr.success(res, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorGetItems() {
            $toastr.error('Can\'t retrieve items', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorGetFile() {
            $toastr.error('Something wrong when convert file', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorParseFile() {
            $toastr.error('Can\'t parse file', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorDeleteItem() {
            $toastr.error('Can\'t delete value in namespaced list', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function alreadyExists(name) {
            $toastr.error('Can\'t save \'' + name + '\' is already exists', 'Error', {
                closeButton: true,
                timeOut: 6000
            });
        }

        function errorSearch(message) {
            $toastr.error('Can\'t get search result: ' + message, 'Error', {
                closeButton: true,
                timeOut: 6000
            });
        }

    }
})();
