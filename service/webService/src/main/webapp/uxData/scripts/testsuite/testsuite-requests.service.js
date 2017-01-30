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

(function() {
    'use strict';
angular
    .module('uxData.testsuite')
    .factory('testSuiteRequestService', RequestsServicetestsuite);


    RequestsServicetestsuite.$inject = ['$rootScope', 'requestsService', 'testsuiteAlertsService'];

    function RequestsServicetestsuite ($rootScope, requestsService, testsuiteAlertsService) {
        return {
            getTestCases: getTestCases,
            getAutoTestCases: getAutoTestCases,
            getTestCase: getTestCase,
            sendTestCase: sendTestCase,
            exportTestCase: exportTestCase,
            exportTestCases: exportTestCases,
            runTestCase: runTestCase,
            deleteTestCases: deleteTestCases,
            deleteTestCase: deleteTestCase
        };


        function getTestCases (appName) {
            return requestsService.getData(requestsService.getBaseApiUrl() + 'testSuite/' + appName,
                    {'Accept': 'application/json'});
        }

        function getAutoTestCases (appName, mode) {
            return requestsService.getData(requestsService.getBaseApiUrl() + 'testSuite/runAuto/' + appName + "/" + mode,
                {'Accept': 'application/json'});
        }

        function exportTestCases (appName) {
            window.open(requestsService.getBaseApiUrl() + 'testSuite/export/' + appName);
        }

        function getTestCase (caseName) {
            return requestsService.getData(requestsService.getBaseApiUrl() + 'testSuite/' + $rootScope.currentApplication + '/' + caseName,
                {'Accept': 'application/json'});
        }

        function exportTestCase (appName, caseName) {
            window.open(requestsService.getBaseApiUrl() + 'testSuite/export/' + appName + '/' + caseName);
        }

        function runTestCase (caseName, mode) {
            return requestsService.getData(requestsService.getBaseApiUrl() + 'testSuite/' + $rootScope.currentApplication + '/' + caseName + '/' + mode,
                {'Accept': 'application/json'});
        }

        function sendTestCase (appName, testCase) {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'testSuite/' + appName + '/' + testCase.testName, testCase);
        }

        function deleteTestCases (appName, testNames) {
            if (!angular.isArray(testNames)) {
                testsuiteAlertsService.errorDelete();
            } else {
               return requestsService.deleteItem(requestsService.getBaseApiUrl() + 'testSuite/' + appName + '/' + testNames.join(),
                    {'Accept': 'application/json'});
            }
        }

        function deleteTestCase (appName, testName) {
            return deleteTestCases(appName, [testName]);
        }
    }
})();
