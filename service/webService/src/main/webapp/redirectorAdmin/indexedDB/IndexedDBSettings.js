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
        .factory('IndexedDBSettings', IndexedDBSettings);

    IndexedDBSettings.$inject = ['$q', 'IndexedDBCommon', 'IndexedDB_CONSTANTS', 'DataRequesterSimple'];

    function IndexedDBSettings($q, indexedDBCommon, entityCONST, simpleDR) {

        var REDIRECTORCONFIG = entityCONST().REDIRECTORCONFIG;

        var service = {
            getRedirectorConfig: getRedirectorConfig,
            saveRedirectorConfig: saveRedirectorConfig
        };

        return service;

        function getRedirectorConfig() {
            var deferred = $q.defer();
            indexedDBCommon.getEntityByAppNameAndType('', REDIRECTORCONFIG)
                .then(function(data) {
                    deferred.resolve(data[REDIRECTORCONFIG]);
                }, function(error) {
                    deferred.reject(error);
                }
            );
            return deferred.promise;
        }

        function saveRedirectorConfig(redirectorConfig) {
            return simpleDR.saveByEntityName(REDIRECTORCONFIG, REDIRECTORCONFIG, redirectorConfig);
        }
    }
})();
