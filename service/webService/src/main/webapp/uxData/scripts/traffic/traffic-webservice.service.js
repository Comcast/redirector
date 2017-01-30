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
    angular.module('uxData.services')
        .factory('TrafficWebService', TrafficWebService);

    TrafficWebService.$inject = ['$rootScope', 'requestsService'];

    function TrafficWebService($rootScope, requestsService) {

        var service = {
            getTraffic: getTraffic,
            getCurrentTraffic: getCurrentTraffic,
            getAdjustedWeights: getAdjustedWeights,
            getAdjustedThreshold: getAdjustedThreshold
        };

        return service;

        function getTraffic(inputParams) {
            return requestsService.saveData(requestsService.getBaseApiUrl() + 'traffic/' + $rootScope.currentApplication,
                inputParams, {'Content-Type': 'application/json', 'Accept': 'application/json'});
        }

        function getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive) {
            return requestsService.getData(requestsService.getBaseApiUrl() + 'traffic/current/' + $rootScope.currentApplication + '/' + totalNumberConnections + '/' + connectionThreshold + '/' + isActive, {'Accept': 'application/json'});
        }

        function getAdjustedWeights(adjustedWeightHosts, totalConnections, defaultWeight, adjustedTraffic) {
            return requestsService.getData(
                requestsService.getBaseApiUrl() + 'weightCalculator/calculateAdjustedWeights/' + $rootScope.currentApplication + '?'
                + 'adjustedWeightHosts=' + adjustedWeightHosts + '&'
                + 'totalConnections=' + totalConnections + '&'
                + 'defaultWeight=' + defaultWeight + '&'
                + 'adjustedTraffic=' + adjustedTraffic,
                {'Accept': 'application/json'});
        }

        function getAdjustedThreshold(adjustedWeightHosts, totalConnections, defaultWeight, adjustedWeight) {
            return requestsService.getData(
                requestsService.getBaseApiUrl() + 'weightCalculator/calculateAdjustedThreshold/' + $rootScope.currentApplication + '?'
                + 'adjustedWeightHosts=' + adjustedWeightHosts + '&'
                + 'totalConnections=' + totalConnections + '&'
                + 'defaultWeight=' + defaultWeight + '&'
                + 'adjustedWeight=' + adjustedWeight,
                {'Accept': 'application/json'});
        }

    }
})();
