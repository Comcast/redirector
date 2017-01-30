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


angular.module('uxData').controller('RulePreviewController', ['$scope', '$rootScope', 'utilsService', 'authService', 'USER_PERMISSIONS', 'requestsService',  'templatesRequestsService', 'deciderRulesRequestService', 'diffService',
    function RulePreviewController($scope, $rootScope, utils, authService, USER_PERMISSIONS, requestsService, templatesRequestsService, deciderRulesRequestService, diffService) {

        $scope.VIEW_MODES = {
            SIMPLE: 'simple',
            XML: 'xml',
            DIFF_XML: 'diffXml'
        };

        $scope.ruleId = '';
        $scope.viewMode = $scope.VIEW_MODES.SIMPLE;
        $scope.authService = authService;
        $scope.USER_PERMISSIONS = USER_PERMISSIONS;

        if (angular.isDefined($scope.data.id) && !$.isEmptyObject($scope.data.id)) {
            $scope.ruleId = $scope.data.id;
        }

        $scope.onEditRule = function () {
            $scope.onRuleEdit({ruleId: $scope.ruleId});
        };

        $scope.onShowSimple = function () {
            $scope.viewMode = $scope.VIEW_MODES.SIMPLE;
        };

        $scope.onShowXML = function () {
            $scope.viewMode = $scope.VIEW_MODES.XML;
        };

        $scope.onShowDiff = function () {
            $scope.viewMode = $scope.VIEW_MODES.DIFF_XML;
        };

        $scope.onDeleteRule = function () {
            $scope.onRuleDelete({ruleId: $scope.ruleId});
        };

        $scope.isSimpleMode = function (data, i) {
            return data.serverEditMode[i] === 'simple';
        };

        $scope.hideDiffButton = function () {
            return !$scope.data.changeType || $scope.viewMode === $scope.VIEW_MODES.DIFF_XML
                || !$scope.hasChanges($scope.data.diffRuleCurrentText, $scope.data.diffRuleChangedText);
        };

        $scope.isEditable = function () {
            return angular.isDefined($scope.editable) && !$scope.editable ? false : true;
        };

        $scope.exportRule = function (item, type) {
            switch (type) {
                case 'rule':
                {
                    if (!$scope.template) {
                        requestsService.exportRule($rootScope.currentApplication, item);
                    } else {
                        templatesRequestsService.exportTemplate('templates/rules', $rootScope.currentApplication, item);
                    }
                    break;
                }
                case 'urlRule':
                {
                    if (!$scope.template) {
                        requestsService.exportUrlRule($rootScope.currentApplication, item);
                    } else {
                        templatesRequestsService.exportTemplate('templates/urlRules', $rootScope.currentApplication, item);
                    }
                    break;
                }
                case 'deciderRule':
                {
                    deciderRulesRequestService.exportRule(item);
                    break;
                }
                default:
                {
                    break;
                }
            }
        };

        $scope.hasAdvancedServers = function () {
            if (!angular.isDefined($scope.data.serverEditMode)) {
                return false;
            }
            $scope.advancedServersCount = 0;
            for (var i = 0; i < $scope.data.serverEditMode.length; i++) {
                if ($scope.data.serverEditMode[i] === 'advanced') {
                    $scope.advancedServersCount++;
                }
            }
            return $scope.advancedServersCount > 0;
        };

        var getText = function (textObject) {
            return angular.isDefined(textObject) ? textObject : '';
        };

        $scope.getDiffs = function (text1, text2, resultSide) {
            var config = {
                ignoreLeadingWS: true,
                showAllContent: true,
                type: 'default'

            };
            return  diffService.formattedDiff(getText(text1), getText(text2), resultSide, config);
        };

        $scope.hasChanges = function (text1, text2) {
            return !angular.equals(getText(text1), getText(text2));
        };

    }]);
