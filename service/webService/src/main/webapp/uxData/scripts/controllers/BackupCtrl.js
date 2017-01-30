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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */


(function () {
    'use strict';

    angular
        .module('uxData')
        .controller('BackupCtrl', BackupCtrl);

    BackupCtrl.$inject = ['$scope', '$rootScope', '$filter', 'messageService', 'requestsService', 'authService', 'USER_PERMISSIONS', 'toastr'];

    function BackupCtrl($scope, $rootScope, $filter, messageService, requests, authService, USER_PERMISSIONS, toastr) {

        $scope.authService = authService;
        $scope.USER_PERMISSIONS = USER_PERMISSIONS;

        var initModel = function () {
            $scope.backupTriggerProgress = false;
            $scope.backupUsageUpdateProgress = false;
            $scope.weekDays = [{day: 'MON'}, {day: 'TUE'}, {day: 'WED'}, {day: 'THU'}, {day: 'FRI'}, {day: 'SAT'}, {day: 'SUN'}];
            $scope.scheduleTime = new Date();
            $scope.scheduleDuration = {hours: 0, minutes: 0};
        };

        $scope.triggerBackup = function () {
            $scope.backupTriggerProgress = true;
            requests.triggerStacksBackup($rootScope.currentApplication)
                .then(function (status) {
                    toastr.success('Backup triggered successfully', 'Success', {closeButton: true, timeOut: 3000});
                    $scope.backupTriggerProgress = false;
                }, function (err) {
                    toastr.error('Can\'t trigger backup', 'Error', {closeButton: true, timeOut: 3000});
                    $scope.backupTriggerProgress = false;
                });
        };

        $scope.updateBackupUsageSchedule = function () {
            $scope.backupUsageUpdateProgress = true;
            var xmlDefaultServer = '<backupUsageSchedule schedule="' + getCronSchedule() + '" duration="' + getScheduleDurationMillis() + '"/>';
            requests.updateBackupUsageSchedule(xmlDefaultServer, $rootScope.currentApplication)
                .then(function (status) {
                    toastr.success('Schedule updated successfully', 'Success', {closeButton: true, timeOut: 3000});
                    $scope.backupUsageUpdateProgress = false;
                }, function (err) {
                    toastr.error('Can\'t updated schedule', 'Error', {closeButton: true, timeOut: 3000});
                    $scope.backupUsageUpdateProgress = false;
                });
        };

        var getBackupUsageSchedule = function () {
            requests.getBackupUsageSchedule($rootScope.currentApplication)
                .then(function (data) {
                    $scope.scheduleTime = getScheduleTimeFromCronSchedule(data.schedule);
                    $scope.scheduleDuration = getScheduleDurationFromMillis(data.duration);
                    updateScheduleWeekDaysFromCronSchedule(data.schedule);
                }, function (reason) {
                    console.log(reason.status);
                });
        };

        var getCronSchedule = function () {
            var minutes = $scope.scheduleTime.getMinutes();
            var hours = $scope.scheduleTime.getHours();

            var selectedWeekDays = $filter('filter')($scope.weekDays, {checked: true});
            var days = '';
            for (var v in selectedWeekDays) {
                if (days !== '') {
                    days += ',';
                }
                days += selectedWeekDays[v].day;
            }
            if (days === '') {
                days = '*';
            }

            return '0 ' + minutes + ' ' + hours + ' ? * ' + days;
        };

        var getScheduleDurationFromMillis = function (millis) {
            var minutesVal = (millis / 60000) % 60;
            var hoursVal = ((millis / 60000) - minutesVal) / 60;
            return {
                hours: hoursVal,
                minutes: minutesVal
            };
        };

        var getScheduleTimeFromCronSchedule = function (cronSchedule) {
            var cronFields = cronSchedule.split(' ');
            var date = new Date();
            date.setHours(cronFields[2]);
            date.setMinutes(cronFields[1]);

            return date;
        };

        var updateScheduleWeekDaysFromCronSchedule = function (cronSchedule) {
            var cronFields = cronSchedule.split(' ');
            var days = cronFields[5].split(',');

            for (var aDay in $scope.weekDays) {
                for (var d in days) {
                    if (days[d] === $scope.weekDays[aDay].day) {
                        $scope.weekDays[aDay].checked = true;
                        break;
                    }
                }
            }
        };

        var getScheduleDurationMillis = function () {
            return ($scope.scheduleDuration.hours * 60 + 1 * $scope.scheduleDuration.minutes) * 60000;
        };

        messageService.onChangeApp($scope, function (message) {
            initModel();
            getBackupUsageSchedule();
        });

        angular.element(document).ready(function () {
            initModel();
            getBackupUsageSchedule();
        });
    }
})();
