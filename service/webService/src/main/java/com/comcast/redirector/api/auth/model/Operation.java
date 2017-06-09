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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.auth.model;

import javax.ws.rs.HttpMethod;

public enum Operation {
    READ, WRITE, NONE;

    public static Operation fromString(String substring) {
        switch (substring.toUpperCase()) {
            case "READ" :
                return READ;
            case "WRITE" :
                return WRITE;
            default:
                return NONE;
        }
    }

    public static Operation fromHttpMethod(String method) {
        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
            return READ;
        } else {
            return WRITE;
        }
    }
}
