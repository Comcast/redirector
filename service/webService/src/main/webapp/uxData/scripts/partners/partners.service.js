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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */


(function () {
    'use strict';
    angular
        .module('uxData.partners')
        .factory('partnersService', partnersService);

    partnersService.$inject = ['$rootScope', '$q', 'requestsService', 'utilsService'];

    function partnersService($rootScope, $q, requestsService, utilsService) {

        var service = {
            getPartners: getPartners,
            getPartnersFromDS: getPartnersFromDS,
            savePartners: savePartners,
            savePartner: savePartner,
            deletePartner: deletePartner,
            exportAllPartners: exportAllPartners,
            exportPartner: exportPartner,
            getPartnersFromPartnersObject: getPartnersFromPartnersObject,
            findPositionOfPartnerById: findPositionOfPartnerById,
            getPartnerPropertiesAsString: getPartnerPropertiesAsString
        };
        return service;

        function getPartners () {
            var defer = $q.defer();
            requestsService.getPartnersJSON()
                .then(function (data) {
                    if (utilsService.isNullOrUndefined(data) || utilsService.isNullOrUndefined(data.partner) || data.partner === '') {
                        defer.reject({'message': 'WS returned empty partners list data', status: 200});
                        return defer.promise;
                    }
                    var partners = getPartnersFromPartnersObject(data);
                    defer.resolve(partners);
                }, function (reason) {
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function getPartnersFromPartnersObject(partnersObject) {
            var partners = [];
            angular.forEach(utilsService.toArray(partnersObject.partner), function (partner, key) {
                var obj = {};
                obj.id = partner.id;
                angular.forEach(utilsService.toArray(partner.properties.property), function (property, key) {
                    obj[property.name] = property.value;
                });
                partners.push(obj);
            });
            return partners;
        }

        function getPartnersFromDS(basePartnersData) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.getPartnersJSON()
                .then(function (data) {
                    var partnersObject = data;
                    if (utilsService.isNullOrUndefined(partnersObject) || utilsService.isNullOrUndefined(partnersObject.partner) || partnersObject.partner === '') {
                        defer.reject({'message': 'WS returned empty partners list data', status: 200});
                        $rootScope.isSaving = false;
                        return defer.promise;
                    }
                    var partnersFromBackend = getPartnersFromPartnersObject(partnersObject);
                    basePartnersData.splice(0, basePartnersData.length);
                    partnersFromBackend = utilsService.toArray(partnersFromBackend);
                    Array.prototype.push.apply(basePartnersData, partnersFromBackend);
                    $rootScope.isSaving = false;
                    defer.resolve(basePartnersData);
                }, function (reason) {
                    $rootScope.isSaving = false;
                    defer.reject(reason);
                });
            return defer.promise;
        }

        function savePartners(partners) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            var partnersToSend = {};
            partnersToSend.partner = [];
            angular.forEach(partners, function(value, key) {
                var properties = {};
                properties.property = [];
                for (var name in value) {
                    if (name !== 'id') {
                        properties.property.push({
                            'name': name,
                            'value': value[name]
                        });
                    }
                }
                partnersToSend.partner.push({'id': value.id, 'properties': properties});
            });
            requestsService.savePartners($rootScope.currentApplication, partnersToSend)
                .then(function (status) {
                    $rootScope.isSaving = false;
                    defer.resolve();
                }, function (err) {
                    $rootScope.isSaving = false;
                    defer.reject(err);
                });
            return defer.promise;
        }

        function savePartner(partner) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            var properties = {};
            properties.property = [];
            for (var name in partner) {
                if (name !== 'id') {
                    properties.property.push({
                        'name': name,
                        'value': partner[name]
                    });
                }
            }
            requestsService.addOrUpdatePartner({'id': partner.id, 'properties': properties})
                .then(function (status) {
                    $rootScope.isSaving = false;
                    defer.resolve();
                }, function (err) {
                    $rootScope.isSaving = false;
                    defer.reject(err);
                });
            return defer.promise;
        }

        function deletePartner(partnerId) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.deletePartner(partnerId)
                .then(function () {
                    $rootScope.isSaving = false;
                    defer.resolve();
                }, function (reason) {
                    $rootScope.isSaving = false;
                    if (reason === null) {
                        defer.reject({'message': 'Server return null'});
                    } else {
                        defer.reject(reason);
                    }
                });
            return defer.promise;
        }

        function exportAllPartners() {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.getExportPartners();
            $rootScope.isSaving = false;
            defer.resolve();
            return defer.promise;
        }

        function exportPartner(partnerId) {
            var defer = $q.defer();
            $rootScope.isSaving = true;
            requestsService.getExportPartner(partnerId);
            $rootScope.isSaving = false;
            defer.resolve();
            return defer.promise;
        }

        function findPositionOfPartnerById(partners, partnerId) {
            var index;
            for (var i = 0; i < partners.length; i++) {
                if (partners[i].id === partnerId) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        function getPartnerPropertiesAsString (partner) {
            // ' "property_1 = value_1"\n"property_2 = value_2"... '
            var result = '';
            var propertyNames = Object.getOwnPropertyNames(partner).sort();
            for (var i = 0; i < propertyNames.length; i++) {
                if (propertyNames[i] !== 'id') {
                    result += propertyNames[i] + ' = ' + partner[propertyNames[i]] + '\n';
                }
            }
            return (result === '') ? '' : result.substring(0, result.length - 1);
        }

    }
})();
