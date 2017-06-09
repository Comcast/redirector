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

angular.module('uxData').directive('btnSwitch',
    function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            templateUrl: '../uxData/views/switcher.html',
            replace: true,
            link: function (scope, element, attrs, ngModel) {

                // Specify how UI should be updated
                ngModel.$render = function () {
                    render();
                };

                var render = function () {
                    var val = ngModel.$viewValue;
                    var open = angular.element(element.children()[0]);
                    open.removeClass(val ? 'hide' : 'show');
                    open.addClass(val ? 'show' : 'hide');

                    var closed = angular.element(element.children()[1]);
                    closed.removeClass(val ? 'show' : 'hide');
                    closed.addClass(val ? 'hide' : 'show');
                };

                // Listen for the button click event to enable binding
                element.bind('click', function (event) {
                    event.preventDefault();
                    event.stopPropagation();
                    scope.$apply(toggle);
                });

                // Toggle the model value
                function toggle() {
                    var val = ngModel.$viewValue;
                    ngModel.$setViewValue(!val);
                    render();
                }

                if (!ngModel) {
                    //TODO: Indicate that something is missing!
                    return;
                }  // do nothing if no ng-model

                // Initial render
                render();
            }
        };
});
