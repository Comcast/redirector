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
        .factory('diffLineService', diffLineService);

    function diffLineService() {
        var service = {
            lineDiff: lineDiff,
            lineFormatter: lineFormatter,
            editSet: editSet
        };

        return service;

        function lineDiff() {
            var _added = [];
            var _deleted = [];
            var _common = [];

            var addCommon = function (leftPosition, rightPosition) {
                _common.unshift({
                    leftPosition: leftPosition,
                    leftEndPosition: leftPosition,
                    rightPosition: rightPosition,
                    rightEndPosition: rightPosition
                });
            };

            var addDelete = function (position) {
                _deleted.unshift({
                    position: position,
                    endPosition: position
                });
            };

            var addInsert = function (position) {
                _added.unshift({
                    position: position,
                    endPosition: position
                });
            };

            var editLength = function (edit) {
                if (!edit) {
                    return 0;
                }
                return edit.endPosition - edit.position + 1;
            };

            var cleanUp = function () {
                mergeAdjacent(_added);
                mergeAdjacent(_deleted);
                mergeAdjacentCommon();

                do {
                    var didMerge = false;
                    for (var i = 0; i < _common.length; i++) {
                        var equalityLength = _common[i].leftEndPosition - _common[i].leftPosition + 1;

                        var leftDelete = findEditWithEndingPosition(_deleted, _common[i].leftPosition - 1);
                        var rightDelete = findEditWithPosition(_deleted, _common[i].leftEndPosition + 1);

                        var leftAdd = findEditWithEndingPosition(_added, _common[i].rightPosition - 1);
                        var rightAdd = findEditWithPosition(_added, _common[i].rightEndPosition + 1);
                        if (equalityLength <= 8 && editLength(leftDelete) + editLength(leftAdd) >= equalityLength
                            && editLength(rightDelete) + editLength(rightAdd) >= equalityLength) {
                            didMerge = true;
                            if (leftDelete && rightDelete) {
                                leftDelete.endPosition = rightDelete.endPosition;
                                removeEdit(_deleted, rightDelete);
                            } else if (leftDelete) {
                                leftDelete.endPosition = _common[i].leftEndPosition;
                            } else if (rightDelete) {
                                rightDelete.position = _common[i].leftPosition;
                            } else {
                                addEdit(_deleted, _common[i].leftPosition, _common[i].leftEndPosition);
                            }

                            if (leftAdd && rightAdd) {
                                leftAdd.endPosition = rightAdd.endPosition;
                                removeEdit(_added, rightAdd);
                            } else if (leftAdd) {
                                leftAdd.endPosition = _common[i].rightEndPosition;
                            } else if (rightAdd) {
                                rightAdd.position = _common[i].rightPosition;
                            } else {
                                addEdit(_added, _common[i].rightPosition, _common[i].rightEndPosition);
                            }

                            _common.splice(i, 1);
                        }
                    }
                } while (didMerge)
            };

            var mergeAdjacentCommon = function () {
                for (var i = 0; i < _common.length; i++) {
                    if (i + 1 < _common.length
                        && _common[i].leftEndPosition + 1 === _common[i + 1].leftPosition
                        && _common[i].rightEndPosition + 1 === _common[i + 1].rightPosition) {
                        _common[i].leftEndPosition = _common[i + 1].leftEndPosition;
                        _common[i].rightEndPosition = _common[i + 1].rightEndPosition;
                        _common.splice(i + 1, 1);
                        i--;
                    }
                }
            };

            var addEdit = function (edits, position, endPosition) {
                var newEdit = {
                    position: position,
                    endPosition: endPosition
                };

                if (edits.length === 0) {
                    edits.push(newEdit);
                } else if (position < edits[0].position) {
                    edits.unshift(newEdit);
                } else {
                    for (var i = edits.length - 1; i >= 0; i--) {
                        if (position > edits[i].position) {
                            edits.splice(i + 1, 0, newEdit);
                            break;
                        }
                    }
                }
            };

            var removeEdit = function (edits, item) {
                for (var i = 0; i < edits.length; i++) {
                    if (edits[i] === item) {
                        edits.splice(i, 1);
                        break;
                    }
                }
            };

            var findEditWithPosition = function (edits, pos) {
                for (var i = 0; i < edits.length; i++) {
                    if (edits[i].position === pos) {
                        return edits[i];
                    }
                }
            };

            var findEditWithEndingPosition = function (edits, endPos) {
                for (var i = 0; i < edits.length; i++) {
                    if (edits[i].endPosition === endPos) {
                        return edits[i];
                    }
                }
            };

            var mergeAdjacent = function (edits) {
                for (var i = 0; i < edits.length; i++) {
                    if (i + 1 < edits.length && edits[i].endPosition + 1 === edits[i + 1].position) {
                        edits[i].endPosition = edits[i + 1].endPosition;
                        edits.splice(i + 1, 1);
                        i--;
                    }
                }
            };

            return {
                addDelete: addDelete,
                addInsert: addInsert,
                addCommon: addCommon,
                cleanUp: cleanUp,
                added: _added,
                deleted: _deleted,
                common: _common
            };
        }

        function editSet() {
            var _set = {};

            var add = function (line) {
                _set[line] = true;
            };

            var addValue = function (line, value) {
                _set[line] = value;
            };

            var remove = function (line) {
                _set[line] = undefined;
            };

            var count = function () {
                return all().length;
            };

            var get = function (line) {
                return _set[line];
            };

            var sortIntegers = function (a, b) {
                return a - b;
            };

            var all = function () {
                var arr = [];

                for (var prop in _set) {
                    if (_set[prop]) {
                        arr.push(parseInt(prop));
                    }
                }

                return arr.sort(sortIntegers);
            };

            var contains = function (lineNumber) {
                return _set[lineNumber] !== undefined;
            };

            var updateNumbers = function (lineNumber) {
                var newSet = {};

                for (var prop in _set) {
                    var value = _set[prop];
                    if (value) {
                        var parsed = parseInt(prop);
                        if (parsed >= lineNumber) {
                            newSet[parsed + 1] = value;
                        } else {
                            newSet[parsed] = value;
                        }
                    }
                }

                _set = newSet;
            };

            return {
                add: add,
                addValue: addValue,
                get: get,
                remove: remove,
                count: count,
                all: all,
                updateNumbers: updateNumbers,
                contains: contains
            };
        }

        function lineFormatter(results, lineDiffs) {
            var anchors = editSet();

            var added = results.added.all();
            var deleted = results.deleted.all();

            var lineIsCommon = function (i) {
                return !results.added.contains(i) && !results.deleted.contains(i)
            };

            if (!lineIsCommon(0)) {
                anchors.add(0);
            }

            for (var i = 0; i < Math.max(Math.max.apply(null, added), Math.max.apply(null, deleted)); i++) {
                if (lineIsCommon(i) && !lineIsCommon(i + 1)) {
                    anchors.add(i);
                }
            }

            var formatLeftText = function (text1Lines, config) {
                var deletedText = '';

                var startingPos = getStartingPos(results, config);
                var text1EndingPos = getEndingPos(results, text1Lines, config);

                for (var i = startingPos; i < text1EndingPos; i++) {
                    if (anchors.contains(i)) {
                        deletedText += '<a name="' + i + '"></a>';
                    }
                    if (lineDiffs.contains(i) && results.modifiedLeft.contains(i)) {
                        var lineDiff = lineDiffs.get(i);
                        deletedText += appendModifiedLine(text1Lines[i], lineDiff.deleted);
                    } else {
                        var className = getClassNameLeft(results, i);
                        deletedText += appendLine(className, text1Lines[i]);
                    }
                }

                return deletedText;
            };

            var formatRightText = function (text2Lines, config) {
                var addedText = '';
                var startingPos = getStartingPos(results, config);
                var text2EndingPos = getEndingPos(results, text2Lines, config);

                for (var i = startingPos; i < text2EndingPos; i++) {
                    if (lineDiffs.contains(i) && results.modifiedRight.contains(i)) {
                        var lineDiff = lineDiffs.get(i);
                        addedText += appendModifiedLine(text2Lines[i], lineDiff.added);
                    } else {
                        var className = getClassNameRight(results, i);
                        addedText += appendLine(className, text2Lines[i]);
                    }
                }
                return addedText;
            };

            var appendModifiedLine = function (textLine, lineEdits) {
                var formattedText = '<span class="modified">';
                var startIndex = 0;
                for (var j = 0; j < lineEdits.length; j++) {
                    formattedText += escapeHtml(textLine.substring(startIndex, lineEdits[j].position));
                    startIndex = lineEdits[j].endPosition + 1;
                    formattedText += '<span class="modified-light">' + escapeHtml(textLine.substring(lineEdits[j].position, startIndex))
                        + '</span>';
                }

                if (startIndex < textLine.length) {
                    formattedText += escapeHtml(textLine.substring(startIndex, textLine.length));
                }

                formattedText += '</span><br>';

                return formattedText;
            };

            var getStartingPos = function (results, config) {
                if (config.showAllContent) {
                    return 0;
                }
                var allDeletes = results.deleted.all();

                var firstDelete = allDeletes.length > 0
                    ? allDeletes[0]
                    : -1;

                var allAdds = results.added.all();

                var firstAdd = allAdds.length > 0
                    ? allAdds[0]
                    : -1;

                var firstEdit;
                if (firstDelete === -1) {
                    firstEdit = firstAdd;
                } else if (firstAdd === -1) {
                    firstEdit = firstDelete;
                } else {
                    firstEdit = Math.min(firstDelete, firstAdd)
                }

                return Math.max(0, firstEdit - 10);
            };

            var getEndingPos = function (results, lines, config) {
                if (config.showAllContent) {
                    return lines.length;
                }
                var allDeletes = results.deleted.all();

                var lastDelete = allDeletes.length > 0
                    ? allDeletes[allDeletes.length - 1]
                    : 0;

                var allAdds = results.added.all();

                var lastAdd = allAdds.length > 0
                    ? allAdds[allAdds.length - 1]
                    : 0;

                var lastEdit = Math.max(lastDelete, lastAdd);

                return Math.min(lines.length, lastEdit + 11);
            };

            var getClassNameLeft = function (results, i) {
                var className = '';
                if (results.modifiedLeft.contains(i)) {
                    className = 'modified';
                } else if (results.paddingLeft.contains(i)) {
                    className = 'padding';
                } else if (results.deleted.contains(i)) {
                    className = 'deleted';
                }
                return className;
            };

            var getClassNameRight = function (results, i) {
                var className = '';
                if (results.modifiedRight.contains(i)) {
                    className = 'modified';
                } else if (results.paddingRight.contains(i)) {
                    className = 'padding';
                } else if (results.added.contains(i)) {
                    className = 'inserted';
                }
                return className;
            };

            var appendLine = function (className, line) {
                var append = '';

                if (className != '') {
                    append += '<span class="' + className + '">';
                }
                append += escapeHtml(line);
                if (className != '') {
                    append += '</span>';
                }
                append += '<br>';
                return append;
            };

            var escapeHtml = function (string) {
                var entityMap = {
                    '&': '&amp;',
                    '<': '&lt;',
                    '>': '&gt;',
                    '"': '&quot;',
                    "'": '&#39;',
                    '/': '&#x2F;'
                };

                var replacedTabs = string.replace(/\t/g, '   ');

                return String(replacedTabs).replace(/[&<>"'\/]/g, function (s) {
                    return entityMap[s];
                });
            };

            var getEditIterator = function () {
                return anchorIterator(anchors);
            };

            return {
                formatLeftText: formatLeftText,
                formatRightText: formatRightText,
                getEditIterator: getEditIterator
            };
        }

        function anchorIterator(anchors) {
            var allAnchors = anchors.all();
            var currentIndex = 0;

            var getNextEdit = function () {
                if (currentIndex + 1 < allAnchors.length) {
                    currentIndex++;
                    return allAnchors[currentIndex];
                }
                return false;
            };

            var getPrevEdit = function () {
                if (currentIndex - 1 >= 0) {
                    currentIndex--;
                    return allAnchors[currentIndex];
                }
                return false;
            };

            return {
                getNextEdit: getNextEdit,
                getPrevEdit: getPrevEdit
            };
        }

    }
})();
