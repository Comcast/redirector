(function () {
    'use strict';

    var app = angular.module('report', [])
        .constant('TEST_TYPE', {
            MAIN_TEST: 'mainTest',
            EXTRA_TEST: 'extraTest'
        })
        .constant('TEST_STATUS', {
            PASSED: 'PASSED',
            FAILED: 'FAILED'
        });

    app.controller('reportController', ['$scope', '$http', 'TEST_TYPE', 'TEST_STATUS',

        function ($scope, $http, TEST_TYPE, TEST_STATUS) {

            $scope.hide = [];
            $scope.applications = [];
            $scope.currentTest = TEST_TYPE.MAIN_TEST;
            $scope.TEST_TYPE = TEST_TYPE;
            $scope.TEST_STATUS = TEST_STATUS;
            $scope.testTypes = [
                TEST_TYPE.MAIN_TEST,
                TEST_TYPE.EXTRA_TEST
            ];
            $scope.testStatus = [

            ];
            $scope.flavorRulesUnderTest = [];
            $scope.distributionRulesUnderTest = [];
            $scope.generalResult = TEST_STATUS.PASSED;

            init();

            function load() {
                if ($scope.currentTest === TEST_TYPE.MAIN_TEST) {
                    getReportForApplication($scope.application).then(function (reportData) {
                        setupData(reportData.data.items);
                    });
                } else if ($scope.currentTest === TEST_TYPE.EXTRA_TEST) {
                    getReportForExtraTests().then(function (reportData) {
                        setupData(reportData.data.reports);
                    });
                }
            }

            function setupData(reportItems) {
                $scope.reportItems = reportItems;

                for (var i; i < $scope.reportItems.length; i++) {
                    $scope.hide[i] = true;
                }
            }

            function getReportForApplication(application) {
                return $http.get(application + '/report.json');
            }

            function getReportForExtraTests() {
                return $http.get('extraTests/extraTestsReport.json');
            }

            function setGeneralResult() {
                for (var i = 0; i < $scope.applications.length; i++) {
                    var application = $scope.applications[i];
                    getReportForApplication(application).then(
                        function(result) {
                            if (!isSuccessfulMainTestReport(result.data.items)) $scope.generalResult = TEST_STATUS.FAILED;
                        },
                        function(reason) {}
                    );
                }

                if ($scope.generalResult !== TEST_STATUS.FAILED) {
                    getReportForExtraTests().then(
                        function(result) {
                            if (!isSuccessfulExtraTestsReport(result.data.reports)) $scope.generalResult = TEST_STATUS.FAILED;
                        },
                        function(reason) {}
                    );
                }

            }

            function isSuccessfulReport(reportItems) {
                for (var i = 0; i < reportItems.length; i++) {
                    var reportItem = reportItems[i];
                    if (reportItem.status === TEST_STATUS.FAILED) return false;
                }

                return true;
            }

            function isSuccessfulMainTestReport(reportItems) {
                if (reportItems.length < 1) return false;
                return isSuccessfulReport(reportItems);
            }

            function isSuccessfulExtraTestsReport(reportItems) {
                if (reportItems.length < 1) return true;
                return isSuccessfulReport(reportItems);
            }

            $scope.isFailed = function (itemIndex) {
                return $scope.reportItems[itemIndex].status === TEST_STATUS.FAILED;
            };

            $scope.hideOrShow = function (id) {
                $scope.hide[id] = !$scope.hide[id];
                if ($scope.hide[id] && $scope.currentTest === TEST_TYPE.MAIN_TEST) {
                    getRulesUnderTestAsXml(id);
                }
            };

            $scope.isNotEquals = function (item1, item2) {
                if (angular.isUndefined(item2) || item2 === null) {
                    return false;
                }
                return !(item1 === item2);
            };

            $scope.submitTheForm = function () {
                load();
            };

            $scope.isApplicationsNotExist = function () {
                return angular.isUndefined($scope.applications) || $scope.applications === null || $scope.applications.length == 0;
            };

            $scope.isEmptyReportList = function () {
                return ($scope.currentTest === TEST_TYPE.MAIN_TEST && $scope.isApplicationsNotExist())
                    || ($scope.currentTest === TEST_TYPE.EXTRA_TEST && $scope.reportItems.length < 1);
            };

            $scope.appliedUrlRulesMatch = function (actualUrlRules, expectedUrlRules) {
                if (!angular.isArray(expectedUrlRules)) {
                    return true;
                }

                if (expectedUrlRules.length > 0 && !angular.isArray(actualUrlRules)) {
                    return false;
                }

                return isSubset(actualUrlRules, expectedUrlRules);
            };

            $scope.arrayToString = function(array) {
                if (angular.isArray(array)) {
                    return array.join(', ');
                }

                return array;
            };

            function isSubset(array, arrayToCheck) {
                var length = arrayToCheck.length;
                for (var i = 0; i < length; i++) {
                    if (array.indexOf(arrayToCheck[i]) < 0) return false;
                }

                return true;
            }

            function init() {
                $http.get('/list.json').then(function (reportData) {
                    $scope.applications = reportData.data;
                    setGeneralResult();
                    if ($scope.applications !== null && $scope.applications.length > 0) {
                        $scope.application = $scope.applications[0];
                        load();
                    }
                });
            }

            function getRulesUnderTestAsXml(index) {
                var ruleUnderTest = $scope.reportItems[index].testCase.ruleUnderTest;
                if (ruleUnderTest) {
                    if (ruleUnderTest.hasOwnProperty('percent')) {
                        $http.get('distributions/' + $scope.application + '/').then(
                            function(result) {
                                $scope.distributionRulesUnderTest[index] = result.data;
                            },
                            function(reason) {
                                console.log(reason);
                            }
                        );
                    } else {
                        $http.get('rules/' + $scope.application + '/' + ruleUnderTest.id).then(
                            function(result) {
                                $scope.flavorRulesUnderTest[index] = result.data;
                            },
                            function(reason) {
                                console.log(reason);
                            }
                        );
                    }
                }
            }
        }]);

})();