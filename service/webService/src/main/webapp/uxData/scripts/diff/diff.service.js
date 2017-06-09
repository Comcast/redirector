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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

(function () {
    'use strict';
    angular
        .module('uxData.diff')
        .factory('diffService', diffService);

    diffService.$inject = ['diffLineService', 'diffTestCaseService'];

    function diffService(diffLineService, diffTestCaseService) {
        var service = {
            formattedDiff: formattedDiff
        };

        return service;

        function formattedDiff(originalText, editedText, resultSide, config) {
            if (config.type === 'testSuite') {
                var lines = diffTestCaseService.sortedLines(originalText, editedText);
                originalText = lines[0];
                editedText = lines[1];
            }

            var results = diff(originalText, editedText, config);

            var lines = lineUpText(originalText, editedText, results);

            var originalLines = lines[0];
            var editedLines = lines[1];

            findModifiedLines(originalLines, editedLines, results);

            var lineDiffs = diffModifiedLines(originalLines, editedLines, results, config);

            var lineFormatterResult = diffLineService.lineFormatter(results, lineDiffs);

            var deletedText = lineFormatterResult.formatLeftText(originalLines, config);
            var addedText = lineFormatterResult.formatRightText(editedLines, config);
            // "left" side - current text changes beatify view; "right" side - changed text changes beatify view;
            if (angular.lowercase(resultSide) == "left") {
                return deletedText;
            }
            return addedText;
        }

        function lineUpText(originalText, editedText, results) {
            var originalLines = split(originalText);
            var editedLines = split(editedText);

            padBlankLines(originalLines);
            padBlankLines(editedLines);

            results.paddingLeft = diffLineService.editSet();
            results.paddingRight = diffLineService.editSet();

            for (var i = 0; i < Math.max(originalLines.length, editedLines.length); i++) {
                if (!results.deleted.contains(i) && results.added.contains(i)) {
                    originalLines.splice(i, 0, ' ');
                    results.deleted.updateNumbers(i);
                    results.paddingLeft.add(i);
                } else if (results.deleted.contains(i) && !results.added.contains(i)) {
                    editedLines.splice(i, 0, ' ');
                    results.added.updateNumbers(i);
                    results.paddingRight.add(i);
                }
            }

            return [originalLines, editedLines];
        }

        function findModifiedLines(originalLines, editedLines, results) {
            results.modifiedRight = diffLineService.editSet();
            results.modifiedLeft = diffLineService.editSet();
            for (var i = 0; i < originalLines.length && i < editedLines.length; i++) {
                if (results.added.contains(i) && results.deleted.contains(i)) {
                    results.modifiedLeft.add(i);
                    results.modifiedRight.add(i);
                } else if (results.added.contains(i) && results.modifiedRight.contains(i - 1)) {
                    results.modifiedRight.add(i);
                } else if (results.deleted.contains(i) && results.modifiedLeft.contains(i - 1)) {
                    results.modifiedLeft.add(i);
                }
            }
        }

        function diffModifiedLines(originalLines, editedLines, results, config) {
            var lineDiffs = diffLineService.editSet();

            for (var i = 0; i < originalLines.length && i < editedLines.length; i++) {
                if (results.modifiedLeft.contains(i) || results.modifiedRight.contains(i)) {
                    var lineDiff = lineDiffFormatter(originalLines[i], editedLines[i], config);
                    lineDiff.cleanUp();

                    lineDiffs.addValue(i, lineDiff);
                }
            }

            return lineDiffs;
        }

        function diff(originalText, editedText, config) {
            var originalLines = split(originalText);
            var editedLines = split(editedText);

            padBlankLines(originalLines);
            padBlankLines(editedLines);

            var startPos = trimCommonLines(originalLines, editedLines, config);

            var matrix = createMatrix(startPos, originalLines, editedLines);

            fillMatrix(startPos, originalLines, editedLines, matrix, config);

            var results = findAddsAndDeletes(originalLines, editedLines, startPos, matrix, config);

            checkShiftEdits(split(originalText), results.deleted, config);
            checkShiftEdits(split(editedText), results.added, config);

            return results;
        }

        function trimCommonLines(originalLines, editedLines, config) {
            var linesRemaining = function (startPos) {
                return originalLines.length > startPos && editedLines.length > startPos
            };

            var startPos = 0;

            while (linesRemaining(startPos) && linesAreEqual(originalLines[startPos], editedLines[startPos], config)) {
                startPos++;
            }

            while (linesRemaining(startPos) && linesAreEqual(originalLines[originalLines.length - 1], editedLines[editedLines.length - 1], config)) {
                originalLines.pop();
                editedLines.pop();
            }

            return startPos;
        }

        function lineDiffFormatter(originalLine, editedLine, config) {
            var originalTrimmed = checkTrimLeadingWhiteSpace(originalLine, config);
            var editedTrimmed = checkTrimLeadingWhiteSpace(editedLine, config);

            var originalOffset = originalLine.length - originalTrimmed.length;
            var editOffset = editedLine.length - editedTrimmed.length;

            originalTrimmed = trimTrailingWhiteSpace(originalTrimmed);
            editedTrimmed = trimTrailingWhiteSpace(editedTrimmed);

            var matrix = createMatrix(0, originalTrimmed, editedTrimmed);

            fillMatrix(0, originalTrimmed, editedTrimmed, matrix, config);

            return createLineDiff(originalTrimmed, editedTrimmed, originalOffset, editOffset, matrix);
        }

        function trimTrailingWhiteSpace(str) {
            if (str) {
                return str.trimRight();
            }
            return str;
        }

        function checkTrimLeadingWhiteSpace(str, config) {
            if (str && config.ignoreLeadingWS) {
                return str.trimLeft();
            }
            return str;
        }

        function trimWhiteSpace(str, config) {
            if (!str) return str;

            if (config.ignoreLeadingWS) {
                return str.trim();
            } else {
                return str.trimRight();
            }
        }

        function createLineDiff(originalTrimmed, editedTrimmed, originalOffset, editOffset, matrix) {
            var diff = diffLineService.lineDiff();

            var i = originalTrimmed.length;
            var j = editedTrimmed.length;

            while (i >= 0 && j >= 0) {
                if (originalTrimmed[i - 1] === editedTrimmed[j - 1]) {
                    if (originalTrimmed[i - 1]) {
                        diff.addCommon(originalOffset + i - 1, editOffset + j - 1);
                    }
                    i--;
                    j--;
                } else if (j >= 0 && (i === 0 || matrix[i][j - 1] >= matrix[i - 1][j])) {
                    if (editedTrimmed[j - 1].length > 0) {
                        diff.addInsert(editOffset + j - 1);
                    }
                    j--;
                } else if (i >= 0 && (j === 0 || matrix[i][j - 1] < matrix[i - 1][j])) {
                    if (originalTrimmed[i - 1].length > 0) {
                        diff.addDelete(originalOffset + i - 1);
                    }
                    i--;
                }
            }

            return diff;
        }

        function findAddsAndDeletes(originalLines, editedLines, startPos, matrix, config) {
            var i = originalLines.length;
            var j = editedLines.length;

            var added = diffLineService.editSet();
            var deleted = diffLineService.editSet();

            var allAddsOrDeletes = checkAllAddsOrDeletes(originalLines, editedLines, added)
                || checkAllAddsOrDeletes(editedLines, originalLines, deleted);

            if (!allAddsOrDeletes) {
                while (i >= startPos && j >= startPos) {
                    var m = i - startPos;
                    var n = j - startPos;
                    if (m > 0 && n > 0 && linesAreEqual(originalLines[i - 1], editedLines[j - 1], config)) {
                        i--;
                        j--;
                    } else if (j >= startPos && (i === startPos || matrix[m][n - 1] >= matrix[m - 1][n])) {
                        if (j - 1 >= startPos && editedLines[j - 1].length > 0) {
                            added.add(j - 1);
                        }
                        j--;
                    } else if (i >= startPos && (j === startPos || matrix[m][n - 1] < matrix[m - 1][n])) {
                        if (i - 1 >= startPos && originalLines[i - 1].length > 0) {
                            deleted.add(i - 1);
                        }
                        i--;
                    }
                }
            }

            return {added: added, deleted: deleted};
        }

        function checkAllAddsOrDeletes(lines, otherLines, editSet) {
            if (lines.length === 1 && lines[0] === '') {
                for (var i = 0; i < otherLines.length; i++) {
                    editSet.add(i);
                }
                return true;
            }
            return false;
        }

        function linesAreEqual(line1, line2, config) {
            return trimWhiteSpace(line1, config) === trimWhiteSpace(line2, config);
        }

        //Find all continuous runs of inserts or deletes. For each run, see if it can be shifted forward 1 line.
        //This is useful for properly pairing opening and closing braces in C-like languages, for example.
        function checkShiftEdits(textLines, editSet, config) {
            var editArray = editSet.all();
            if (editArray.length > 0) {
                var startRun = editArray[0];

                var current = startRun;
                for (var i = 1; i < editArray.length; i++) {
                    if (i === editArray.length - 1) {   //end of the run and the edits
                        if (editArray[i] === current + 1) {
                            current++;
                        }
                        checkShiftRun(textLines, editSet, startRun, current, config);
                    } else if (editArray[i] === current + 1) {
                        current++;
                    } else {    //end of the run
                        checkShiftRun(textLines, editSet, startRun, current, config);

                        startRun = current = editArray[i];
                    }
                }
            }
        }

        function checkShiftRun(textLines, editSet, startRun, endRun, config) {
            if (linesAreEqual(textLines[startRun], textLines[endRun + 1], config) && lineIsBlank(textLines[startRun + 1])) {
                editSet.remove(startRun);
                editSet.add(endRun + 1);
            }
        }

        function lineIsBlank(line) {
            return /^\s*$/.test(line);
        }

        function createMatrix(startPos, originalLines, editedLines) {
            var matrix = [];
            for (var i = 0; i <= originalLines.length - startPos; i++) {
                matrix[i] = new Array(editedLines.length - startPos + 1);
                matrix[i][0] = 0;
            }

            for (var j = 1; j <= editedLines.length - startPos; j++) {
                matrix[0][j] = 0;
            }

            return matrix;
        }

        function fillMatrix(startPos, originalLines, editedLines, matrix, config) {
            for (var i = 1; i <= originalLines.length - startPos; i++) {
                var originalTrimmed = trimWhiteSpace(originalLines[i + startPos - 1], config);
                for (var j = 1; j <= editedLines.length - startPos; j++) {
                    var trimmedEdit = trimWhiteSpace(editedLines[j + startPos - 1], config);
                    if (originalTrimmed === trimmedEdit) {
                        matrix[i][j] = matrix[i - 1][j - 1] + 1;
                    } else {
                        matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
                    }
                }
            }
        }

        function padBlankLines(lines) {
            if (lines.length === 1 && lines[0] === '') {
                return;
            }

            for (var l = 0; l < lines.length; l++) {
                if (lines[l] === '') {
                    lines[l] = ' ';
                }
            }

        }

        function split(string) {
            return string.split(/\r?\n/);
        }
    }
})();
