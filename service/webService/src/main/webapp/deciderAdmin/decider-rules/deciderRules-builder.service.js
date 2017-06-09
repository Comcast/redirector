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
        .module('uxData.services')
        .factory('deciderRulesBuilderService', deciderRulesBuilderService);

    deciderRulesBuilderService.$inject = ['utilsService'];

    function deciderRulesBuilderService(utils) {

        var service = {
            completeRuleUnmarshalling: completeRuleUnmarshalling,
            buildReturnStatement: buildPartners,
            prepareReturnForMarshalling: preparePartnersForMarshalling,
            objectToXml: objectToXml,
            getRuleReturnSimpleText: getRuleReturnSimpleText,
            getRuleReturnDiffText: getRuleReturnDiffText,
            buildRuleObjectForPreview: buildRuleObjectForPreview

        };

        return service;

        function completeRuleUnmarshalling() {
            // currently no additional specific decider rule initialization is needed
            // so just leaving empty function definition to avoid exception
        }

        function buildPartners(returnStatement) {
            return returnStatement.partner;
        }

        function preparePartnersForMarshalling(returnObj) {
            return {
                'return': {
                    partner: [returnObj.partner]
                }
            };
        }

        function objectToXml (ruleObject) {
            return utils.objectToXml(ruleObject.data.rule)
        }

        function getRuleReturnSimpleText(returnStatement) {
            return '\nRETURN [ Partner: ' + returnStatement + ' ]';
        }

        function getRuleReturnDiffText(returnStatement) {
            return '';
        }

        function buildRuleObjectForPreview(rawRule, jsonRule, servicePaths) {

            var ruleObject = {};
            ruleObject.params = rawRule.expressions;
            ruleObject.returnStatement = rawRule.returnStatement;
            ruleObject.data = {};
            ruleObject.data.rule = jsonRule;

            return ruleObject;
        }
    }
})();
