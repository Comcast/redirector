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
 */
(function () {
    'use strict';

    angular
        .module('uxData.namespaced')
        .controller('NamespacedDeleteEntities', NamespacedDeleteEntities);

    NamespacedDeleteEntities.$inject = ['$log', 'authService', 'USER_PERMISSIONS', 'namespacedService', 'namespacedAlertsService', 'importService', '$modalInstance', 'namespacedName', 'requestsService'];

    function NamespacedDeleteEntities($log, authService, USER_PERMISSIONS, namespacedService, namespacedAlertsService, importService, $modalInstance, namespacedName, requestsService) {
        /* jshint validthis: true */
        var vm = this;

        angular.extend(vm, {'namespacedName': namespacedName});

        vm.authService = authService;
        vm.USER_PERMISSIONS = USER_PERMISSIONS;
        vm.canDelete = true;
        vm.canOpenFile = true;

        vm.title = 'Choose a file with entities description';

        vm.entitiesFromFile = [];
        vm.notFoundEntities = [];

        vm.getFile = getFile;
        vm.deleteEntities = deleteEntities;
        vm.close = close;

        var FILE_FORMATTING_ERROR = 'Something is wrong with file formatting';
        var DELETE_PROMPT = 'Delete entities by clicking \"Delete\" button';

        function getFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                getEntitiesFromFile(result);
            }, function (reason) {
                $log.error('Reason: ' + reason.message);
                vm.title = FILE_FORMATTING_ERROR;
                namespacedAlertsService.errorGetFile();
            });
        }

        function getEntitiesFromFile(result) {
            try {
                if (angular.equals(result.trim(), '')) {
                    vm.title = "File is empty, please select another one";
                    return;
                }
                var entities = JSON.parse(result);
                if (entities != "" && angular.isDefined(entities.entities)) {
                    vm.entitiesFromFile = entities.entities;
                    vm.title = DELETE_PROMPT;
                    vm.canOpenFile = false;
                } else {
                    throw {message: "JSON is invalid"};
                }
            } catch (reason) { //plain text case
                try {
                    vm.entitiesFromFile = [];
                    var values = result.match(/[^\r\n]+/g); //contains new lines
                    if (values !== '') {
                        angular.forEach(values, function (value, key) {
                            if (!angular.equals(value.trim(), '')) {
                                vm.entitiesFromFile.push(value);
                            }
                        });
                    }
                    vm.title = DELETE_PROMPT;
                    vm.canOpenFile = false;
                } catch (error) {
                    $log.error('Reason: ' + reason.message);
                    vm.title = FILE_FORMATTING_ERROR;
                    namespacedAlertsService.errorParse();
                }
            }
        }

        function deleteEntities () {
            $log.info("Attempting to delete bulk of namespaced entities from file");
            requestsService.bulkDeleteNamespacedValues(vm.namespacedName, vm.entitiesFromFile).then(
                function (data) {
                    $log.info("Deletion succeeded, following entities were not found ", data);
                    vm.notFoundEntities = data.entities;
                    if (data.deletedValues.length == 0) {
                        vm.title = 'Deletion is unsuccessful';
                        vm.subTitle = '(no entries were deleted)';
                    } else {
                        vm.title = 'Deletion is succeeded';
                    }
                    vm.canDelete = false;
                },
                function (error) {
                    $log.error ("Error while deleting bulk of namespaces from file ", error);
                    vm.title = 'Deletion was in error';
                }
            );
        }

        function close () {
            $modalInstance.close(true);
        }
    }
})();
