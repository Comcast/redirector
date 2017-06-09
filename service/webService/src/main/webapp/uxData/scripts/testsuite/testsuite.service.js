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
        .module('uxData.testsuite')
        .service('testsuiteService', testsuiteService);

    testsuiteService.$inject = ['$q', '$log', '$state', '$rootScope', 'validationService', 'utilsService', 'testSuiteRequestService', 'validationServicetestsuite', 'testsuiteAlertsService'];

    /**
     * Encapsulates functions which are used by multiple controllers
     * Please always place local ang global declarations and functions in the appropriate sections
     * @constructor
     */
    function testsuiteService($q, $log, $state, $rootScope, validationService, utilsService, testSuiteRequestService, validationServicetestsuite, testsuiteAlertsService) {
        var service;
        var testNamesToRunNext = [];
        var testNamesToRun = [];

        var FAILED = 'failed';

        service = {
            arrayPropertiesToStrings: arrayPropertiesToStrings,
            getAndValidateOneTestCase: getAndValidateOneTestCase,
            getAndValidateAllTestCases: getAndValidateAllTestCases,
            getAllAutoTestCases: getAllAutoTestCases,
            getSimpleViewForTestCase: getSimpleViewForTestCase,
            getXmlViewForTestCase: getXmlViewForTestCase,
            generateDiffStringForTestResult: generateDiffStringForTestResult,
            deleteTestCaseByName: deleteTestCaseByName,
            findTestCaseByName: findTestCaseByName,
            findTestCaseIndexByName: findTestCaseIndexByName,
            validateAndSaveTestCase: validateAndSaveTestCase,
            testNamesToRunNext: testNamesToRunNext,
            testNamesToRun: testNamesToRun
        };

        $rootScope.isSaving = false;

        return service;


        /** GLOBAL functions */

        function arrayPropertiesToStrings(obj) {
            var result = angular.copy(obj);
            for (var key in result) {
                if (result.hasOwnProperty(key)) {
                    var value = result[key];
                    if (angular.isArray(value)) {
                        result[key] = value.join(', ');
                    }
                }
            }

            return result;
        }


        function getAndValidateOneTestCase(testName) {
            var defer = $q.defer();
            testSuiteRequestService.getTestCase(testName).then(
                function (data) {
                    $log.info('Got testcase', data);
                    validationServicetestsuite.validateTestCase(data).then(
                        function (validationSuccessMessage) {
                            $log.info('Testcase ', data, ' is valid');
                            defer.resolve(data);
                        },
                        function (reason) {
                            $log.error('Test case ', data, ' from DS is invalid: ', reason.data.message);
                            testsuiteAlertsService.errorValidateIncomingTest(reason.data.message);
                            defer.reject(reason);
                        }
                    );
                },
                function (reason) {
                    defer.reject(reason);
                    $log.error('Error while getting testcase: ', reason.data.message);
                }
            );
            return defer.promise;
        }

        function getAndValidateAllTestCases() {
            var defer = $q.defer();
            $log.info('Trying to get all test cases');
            testSuiteRequestService.getTestCases($rootScope.currentApplication).then(
                function(data) {
                    var validData = [];
                    var promises = [];
                    if (angular.isDefined(data.items.testCase) && angular.isArray(data.items.testCase)) {
                        angular.forEach(data.items.testCase, function(testCase) {
                            var promise = validationServicetestsuite.validateTestCase(testCase);
                            promise.testCase = testCase;
                            promises.push(promise);
                        });

                        $q.allSettled(promises).then(
                            function() {
                                angular.forEach(promises, function(promise, index) {
                                    promise.then(function (success) {
                                        validData.push(promise.testCase);
                                    },
                                    function (error) {
                                        $log.error('Test case ', promise.testCase.testName, promise.testCase, ' was invalid and WILL NOT BE DISPLAYED');
                                        testsuiteAlertsService.errorValidateIncomingTestNotDisplayed(promise.testCase.testName, promise.$$state.value);
                                    });
                                });

                                defer.resolve(validData);
                            });
                    } else {
                        //we're getting this response if no tests are present in Zk
                        defer.resolve([]);
                    }
                },
                function(reason) {
                    $log.error('Error getting all test cases: ', reason.data.message);
                    testsuiteAlertsService.errorGet(reason.data.message);
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function getAllAutoTestCases(mode) {
            var defer = $q.defer();
            $log.info('Trying to get all test cases');
            testSuiteRequestService.getAutoTestCases($rootScope.currentApplication, mode).then(
                function(data) {
                    var validData = [];
                    if (angular.isDefined(data.testCaseResult) && angular.isArray(data.testCaseResult)) {
                        angular.forEach(data.testCaseResult, function(testCaseResult) {
                            validData.push(testCaseResult)
                        });

                        defer.resolve(validData);
                    } else {
                        //we're getting this response if no tests are present in Zk
                        defer.resolve([]);
                    }
                },
                function(reason) {
                    $log.error('Error getting all automatically created test cases: ', reason.data.message);
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        /**
         * returns human-readable diff string for testCase response section, if it is available
         * @param testCase
         * @returns {string}
         */
        function generateDiffStringForTestResult(testCase) {
            if (angular.isDefined(testCase) &&
                angular.isDefined(testCase.actual &&
                    angular.isDefined(testCase.expected))) {
                var retString = '';
                if (testCase.status.toLowerCase() !== FAILED) {
                    retString = 'Passed';
                } else {
                    for (var property in testCase.actual) {
                        if (testCase.actual.hasOwnProperty(property)) {
                            if (testCase.expected.hasOwnProperty(property)) {
                                if (testCase.expected[property] !== testCase.actual[property] && !isEmptyProperty(testCase.expected[property])) {
                                    retString += 'expected ' + property + ' to be ' + testCase.expected[property] +
                                        ', was ' + testCase.actual[property] + '\n';
                                }
                            }
                        }
                    }
                }

                if (angular.isDefined(testCase.logs)) {
                    angular.forEach(testCase.logs, function(item) {
                        retString += "\n" + item;
                    });
                }
                if (angular.isDefined(testCase.actual['appliedUrlRules'])) {
                    retString += "\nurl rules applied: " + testCase.actual['appliedUrlRules'];
                }

                return retString;
            } else {
                return 'Response string is not defined, probably test was in error.';
            }
        }

        function isEmptyProperty(property) {
            return utilsService.isEmptyString(property) && utilsService.isNullOrUndefinedOrEmptyStringArrayOfValues(property);
        }

        function findTestCaseByName(name, testCases) {
            for (var i = 0; i < testCases.length; i++) {
                if (testCases[i].testName === name) {
                    return testCases[i];
                }
            }
        }

        function findTestCaseIndexByName(name, testCases) {
            for (var i = 0; i < testCases.length; i++) {
                if (testCases[i].testName === name) {
                    return i;
                }
            }
        }


        function deleteTestCaseByName(name) {
            var defer = $q.defer();
            testSuiteRequestService.deleteTestCase($rootScope.currentApplication, name).then(
                function (data) {
                    $log.warn('Deleted testcase', name, data);
                    defer.resolve(data);
                }, function (reason) {
                    testsuiteAlertsService.errorDelete(name);
                    $log.error('Error deleting testcase', reason.data.message);
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function saveTestCase (defer, testCase) {
            testSuiteRequestService.sendTestCase($rootScope.currentApplication, testCase).then(function (response) {
                testsuiteAlertsService.successSave(testCase.testName);
                defer.resolve(response);
                $log.info('Test case \'' + testCase.testName + '\' is successfully saved');
            }, function (reason) {
                $log.error('Cannot save test case: ', reason.data.message);
                testsuiteAlertsService.errorPost(reason.data.message);
                defer.reject(reason);
            });
        }

        function validateAndSaveTestCase(testCase, ignoreDuplicates) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            validationServicetestsuite.validateTestCase(testCase).then(
                function (data) {
                    testSuiteRequestService.getTestCase([testCase.testName]).then(
                        function (data) {
                            if (!ignoreDuplicates) {
                                $log.error('duplicates are found');
                                defer.reject('Duplicates are found');
                                $rootScope.isSaving = false;
                                testsuiteAlertsService.errorPost('Duplicates are found');
                            } else {
                                saveTestCase(defer, testCase);
                                $rootScope.isSaving = false;
                            }
                        },
                        function (error) {
                            saveTestCase(defer, testCase);
                            $rootScope.isSaving = false;
                        });
                }, function (reason) {
                    $log.error('Cannot save test case: ', reason.data.message);
                    testsuiteAlertsService.errorPost(reason.data.message);
                    defer.reject(reason);
                    $rootScope.isSaving = false;
                });
            return defer.promise;

        }

        /**
         * Gets a human-readable string that briefly describes a test case
         * @param testCase
         * @returns {string}
         */
        function getSimpleViewForTestCase(testCase) {
            var simpleString = '';
            if (angular.isDefined(testCase.testName)) {
                simpleString += 'Case ' + testCase.testName + ' ';
            } else {
                simpleString = 'Test case name is undefined.';
                return simpleString;
            }
            simpleString += 'for application ' + testCase.application + ', expected return is ';
            var expected = testCase.expected;
            simpleString += ((validationService.isNullOrUndefinedOrEmptyString(expected.protocol)) ? 'any_protocol' : expected.protocol) +
            '://' + (validationService.isNullOrUndefinedOrEmptyString(expected.ipVersion)? 'any_ip_ver' : (expected.ipVersion === '6'? '{ipv6 address}' : '{ipv4 address}')) +
            ':' + (validationService.isNullOrUndefinedOrEmptyString(expected.port) ? 'any_port' : expected.port) +
            '/' + (validationService.isNullOrUndefinedOrEmptyString(expected.urn) ? 'any_urn' : expected.urn) +
            ',' + (validationService.isNullOrUndefinedOrEmptyString(expected.xreStack) ? ' any stack' : ' stack ' + expected.xreStack) +
            ',' + (validationService.isNullOrUndefinedOrEmptyString(expected.flavor) ? ' any flavor' : ' flavor ' + expected.flavor) +
            ',' + (validationService.isNullOrUndefinedOrEmptyString(expected.rule) ? ' through any rule' : ' through rule ' + expected.rule) +
            ',' + (utilsService.isNullOrUndefinedOrEmptyStringArrayOfValues(expected.appliedUrlRules) ? ' through any URL rule' : ' through URL rules: ' + expected.appliedUrlRules);
            return simpleString;
        }

        function getXmlViewForTestCase(testCase) {
            var testCaseToProcess = angular.copy(testCase);
            for (var expected in testCaseToProcess.expected) {
                if (testCaseToProcess.expected.hasOwnProperty(expected)) {
                    if (utilsService.isEmptyString(testCaseToProcess.expected[expected])) {
                        delete(testCaseToProcess.expected[expected]);
                    }
                }
            }
            return $.format(utilsService.objectToXml(testCase), {method: 'xml'});
        }

        /** LOCAL functions */
    }
})();
