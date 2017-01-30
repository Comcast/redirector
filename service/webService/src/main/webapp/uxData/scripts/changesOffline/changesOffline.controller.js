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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */



(function () {
    'use strict';

    angular
        .module('uxData')
        .controller('changesOffline', changesOffline);

    changesOffline.$inject = ['$log', '$scope', '$window', '$state', 'toastr', 'dialogs', 'changesOfflineRequestsService', 'changesRequestsService', 'authService', 'USER_PERMISSIONS', 'changesOfflineService', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'STATES_CONSTANTS'];

    function changesOffline($log, $scope, $window, $state, toastr, dialogs, changesOfflineRequestsService, changesRequestsService, authService, USER_PERMISSIONS, changesOfflineService, messageService, LOCALSTORAGE_PAGE_NAMES, STATES_CONSTANTS) {
        var vm = this;
        vm.changes = {};
        vm.version = 0;
        vm.parseInt = parseInt;
        vm.currentDistribution = {};
        vm.pendingDistribution = {};
        vm.whitelistChangedDiff = '';
        vm.whitelistCurrentDiff = '';

        vm.namespacesChanges = {};

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;


        vm.ITEM_TYPES = {
            RULE: 'RULE',
            TEMPLATE_RULE: 'TEMPLATE_RULE',
            URLRULE: 'URLRULE',
            TEMPLATE_URLRULE: 'TEMPLATE_URLRULE',
            DISTRIBUTION: 'DISTRIBUTION',
            SERVER: 'SERVER',
            WHITELISTED: 'WHITELISTED',
            NAMESPACES: 'NAMESPACES'
        };


        Dropzone.options.dzform = {
            url: "/default",
            paramName: "file",
            dictDefaultMessage: "Click to select XRE backup archive.",
            maxFiles: 1,
            init: function() {
                this.on("processing", function(file) {
                    this.options.url = changesOfflineRequestsService.getCoreBackupUploadUrl();
                });
                this.on("success", function(file, pendingChanges){
                    init(pendingChanges);
                });
                this.on("error", function(error, errorMsg) {
                    if (angular.isDefined(error.accepted) && error.accepted) {
                        dialogs.error('Failed to import XRE backup changes', errorMsg);
                    }
                });
            },
            maxfilesexceeded: function(file) {
                this.removeAllFiles();
                this.addFile(file);
            }

        };

        var myDropzone = new Dropzone("#dzform");

        var itemTypesToLists = {};
        itemTypesToLists[vm.ITEM_TYPES.RULE] = 'pathRules';
        itemTypesToLists[vm.ITEM_TYPES.TEMPLATE_RULE] = 'templatePathRules';
        itemTypesToLists[vm.ITEM_TYPES.URLRULE] = 'urlRules';
        itemTypesToLists[vm.ITEM_TYPES.TEMPLATE_URLRULE] = 'templateUrlPathRules';
        itemTypesToLists[vm.ITEM_TYPES.DISTRIBUTION] = 'distributions';
        itemTypesToLists[vm.ITEM_TYPES.SERVER] = 'servers';
        itemTypesToLists[vm.ITEM_TYPES.WHITELISTED] = 'whitelisted';
        itemTypesToLists[vm.ITEM_TYPES.NAMESPACES] = 'namespaces';

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
        vm.getUrlParamsViewText = changesOfflineService.getUrlParamsViewText;

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
        vm.cancelPendingWhitelisted = cancelPendingWhitelisted;
        vm.validateAndApprovePendingWhitelisted = validateAndApprovePendingWhitelisted;

        //approve,cancel,reload
        vm.confirmApprovePendingChanges = confirmApprovePendingChanges;
        vm.confirmValidateAndApprovePendingChanges = confirmValidateAndApprovePendingChanges;
        vm.confirmCancelPendingChanges = confirmCancelPendingChanges;
        vm.triggerModelReload = triggerModelReload;
        vm.downloadCoreBackup = downloadCoreBackup;

        //namespaces
        vm.approveAllNamespacesChanges = approveAllNamespacesChanges;
        vm.cancelAllNamespacesChanges = cancelAllNamespacesChanges;
        vm.approveNamespacesChanges = approveNamespacesChanges;
        vm.cancelNamespacesChanges = cancelNamespacesChanges;

        reload();

        function checkForConflict(status, message) {
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
            toastr.error('Action failed due to server error: ' + message, 'Error', {closeButton: true, timeOut: 20000});
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
            reload();
        });

        /* RULE PAGE */
        function cancelPendingRule(ruleId) {
            changesOfflineRequestsService.cancelPendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingRule(ruleId) {
            changesOfflineRequestsService.approvePendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/

        /* TEMPLATE RULE PAGE */
        function cancelTemplatePendingRule(ruleId) {
            changesOfflineRequestsService.cancelTemplatePendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveTemplatePendingRule(ruleId) {
            changesOfflineRequestsService.approveTemplatePendingRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }
        /************************************************/

        /* TEMPLATE URL RULE PAGE */
        function cancelTemplatePendingUrlRule(ruleId) {
            changesOfflineRequestsService.cancelTemplatePendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveTemplatePendingUrlRule(ruleId) {
            changesOfflineRequestsService.approveTemplatePendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }
        /************************************************/

        /* URL RULE PAGE */
        function approvePendingUrlRule(ruleId) {
            changesOfflineRequestsService.approvePendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelPendingUrlRule(ruleId) {
            changesOfflineRequestsService.cancelPendingUrlRule(ruleId, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingUrlParams() {
            changesOfflineRequestsService.approvePendingUrlParams(vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelPendingUrlParams() {
            changesOfflineRequestsService.cancelPendingUrlParams(vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/


        /* DEFAULT SERVER */
        function cancelPendingServer(serverName) {
            changesOfflineRequestsService.cancelPendingServer(serverName, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingServer(serverName) {
            changesOfflineRequestsService.approvePendingServer(serverName, vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/

        /* STACK MANAGEMENT */
        function cancelPendingWhitelisted() {
            changesOfflineRequestsService.cancelPendingStackManagement(vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approvePendingWhitelisted() {
            changesOfflineRequestsService.approvePendingStackManagement(vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function validateAndApprovePendingWhitelisted() {
            changesOfflineRequestsService.validateAndApprovePendingStackManagement(vm.changes.version)
                .then(function (data) {
                    checkForWarning(data);
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        /************************************************/


        function confirmApprovePendingChanges() {
            var dlg = dialogs.confirm('Approve changes confirmation', 'Are you sure you want to approve all the changes in the list ?');
            dlg.result.then(function (btn) {
                changesOfflineRequestsService.approvePendingChanges(vm.changes.version)
                    .then(function (data) {
                        reload();
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
                changesOfflineRequestsService.hasPendingChanges()
                    .then(function (result) {
                        if (result == "true") {
                            changesOfflineRequestsService.validateAndApprovePendingChanges(vm.changes.version)
                                .then(function (data) {
                                    checkForWarning(data);
                                    approveAllNamespacesChanges();
                                    reload();
                                }, function (err) {
                                    checkForConflict(err.status, err.data);
                                });
                        } else {
                            approveAllNamespacesChanges();
                        }
                    }, function () {

                    });

            });
        }

        function triggerModelReload() {
            var dlg = dialogs.confirm('Model reload confirmation', 'Are you sure you want to reload current model on XRE Redirector? ');
            dlg.result.then(function (btn) {
                changesRequestsService.triggerModelReload()
                    .then(function (status) {
                        toastr.success('Model reload triggered', 'Success', {closeButton: true, timeOut: 3000});
                    }, function (err) {
                        toastr.error('Action failed due to server error', 'Error', {closeButton: true, timeOut: 3000});
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
            cancelAllNamespacesChanges();
            changesOfflineRequestsService.cancelAllPendingChanges(vm.changes.version)
                .then(function (data) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function cancelAllPendingDistribution() {
            changesOfflineRequestsService.cancelAllPendingDistribution(vm.changes.version)
                .then(function (status) {
                    reload();
                }, function (err) {
                    checkForConflict(err.status, err.data);
                });
        }

        function approveAllPendingDistributionChanges() {
            changesOfflineRequestsService.approveAllPendingDistribution(vm.changes.version)
                .then(function (status) {
                    reload();
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

        // -------- NamespacedList

        function cancelNamespacesChanges(item) {
            changesOfflineRequestsService.cancelNamespacesChanges(item.key)
                .then(function (status) {
                    reload();
                }, function (err) {
                    toastr.error('Cancel failed due to server error: ' + err.data, 'Error', {closeButton: true, timeOut: 20000});
                });
        }

        function cancelAllNamespacesChanges() {
            changesOfflineRequestsService.cancelAllNamespacesChanges()
                .then(function (status) {
                    reload();
                }, function (err) {
                    toastr.error('Cancel failed due to server error: ' + err.data, 'Error', {closeButton: true, timeOut: 20000});
                });
        }


        function approveAllNamespacesChanges() {
            changesOfflineRequestsService.approveAllNamespacesChanges(vm.namespacesChanges)
                .then(function (namespaceChangesStatus) {
                        vm.namespacesChanges = angular.copy(namespaceChangesStatus.namespaceChanges);
                        toastr.success('Approve Namespaces Changes', 'Success', {closeButton: true, timeOut: 3000});
                        reload();
                    }, function (err) {
                        reloadNamespacesChanges();
                        dialogs.notify('Failed to approve namespaced list(s): ' + getNamespacedListNameStringWithComma(), err.data);
                    }
                );
        }

        function approveNamespacesChanges(item) {
            changesOfflineRequestsService.approveNamespacesChanges(item.key)
                .then(function (status) {
                        toastr.success('Approve Namespaces Changes', 'Success', {closeButton: true, timeOut: 3000});
                    }, function (err) {
                        toastr.error('Approve failed due to server error: ' + err.data, 'Error', {closeButton: true, timeOut: 20000});
                    }
                );
        }


        function init(pendingChanges) {
            changesOfflineRequestsService.hasChanges(pendingChanges)
                .then(function (emptyPending) {
                    if (emptyPending == 'true') {
                        toastr.warning('Model has no changes to import', 'Warning', {closeButton: true, timeOut: 12000});
                    } else {
                        // Because pendingData different in API and UI
                        reload();
                    }

                }, function (error) {
                    $log.error("Error occured  while determining pending changes emptyness", error);
                });
        }

        function reload() {
            reloadNamespacesChanges();
            changesOfflineService.loadPendingChanges().
                then(function (pendingData) {
                    fillInModel(pendingData);
            }, function (reason) {
                $log.error(reason);
            });
        }

        function fillInModel(pendingData) {
            vm.pendingDistribution = angular.copy(pendingData.pendingDistribution);
            vm.currentDistribution = angular.copy(pendingData.currentDistribution);
            vm.changes = angular.copy(pendingData.pending);

            vm.changes.pathRules = changesOfflineService.generatePathRules(vm.changes.pathRules);
            vm.changes.templatePathRules = changesOfflineService.generatePathRules(vm.changes.templatePathRules);
            vm.changes.urlRules = changesOfflineService.generateUrlRules(vm.changes.urlRules);
            vm.changes.templateUrlPathRules = changesOfflineService.generateUrlRules(vm.changes.templateUrlPathRules);

            var whitelistedData = changesOfflineService.generateWhitelistedData(vm.changes.whitelisted, pendingData.whitelisted);
            vm.whitelistChangedDiff = whitelistedData.whitelistChangedDiff;
            vm.whitelistCurrentDiff = whitelistedData.whitelistCurrentDiff;
        }

        function reloadNamespacesChanges() {
            changesOfflineService.loadNamespacesChanges().
            then(function (namespacesChangesStatus) {
                vm.namespacesChanges = angular.copy(namespacesChangesStatus.namespaceChanges);
            }, function (reason) {
                $log.error(reason);
            });
        }

        function getNamespacedListNameStringWithComma() {
            var namespacesList = "";
            var prefix = "";
            angular.forEach(vm.namespacesChanges.entry, function(entry) {
                namespacesList += prefix + entry.key.name;
                prefix = ", ";
            });
            return namespacesList;
        }
    }
})();
