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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */



(function () {
    'use strict';
    angular
        .module('uxData.stacks')
        .factory('stacksManagementService', stacksManagementService);

    stacksManagementService.$inject = ['$rootScope', '$filter', '$q', 'requestsService', 'utilsService'];
    function stacksManagementService($rootScope, $filter, $q, requestsService, utilsService) {

        return {
            getStacks: getStacks,
            getWhitelistedFromDS: getWhitelistedFromDS,
            getServicePathsFromDS: getServicePathsFromDS,
            saveWhitelisted: saveWhitelisted,
            exportAllWhitelisted: exportAllWhitelisted,
            exportAllStacks: exportAllStacks
        };

        function getStacks(stacks, stacksAndFlavors, baseWhitelisted, inactiveStacks) {
            var defer = $q.defer();
            getServicePathsFromDS(stacks, [], stacksAndFlavors)
                .then(function () {
                    getWhitelistedFromDS(baseWhitelisted)
                        .then(function (receivedWhitelistedData) {
                            var result = generateModel(baseWhitelisted, stacksAndFlavors, receivedWhitelistedData, inactiveStacks);
                            defer.resolve(result);
                        }, function (reason) {
                            generateModel(baseWhitelisted, stacksAndFlavors, {'paths': []}, inactiveStacks);
                            defer.reject(reason);
                        });
                }, function (reason) {
                    defer.reject(reason);
                });

            return defer.promise;
        }


        function getWhitelistedFromDS(baseWhitelistedData) {
            $rootScope.isSaving = true;
            var defer = $q.defer();
            if (utilsService.isNullOrUndefined(baseWhitelistedData) || !angular.isArray(baseWhitelistedData)) {
                defer.reject({'message': 'Initial object is null or object', status: 1});
                $rootScope.isSaving = false;
                return defer.promise;
            }

            requestsService.getWhitelisted($rootScope.currentApplication)
                .then(function (data) {
                    var whitelistedObj = data;
                    if (utilsService.isNullOrUndefined(whitelistedObj) ||
                        utilsService.isNullOrUndefined(whitelistedObj.paths) ||
                        utilsService.isNullOrUndefined(whitelistedObj.paths.entry) ||
                        !angular.isArray(whitelistedObj.paths.entry)) {
                        defer.reject({'message': 'WS returned empty whitelisted data or incorrect', status: 200});
                        $rootScope.isSaving = false;
                        return defer.promise;
                    }
                    baseWhitelistedData.splice(0, baseWhitelistedData.length); //clear data
                    $rootScope.isSaving = false;
                    defer.resolve(data);
                }, function (error) {
                    defer.reject({'message': error.data, 'status': error.status});
                    $rootScope.isSaving = false;
                });
            return defer.promise;
        }


        function getServicePathsFromDS(stacks, paths, stacksAndFlavors) {
            $rootScope.isSaving = true;
            var defer = $q.defer();

            requestsService.getServicePaths($rootScope.currentApplication)
                .then(function (data) {
                    if (angular.isDefined(data) &&
                        angular.isDefined(data.paths) &&
                        angular.isArray(data.paths) &&
                        angular.isDefined(data.paths[0]) &&
                        angular.isDefined(data.paths[0].stack)) {

                        cleanCurrentFlavorsLists(stacksAndFlavors);
                        for (var i = 0; i < data.paths[0].stack.length; i++) {
                            var resultStack = parseStack(data.paths[0].stack[i]);
                            if (paths.indexOf(resultStack.path) === -1) {
                                paths.push(resultStack.path);
                            }
                            stacks.push(resultStack.rawPath);
                            var flavor = {
                                name: resultStack.flavor,
                                active: resultStack.nodes > 0,
                                nodes: resultStack.nodes,
                                nodesWhitelisted: resultStack.nodesWhitelisted
                            };
                            if (angular.isDefined(stacksAndFlavors[resultStack.path])) {
                                stacksAndFlavors[resultStack.path].flavors.push(flavor);
                            } else {
                                stacksAndFlavors[resultStack.path] = {
                                    name: resultStack.path,
                                    flavors: [flavor]
                                };
                            }
                        }
                        $rootScope.isSaving = false;
                        defer.resolve();
                    } else {
                        $rootScope.isSaving = false;
                        defer.reject({'message': 'WS returned incorrect or empty data for service paths', status: 200});
                        return defer.promise;
                    }
                }, function (reason) {
                    $rootScope.isSaving = false;
                    defer.reject({'message': reason.message, 'status': reason.status});
                });

            return defer.promise;
        }

        function cleanCurrentFlavorsLists(stacksAndFlavors) {
            angular.forEach(stacksAndFlavors, function (value, key) {
                while (value.flavors.length) {
                    value.flavors.pop();
                }
            });
        }


        function parseStack(stack) {
            var rawPath = stack.value;
            var path = rawPath.substr(0, rawPath.lastIndexOf('/'));
            var flavor = rawPath.substr(rawPath.lastIndexOf('/') + 1, rawPath.length);
            var nodes = stack.nodes;
            return {
                rawPath: rawPath,
                path: path,
                flavor: flavor,
                nodes: nodes,
                nodesWhitelisted: stack.nodesWhitelisted
            };
        }


        function generateModel(baseWhitelistedData, stacksAndFlavors, receivedWhitelistedData, inactiveStacks) {
            if (utilsService.isNullOrUndefined(stacksAndFlavors)) {
                return null;
            }

            var orderBy = $filter('orderBy');
            angular.forEach(stacksAndFlavors, function (value, key) {

                var whitelistedStack = getWhitelistedStack(key, receivedWhitelistedData);
                var flavors = stacksAndFlavors[key];
                var visibleFlavors = [];
                for (var i = 0; i < flavors.flavors.length; i ++) {
                    visibleFlavors.push({"flavor" : key + "/" + flavors.flavors[i].name});
                    flavors.flavors[i].realFlavor = getWhitelistedStack(key + "/" + flavors.flavors[i].name, receivedWhitelistedData);
                    flavors.flavors[i].fullName = key + "/" + flavors.flavors[i].name;
                    if (!utilsService.isNullOrUndefined(flavors.flavors[i].realFlavor)) {
                        flavors.flavors[i].realFlavor.updated = !utilsService.isNullOrUndefined(flavors.flavors[i].realFlavor.updated)
                        && !utilsService.isNullOrUndefined(flavors.flavors[i].realFlavor.updated)
                        && flavors.flavors[i].realFlavor.updated !== 0 ? timeConverter(flavors.flavors[i].realFlavor.updated) : "";
                    } else {
                        flavors.flavors[i].realFlavor = {};
                    }

                }
                var nodesCount = countInactiveStack(key, value, inactiveStacks);
                if (nodesCount > 0) {
                    var stackIsNew = utilsService.isNullOrUndefined(whitelistedStack) || utilsService.isNullOrUndefined(whitelistedStack.action);
                    var stackHasPath = !(utilsService.isNullOrUndefined(whitelistedStack) || utilsService.isNullOrUndefined(whitelistedStack.path));

                    var isStackWhitelisted = stackIsNew ? stackHasPath : whitelistedStack.action !== 'DELETE';
                    var flavorFullName = stacksAndFlavors[key].flavors[0].fullName;//assume that we have only 1 stack per flavor
                    var fullFlavor = stacksAndFlavors[key].flavors[0];
                    var flavorUpdated = fullFlavor.realFlavor.updated == undefined ? "" : fullFlavor.realFlavor.updated;
                    var action = fullFlavor.realFlavor.action == undefined ? "NEW" : fullFlavor.realFlavor.action;

                    var data = {
                        pos: baseWhitelistedData.length + 1,
                        isChecked: isStackWhitelisted,
                        path: key,
                        flavorFullName:flavorFullName,
                        flavor:fullFlavor,
                        nodes: fullFlavor.nodes,
                        updated:  flavorUpdated,
                        action:  action,
                        secondSorting: orderBy(stacksAndFlavors[key].flavors, 'name', false)[0].name + ' ' + key //APPDS-1361
                    };
                    baseWhitelistedData.push(data);
                }
            });

            return baseWhitelistedData;
        }

        function getWhitelistedStack(key, receivedWhitelistedData) {
            if (utilsService.isNullOrUndefined(receivedWhitelistedData.paths) || utilsService.isNullOrUndefined(receivedWhitelistedData.paths.entry)) {
                return null;
            }
            for (var i = 0; i < receivedWhitelistedData.paths.entry.length; i++) {
                if (receivedWhitelistedData.paths.entry[i].key === key) {
                    return receivedWhitelistedData.paths.entry[i].value;
                }
            }
            return null;
        }

        function timeConverter(UNIX_timestamp) {
            var date = new Date(UNIX_timestamp);

            return  date.toLocaleString('en-US');
        }

        function countInactiveStack(stackName, stackValue, inactiveStacks) {
            // TODO: method is responsible for 2 things: count of active nodes and collecting inactive stacks. Let's simplify when we have a chance
            var nodeCount = 0;
            if (stackValue.flavors.length > 0) {
                for (var i = 0; i < stackValue.flavors.length; i++) {
                    if (!stackValue.flavors[i].active) {
                        inactiveStacks.push({
                            value: stackName + '/' + stackValue.flavors[i].name
                        });
                    } else {
                        nodeCount += stackValue.flavors[i].nodes;
                    }
                }
            }
            return nodeCount;
        }

        function saveWhitelisted(whitelistedData) {
            var defer = $q.defer();
            if (whitelistedData === null || angular.isUndefined(whitelistedData) || whitelistedData.length === 0) {
                defer.reject({message: 'The whitelisted array is empty or null'});
                return defer.promise;
            }

            $rootScope.isSaving = true;
            var patchesToSave = [];

            for (var i = 0; i < whitelistedData.length; i++) {
                if (whitelistedData[i].isChecked) {
                    patchesToSave.push(whitelistedData[i].path);
                }
            }

            requestsService.sendWhitelisted($rootScope.currentApplication, {'paths': patchesToSave})
                .then(function (data) {
                    $rootScope.isSaving = false;
                    defer.resolve(data);
                }, function (err) {
                    defer.reject(err);
                    $rootScope.isSaving = false;
                });
            return defer.promise;
        }

        function exportAllWhitelisted() {
            requestsService.exportAllWhitelisted($rootScope.currentApplication);
        }

        function exportAllStacks() {
            requestsService.exportAllServicePaths($rootScope.currentApplication);
        }
    }
})();
