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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.model.xrestack;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement (name = "host")
public class HostIPs {
    private String ipV4Address;
    private String ipV6Address;
    private String weight;

    public HostIPs() {}

    public HostIPs(String ipV4Address, String ipV6Address) {
        this(ipV4Address, ipV6Address, null);
    }

    public HostIPs(String ipV4Address, String ipV6Address, String weight) {
        this.ipV4Address = ipV4Address;
        this.ipV6Address = ipV6Address;
        this.weight = weight;
    }

    public String getIpV4Address() {
        return ipV4Address;
    }

    public void setIpV4Address(String ipV4Address) {
        this.ipV4Address = ipV4Address;
    }

    public String getIpV6Address() {
        return ipV6Address;
    }

    public void setIpV6Address(String ipV6Address) {
        this.ipV6Address = ipV6Address;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostIPs hostIPs = (HostIPs) o;
        return Objects.equals(ipV4Address, hostIPs.ipV4Address) &&
                Objects.equals(ipV6Address, hostIPs.ipV6Address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipV4Address, ipV6Address);
    }
}
