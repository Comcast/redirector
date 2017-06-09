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

import org.apache.commons.lang3.StringUtils;

public class IPHelper {

    private final static int MAX_COLONS_IPv6 = 7;
    private final static String TWIN_COLONS = "::";
    private final static String COLON = ":";

    public static boolean compare(long a, long b) {

        if((a & (Long.MAX_VALUE + 1)) != 0) {
            return (b & (Long.MAX_VALUE + 1)) != 0 ? (a < b) : true;
        } else {
            return (b & (Long.MAX_VALUE + 1)) != 0 ? false : a < b;
        }
    }

    public static String createIPv6FullNotation(String string) {
        if(!string.contains(TWIN_COLONS)) {
            return string;
        } else if(string.equals(TWIN_COLONS)) {
            return fillZero(8);
        } else {
            int numberOfColons = colonCount(string);
            if(string.startsWith(TWIN_COLONS)) {
                return string.replace(TWIN_COLONS, fillZero((MAX_COLONS_IPv6 + 2) - numberOfColons));
            } else if(string.endsWith(TWIN_COLONS)) {
                return string.replace(TWIN_COLONS, COLON + fillZero((MAX_COLONS_IPv6 + 2) - numberOfColons));
            } else {
                return string.replace(TWIN_COLONS, COLON + fillZero((MAX_COLONS_IPv6 + 2 - 1) - numberOfColons));
            }
        }
    }

    private static String fillZero(int size) {
        return StringUtils.repeat("0:", size);
    }

    private static int colonCount(String string) {
        return StringUtils.countMatches(string, COLON);
    }

    public static long[] parseStringIntoLongArray(String string) {
        String strings[] = string.split(COLON);

        final long[] bits = new long[strings.length];

        for(int i = 0; i < strings.length; i++) {
            bits[i] = Long.parseLong(strings[i], 16);
        }
        return bits;
    }

    public static String fromBitsToString(long bits, int counter) {
        short bitNotation = (short) (((bits << counter * 16) >>> 16 * (8 - 1)) & 0xFFFF);
        return String.format("%04x", bitNotation);
    }

    public static long addOctet(long value, int octet) {
        return ((value) << 8) | octet;
    }

}
