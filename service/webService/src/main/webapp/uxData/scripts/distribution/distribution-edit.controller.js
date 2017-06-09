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
        .controller('DistributionEdit', DistributionEdit);
    
    DistributionEdit.$inject = ['$scope', '$modalInstance', '$log', '$filter', 'toastr', 'ngTableParams','authService', 'USER_PERMISSIONS', 'distributionAlertsService', 'utilsService', 'rule', 'editMode', 'distributionObject', 'paths', 'SERVER_CONSTANTS', 'distributionValidationService'];

    function DistributionEdit($scope, $modalInstance, $log, $filter, toastr, ngTableParams, authService, USER_PERMISSIONS, distributionAlertsService, utilsService, rule, editMode, distributionObject, paths, CONST, distributionValidationService) {
        /* jshint validthis: true */

        var vm = this;
        
        var EMPTY_QUERY = {"entry" : []};

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        angular.extend(vm, {'originalRule': rule});
        angular.extend(vm, {'originalDistributionObject': distributionObject});
        angular.extend(vm, {'originalEditMode': editMode});
        angular.extend(vm, {'paths': paths});

        vm.title = (vm.editMode === 'edit') ? 'Edit distribution' : 'Add new distribution';
        vm.percent = '';
        vm.path = '';

        vm.removeElements = false;
        vm.saveDistribution = saveDistribution;
        vm.hasError = utilsService.isDefinedAndNotEmpty;

        vm.addNewQueryPair = addNewQueryPair;
        vm.removeQueryPair = removeQueryPair;
        vm.queryChanged = queryChanged;
        vm.validationData = {};

        init();

        function init() {
            vm.rule = angular.copy(vm.originalRule);
            vm.distributionObject = angular.copy(vm.originalDistributionObject);
            vm.editMode = angular.copy(vm.originalEditMode);
            vm.query = utilsService.isEmptyObject(vm.rule.rule.server.query) ? EMPTY_QUERY : vm.rule.rule.server.query;
        }

        function saveDistribution() {
            var validationResult = distributionValidationService.validateQueryPair(vm.query);
            if (validationResult.isValid) {
                vm.rule.rule.server.query = removeDuplicateFromQuery();
                if (editMode === "add") {
                    if (angular.isUndefined(vm.rule.rule.server.path)) {
                        distributionAlertsService.errorPathNotSelected();
                    } else {
                        $modalInstance.close(vm.rule);
                        vm.modify = false;
                    }
                } else {
                    $.extend(true, vm.originalRule, vm.rule);
                    if (vm.removeElements) {
                        vm.originalRule.rule.server.query = vm.rule.rule.server.query;
                        vm.removeElements = false;
                    }
                    $modalInstance.close(true);
                }
            } else {
                vm.validationData = validationResult.validationData;
            }
        }

        function addNewQueryPair() {
            createEmptyQueryIfNull();
            vm.query.entry.push({"key" : "", "value" : ""});
            queryChanged();
        }

        function queryChanged() {
            vm.rule.rule.server.query = vm.query;
            if (angular.isDefined(vm.validationData.queryError) && vm.validationData.queryError.length > 0) {
                vm.validationData = distributionValidationService.checkValidationQueryPairAfterChanges(vm.query, vm.validationData.queryError);
            }
        }

        function removeQueryPair(index) {
            vm.query.entry.splice(index, 1);
            nullifyEmptyQuery();
            vm.validationData.queryError = distributionValidationService.removeValidationErrorIfExists(vm.validationData.queryError, index);
            queryChanged();
            vm.removeElements = true;
        }

        function createEmptyQueryIfNull() {
            if (utilsService.isEmptyObject(vm.query)) {
                vm.query = EMPTY_QUERY;
            }
        }

        function nullifyEmptyQuery() {
            if (!utilsService.isEmptyObject(vm.query)
                && (utilsService.isEmptyObject(vm.query.entry) || vm.query.entry.length == 0)) {
                vm.query = null;
            }
        }

        function removeDuplicateFromQuery() {
            var result =  {"entry" : []};
            var uniq = true;
            if (angular.isDefined(vm.query) && vm.query != null && angular.isDefined(vm.query.entry) && vm.query.entry != null) {
                angular.forEach(vm.query.entry, function (pair, index) {
                    uniq = true;
                    for( var i = 0; i < result.entry.length; i++ ) {
                        if (result.entry[i].key == pair.key && result.entry[i].value == pair.value) {
                            uniq = false;
                            break;
                        }
                    }
                    if (uniq) {
                        vm.removeElements = true;
                        result.entry.push(pair);
                    }
                });
            } else {
                result = null;
            }
            return result;
        }
    }
})();
