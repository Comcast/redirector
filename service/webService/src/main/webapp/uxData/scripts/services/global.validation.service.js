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

(function() {
    'use strict';
    angular.module('uxData.services')
        .factory('validationService', ValidationService);


    ValidationService.$inject = ['utilsService', 'REGEXP_CONSTANTS'];

    function ValidationService (utilsService, REGEXP_CONSTANTS) {
        return {
            isUnDefinedOrNullOrHasMiddleSpaces: isUnDefinedOrNullOrHasMiddleSpaces,
            isNullOrUndefined: isNullOrUndefined,
            isNullOrUndefinedOrEmptyString: isNullOrUndefinedOrEmptyString,
            isProtocolValid: isProtocolValid,
            isIpValid: isIpValid,
            isPortValid: isPortValid,
            isUrnValid: isUrnValid,
            isStackValid: isStackValid,
            isFlavorValid: isFlavorValid,
            isRuleNameValid: isRuleNameValid,
            isIpVersionValid: isIpVersionValid
        };

        /**GLOBAL***************/

        /**
         * Validates protocol
         * @param protocol
         * @returns {boolean}
         */
        function isProtocolValid(protocol) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(protocol)) {
                return false;
            }
            if (!protocol.match(REGEXP_CONSTANTS().alphabetical)) {
                return false;
            }
            return true;
        }

        /**
         * Validates ip
         * @param ip
         * @returns {boolean}
         */
        function isIpValid(ip) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(ip)) {
                return false;
            }
            var ipv6Valid = new v6.Address(ip).isValid();
            var ipv4Valid = new v4.Address(ip).isValid();
            if (!(ipv4Valid || ipv6Valid)) {
                return false;
            }
            return true;
        }

        /**
         * Validates ip version
         * @param ip
         * @returns {boolean}
         */
        function isIpVersionValid(ipVersion) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(ipVersion)) {
                return false;
            }
            return (ipVersion === '4' || ipVersion === '6');

        }

        /**
         * Validates port
         * @param port
         * @returns {boolean}
         */
        function isPortValid(port) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(port)) {
                return false;
            }
            if (!port.match(REGEXP_CONSTANTS().numerical)) {
                return false;
            }
            return true;
        }

        /**
         * Validates urn
         * @param urn
         * @returns {boolean}
         */
        function isUrnValid(urn) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(urn)) {
                return false;
            }
            if(!urn.match(REGEXP_CONSTANTS().alphaNumericalWithUnderscores)) {
                return false;
            }
            return true;
        }

        /**
         * Validates stack
         * @param stack
         * @returns {boolean}
         */
        function isStackValid(stack) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(stack)) {
                return false;
            }
            if (!stack.match(REGEXP_CONSTANTS().stackName)) {
                return false;
            }
            return true;
        }

        /**
         * Validates flavor
         * @param flavor
         * @returns {boolean}
         */
        function isFlavorValid(flavor) {
            if (isUnDefinedOrNullOrHasMiddleSpaces(flavor)) {
                return false;
            }
            else return true;
        }

        /**
         * Validates rule name
         * @param name
         * @returns {boolean}
         */
        function isRuleNameValid(name) {
            if (isNullOrUndefined(name)) {
                return false;
            }
            if (!name.match(REGEXP_CONSTANTS().alphaNumericalWithUnderscoresAndSpaces)) {
                return false;
            }
            return true;
        }

        function isNullOrUndefined(value) {
            if (value == null || angular.isUndefined(value)) {
                return true;
            }
            return false;
        }

        function isNullOrUndefinedOrEmptyString (value) {
            return isNullOrUndefined(value) || utilsService.isEmptyString(value);
        }

        /**
         * Is value:
         * 1. Undefined or null
         * 2. Is an empty string
         * 3. Has middle whitespaces
         * @param value
         * @returns {*}
         */
        function isUnDefinedOrNullOrHasMiddleSpaces (value) {
            return isNullOrUndefinedOrEmptyString(value) || (value.indexOf(' ') !== -1);
        }

    }
})();
