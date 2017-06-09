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

    angular.module('uxData.constants').constant('USER_PERMISSIONS', permissions);

    function permissions() {
        var result = {
            read: 'read',
            write: 'write',
            readRules: 'read-rules',
            readUrlRules: 'read-urlRules',
            readChanges: 'read-changes',
            readDistributions: 'read-distributions',
            readServers: 'read-servers',
            readNamespacedLists: 'read-namespacedLists',
            readBackups: 'read-backups',
            readStacks: 'read-stacks',
            readWhitelist: 'read-whitelist',
            readTestSuite: 'read-testSuite',
            readPartners: 'read-partners',
            readDeciderRules: 'read-deciderRules',
            writeRules: 'write-rules',
            writeUrlRules: 'write-urlRules',
            writeChanges: 'write-changes',
            writeDistributions: 'write-distributions',
            writeServers: 'write-servers',
            writeNamespacedLists: 'write-namespacedLists',
            writeBackups: 'write-backups',
            writeStacks: 'write-stacks',
            writeWhitelist: 'write-whitelist',
            writeDeciderRules: 'write-deciderRules',
            writePartners: 'write-partners',
            all: 'permitAll',
            writeTestSuite: 'write-testSuite',
            writeSettings: 'write-settings',
            writeModelInitializer: 'write-modelInitializer'
        };
        return result;
    }
})();
