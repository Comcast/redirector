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
        .module('uxData.services')
        .factory('importService', importService);

    importService.$inject = ['$rootScope', '$q', 'fileReader', 'dialogs', '$timeout'];

    function importService($rootScope, $q, fileReader, $dialogs, $timeout) {

        var MAX_VALUES_FILESIZE = 1024 * 1024 * 50;//50 MiB

        $rootScope.$on('fileProgress', function (event, data) {
            if (data.loaded < data.total) {
                var percentage = Math.round((data.loaded * 100) / data.total);
                $rootScope.$broadcast('dialogs.wait.progress', {'progress': percentage});
            } else {
                $rootScope.$broadcast('dialogs.wait.progress', {'progress': 100});
                $timeout(function () {
                    $rootScope.$broadcast('dialogs.wait.complete');
                }, 1000);
            }
        });

        var service = {
            openFile: openFile
        };
        return service;


        function openFile(fileName, limit, scope) {
            var deferred = $q.defer();
            if (angular.isUndefined(fileName)) {
                deferred.reject('File name is undefined');
                return deferred.promise;
            }

            if (limit == null || angular.isUndefined(limit)) {
                limit = MAX_VALUES_FILESIZE;
            }

            if (fileName.size > MAX_VALUES_FILESIZE) {
                deferred.reject('File is too big [' + fileName.name + '], try to use a smaller one, limit is ' + limit + ' bytes');
                return deferred.promise;
            }

            //show wait dialog
            $dialogs.wait(undefined, undefined, 0);

            return fileReader.readAsTextContent(fileName, scope);
        }
    }
})();
