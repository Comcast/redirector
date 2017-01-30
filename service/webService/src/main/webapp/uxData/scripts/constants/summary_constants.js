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

    angular.module('uxData.constants').constant('SUMMARY_CONSTANTS', constants);

    function constants() {

        var TRAFFIC = {
            DEFAULT_SERVER: 'Customer',
            DISTRIBUTIONS: 'Customer/Internal (Maintenance)/Prod Preview',
            RULES: 'Feature'
        };

        var NAMESPACED_DEFAULT_NAMES =  {
            INTERNAL_MAINTENANCE: 'Internal_Maintenance',
            PRODUCTION_PREVIEW_LIST_2: 'Production_Preview_List_2',
            PRODUCTION_PREVIEW_LIST: 'Production_Preview_List',
            INTERNAL_FEATURE: 'Internal_Feature'
        };

        var result = {
            TRAFFIC: TRAFFIC,
            NAMESPACED_DEFAULT_NAMES: NAMESPACED_DEFAULT_NAMES

        };

        return result;
    }
})();
