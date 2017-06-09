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
 */
(function () {
    'use strict';

    angular
        .module('uxData.distribution')
        .factory('distributionValidationService', DistributionValidationService);

    DistributionValidationService.$inject = ['utilsService'];
    
    function DistributionValidationService(utilsService) {

        var service = {
            validateQueryPair: validateQueryPair,
            checkValidationQueryPairAfterChanges: checkValidationQueryPairAfterChanges,
            removeValidationErrorIfExists: removeValidationErrorIfExists
        };
        return service;

        function validateQueryPair(query) {
            var result = {
                isValid: true,
                validationData: { queryError: []}
            };

            if (angular.isDefined(query) && query != null && angular.isDefined(query.entry) && query.entry != null) {
                angular.forEach(query.entry, function (pair, index) {
                    if (!utilsService.isDefinedAndNotEmpty(pair.key) || !utilsService.isDefinedAndNotEmpty(pair.value)) {
                        result.validationData.queryError[index] =  "Query key/value should not be empty";
                        result.isValid = false;
                    }
                });
            }

            return result;
        }

        function checkValidationQueryPairAfterChanges(query, queryError) {
            var result = {
                queryError: []
            };
            if (angular.isDefined(queryError) && queryError.length > 0) {
                angular.forEach(queryError, function (validationError, index) {
                    if (!utilsService.isDefinedAndNotEmpty(query.entry[index].key)
                        || !utilsService.isDefinedAndNotEmpty(query.entry[index].value)) {
                        result.queryError[index] = validationError;
                    }
                });
            }

            return result;
        }

        function removeValidationErrorIfExists(queryError, index) {
            if (angular.isDefined(queryError) && queryError.length > 0) {
                queryError.splice(index, 1);
            }
            return queryError;
        }
    }
})();
