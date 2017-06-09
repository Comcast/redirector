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
        .controller('PartnersEdit', PartnersEdit);

    PartnersEdit.$inject = ['$scope', '$modalInstance', '$log', '$filter', 'toastr', 'ngTableParams','authService', 'USER_PERMISSIONS', 'partnersService', 'utilsService', 'partners', 'partnerName', 'editMode', 'properties'];

    function PartnersEdit($scope, $modalInstance, $log, $filter, toastr, ngTableParams, authService, USER_PERMISSIONS, partnersService, utilsService, partners, partnerName, editMode, properties) {
        /* jshint validthis: true */

        var vm = this;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;

        angular.extend(vm, {'partnerName': partnerName});
        angular.extend(vm, {'properties': properties});
        angular.extend(vm, {'editMode': editMode});

        vm.title = vm.editMode ? 'Edit partner' : 'Add new partner';
        vm.propertyName = '';
        vm.propertyValue = '';

        vm.addProperty = addProperty;
        vm.deleteProperty = deleteProperty;
        vm.savePartner = savePartner;
        vm.isPropertiesListEmpty = isPropertiesListEmpty;

        initData();

        function initData() {
            vm.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 10,          // count per page
                sorting: {
                    propertyId: 'asc'     // initial sorting
                }
            }, {
                total: 0, // length of data
                getData: function ($defer, params) {
                    var orderedData = params.sorting() ? $filter('orderBy')(generateTableData(params.data), params.orderBy()) : generateTableData(params.data);

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

        function generateTableData(data) {
            if (angular.isDefined(data) && data.length > 0 && data.length === vm.properties.length) {
                // sorting or filtering called getData() - we don't need to generate tableData in this case
                return data;
            }
            var tableData = [];
            angular.forEach(vm.properties, function (propValue, propName) {
                tableData.push(
                    {
                        'propertyId': propName,
                        'propertyValue': propValue
                    }
                );
            });
            return tableData;
        }

        function addProperty() {
            if (vm.properties[this.propertyName]) {
                toastr.error('This property exists already', 'Error', {
                    closeButton: true,
                    timeOut: 3000
                });
                return;
            }
            vm.properties[vm.propertyName] = vm.propertyValue;
            vm.propertyName = '';
            vm.propertyValue = '';
            vm.tableParams.reload();
        }

        function deleteProperty(name) {
            delete vm.properties[name];
            vm.tableParams.reload();
        }

        function isPropertiesListEmpty() {
            return utilsService.isEmptyObject(vm.properties);
        }

        function savePartner() {
            if (!validatePartner(vm.partnerName)) {
                return;
            }
            var partnerToSave = angular.copy(vm.properties);
            partnerToSave['id'] = vm.partnerName;
            partnersService.savePartner(partnerToSave)
                .then(function (status) {
                    toastr.success('Saved partner item: \"' + partnerToSave.id + '\"', 'Success', {closeButton: true, timeOut: 3000});
                    $log.info('Success: Saved partner: ' + partnerToSave.id);
                    $modalInstance.close(true);
                }, function (err) {
                    toastr.error('Can\'t save partner: ' + err.message, 'Error', {closeButton: true, timeOut: 3000});
                    $log.error('Failed: ' + err.message);
                });
        }

        function validatePartner(partnerName) {
            if (!vm.editMode && $.inArray(partnerName, getPartnerIdList()) !== -1) {
                toastr.error('This partner name exists already', 'Error', {
                    closeButton: true,
                    timeOut: 3000
                });
                return false;
            }
            if (isPropertiesListEmpty()) {
                toastr.error('Validation error: partner must contain at least one property', 'Error', {
                    closeButton: true,
                    timeOut: 3000
                });
                return false;
            }
            return true;
        }

        var getPartnerIdList = function() {
            var partnerIdList = [];
            angular.forEach(partners, function(value, key) {
                partnerIdList.push(value.id);
            });
            return partnerIdList;
        };
    }
})();
