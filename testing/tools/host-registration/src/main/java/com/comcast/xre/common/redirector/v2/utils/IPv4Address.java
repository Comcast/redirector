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
 */
package com.comcast.xre.common.redirector.v2.utils;

public class IPv4Address {
    private static final int THREE_OCTETS = 24;
    private static final int TWO_OCTETS = 16;
    private static final int ONE_OCTET = 8;
    public static final int BYTE_MASK = 0xff;

    private final Long value;

    protected IPv4Address(Long value) {
        this.value = value;
    }

    public static IPv4Address parse(String ipv4Address) {

        String ipv4String = ipv4Address.trim();

        long value = 0;
        int octet = 0;
        for(int i = 0; i < ipv4String.length(); ++i) {
            char ch = ipv4String.charAt(i);
            if(Character.isDigit(ch)) {
                octet = octet * 10 + (ch - '0');
            } else if(ch == '.') {
                value = IPHelper.addOctet(value, octet);
                octet = 0;
            } else {
                throw new IllegalArgumentException();
            }
        }

        return new IPv4Address(IPHelper.addOctet(value, octet));
    }

    @Override
    public String toString() {
        int a = (int) (value >> THREE_OCTETS);
        int b = (int) (value >> TWO_OCTETS) & BYTE_MASK;
        int c = (int) (value >> ONE_OCTET) & BYTE_MASK;
        int d = (int) (value & BYTE_MASK);

        return a + "." + b + "." + c + "." + d;
    }

    public IPv4Address add(int octet) {
        return new IPv4Address(value + octet);
    }
}
