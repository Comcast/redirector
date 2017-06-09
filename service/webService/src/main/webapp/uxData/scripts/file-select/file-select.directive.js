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

(function () {
    'use strict';

    angular.module('uxData').directive('fileSelect', fileSelect);
    function fileSelect() {
        return {
            restrict: 'E',
            scope: {
                onChange: '=',
                limit: '='
            },
            link: function (scope, el) {
                //ToDo: use jquery selectors or something similar
                var fileInputEl = el.find('input')[1];// input (type=file) is used to choose files
                fileInputEl.onchange = function (e) {
                    scope.onChange((e.srcElement || e.target).files[0]);
                    el.find('input')[0].value = fileInputEl.value;// input (type=text) is used to display filename
                };
            },
            templateUrl: '../uxData/scripts/file-select/file-select.directive.html'
        };
    }
})();
