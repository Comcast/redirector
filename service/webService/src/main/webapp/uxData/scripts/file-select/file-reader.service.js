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
    angular
        .module('uxData.services')
        .factory('fileReader', fileReader);


    fileReader.$inject = ['$q'];

    function fileReader($q) {
        var service = {
            readAsDataUrl: readAsDataURL,
            readAsTextContent: readAsTextContent
        };

        return service;

        function onLoad(reader, deferred, scope) {
            return function (event) {
                scope.$apply(function () {
                    deferred.resolve(reader.result);
                });

                scope.$emit('fileProgress',
                    {
                        total: event.loaded,
                        loaded: event.loaded
                    });
            };
        }

        function onError(reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.reject(reader.result);
                });
            };
        }

        function onProgress(reader, scope) {
            return function (event) {
                scope.$emit('fileProgress',
                    {
                        total: event.total,
                        loaded: event.loaded
                    });
            };
        }

        function getReader(deferred, scope) {
            var reader = new FileReader();
            reader.onload = onLoad(reader, deferred, scope);
            reader.onerror = onError(reader, deferred, scope);
            reader.onprogress = onProgress(reader, scope);
            return reader;
        }

        function readAsDataURL(file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope);
            reader.readAsDataURL(file);

            return deferred.promise;
        }

        function readAsTextContent(file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope);
            reader.readAsText(file);

            return deferred.promise;
        }
    }
}());
