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
        .module('uxData.rules')
        .factory('rulesValidationService', rulesValidationService);

    rulesValidationService.$inject = ['rulesEditService', 'RULES_CONSTANTS', 'SERVER_CONSTANTS', 'REGEXP_CONSTANTS',
        'rulesAlertsService', 'utilsService'];

    function rulesValidationService(rulesEditService, RULES_CONSTANTS, serverCONST, regexpCONST,
                                    rulesAlertsService, utils) {


        var LEFT = RULES_CONSTANTS().LEFT;
        var RIGHT = RULES_CONSTANTS().RIGHT;

        var SIMPLE = serverCONST().EXP_EDIT_MODE.SIMPLE;
        var ADVANCED = serverCONST().EXP_EDIT_MODE.ADVANCED;

        var service = {
            validateDuplicatesOfAllExpressions: validateDuplicatesOfAllExpressions,
            validateDuplicatedRelationalExpressions: validateDuplicatedRelationalExpressions,
            revalidateDuplicatesOnRemove: revalidateDuplicatesOnRemove,
            validateReturnValue: validateServers,
            validateServer: validateServerAndFillInTheResult
        };
        return service;

//================================================== Public section ==================================================//

        function validateDuplicatesOfAllExpressions(expressions, validationData) {

            // since we are going to revalidate all duplicates of all expressions
            // we need to clear all results of previous results of
            angular.forEach(validationData, function(validationObj, index) {
                angular.forEach(validationObj, function(expression, side){
                    expression.duplicates = {};
                    expression.generalErrorMsg = '';
                });
            });

            angular.forEach(expressions, function(expression, expIndex) {
                angular.forEach(expression, function(operand, operandSide) {
                    if(angular.equals(operandSide, LEFT) || angular.equals(operandSide, RIGHT)) {
                        validateDuplicatedRelationalExpressions(expressions, validationData, expIndex, operandSide);
                    }
                });
            });

        }

        function validateDuplicatedRelationalExpressions(expressions, validationData, changedIndex, changedSide) {

            // I. if current expression is binary, check whether its left and right operands duplicate each other
            if (rulesEditService.isBinary(expressions[changedIndex].type)) {
                if (hasDuplicatesInBinaryExpression(expressions[changedIndex][LEFT], expressions[changedIndex][RIGHT])) {
                    validationData[changedIndex][LEFT].duplicates[changedIndex + RIGHT] = changedIndex;
                    validationData[changedIndex][RIGHT].duplicates[changedIndex + LEFT] = changedIndex;
                }
                else {
                    // it's possible that expressions were in conflict(duplicates), but now they are not
                    // so remove indexes to duplicates
                    delete validationData[changedIndex][LEFT].duplicates[changedIndex + RIGHT];
                    delete validationData[changedIndex][RIGHT].duplicates[changedIndex + LEFT];
                }
                updateDuplicatesErrorMessage(validationData[changedIndex], expressions[changedIndex]);
            }


            // II. check for duplicates with rest of the expressions
            angular.forEach(expressions, function (expression, index) {
                if (index !== changedIndex) {
                    angular.forEach(expression, function (operandValue, operandSide) {
                        if (rulesEditService.isRelationalOperator(operandValue.expOperator)) {
                            if (areExpressionsDuplicates(operandValue, expressions[changedIndex][changedSide])) {
                                validationData[index][operandSide].duplicates[changedIndex + changedSide] = changedIndex;
                                validationData[changedIndex][changedSide].duplicates[index + operandSide] = index;
                            }
                            else {
                                delete validationData[index][operandSide].duplicates[changedIndex + changedSide];
                                delete validationData[changedIndex][changedSide].duplicates[index + operandSide];
                            }
                            updateDuplicatesErrorMessage(validationData[changedIndex], expressions[changedIndex]);
                            updateDuplicatesErrorMessage(validationData[index], expressions[index]);
                        }
                    });
                }
            });
        }

        /**
         * update indexes of duplicates
         * index here is a number of expression which duplicates current expression
         * Example:
         * Consider we have 3 expressions and left operand of expression 1 has is a duplicate of
         * left operand of expression no. 2 and right operand of expression no. 3.
         * Then duplicates of validation objects of each expression will look like:
         * duplicates of first expr:  duplicates: {2left: 2, 3right: 3}
         * duplicates of second expr: duplicates: {1left: 1, 3right: 3}
         * duplicates of third expr:  duplicates: {1left: 1, 2left:  2}
         *
         * @param removedIndex
         * @param expressions
         * @param validationData
         */
        function revalidateDuplicatesOnRemove(removedIndex, expressions, validationData) {

            angular.forEach(validationData, function (validationObject, index) {
                angular.forEach(validationObject, function (operand, key) {
                    // 1. delete everywhere index of removed expression
                    delete operand.duplicates[removedIndex + LEFT];
                    delete operand.duplicates[removedIndex + RIGHT];
                    var duplicatesCopy = angular.copy(operand.duplicates);
                    operand.duplicates = {};
                    // 2. since expression was removed, total count of elements decreased
                    //    so we need to update indexes
                    angular.forEach(duplicatesCopy, function (duplicateIndex, key) {
                        // 2.1 if index (number of expression
                        if (duplicateIndex < removedIndex) {
                            operand.duplicates[key] = duplicateIndex;
                        }
                        else if (duplicateIndex >= removedIndex) {
                            var side = key.replace(/\d/g, '');
                            var newIndex = (duplicateIndex - 1) + side;
                            operand.duplicates[newIndex] = duplicateIndex - 1;
                        }
                    });

                    updateDuplicatesErrorMessage(validationData[index], expressions[index]);
                });
            });

            if (validationData.length > 0) {
                if (removedIndex - 1 < 0) {
                    validateDuplicatedRelationalExpressions(expressions, validationData, 0, LEFT);
                }
                else {
                    validateDuplicatedRelationalExpressions(expressions, validationData, removedIndex - 1, LEFT);
                }
            }
        }

        function validateServers(vm) {
            if (vm.uiModelHolder.servers.length === 0) {
                rulesAlertsService.failedToSave('rule', 'Rule must contain at least one return server');
                return false;
            }

            var serversAreValid = true;
            angular.forEach(vm.uiModelHolder.servers, function (server, index) {
                if (!validateServerAndFillInTheResult(index, vm)) {
                    serversAreValid = false;
                }
            });
            return serversAreValid;
        }

//================================================== Private section =================================================//
        function areExpressionsDuplicates(expr1, expr2) {

            return rulesEditService.isRelationalOperator(expr1.expOperator) &&
                rulesEditService.isRelationalOperator(expr2.expOperator) &&
                angular.equals(expr1.expOperator, expr2.expOperator) &&
                angular.isDefined(expr1.expName) &&
                angular.isDefined(expr2.expName) &&
                angular.equals(expr1.expName, expr2.expName) &&
                expr1.expName.replace(/\s/g, '').length > 0;
        }

        function updateDuplicatesErrorMessage(exprValidationObject, rawExpression) {

            // iterate over left and right operands of expression and update error message for each
            // see desc. of structure of vm.exprValidationData object in RulesEditController
            angular.forEach(exprValidationObject, function (expression, operandSide) {

                var duplicatesArray = [];
                angular.forEach(expression.duplicates, function (value, key) {
                    if ($.inArray(value + 1, duplicatesArray) === -1) {
                        duplicatesArray.push(value + 1);
                    }
                });

                if (duplicatesArray.length > 0) {
                    duplicatesArray.sort();
                    var duplicatesStr = '';
                    angular.forEach(duplicatesArray, function (value, index) {
                        duplicatesStr += value + ', ';
                    });
                    duplicatesStr = duplicatesStr.substr(0, duplicatesStr.length - 2);
                    expression['generalErrorMsg'] = 'Expression ' + rawExpression[operandSide].expOperator + ' has a duplicate in condition(s) no: ' + duplicatesStr;
                }
                else {
                    expression['generalErrorMsg'] = '';
                }
            });
        }

        function hasDuplicatesInBinaryExpression(left, right) {

            return rulesEditService.isRelationalOperator(left.expOperator) &&
                rulesEditService.isRelationalOperator(right.expOperator) &&
                angular.equals(left.expOperator, right.expOperator) &&
                angular.isDefined(left.expName) &&
                angular.isDefined(right.expName) &&
                angular.equals(left.expName, right.expName) &&
                left.expName.replace(/\s/g, '').length > 0;
        }

        function validateServerAndFillInTheResult(index, vm) {
            var server = vm.uiModelHolder.servers[index];
            var validationResult = validateServer(server);

            if(validationResult.isValid === true) {
                vm.serverValidationData[index].pathError = '';
                vm.serverValidationData[index].urlError = '';
                vm.serverValidationData[index].queryError = [];
            } else {
                if (validationResult.invalidQuery.length > 0) {
                    var queryError = [];
                    for (var i = 0; i< server.query.entry.length; i++) {
                        var isError = false;
                        for (var j in validationResult.invalidQuery) {
                            if (validationResult.invalidQuery[j] == i) {
                                isError = true;
                                break;
                            }
                        }
                        queryError.push((isError) ? "Query key/value should not be empty" : "");
                    }

                    vm.serverValidationData[index].queryError = queryError;
                } else {
                    vm.serverValidationData[index].queryError = [];
                }

                if (angular.isUndefined(server.editMode) || angular.equals(server.editMode, serverCONST().EXP_EDIT_MODE.SIMPLE)) {
                    vm.serverValidationData[index].pathError = validationResult.errorMsg;
                }
                else {
                    vm.serverValidationData[index].urlError = validationResult.errorMsg;
                }
            }

            return validationResult.isValid;
        }

        function validateServer(server) {
            var result = {isValid: true, errorMsg: '', invalidQuery: []};

            if (angular.equals(server.editMode, SIMPLE) && utils.isEmptyString(server.path)) {
                result.errorMsg = 'Service path is required';
                result.isValid = false;
            }
            else if (angular.equals(server.editMode, ADVANCED)
                    && !regexpCONST().urlPattern.test(server.url)
                    && !regexpCONST().urlIPv6Pattern.test(server.url)) {
                result.errorMsg = 'Valid service url is required. Valid service url must match pattern: protocol://host[:port][/urn]';
                result.isValid = false;
            }
            if (! utils.isEmptyObject(server.query)) {
                angular.forEach(server.query.entry, function(entry, index) {
                    if (utils.isEmptyString(entry.key) || utils.isEmptyString(entry.value)) {
                        result.isValid = false;
                        result.invalidQuery.push(index);
                    }
                });
            }
            return result;
        }
    }
})();
