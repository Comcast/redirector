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
 */
(function () {
    'use strict';
    angular.module('uxData.services')
        .factory('utilsService', UtilsService);

    UtilsService.$inject = ['$rootScope', '$document'];

    function UtilsService($rootScope, $document) {

        var x2js = new X2JS({escapeMode: false});

        //Convert object to array
        var toArray = function (value) {
            if (angular.isDefined(value)) {
                if (!angular.isArray(value)) {
                    value = [value];
                }
            } else {
                value = [];
            }
            return value;
        };

        var valuesArray = function (map){
            var values = [];
            angular.forEach(map, function(key, value){
                values.push(key);
            });
            return values;
        };

        var xmlToJson = function (xml) {
            return x2js.xml_str2json(xml);
        };

        var objectToXml = function (object) {
            return x2js.json2xml_str(object);
        };

        var jsonObjToXml = function(jsonRule) {
            if (angular.isUndefined(jsonRule)) {
                return "";
            }
            return objectToXml(jsonRule);
        };

        var xmlToObject = function (xmlString) {
            return x2js.xml_str2json(xmlString);
        };

        var createUrl = function () {
            return '{protocol}://{host}:{port}/{urn}';
        };

        var parseServicePaths = function (data) {

            var servicePaths = [];

            if (typeof data.paths !== 'object') {
                return;
            }

            data.paths = toArray(data.paths);

            angular.forEach(data.paths, function (value, index) {
                var data = {'stacks': [], 'flavors': []};

                value.stack = toArray(value.stack);
                angular.forEach(value.stack, function (stackValue, index) {
                    data['stacks'].push({'nodes': stackValue.nodes, 'text': stackValue.value});
                });

                value.flavor = toArray(value.flavor);
                angular.forEach(value.flavor, function (flavorValue, index) {
                    data['flavors'].push({'nodes': flavorValue.nodes, 'text': flavorValue.value});
                });

                data['serviceName'] = value.serviceName;
                servicePaths.push(data);
            });

            return servicePaths;
        };

        var getFromCookies = function (key) {
            var cookies = $document[0].cookie && $document[0].cookie.split(';') || [];
            for (var i = 0; i < cookies.length; i++) {
                var thisCookie = cookies[i];
                while (thisCookie.charAt(0) === ' ') {
                    thisCookie = thisCookie.substring(1, thisCookie.length);
                }
                if (thisCookie.indexOf(key + '=') === 0) {
                    return decodeURIComponent(thisCookie.substring($rootScope.prefix.length + key.length + 1, thisCookie.length));
                }
            }
            return null;
        };

        var formatHostNameAndIP = function (input, template, notAvailablePlaceholders) {
            var hostname = '';
            var ip = '';
            var port = '';

            if (input.indexOf(':') >= 0) {
                hostname = input.substring(0, input.indexOf(':'));
                if ($.trim(hostname) === '') {
                    hostname = notAvailablePlaceholders.host;
                }
                ip = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
                if ($.trim(ip) === '') {
                    ip = notAvailablePlaceholders.ip;
                }
                port = input.substring(input.lastIndexOf(':') + 1);
            }

            return template.replace('{HOSTNAME}', hostname)
                .replace('{IP}', ip)
                .replace('{PORT}', port)
                .replace('{APP}', $rootScope.currentApplication);
        };


        var isEmptyString = function (stringToTest) {
            if (!$.isEmptyObject(stringToTest) && angular.isDefined(stringToTest) && angular.isString(stringToTest)) {
                return stringToTest.replace(/\s/g, '').length === 0;
            } else {
                return true;
            }
        };

        var isDefinedAndNotEmpty = function (object) {
            if (angular.isDefined(object) && (object !== null)) {
                var stringObject = object.toString().replace(/\s/g, '');
                return !$.isEmptyObject(stringObject);
            }

            return false;
        };

        var isEmptyObject = function (object) {
            return angular.isUndefined(object) || (angular.isDefined(object) && $.isEmptyObject(object));
        };

        var getString = function (str) {
            return (angular.isDefined(str) && str != null) ? str.toString() : '';
        };

        var arrayHasDuplicates = function (array) {
            var valuesSoFar = {};
            for (var i = 0; i < array.length; ++i) {
                var value = array[i];
                if (Object.prototype.hasOwnProperty.call(valuesSoFar, value)) {
                    return true;
                }
                valuesSoFar[value] = true;
            }
            return false;
        };

        /**
         *   This method checks if JS object (Map) is empty (it has only its prototype properties)
         */
        function isMapEmpty(object) {
            if (Object.getOwnPropertyNames(object).length === 0) {
                //is empty
                return true;
            }
            return false;
        }

        /**
         * Check if value not null and defined
         * @param value
         * @returns {boolean}
         */
        function isNullOrUndefined(value) {
            if (value == null || angular.isUndefined(value)) {
                return true;
            }
            return false;
        }

        var checkObjHasDeepPath = function(obj, keys) {
            // check that obj has deep path
            // for example keys=['child', 'childOfChild'] => check that obj.child.childOfChild exists
            var next = keys.shift();
            return obj.hasOwnProperty(next) && (!keys.length || checkObjHasDeepPath(obj[next], keys));
        };

        var getPendingChangesObjectById = function(pendingChanges, pendingTypeName, id) {
            if (checkObjHasDeepPath(pendingChanges, [pendingTypeName, 'entry']) &&
                angular.isArray(pendingChanges[pendingTypeName]['entry'])) {
                var entry = pendingChanges[pendingTypeName]['entry'];
                for (var i in entry) {
                    if (angular.equals(entry[i].key, id)) {
                        return entry[i].value;
                    }
                }
            }
            return undefined;
        };

        var getPendingChangesObjectAndIndexById = function(pendingChanges, pendingTypeName, id) {
            if (checkObjHasDeepPath(pendingChanges, [pendingTypeName, 'entry']) &&
                angular.isArray(pendingChanges[pendingTypeName]['entry'])) {
                var entry = pendingChanges[pendingTypeName]['entry'];
                for (var i in entry) {
                    if (angular.equals(entry[i].key, id)) {
                        return {change: entry[i].value, index: i};
                    }
                }
            }
            return undefined;
        };

        var stringContains = function (source, substring) {
            return source.indexOf(substring) > -1;
        };

        var hasPermissions = function(authService, permission) {
            var hasPermissions = true;
            for (var i = 1; i < arguments.length; i++) {
                if (!authService.isAuthorized(arguments[i])) {
                    hasPermissions = false;
                }
            }
            return hasPermissions;
        };

        var getPathFromStack = function(stack) {
            var match = stack.match(/^(\/(\w+)){2}/g);
            if(angular.isDefined(match)) {
                return match[0];
            }
            return match;
        };

        var getFlavorFromStack = function(stack) {
            var match = stack.match(/^(\/(\w+)){2}/g);
            if(angular.isDefined(match)) {
                return stack.substring(match[0].length + 1, stack.length);
            }
            return match;
        };

        /**
         * Check if any value in array of fields not null and defined
         * @param value
         * @returns {boolean}
         */
        function isNullOrUndefinedOrEmptyStringArrayOfValues(array) {
            var valid = false;
            valid = isNullOrUndefined(array);

            if (valid === false) {
                for (var i = 0; i < array.length; i++) {
                    valid = isNullOrUndefined(array[i]);
                    if (valid === true) {
                        break;
                    }
                }
            }

            return valid;
        }

        return {
            toArray: toArray,
            stringContains: stringContains,
            parseServicePaths: parseServicePaths,
            createUrl: createUrl,
            jsonObjToXml: jsonObjToXml,
            objectToXml: objectToXml,
            xmlToObject: xmlToObject,
            xmlToJson: xmlToJson,
            formatHostNameAndIP: formatHostNameAndIP,
            isEmptyObject: isEmptyObject,
            isEmptyString: isEmptyString,
            isDefinedAndNotEmpty: isDefinedAndNotEmpty,
            getString: getString,
            getFromCookies: getFromCookies,
            arrayHasDuplicates: arrayHasDuplicates,
            isNullOrUndefined: isNullOrUndefined,
            isMapEmpty: isMapEmpty,
            checkObjHasDeepPath: checkObjHasDeepPath,
            getPendingChangesObjectById: getPendingChangesObjectById,
            getPendingChangesObjectAndIndexById: getPendingChangesObjectAndIndexById,
            hasPermissions: hasPermissions,
            getPathFromStack: getPathFromStack,
            getFlavorFromStack: getFlavorFromStack,
            valuesArray: valuesArray,
            isNullOrUndefinedOrEmptyStringArrayOfValues: isNullOrUndefinedOrEmptyStringArrayOfValues
        };
    }
})();
