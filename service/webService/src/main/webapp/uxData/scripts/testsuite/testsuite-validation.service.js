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
        .factory('validationServicetestsuite', ValidationServicetestsuite);


    ValidationServicetestsuite.$inject = ['$q', 'REGEXP_CONSTANTS', 'validationService', 'utilsService'];

    function ValidationServicetestsuite($q, REGEXP_CONSTANTS, validationService, utilsService) {
        return {
            isNameValid: isNameValid,
            isParameterNameValid: isParameterNameValid,
            isParameterValueValid: isParameterValueValid,
            isProtocolValid: isProtocolValid,
            isIpValid: isIpValid,
            isIpVersionValid: isIpVersionValid,
            isPortValid: isPortValid,
            isUrnValid: isUrnValid,
            isStackValid: isStackValid,
            isFlavorValid: isFlavorValid,
            isModeValid: isModeValid,
            isResponseTypeValid: isResponseTypeValid,
            isRuleNameValid: isRuleNameValid,
            validateTestCase: validateTestCase,
            validateTestResult: validateTestResult,
            validateTestCaseInstantly: validateTestCaseInstantly
        };

        /**
         * Validates test case name
         * @param name
         * @returns {boolean}
         */
        function isNameValid(name) {
            if (validationService.isUnDefinedOrNullOrHasMiddleSpaces(name)) {
                return false;
            }
            if (!name.match(REGEXP_CONSTANTS().alphaNumericalWithUnderscores)) {
                return false;
            }
            return true;
        }

        /**
         * Validates test case parameter name
         * @param name
         * @returns {boolean}
         */
        function isParameterNameValid(name) {
            if (validationService.isUnDefinedOrNullOrHasMiddleSpaces(name)) {
                return false;
            }
            if (!name.match(REGEXP_CONSTANTS().alphaNumericalWithUnderscoresDotsAndColons)) {
                return false;
            }
            return true;
        }


        /**
         * Validates test case parameter value
         * @param value
         * @returns {boolean}
         */
        function isParameterValueValid(value) {
            if (validationService.isUnDefinedOrNullOrHasMiddleSpaces(value)) {
                return false;
            }
            return true;
        }

        /**
         * Validates expected protocol
         * (Protocol examples: http, https, xre, xres etc: )
         * @param protocol
         * @returns {boolean}
         */
        function isProtocolValid(protocol) {
            return (validationService.isProtocolValid(protocol) || utilsService.isEmptyString(protocol));
        }

        /**
         * Validates expected ip
         * IP examples:
         * IpV4: 192.168.0.1
         * IpV6: FE80:0000:0000:0000:0202:B3FF:FE1E:8329
         * @param ip
         * @returns {boolean}
         */
        function isIpValid(ip) {
            return validationService.isIpValid(ip) || utilsService.isEmptyString(ip);
        }

        /**
         * Validates expected ip version
         * IP version examples:
         * IpV4: 4
         * IpV6: 6
         * @param ipVersion
         * @returns {boolean}
         */
        function isIpVersionValid(ipVersion) {
            return validationService.isIpVersionValid(ipVersion) || utilsService.isEmptyString(ipVersion);
        }

        /**
         * Validates expected port
         * @param port
         * @returns {boolean}
         */
        function isPortValid(port) {
            return validationService.isPortValid(port) || utilsService.isEmptyString(port);
        }

        /**
         * Validates expected urn
         * @param urn
         * @returns {boolean}
         */
        function isUrnValid(urn) {
            return validationService.isUrnValid(urn) || utilsService.isEmptyString(urn);
        }

        /**
         * Validates expected stack
         * Example: /po/poc3
         * @param stack
         * @returns {boolean}
         */
        function isStackValid(stack) {
            return validationService.isStackValid(stack) || utilsService.isEmptyString(stack);
        }

        /**
         * Validates an expected flavor
         * Example: 1.41, xappl-1.41
         * @param flavor
         * @returns {boolean}
         */
        function isFlavorValid(flavor) {
            return validationService.isFlavorValid(flavor) || utilsService.isEmptyString(flavor);
        }

        /**
         * Validates an expected mode
         * @param mode
         * @returns {boolean}
         */
        function isModeValid(mode) {
            if (validationService.isUnDefinedOrNullOrHasMiddleSpaces(mode) && !utilsService.isEmptyString(mode)) {
                return false;
            }
            return true;
        }

        /**
         * Validates expected response
         * @param type
         * @returns {boolean}
         */
        function isResponseTypeValid(type) {
            if (validationService.isUnDefinedOrNullOrHasMiddleSpaces(type) && !utilsService.isEmptyString(type)) {
                return false;
            }
            return true;
        }

        /**
         * Validates an expected rule name
         * @param name
         * @returns {boolean}
         */
        function isRuleNameValid(name) {
            return validationService.isRuleNameValid(name) || utilsService.isEmptyString(name);
        }

        function buildReturnForValidate (result, message) {
            return {
                result: result,
                message: message
            };
        }

        /**
         * Validates test case and returns a promise
         * @param testCase
         * @returns {*}
         */
        function validateTestCase (testCase) {
            var defer = $q.defer();
            var result = validateTestCaseInstantly(testCase);
            if (result.result) {
                defer.resolve(result.message);
            } else {
                defer.reject(result.message);
            }
            return defer.promise;
        }

        /**
         * Validates an entire test case by returning a result object
         * @param testCase
         */
        function validateTestCaseInstantly(testCase) {
            try {
                var atLeastOneExpectedIsPresent = false;
                if (validationService.isNullOrUndefined(testCase)) {
                    return buildReturnForValidate(false, 'testCase is null or undefined');
                }
                for (var expected in testCase.expected) {
                    if (testCase.expected.hasOwnProperty(expected)) {
                        if (!utilsService.isEmptyString(testCase.expected[expected])) {
                            atLeastOneExpectedIsPresent = true;
                            break;
                        }
                        if (Array.isArray(testCase.expected[expected]) && testCase.expected[expected].length != 0) {
                            atLeastOneExpectedIsPresent = true;
                            break;
                        }
                    }
                }
                if (!atLeastOneExpectedIsPresent) {
                    return buildReturnForValidate(false, 'All expected properties are empty. At least one is required');
                }
                if (!isRuleNameValid(testCase.expected.rule)) {
                    return buildReturnForValidate(false, 'expected rule name is invalid');
                }
                if (!isRuleNameValid(testCase.expected.urlRule)) {
                    return buildReturnForValidate(false, 'expected URL rule name is invalid');
                }
                if (!isResponseTypeValid(testCase.expected.responseType)) {
                    return buildReturnForValidate(false, 'expected response type is invalid');
                }
                if (!isFlavorValid(testCase.expected.flavor)) {
                    return buildReturnForValidate(false, 'expected response flavor is invalid');
                }
                if (!isStackValid(testCase.expected.xreStack)) {
                    return buildReturnForValidate(false, 'expected response stack is invalid');
                }
                if (!isUrnValid(testCase.expected.urn)) {
                    return buildReturnForValidate(false, 'expected response urn is invalid');
                }
                if (!isPortValid(testCase.expected.port)) {
                    return buildReturnForValidate(false, 'expected response port is invalid');
                }
                if (!isIpVersionValid(testCase.expected.ipVersion)) {
                    return buildReturnForValidate(false, 'expected response ip version is invalid');
                }
                if (!isProtocolValid(testCase.expected.protocol)) {
                    return buildReturnForValidate(false, 'expected response protocol is invalid');
                }
                if (!isNameValid(testCase.testName)) {
                    return buildReturnForValidate(false, 'test case name is invalid');
                }
                if (testCase.parameters.parameter.length === 0) {
                    return buildReturnForValidate(false, 'Parameters are empty');
                }
                angular.forEach(testCase.parameters.parameter, function (parameter, index) {
                    if (!isParameterNameValid(parameter.name)) {
                        return buildReturnForValidate(false, 'test case parameter is invalid');
                    }
                    angular.forEach(parameter.values, function (value) {
                        if (!isParameterValueValid(value)) {
                            return buildReturnForValidate(false, 'test case value ' + value + ' is invalid');
                        }
                    });
                });
                return buildReturnForValidate(true, 'Validation is passed');
            } catch (e) {
                return buildReturnForValidate(false, e);
            }
            return buildReturnForValidate(false, '');
        }

        /**
         * Validates a test case which is already ran
         * @param testCase
         * @returns {*}
         */
        function validateTestResult(testCase) {
            var defer = $q.defer();
            if (!isRuleNameValid(testCase.actual.rule)) {
                defer.reject('actual rule name is invalid');
                return defer.promise;
            }
            if (!isRuleNameValid(testCase.actual.urlRule)) {
                defer.reject('actual URL rule name is invalid');
                return defer.promise;
            }
            if (!isResponseTypeValid(testCase.actual.responseType)) {
                defer.reject('actual response type is invalid');
                return defer.promise;
            }
            if (!isModeValid(testCase.actual.mode)) {
                defer.reject('actual response mode is invalid');
                return defer.promise;
            }
            if (!isFlavorValid(testCase.actual.flavor)) {
                defer.reject('actual response flavor is invalid');
                return defer.promise;
            }
            if (!isStackValid(testCase.actual.xreStack)) {
                defer.reject('actual response stack is invalid');
                return defer.promise;
            }
            if (!isUrnValid(testCase.actual.urn)) {
                defer.reject('actual response urn is invalid');
                return defer.promise;
            }
            if (!isPortValid(testCase.actual.port)) {
                defer.reject('actual response port is invalid');
                return defer.promise;
            }
            if (!isIpVersionValid(testCase.actual.ipVersion)) {
                defer.reject('actual response ip is invalid');
                return defer.promise;
            }
            if (!isProtocolValid(testCase.actual.protocol)) {
                defer.reject('actual response protocol is invalid');
                return defer.promise;
            }
            defer.resolve('Test result is valid');
            return defer.promise;
        }
    }
})();
