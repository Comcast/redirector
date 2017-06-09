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

package com.comcast.redirector.api.model.xrestack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

public class StackData {
    private final XreStackPath stackPath;
    private Optional<List<HostIPs>> hosts;

    /**
     * @param path /<dataCenter>/<availabilityZone>/<flavor>/<serviceName>
     * @throws IllegalArgumentException if path doesn't match pattern above
     */
    public StackData(String path, List<HostIPs> hosts) {
        this.stackPath = new XreStackPath(path);
        this.hosts = Optional.ofNullable(hosts);
    }

    public StackData(String path) {
        this(path, null);
    }

    public StackData(String dataCenter, String availabilityZone, String flavor, String serviceName, List<HostIPs> hosts) {
        this(DELIMETER + dataCenter + DELIMETER + availabilityZone + DELIMETER + flavor + DELIMETER + serviceName, hosts);
    }

    public StackData(String dataCenter, String availabilityZone, String flavor, String serviceName) {
        this(dataCenter, availabilityZone, flavor, serviceName, null);
    }

    public String getDataCenter() {
        return stackPath.getDataCenter();
    }

    public String getAvailabilityZone() {
        return stackPath.getAvailabilityZone();
    }

    public String getFlavor() {
        return stackPath.getFlavor();
    }

    public String getServiceName() {
        return stackPath.getServiceName();
    }

    public String getPath() {
        return stackPath.getPath();
    }

    public String getStackOnlyPath() {
        return stackPath.getStackOnlyPath();
    }

    @Override
    public String toString() {
        return stackPath.getPath() + (hosts.map(hostIPs -> "(hosts: " + hostIPs.size() + ") ").orElse(""));
    }

    public Optional<List<HostIPs>> getHosts() {
        return hosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackData stackData = (StackData) o;
        return Objects.equals(stackPath, stackData.stackPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackPath);
    }
}
