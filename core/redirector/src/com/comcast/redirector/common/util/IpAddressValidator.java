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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.common.util;


import com.google.common.net.InetAddresses;

public class IpAddressValidator {
    /**
     * Validates IP string, both IPV6 and IPV4
     * Also accounts for CIDR notation
     * Examples:
     * fe80::3ab1:dbff:fed1:c62d/64
     * 2001:db8::
     * 192.168.0.1
     * 2001:db8::/116
     * 192.168.0.0/32
     * ::/0
     *
     * @param address
     * @return
     */
    public static boolean isValidIpString (String address) {
        int slashIndex = address.lastIndexOf('/');
        String ipAddressPart = address;
        Integer subnetPart;
        if (slashIndex != -1) {
            ipAddressPart = address.substring(0, slashIndex);
            try {
                subnetPart = Integer.parseInt(address.substring(slashIndex + 1, address.length()));
            } catch (NumberFormatException nfe) {
                return false;
            }
            if (subnetPart < 0) {
                return false;
            }
            if (subnetPart > 128) {
                return false;
            }
            if (subnetPart > 32 && ipAddressPart.contains(".")) {
                return false;
            }
        }
        return InetAddresses.isInetAddress(ipAddressPart);
    }
}
