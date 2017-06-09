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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('IndexedDBRulesTemplates', IndexedDBRulesTemplates);

    IndexedDBRulesTemplates.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource', 'utilsService'];

    function IndexedDBRulesTemplates($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource, utils) {

        var STACKS = entityCONST().STACKS;
        var PATHRULES = entityCONST().PATHRULES;
        var TEMPLATEPATHRULES = entityCONST().TEMPLATEPATHRULES;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;

        var service = {
            getAllFlavorRuleTemplates: getAllFlavorRuleTemplates,
            getFlavorRuleTemplateById: getFlavorRuleTemplateById,
            saveFlavorRuleTemplate: saveFlavorRuleTemplate,
            approveFlavorRuleTemplate: approveFlavorRuleTemplate,
            deleteFlavorRuleTemplate: deleteFlavorRuleTemplate,
            cancelFlavorRuleTemplate: cancelFlavorRuleTemplate
        };

        return service;

        function getAllFlavorRuleTemplates(appName) {
            var deferred = $q.defer();

            indexedDR.getAll(TEMPLATEPATHRULES, appName)
                .then(function(rules){
                    deferred.resolve({if: rules});
                }, function(error){
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function getFlavorRuleTemplateById(appName, id) {
            return indexedDR.get(appName, TEMPLATEPATHRULES, id);
        }

        function saveFlavorRuleTemplate(appName, rule, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, STACKS, TEMPLATEPATHRULES])
                .then(function (data) {
                    data.snapshot.entityToSave = {if: angular.fromJson(rule)};
                    webServiceDataSource.saveFlavorRuleTemplateOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            );
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function () {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function deleteFlavorRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, TEMPLATEPATHRULES, PATHRULES])
                .then(function (data) {
                    webServiceDataSource.deleteFlavorRuleTemplateOffline(appName, ruleId, data.snapshot)
                        .then(function(result){
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function(){
                                    deferred.resolve();
                                }, function(error){
                                    deferred.reject(error);
                                }
                            );
                        }, function(error) {
                            deferred.reject(error);
                        }
                    );
                }, function () {
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function approveFlavorRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.approveFlavorRuleTemplateOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, TEMPLATEPATHRULES, result.if, result.pendingChanges, ruleId)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            )
                        }, function (error) {
                            deferred.reject(error);
                        }
                    )
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function cancelFlavorRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelFlavorRuleTemplateOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            simpleDR.saveByAppName(PENDINGCHANGES, appName, result.pendingChanges)
                                .then(function () {
                                    deferred.resolve();
                                }, function (error) {
                                    deferred.reject(error);
                                }
                            )
                        }, function (error) {
                            deferred.reject(error);
                        }
                    );
                }, function (error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }
    }

})();
