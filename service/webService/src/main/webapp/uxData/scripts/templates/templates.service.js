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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.templates')
        .factory('templatesService', templatesService);

    templatesService.$inject = ['$rootScope', '$q', 'dialogs', 'rulesRequestService', 'templatesRequestsService', 'changesRequestsService', 'utilsService', 'RULES_CONSTANTS'];
    function templatesService($rootScope, $q, dialogs, rulesRequestService, templatesRequestsService, changesRequestsService, utils, RULES_CONSTANTS) {

        var service = {
            getAll: getAllTemplates,
            deleteTemplate: deleteTemplate,
            loadTemplateRule: loadTemplateRule,
            loadTemplateUrlRule: loadTemplateUrlRule,
            loadAllTemplateIds: loadAllTemplateIds,
            saveTemplate: saveTemplate
        };

        return service;


        function checkForConflict(status) {
            if (status === 409) {
                dialogs.error('Can\'t delete template', 'Template is used in rules.');
            }
            return false;
        }


        function getAllTemplates(name) {
            var defer = $q.defer();
            var result = {};

            var promises = [];
            switch (name) {
                case RULES_CONSTANTS().TEMPLATES_TYPE.PATH:
                    promises.push($q.when(loadTemplateRule()));
                    break;
                case RULES_CONSTANTS().TEMPLATES_TYPE.URL:
                    promises.push($q.when(loadTemplateUrlRule()));
                    break;
                default:
                    defer.reject('You should provide a template name (rules or urlRules)');
                    return defer.promise;
            }

            promises.push($q.when(loadPendingChanges()));
            promises.push($q.when(loadServicePaths()));

            $q.allSettled(promises).then(function (results) {
                angular.forEach(results, function (resultPromise, index) {
                    angular.extend(result, resultPromise.value);
                });
                $rootScope.isSaving = false;
                defer.resolve(result);
            }, function (reason) {
                $rootScope.isSaving = false;
                defer.reject(reason);
            });
            return defer.promise;
        }

        function loadTemplateRule() {
            var defer = $q.defer();
            templatesRequestsService.getTemplateRules().then(function (data) {
                    defer.resolve({templatePathRules: data});
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function loadTemplateUrlRule() {
            var defer = $q.defer();
            templatesRequestsService.getTemplateUrlRules().then(function (data) {
                    defer.resolve({templateUrlPathRules: data});
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }

        function loadAllTemplateIds(name) {
            var result = {};
            var defer = $q.defer();
            var promises = [];

            promises.push($q.when(getAllApprovedTemplateIds(name)));
            promises.push($q.when(getAllNotApprovedTemplateIds(name)));

            $q.all(promises).then(
                function (results) {
                    angular.forEach(results, function (data, index) {
                        angular.extend(result, data);
                    });
                    defer.resolve(result);
                }, function (reason) {
                    defer.reject({message: 'Failed to load rules IDs', data: reason});

                });
            return defer.promise;
        }

        function getAllApprovedTemplateIds(name) {
            var defer = $q.defer();

            var idsRequest = null;

            switch (name) {
                case RULES_CONSTANTS().TEMPLATES_TYPE.PATH:
                    idsRequest = templatesRequestsService.getTemplateRulesIds();
                    break;
                case RULES_CONSTANTS().TEMPLATES_TYPE.URL:
                    idsRequest = templatesRequestsService.getTemplateUrlRulesIds();
                    break;
                default:
                    defer.reject('You should provide a template name (rules or urlRules)');
                    return defer.promise;
            }

            idsRequest.then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve(ruleIdsMap);
                }, function (reason) {
                    defer.reject({message: 'Failed to load existing approved rule names. ' + reason});
                }
            );
            return defer.promise;
        }

        function getAllNotApprovedTemplateIds(name) {
            var defer = $q.defer();
            changesRequestsService.getUnapprovedRulesIds(name)
                .then(function (data) {
                    var ruleIdsMap = {};
                    angular.forEach(data.id, function (id, index) {
                        if (!utils.isEmptyString(id)) {
                            ruleIdsMap[id] = id;
                        }
                    });
                    defer.resolve(ruleIdsMap);
                }, function (reason) {
                    defer.reject({message: 'Failed to load existing rule names of pending changes. ' + reason});
                }
            );
            return defer.promise;
        }

        //===================================== private methods =====================================//
        function loadServicePaths() {
            var defer = $q.defer();
            rulesRequestService.getServicePaths()
                .then(function (result) {
                    defer.resolve({servicePaths: result});
                    //$log.info('Service paths successfully loaded');
                }, function (reason) {
                    defer.reject({message: 'Failed to load service paths: ' + reason.message});
                });
            return defer.promise;
        }


        function loadPendingChanges() {
            var defer = $q.defer();
            changesRequestsService.getPendingChangesJson().then(function (data) {
                    defer.resolve({pending: data});
                }, function (reason) {
                    defer.reject(reason);
                }
            );
            return defer.promise;
        }


        function deleteTemplate(name, ruleId) {
            var defer = $q.defer();
            var promise;
            switch (name) {
                case RULES_CONSTANTS().TEMPLATES_TYPE.PATH:
                    promise = templatesRequestsService.deleteTemplateRule(ruleId);
                    break;
                case RULES_CONSTANTS().TEMPLATES_TYPE.URL:
                    promise = templatesRequestsService.deleteTemplateUrlRule(ruleId);
                    break;
                default:
                    defer.reject('You should provide a template name (rules or urlRules)');
                    return defer.promise;
            }

            promise.then(function () {
                    defer.resolve();
                }, function (status, reason) {
                    checkForConflict(status);
                    if (angular.isUndefined(reason)){
                        defer.reject(status)
                    }
                    else {
                        defer.reject(reason);
                    }
                }
            );
            return defer.promise;
        }

        function saveTemplate(type, ruleObj, ruleId) {
            var defer = $q.defer();
            var promise;
            switch (type) {
                case RULES_CONSTANTS().TEMPLATES_TYPE.PATH:
                    promise = templatesRequestsService.saveTemplateRule(ruleObj, ruleId);
                    break;
                case RULES_CONSTANTS().TEMPLATES_TYPE.URL:
                    promise = templatesRequestsService.saveTemplateUrlRule(ruleObj, ruleId);
                    break;
                default:
                    defer.reject('You should provide a template type ("templatePathRules" or "saveTemplateRule")');
                    return defer.promise;
            }

            promise.then(function (data) {
                    defer.resolve(data);
                }, function (reason) {
                    defer.reject({message: reason});
                }
            );
            return defer.promise;
        }
    }
})();
