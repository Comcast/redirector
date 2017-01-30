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

angular.module('uxData').filter('findObjectByFirstOrderPropertyOrPath', function() {
    return function(items, field, fieldValue) {
        var PATH_FIELD = "path";
        if (angular.isUndefined(fieldValue) || fieldValue === null || fieldValue === "") {
            return items;
        }
        var filtered = [];
        angular.forEach(items, function(item) {
            filtered.push(item);
        });
        var found =[];
        var searchByPath = (field === PATH_FIELD);
        if (searchByPath) {
            angular.forEach(filtered, function (item) {
                for (var i = 0; i < item.path.length; i++) {
                    if (item.path[i].value.toLowerCase() == "".toLowerCase() && (item.url.toLowerCase().indexOf(fieldValue.toLowerCase()) > -1)) {
                        found.push(item);
                        break;
                    }
                    if (item.path[i].value.toLowerCase().indexOf(fieldValue.toLowerCase()) > -1) {
                        found.push(item);
                        break;
                    }
                }
            });
        } else {
            angular.forEach(filtered, function (item) {
                if (item[field].toLowerCase().indexOf(fieldValue.toLowerCase()) > -1) {
                    found.push(item);
                }
            });
        }

        return found;
    };
});
