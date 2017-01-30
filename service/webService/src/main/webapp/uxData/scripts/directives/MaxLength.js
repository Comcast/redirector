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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */


/**
 * AngularJS v1.3.0-beta.13 has issue for maxlengthDirective for Number type as value.
 * (ngMaxlength, maxlength)
 */
var maxlengthDirective = function () {
    return {
        require: '?ngModel',
        priority: 101,
        link: function (scope, elm, attr, ctrl) {
            if (!ctrl) return;

            var maxlength = 0;
            attr.$observe('maxlength', function (value) {
                maxlength = int(value) || 0;
                ctrl.$validate();
            });
            ctrl.$validators.maxlength = function (value, viewValue) {
                return ctrl.$isEmpty(viewValue) || viewValue.length <= maxlength;
            };
        }
    };
};

function int(str) {
    return parseInt(str, 10);
}


angular.module('uxData')
    .directive('ngMaxlength', maxlengthDirective)
    .directive('maxlength', maxlengthDirective);


