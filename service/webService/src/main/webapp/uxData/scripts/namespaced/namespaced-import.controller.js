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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */


(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedImport', NamespacedImport);

    NamespacedImport.$inject = ['$log', '$timeout', 'authService', 'USER_PERMISSIONS', 'namespacedService', 'namespacedAlertsService', 'importService', '$modal'];

    function NamespacedImport($log, $timeout, authService, USER_PERMISSIONS, namespacedService, namespacedAlertsService, importService, $modal) {
        /* jshint validthis: true */
        var vm = this;

        vm.autoResolveText = false;

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;


        vm.title = 'Choose a file with namespaces description';
        vm.savedStatus = true;
        vm.namespaceImportAll = false;

        vm.namespacedDuplicateMap = {};
        vm.namespacesFromFile = {};

        vm.namespaceCandidates = {};

        vm.importAllNamespaces = importAllNamespaces;
        vm.importNamespace = importNamespace;
        vm.getFile = retrieveFile;

        function retrieveFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                getNamespacesFromFile(result);
            }, function (reason) {
                $log.error('Reason: ' + reason.message);
                namespacedAlertsService.errorGetFile();
            });
        }

        function getNamespacesFromFile(result) {
            try {
                var namespaces = JSON.parse(result);
                if (namespaces !== '') {
                    vm.namespacesFromFile = namespaces;
                }
                fillNamespacesModel(vm.namespacesFromFile);
                vm.title = 'Save the namespaced lists by clicking \"Save\" button';
            } catch (reason) {
                $log.error('Reason: ' + reason.message);
                namespacedAlertsService.errorParse();
            }

        }

        function fillNamespacesModel(namespacesObject) {
            if (!angular.isArray(namespacesObject.namespace)) {
                namespacesObject.namespace = [namespacesObject.namespace];
            }
            angular.forEach(namespacesObject.namespace, function (value, key) {
                if (angular.isDefined(namespacesObject.namespace[key].value)) {//old format case
                    if (!angular.isArray(namespacesObject.namespace[key].value)) {
                        namespacesObject.namespace[key].value = [namespacesObject.namespace[key].value]
                    }
                    //backward capability
                    angular.forEach(namespacesObject.namespace[key].value, function (value, key_val) {
                        if (!angular.isObject(value)) {
                            namespacesObject.namespace[key].value[key_val] = {value: value};
                        }
                    });
                    namespacesObject.namespace[key].type = 'TEXT';
                    namespacesObject.namespace[key].valueSet = [];
                    angular.forEach (namespacesObject.namespace[key].value, function(value) {
                        namespacesObject.namespace[key].valueSet.push(value);
                    });
                } else {//new format case
                    if (!angular.isArray(namespacesObject.namespace[key].valueSet)) {
                        if (angular.isDefined(namespacesObject.namespace[key].valueSet)) {
                            namespacesObject.namespace[key].valueSet = [namespacesObject.namespace[key].valueSet];
                        }
                    }
                }
            });
        }

        function importAllNamespaces() {
            if (vm.namespacesFromFile.namespace.length === 0) {
                vm.namespaceImportAll = false;
                return;
            }
            vm.namespaceImportAll = true;
            for (var i = 0; i < vm.namespacesFromFile.namespace.length; i++) {
                importNamespace(vm.namespacesFromFile.namespace[i], vm.namespacesFromFile.namespace[i].name);
            }
        }

        function importNamespace(namespace, name) {
            vm.isImportInProgress = true;
            vm.namespacedDuplicateObject = {};
            vm.progress=20;
            if (vm.autoResolveText && namespace.type == 'TEXT') {
                validateAndSave(namespace, name, vm.autoResolveText);
            } else {
                $timeout(function () {
                    namespacedService.validateDuplicates(namespace).then(function (result) {
                        var name = namespace.name;

                        vm.progress = 60;
                        validateAndSave(namespace, name);
                        vm.progress = 90;
                    }, function (result) {
                        vm.progress = 60;

                        $log.warn(result.message);
                        vm.namespaceCandidates = namespace;
                        vm.namespacedDuplicateMap = angular.copy(result.data);
                        var modalInstance = $modal.open({
                            templateUrl: '../uxData/scripts/namespaced/namespaced-modal.html',
                            controller: 'NamespacedMerger as vm',
                            resolve: {
                                'namespaceCandidates': function () {
                                    return vm.namespaceCandidates;
                                },
                                'namespacedDuplicateMap': function () {
                                    return vm.namespacedDuplicateMap;
                                },
                                'namespacesFromFile': function () {
                                    return vm.namespacesFromFile;
                                },
                                'isImporting': function () {
                                    return true;
                                }
                            }
                        }); // end modal.open

                        modalInstance.result.then(function (result) {
                            var position = namespacedService.findPostionOfNamespaceByName(namespace.name, vm.namespacesFromFile.namespace);
                            vm.namespacesFromFile.namespace.splice(position, 1);
                            vm.isImportInProgress = false;
                        });
                    }, 1000);
                });
            }
        }

        function validateAndSave(namespace, name, autoResolveText) {
            if (angular.isUndefined (autoResolveText)) {
                autoResolveText = false;
            }
            namespacedService.validateAndSave(namespace, name, true, autoResolveText).then(function () {
                $log.info('Added namespace: \"' + name + '\"');
                namespacedAlertsService.success(name);
                vm.namespaceCandidates = {}; //clear
                var position = namespacedService.findPostionOfNamespaceByName(namespace.name, vm.namespacesFromFile.namespace);
                vm.namespacesFromFile.namespace.splice(position, 1);
                vm.isImportInProgress = false;
            }, function (reason) {
                $log.error('Failed: ' + reason.message);
                namespacedAlertsService.errorPostWithMessage(reason.message);
                vm.isImportInProgress = false;
            });
        }

    }
})();
