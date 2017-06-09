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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.templates')
        .factory('templatesRequestsService', templatesRequestsService);

    templatesRequestsService.$inject = ['$rootScope', 'requestsService'];

    function templatesRequestsService($rootScope, requestsService) {
        var service = {
            saveTemplateRule: saveTemplateRule,
            saveTemplateUrlRule: saveTemplateUrlRule,
            getTemplateRules: getTemplateRules,
            getTemplateRulesIds: getTemplateRulesIds,
            getTemplateUrlRules: getTemplateUrlRules,
            getTemplateUrlRulesIds: getTemplateUrlRulesIds,
            deleteTemplateRule: deleteTemplateRule,
            deleteTemplateUrlRule: deleteTemplateUrlRule,
            exportTemplate: exportTemplate

        };
        return service;


        function saveTemplateRule(ruleObj, ruleName) {
            return requestsService.saveTemplateFlavorRule($rootScope.currentApplication, ruleObj, ruleName);
        }

        function saveTemplateUrlRule(ruleObj, ruleName) {
            return requestsService.saveTemplateUrlRule($rootScope.currentApplication, ruleObj, ruleName);
        }

        function getTemplateRules() {
            return requestsService.getFlavorRulesTemplates($rootScope.currentApplication);
        }

        function getTemplateRulesIds() {
            return requestsService.getFlavorRuleTemplatesIds($rootScope.currentApplication);
        }

        function getTemplateUrlRules() {
            return requestsService.getUrlRulesTemplates($rootScope.currentApplication);
        }

        function getTemplateUrlRulesIds() {
            return requestsService.getUrlRuleTemplatesIds($rootScope.currentApplication);
        }


        function deleteTemplateRule(ruleId) {
            return requestsService.deleteTemplateFlavorRule($rootScope.currentApplication, ruleId);

        }

        function deleteTemplateUrlRule(ruleId) {
            return requestsService.deleteTemplateUrlRule($rootScope.currentApplication, ruleId);
        }

        function exportTemplate (pathToTemplateService, appName, ruleName) {
            window.open(requestsService.getBaseApiUrl() + pathToTemplateService + '/' + appName + '/export/' + ruleName);
        }
    }
})();
