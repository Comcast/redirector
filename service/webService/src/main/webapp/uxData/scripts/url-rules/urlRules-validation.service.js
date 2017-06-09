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
        .module('uxData.urlRules')
        .factory('urlRulesValidationService', urlRulesValidationService);

    urlRulesValidationService.$inject = ['utilsService', 'REGEXP_CONSTANTS'];

    function urlRulesValidationService(utils, regexp) {


        var service = {
            validateReturnValue: validateUrlParamsAndFillInTheResult,
            validateDefaultReturnValue: validateDefaultUrlParamsAndFillInTheResult
        };

        return service;

        function validateUrlParamsAndFillInTheResult(vm) {
            var validationResult = validateReturnUrlParams(vm.uiModelHolder.urlParams);
            angular.copy(validationResult.validationData, vm.urlParamsValidationData);
            return validationResult.isValid;
        }

        function validateDefaultUrlParamsAndFillInTheResult(vm) {
            var validationResult = validateDefaultUrlParams(vm.defaultUrlParams);
            angular.copy(validationResult.validationData, vm.defaultUrlParamsValidationData);
            return validationResult.isValid;
        }

        function validateReturnUrlParams(urlParams) {

            var result = {
                isValid: true,
                validationData: { generalErrorMsg: '', ipProtocolVersionErrorMsg: '', urnErrorMsg: '', portErrorMsg: '', protocolErrorMsg: ''}
            };

            // 1. validate that at least one param is present
            if (allParamsAreEmpty(urlParams)) {
                result.isValid = false;
                result.validationData.generalErrorMsg = 'At least one server url parameter should be added.';
            }
            // 2. validate each of params
            validateUrlParams(urlParams, result);

            return result;
        }

        function validateDefaultUrlParams(urlParams) {

            var result = {
                isValid: true,
                validationData: { generalErrorMsg: '', ipProtocolVersionErrorMsg: '', urnErrorMsg: '', portErrorMsg: '', protocolErrorMsg: ''}
            };

            // 1. validate that ALL fields are filled in
            if (hasEmptyParam(urlParams)) {
                result.isValid = false;
                result.validationData.generalErrorMsg = 'All Default URL Parameters should be filled in.';
            }
            // 2. validate each of params
            validateUrlParams(urlParams, result);

            return result;
        }

        function validateUrlParams(urlParams, result) {

            var protocolValidationResult = validateProtocol(urlParams.protocol);
            if (!utils.isEmptyString(urlParams.protocol) && !protocolValidationResult.isValid) {
                result.isValid = false;
                result.validationData.protocolErrorMsg = protocolValidationResult.error;
            }

            var portValidationResult = validatePort(urlParams.port);
            if (!utils.isEmptyString(urlParams.port) && !portValidationResult.isValid) {
                result.isValid = false;
                result.validationData.portErrorMsg = portValidationResult.error;
            }

            var urnValidationResult = validateUrn(urlParams.urn);
            if (!utils.isEmptyString(urlParams.urn) && !urnValidationResult.isValid) {
                result.isValid = false;
                result.validationData.urnErrorMsg = urnValidationResult.error;
            }

            var ipValidationResult = validateIPProtocolVersion(urlParams.ipProtocolVersion);
            if (!utils.isEmptyString(urlParams.ipProtocolVersion) && !ipValidationResult.isValid) {
                result.isValid = false;
                result.validationData.ipProtocolVersionErrorMsg = ipValidationResult.error;
            }

            return result;
        }

        function validateProtocol(protocol) {

            var result = {isValid: true, error: ''};

            var protocolIsEmpty = !protocol || utils.isEmptyString(protocol);

            if (protocolIsEmpty) {
                result.error = 'Protocol is required';
                result.isValid = false;
            }
            else if (!protocolIsEmpty && !regexp().nonEmptyAlphabetical.test(protocol)) {
                result.error = 'Protocol is invalid. Only word characters are allowed.';
                result.isValid = false;
            }

            return result;
        }

        function validatePort(port) {

            var result = {isValid: true, error: ''};

            var portIsEmpty = !port || utils.isEmptyString(utils.getString(port));

            if (portIsEmpty) {
                result.error = 'Port is required';
                result.isValid = false;
            }
            else if (!portIsEmpty && (!regexp().portPattern.test(utils.getString(port)) || port < 1 || port > 65535)) {
                result.error = 'Port is invalid. Must be a value between 1 and 65535.';
                result.isValid = false;
            }

            return result;
        }

        function validateUrn(urn) {

            var result = {isValid: true, error: ''};

            var urnIsEmpty = !urn || utils.isEmptyString(urn);
            if (urnIsEmpty) {
                result.error = 'Urn is required';
                result.isValid = false;
            }
            else if (!urnIsEmpty && !regexp().urnPattern.test(urn)) {
                result.error = 'Urn is invalid. Allowed symbols are: alphanumeric and - _ ; .';
                result.isValid = false;
            }

            return result;
        }

        function validateIPProtocolVersion(ipProtocolVersion) {

            var result = {isValid: true, error: ''};

            var ipProtocolVersionIsEmpty = utils.isEmptyString(utils.getString(ipProtocolVersion));
            if (ipProtocolVersionIsEmpty) {
                result.error = 'IP protocol version is required';
                result.isValid = false;
            }
            else if (!ipProtocolVersionIsEmpty && utils.getString(ipProtocolVersion) !== '4' && utils.getString(ipProtocolVersion) !== '6') {
                result.error = 'IP protocol version is invalid. Allowed \'IPv4\' or \'IPv6\'';
                result.isValid = false;
            }

            return result;
        }

        function hasEmptyParam(urlParams) {
            return utils.isEmptyString(urlParams.protocol) ||
                !utils.isDefinedAndNotEmpty(urlParams.port) ||
                utils.isEmptyString(urlParams.urn) ||
                !utils.isDefinedAndNotEmpty(urlParams.ipProtocolVersion);
        }

        function allParamsAreEmpty(urlParams) {
            return utils.isEmptyString(urlParams.protocol) &&
                !utils.isDefinedAndNotEmpty(urlParams.port) &&
                 utils.isEmptyString(urlParams.urn) &&
                !utils.isDefinedAndNotEmpty(urlParams.ipProtocolVersion);
        }
    }
})();
