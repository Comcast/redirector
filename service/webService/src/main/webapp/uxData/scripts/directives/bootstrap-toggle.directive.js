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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

angular.module('uxData') .directive('toggleButton', function () {
    return {
        restrict: 'A',
        transclude: true,
        replace: false,
        require: 'ngModel',
        link: function ($scope, $element, $attr, require) {
            var ngModel = require;
            var updateModelFromElement = function () {
                // If modified
                var checked = $element.prop('checked');
                if (checked != ngModel.$viewValue) {
                    // Update ngModel
                    ngModel.$setViewValue(checked);
                    $scope.$apply();
                }
            };

            var updateElementFromModel = function () {
                // Update button state to match model
                $element.trigger('change');
            };

            $element.on('change', function () {
                updateModelFromElement();
            });

            $scope.$watch(function () {
                return ngModel.$viewValue;
            }, function () {
                updateElementFromModel();
            });

            $element.bootstrapToggle();
        }
    };
});
