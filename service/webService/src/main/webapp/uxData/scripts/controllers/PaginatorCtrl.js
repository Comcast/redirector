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
angular.module('uxData').controller('PaginatorCtrl', ['$scope', 'localStorageService',
    function PaginatorCtrl($scope, localStorageService) {
        $scope.paginationSettings.arrayRows = $scope.arrayRows;

        $scope.paginationSettings.page = (localStorageService.get($scope.paginationSettings.localStoragePath + ".savePageNo") == "true") &&
        angular.isDefined(localStorageService.get($scope.paginationSettings.localStoragePath + ".pageNo")) &&
        localStorageService.get($scope.paginationSettings.localStoragePath + ".pageNo") != null ?
            localStorageService.get($scope.paginationSettings.localStoragePath + ".pageNo") : 0;

        localStorageService.remove($scope.paginationSettings.localStoragePath + ".savePageNo");

        $scope.paginationSettings.perPage = angular.isDefined(localStorageService.get($scope.paginationSettings.localStoragePath)) && localStorageService.get($scope.paginationSettings.localStoragePath) != null ?
            localStorageService.get($scope.paginationSettings.localStoragePath) : 50;

        $scope.prevPage = function () {
            if ($scope.paginationSettings.page > 0) {
                $scope.paginationSettings.page -= 1;
            }
        };
        $scope.nextPage = function () {
            if ($scope.paginationSettings.page < $scope.paginationSettings.numPages - 1) {
                $scope.paginationSettings.page += 1;
            }
        };

        $scope.toPageId = function (id) {
            if (id >= 0 && id <= $scope.paginationSettings.numPages - 1) {
                $scope.paginationSettings.page = id;
            }
        };

        $scope.paginationSettings.refresh = function () {
            localStorageService.set($scope.paginationSettings.localStoragePath, $scope.paginationSettings.perPage);
            $scope.paginationSettings.numPages = Math.ceil($scope.paginationSettings.arrayRows.length / $scope.paginationSettings.perPage);

            if (+$scope.paginationSettings.page + 1 > $scope.paginationSettings.numPages && +$scope.paginationSettings.numPages > 0) {
                $scope.paginationSettings.page = ($scope.paginationSettings.page == 0) ? 0 : $scope.paginationSettings.numPages - 1
            }
            localStorageService.set($scope.paginationSettings.localStoragePath + ".pageNo", $scope.paginationSettings.page);
        };

        $scope.refresh = $scope.paginationSettings.refresh;

        $scope.paginationSettings.numPages = Math.ceil($scope.paginationSettings.itemsLength / $scope.paginationSettings.perPage)
    }]).filter('startFrom', function () {
    return function (input, start) {
        if (angular.isUndefined(input)) {
            return input;
        } else {
            return input.slice(+start);
        }
    };
}).filter('range', function () {
    return function (input, total) {
        total = parseInt(total);
        for (var i = 0; i < total; i++) {
            input.push(i);
        }
        return input;
    };
});
