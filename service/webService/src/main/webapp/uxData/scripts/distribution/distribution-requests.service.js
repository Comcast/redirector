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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';
    angular
        .module('uxData.distribution')
        .factory('distributionRequestService', DistributionRequestService);

    DistributionRequestService.$inject = ['$rootScope', '$injector', 'requestsService', 'distributionAlertsService', 'redirectorOfflineMode'];

    function DistributionRequestService($rootScope, $injector, requestsService, distributionAlertsService, redirectorOfflineMode) {
        var dataSource;
        if (redirectorOfflineMode) {
            dataSource = $injector.get('IndexedDBDataSource');
        } else {
            dataSource = $injector.get('WebServiceDataSource');
        }

        return {
            getDistributions: getDistributions,
            sendDistributions: sendDistributions,
            exportDistributions: exportDistributions,
            saveDistributionWithDefaultServer: saveDistributionWithDefaultServer
        };

        function getDistributions(appName) {
            return dataSource.getDistribution(appName);
        }

        function sendDistributions(applicationName, distribution) {
            return dataSource.saveDistribution(applicationName, distribution);
        }

        function exportDistributions() {
            window.open(requestsService.getBaseApiUrl() + 'distributions/export/' + $rootScope.currentApplication);
        }

        function saveDistributionWithDefaultServer(appName, distributionWithDefaultServer) {
            return dataSource.saveDistributionWithDefaultServer(appName, distributionWithDefaultServer);
        }
    }
})();
