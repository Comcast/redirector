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

package com.comcast.xre.common.redirector.v2.utils;

public class Host {
    private String ipv4;
    private String ipv6;
    private String weight;

    public Host(String ipv4, String ipv6) {
        this(ipv4, ipv6, null);
    }

    public Host(String ipv4, String ipv6, String weight) {
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.weight = weight;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public String getWeight() {
        return weight;
    }
}
