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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

(function () {
    'use strict';

    angular.module('uxData.constants').constant('REGEXP_CONSTANTS', regexpConstants);

    function regexpConstants() {
        var numericPattern = new RegExp('^[-]?[0-9]+([.]{1}[0-9]+)?$');
        var versionPattern = new RegExp('^([0-9a-zA-Z]+([-]?[0-9a-zA-Z]+)*)+([.]{1}[0-9a-zA-Z]+([0-9a-zA-Z-]+[0-9a-zA-Z]+)?)*$');
        var urlPattern = new RegExp('^([a-z]+){1}(:\\/\\/)([\\w\\-]+(\\.{1})?)+(:([1-9]){1}(([0-9]){1,4})?)?(\\/[\\w~!@#$%^&*()_+-=\\{\\}\\[\\]\']+)*$');
        var urlIPv6Pattern = new RegExp('^([a-z]+){1}(:\\/\\/)\\[[a-fA-F0-9:]*\\](:([1-9]){1}(([0-9]){1,4})?)?(\\/[\\w~!@#$%^&*()_+-=\\{\\}\\[\\]\']+)*$');
        var result = {
            alphabetical: /^[a-zA-Z]*$/,
            nonEmptyAlphabetical: /^[a-zA-Z]+$/,
            numerical: /^\d+$/,
            percent: /^(?:100|\d{1,2})(?:\.\d{1,2})?$/,
            alphaNumericalWithUnderscores: /^[a-zA-z0-9_]+$/,
            alphaNumericalWithUnderscoresDotsAndColons: /^[a-zA-Z0-9.:_]+$/,
            alphaNumericalWithUnderscoresAndSpaces: /^[a-zA-z0-9_ ]+$/,
            ipv4: /^0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])$/,
            //ipv6 validates using ipv6.js
            stackName: /^\/[a-zA-Z0-9._-]+\/[a-zA-Z0-9._-]+$/,
            numericPattern: numericPattern,
            versionPattern: versionPattern,
            urlPattern: urlPattern,
            urlIPv6Pattern: urlIPv6Pattern,
            urnPattern: /^[a-zA-Z0-9-_;\.]+$/,
            portPattern: /^[0-9]{1,5}$/
        };
        return result;
    }
})();
