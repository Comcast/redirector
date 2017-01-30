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
 */
(function () {
    'use strict';

    angular
        .module('uxData.partners')
        .controller('Partners', Partners);

    Partners.$inject = ['$rootScope', '$scope', '$modal', 'toastr', 'dialogs', '$filter', '$log', '$q', 'ngTableParams', 'authService', 'USER_PERMISSIONS', 'partnersService', 'requestsService'];

    function Partners($rootScope, $scope, $modal, toastr, dialogs, $filter, $log, $q, ngTableParams, authService, USER_PERMISSIONS, partnersService, requestsService) {
        /* jshint validthis: true */
        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.needToLoadData = true;

        vm.duplicatedIds = [];
        vm.partners = [];

        vm.addPartner = addPartner;
        vm.editPartner = editPartner;
        vm.deletePartner = deletePartner;
        vm.exportAllPartners = exportAllPartners;
        vm.exportPartnerById = exportPartnerById;
        vm.openImportForm = openImportForm;

        initData();

        function refreshWithDataLoading() {
            vm.needToLoadData = true;
            vm.tableParams.reload();
        }

        function initData() {
            vm.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 10,          // count per page
                sorting: {
                    id: 'asc'     // initial sorting
                }
            }, {
                total: 0, // length of data
                getData: function ($defer, params) {
                    if (vm.needToLoadData) {
                        getPartners().then(function (result) {
                            var orderedData = prepareTableDataForShow(result, params);
                            $defer.resolve(orderedData);
                        }, function () {
                        });
                        vm.needToLoadData = false;
                    } else {
                        var orderedData = prepareTableDataForShow(generateTableData(vm.partners, params.data), params);
                        $defer.resolve(orderedData);
                    }
                }
            });
            vm.tableParams.settings().$scope = $scope;
        }

        function prepareTableDataForShow(tableData, params) {
            var orderedData = params.sorting() ? $filter('orderBy')(tableData, params.orderBy()) : tableData;

            // use build-in angular filter
            orderedData = params.filter() ?
                $filter('filter')(orderedData, params.filter()) : orderedData;
            params.total(orderedData.length); // set total for recalc pagination
            var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
            if (params.page() > totalPages) {
                params.page(totalPages);
            }

            orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
            return orderedData;
        }

        function generateTableData(partners, data) {
            if (angular.isDefined(data) && data.length > 0 && data.length === vm.partners.length) {
                // sorting or filtering called getData() - we don't need to generate tableData in this case
                return data;
            }
            var tableData = [];
            angular.forEach(partners, function (partner, index) {
                tableData.push(
                    {
                        'id': partner.id,
                        'properties': partnersService.getPartnerPropertiesAsString(partner)
                    }
                );
            });
            return tableData;
        }

        function getPartners () {
            var defer = $q.defer();
            partnersService.getPartnersFromDS(vm.partners)
                .then(function (result) {
                    $log.info('Success: PartnersDS provided ' + result.length + ' item(s)');
                    var dataForTable = generateTableData(result);
                    defer.resolve(dataForTable);
                },
                function (reason) {
                    if (reason.status === 200) {
                        // returned empty list
                        $log.warn('Warn: ' + reason.data.message);
                        vm.partners = [];
                        defer.resolve([]);
                        return defer.promise;
                    } else {
                        $log.error('Failed: ' + reason.data.message);
                        if (reason.status !== 404) {
                            toastr.error('Can\'t retrieve items:' + reason.data.message, 'Error', {closeButton: true, timeOut: 3000});
                        }
                    }
                    defer.reject();
                }
            );
            return defer.promise;
        }

        function addPartner() {
            showEditPartnerForm('', {}, false);
        }

        function editPartner(partnerId) {
            var index = partnersService.findPositionOfPartnerById(vm.partners, partnerId);
            var properties = {};
            for (var name in vm.partners[index]) {
                if (name !== 'id') {
                    properties[name] = vm.partners[index][name];
                }
            }
            showEditPartnerForm(partnerId, properties, true);
        }

        function showEditPartnerForm(partnerName, properties, editMode) {
            var modalInstance = $modal.open(
                {
                    templateUrl: '../uxData/scripts/partners/partners-edit.html',
                    controller: 'PartnersEdit as vm',
                    windowClass: 'edit-partners-modal-window',
                    resolve: {
                        'partners': function () {
                            return vm.partners;
                        },
                        'partnerName': function () {
                            return partnerName;
                        },
                        'properties': function() {
                            return properties;
                        },
                        'editMode': function () {
                            return editMode;
                        }
                    }
                }
            );
            modalInstance.result.then(function (isSaved) {
                if (isSaved) {
                    refreshWithDataLoading();
                } else {
                    vm.tableParams.reload();
                }
            }, function () {
            });
        }

        function deletePartner(partnerId) {
            var dlg = dialogs.confirm('Delete confirmation', 'Are you sure you want to Delete partner ' + partnerId + '?');
            dlg.result.then(function (btn) {
                partnersService.deletePartner(partnerId)
                    .then(function (data) {
                        toastr.success('Deleted partner item: ' + partnerId, 'Success', {closeButton: true, timeOut: 3000});
                        $log.warn('Deleted partner item: ' + partnerId);
                        refreshWithDataLoading();
                    }, function (reason) {
                        $log.error('Failed: ' + reason.message);
                        dialogs.notify('Failed to delete partner item:  \"' + partnerId + '\"', reason.message);
                    }, function (btn) {
                        //click cancel
                    });
            });
        }

        function exportAllPartners() {
            partnersService.exportAllPartners()
                .then(function () {
                    $log.info('Success export all (' + vm.partners.length + ') partners');
                }, function (reason) {
                    $log.error('Failed: ' + reason);
                });
        }

        function exportPartnerById(partnerId){
            partnersService.exportPartner(partnerId)
                .then(function () {
                    $log.info('Success export partner: \"' + partnerId + '\"');
                }, function (reason) {
                    $log.error('Failed: ' + reason);
                });
        }

        function openImportForm() {
            var modalInstance = $modal.open(
                {
                    templateUrl: '../uxData/scripts/partners/partners-import.html',
                    controller: 'PartnersImport as vm',
                    backdrop: 'static',
                    keyboard: false,
                    windowClass: 'import-partners-modal-window',
                    resolve: {
                        'partners': function () {
                            return vm.partners;
                        }
                    }
                }
            );
            modalInstance.result.then(function (wasImport) {
                if (wasImport) {
                    refreshWithDataLoading();
                } else {
                    vm.tableParams.reload();
                }
            }, function () {
            });

        }

    }
})();
