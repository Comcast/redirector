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

package com.comcast.redirector.api.model.testsuite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSuiteResponseTest {
    private static final String PROTOCOL = "PROTOCOL";
    private static final String PORT = "PORT";
    private static final String IP_VERSION = "IP_VERSION";
    private static final String URN = "URN";
    private static final String XRE_STACK = "XRE_STACK";
    private static final String FLAVOR = "FLAVOR";
    private static final String RULE = "RULE";
    private static final String RESPONSE_TYPE = "RESPONSE_TYPE";

    private static final String DIFFERENT_VALUE = "DIFFERENT_VALUE";

    private TestSuiteResponse other;
    private TestSuiteResponse testee;

    @Before
    public void setUp() throws Exception {
        other = new TestSuiteResponse();
        other.setProtocol(PROTOCOL);
        other.setPort(PORT);
        other.setIpVersion(IP_VERSION);
        other.setUrn(URN);
        other.setXreStack(XRE_STACK);
        other.setFlavor(FLAVOR);
        other.setRule(RULE);
        other.setResponseType(RESPONSE_TYPE);

        testee = new TestSuiteResponse();
    }

    @Test
    public void testMatchBySingleParameter_Protocol() throws Exception {
        testee.setProtocol(PROTOCOL);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_Protocol() throws Exception {
        testee.setProtocol(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_Port() throws Exception {
        testee.setPort(PORT);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_Port() throws Exception {
        testee.setPort(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_IpVersion() throws Exception {
        testee.setIpVersion(IP_VERSION);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_IpVersion() throws Exception {
        testee.setIpVersion(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_Urn() throws Exception {
        testee.setUrn(URN);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_Urn() throws Exception {
        testee.setUrn(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_XreStack() throws Exception {
        testee.setXreStack(XRE_STACK);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_XreStack() throws Exception {
        testee.setXreStack(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_Flavor() throws Exception {
        testee.setFlavor(FLAVOR);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_Flavor() throws Exception {
        testee.setFlavor(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_Rule() throws Exception {
        testee.setRule(RULE);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_Rule() throws Exception {
        testee.setRule(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchBySingleParameter_ResponseType() throws Exception {
        testee.setResponseType(RESPONSE_TYPE);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBySingleParameter_ResponseType() throws Exception {
        testee.setResponseType(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testNotMatchBecauseOneParameterHasDifferentValue() throws Exception {
        testee.setProtocol(PROTOCOL);
        testee.setResponseType(DIFFERENT_VALUE);

        Assert.assertFalse(testee.matches(other));
    }

    @Test
    public void testMatchByTwoParameters() throws Exception {
        testee.setProtocol(PROTOCOL);
        testee.setResponseType(RESPONSE_TYPE);

        Assert.assertTrue(testee.matches(other));
    }

    @Test
    public void testNotMatchBecauseEmpty() throws Exception {
        Assert.assertFalse(testee.matches(other));
    }
}
