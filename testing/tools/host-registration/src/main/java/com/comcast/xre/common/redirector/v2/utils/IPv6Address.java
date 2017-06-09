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

public class IPv6Address {

    private long highBits;
    private long lowBits;

    public IPv6Address(long highBits, long lowBits) {
        this.highBits = highBits;
        this.lowBits = lowBits;
    }

    public static IPv6Address parse(String address) {
        String ipv6 = IPHelper.createIPv6FullNotation(address);

        return createIPv6(IPHelper.parseStringIntoLongArray(ipv6));
    }

    public IPv6Address add(int value) {
        final long newLowBits = lowBits + value;

        if(value >= 0) {
            if(IPHelper.compare(newLowBits, lowBits)) {
                return new IPv6Address(highBits + 1, newLowBits);
            } else {
                return new IPv6Address(highBits, newLowBits);
            }
        } else {
            if(IPHelper.compare(lowBits, newLowBits)) {
                return new IPv6Address(highBits - 1, newLowBits);
            } else {
                return new IPv6Address(highBits, newLowBits);
            }
        }
    }

    @Override
    public String toString() {

        final String[] strings = fromBits();
        final StringBuilder result = new StringBuilder();
        for(int i = 0; i < strings.length - 1; i++) {
            result.append(strings[i]).append(":");
        }

        result.append(strings[strings.length - 1]);

        return result.toString();
    }

    private static IPv6Address createIPv6(long[] bits) {

        long high = 0L;
        long low = 0L;

        for(int i = 0; i < bits.length; i++) {
            if(i >= 0 && i < 4) {
                high |= (bits[i] << ((bits.length - i - 1) * 16));
            } else {
                low |= (bits[i] << ((bits.length - i - 1) * 16));
            }
        }

        return new IPv6Address(high, low);
    }

    private String[] fromBits() {

        final String[] strings = new String[8];

        for(int i = 0; i < 8; i++) {
            if(i >= 0 && i < 4) {
                strings[i] = IPHelper.fromBitsToString(highBits, i);
            } else {
                strings[i] = IPHelper.fromBitsToString(lowBits, i);
            }
        }

        return strings;
    }
}
