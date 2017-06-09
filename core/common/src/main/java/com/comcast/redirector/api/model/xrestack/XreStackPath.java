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

package com.comcast.redirector.api.model.xrestack; // TODO: remove xre from package name

import java.util.Objects;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

/**
 * Class represents XRE stack path /dataCenter/availabilityZone/flavor/serviceName and encapsulates
 * basic operations like parsing full path into components or building full path from components or building
 * some specific path like /dataCenter/availabilityZone or /dataCenter/availabilityZone/flavor
 */
// TODO: remove XRE from class name
public class XreStackPath {
    private static final String ELEMENTS_DESCRIPTION = "/<dataCenter>/<availabilityZone>/<flavor>/<serviceName>";

    private final String path;
    private final String dataCenter;
    private final String availabilityZone;
    private final String flavor;
    private final String serviceName;

    public XreStackPath(String path) {
        this.path = path;

        String[] args = path.substring(DELIMETER.length()).split(DELIMETER);
        if (args.length != 4) {
            throw new IllegalArgumentException(ELEMENTS_DESCRIPTION + " expected, but passed: " + path);
        }
        this.dataCenter = args[0];
        this.availabilityZone = args[1];
        this.flavor = args[2];
        this.serviceName = args[3];
    }

    public XreStackPath(String stackAndFlavorPath, String serviceName) {
        this(stackAndFlavorPath + DELIMETER + serviceName);
    }

    public XreStackPath(String dataCenter, String availabilityZone, String flavor, String serviceName) {
        this.dataCenter = dataCenter;
        this.availabilityZone = availabilityZone;
        this.flavor = flavor;
        this.serviceName = serviceName;
        this.path = DELIMETER + dataCenter + DELIMETER + availabilityZone + DELIMETER + flavor + DELIMETER + serviceName;
    }

    public String getPath() {
        return path;
    }

    public String getDataCenterPath() {
        return DELIMETER + dataCenter;
    }

    public String getStackOnlyPath() {
        return getDataCenterPath() + DELIMETER + availabilityZone;
    }

    public String getStackAndFlavorPath() {
        return getStackOnlyPath() + DELIMETER + flavor;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XreStackPath that = (XreStackPath) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(dataCenter, that.dataCenter) &&
                Objects.equals(availabilityZone, that.availabilityZone) &&
                Objects.equals(flavor, that.flavor) &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, dataCenter, availabilityZone, flavor, serviceName);
    }

    @Override
    public String toString() {
        return path;
    }
}
