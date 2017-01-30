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


angular.module('uxData').directive('tableSorter',
    function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                onClick: '&',
                predicate: '@'
            },
            templateUrl: "../uxData/views/tableSorter.html",
            controller: function ($scope, $element, $attrs, $transclude) {
                $scope.click = function () {
                    if (angular.isUndefined($scope.reverse)) {
                        $scope.reverse = true;
                    } else {
                        $scope.reverse = !$scope.reverse;
                    }
                    $scope.showUpArrow = !$scope.reverse;
                    $scope.showDownArrow = $scope.reverse;
                    $scope.onClick({predicate: $scope.reverse ? $scope.predicate : "-"+$scope.predicate, reverse: $scope.reverse});
                };

                $scope.showUpArrow = true;
                $scope.showDownArrow = true;
            }
        }
    });



