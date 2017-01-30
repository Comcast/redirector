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

    angular
        .module('uxData.constants')
        .constant('EXPRESSION_CONSTANTS', constants);

    function constants() {

        var EXPRESSION_TYPE = {
            PATH_RULE_EXP: 'pathRuleExp',
            URL_RULE_EXP: 'urlRuleExp'
        };

        var VALUE_TYPE = {
            NONE: 'none',
            STRING: 'string',
            NUMERIC: 'numeric',
            VERSION: 'version',
            IPV6: 'ipv6'
        };

        var OPERATOR_TYPE = {
            EQUAL: 'equals',
            MATCHES: 'matches',
            NOTEQUAL: 'notEqual',
            CONTAINS: 'contains',            // Deprecated simple contains (without contains type) - use NAMESPACED_LIST instead
            IN_IP_RANGE: 'inIpRange',
            CONTAINS_NMSP: 'containsnmsp',
            IN_IP_RANGE_NMSP: 'inIpRangeList',
            LESSTHAN: 'lessThan',
            LESSOREQUAL: 'lessOrEqual',
            GREATERTHAN: 'greaterThan',
            GREATEROREQUAL: 'greaterOrEqual',
            ISEMPTY: 'isEmpty',
            PERCENT: 'percent',
            RANDOM: 'random',
            NAMESPACED_LIST: 'namespacedList'
        };

        var DEFAULT_TITLE_NAME = {
            IN_IP_RANGE : 'clientAddress' //according to APPDS-1554
        };

        var result = {
            EXPRESSION_TYPE: EXPRESSION_TYPE,
            OPERATOR_TYPE: OPERATOR_TYPE,
            VALUE_TYPE: VALUE_TYPE,
            DEFAULT_TITLE_NAME: DEFAULT_TITLE_NAME
        };

        return result;
    }
})();
