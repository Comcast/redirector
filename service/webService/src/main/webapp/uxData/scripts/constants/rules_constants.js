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

    angular.module('uxData.constants').constant('RULES_CONSTANTS', constants);

    function constants() {

        var RULE_TYPE = {
            PATH: 'pathRules',
            URL: 'urlRules',
            DECIDER: 'deciderRules'
        };

        var RETURN_TYPE = {
            URL_PARAMS: 'urlParams'
        };

        var TEMPLATES_TYPE = {
            PATH: 'templatePathRules',
            URL: 'templateUrlPathRules'
        };

        var RULES_HASH_PATH = {
            PATH_RULES_SHOW: '#/flavorRules/showAll/',
            TEMPLATE_PATH_RULES_SHOW: '#/templates/' + TEMPLATES_TYPE.PATH + '/',
            URL_RULES_SHOW: '#/' + RULE_TYPE.URL + '/showAll/',
            TEMPLATE_URL_RULES_SHOW: '#/templates/' + TEMPLATES_TYPE.URL + '/',
            DECIDER_RULES_SHOW: '#/showDeciderRules/'
        }

        var result = {
            LEFT : 'left',
            RIGHT: 'right',
            OR: 'OR',
            XOR: 'XOR',
            HOST: '{host}',
            RULE_TYPE: RULE_TYPE,
            RETURN_TYPE: RETURN_TYPE,
            TEMPLATES_TYPE: TEMPLATES_TYPE,
            RULES_HASH_PATH: RULES_HASH_PATH
        };

        return result;
    }
})();
