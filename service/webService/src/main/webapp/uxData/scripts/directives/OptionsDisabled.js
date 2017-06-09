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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */


// Workaround for feature #638
// https://github.com/angular/angular.js/issues/638
angular.module('uxData').directive('optionsDisabled', ['$parse', '$timeout' ,function ($parse, $timeout) {
    var updateDisabled = function (scope, attr, element, data, fnDisableIfTrue) {
        $("option[value!='?']", element).each(function (i, e) {
            var locals = {};
            locals[attr] = data[i];
            $(this).attr('disabled', fnDisableIfTrue(scope, locals));
        });
    };
    var disableOptions = function (scope, attr, element, data, fnDisableIfTrue) {
        // refresh the disabled options in the select element.
        var options = $("option[value!='?']", element);
        if (options.length < data.length) {
            $timeout(function () {
                updateDisabled(scope, attr, element, data, fnDisableIfTrue);
            }, 10);
        }
        else {
            updateDisabled(scope, attr, element, data, fnDisableIfTrue);
        }
    };
    return {
        priority: 0,
        require: 'ngModel',
        link: function (scope, iElement, iAttrs, ctrl) {
            // parse expression and build array of disabled options
            var expElements = iAttrs.optionsDisabled.match(/^\s*(.+)\s+for\s+(.+)\s+in\s+(.+)?\s*/);
            var attrToWatch = expElements[3];
            var fnDisableIfTrue = $parse(expElements[1]);
            scope.$watch(attrToWatch, function (newValue, oldValue) {
                if (newValue) {
                    disableOptions(scope, expElements[2], iElement, newValue, fnDisableIfTrue);
                }
            }, true);
            // handle model updates properly
            scope.$watch(iAttrs.ngModel, function (newValue, oldValue) {
                var disOptions = $parse(attrToWatch)(scope);
                if (newValue) {
                    disableOptions(scope, expElements[2], iElement, disOptions, fnDisableIfTrue);
                }
            });
        }
    };
}]);
