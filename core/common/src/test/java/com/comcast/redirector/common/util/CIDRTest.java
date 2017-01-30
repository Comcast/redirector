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

import com.comcast.redirector.common.util.CIDR;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

public class CIDRTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getCIDRUtilsTestIfCIDRIsNotValidFormat() throws UnknownHostException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("CIDR format isn't valid");
        CIDR cidr = new CIDR("204.11.58.63");
    }

    @Test
    public void getCIDRUtilsTestIfCIDRIsIPV4() throws UnknownHostException {
        CIDR cidr = new CIDR("204.11.58.63/27");
        assertEquals("204.11.58.32", cidr.getStartAddress());
        assertEquals("204.11.58.63", cidr.getEndAddress());

        cidr = new CIDR("172.16.0.0/21");
        assertEquals("172.16.0.0", cidr.getStartAddress());
        assertEquals("172.16.7.255", cidr.getEndAddress());

        cidr = new CIDR("255.255.255.192/26");
        assertEquals("255.255.255.192", cidr.getStartAddress());
        assertEquals("255.255.255.255", cidr.getEndAddress());

    }

    @Test
    public void getCIDRUtilsTestIfCIDRIsIPV6() throws UnknownHostException {
        CIDR cidr = new CIDR("435:23f::45:23/101");
        assertEquals("435:23f:0:0:0:0:0:0", cidr.getStartAddress());
        assertEquals("435:23f:0:0:0:0:7ff:ffff", cidr.getEndAddress());

        cidr = new CIDR("2031:0:130F:0:0:9C0:876A:130D/64");
        assertEquals("2031:0:130f:0:0:0:0:0", cidr.getStartAddress());
        assertEquals("2031:0:130f:0:ffff:ffff:ffff:ffff", cidr.getEndAddress());
    }
}
