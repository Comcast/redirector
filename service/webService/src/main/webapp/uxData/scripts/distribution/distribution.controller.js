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


(function () {
    'use strict';

    angular
        .module('uxData.distribution')
        .controller('DistributionController', DistributionController);

    DistributionController.$inject = ['$scope', '$rootScope', '$log', '$q', '$modal', 'dialogs', 'messageService', 'requestsService', 'utilsService', 'constantsProvider', 'authService', 'USER_PERMISSIONS', 'LOCALSTORAGE_PAGE_NAMES', 'distributionRequestService', 'distributionAlertsService', 'distributionService'];

    function DistributionController($scope, $rootScope, $log, $q, $modal, dialogs, messageService, requests, utils, constants, authService, USER_PERMISSIONS, LOCALSTORAGE_PAGE_NAMES, distributionRequestService, distributionAlertsService, distributionService) {
        /* jshint validthis: true */
        var vm = this;

        vm.inputs = {
            newServer:  {
                generalErrorMsg: '',
                isValid:  false,
                dirty: false
            },
            percent_new : 1,
            saved: true,
            isValidCurrentTotalPercentage: true,
            isValidTotalPercentage: true,
            isSomeCurrentPercentOutOfRange: false,
            isSomeChangedPercentOutOfRange: false
        };

        vm.distribution = {
            displayableRules : [], //rules that will display in the UI
            rulesFromDataSource: [], //rules that were got from data source. Those remain unchanged during rules adding/deletion
            changedRules : [] //rules that will be saved to DS.
        };

        vm.loading = true;
        vm.isSaving = false;
        vm.servicePaths = {};

        vm.allowAddDistributions = true;

        vm.changes = {};

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        var HOST = constants.HOST;

        vm.responseErrorMsg = '';

        // available range is [0.01,...,99.99]
        vm.MAX_TOTAL_Percentage = distributionService.MAX_TOTAL_Percentage;

        // in case when some percent is out of range in distribution rules.

        vm.addItem = addItem;
        vm.saveDistributions = saveDistributions;
        vm.hasDiffChanges = hasDiffChanges;
        vm.calculateAndGetCurrentTotalPercentage = calculateAndGetCurrentTotalPercentage;
        vm.calculateAndGetTotalPercentage = calculateAndGetTotalPercentage;
        vm.isRuleUpdated = isRuleUpdated;
        vm.getChangeType = getChangeType;
        vm.onPercentageChanged = onPercentageChanged;
        vm.onServerChanged = onServerChanged;
        vm.editDistribution = enableDistribution;
        vm.deleteDistribution = deleteDistribution;
        vm.isLoaded = isLoaded;
        vm.exportDistributions = distributionRequestService.exportDistributions;
        vm.returnOnlyNonSelectedPaths = returnOnlyNonSelectedPaths;

        vm.returnOnlyActivePaths = returnOnlyActivePaths;

        vm.defaultServer = {};

        loadAppData();

        function setEmpty() {
            vm.loading = false;
        }

        //GET DISTRIBUTION DATA
        function getDistribution(name) {
            var defer = $q.defer();
            distributionService.getDistributionsWithChanges(name).then(
                function (distributionWithChanges) {
                    angular.extend(vm.changes, distributionWithChanges.changes);
                    var data = distributionWithChanges.distribution;
                    if (angular.isDefined(data) && angular.isDefined(data.rule)) {
                        if (!angular.isArray(data.rule)) {
                            data.rule = [data.rule];
                        }
                        Array.prototype.push.apply(vm.distribution.displayableRules, data.rule);
                        vm.distribution.rulesFromDataSource.splice(0, vm.distribution.rulesFromDataSource.length);
                        vm.distribution.changedRules.splice(0, vm.distribution.changedRules.length);
                        vm.loading = false;

                        if (angular.isDefined(vm.distribution.displayableRules)) {
                            angular.forEach(vm.distribution.displayableRules, function (rule, index) {
                                var changedDistribution = utils.getPendingChangesObjectById(vm.changes, 'distributions', rule.id);
                                if (angular.isDefined(changedDistribution)) {
                                    if (changedDistribution.changeType !== constants.PENDING_CHANGE_TYPE.DELETE) {
                                        rule.percent = changedDistribution.changedExpression.percent;
                                        rule.server.path = changedDistribution.changedExpression.server.path;
                                    }
                                }
                                rule.server.editMode = rule.server.url.indexOf(HOST) > -1 ? 'simple' : 'advanced';
                                vm.distribution.changedRules[index] = angular.copy(rule);
                            });
                            Array.prototype.push.apply(vm.distribution.rulesFromDataSource, vm.distribution.displayableRules);
                            mergeDiffFields();
                        } else {
                            setEmpty();
                        }
                    } else {
                        if (angular.isUndefined(data)) {
                            $log.error('incorrect distributions object was retrieved');
                        } else {
                            if (angular.isUndefined(data.rule)) {
                                $log.info("empty distributions were retrieved");
                            }
                        }
                        mergeDiffFields();
                        vm.loading = false;
                    }
                    defer.resolve();
                },
                function (error) {
                    distributionAlertsService.errorGetItems();
                    setEmpty();
                    defer.reject();
                }
            );
            return defer.promise;
        }


        function mergeDiffFields() {
            angular.forEach(vm.changes.distributions.entry, function (changeEntry, index) {
                var change = changeEntry.value;
                if (change.changeType == constants.PENDING_CHANGE_TYPE.DELETE) {
                    var rule = getRuleById(vm.distribution.displayableRules, parseInt(change.id));
                    if (angular.isDefined(rule.index)) {
                        vm.distribution.displayableRules[rule.index].hasChanges = true;
                        vm.distribution.displayableRules[rule.index].changeType = change.changeType;
                    }
                    var diffRule = getRuleById(vm.distribution.changedRules, parseInt(change.id));
                    if (angular.isDefined(diffRule.index)) {
                        vm.distribution.changedRules.splice(diffRule.index, 1);
                    }
                } else if (change.changeType === constants.PENDING_CHANGE_TYPE.ADD) {
                    var changeObj = change.changedExpression;
                    changeObj.hasChanges = true;
                    changeObj.changeType = change.changeType;
                    vm.distribution.changedRules.push(changeObj);
                    vm.distribution.displayableRules.push(changeObj);
                } else if (angular.isDefined(change.changedExpression)) {
                    var diffRule = getRuleById(vm.distribution.changedRules, parseInt(change.id));
                    if (angular.isDefined(diffRule.index)) {
                        vm.distribution.changedRules[diffRule.index] = change.changedExpression;
                    }
                }
            });
        }

        function updateDiffRule(rule) {
            var diffRule = getRuleById(vm.distribution.changedRules, rule.id);
            if (angular.isDefined(diffRule.index)) {
                vm.distribution.changedRules[diffRule.index] = rule;
            }
        }

        function onServerChanged(index) {
            updateDiffRule(vm.distribution.displayableRules[index]);
        }

        function getDuplicatesStringOfObject(serverObj) {

            var duplicatesIndexes = '';
            angular.forEach(serverObj.duplicates, function (index, value) {
                duplicatesIndexes += (index + 1) + ', ';
            });
            duplicatesIndexes = duplicatesIndexes.substring(0, duplicatesIndexes.lastIndexOf(', '));

            return duplicatesIndexes;
        }

        function returnOnlyNonSelectedPaths(pathToKeep) {
            var returnedPaths = angular.copy(vm.servicePaths);
            angular.forEach(vm.servicePaths.flavor, function(path, index) {
                for (var i = 0; i < vm.distribution.displayableRules.length; i++) {
                    if (path.value === vm.distribution.displayableRules[i].server.path && pathToKeep !== vm.distribution.displayableRules[i].server.path) {
                        returnedPaths.flavor.splice(getFlavorIndexByValue(returnedPaths.flavor, path.value), 1);
                    }
                }

                if (vm.defaultServer.path === path.value) {
                    returnedPaths.flavor.splice(getFlavorIndexByValue(returnedPaths.flavor, path.value), 1);
                }
            });
            vm.allowAddDistributions = returnedPaths.flavor.length !== 1;
            return returnedPaths;
        }

        function returnOnlyActivePaths() {
            var returnedPaths = [];
            angular.forEach(vm.servicePaths.flavor, function(path, index) {
                if (path.nodes > 0) {
                    returnedPaths.push(path.value);
                }
            });
            return returnedPaths;
        }

        function getFlavorIndexByValue (flavors, value) {
            for (var i = 0; i < flavors.length; i++ ){
                if (flavors[i].value === value) {
                    return i;
                }
            }
        }


        //GET ONE SERVICE PATH FOR ALL SERVER DIRECTIVE
        function getServicePaths() {
            var defer = $q.defer();
            requests.getServicePaths($rootScope.currentApplication)
                .then(function (data) {
                    if (angular.isDefined(data) && angular.isArray(data.paths)) {
                        angular.extend(vm.servicePaths, data.paths[0]);
                    }
                    defer.resolve();
                }, function (reason) {
                    distributionAlertsService.errorGetPaths();
                    defer.reject();
                });

            return defer.promise;
        }

        function saveDistributions() {
            if (vm.inputs.newServer.dirty) {
                distributionAlertsService.errorItemNotAdded();
            } else {
                    distributionService.validateAndSaveDistributions(vm.distribution.changedRules, vm.defaultServer)
                        .then(function (status) {
                            distributionAlertsService.successSaved();
                            vm.inputs.saved = true;
                            vm.isSaving = false;
                            vm.distribution.displayableRules.splice(0, vm.distribution.displayableRules.length);
                            clearAll();
                            loadAppData();
                        }, function (error) {
                            if (error.data.message.indexOf('com.comcast.hydra.redirector.data.ExpressionValidationException: ') !== -1) {
                                vm.responseErrorMsg = error.data.replace('com.comcast.hydra.redirector.data.ExpressionValidationException: ', '');
                            }
                            else {
                                vm.responseErrorMsg = error.data.message;
                            }
                            distributionAlertsService.errorItemNotSaved();
                            dialogs.error('Failed to save distributions', error.data.message);
                            vm.isSaving = false;
                        });

            }
        }

        function isLoaded() {
            return vm.loading;
        }

        function calculateAndGetCurrentTotalPercentage() {
            var total = 0;
            var isPercentOutOfRange = false;
            if (angular.isDefined(vm.distribution.rulesFromDataSource)) {
                angular.forEach(vm.distribution.rulesFromDataSource, function (value, key) {
                    if (angular.isUndefined(value.percent)) {
                        isPercentOutOfRange = true;
                    }
                    total += angular.isUndefined(value.percent) ? 0 : value.percent;
                });
            }
            vm.inputs.isValidCurrentTotalPercentage = parseFloat(total) < vm.MAX_TOTAL_Percentage;
            vm.inputs.isSomeCurrentPercentOutOfRange = isPercentOutOfRange;
            return parseFloat(total) || 0;
        }

        function calculateAndGetTotalPercentage() {
            var total = 0;
            var isPercentOutOfRange = false;
            if (angular.isDefined(vm.distribution.changedRules)) {
                angular.forEach(vm.distribution.changedRules, function (value, key) {
                    if (!(value.hasChanges && value.changeType === constants.PENDING_CHANGE_TYPE.DELETE)) {
                        if (angular.isUndefined(value.percent)) {
                            isPercentOutOfRange = true;
                        }
                        total += angular.isUndefined(value.percent) ? 0 : value.percent;
                    }
                });
            }
            vm.inputs.isValidTotalPercentage = parseFloat(total) < vm.MAX_TOTAL_Percentage;
            vm.inputs.isSomeChangedPercentOutOfRange = isPercentOutOfRange;
            return parseFloat(total) || 0;
        }

        function onPercentageChanged(index) {
            updateDiffRule(vm.distribution.displayableRules[index]);
        }

        function addItem() {
            var ruleToAdd = {
                    id: getNewRuleId(),
                    percent: 1,
                    server: {
                    },
                    hasChanges: true,
                    changeType: constants.PENDING_CHANGE_TYPE.ADD
                };
            var modalInstance = $modal.open(
                {
                    templateUrl: '../uxData/scripts/distribution/distribution-edit.html',
                    controller: 'DistributionEdit as vm',
                    windowClass: 'edit-distribution-modal-window',
                    resolve: {
                        'distributionObject': function () {
                            return vm.distribution;
                        },
                        'rule': function () {
                            return {rule : ruleToAdd};
                        },
                        'editMode': function () {
                            return "add";
                        },
                        'paths': function () {
                            return vm.returnOnlyNonSelectedPaths();
                        }
                    }
                }
            );

            modalInstance.result.then(function (rule) {
                vm.inputs.saved = false;
                var newRule = rule.rule;

                vm.distribution.displayableRules.push(newRule);
                vm.distribution.changedRules.push(newRule);
                vm.inputs.percent_new = 1;
                vm.inputs.newServer = angular.copy({});
                setAllowAddDistribution();

            }, function () {
                setAllowAddDistribution();
            });
        }

        function getNewRuleId() {
            var maxId = 0;
            angular.forEach(vm.distribution.rulesFromDataSource, function (rule, key) {
                maxId = Math.max(maxId, rule.id);
            });
            angular.forEach(vm.distribution.changedRules, function (rule, key) {
                maxId = Math.max(maxId, rule.id);
            });
            return (vm.distribution.rulesFromDataSource.length < 1 && vm.distribution.changedRules.length < 1) ? 0 : maxId + 1;
        }

        function enableDistribution(index) {
            vm.inputs.saved = false;
            var displayableRule = getRuleById(vm.distribution.changedRules, vm.distribution.displayableRules[index].id);
            if (angular.isDefined(displayableRule.index)) {

                vm.distribution.displayableRules[index] = vm.distribution.changedRules[displayableRule.index];
                onServerChanged(index);
            }
            var modalInstance = $modal.open(
                {
                    templateUrl: '../uxData/scripts/distribution/distribution-edit.html',
                    controller: 'DistributionEdit as vm',
                    windowClass: 'edit-distribution-modal-window',
                    resolve: {
                        'distributionObject': function () {
                            return vm.distribution;
                        },
                        'rule': function () {
                            return displayableRule;
                        },
                        'editMode': function () {
                            return "edit";
                        },
                        'paths': function () {
                            return vm.returnOnlyNonSelectedPaths(displayableRule.rule.server.path);
                        }
                    }
                }
            );
            modalInstance.result.then(function (isSaved) {
                if (isSaved) {
                    updateDiffRule(vm.distribution.displayableRules[index]);
                } else {
                }
            }, function () {
            });
        }

        function clearAll() {
            vm.distribution.displayableRules.splice(0, vm.distribution.displayableRules.length);
            vm.distribution.rulesFromDataSource.splice(0, vm.distribution.rulesFromDataSource.length);
            vm.distribution.changedRules.splice(0, vm.distribution.changedRules.length);
            vm.inputs.percent_new = 1;
            vm.loading = true;
            vm.isSaving = false;
            vm.servicePaths = angular.copy({});
            vm.inputs.newServer = angular.copy({});
            vm.inputs.newServer.generalErrorMsg = '';
            vm.inputs.newServer.isValid = false;
            vm.inputs.newServer.dirty = false;
            vm.changes = angular.copy({});
            vm.inputs.saved = true;

            vm.inputs.isValidCurrentTotalPercentage = true;
            vm.inputs.isValidTotalPercentage = true;
            vm.responseErrorMsg = '';
            vm.inputs.isSomeCurrentPercentOutOfRange = false;
            vm.inputs.isSomeChangedPercentOutOfRange = false;
        }

        messageService.onChangeApp(angular.extend($scope, vm), function (message) {
            clearAll();
            loadAppData();
        });

        function setAllowAddDistribution () {
            vm.returnOnlyNonSelectedPathsForDefault();
            vm.allowAddDistributions = vm.returnOnlyNonSelectedPaths().flavor.length !== 0;
        }

        function loadAppData() {
            getServicePaths().then(function () {
                getDistribution($rootScope.currentApplication)
                    .then(function () {
                        setAllowAddDistribution();
                    }, function (reason) {
                    });
            });
        }

        function deleteDistribution(index) {
            vm.toDeleteItemId = index;
            var dlg = dialogs.confirm('Delete confirmation', 'Are you sure you want to delete distribution: ' + vm.toDeleteItemId);
            dlg.result.then(function (btn) {
                updateValidationDataOnDeleteServer(vm.toDeleteItemId);
                deleteItemFromChangedRules(vm.distribution.displayableRules[vm.toDeleteItemId].id);
                if (angular.isDefined(getRuleById(vm.distribution.rulesFromDataSource, vm.toDeleteItemId).index)) {
                    vm.distribution.displayableRules[vm.toDeleteItemId].hasChanges = true;
                    vm.distribution.displayableRules[vm.toDeleteItemId].changeType = constants.PENDING_CHANGE_TYPE.DELETE;
                } else {
                    vm.distribution.displayableRules.splice(vm.toDeleteItemId, 1);
                }
                distributionAlertsService.infoWillBeDeleted();

                setAllowAddDistribution();
                vm.inputs.saved = false;
                vm.isSaving = false;
            });
        }

        function updateValidationDataOnDeleteServer(index) {

        }

        function deleteItemFromChangedRules(id) {
            var diffRule = getRuleById(vm.distribution.changedRules, id);
            if (angular.isDefined(diffRule.index)) {
                vm.distribution.changedRules.splice(diffRule.index, 1);
            }
        }

        function hasDiffChanges() {
            return !angular.equals(vm.distribution.rulesFromDataSource, vm.distribution.changedRules);
        }

        function isRuleDeleted(rule) {
            return (rule && rule.hasChanges && rule.changeType === constants.PENDING_CHANGE_TYPE.DELETE);
        }

        function isRuleAdded(rule) {
            return (rule && rule.hasChanges && rule.changeType === constants.PENDING_CHANGE_TYPE.ADD);
        }

        function isRuleUpdated(rule) {
            var diffRule = getRuleById(vm.distribution.changedRules, rule.id);
            var baseRule = getRuleById(vm.distribution.rulesFromDataSource, rule.id);
            return (diffRule.rule && baseRule.rule && !isRuleAdded(diffRule.rule) && !isRuleDeleted(vm.distribution.displayableRules[baseRule.index]) && !angular.equals(diffRule.rule, baseRule.rule));
        }

        function getChangeType(rule) {
            var changeType;
            if (isRuleAdded(rule)) {
                changeType = constants.PENDING_CHANGE_TYPE.ADD;
            } else if (isRuleDeleted(rule)) {
                changeType = constants.PENDING_CHANGE_TYPE.DELETE;
            } else if (isRuleUpdated(rule)) {
                changeType = constants.PENDING_CHANGE_TYPE.UPDATE;
            }
            return changeType;
        }

        function getRuleById(rules, id) {
            var result = {};
            if (angular.isDefined(rules) && rules !== null) {
                for (var i = 0; i < rules.length; i++) {
                    if (rules[i] !== null && angular.equals(rules[i].id, id)) {
                        result = {rule: rules[i], index: i};
                        break;
                    }
                }
            }
            return result;
        }
    }
})();
