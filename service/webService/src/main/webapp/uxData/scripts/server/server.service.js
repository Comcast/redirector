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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


(function() {
    'use strict';

    angular
        .module('uxData.server')
        .factory('serverService', serverService);

    serverService.$inject = [];

    function serverService () {

        var service = {
            getListOfPaths: getListOfPaths
        };

        return service;

        /**
         *
         * @param servicePaths
         * @param currentApp
         * @param showStacks flag which indicates whether to show stacks or not
         * @param showFlavors flag which indicates whether to show flavors or not
         * @returns {Array}
         */
        function getListOfPaths(servicePaths, currentApp, showStacks, showFlavors, showWhitelistedOnly) {
            var listOfPaths = [];

            if (!$.isEmptyObject(servicePaths)) {

                if (angular.isDefined(currentApp)) {
                    if (!angular.isArray(servicePaths.stack)) {
                        servicePaths.stack = [servicePaths.stack];
                    }

                    if (!angular.isArray(servicePaths.flavor)) {
                        servicePaths.flavor = [servicePaths.flavor];
                    }

                    if (angular.isUndefined(showStacks) || (angular.isDefined(showStacks) && showStacks === true)) {
                        angular.forEach(servicePaths.stack, function (value, key) {
                            var nodes = (showWhitelistedOnly) ? parseInt(value.nodesWhitelisted) : parseInt(value.nodes);
                                listOfPaths.push(
                                    {
                                        GROUP: 'Stacks',
                                        NAME: value.value,
                                        VALUE: nodes > 0 ? value.value + " (" + nodes + ")" : value.value + " (inactive)",
                                        ACTIVE: nodes > 0
                                    }
                                );
                        });
                    }
                    if (angular.isUndefined(showFlavors) || (angular.isDefined(showFlavors) && showFlavors === true)) {
                        angular.forEach(servicePaths.flavor, function (value, key) {
                            var nodes = (showWhitelistedOnly) ? parseInt(value.nodesWhitelisted) : parseInt(value.nodes);
                            if (nodes > 0) {
                                listOfPaths.push(
                                    {
                                        GROUP: 'Flavors',
                                        NAME: value.value,
                                        NUMBER_VALUE: parseFloat(value.value),
                                        VALUE: nodes > 0 ? value.value + " (" + nodes + ")" : value.value + " (inactive)",
                                        ACTIVE: nodes > 0
                                    });
                            }
                        });
                    }
                }
            }
            return listOfPaths;
        }
    }
})();
