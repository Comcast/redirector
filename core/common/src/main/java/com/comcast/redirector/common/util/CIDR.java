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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.common.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CIDR {
    private static final int IPV4_BYTES = 4;
    private static final int IPV6_BYTES = 16;
    private static final String CIDR_RANGE_TOKEN = "/";

    private InetAddress inetAddress;
    private InetAddress startAddress;
    private InetAddress endAddress;
    private int prefixLength;

    public CIDR(String cidr) {
        if (isValidFormatCIDR(cidr)) {
            try {
                calculate(cidr);
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Unknown Host" + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("CIDR format isn't valid");
        }
    }

    private void calculate(String cidr) throws UnknownHostException {
        int index = cidr.indexOf(CIDR_RANGE_TOKEN);
        String addressPart = cidr.substring(0, index);
        String networkPart = cidr.substring(index + 1);

        inetAddress = InetAddress.getByName(addressPart);
        prefixLength = Integer.parseInt(networkPart);
        ByteBuffer maskBuffer;
        int targetSize;
        if (inetAddress.getAddress().length == IPV4_BYTES) {
            maskBuffer = ByteBuffer.allocate(IPV4_BYTES).putInt(-1);
            targetSize = IPV4_BYTES;
        } else {
            maskBuffer = ByteBuffer.allocate(IPV6_BYTES).putLong(-1L);
            targetSize = IPV6_BYTES;
        }

        BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

        ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
        BigInteger ipValue = new BigInteger(1, buffer.array());

        BigInteger startIp = ipValue.and(mask);
        BigInteger endIp = startIp.add(mask.not());

        byte[] startIpBytes = toBytes(startIp.toByteArray(), targetSize);
        byte[] endIpBytes = toBytes(endIp.toByteArray(), targetSize);

        startAddress = InetAddress.getByAddress(startIpBytes);
        endAddress = InetAddress.getByAddress(endIpBytes);
    }

    private byte[] toBytes(byte[] array, int targetSize) {
        int counter = 0;
        List<Byte> newBytes = new ArrayList<>();
        while (counter < targetSize && (array.length - 1 - counter >= 0)) {
            newBytes.add(0, array[array.length - 1 - counter]);
            counter++;
        }

        int size = newBytes.size();
        for (int i = 0; i < (targetSize - size); i++) {
            newBytes.add(0, (byte) 0);
        }

        byte[] ret = new byte[newBytes.size()];

        for (int i = 0; i < newBytes.size(); i++) {
            ret[i] = newBytes.get(i);
        }
        return ret;
    }

    public String getStartAddress() {
        return startAddress.getHostAddress();
    }

    public String getEndAddress() {
        return endAddress.getHostAddress();
    }

    public static boolean isValidFormatCIDR(String cidr) {
        return cidr.contains(CIDR_RANGE_TOKEN);
    }
}
