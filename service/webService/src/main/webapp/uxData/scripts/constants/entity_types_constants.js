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

    angular.module('uxData.constants').constant('IndexedDB_CONSTANTS', constants);

    function constants() {

        var result = {
            DISTRIBUTION : 'distributions',
            PATHRULES : 'pathRules',
            PENDINGCHANGES : 'pendingChanges',
            STACKS : 'stacks',
            APPLICATIONS : 'applicationsNames',
            TEMPLATEPATHRULES : 'templatePathRules',
            TEMPLATEURLRULES : 'templateUrlPathRules',
            URLRULES : 'urlRules',
            WHITELISTED : 'whitelisted',
            WHITELISTED_UPDATES : 'whitelistedUpdates',
            DEFAULTPARAMS : 'urlParams',
            SERVERS: 'servers',
            NAMESPACE: 'namespace',
            VERSION: 'version',
            STACKBACKUP: 'stackBackup',
            REDIRECTORCONFIG: 'redirectorConfig'
        };

        return result;
    }
})();
