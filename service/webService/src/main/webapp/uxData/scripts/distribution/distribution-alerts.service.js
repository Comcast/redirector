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
    angular
        .module('uxData.distribution')
        .factory('distributionAlertsService', distributionAlertsService);


    distributionAlertsService.$inject = ['toastr'];

    function distributionAlertsService($toastr) {

        var service = {
            errorGetItems: errorGetItems,
            errorGetPaths: errorGetPaths,
            errorHasDuplicatedServers: errorHasDuplicatedServers,
            errorPercentageIncorrect: errorPercentageIncorrect,
            errorItemNotAdded: errorItemNotAdded,
            successSaved: successSaved,
            errorPathNotSelected: errorPathNotSelected,
            errorItemNotSaved: errorItemNotSaved,
            infoWillBeDeleted: infoWillBeDeleted

        };
        return service;

        function errorGetItems() {
            $toastr.error('Can\'t retrieve items', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorGetPaths() {
            $toastr.error('Can\'t get servicePath', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorHasDuplicatedServers() {
            $toastr.error('Duplicated servers!', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorPercentageIncorrect() {
            $toastr.error('Total percentage is more than 99.99%.', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorItemNotAdded() {
            $toastr.error('Add the new item first', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorItemNotSaved() {
            $toastr.error('Can\'t save items', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

       function errorPathNotSelected() {
            $toastr.error('Path is not selected!', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successSaved() {
            $toastr.success('Saved. Have you Verified Whitelist for Distribution?', 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function infoWillBeDeleted() {
            $toastr.error('This distribution will be deleted on Save', 'Reminder', {
                closeButton: true,
                timeOut: 3000
            });
        }
    }
})();
