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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';

    angular
        .module('uxData.active-whitelisted')
        .controller('ActiveWhitelistedCtrl', ActiveWhitelistedCtrl);

    ActiveWhitelistedCtrl.$inject = ['$scope', '$location', 'toastr', 'messageService', 'stacksManagementService'];

    function ActiveWhitelistedCtrl($scope, $location, $toastr, messageService, stacksManagementService) {

        var vm = this;

        vm.getWhitelistedInfo = getWhitelistedInfo;

        vm.whitelistedCount = 0;
        vm.whitelistedStacks = [];
        vm.stacksAndFlavors = {};
        vm.flavors = {};

        init();

        function init() {
            vm.url = $location.path();
            vm.getWhitelistedInfo();
        }

        /**
         * This is the {@link stacksManagementService.getServicePathsFromDS} call with overall active nodes count calculation
         */
        function getStacks() {
            stacksManagementService.getServicePathsFromDS([], [], vm.stacksAndFlavors).then(
                function () {
                    var activeNodes = 0;
                    angular.forEach(vm.whitelistedStacks, function (value) {
                        if (angular.isDefined(vm.stacksAndFlavors[value]) && angular.isDefined(vm.stacksAndFlavors[value].flavors)) {
                            angular.forEach(vm.stacksAndFlavors[value].flavors, function (flavor) {
                                /*
                                 * TODO: this implementation is overcomplicated:
                                 * We create flavor objects on front-end but we can get them ready just from back-end response.
                                 * Need to simplify in next release
                                 */
                                if (flavor.nodesWhitelisted > 0) {
                                    if (angular.isDefined(vm.flavors[flavor.name])) {
                                        vm.flavors[flavor.name].nodes += flavor.nodesWhitelisted;
                                    } else {
                                        vm.flavors[flavor.name] = {
                                            name: flavor.name,
                                            nodes: flavor.nodesWhitelisted
                                        };
                                    }
                                }

                                if (flavor.active) {
                                    activeNodes += flavor.nodes;
                                }
                            });
                        }
                    });
                    vm.whitelistedCount = activeNodes;
                }
            );
        }

        function getWhitelistedInfo() {
            vm.whitelistedStacks = [];
            stacksManagementService.getWhitelistedFromDS(vm.whitelistedStacks)
                .then(function (data) {
                    angular.forEach(data.paths.entry, function(entry) {
                        if (entry.value.action !== 'DELETE') {
                           vm.whitelistedStacks.push(entry.key);
                        }
                    });
                    getStacks();
                },
                function (error) {
                    $toastr.error(error.message, 'Error', {
                        closeButton: true,
                        timeOut: 3000
                    });
                });
        }

        messageService.onChangeApp($scope, function () {
            vm.whitelistedCount = 0;
            vm.whitelistedStacks = [];
            vm.stacksAndFlavors = {};
            vm.flavors = {};
            vm.getWhitelistedInfo();
        });
    }
})();
