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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */


(function () {
    'use strict';

    angular.
        module('uxData.traffic').
        controller('TrafficCtrl', TrafficCtrl);

    TrafficCtrl.$inject = ['$scope', 'toastr', 'messageService', 'LOCALSTORAGE_PAGE_NAMES', 'trafficService', 'TRAFFIC_CONSTANTS'];

    function TrafficCtrl($scope, toastr, messageService, LOCALSTORAGE_PAGE_NAMES, trafficService, TRAFFIC_CONSTANTS) {
        var vm = this;

        var ADJUSTED_TRAFFIC = TRAFFIC_CONSTANTS().ADJUSTED_CALCULATION_MODE.ADJUSTED_TRAFFIC;
        var ADJUSTED_WEIGHT = TRAFFIC_CONSTANTS().ADJUSTED_CALCULATION_MODE.ADJUSTED_WEIGHT;
        var ALL_WHITELISTED = TRAFFIC_CONSTANTS().HOSTS_MODE.ALL_WHITELISTED;
        var CURRENT         = TRAFFIC_CONSTANTS().DISTRIBUTION_MODE.CURRENT;

        var OFF             = "off";

        vm.reloadTraffic = reloadTraffic;
        vm.getTraffic = getTraffic;

        vm.traffic = {};

        vm.horizontalTableOrientation = true; //if amount of distribution rules less than 3 orientation is horizontal, otherwise - vertical
        vm.adjustedTrafficCalculationModes = [
            {name: 'Adjusted traffic', value: ADJUSTED_TRAFFIC},
            {name: 'Adjusted weight', value: ADJUSTED_WEIGHT}
        ];
        vm.trafficInputParams = {
            connectionThreshold: 0,
            totalNumberConnections: 0,
            hostsMode: ALL_WHITELISTED,
            distributionMode: CURRENT,
            adjustedTrafficInputParams : {
                adjustedTrafficCalculationMode: '',
                defaultServer : {},
                distributions : []
            }
        };

        init();

        function init() {
            getTraffic(vm.trafficInputParams);
        }

        function getTraffic(trafficInputParams) {
            updateValuesBeforeGettingTraffic();
            trafficService.getTraffic(trafficInputParams)
                .then(function(data) {
                    updateModelWithNewTraffic(data);
                    updateValuesAfterGettingTraffic();
                }, function(error){
                    toastr.error('Failed to calculate traffic', 'Error', {closeButton: true, timeOut: 3000});
                }
            );
        }

        function updateValuesBeforeGettingTraffic () {
            if (angular.isDefined (vm.traffic.defaultServer)) {
                vm.trafficInputParams.adjustedTrafficInputParams.defaultServer.adjustedThreshold =
                    getIntValueFromHumanReadableValueFromInput(vm.trafficInputParams.adjustedTrafficInputParams.defaultServer.adjustedThreshold);
            }
            angular.forEach(vm.trafficInputParams.adjustedTrafficInputParams.distributions, function(distribution, index) {
                distribution.adjustedThreshold = getIntValueFromHumanReadableValueFromInput(distribution.adjustedThreshold);
            });
        }

        function updateValuesAfterGettingTraffic () {
            if (angular.isDefined (vm.traffic.defaultServer.adjustedThreshold)) {
                vm.traffic.defaultServer.adjustedThresholdHumanReadable = getHumanReadableValueForNegativePercentForTable(vm.traffic.defaultServer.adjustedThreshold);
            }
            angular.forEach(vm.traffic.distributions, function(distribution, index) {
                distribution.adjustedThresholdHumanReadable = getHumanReadableValueForNegativePercentForTable(distribution.adjustedThreshold);
            });

            if (angular.isDefined (vm.trafficInputParams.adjustedTrafficInputParams.defaultServer.adjustedThreshold)) {
                vm.trafficInputParams.adjustedTrafficInputParams.defaultServer.adjustedThreshold =
                    getHumanReadableValueForNegativePercentForInput(vm.trafficInputParams.adjustedTrafficInputParams.defaultServer.adjustedThreshold);
            }
            angular.forEach(vm.trafficInputParams.adjustedTrafficInputParams.distributions, function(distribution, index) {
                distribution.adjustedThreshold = getHumanReadableValueForNegativePercentForInput(distribution.adjustedThreshold);
            });
        }

        function getHumanReadableValueForNegativePercentForTable (percent) {
            return percent < 0 ? -percent + '% taken off' : percent;
        }

        function getHumanReadableValueForNegativePercentForInput (percent) {
            if ((percent + '').indexOf (OFF) > 0) {
                return percent;
            }
            return percent < 0 ? -percent + ' ' + OFF: percent;
        }

        function getIntValueFromHumanReadableValueFromInput (value) {
            value = value + '';
            return value.indexOf(' ' + OFF) > 0 ? -value.replace(' ' + OFF, '') : value;
        }

        function updateModelWithNewTraffic(newTraffic) {

            // 1. update traffic
            updateTrafficModel(newTraffic);

            // 2. update input params of default distribution server
            updateDefaultServerAdjustedInputParams(newTraffic);

            // 3. update input params for each of distribution rules
            updateDistributionRulesAdjustedInputParams(newTraffic);

            // 4. set table orientation
            vm.horizontalTableOrientation = newTraffic.distributions.length < 3;
        }

        function updateTrafficModel(newTraffic) {

            angular.copy(newTraffic, vm.traffic);
            var calculationMode = vm.trafficInputParams.adjustedTrafficInputParams.adjustedTrafficCalculationMode;

            // this is needed to simplify the vm.traffic structure and eliminate the need for UI to know the type of
            // adjusted calculation mode. It will simplify generation of table with traffic

            // 1. deal with default server
            angular.equals(ADJUSTED_TRAFFIC, calculationMode) ?
                angular.extend(vm.traffic.defaultServer, newTraffic.defaultServer.adjustedThreshold) :
                angular.extend(vm.traffic.defaultServer, newTraffic.defaultServer.adjustedWeights);

            // 2. make the same with distributions
            angular.forEach(vm.traffic.distributions, function(distribution, index) {
                angular.equals(ADJUSTED_TRAFFIC, calculationMode) ?
                    angular.extend(distribution, distribution.adjustedThreshold) :
                    angular.extend(distribution, distribution.adjustedWeights);
            });
        }

        function updateDefaultServerAdjustedInputParams(newTraffic) {
            var defaultServer = vm.trafficInputParams.adjustedTrafficInputParams.defaultServer;
            var calculationMode = vm.trafficInputParams.adjustedTrafficInputParams.adjustedTrafficCalculationMode;

            if (angular.equals(ADJUSTED_TRAFFIC, calculationMode) && angular.isDefined(newTraffic.defaultServer.adjustedThreshold)) {
                defaultServer.adjustedThreshold = newTraffic.defaultServer.adjustedThreshold.adjustedThreshold;
            }
            else if (angular.equals(ADJUSTED_WEIGHT, calculationMode) && angular.isDefined(newTraffic.defaultServer.adjustedWeights)) {
                defaultServer.adjustedWeight = newTraffic.defaultServer.adjustedWeights.adjustedWeight;
            }
            else {
                angular.copy(getAdjustedEmptyInputParams(newTraffic.defaultServer.flavor, newTraffic.defaultServer.title), defaultServer);
            }
        }

        /**
         * if for existing distribution rule new traffic was calculated updates input fields with result
         * if a new distribution rule was added -> adds adjusted input params for new rule
         * if existing distribution rule was removed -> removes adjusted input params for it
         */
        function updateDistributionRulesAdjustedInputParams(newTraffic) {
            var calculationMode = vm.trafficInputParams.adjustedTrafficInputParams.adjustedTrafficCalculationMode;
            var currentDistributions = vm.trafficInputParams.adjustedTrafficInputParams.distributions;
            var newDistributions = [];

            angular.forEach(newTraffic.distributions, function(newDistribution, index) {

                var currentDistribution = distributionContainsRule(currentDistributions, newDistribution);

                if (angular.isDefined(currentDistribution)) {
                    if (angular.equals(ADJUSTED_TRAFFIC, calculationMode) && angular.isDefined(newDistribution.adjustedThreshold.adjustedThreshold)) {
                        currentDistribution.adjustedThreshold = newDistribution.adjustedThreshold.adjustedThreshold;
                    }
                    else if (angular.equals(ADJUSTED_WEIGHT, calculationMode) && angular.isDefined(newDistribution.adjustedWeights.adjustedWeight)) {
                        currentDistribution.adjustedWeight = newDistribution.adjustedWeights.adjustedWeight;
                    }
                    newDistributions.push(currentDistribution);
                }
                else {
                    newDistributions.push(getAdjustedInputParams(newDistribution.flavor, newDistribution.title, '', '', '', ''));
                }

            });

            vm.trafficInputParams.adjustedTrafficInputParams.distributions = newDistributions;
        }

        function getAdjustedEmptyInputParams(flavor, title) {
            return getAdjustedInputParams(flavor, title, '', '', '', '');
        }

        function getAdjustedInputParams(flavor, title, numberOfHostsToAdjust, defaultWeight, adjustedWeight, adjustedThreshold) {
            return {flavor: flavor, title: title, numberOfHostsToAdjust: numberOfHostsToAdjust, defaultWeight: defaultWeight, adjustedWeight: adjustedWeight, adjustedThreshold: adjustedThreshold}
        }

        function reloadTraffic() {
            getTraffic(vm.trafficInputParams);
        }

        function distributionContainsRule(distributions, rule) {
            var result = undefined;

            angular.forEach(distributions, function (distribution, index) {
                if (angular.equals(rule.flavor, distribution.flavor)) {
                    result = distribution;
                }
            });

            return result;
        }

        messageService.onChangeApp($scope, function () {
            vm.traffic = [];
            vm.isActive = false;
            vm.trafficInputParams = {
                connectionThreshold: 0,
                totalNumberConnections: 0
            };
            init();
        });
    }
})();
