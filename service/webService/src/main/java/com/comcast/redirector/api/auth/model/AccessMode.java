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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.auth.model;

import java.util.Optional;
import java.util.stream.Stream;

public enum AccessMode {
    READ("read"), WRITE("write"), ACCESS_APP("redirector-accessApp"),
    DENY_READ("deny-read"), DENY_WRITE("deny-write"),
    ALLOW_READ("allow-read"), ALLOW_WRITE("allow-write");

    private String code;
    private Operation operation;

    AccessMode(String code) {
        this.code = code;
        this.operation = Operation.fromString(code.substring(0, Math.max(code.indexOf("-"), code.length() - 1)));
    }

    static AccessMode fromCode(String code) {
        Optional<AccessMode> accessMode = Stream.of(AccessMode.values())
            .filter(accessModeItem -> accessModeItem.getCode().equalsIgnoreCase(code))
            .findFirst();
        return accessMode.isPresent() ? accessMode.get() : null;
    }

    public static AccessMode fromRestrictionAndOperation(Restriction restriction, Operation operation) {
        return fromCode(restriction.name() + "-" + operation.name());
    }

    public static AccessMode fromGranularPermissionAndHttpMethod(GranularPermission permission, String httpMethod) {
        String code = permission.toString() + "-" + Operation.fromHttpMethod(httpMethod).name();
        return fromCode(code);
    }

    public static AccessMode fromOperation(Operation operation) {
        return fromCode(operation.name().toLowerCase());
    }

    public String getCode() {
        return code;
    }

    public Operation getOperation() {
        return operation;
    }
}
