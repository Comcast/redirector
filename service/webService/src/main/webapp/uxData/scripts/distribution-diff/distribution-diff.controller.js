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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */


angular.module('uxData.diff-distribution').controller('DiffDistributions', ['$scope', 'constantsProvider', 'utilsService','diffService',
    function DiffDistributionsCtrl($scope, constants, utilsService, diffService) {

        var vm = this;
        vm.baseHeadTitle = $scope.baseHeadTitle || 'Current distributions';
        vm.changedHeadTitle = $scope.changedHeadTitle || 'Pending distributions';
        vm.baseNormalizedDistributions = {};
        vm.changedNormalizedDistributions = {};

        vm.SIDES = {LEFT: "LEFT", RIGHT: "RIGHT"};

        // in case when some percent is out of range in distribution rules.
        vm.isSomeBasePercentOutOfRange = false;
        vm.isSomeChangedPercentOutOfRange = false;

        // available range is [0.01,...,99.99]
        vm.MAX_TOTAL_Percentage = 100;


        vm.refresh = refresh;

        vm.isDeleteStatus = isDeleteStatus;
        vm.isAddStatus = isAddStatus;
        vm.isUpdateStatus = isUpdateStatus;
        vm.hasDiffChanges = hasDiffChanges;
        vm.getDistributionsBySide = getDistributionsBySide;
        vm.isValidTotalPercentage = isValidTotalPercentage;
        vm.updatePercentOutOfRangeFlagBySide = updatePercentOutOfRangeFlagBySide;
        vm.calculateTotalPercentage = calculateTotalPercentage;
        vm.getDistributionViewText = getDistributionViewText;
        vm.getDistributionById = getDistributionById;
        vm.getNormalizedArray = getNormalizedArray;
        vm.getDistributionsSize = getDistributionsSize;
        vm.getDiffs = getDiffs;

        function isAddStatus () {
            return getDistributionsViewText($scope.baseDistributions) == "";
        }

        function isDeleteStatus () {
            return getDistributionsViewText($scope.changedDistributions) == "";
        }

        function hasDiffChanges () {
            return !angular.equals(getDistributionsViewText($scope.baseDistributions),
                                   getDistributionsViewText($scope.changedDistributions));
        }

        function isUpdateStatus () {
            if (isAddStatus() || isDeleteStatus()) {
                return false;
            }
            return vm.hasDiffChanges();
        }

        function getDistributionsBySide (side) {
            var distributions = {};
            switch (side) {
                case vm.SIDES.LEFT:
                    distributions = $scope.baseDistributions;
                    break;
                case vm.SIDES.RIGHT:
                    distributions = $scope.changedDistributions;
                    break;
            }
            return distributions;
        }

        function updatePercentOutOfRangeFlagBySide (side, value) {
            switch (side) {
                case vm.SIDES.LEFT:
                    vm.isSomeBasePercentOutOfRange = value;
                    break;
                case vm.SIDES.RIGHT:
                    vm.isSomeChangedPercentOutOfRange = value;
                    break;
            }
        }

        function calculateTotalPercentage (side) {
            var total = 0;
            var distributions = getDistributionsBySide(side);
            var isPercentOutOfRange = false;
            if (angular.isDefined(distributions) && distributions != null) {
                angular.forEach(distributions, function (distribution, key) {
                    if (angular.isDefined(distribution) && distribution != null) {
                        if (angular.isUndefined(distribution.percent)) {
                            isPercentOutOfRange = true;
                        }
                        total += angular.isUndefined(distribution.percent) ? 0 : distribution.percent;
                    }
                });
            }
            updatePercentOutOfRangeFlagBySide(side, isPercentOutOfRange);
            return parseFloat(total) || 0;
        }

        function isValidTotalPercentage (side) {
            return calculateTotalPercentage(side) < vm.MAX_TOTAL_Percentage;
        }

        function getDistributionsViewText (distributions) {
            var viewText = "";
            if (angular.isDefined(distributions) && distributions != null) {
                angular.forEach(distributions, function (distribution, index) {
                    if (angular.isDefined(distribution) && distribution != null) {
                        viewText += getDistributionViewText(distribution, index + 1) + "\n";
                    }
                });
            }
            return viewText.substring(0, viewText.length - 1);
        }


        function getDistributionViewText(distribution, lineNumber) {
            var viewText = "";
            if (angular.isDefined(distribution) && distribution != null && angular.isDefined(distribution.server)) {
                viewText += (lineNumber) + ". " + (angular.isUndefined(distribution.percent) ? ' OUT OF RANGE! '  : distribution.percent) + '% - ' + distribution.server.path;
                if (angular.isDefined(distribution.server.query) && distribution.server.query != null && angular.isDefined(distribution.server.query.entry) 
                    &&  distribution.server.query.entry != null && distribution.server.query.entry.length > 0) {
                    viewText += " [";
                    angular.forEach(distribution.server.query.entry, function (pair, id) {
                        viewText += pair.key + "=" + pair.value + ", ";
                    });
                    viewText = viewText.slice(0,-2);
                    viewText += "]"
                }

            }
            return viewText;
        }

        function getDistributionsSize () {
            var maxId = 0;
            if ($scope.baseDistributions && $scope.baseDistributions != null && angular.isArray($scope.baseDistributions)) {
                angular.forEach($scope.baseDistributions, function (rule, index) {
                    maxId = Math.max(maxId, rule.id);
                });
            }
            if ($scope.changedDistributions && $scope.changedDistributions != null && angular.isArray($scope.changedDistributions)) {
                angular.forEach($scope.changedDistributions, function (rule, index) {
                    maxId = Math.max(maxId, rule.id);
                });
            }
            return ($scope.baseDistributions && $scope.baseDistributions.length < 1
                && $scope.changedDistributions.length && $scope.changedDistributions.length < 1) ? 0 : maxId + 1;
        }

        function getDistributionById (distributions, id) {
            var result = {};
            if (angular.isDefined(distributions) && distributions != null) {
                for (var i = 0; i < distributions.length; i++) {
                    if (distributions[i] != null && angular.equals(distributions[i].id, id)) {
                        result = distributions[i];
                        break;
                    }
                }
            }
            return result;
        }

        function getNormalizedArray (distributions) {
            var normalizedArray = [];
            var distributionsSize = getDistributionsSize();
            for (var i = 0; i < distributionsSize; i++) {
                normalizedArray[i] = getDistributionById(distributions, i);
            }
            return normalizedArray;
        }

        function refresh  () {
            vm.baseNormalizedDistributions = vm.getNormalizedArray($scope.baseDistributions);
            vm.changedNormalizedDistributions = vm.getNormalizedArray($scope.changedDistributions);
        }

        function getText (textObject) {
            return angular.isDefined(textObject) ? textObject : "";
        }

        function getDiffs (text1, text2, resultSide) {
            var config = {
                ignoreLeadingWS: true,
                showAllContent: true,
                type: 'default'
            };
            return diffService.formattedDiff(getText(text1), getText(text2), resultSide, config);
        }
    }]);

