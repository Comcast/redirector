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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */


/**
 *
 * @author: Alexander Pletnev
 * Date: 4/24/14
 */


angular.module('uxData').directive('serverold',
    ['$parse', function ($parse) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '@',
                servicePaths: '=paths',
                editMode: '=editMode',
                values: '=',
                disabled: '=',
                showWhitelistedOption: '=',
                onlySimpleMode: '=onlySimpleMode',
                validationData: '=',
                showStacks: '=',
                showFlavors: '=',
                showWhitelistedCount: '=',
                onChanged: '&',
                showLabels: '&'
            },
            templateUrl: '../uxData/views/Server.html',
            controller: 'serverCtrl',
            link: function (scope, element, attrs) {
                scope.$watch('disabled', function () {
                    if (scope.disabled === true || scope.disabled === 'true') {
                        scope.disabled = true;
                    } else {
                        scope.disabled = false;
                    }
                });

                //convert from text 'false/true' to boolean
                if (angular.isDefined(attrs.showLabels)) {
                    scope.showLabels = $parse(attrs.showLabels)(scope);
                } else {
                    scope.showLabels = true;
                }

                scope.$watch('values', function () {
                    if (angular.isDefined(scope.values)) {
                        scope.setData();
                    }
                });
            }
        };
    }]);
