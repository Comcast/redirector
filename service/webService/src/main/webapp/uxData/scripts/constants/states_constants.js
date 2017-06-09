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

    angular.module('uxData.constants').constant('STATES_CONSTANTS', constants);

    function constants() {
        var result = {
            // redirector
            showFlavorRules: 'showFlavorRules',
            editFlavorRule: 'editFlavorRule',
            addNewFlavorRule: 'addNewFlavorRule',
            importFlavorRules: 'importFlavorRules',
            showURLRules: 'showURLRules',
            editURLRule: 'editURLRule',
            importURLRules: 'importURLRules',
            addNewURLRule: 'addNewURLRule',
            distribution: 'distribution',
            backup: 'backup',
            stacksManagement: 'stacksManagement',
            summary: 'summary',
            settings: 'settings',
            changes: 'changes',
            changesOffline: 'changesOffline',
            testsuiteShow: 'testsuite-show',
            testsuiteShowAuto: 'testsuite-showauto',
            testsuiteAdd: 'testsuite-add',
            testsuiteEdit: 'testsuite-edit',
            testsuiteRun: 'testsuite-run',
            testsuiteImport: 'testsuite-import',
            // decider
            addDeciderRule: 'addDeciderRule',
            editDeciderRule: 'editDeciderRule',
            showDeciderRules: 'showDeciderRules',
            importDeciderRules: 'importDeciderRules',
            partners: 'partners',

            // namespace
            namespacesAdd: 'namespacesAdd',
            namespacesAddIp: 'namespacesAddIp',
            namespacesAddEncoded: 'namespacesAddEncoded',
            namespacesImport: 'namespacesImport',
            namespacesShow: 'namespacesShow',
            namespacesEdit: 'namespacesEdit',
            namespacesFindByItem: 'namespacesFindByItem',
            namespacesSearch: 'namespacesSearch',

            templates: 'templates',
            error: 'error',

            modelInitializer: 'modelInitializer'
        };
        return result;
    }
})();
