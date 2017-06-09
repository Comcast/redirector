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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.engine;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.core.balancer.serviceprovider.ServiceProviderManagerFactory;
import com.comcast.redirector.core.modelupdate.holder.NamespacedListsHolder;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import com.comcast.redirector.ruleengine.model.ReturnStatementType;
import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticEngineBuiltFromWSModelTest {
    private static final String NO_MATTER_STACK = "no-matter";
    private static final String PROTOCOL = "xre";
    private static final String PORT = "10001";
    private static final String URN = "shell";
    private static final String IPV4_ADDRESS = "IPV4";
    private static final String IPV6_ADDRESS = "::1";
    private static final String IP_PROTOCOL_VERSION = "4";
    public static final String APP_NAME = "test";

    private URLRules urlRules;
    private NamespacedListRepository stubNamespacedListsHolder;
    private RedirectorEngineFactory factory;
    private Serializer serializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());

    @Before
    public void setUp() throws Exception {
        urlRules = buildURLRules(buildDefaultStatement(PORT, PROTOCOL, URN, "4"));
        stubNamespacedListsHolder = new StubNamespacedListsHolder();
        Config config = new Config();

        ServiceProviderManagerFactory serviceProviderManagerFactory = new ServiceProviderManagerFactory();
        serviceProviderManagerFactory.setConfig(config);
        serviceProviderManagerFactory.setProviderStrategy(new RoundRobinStrategy<>());

        factory = new RedirectorEngineFactory(serviceProviderManagerFactory);

        ReflectionTestUtils.setField(factory, "config", config);
        ReflectionTestUtils.setField(factory, "isStaticDiscoveryNeededForApp", Predicate.isEqual(APP_NAME));
        ReflectionTestUtils.setField(factory, "serializer", serializer);
    }

    @Test
    public void redirectByFlavorRule() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.48-SNAPSHOT");
        String expectedStack = "/PO/POC6";
        String flavor = "1.48-SNAPSHOT";
        String ruleId = "equals";
        final String ipv4 = "10.10.10.10";
        final String parameter = "receiverType";
        final String matchedValue = "Native";

        SelectServer flavorRules = buildFlavorRules(
            buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, NO_MATTER_STACK)),
            buildExpression(ruleId,
                    buildServer(ruleId, RedirectorConstants.URL_TEMPLATE, flavor),
                    buildEqualsExpression(parameter, matchedValue)
            )
        );
        Set<StackData> stacks = new HashSet<>();
        Whitelisted whitelist = buildWhitelist("/PO/POC6");
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{ add(new HostIPs(ipv4, IPV6_ADDRESS)); }}));

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules,  whitelist, stubNamespacedListsHolder, stacks,  null /* sessionLogger */);

        InstanceInfo instanceInfo = testee.redirect(new HashMap<String, String>() {{ put(parameter, matchedValue); }});

        Assert.assertEquals(flavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(ruleId, instanceInfo.getServer().getName());
        Assert.assertEquals(buildUrl(ipv4) + "?ruleName=" + ruleId, instanceInfo.getUrl());
        Assert.assertEquals(ipv4, instanceInfo.getAddress());
        Assert.assertEquals(PORT, Integer.toString(instanceInfo.getPort()));
        Assert.assertEquals(PROTOCOL, instanceInfo.getProtocol());
        Assert.assertEquals(URN, instanceInfo.getUrn());
        Assert.assertEquals(IP_PROTOCOL_VERSION, Integer.toString(instanceInfo.getIpProtocolVersion()));
        Assert.assertEquals(ReturnStatementType.PATH_RULE, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectByFlavorRulesWithNamespacedLists() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.48-SNAPSHOT");
        String expectedStack = "/PO/POC6";
        String flavor = "1.48-SNAPSHOT";
        String namespacedListName = "NSList";
        String ruleId = "equals";
        final String ipv4 = "10.10.10.10";
        final String parameterMac = "mac";
        final String macIncludedInList = "mac1";
        ReflectionTestUtils.setField(factory, "serializer", serializer);
        NamespacedListRepository namespacedListsHolder = new StubNamespacedListsHolder(Stream.of(macIncludedInList, "mac2").collect(Collectors.toSet()));

        SelectServer flavorRules = buildFlavorRules(
                buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, NO_MATTER_STACK)),
                buildExpression(ruleId,
                        buildServer(ruleId, RedirectorConstants.URL_TEMPLATE, flavor),
                        buildContainsNSExpression(parameterMac, namespacedListName)
                )
        );
        Whitelisted whitelist = buildWhitelist("/PO/POC6", "/BR/BRC6");
        Set<StackData> stacks = new HashSet<>();
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{ add(new HostIPs(ipv4, IPV6_ADDRESS)); }}));

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules, whitelist, namespacedListsHolder, stacks,  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(new HashMap<String, String>() {{ put(parameterMac, macIncludedInList); }});

        Assert.assertEquals(flavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(ruleId, instanceInfo.getServer().getName());
        Assert.assertEquals(buildUrl(ipv4) + "?ruleName=" + ruleId, instanceInfo.getUrl());
        Assert.assertEquals(ipv4, instanceInfo.getAddress());
        Assert.assertEquals(ReturnStatementType.PATH_RULE, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectToDefaultHost() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.46");
        String expectedStack = "/PO/POC6";
        String defaultFlavor = "1.46";
        SelectServer flavorRules = buildFlavorRules(
                buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, defaultFlavor))
        );
        Whitelisted whitelist = buildWhitelist("/PO/POC6", "/BR/BRC6");
        Set<StackData> stacks = new HashSet<>();
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{
            add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS));
        }}));

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules,  whitelist, stubNamespacedListsHolder, stacks,  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(Collections.<String, String>emptyMap());

        Assert.assertEquals(defaultFlavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(RedirectorConstants.DEFAULT_SERVER_NAME, instanceInfo.getServer().getName());
        Assert.assertEquals(buildUrl(IPV4_ADDRESS), instanceInfo.getUrl());
        Assert.assertEquals(ReturnStatementType.DEFAULT_SERVER, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectToDefaultOnFallbackToDistribution() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.46");
        String nonWhitelistedStack = getFullStackPath("/PO/POC7/1.47");
        String expectedStack = "/PO/POC6";
        String defaultFlavor = "1.46";
        String distributionFlavor = "1.47";
        final String parameter = "mac";
        final String macThatFitsPercent = "B4:F2:E8:79:9F:76";

        SelectServer flavorRules = buildFlavorRules(
            buildDistribution(
                buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, defaultFlavor),
                buildDistributionRule(0, 99.99F, buildServer("Distribution 0", RedirectorConstants.URL_TEMPLATE, distributionFlavor))
            )
        );
        Whitelisted whitelist = buildWhitelist("/PO/POC6", "/BR/BRC6");
        Set<StackData> stacks = new HashSet<>();
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));
        stacks.add(new StackData(nonWhitelistedStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules, whitelist, stubNamespacedListsHolder, stacks,  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(new HashMap<String, String>() {{ put(parameter, macThatFitsPercent); }});

        Assert.assertEquals(defaultFlavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(RedirectorConstants.DEFAULT_SERVER_NAME, instanceInfo.getServer().getName());
        Assert.assertEquals(ReturnStatementType.DEFAULT_SERVER, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectToDefaultWhenNonWhitelistedOnlyRuleInWhitelist() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.46");
        String fullRuleStack = getFullStackPath("/PO/POC7/1.47-SNAPSHOT");
        String expectedStack = "/PO/POC6";
        String defaultFlavor = "1.46";
        String ruleFlavor = "1.47-SNAPSHOT";
        String ruleId = "rule";
        final String parameter = "receiverType";
        final String matchedValue = "Native";

        SelectServer flavorRules = buildFlavorRules(
            buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, defaultFlavor)),
            buildExpression(ruleId,
                buildServer(ruleId, RedirectorConstants.URL_TEMPLATE, ruleFlavor, true),
                buildEqualsExpression(parameter, matchedValue)
            )
        );
        Set<StackData> stacks = new HashSet<>();
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));
        stacks.add(new StackData(fullRuleStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));
        Whitelisted whitelist = buildWhitelist("/PO/POC6", "/PO/POC7");

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules, whitelist, stubNamespacedListsHolder, stacks,  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(new HashMap<String, String>() {{ put(parameter, matchedValue); }});

        Assert.assertEquals(defaultFlavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(RedirectorConstants.DEFAULT_SERVER_NAME, instanceInfo.getServer().getName());
        Assert.assertEquals(ReturnStatementType.DEFAULT_SERVER, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectToFlavorRuleWhenNonWhitelistedOnlyRuleNotInWhitelist() throws Exception {
        String fullStack = getFullStackPath("/PO/POC6/1.46");
        String fullRuleStack = getFullStackPath("/PO/POC7/1.47-SNAPSHOT");
        String expectedStack = "/PO/POC7";
        String ruleFlavor = "1.47-SNAPSHOT";
        String ruleId = "rule";
        final String parameter = "receiverType";
        final String matchedValue = "Native";

        SelectServer flavorRules = buildFlavorRules(
                buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, RedirectorConstants.URL_TEMPLATE, NO_MATTER_STACK)),
                buildExpression(ruleId,
                        buildServer(ruleId, RedirectorConstants.URL_TEMPLATE, ruleFlavor, true),
                        buildEqualsExpression(parameter, matchedValue)
                )
        );
        Set<StackData> stacks = new HashSet<>();
        stacks.add(new StackData(fullStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));
        stacks.add(new StackData(fullRuleStack, new ArrayList<HostIPs>() {{ add(new HostIPs(IPV4_ADDRESS, IPV6_ADDRESS)); }}));
        Whitelisted whitelist = buildWhitelist("/PO/POC6", "/BR/BRC7");

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules, whitelist, stubNamespacedListsHolder, stacks,  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(new HashMap<String, String>() {{ put(parameter, matchedValue); }});

        Assert.assertEquals(ruleFlavor, instanceInfo.getFlavor());
        Assert.assertEquals(expectedStack, instanceInfo.getStack());
        Assert.assertEquals(ruleId, instanceInfo.getServer().getName());
        Assert.assertEquals(ReturnStatementType.PATH_RULE, instanceInfo.getServer().getReturnStatementType());
    }

    @Test
    public void redirectToAdvancedServer() throws Exception {
        String absoluteUrl = "xre://absolute-url:1000/absolute";
        SelectServer flavorRules = buildFlavorRules(
            buildDistribution(buildServer(RedirectorConstants.DEFAULT_SERVER_NAME, absoluteUrl, null))
        );

        IRedirectorEngine testee = factory.newRedirectorEngine(APP_NAME, flavorRules, urlRules, null, stubNamespacedListsHolder, new HashSet<>(),  null /* sessionLogger */);
        InstanceInfo instanceInfo = testee.redirect(Collections.<String, String>emptyMap());

        Assert.assertEquals(absoluteUrl, instanceInfo.getUrl());
    }

    private String getFullStackPath(String pathWithoutApp) {
        return pathWithoutApp + "/" + APP_NAME;
    }

    private String buildUrl(String ipv4) {
        return PROTOCOL + "://" + ipv4 + ":" + PORT + "/" + URN;
    }

    private Default buildDefaultStatement(String port, String protocol, String urn, String ipProtocolVersion) {
        Default defaultStatement = new Default();
        UrlRule rule = new UrlRule();
        rule.setPort(port);
        rule.setProtocol(protocol);
        rule.setUrn(urn);
        rule.setIpProtocolVersion(ipProtocolVersion);
        defaultStatement.setUrlRule(rule);
        return defaultStatement;
    }

    private Whitelisted buildWhitelist(String... aStacks) {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Arrays.asList(aStacks));
        return whitelisted;
    }

    private URLRules buildURLRules(Default defaultStatement) {
        URLRules rules = new URLRules();
        rules.setDefaultStatement(defaultStatement);
        return rules;
    }

    private SelectServer buildFlavorRules(Distribution distribution, IfExpression... ifExpressions) {
        SelectServer selectServer = new SelectServer();
        selectServer.setDistribution(distribution);
        selectServer.setItems(Arrays.asList(ifExpressions));
        return selectServer;
    }

    private Rule buildDistributionRule(int id, float percent, Server server) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setPercent(percent);
        rule.setServer(server);
        return rule;
    }

    private IfExpression buildExpression(String id, Server server, Expressions... condition) {
        IfExpression expression = new IfExpression();
        expression.setId(id);
        expression.setItems(Arrays.asList(condition));
        expression.setReturn(server);
        return expression;
    }

    private Equals buildEqualsExpression(String left, String right) {
        Equals equals = new Equals();
        equals.setParam(left);
        equals.setValue(right);
        return equals;
    }

    private Contains buildContainsNSExpression(String left, String right) {
        Contains contains = new Contains();
        contains.setParam(left);
        contains.setNamespacedLists(Collections.singletonList(new Value(right)));
        return contains;
    }

    private Server buildServer(String id, String url, String path) {
        return buildServer(id, url, path, false);
    }

    private Server buildServer(String id, String url, String path, boolean isNonWhitelisted) {
        Server server = new Server();
        server.setName(id);
        server.setUrl(url);
        server.setPath(path);
        server.setIsNonWhitelisted(Boolean.toString(isNonWhitelisted));
        return server;
    }

    private Distribution buildDistribution(Server defaultServer, Rule... rules) {
        Distribution distribution = new Distribution();
        distribution.setRules(Arrays.asList(rules));
        distribution.setDefaultServer(defaultServer);
        return distribution;
    }

    private static class StubNamespacedListsHolder implements NamespacedListRepository {
        private Set<String> values;
        private NamespacedListsHolder.NamespacedListToIpAddressListConverter toIpAddressListConverter =
            new NamespacedListsHolder.NamespacedListToIpAddressListConverter(this::getNamespacedListValues);

        StubNamespacedListsHolder() {
            this(new HashSet<>());
        }

        private StubNamespacedListsHolder(Set<String> values) {
            this.values = values;
        }

        @Override
        public Set<String> getNamespacedListValues(String name) {
            return values;
        }

        @Override
        public Set<IpAddress> getIpAddressesFromNamespacedList(String name) {
            return toIpAddressListConverter.convert(name);
        }
    }
}
