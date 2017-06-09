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
        .constant('TRAFFIC_CONSTANTS', constants);

    function constants() {

        var ADJUSTED_CALCULATION_MODE = {
            ADJUSTED_TRAFFIC : 'ADJUSTED_TRAFFIC',
            ADJUSTED_WEIGHT: 'ADJUSTED_WEIGHT'
        };

        var DISTRIBUTION_MODE = {
            CURRENT: 'CURRENT',
            NEXT: 'NEXT'
        };

        var HOSTS_MODE = {
            ONLY_ACTIVE_WHITELISTED: 'ONLY_ACTIVE_WHITELISTED',
            ALL_WHITELISTED: 'ALL_WHITELISTED'
        }

        var result = {
            ADJUSTED_CALCULATION_MODE : ADJUSTED_CALCULATION_MODE,
            DISTRIBUTION_MODE: DISTRIBUTION_MODE,
            HOSTS_MODE: HOSTS_MODE
        };

        return result;
    }
})();
