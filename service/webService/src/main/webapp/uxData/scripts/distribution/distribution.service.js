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
        .module('uxData.distribution')
        .service('distributionService', distributionService);

    distributionService.$inject = ['$q', '$log', '$rootScope', 'constantsProvider', 'distributionRequestService', 'requestsService', 'distributionAlertsService'];

    /**
     * Encapsulates functions which are used by multiple controllers
     * Please always place local ang global declarations and functions in the appropriate sections
     * @constructor
     */
    function distributionService($q, $log, $rootScope, constantsProvider, distributionRequestService, requestsService, distributionAlertsService) {
        var service;
        var MAX_TOTAL_Percentage = 100;

        service = {
            MAX_TOTAL_Percentage: MAX_TOTAL_Percentage,
            validateAndSaveDistributions: validateAndSaveDistributions,
            getDistributionsWithChanges: getDistributionsWithChanges
        };

        $rootScope.isSaving = false;

        return service;

        function validateAndSaveDistributions(changedRules, defaultServer) {
            var defer = $q.defer();
            $log.info('trying to save distributions');
            $rootScope.isSaving = true;
            var rulesForSave = [];
            //merge data from diff
            for (var i = 0; i < changedRules.length; i++) {
                var rule = changedRules[i];
                if (rule !== null) {
                    if (rule.hasOwnProperty('changeType')) {
                        delete rule.changeType;
                    }
                    if (rule.hasOwnProperty('hasChanges')) {
                        delete rule.hasChanges;
                    }
                    if (rule.changeType !== constantsProvider.PENDING_CHANGE_TYPE.DELETE) {
                        rulesForSave.push(rule);
                    }
                }
            }

            var currentApplication = $rootScope.currentApplication;
            distributionRequestService.saveDistributionWithDefaultServer(currentApplication, angular.toJson(
                {
                distribution: {
                    'rule': rulesForSave
                }, defaultServer: defaultServer
            }))
                .then(function (status) {
                    $log.info('Distributions are saved, got response with status:', status);
                    defer.resolve(status);
                    $rootScope.isSaving = false;
                }, function (error) {
                    $log.error('Error saving distributions:', error);
                    defer.reject(error);
                    $rootScope.isSaving = false;
                });

            return defer.promise;
        }

        //GET DISTRIBUTION DATA
        function getDistributionsWithChanges(name) {
            $log.info('Trying to get distributions with changes');
            var defer = $q.defer();
            var resolvingObject = {
                changes: {},
                distribution: {}
            };
            requestsService.getPendingChangesJson(name)
                .then(function (data) {
                    $log.info('Got pending changes: ', data);
                    resolvingObject.changes = data;
                    distributionRequestService.getDistributions(name)
                        .then(function (data) {
                            $log.info('Got distributions: ', data);
                            resolvingObject.distribution = data;
                            defer.resolve(resolvingObject);
                        }, function (error) {
                            $log.error('Error getting distributions: ', error);
                            resolvingObject.distribution = error;
                            defer.reject(resolvingObject);
                        });
                }, function (error) {
                    $log.error('Error getting pending changes: ', error);
                    resolvingObject.changes = error;
                    defer.reject(resolvingObject);
                });

            return defer.promise;
        }
    }
})();
