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
 */
// This provider holds constants which are used in several controllers.

(function() {
    'use strict';
angular.module('uxData.services')

    .factory("constantsProvider",
    function () {

        // RulesController, ExpressionController use these constants
        var LEFT = "left";
        var RIGHT = "right";
        var OR = "OR";
        var XOR = "XOR";
        var HOST = "{host}";

        var PENDING_CHANGE_TYPE = {
            ADD : "ADD",
            UPDATE : "UPDATE",
            DELETE : "DELETE"
        };

        return {
            LEFT : LEFT,
            RIGHT: RIGHT,
            OR: OR,
            XOR: XOR,
            HOST: HOST,
            PENDING_CHANGE_TYPE : PENDING_CHANGE_TYPE
        }
    });
})();
