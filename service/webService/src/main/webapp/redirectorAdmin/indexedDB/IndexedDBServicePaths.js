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
        .factory('IndexedDBServicePaths', IndexedDBServicePaths);

    IndexedDBServicePaths.$inject = ['$q', 'DataRequesterIndexed', 'DataRequesterSimple', 'IndexedDB_CONSTANTS', 'IndexedDBCommon',
        'WebServiceDataSource'];

    function IndexedDBServicePaths($q, indexedDR, simpleDR, entityCONST, indexedDBCommon, webServiceDataSource) {

        var STACKS = entityCONST().STACKS;

        var service = {
            getServicePaths: getServicePaths
        };

        return service;

        function getServicePaths(appName) {
            return simpleDR.get(STACKS, appName);
        }
    }
})();
