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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */


(function () {
    'use strict';
    angular
        .module('uxData.testsuite')
        .factory('testsuiteAlertsService', testsuiteAlertsService);


    testsuiteAlertsService.$inject = ['toastr'];

    function testsuiteAlertsService($toastr) {

        var service = {
            successSave: successSave,
            errorPost: errorSave,
            errorGet: errorGetItems,
            errorGetFile: errorGetFile,
            successRun: successRunTest,
            errorRunTest: errorRunTest,
            errorImportTest: errorImportTest,
            successImportTest: successImportTest,
            successDelete: successDelete,
            errorDelete: errorDelete,
            errorValidateIncomingTest: errorValidateIncomingTest,
            errorValidateIncomingTestNotDisplayed: errorValidateIncomingTestNotDisplayed,
            errorValidationNullUndefined: errorValidationNullUndefined
        };
        return service;

        function errorGetFile() {
            $toastr.error('Something wrong when convert file', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }


        function successSave(testCaseName) {
            $toastr.success('Test case ' + testCaseName + ' saved', 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorSave(reason) {
            $toastr.error('Can\'t save test cases (' + reason + ')', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successDelete(testCaseName) {
            $toastr.success('Deleted test case ' + testCaseName, 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

         function errorDelete(testCaseName) {
            $toastr.error('Error while deleting test case ' + testCaseName, 'Error', {
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

        function errorRunTest(testName) {
            $toastr.error('Cannot run test ' + testName +', please rerun', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorImportTest(testName) {
            $toastr.error('Cannot import test ' + testName +'', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorValidateIncomingTest(reason) {
            $toastr.error('Cannot validate test  for reason: \'' + reason +'\'. Probably, it`s corrupted or incompatible with this UI', 'Error', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function errorValidateIncomingTestNotDisplayed(testName, reason) {
            $toastr.error('Cannot validate test ' + testName + '  for reason: \'' + reason +
                '\'. Probably, it`s corrupted or incompatible with this UI. THIS TEST CASE WILL NOT BE DISPLAYED IN THE UI', 'Error', {
                closeButton: true,
                timeOut: 30000
            });
        }

        function errorValidationNullUndefined() {
            $toastr.error('Cannot validate incoming data from DS: there are undefined or null', 'Error', {
                    closeButton: true,
                    timeOut: 300000
                });
        }

        function successImportTest(testName) {
            $toastr.success('Test ' + testName +' was imported successfully', 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }

        function successRunTest(testName) {
            $toastr.success('Test ' + testName +' was executed successfully', 'Success', {
                closeButton: true,
                timeOut: 3000
            });
        }
    }
})();
