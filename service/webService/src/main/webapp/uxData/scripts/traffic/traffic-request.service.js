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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

(function () {
    'use strict';

    angular
        .module('uxData.traffic')
        .factory('trafficRequestService', trafficRequestService);

    trafficRequestService.$inject = ['$injector', 'redirectorOfflineMode'];

    function trafficRequestService($injector, redirectorOfflineMode) {

        var dataSource;
        if (redirectorOfflineMode) {
            dataSource = $injector.get('IndexedDBTrafficService');
        }
        else {
            dataSource = $injector.get('TrafficWebService');
        }

        var service = {
            getTraffic: getTraffic,
            getCurrentTraffic: getCurrentTraffic,
            getAdjustedWeights: getAdjustedWeights,
            getAdjustedThreshold: getAdjustedThreshold

        };

        return service;

        function getTraffic(inputParams) {
            return dataSource.getTraffic(inputParams);
        }

        function getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive) {
            return dataSource.getCurrentTraffic(totalNumberConnections, connectionThreshold, isActive);
        }

        function getAdjustedWeights(adjustedWeightHosts, totalConnections, defaultWeight, adjustedTraffic) {
            return dataSource.getAdjustedWeights(adjustedWeightHosts, totalConnections, defaultWeight, adjustedTraffic);
        }

        function getAdjustedThreshold(adjustedWeightHosts, totalConnections, defaultWeight, adjustedWeight) {
            return dataSource.getAdjustedThreshold(adjustedWeightHosts, totalConnections, defaultWeight, adjustedWeight);
        }
    }
})();
