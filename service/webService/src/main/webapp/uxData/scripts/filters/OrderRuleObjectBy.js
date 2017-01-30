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



angular.module('uxData').filter('orderRuleObjectBy', function() {
    return function(items, field, reverse) {
        var filtered = [];
        angular.forEach(items, function(item) {
            filtered.push(item);
        });
        var sortByPath = (field === "path");
        if (sortByPath) {
            filtered.sort(function (a, b) {
                return (a.path[0].value.toUpperCase() > b.path[0].value.toUpperCase() ? 1 : -1);
            });
        }else {
            filtered.sort(function (a, b) {
                return (a[field].toUpperCase() > b[field].toUpperCase() ? 1 : -1);
            });
        }
        if(reverse) filtered.reverse();
        return filtered;
    };
});
