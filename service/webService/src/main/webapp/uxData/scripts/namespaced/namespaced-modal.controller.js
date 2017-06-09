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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */


/* global sinon, describe, it, afterEach, beforeEach, expect, inject */

(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedMerger', NamespacedMerger);

    NamespacedMerger.$inject = ['$log', '$q', '$modalInstance', 'namespacedService', 'namespacedAlertsService', 'namespaceCandidates', 'namespacedDuplicateMap', 'isImporting'];
    function NamespacedMerger($log, $q, $modalInstance, namespacedService, namespacedAlertsService, namespaceCandidates, namespacedDuplicateMap, isImporting) {
        /* jshint validthis: true */
        var vm = this;
        angular.extend(vm, {'namespaceCandidates': namespaceCandidates});
        angular.extend(vm, {'namespacedDuplicateMap': namespacedDuplicateMap});
        angular.extend(vm, {'isImporting': isImporting});

        vm.delFromExistent = delFromExistent;
        vm.delFromCandidate = delFromCandidate;
        vm.continueFromDuplicatesModal = continueFromDuplicatesModal;
        vm.duplicateMapEmpty = vm.namespacedDuplicateMap.length === 0;

        var valuesToDeleteByNamespacedName = [];

        function delFromExistent(name, value) {
            var index = getIndexForName(name, valuesToDeleteByNamespacedName);
            if (index >= 0) {
                valuesToDeleteByNamespacedName[index].valuesToDelete.push(value);
            } else {
                valuesToDeleteByNamespacedName.push({name: name, valuesToDelete: [value]});
            }

            var index = namespacedService.getIndexOfDuplicateItemFromMapByKey(vm.namespacedDuplicateMap, value);
            vm.namespacedDuplicateMap.splice(index, 1);
            vm.duplicateMapEmpty = vm.namespacedDuplicateMap.length === 0;
            $log.info('Pending to delete item \"' + value + '\" from existent namespaced list: ' + name);
        }

        function deleteItemsFromDS(valuesByNamespacedNameList) {
            var defer = $q.defer();
            if (valuesByNamespacedNameList.length > 0) {
                namespacedService.deleteEntitiesFromNamespacedLists(valuesByNamespacedNameList).then(
                    function (result) {
                        $log.info('Deleted :', valuesByNamespacedNameList);
                        defer.resolve();
                    },
                    function (reason) {
                        $log.error('Failed: ' + reason.data.message);
                        namespacedAlertsService.errorDelete();
                        defer.reject();
                    });
            } else {
                defer.resolve();
            }
            return defer.promise;
        }


        function delFromCandidate(name, value) {
            vm.namespaceCandidates.valueSet.splice(namespacedService.getValuePosition(vm.namespaceCandidates.valueSet,
                angular.isObject(value) ? value: getProperValueByListType(vm.namespaceCandidates.type, value)), 1);
            var index = namespacedService.getIndexOfDuplicateItemFromMapByKey(vm.namespacedDuplicateMap, value);
            if (index !== null) {
                vm.namespacedDuplicateMap.splice(index, 1);
                vm.duplicateMapEmpty = vm.namespacedDuplicateMap.length === 0;
                $log.info('Deleted item \"' + value + '\" from imported namespaced list (\'' + name + '\')');
            } else {
                $log.error('Can\'t find item: \"' + value + '\", aborted!');
            }
        }

        function continueFromDuplicatesModal() {
            deleteItemsFromDS(valuesToDeleteByNamespacedName).then(function () {
                saveNamespaceAfterMerge().then(function() {
                        $modalInstance.close();
                    }
                );
            }, function () {
            });
        }

        function getProperValueByListType (listType, value) {
            return {value: value};
        }

        function saveNamespaceAfterMerge() {
            var defer = $q.defer();
            if (angular.isUndefined(vm.namespaceCandidates) || vm.namespacedDuplicateMap.length > 0) {
                return;
            }

            if (angular.isDefined(vm.namespaceCandidates.value) && (vm.namespaceCandidates.value.length === 0)) {
                delete (vm.namespaceCandidates.value);
            }

            var name = vm.namespaceCandidates.name;
            namespacedService.validateAndSave(vm.namespaceCandidates, name, isImporting).then(function () {
                $log.info('Added namespaced list: \"' + name + '\"');
                namespacedAlertsService.success();
                vm.namespaceCandidates = {}; //clear
                defer.resolve();
            }, function (reason) {
                $log.error('Failed: ' + reason.message);
                namespacedAlertsService.errorPostWithMessage(reason.message);
                defer.reject();
            });
            return defer.promise;
        }

        function getIndexForName (name, valuesByNames) {
            for (var i = 0; i < valuesByNames.length; i ++) {
                if (angular.equals(name, valuesByNames[i].name)) {
                    return i;
                }
            }

            return -1;
        }
    }
})();
