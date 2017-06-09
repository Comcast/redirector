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


/*global angular */
(function () {
    'use strict';

    var core = angular.module('uxData.core');

    core.factory('zkDownInterceptor', ['$q', '$injector', function ($q, $injector) {
        return {
            responseError: function (rejected) {
                var proceedToCallback = true;
                if ((rejected.data != null) && angular.isDefined(rejected.data.errors)) {
                    var error = " ";
                    angular.forEach(rejected.data.errors.entry, function (element) {
                        error += element.value + "  ";
                    });
                    rejected.data.message = error;
                } else  {
                    if (angular.isUndefined(rejected.data.message)) {
                        if (angular.isString(rejected.data)) {
                            rejected.data = {message: rejected.data};
                        } else {
                            rejected.data = {message: "Cannot get error data"}
                        }
                    }
                }
                if (rejected.status === 500) {
                    $injector.get('$state').go('error');
                }
                if (rejected.status === 503) {
                    var $rootScope = $injector.get('$rootScope');

                    if (!($rootScope.currentApplication == "decider")) {
                        rejected.data = {message: "Zookeeper is unavaliable"};
                        proceedToCallback = false;
                    }

                    if (!$rootScope.is503DialogShown && !($rootScope.currentApplication == "decider")) {
                        var DEV = "/dev";
                        var DEV_OFFLINE = "/devRedirectorOffline";
                        var ADMIN = "/admin";
                        var ADMIN_OFFLINE = "/adminOffline";

                        var dialog = $injector.get('dialogs').confirm('Offline mode may be needed',
                            'Seems that zookeeper is down. We have an offline mode for such cases. Go to offline mode?');

                        $rootScope.is503DialogShown = true;

                        dialog.result.then(
                            function (yes) {
                                $rootScope.is503DialogShown = false;
                                if ($injector.get('$window').location.href.indexOf(DEV) >= 0) {
                                    $injector.get('$window').location.href = $injector.get('$window').location.href.replace(DEV, DEV_OFFLINE);
                                } else {
                                    $injector.get('$window').location.href = $injector.get('$window').location.href.replace(ADMIN, ADMIN_OFFLINE);
                                }
                            },
                            function (no) {
                                $rootScope.is503DialogShown = false;
                            }
                        );
                    }
                }

                if (rejected.status === 400 && rejected.data.message.indexOf('UnmarshalException') > 0 && proceedToCallback) {
                    return rejected;
                }
                if (proceedToCallback) {
                    return $q.reject(rejected);
                }
            }
        };
    }]);

    core.factory('invalidApplicationInterceptor', ['$q', '$injector', function ($q, $injector) {
        return {
            responseError: function (rejected) {
                var $rootScope = $injector.get('$rootScope');

                if (!$rootScope.invalidApplicationDialogShown && rejected.status === 404 && angular.isDefined(rejected.data.message)
                                        && rejected.data.message.indexOf('Application not found') > -1) {

                    var dialog = $injector.get('dialogs').error('Need at least one stack', 'You need to register stack for current application');
                    dialog.result.then(
                        function() {
                            $rootScope.invalidApplicationDialogShown = false;
                        },
                        function() {
                            $rootScope.invalidApplicationDialogShown = false;
                        }
                    );

                    $rootScope.invalidApplicationDialogShown = true;
                    $q.reject(rejected);
                }


                return $q.reject(rejected);
            }
        };
    }]);

    core.factory('authInterceptor', ['$rootScope', '$q', 'AUTH_EVENTS',
        function ($rootScope, $q, AUTH_EVENTS) {
            return {
                responseError: function (response) {
                    $rootScope.$broadcast({
                        401: AUTH_EVENTS().notAuthenticated,
                        403: AUTH_EVENTS().notAuthorized
                    }[response.status], response);
                    return $q.reject(response);
                }
            };
        }]);

    core.config(['$httpProvider', function ($httpProvider) {
        //Http Intercpetor to check auth failures for xhr requests

        $httpProvider.interceptors.push('invalidApplicationInterceptor');
        $httpProvider.interceptors.push('zkDownInterceptor');
        $httpProvider.interceptors.push('authInterceptor');
    }]);

    core.config(['$locationProvider', function ($locationProvider) {
        $locationProvider.html5Mode(false);
    }]);


    var config = {
        appErrorPrefix: '[Redirector-UI Error] ', //Configure the exceptionHandler decorator
        version: '1.0.0'
    };

    core.value('config', config);

    core.run(['$rootScope', '$injector', '$location', '$window', '$state', 'editableOptions', 'editableThemes',
        'dialogs', 'currentApplication', 'redirectorOfflineMode', 'requestsService', 'authService', 'AUTH_EVENTS', 'tokenService',
        'logoutService', 'STATES_CONSTANTS', 'utilsService',
        function ($rootScope, $injector, $location, $window, $state, editableOptions, editableThemes,
                  $dialogs, currentApplication, redirectorOfflineMode, requestsService, authService, AUTH_EVENTS, tokenService,
                  logoutService, STATES_CONSTANTS, utilsService) {

            var controllersForOnlineModeOnly = [
                'ShowTestSuite',
                'ShowAutoTestSuite',
                'EditTestSuite',
                'EditTestSuite',
                'RunTestSuite',
                'ImportTestSuite',
                'RulesImportController',
                'UrlRulesImportController',
                'summaryController',
                'changesOffline',
                'modelInitializerController'
            ];

            const MINUTES_TO_EXPIRE = 2;
            const EXPIRATION_CHECK_INTERVAL_SECONDS = 60;

            $rootScope.tokenTimerVar = setInterval(tokenExpirationTimer, EXPIRATION_CHECK_INTERVAL_SECONDS * 1000);
            $rootScope.showExpirationDialog = true;
            $rootScope.isSessionRestoreDialogShown = false;

            function tokenExpirationTimer() {
                var tokenCookieValue = utilsService.getFromCookies('token');
                if (tokenCookieValue != null) {
                    var diffMinutes = getExpirationTimeInMinutesFromToken(tokenCookieValue);
                    if (diffMinutes <= MINUTES_TO_EXPIRE) {
                        if (!$rootScope.isSessionRestoreDialogShown && $rootScope.showExpirationDialog) {
                            $rootScope.isSessionRestoreDialogShown = true;

                            var dialog = $injector.get('dialogs').confirm('Session will expire in ' + MINUTES_TO_EXPIRE + ' minute(s). ',
                                'Session will expire in ' + MINUTES_TO_EXPIRE + ' minute(s). Restore the session?');

                            dialog.result.then(
                                function (yes) {

                                    //todo:remove this and do a request to requestsService.getAuthInfo() when APPDS-2585 will be done and deployed on PROD
                                    requestsService.searchNamespacesByItem("Stub_for_session_restore").then(
                                        function (success) { },
                                        function (error) {
                                            logoutService.cleanTokenAndGoToLoginPage();
                                        }
                                    );


                                    $rootScope.isSessionRestoreDialogShown = false;
                                },
                                function (no) {
                                    $rootScope.isSessionRestoreDialogShown = false;
                                    $rootScope.showExpirationDialog = false;
                                }
                            );
                        }
                    } else {
                        $rootScope.showExpirationDialog = true;
                    }

                }
            }

            function getExpirationTimeInMinutesFromToken(token) {
                var mainToken = token.split(".")[1];
                var decodedToken = $window.atob(mainToken);
                var expiration = JSON.parse(decodedToken).exp;
                var diffMillis = Math.abs(new Date(expiration * 1000) - new Date());
                return Math.round((diffMillis / 60000));

            }

            //toDo: do we need this commented line?
            // exceptionHandlerProvider.configure(config.appErrorPrefix);

            $rootScope.redirectorOfflineMode = redirectorOfflineMode;
            if (!redirectorOfflineMode) {
                tokenService.removeGlobalUser();
            }
            if (tokenService.saveTokenAndReloadPage(requestsService)) {
                // If page is being reloaded no listeners need to be added. Hence return from "run" function
                return;
            }

            $rootScope.$on(AUTH_EVENTS().notAuthorized, function (event, error) {
                $dialogs.error('Access is denied', 'You are not authorized for this functionality: ' + (angular.isDefined(error) ? error.data.message : ""));
            });

            $rootScope.$on(AUTH_EVENTS().notAuthenticated, function (event, message) {
                if (!redirectorOfflineMode) {
                    var dlg = $dialogs.notify('Session is expired', 'Your session is expired. No data and actions will be available until you re-login. Please re-login.');
                    dlg.result.then(function (btn) {
                        logoutService.cleanTokenAndGoToLoginPage();
                    });
                }
            });

            $rootScope.$on('$locationChangeSuccess', function(evt) {
                tokenService.getHashFromPageAndSaveItGlobal();
            });

            if (!redirectorOfflineMode && (currentApplication !== "decider")) {
                requestsService.atLeastOneValidModelExists().then(
                    function (data) {
                        if (data == "false") {
                            $state.go(STATES_CONSTANTS().modelInitializer);
                        }
                    });
            }

            $rootScope.$on('$stateChangeStart', function (event, next) {
                if (angular.isDefined(next.data)) {

                    /*
                    When user logs in, token is removed in TokenService.js and $window.location.href is changed to url without token.
                    So redirecting occurs. Flag $rootScope.isPageReloading is set to true before redirecting.
                    Meanwhile, state change described in state.config.js is proceeding and it cancels redirecting started by TokenService.js.
                    To complete redirecting, it is required to cancel state change by calling "event.preventDefault()"
                     */
                    if (angular.isDefined($rootScope.isPageReloading) && $rootScope.isPageReloading === true) {
                        event.preventDefault();
                    }

                    var pageAccessPermissions = next.data.permissions;

                    if (!authService.isAuthorized(pageAccessPermissions)) {
                        event.preventDefault();
                        if (authService.isAuthenticated()) {
                            // user is not allowed
                            $rootScope.$broadcast(AUTH_EVENTS().notAuthorized);
                        } else {
                            // user is not logged in
                            $rootScope.$broadcast(AUTH_EVENTS().notAuthenticated);
                        }
                    }

                    if ((controllersForOnlineModeOnly.indexOf(next.controller) >= 0) && ($rootScope.redirectorOfflineMode)) {
                        event.preventDefault();
                        $state.go(STATES_CONSTANTS().showFlavorRules);
                    }
                }
            });

            $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                $state.previousState = fromState;
                $state.previousParams = fromParams;
                $state.previousServiceName = $rootScope.currentApplication;
            });

        }]);
})();
