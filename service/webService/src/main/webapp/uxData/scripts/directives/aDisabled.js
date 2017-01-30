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
 * a-disabled attribute for <a> tag to disable link in form
 */
angular.module('uxData').directive('aDisabled', function () {
    return {
        restrict: 'A',
        priority: -99999,
        link: function (scope, element, attrs) {
            scope.$watch(attrs.aDisabled, function (val, oldval) {
                if (!!val) {
                    element.unbind('click');
                } else if (oldval) {
                    element.bind('click', function () {
                        scope.$apply(attrs.ngClick);
                    });
                }
            });
        }
    };
});
