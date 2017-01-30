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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

public class Permission {
    public static final String PERMIT_ALL = "permitAll";
    private static final String REST_ONLY = "RESTOnly-";

    private static EnumSet<AccessMode> granularAccess = EnumSet.of(
        AccessMode.ALLOW_READ, AccessMode.ALLOW_WRITE, AccessMode.DENY_READ, AccessMode.DENY_WRITE);

    private String rawValue;
    private AccessMode accessMode;
    private String entity;
    private String endpoint;

    public Permission(String rawValue) {
        this.rawValue = rawValue.replaceAll("(?i)" + REST_ONLY, "");
        this.accessMode = getAccessModeFromRawValue();
        this.entity = getEntityFromRawValue();
        this.endpoint = isAccessApp() || isPermitAll() || !isValid() ? null : getEndpointFromRawValue();
    }

    public boolean isGranular() {
        return granularAccess.contains(accessMode);
    }

    public boolean isValid() {
        return accessMode != null || isPermitAll();
    }

    public boolean isRegular() {
        return !isGranular() && !isAccessApp();
    }

    public boolean isAccessApp() {
        return accessMode == AccessMode.ACCESS_APP;
    }

    public boolean isPermitAll() {
        return PERMIT_ALL.toLowerCase().equals(rawValue.toLowerCase());
    }

    public String getApplicationName() {
        return isAccessApp() ? rawValue.replaceFirst(AccessMode.ACCESS_APP.getCode() + "-", "") : "";
    }

    public String getEndpoint() {
        return endpoint;
    }

    private String getEndpointFromRawValue() {
        String endpointSubstr = rawValue.replace(accessMode.getCode() + "-", "");
        if (endpointSubstr.contains("-")) {
            return endpointSubstr.substring(0, endpointSubstr.lastIndexOf("-"));
        } else {
            return endpointSubstr;
        }
    }

    public String getEntity() {
        return entity;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    private String getEntityFromRawValue() {
        return rawValue.substring(rawValue.lastIndexOf("-") + 1, rawValue.length());
    }

    private AccessMode getAccessModeFromRawValue() {
        String code;

        boolean accessAppPermission = rawValue.startsWith(AccessMode.ACCESS_APP.getCode());
        boolean granularPermission = rawValue.contains(Restriction.DENY.name().toLowerCase())
            || rawValue.contains(Restriction.ALLOW.name().toLowerCase());

        if (!granularPermission && !accessAppPermission) {
            code = (rawValue.contains("-")) ? rawValue.substring(0, rawValue.indexOf("-")) : rawValue;
        } else {
            String[] parts = rawValue.split("-");
            code = parts[0] + "-" + parts[1];
        }

        return AccessMode.fromCode(code);
    }

    String getValue() {
        return rawValue;
    }

    public static boolean hasPermitAll(Collection<Permission> permissions) {
        return permissions.stream().anyMatch(permission -> permission.isPermitAll());
    }

    public static boolean rawPermissionsContainPermitAll(Collection<String> rawPermissions) {
        return rawPermissions.stream().anyMatch(permission -> permission.toLowerCase().contains(PERMIT_ALL.toLowerCase()));
    }

    public static Permission of(Operation operation, String endpoint) {
        return new Permission(operation.name().toLowerCase() + "-" + endpoint.toLowerCase());
    }

    public static Permission accessAppPermission(String appName) {
        return new Permission(AccessMode.ACCESS_APP.getCode() + "-" + appName.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(rawValue.toLowerCase(), that.rawValue.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawValue);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Permission{");
        sb.append("rawValue='").append(rawValue).append('\'');
        sb.append(", accessMode=").append(accessMode);
        sb.append(", entity='").append(entity).append('\'');
        sb.append(", endpoint='").append(endpoint).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
