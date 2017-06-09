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
        .module('uxData.partners')
        .controller('PartnersImport', PartnersImport);

    PartnersImport.$inject = ['$scope', '$modalInstance', '$log', '$filter', 'toastr', 'ngTableParams', 'authService', 'USER_PERMISSIONS', 'partnersService', 'utilsService', 'importService', 'partners'];

    function PartnersImport($scope, $modalInstance, $log, $filter, toastr, ngTableParams, authService, USER_PERMISSIONS, partnersService, utilsService, importService, partners) {
        /* jshint validthis: true */

        var vm = this;
        angular.extend(vm, {'partners': partners});

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        vm.textTitle = {};
        vm.textTitle.CHOOSE_PROMPT = 'Choose a file with Partners';
        vm.textTitle.SAVE_PROMPT = 'Save the partners lists by clicking \"Save\" button';
        vm.title = vm.textTitle.CHOOSE_PROMPT;
        vm.importContentForPreview = null;

        vm.partnersIdList = [];
        vm.partnersFromFile = [];

        vm.importAllPartners = importAllPartners;
        vm.importPartnerById = importPartnerById;
        vm.getFile = retrieveFile;
        vm.closeForm = closeForm;

        initData();

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
                    var orderedData = params.sorting() ? $filter('orderBy')(generateTableData(vm.partnersFromFile, params.data),
                        params.orderBy()) : generateTableData(vm.partnersFromFile, params.data);

                    // use build-in angular filter
                    orderedData = params.filter() ?
                        $filter('filter')(orderedData, params.filter()) : orderedData;
                    params.total(orderedData.length); // set total for recalc pagination
                    var totalPages = Math.max(1, Math.ceil(params.total() / params.count()));
                    if (params.page() > totalPages) {
                        params.page(totalPages);
                    }

                    orderedData = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());
                    $defer.resolve(orderedData);
                }
            });
            vm.tableParams.settings().$scope = $scope;
        }

        function generateTableData(partners, data) {
            if (angular.isDefined(data) && data.length > 0 && data.length === partners.length) {
                // sorting or filtering called getData() - we don't need to generate tableData in this case
                return data;
            }
            var tableData = [];
            angular.forEach(partners, function (partner, index) {
                tableData.push(
                    {
                        'id': partner.id,
                        'properties': partnersService.getPartnerPropertiesAsString(partner),
                        'propCount': Object.getOwnPropertyNames(partner).length - 1
                    }
                );
            });
            return tableData;
        }

        function retrieveFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                getPartnersFromFile(result);
            }, function (reason) {
                $log.error('Reason: ' + reason.message);
                toastr.error('Something wrong when convert file', 'Error', {closeButton: true, timeOut: 3000});
            });
        }

        function getPartnersFromFile(result) {
            try {
                vm.importContentForPreview = angular.copy(result);
                var partnersObject = JSON.parse(result);
                if (utilsService.isDefinedAndNotEmpty(partnersObject) &&
                    utilsService.isDefinedAndNotEmpty(partnersObject.partner)) {
                    vm.partnersFromFile = partnersService.getPartnersFromPartnersObject(partnersObject);
                    vm.title = vm.textTitle.SAVE_PROMPT;
                    vm.wasImport = false;
                    vm.partnersIdList = getPartnersIdList(vm.partners);
                    vm.tableParams.reload();
                }
            } catch (reason) {
                $log.error('Reason: ' + reason.message);
                toastr.error('Can\'t parse file', 'Error', {closeButton: true, timeOut: 3000});
            }

        }

        function importAllPartners() {
            if (vm.partnersFromFile.length === 0) {
                return;
            }
            for (var i = 0; i < vm.partnersFromFile.length; i++) {
                importPartner(vm.partnersFromFile[i]);
            }
        }

        function importPartnerById(partnerId) {
            var index = partnersService.findPositionOfPartnerById(vm.partnersFromFile, partnerId);
            importPartner(vm.partnersFromFile[index]);
        }

        function importPartner(partner) {
            vm.namespacedDuplicateObject = {};
            if ($.inArray(partner.id, vm.partnersIdList) !== -1) {
                toastr.error('Can\'t save \"' + partner.id + '\" is already exists', 'Error', {
                    closeButton: true,
                    timeOut: 6000
                });
                return;
            }
            var partnerToSave = partner;
            partnersService.savePartner(partnerToSave).then(function (data) {
                $log.info('Added partner: \"' + partner.id + '\"');
                toastr.success('Saved partner item: ' + partner.id, 'Success', {closeButton: true, timeOut: 3000});
                var position = partnersService.findPositionOfPartnerById(vm.partnersFromFile, partner.id);
                vm.partners.push(vm.partnersFromFile[position]);
                vm.partnersFromFile.splice(position, 1);
                vm.wasImport = true;
                if (vm.partnersFromFile.length < 1) {
                    vm.title = vm.textTitle.CHOOSE_PROMPT;
                }
                vm.tableParams.reload();
            }, function (reason) {
                $log.error('Failed: ' + reason.message);
                toastr.error('Can\'t add partner: \"' + partner.id + '\": ' + reason, 'Error', {closeButton: true, timeOut: 3000});
            });
        }

        function getPartnersIdList(partners) {
            var partnersIdList = [];
            angular.forEach(partners, function(value, key) {
                partnersIdList.push(value.id);
            });
            return partnersIdList;
        }

        function closeForm() {
            $modalInstance.close(vm.wasImport);
        }
    }
})();
