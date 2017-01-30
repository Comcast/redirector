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
        .module('uxData.services')
        .factory('urlRulesBuilderService', urlRulesBuilderService);

    urlRulesBuilderService.$inject = ['utilsService'];

    function urlRulesBuilderService(utils) {

        var service = {
            completeRuleUnmarshalling: completeRuleUnmarshalling,
            buildReturnStatement: buildUrlParams,
            prepareReturnForMarshalling: prepareUrlParamsForMarshalling,
            objectToXml: objectToXml,
            getRuleReturnSimpleText: getRuleReturnSimpleText,
            getRuleReturnDiffText: getRuleReturnDiffText,
            buildRuleObjectForPreview: buildRuleObjectForPreview
        };

        return service;

        function completeRuleUnmarshalling(jsonRule, rawRule, rule) {
            rule.templateName = jsonRule.templateName;
            return rule;
        }

        function buildUrlParams(returnStatement) {
            return returnStatement;
        }

        function prepareUrlParamsForMarshalling(urlParams) {
            var urlParamsReturnStatement = {
                'return': {
                    urlRule: {}
                }
            };
            // ignore string fields if they are empty or undefined
            if (!utils.isEmptyString(utils.getString(urlParams.protocol))) {
                urlParamsReturnStatement.return.urlRule.protocol = urlParams.protocol;
            }
            if (!utils.isEmptyString(utils.getString(urlParams.urn))) {
                urlParamsReturnStatement.return.urlRule.urn = urlParams.urn;
            }

            // ignore integer fields if they are empty or equal 0
            if (!(utils.isEmptyString(utils.getString(urlParams.port)) ||
                angular.equals(utils.getString(urlParams.port), '0'))) {
                urlParamsReturnStatement.return.urlRule.port = urlParams.port;
            }
            if (!(utils.isEmptyString(utils.getString(urlParams.ipProtocolVersion)) ||
                angular.equals(utils.getString(urlParams.ipProtocolVersion), '0'))) {
                urlParamsReturnStatement.return.urlRule.ipProtocolVersion = urlParams.ipProtocolVersion;
            }
            return urlParamsReturnStatement;
        }

        function objectToXml (rulesObject) {
            return utils.objectToXml(getUrlRuleWithoutEmptyUrlParams(rulesObject.data.rule));
        }

        function getUrlRuleWithoutEmptyUrlParams (rule) {
            var ruleObject = angular.copy(rule);
            if (angular.isDefined(ruleObject.return) && angular.isDefined(ruleObject.return.urlRule)) {
                var returnStatement = ruleObject.return.urlRule;
                if (angular.isDefined(returnStatement.protocol) &&
                    utils.isEmptyString(utils.getString(returnStatement.protocol))) {
                    delete returnStatement.protocol;
                }
                if (angular.isDefined(returnStatement.port) &&
                    (utils.isEmptyString(utils.getString(returnStatement.port)) ||
                    angular.equals(utils.getString(returnStatement.port), '0'))) {
                    delete returnStatement.port;
                }
                if (angular.isDefined(returnStatement.urn) &&
                    utils.isEmptyString(utils.getString(returnStatement.urn))) {
                    delete returnStatement.urn;
                }
                if (angular.isDefined(returnStatement.ipProtocolVersion) &&
                    (utils.isEmptyString(utils.getString(returnStatement.ipProtocolVersion)) ||
                    angular.equals(utils.getString(returnStatement.ipProtocolVersion), '0'))) {
                    delete returnStatement.ipProtocolVersion;
                }
            }
            return ruleObject;
        }

        function getRuleReturnSimpleText(urlParams) {
            var simpleReturn = '';
            if (!utils.isEmptyString(utils.getString(urlParams.protocol))) {
                simpleReturn += 'protocol: ' + urlParams.protocol + '; ';
            }
            if (!(utils.isEmptyString(utils.getString(urlParams.port))
                || angular.equals(utils.getString(urlParams.port), '0'))) {
                simpleReturn += 'port: ' + urlParams.port + '; ';
            }
            if (!utils.isEmptyString(utils.getString(urlParams.urn))) {
                simpleReturn += 'urn: ' + urlParams.urn + ';';
            }
            if (!(utils.isEmptyString(utils.getString(urlParams.ipProtocolVersion)) ||
                angular.equals(utils.getString(urlParams.ipProtocolVersion), '0'))) {
                simpleReturn += 'ipProtocolVersion: ' + urlParams.ipProtocolVersion + ';';
            }

            return '\nRETURN [ ' + simpleReturn + ' ]';

        }

        function getRuleReturnDiffText(urlParams) {
            var urlRuleTextReturn = '';
            if (angular.isDefined(urlParams.protocol)) {
                urlRuleTextReturn += '\n\t\tprotocol: ' + urlParams.protocol + ';';
            }
            if (angular.isDefined(urlParams.port)) {
                urlRuleTextReturn += '\n\t\tport: ' + urlParams.port + ';';
            }
            if (angular.isDefined(urlParams.urn)) {
                urlRuleTextReturn += '\n\t\turn: ' + urlParams.urn + ';';
            }
            if (angular.isDefined(urlParams.ipProtocolVersion)) {
                urlRuleTextReturn += '\n\t\tipProtocolVersion: ' + urlParams.ipProtocolVersion + ';';
            }
            return '\nRETURN\n\t[ ' + urlRuleTextReturn + '\n\t]';
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
