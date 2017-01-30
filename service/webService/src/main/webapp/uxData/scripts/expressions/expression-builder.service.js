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


(function() {
    'use strict';

    angular
        .module('uxData.expression')
        .factory('expressionBuilderService', expressionBuilderService);

    expressionBuilderService.$inject = ['EXPRESSION_CONSTANTS'];

    function expressionBuilderService (CONST) {

        var service = {
            buildOperators: buildOperators,
            buildValueTypes: buildValueTypes
        };

        return service;

        function buildOperators (expressionType) {
            var operators = {};
            if (angular.equals(CONST().EXPRESSION_TYPE.PATH_RULE_EXP, expressionType) || angular.isUndefined(expressionType)) {
                operators[CONST().OPERATOR_TYPE.EQUAL]             = {name: 'Equals',                   value: CONST().OPERATOR_TYPE.EQUAL};
                operators[CONST().OPERATOR_TYPE.MATCHES]           = {name: 'Matches',                  value: CONST().OPERATOR_TYPE.MATCHES};
                operators[CONST().OPERATOR_TYPE.NOTEQUAL]          = {name: 'Not Equals',               value: CONST().OPERATOR_TYPE.NOTEQUAL};
                operators[CONST().OPERATOR_TYPE.CONTAINS_NMSP]     = {name: 'Contains Namespaced List', value: CONST().OPERATOR_TYPE.CONTAINS_NMSP};
                operators[CONST().OPERATOR_TYPE.IN_IP_RANGE]       = {name: 'In IP Range',              value: CONST().OPERATOR_TYPE.IN_IP_RANGE};
                operators[CONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP]  = {name: 'In IP Range Namespaced',   value: CONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP};
                operators[CONST().OPERATOR_TYPE.LESSTHAN]          = {name: 'Less Than',                value: CONST().OPERATOR_TYPE.LESSTHAN};
                operators[CONST().OPERATOR_TYPE.LESSOREQUAL]       = {name: 'Less Than or Equal To',    value: CONST().OPERATOR_TYPE.LESSOREQUAL};
                operators[CONST().OPERATOR_TYPE.GREATERTHAN]       = {name: 'Greater Than',             value: CONST().OPERATOR_TYPE.GREATERTHAN};
                operators[CONST().OPERATOR_TYPE.GREATEROREQUAL]    = {name: 'Greater Than or Equal To', value: CONST().OPERATOR_TYPE.GREATEROREQUAL};
                operators[CONST().OPERATOR_TYPE.ISEMPTY]           = {name: 'Is Empty',                 value: CONST().OPERATOR_TYPE.ISEMPTY};
                operators[CONST().OPERATOR_TYPE.RANDOM]            = {name: 'Random',                   value: CONST().OPERATOR_TYPE.RANDOM};
            }
            else if (angular.equals(CONST().EXPRESSION_TYPE.URL_RULE_EXP, expressionType)) {
                operators[CONST().OPERATOR_TYPE.EQUAL]             = {name: 'Equals',                   value: CONST().OPERATOR_TYPE.EQUAL};
                operators[CONST().OPERATOR_TYPE.MATCHES]           = {name: 'Matches',                  value: CONST().OPERATOR_TYPE.MATCHES};
                operators[CONST().OPERATOR_TYPE.CONTAINS_NMSP]     = {name: 'Contains Namespaced List', value: CONST().OPERATOR_TYPE.CONTAINS_NMSP};
                operators[CONST().OPERATOR_TYPE.IN_IP_RANGE]       = {name: 'In IP Range',              value: CONST().OPERATOR_TYPE.IN_IP_RANGE};
                operators[CONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP]  = {name: 'In IP Range Namespaced',   value: CONST().OPERATOR_TYPE.IN_IP_RANGE_NMSP};
                operators[CONST().OPERATOR_TYPE.PERCENT]           = {name: 'Percent',                  value: CONST().OPERATOR_TYPE.PERCENT};
                operators[CONST().OPERATOR_TYPE.RANDOM]            = {name: 'Random',                   value: CONST().OPERATOR_TYPE.RANDOM};
            }
            return operators;
        }
        
        function buildValueTypes () {
            var valueTypes = {};
            valueTypes[CONST().VALUE_TYPE.NONE]      = {name : 'NONE',       value : CONST().VALUE_TYPE.NONE};
            valueTypes[CONST().VALUE_TYPE.STRING]    = {name : 'String',     value : CONST().VALUE_TYPE.STRING};
            valueTypes[CONST().VALUE_TYPE.NUMERIC]   = {name : 'Numeric',    value : CONST().VALUE_TYPE.NUMERIC};
            valueTypes[CONST().VALUE_TYPE.IPV6]      = {name : 'IPv6',       value : CONST().VALUE_TYPE.IPV6};
            valueTypes[CONST().VALUE_TYPE.VERSION]   = {name : 'Version / IPv4 address', value : CONST().VALUE_TYPE.VERSION};

            return valueTypes;
        }
    }
})();
