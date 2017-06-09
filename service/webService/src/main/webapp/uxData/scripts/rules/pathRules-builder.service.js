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
        .module('uxData.services')
        .factory('pathRulesBuilderService', pathRulesBuilderService);

    pathRulesBuilderService.$inject = ['RULES_CONSTANTS', 'SERVER_CONSTANTS', 'utilsService'];

    function pathRulesBuilderService(rulesCONST, serverCONST, utils) {

        var service = {
            completeRuleUnmarshalling: completeRuleUnmarshalling,
            buildReturnStatement: buildServers,
            prepareReturnForMarshalling: prepareServersForMarshalling,
            buildRuleObjectForPreview: buildRuleObjectForPreview,
            objectToXml: objectToXml,
            getRuleReturnSimpleText: getRuleReturnSimpleText,
            getRuleReturnDiffText: getRuleReturnDiffText
        };

        var HOST = rulesCONST().HOST;
        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var ADVANCED = serverCONST().EXP_EDIT_MODE.ADVANCED;
        var URL_TEMPLATE = 'protocol://{host}:port/URN';

        return service;

        function completeRuleUnmarshalling(jsonRule, rawRule, rule) {

            rule.countDownTime = angular.isDefined(rawRule.returnStatement.countDownTime) ?
                parseInt(rawRule.returnStatement.countDownTime) : 15;

            rule.enablePrivate = angular.isDefined(rawRule.returnStatement.enablePrivate) ?
                rawRule.returnStatement.enablePrivate : true;

            rule.templateName = jsonRule.templateName;

            return rule;
        }

        function buildServers(returnStatement) {
            var servers = [];

            angular.forEach(returnStatement.servers, function (server, index) {
                var ret = {};
                ret.url = angular.copy(server.url);
                ret.path = angular.copy(server.path);
                ret.query = angular.copy(server.query);
                ret.serverName = angular.copy(server.serverName);
                ret.description = angular.copy(server.description);
                ret.editMode = ret.url.indexOf(HOST) > -1 ? SIMPLE : ADVANCED;
                ret.isNonWhitelisted = angular.equals(server.isNonWhitelisted, 'true') || server.isNonWhitelisted === true;

                servers.push(ret);
            });

            angular.copy(servers, returnStatement.servers);
            return returnStatement;
        }

        function prepareServersForMarshalling(returnObj) {

            angular.forEach(returnObj.servers, function (server, index) {
                server.name = returnObj.ruleName;
                server.description = returnObj.ruleName + ' server route';
            });

            var serverStatement;
            var server = buildServerListObject(returnObj.servers);
            if (server.length > 1) {
                serverStatement = {
                    'return': {
                        'serverGroup': [{
                            'enablePrivate': returnObj.enablePrivate,
                            'countDownTime': returnObj.countDownTime,
                            'server': server
                        }]
                    }
                };
            }
            else {
                serverStatement = {
                    'return': {
                        'server': server
                    }
                };
            }

            return serverStatement;
        }

        function buildServerListObject(serverList) {
            var server = [];
            for (var i = 0; i < serverList.length; i++) {
                server.push(buildServerObject(serverList[i]));
            }
            return server;
        }

        function buildServerObject(server) {
            var resultingObj = {
                name: server.name,
                description: server.description,
                url: server.url,
                query: angular.copy(server.query)
            };

            if (server.editMode === 'simple') {
                resultingObj.path = server.path;
            }

            resultingObj.isNonWhitelisted = angular.isDefined(server.isNonWhitelisted) ? server.isNonWhitelisted : 'false';

            return resultingObj;
        }

        function buildRuleObjectForPreview(rawRule, jsonRule, servicePaths) {

            var ruleObject = {};
            ruleObject.params = rawRule.expressions;
            ruleObject.returnStatement = rawRule.returnStatement;
            ruleObject.data = {};
            ruleObject.data.rule = jsonRule;
            ruleObject.data.countDownTime = rawRule.countDownTime;
            ruleObject.data.enablePrivate = rawRule.enablePrivate;

            ruleObject.data.path = [];
            ruleObject.data.serverEditMode = [];

            angular.forEach(ruleObject.returnStatement.servers, function(server, index) {
                var editMode = SIMPLE;
                if (angular.isUndefined(server.path)) {
                    server.path = '';
                    editMode = ADVANCED;
                }

                server.editMode = editMode;
                ruleObject.data.serverEditMode[index] = editMode;
                var path = getPathByPathId(server, servicePaths);
                ruleObject.data.path.push(path);
                if (editMode === ADVANCED) {
                    ruleObject.data.url = server.url;
                    ruleObject.data.query = server.query;
                }
            });
            return ruleObject;
        }

        function getPathByPathId(server, servicePaths) {
            var pathList = (server.path.indexOf('/') === -1) ? servicePaths.flavor : servicePaths.stack;
            var result = {value: server.path, nodes: 0};

            angular.forEach(pathList, function(entry, index) {
                if (angular.equals(entry.value, server.path)) {
                    angular.copy(entry, result);
                }
            });
            result.isNonWhitelisted = server.isNonWhitelisted;

            return result;
        }

        function objectToXml (ruleObject) {
            return utils.objectToXml(ruleObject.data.rule)
        }

        function getRuleReturnSimpleText(returnStatement) {
            var simpleReturn = '';
            for (var y = 0; y < returnStatement.servers.length; y++) {
                if (returnStatement.servers[y].editMode === 'simple') {
                    var url = URL_TEMPLATE;
                    url = url.replace('protocol', returnStatement.servers[y].protocol);
                    url = url.replace('port', returnStatement.servers[y].port);
                    url = url.replace('URN', returnStatement.servers[y].urn);

                    var query = getRedirectQueryString(returnStatement.servers[y].query, '', '&');
                    if (! utils.isEmptyString(query)) {
                        query = "; query: " + query;
                    }

                    simpleReturn += '(url: ' + returnStatement.servers[y].url + '; serverName: ' + returnStatement.servers[y].serverName
                        + '; path: ' + returnStatement.servers[y].path + '; description: ' + returnStatement.servers[y].description + query + ')';
                } else {
                    var query = getRedirectQueryString(returnStatement.servers[y].query, '', '&');
                    if (! utils.isEmptyString(query)) {
                        query = "; query: " + query;
                    }

                    simpleReturn += 'url: ' + returnStatement.servers[y].url + query;

                }
                if (y != returnStatement.servers.length - 1) {
                    simpleReturn += ', ';
                }
            }

            if (returnStatement.servers.length > 1) {
                var countDownTime = '; countDownTime = ' + returnStatement.countDownTime + ' seconds, ';
                var privte = ' private = ' + returnStatement.enablePrivate;
                simpleReturn = '\nRETURN SERVER_GROUP [ ' + simpleReturn + countDownTime + privte + ' ]';
            }
            else if (returnStatement.servers.length === 1) {
                simpleReturn = '\nRETURN [ ' + simpleReturn + ' ]';
            }
            return simpleReturn;
        }

        function getRuleReturnDiffText(returnStatement) {
            var SERVER_GROUP_SEPARATOR = '\n\t\t-----';
            var ruleReturnText = '';
            for (var i = 0; i < returnStatement.servers.length; i++) {
                if (returnStatement.servers[i].url.indexOf('{host}') != -1) {
                    var url = URL_TEMPLATE;
                    url = url.replace('protocol', returnStatement.servers[i].protocol);
                    url = url.replace('port', returnStatement.servers[i].port);
                    url = url.replace('URN', returnStatement.servers[i].urn);

                    var query = getRedirectQueryString(returnStatement.servers[i].query, '\n\t\t\t', '');

                    ruleReturnText +=
                        '\n\t\turl: ' + returnStatement.servers[i].url +
                        '\n\t\tpath: ' + returnStatement.servers[i].path +
                        '\n\t\tisNonWhitelistedOnly: ' + returnStatement.servers[i].isNonWhitelisted;

                    if ( !utils.isEmptyString(query) ) {
                        ruleReturnText += '\n\t\tquery: ' + query;
                    }
                } else {
                    var query = getRedirectQueryString(returnStatement.servers[i].query, '\n\t\t\t', '');
                    ruleReturnText += '\n\turl: ' + returnStatement.servers[i].url;
                    if ( !utils.isEmptyString(query) ) {
                        ruleReturnText += '\n\t\tquery: ' + query;
                    }
                }
                if (i != returnStatement.servers.length - 1) {
                    ruleReturnText += SERVER_GROUP_SEPARATOR;
                }
            }

            if (returnStatement.servers.length > 1) {
                var countDownTime = ' countDownTime = ' + returnStatement.countDownTime + ' seconds, ';
                var private_res = ' private = ' + returnStatement.enablePrivate;
                ruleReturnText = '\nRETURN SERVER_GROUP\n   [ ' + ruleReturnText + SERVER_GROUP_SEPARATOR
                    + '\n\t' + countDownTime + private_res + '\n   ]';
            }
            else if (returnStatement.servers.length === 1) {
                ruleReturnText = '\nRETURN\n\t[ ' + ruleReturnText + '\n\t]';
            }
            return ruleReturnText;
        }

        function getRedirectQueryString(query, prefix, separator) {
            var result = "";

            if (! utils.isEmptyObject(query)) {
                angular.forEach(query.entry, function(entry, index) {
                    result += prefix + ((index > 0) ? separator : '') + entry.key + '=' + entry.value;
                });
            }

            return result;
        }
    }
})();
