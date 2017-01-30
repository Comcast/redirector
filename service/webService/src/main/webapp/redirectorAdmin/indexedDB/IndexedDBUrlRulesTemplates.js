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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('IndexedDBUrlRulesTemplates', IndexedDBUrlRulesTemplates);

    IndexedDBUrlRulesTemplates.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource', 'utilsService'];

    function IndexedDBUrlRulesTemplates($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource, utils) {

        var STACKS = entityCONST().STACKS;
        var URLRULES = entityCONST().URLRULES;
        var TEMPLATEURLRULES = entityCONST().TEMPLATEURLRULES;
        var PENDINGCHANGES = entityCONST().PENDINGCHANGES;

        var service = {
            getAllUrlRuleTemplates: getAllUrlRuleTemplates,
            getUrlRuleTemplateById: getUrlRuleTemplateById,
            saveUrlRuleTemplate: saveUrlRuleTemplate,
            approveUrlRuleTemplate: approveUrlRuleTemplate,
            deleteUrlRuleTemplate: deleteUrlRuleTemplate,
            cancelUrlRuleTemplate: cancelUrlRuleTemplate
        };

        return service;

        function getAllUrlRuleTemplates(appName) {
            var deferred = $q.defer();

            indexedDR.getAll(TEMPLATEURLRULES, appName)
                .then(function(rules){
                    deferred.resolve({if: rules});
                }, function(error){
                    deferred.reject(error);
                }
            );

            return deferred.promise;
        }

        function getUrlRuleTemplateById(appName, id) {
            return indexedDR.get(appName, TEMPLATEURLRULES, id);
        }

        function saveUrlRuleTemplate(appName, rule, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, STACKS, TEMPLATEURLRULES])
                .then(function (data) {
                    data.snapshot.entityToSave = {if: angular.fromJson(rule)};
                    webServiceDataSource.saveTemplateUrlRuleOffline(appName, ruleId, data.snapshot)
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

        function deleteUrlRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();

            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, TEMPLATEURLRULES, URLRULES])
                .then(function (data) {
                    webServiceDataSource.deleteTemplateUrlRuleOffline(appName, ruleId, data.snapshot)
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

        function approveUrlRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES, TEMPLATEURLRULES])
                .then(function (data) {
                    webServiceDataSource.approveTemplateUrlRuleOffline(appName, ruleId, data.snapshot)
                        .then(function (result) {
                            indexedDBCommon.saveApprovedEntity(appName, TEMPLATEURLRULES, result.if, result.pendingChanges, ruleId)
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

        function cancelUrlRuleTemplate(appName, ruleId) {
            var deferred = $q.defer();
            indexedDBCommon.getSnapshot(appName, [PENDINGCHANGES])
                .then(function (data) {
                    webServiceDataSource.cancelUrlRuleTemplateOffline(appName, ruleId, data.snapshot)
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
