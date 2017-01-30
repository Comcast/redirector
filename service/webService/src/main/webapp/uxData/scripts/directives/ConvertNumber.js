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

angular.module('uxData').directive('convertNumber',
    function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                var POWER_MILLION = 6;
                var SYMBOL_MILLION = ' M';
                var DELIMITER_POINT = '.';
                var ZERO = '0';
                var MAX_DIGITS = 12;

                ngModel.$parsers.unshift(function (viewVal) {
                    if (isValid(viewVal)) {
                        ngModel.$setValidity('convertNumber', true);
                        return viewVal;
                    } else {
                        ngModel.$setValidity('convertNumber', false);
                        return void 0;
                    }
                });

                ngModel.$formatters.push(function (val) {
                    if ((val == null)) {
                        return val;
                    }
                    if (val.toString() !== '0') {
                        val = removeLeadingZero(val);
                    }
                    if (val.length > POWER_MILLION) {
                        val = convertMillionFormat(val.toString());
                    }
                    return val;
                });

                element.on('blur', function () {
                    var formatter;
                    var viewValue = ngModel.$modelValue;
                    if ((viewValue == null)) {
                        return;
                    }
                    var formatters = ngModel.$formatters;
                    var len = formatters.length;
                    for (var i = 0; i < len; i++) {
                        formatter = formatters[i];
                        viewValue = formatter(viewValue);
                    }
                    ngModel.$viewValue = viewValue;
                    return ngModel.$render();
                });

                element.on('focus', function () {
                    var val = element.val();
                    if (val !== ZERO && val.indexOf(SYMBOL_MILLION) !== -1) {
                        val = val.replace(SYMBOL_MILLION, '');
                        val = addTrillingZero(val);
                        element.val(val.replace(DELIMITER_POINT, ''));
                    }
                    return element[0].select();
                });

                var convertMillionFormat = function (val) {
                    val = val.replace(/(\d)(?=(\d{6})+(?!\d))/g, '$1.');
                    val = removeTrillingZero(val);
                    if ((val.indexOf(DELIMITER_POINT) + 1) === val.length) {
                        val = val.replace(DELIMITER_POINT, '');
                    }
                    val = val + SYMBOL_MILLION;
                    return val;
                };

                var removeTrillingZero = function (val) {
                    val = val.replace(/0+$/, '');
                    return val;
                };

                var removeLeadingZero = function (val) {
                    val = val.replace(/^0+/, '');
                    return val;
                };

                var addTrillingZero = function (val) {
                    var len = val.indexOf(DELIMITER_POINT) === -1 ? 0 : val.substring(val.indexOf(DELIMITER_POINT), val.length - 1).length;
                    for (var i = 0; i < POWER_MILLION - len; i++) {
                        val = val + ZERO;
                    }
                    return val;
                };

                var isValid = function (val) {
                    return isPositiveIntegerNumber(val) && isMaxDigits(val, MAX_DIGITS);
                };

                var isPositiveIntegerNumber = function (val) {
                    return isInteger(val) && isFinite(val) && (val > 0);
                };

                var isMaxDigits = function (val, maxDigits) {
                    return val.toString().length < maxDigits;
                };

                function isInteger(val) {
                    return val.indexOf('.') === -1;
                }
            }
        };
    });
