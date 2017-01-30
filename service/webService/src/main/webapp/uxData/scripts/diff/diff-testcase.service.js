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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

(function () {
    'use strict';
    angular
        .module('uxData.diff')
        .factory('diffTestCaseService', diffTestCaseService);

    function diffTestCaseService() {
        var service = {
            sortedLines: sortedLines
        };

        return service;

        function sortedLines(originalText, editedText) {
            var originalLines = split(originalText);
            var editedLines = split(editedText);

            deletedLastComma(originalLines);
            deletedLastComma(editedLines);

            for (var i = 1; i < originalLines.length - 1; i++) {
                for (var j = 1; j < editedLines.length - 1; j++) {
                    if (originalLines[i] === editedLines[j]) {
                        arrayMove(originalLines, i, 1);
                        break;
                    }
                }
            }
            for (var i = 1; i < originalLines.length - 1; i++) {
                for (var j = 1; j < editedLines.length - 1; j++) {
                    if (nameFieldsAreEqual(originalLines[i], editedLines[j])) {
                        arrayMove(editedLines, j, i);
                    }
                }
            }

            var originalLinesString = createStringByArray(originalLines);
            var editedLinesString = createStringByArray(editedLines);

            return [originalLinesString, editedLinesString];
        }

        function arrayMove(arr, fromIndex, toIndex) {
            var element = arr[fromIndex];
            arr.splice(fromIndex, 1);
            arr.splice(toIndex, 0, element);
        }

        function nameFieldsAreEqual(line1, line2) {
            var nameField1 = line1.slice(0, line1.indexOf(":"));
            var nameField2 = line2.slice(0, line2.indexOf(":"));
            return nameField1 === nameField2;
        }

        function createStringByArray(array) {
            var output = '';
            angular.forEach(array, function (object) {
                output += object + "\n";
            });
            return output;
        }

        function deletedLastComma(lines) {
            for (var i = 1; i < lines.length - 1; i++) {
                if (lines[i].lastIndexOf(",") === (lines[i].length - 1)) {
                    lines[i] = lines[i].slice(0, lines[i].lastIndexOf(","));
                }
            }
        }

        function split(string) {
            return string.split(/\r?\n/);
        }
    }
})();
