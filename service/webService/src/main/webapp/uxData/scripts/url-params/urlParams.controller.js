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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


(function () {
    'use strict';

    angular
        .module('uxData.urlParams')
        .controller('urlParamsController', urlParamsController);

    urlParamsController.$inject = ['$scope', 'RulesCommonValidationService'];

    function urlParamsController($scope, rulesCommonVS) {

        /* jshint validthis: true */
        var vm = this;

        vm.server = $scope.server;
        
        vm.IP_PROTOCOL_VERSION_LIST = [{id: '4', name: 'IPv4'}, {id: '6', name: 'IPv6'}];
        
        vm.onUrnChanged = onUrnChanged;
        vm.onPortChanged = onPortChanged;
        vm.onProtocolChanged = onProtocolChanged;
        vm.onIPProtocolVersionChanged = onIPProtocolVersionChanged;
        vm.hasError = rulesCommonVS.hasError;

        function onProtocolChanged () {
            $scope.onServerChanged({newProtocol: vm.server.protocol, newPort: vm.server.port, newUrn: vm.server.urn, newIPProtocolVersion: vm.server.ipProtocolVersion});
        }

        function onPortChanged () {
            $scope.onServerChanged({newProtocol: vm.server.protocol, newPort: vm.server.port, newUrn: vm.server.urn, newIPProtocolVersion: vm.server.ipProtocolVersion});
        }

        function onUrnChanged () {
            $scope.onServerChanged({newProtocol: vm.server.protocol, newPort: vm.server.port, newUrn: vm.server.urn, newIPProtocolVersion: vm.server.ipProtocolVersion});
        }

        function onIPProtocolVersionChanged () {
            $scope.onServerChanged({newProtocol: vm.server.protocol, newPort: vm.server.port, newUrn: vm.server.urn, newIPProtocolVersion: vm.server.ipProtocolVersion});
        }
    }
})();
