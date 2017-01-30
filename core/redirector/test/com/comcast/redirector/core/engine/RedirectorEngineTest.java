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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.xre.common.redirector.DataParamName;
import com.comcast.redirector.ruleengine.model.ReturnStatementType;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.balancer.IBalancer;
import com.comcast.redirector.core.engine.rules.IFlavorRuleSet;
import com.comcast.redirector.core.engine.rules.IURLRuleSet;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.model.UrlParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class RedirectorEngineTest {
    private static final String APP_NAME = "anyAppName";
    private Server testServer;
    private Server testDistributionServer;
    private Server testDefaultServer;
    private Server testFallbackServer;

    private static final String RULE_PATH = "/PO/POC5/1.45-RULE";
    private static final String DISTRIBUTION_PATH = "1.39-DISTRIBUTION";
    private static final String DEFAULT_PATH = "/PO/POC6/1.39-DEFAULT";

    private Map<String, String> context = Collections.emptyMap();

    private IBalancer balancer;
    private IFlavorRuleSet flavorRuleSet;
    private IURLRuleSet urlRuleSet;
    private RedirectorEngine redirectorEngine;

    private InstanceInfo successInstance;
    private InstanceInfo defaultInstance;
    private InstanceInfo fallbackInstance;

    UrlParams urlParams;

    @Before
    public void setUp() throws Exception {
        testServer = new Server("test", "{protocol}://{host}:{port}/{urn}", "");
        testDistributionServer = new Server("test", "{protocol}://{host}:{port}/{urn}", "");
        testDefaultServer = new Server("test", "{protocol}://{host}:{port}/{urn}", "");
        testFallbackServer = new Server("test", "{protocol}://{host}:{port}/{urn}", "");

        balancer = mock(IBalancer.class);
        flavorRuleSet = mock(IFlavorRuleSet.class);
        urlRuleSet = mock(IURLRuleSet.class);

        redirectorEngine = new RedirectorEngine(APP_NAME, balancer, flavorRuleSet, urlRuleSet, null, RedirectorConstants.NO_MODEL_NODE_VERSION);

        successInstance = new InstanceInfo(testServer, RULE_PATH, "10.10.10.10", null, true);
        defaultInstance = new InstanceInfo(testDefaultServer, DEFAULT_PATH, "20.10.10.10", null, true);

        testServer.setPath(RULE_PATH);
        testDefaultServer.setPath(DEFAULT_PATH);
        testDistributionServer.setReturnStatementType(ReturnStatementType.DISTRIBUTION_RULE);
        testDistributionServer.setPath(DISTRIBUTION_PATH);

        urlParams = new UrlParams("xre", "shell", 10004, 4);

        when(balancer.getServiceInstance(eq(testServer), eq(ServerLookupMode.DEFAULT))).thenReturn(successInstance);
        when(balancer.getServiceInstance(eq(testDefaultServer), eq(ServerLookupMode.DEFAULT))).thenReturn(defaultInstance);
        when(balancer.getServiceInstance(eq(testFallbackServer), eq(ServerLookupMode.NON_FILTERED))).thenReturn(fallbackInstance);

        when(flavorRuleSet.isAvailable()).thenReturn(true);
        when(urlRuleSet.isAvailable()).thenReturn(true);
        when(urlRuleSet.getUrlParams(anyMap())).thenReturn(urlParams);

        when(flavorRuleSet.getDefault()).thenReturn(testDefaultServer);
    }

    @Test
    public void testRedirect() {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testServer);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(RULE_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetDefaultHost() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDefaultServer);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(DEFAULT_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetDefaultOnFallbackToDistribution() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDistributionServer);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(DEFAULT_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetWhitelistedRuleNotInWhitelist() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testServer);
        testServer.setNonWhitelistOnly(true);
        when(balancer.getServiceInstance(eq(testServer), eq(ServerLookupMode.DEFAULT_NON_WHITELISTED))).thenReturn(defaultInstance);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(DEFAULT_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetWhitelistedRuleInWhitelist() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testServer);
        testServer.setNonWhitelistOnly(true);
        when(balancer.getServiceInstance(eq(testServer), eq(ServerLookupMode.DEFAULT_NON_WHITELISTED))).thenReturn(successInstance);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(RULE_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetNonWhitelistedRuleAvailableHosts() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testServer);
        testServer.setReturnStatementType(ReturnStatementType.PATH_RULE);
        testServer.setNonWhitelistOnly(false);
        when(balancer.getServiceInstance(eq(testServer), eq(ServerLookupMode.NON_FILTERED))).thenReturn(successInstance);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(RULE_PATH, instanceInfo.getStack());
    }

    @Test
    public void testGetNonWhitelistedRuleNoHosts() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testServer);
        testServer.setReturnStatementType(ReturnStatementType.PATH_RULE);
        testServer.setNonWhitelistOnly(false);
        when(balancer.getServiceInstance(eq(testServer), eq(ServerLookupMode.DEFAULT))).thenReturn(null);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);
        Assert.assertEquals(DEFAULT_PATH, instanceInfo.getStack());
    }

    @Test
    public void testAdvancedServer() throws Exception {
        String url = "xre://test:8080";
        when(flavorRuleSet.getResult(anyMap())).thenReturn(new Server("name", url, ""));
        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertTrue(instanceInfo.getIsAdvancedRule());
        Assert.assertEquals(url + "?ruleName=name", instanceInfo.getUrl());
    }

    @Test
    public void testAppendQuery() throws Exception {
        Map<String, String> context = new HashMap<>();
        context.put(DataParamName.connectUrl.name(), "xre://source:1000/app?a=b&c=d");

        String url = "xre://test:8080";
        when(flavorRuleSet.getResult(anyMap())).thenReturn(new Server("name", url, ""));
        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertEquals(url + "?a=b&c=d&ruleName=name", instanceInfo.getUrl());
    }

    @Test
    public void testAppendQueryToUrlWithQuery() throws Exception {
        Map<String, String> context = new HashMap<>();
        context.put(DataParamName.connectUrl.name(), "xre://source:1000/app?a=b&c=d");

        String url = "xre://test:8080?test=true";
        when(flavorRuleSet.getResult(anyMap())).thenReturn(new Server("name", url, ""));
        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertEquals(url + "&a=b&c=d&ruleName=name", instanceInfo.getUrl());
    }

    @Test
    public void testNullIPv6() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDefaultServer);
        when(urlRuleSet.getUrlParams(anyMap())).thenReturn(new UrlParams("xre", "shell", 10004, 6));
        when(balancer.getServiceInstance(eq(testDefaultServer), eq(ServerLookupMode.DEFAULT))).thenReturn(defaultInstance);

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertNull(instanceInfo);
    }

    @Test
    public void testNullIPv4() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDefaultServer);
        when(balancer.getServiceInstance(eq(testDefaultServer), eq(ServerLookupMode.DEFAULT))).thenReturn(
                new InstanceInfo(testDefaultServer, DEFAULT_PATH, null, "ipv6address", true));

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertNull(instanceInfo);
    }

    @Test
    public void testNotEmptyIPv6() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDefaultServer);
        when(urlRuleSet.getUrlParams(anyMap())).thenReturn(new UrlParams("xre", "shell", 10004, 6));
        when(balancer.getServiceInstance(eq(testDefaultServer), eq(ServerLookupMode.DEFAULT)))
                .thenReturn(new InstanceInfo(testDefaultServer, DEFAULT_PATH, "20.10.10.10", "ipv6address", true));

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertEquals("xre://ipv6address:10004/shell?ruleName=" + testDefaultServer.getName(), instanceInfo.getUrl());
    }

    @Test
    public void testNotEmptyIPv6AndOnlyIPAddress() throws Exception {
        when(flavorRuleSet.getResult(anyMap())).thenReturn(testDefaultServer);
        when(urlRuleSet.getUrlParams(anyMap())).thenReturn(new UrlParams("xre", "shell", 10004, 6));
        when(balancer.getServiceInstance(eq(testDefaultServer), eq(ServerLookupMode.DEFAULT)))
                .thenReturn(new InstanceInfo(testDefaultServer, DEFAULT_PATH, "20.10.10.10", "2001:0DB8:0000:0003:0000:01FF:0000:002E", true));

        InstanceInfo instanceInfo = redirectorEngine.redirect(context);

        Assert.assertEquals("xre://[2001:0DB8:0000:0003:0000:01FF:0000:002E]:10004/shell?ruleName=" + testDefaultServer.getName(), instanceInfo.getUrl());
    }



}
