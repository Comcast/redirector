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
    angular
        .module('uxData.traffic')
        .factory('trafficService', trafficService);

    trafficService.$inject = ['$q', 'trafficRequestService', 'TRAFFIC_CONSTANTS'];

    function trafficService($q, trafficRequestService, TRAFFIC_CONSTANTS) {

        var ADJUSTED_TRAFFIC = TRAFFIC_CONSTANTS().ADJUSTED_TRAFFIC;
        var ADJUSTED_WEIGHT = TRAFFIC_CONSTANTS().ADJUSTED_WEIGHT;

        var service = {
            getAllTraffic: getAllTraffic,
            getTraffic: getTraffic
        };

        return service;

        function getTraffic(inputParams) {
            return trafficRequestService.getTraffic(inputParams);
        }

        function getAllTraffic(totalNumberConnections, connectionThreshold, isActive,
                               adjustedWeightHosts, defaultWeight, adjustedWeight,
                               adjustedTraffic, weightsCalculationMode) {
            var defer = $q.defer();
            var result = {};

            var promises = [];

            promises.push($q.when(getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive)));

            if (angular.equals(weightsCalculationMode, ADJUSTED_WEIGHT)) {
                promises.push($q.when(getAdjustedWeights(adjustedWeightHosts, totalNumberConnections, defaultWeight, adjustedTraffic)));
            }
            else if (angular.equals(weightsCalculationMode, ADJUSTED_TRAFFIC)) {
                promises.push($q.when(getAdjustedThreshold(adjustedWeightHosts, totalNumberConnections, defaultWeight, adjustedWeight)));
            }

            $q.allSettled(promises)
                .then(function (results) {
                    angular.forEach(results, function (promise, index) {
                        if (angular.isDefined(promise.value)) {
                            angular.extend(result, promise.value);
                        }
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function getAdjustedWeights(adjustedWeightHosts, totalConnections, defaultWeight, adjustedTraffic) {
            var deferred = $q.defer();
            trafficRequestService.getAdjustedWeights(adjustedWeightHosts, totalConnections, defaultWeight, adjustedTraffic)
                .then(function (result) {
                    deferred.resolve({adjustedWeights: result});
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function getAdjustedThreshold(adjustedWeightHosts, totalConnections, defaultWeight, adjustedWeight) {
            var deferred = $q.defer();
            trafficRequestService.getAdjustedThreshold(adjustedWeightHosts, totalConnections, defaultWeight, adjustedWeight)
                .then(function (result) {
                    deferred.resolve({adjustedThreshold: result});
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive) {
            var defer = $q.defer();
            var traffic = {};
            trafficRequestService.getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive)
                .then(function (data) {
                    traffic = angular.copy(data)
                    defer.resolve({traffic: traffic});
                }, function (reason) {
                    defer.reject({message: 'Can\'t load data' + reason});
                });

            return defer.promise;
        }
    }
})();
