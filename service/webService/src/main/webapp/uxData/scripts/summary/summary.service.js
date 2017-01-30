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
        .module('uxData.summary')
        .factory('summaryService', summaryService);

    summaryService.$inject = ['$q', 'summaryRequestService', 'SUMMARY_CONSTANTS','utilsService'];

    function summaryService($q, summaryRequestService, summaryCONST, utilsService) {
        var START_POS_DISTRIBUTION = 2;

        var service = {
            getSummary: getSummary,
            getNamespaces: getNamespaces
        };

        return service;

        function getSummary(summary, namespacedListNames) {
            var defer = $q.defer();
            summaryRequestService.getSummary(namespacedListNames)
                .then(function (data) {
                    data.defaultServer.traffic = summaryCONST().TRAFFIC.DEFAULT_SERVER;
                    setPosAndTrafficDistribution(data);
                    setPosRule(data);
                    summary.push(data)
                    defer.resolve(summary);
                }, function (reason) {
                    defer.reject({message: 'Can\'t load data' + reason});
                });

            return defer.promise;
        }


        function getNamespaces(baseNamespacesData) {
            var defer = $q.defer();
            summaryRequestService.getNamespaces()
                .then(function (data) {
                    var namespaceObject = data;
                    if (utilsService.isNullOrUndefined(namespaceObject) || utilsService.isNullOrUndefined(namespaceObject.namespace) || namespaceObject.namespace === '') {
                        defer.reject({'message': 'WS returned empty namespaced list data', status: 200});
                        return defer.promise;
                    }
                    var namespacesFromBackend = namespaceObject;
                    baseNamespacesData.namespace.splice(0, namespacesFromBackend.namespace.length);
                    for (var i = 0; i < namespacesFromBackend.namespace.length; i++) {
                        baseNamespacesData.namespace.push(namespacesFromBackend.namespace[i].name);
                    }
                    defer.resolve(baseNamespacesData);
                }, function (reason) {
                    defer.reject({'message': reason});
                });
            return defer.promise;
        }


        function setPosAndTrafficDistribution(summary) {
            for (var i = 0; i < summary.distributions.length; i++) {
                summary.distributions[i].traffic = summaryCONST().TRAFFIC.DISTRIBUTIONS;
                summary.distributions[i].pos = i + START_POS_DISTRIBUTION;
            }
        }

        function setPosRule(summary) {
            var endPosDistributions = summary.distributions.length;
            for (var i = 0; i < summary.rules.length; i++) {
                summary.rules[i].pos = endPosDistributions + i + START_POS_DISTRIBUTION;
            }
        }
    }
})();
