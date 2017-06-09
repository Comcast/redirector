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
        .module('uxData')
        .controller('Changes', Changes);

    Changes.$inject = ['$log', '$scope', '$window','$state', 'toastr', 'dialogs', 'changesRequestsService', 'authService', 'USER_PERMISSIONS', 'changesService', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'STATES_CONSTANTS'];

    function Changes($log, $scope, $window, $state, toastr, dialogs, changesRequestsService, authService, USER_PERMISSIONS, changesService, messageService, LOCALSTORAGE_PAGE_NAMES, STATES_CONSTANTS) {
        var vm = this;
        vm.changes = {};
        vm.version = 0;
        vm.parseInt = parseInt;
        vm.currentDistribution = {};
        vm.pendingDistribution = {};
        vm.whitelistChangedDiff = '';
        vm.whitelistCurrentDiff = '';

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;


        vm.ITEM_TYPES = {
            RULE: 'RULE',
            TEMPLATE_RULE: 'TEMPLATE_RULE',
            URLRULE: 'URLRULE',
            TEMPLATE_URLRULE: 'TEMPLATE_URLRULE',
            DISTRIBUTION: 'DISTRIBUTION',
            SERVER: 'SERVER',
            WHITELISTED: 'WHITELISTED'
        };


        var itemTypesToLists = {};
        itemTypesToLists[vm.ITEM_TYPES.RULE] = 'pathRules';
        itemTypesToLists[vm.ITEM_TYPES.TEMPLATE_RULE] = 'templatePathRules';
        itemTypesToLists[vm.ITEM_TYPES.URLRULE] = 'urlRules';
        itemTypesToLists[vm.ITEM_TYPES.TEMPLATE_URLRULE] = 'templateUrlPathRules';
        itemTypesToLists[vm.ITEM_TYPES.DISTRIBUTION] = 'distributions';
        itemTypesToLists[vm.ITEM_TYPES.SERVER] = 'servers';
        itemTypesToLists[vm.ITEM_TYPES.WHITELISTED] = 'whitelisted';

        vm.cancelAllPendingChanges = cancelAllPendingChanges;
        vm.toggleItem = toggleItem;

        vm.exportAllPending = exportAllPending;
        vm.exportPending = exportPending;
        vm.exportPendingStackManagement = exportPendingStackManagement;

        //rule
        vm.approvePendingRule = approvePendingRule;
        vm.cancelPendingRule = cancelPendingRule;

       //templateRule
        vm.approveTemplatePendingRule = approveTemplatePendingRule;
        vm.cancelTemplatePendingRule = cancelTemplatePendingRule;


        //templateUrlRule
        vm.approveTemplatePendingUrlRule = approveTemplatePendingUrlRule;
        vm.cancelTemplatePendingUrlRule = cancelTemplatePendingUrlRule;

        //urlRule
        vm.approvePendingUrlRule = approvePendingUrlRule;
        vm.cancelPendingUrlRule = cancelPendingUrlRule;
        vm.approvePendingUrlParams = approvePendingUrlParams;
        vm.cancelPendingUrlParams = cancelPendingUrlParams;
        vm.getUrlParamsViewText = changesService.getUrlParamsViewText;

        //distribution
        vm.approveAllPendingDistributionChanges = approveAllPendingDistributionChanges;
        vm.cancelAllPendingDistribution = cancelAllPendingDistribution;
        vm.exportPendingDistributions = exportPendingDistributions;
        vm.getDistributionViewText = getDistributionViewText;

        //server
        vm.approvePendingServer = approvePendingServer;
        vm.cancelPendingServer = cancelPendingServer;

        //whitelisted
        vm.approvePendingWhitelisted = approvePendingWhitelisted;
        vm.validateAndApprovePendingWhitelisted = validateAndApprovePendingWhitelisted;
        vm.cancelPendingWhitelisted = cancelPendingWhitelisted;

        //approve,cancel,reload
        vm.confirmApprovePendingChanges = confirmApprovePendingChanges;
        vm.confirmValidateAndApprovePendingChanges = confirmValidateAndApprovePendingChanges;
        vm.confirmCancelPendingChanges = confirmCancelPendingChanges;
        vm.triggerModelReload = triggerModelReload;
        vm.triggerStacksReload = triggerStacksReload;
        vm.downloadCoreBackup = downloadCoreBackup;


        init();

        function checkForConflict(status, data) {
            if (status === 409) {
                var dlg = dialogs.notify('Pending changes are out of date', 'While you are reviewing the changes someone made more changes so you need to reload the page and review again before approving or canceling any change.');
                dlg.result.then(function (btn) {
                    $window.location.reload();
                }, function () {

                });
                return true;
            }
            if (status === 406) {
                var confirmDialog = dialogs.confirm('Please Confirm', 'Approve impossible because settings for redirector config are not exist. ' +
                    'If you have access to  "Settings" page, please set settings for redirector config or contact your administrator. ' +
                    'Do you want to go to "Settings" page?');
                confirmDialog.result.then(function (btn) {
                    goToSettingsPage();
                }, function () {

                });
                return true;
            }
            toastr.error('Action failed due to server error: ' + data.message, 'Error', {closeButton: true, timeOut: 20000});
            return false;
        }

        function checkForWarning(data) {
            if (angular.isDefined(data) && data !== '') {
                toastr.warning(data.message, 'Warning', {closeButton: true, timeOut: 12000});
            }
        }

        function goToSettingsPage() {
            if (!vm.authService.isAuthorized(vm.USER_PERMISSIONS().writeSettings)) {
                toastr.error('You do not have permission to access on "Settings" page.' , 'Error', {closeButton: true, timeOut: 20000});
                return;
            }
            $state.go(STATES_CONSTANTS().settings);
        }

        messageService.onChangeApp($scope, function (message) {
            init();
        });

        /* RULE PAGE */
        function cancelPendingRule(ruleId) {
            changesRequestsService.cancelPendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingRule(ruleId) {
            changesRequestsService.approvePendingRule(ruleId, vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/

        /* TEMPLATE RULE PAGE */
        function cancelTemplatePendingRule(ruleId) {
            changesRequestsService.cancelTemplatePendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveTemplatePendingRule(ruleId) {
            changesRequestsService.approveTemplatePendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }
        /************************************************/

        /* TEMPLATE URL RULE PAGE */
        function cancelTemplatePendingUrlRule(ruleId) {
            changesRequestsService.cancelTemplatePendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveTemplatePendingUrlRule(ruleId) {
            changesRequestsService.approveTemplatePendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }
        /************************************************/

        /* URL RULE PAGE */
        function approvePendingUrlRule(ruleId) {
            changesRequestsService.approvePendingUrlRule(ruleId, vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelPendingUrlRule(ruleId) {
            changesRequestsService.cancelPendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingUrlParams() {
            changesRequestsService.approvePendingUrlParams(vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelPendingUrlParams() {
            changesRequestsService.cancelPendingUrlParams(vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/


        /* DEFAULT SERVER */
        function cancelPendingServer(serverName) {
            changesRequestsService.cancelPendingServer(serverName, vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingServer(serverName) {
            changesRequestsService.approvePendingServer(serverName, vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/

        /* STACK MANAGEMENT */
        function cancelPendingWhitelisted() {
            changesRequestsService.cancelPendingStackManagement(vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingWhitelisted() {
            changesRequestsService.approvePendingStackManagement(vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function validateAndApprovePendingWhitelisted() {
            changesRequestsService.validateAndApprovePendingStackManagement(vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/


        function confirmApprovePendingChanges() {
            var dlg = dialogs.confirm('Approve changes confirmation', 'Are you sure you want to approve all the changes in the list ?');
            dlg.result.then(function (btn) {
                changesRequestsService.approvePendingChanges(vm.changes.version)
                    .then(function (data) {
                        init();
                    }, function (err) {
                        checkForConflict(err.status, err.data);
                    });
            }, function () {
                //confirmCancelPendingChanges();
            });
        }

        function confirmValidateAndApprovePendingChanges() {
            var dlg = dialogs.confirm('Approve changes confirmation', 'Are you sure you want to approve all the changes in the list ?');
            dlg.result.then(function (btn) {
                changesRequestsService.validateAndApprovePendingChanges(vm.changes.version)
                    .then(function (data) {
                        checkForWarning(data);
                        init();
                    }, function (err) {
                        checkForConflict(err.status, err.data);
                    });
            }, function () {
            });
        }

        function triggerModelReload() {
            var dlg = dialogs.confirm('Model reload confirmation', 'Are you sure you want to reload current model on XRE Redirector? ');
            dlg.result.then(function (btn) {
                changesRequestsService.triggerModelReload()
                    .then(function (status) {
                        toastr.success('Model reload triggered', 'Success', {closeButton: true, timeOut: 3000});
                    }, function (err) {
                        toastr.error('Action failed due to server error:' + err.data.message, 'Error', {closeButton: true, timeOut: 3000});
                    });
            }, function () {

            });
        }

        function triggerStacksReload() {
            var dlg = dialogs.confirm('Stacks reload confirmation', 'Are you sure you want to reload stacks on XRE Redirector? ');
            dlg.result.then(function (btn) {
                changesRequestsService.triggerStacksReload()
                    .then(function (status) {
                        toastr.success('Stacks reload triggered', 'Success', {closeButton: true, timeOut: 3000});
                    }, function (err) {
                        toastr.error('Action failed due to server error:' + err.data.message, 'Error', {closeButton: true, timeOut: 3000});
                    });
            }, function () {

            });
        }


        function confirmCancelPendingChanges() {
            var dlg = dialogs.confirm('Approve changes confirmation', 'Are you sure you want to <b>cancel</b> all the changes in the list ?');
            dlg.result.then(function (btn) {
                cancelAllPendingChanges();
            }, function () {

            });
        }


        function cancelAllPendingChanges() {
            changesRequestsService.cancelAllPendingChanges(vm.changes.version)
                .then(function (data) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelAllPendingDistribution() {
            changesRequestsService.cancelAllPendingDistribution(vm.changes.version)
                .then(function (status) {
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveAllPendingDistributionChanges() {
            changesRequestsService.approveAllPendingDistribution(vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    init();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function toggleItem(ruleId, itemType) {
            var list = vm.changes[itemTypesToLists[itemType]];
            list.entry.forEach(function (element) {
                var item = element;
                if (item.key === ruleId) {
                    item.value['open'] = !item.value['open'];
                }
            });
        }

        function getDistributionViewText(distribution) {
            if (angular.isDefined(distribution)) {
                return distribution.percent + '% - ' + distribution.server.path;
            }
            return '';
        }

        function downloadCoreBackup() {
            changesRequestsService.downloadCoreBackup();
        }

        function exportAllPending() {
            changesRequestsService.exportAllPending();
        }

        function exportPending(ruleType, changeId) {
            changesRequestsService.exportPending(ruleType, changeId);
        }

        function exportPendingDistributions() {
            changesRequestsService.exportPendingDistributions();
        }

        function exportPendingStackManagement() {
            changesRequestsService.exportPendingStackManagement();
        }

        function init() {
            changesService.loadPendingChanges().then(function (pendingData) {
                vm.pendingDistribution = angular.copy(pendingData.pendingDistribution);
                vm.currentDistribution = angular.copy(pendingData.currentDistribution);
                vm.changes = angular.copy(pendingData.pending);

                vm.changes.pathRules = changesService.generatePathRules(vm.changes.pathRules);
                vm.changes.templatePathRules = changesService.generatePathRules(vm.changes.templatePathRules);
                vm.changes.urlRules = changesService.generateUrlRules(vm.changes.urlRules);
                vm.changes.templateUrlPathRules = changesService.generateUrlRules(vm.changes.templateUrlPathRules);

                var whitelistedData = changesService.generateWhitelistedData(vm.changes.whitelisted, pendingData.whitelisted);
                vm.whitelistChangedDiff = whitelistedData.whitelistChangedDiff;
                vm.whitelistCurrentDiff = whitelistedData.whitelistCurrentDiff;
            }, function (reason) {
                $log.error(reason);
            });
        }
    }
})();
