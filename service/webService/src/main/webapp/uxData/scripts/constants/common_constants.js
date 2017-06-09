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

    angular
        .module('uxData.constants')
        .constant('COMMON_CONSTANTS', constants);

    function constants() {

        var APPLICATIONS = {
            DEFAULT: 'xreGuide',
            DECIDER: 'decider'
        };

        var DEVELOP_PATH = {
            REDIRECTOR: 'dev',
            DECIDER: 'devDecider'
        };

        var PROD_PATH = {
            REDIRECTOR: 'admin',
            DECIDER: 'decider'
        };

        var PENDING_CHANGE_TYPE = {
            ADD : 'ADD',
            UPDATE : 'UPDATE',
            DELETE : 'DELETE'
        };

        var PENDING_CHANGES_ENTITY_TYPE = {
            URL_PARAMS: 'urlParams',
            DEFAULT_URL_PARAMS: 'default'
        };

        var result = {
            PENDING_CHANGE_TYPE : PENDING_CHANGE_TYPE,
            PENDING_CHANGES_ENTITY_TYPE: PENDING_CHANGES_ENTITY_TYPE,
            APPLICATIONS: APPLICATIONS,
            PROD_PATH: PROD_PATH,
            DEVELOP_PATH: DEVELOP_PATH
        };

        return result;
    }
})();
