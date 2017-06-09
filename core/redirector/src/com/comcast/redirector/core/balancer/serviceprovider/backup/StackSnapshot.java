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

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class StackSnapshot {
    private String path;
    private List<Host> hosts;

    public StackSnapshot() {
    }

    public StackSnapshot(String path, List<Host> hosts) {
        this.path = path;
        this.hosts = hosts;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackSnapshot that = (StackSnapshot) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(hosts, that.hosts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, hosts);
    }

    public static class Host {
        private String ipv4;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String ipv6;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String weight;

        public Host() {
        }

        public Host(String ipv4, String ipv6) {
            this(ipv4, ipv6, null);
        }

        public Host(String ipv4, String ipv6, String weight) {
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
            this.weight = weight;
        }

        public Host(String host) {
            fromString(host);
        }

        public String getIpv4() {
            return ipv4;
        }

        public void setIpv4(String ipv4) {
            this.ipv4 = ipv4;
        }

        public String getIpv6() {
            return ipv6;
        }

        public void setIpv6(String ipv6) {
            if ("null".equals(ipv6)) { // this is a temporary check added for backward compatibility with legacy incorrect backups
                this.ipv6 = null;
            } else {
                this.ipv6 = ipv6;
            }
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public void fromString(String host) {
            if (StringUtils.isBlank(host)) {
                throw new IllegalArgumentException("host should not be blank");
            }

            String[] ips = StringUtils.split(host, ",");
            if (ips.length > 1) {
                ipv6 = ips[1];
            }
            ipv4 = StringUtils.substringBefore(ips[0], ":");
        }

        @Override
        public String toString() {
            return ipv4 + "," + ipv6;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Host host = (Host) o;
            return Objects.equals(ipv4, host.ipv4) &&
                    Objects.equals(ipv6, host.ipv6);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ipv4, ipv6);
        }
    }
}
