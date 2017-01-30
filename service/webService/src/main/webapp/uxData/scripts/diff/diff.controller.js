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

    angular.module('uxData.diff').
        controller('DiffCtrl', DiffCtrl);

    DiffCtrl.$inject = ['$scope', 'diffService'];

    function DiffCtrl($scope, diffService) {

        var vm = this;
        vm.baseHeadTitle = $scope.baseHeadTitle || 'Current';
        vm.changedHeadTitle = $scope.changedHeadTitle || 'Pending';
        vm.isUpdateStatus = isUpdateStatus;
        vm.isDeleteStatus = isDeleteStatus;
        vm.hasDiffChanges = hasDiffChanges;
        vm.isAddStatus = isAddStatus;
        vm.getDiffs = getDiffs;


        function hasDiffChanges() {
            return !angular.equals(getText($scope.baseText), getText($scope.changedText));
        }

        function isUpdateStatus() {
            if (isAddStatus() || isDeleteStatus()) {
                return false;
            }
            return !angular.equals($scope.baseText, $scope.changedText);
        }

        function isAddStatus() {
            return (angular.isUndefined($scope.baseText) || ((angular.isDefined($scope.baseText)) && $scope.baseText == ""));
        }

        function isDeleteStatus() {
            return (angular.isUndefined($scope.changedText)
            || ((angular.isDefined($scope.changedText)) && $scope.changedText == ""));
        }

        function getDiffs(text1, text2, resultSide, type) {
            var config = {
                ignoreLeadingWS: true,
                showAllContent: true,
                type: type
            };
            return diffService.formattedDiff(getText(text1), getText(text2), resultSide, config);
        }

        function getText(textObject) {
            return angular.isDefined(textObject) ? textObject : "";
        }
    }
})();

