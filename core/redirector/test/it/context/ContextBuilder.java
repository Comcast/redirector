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

package it.context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ContextBuilder implements ContextBuilderMain {
    private String appName;
    private boolean dynamic = false;
    private TestFlavorRule flavorRule;
    private TestUrlRule urlRule;
    private TestDistribution distribution;
    private TestDefaultServer defaultServer;
    private TestUrlParams defaultUrlParams;
    private TestWhitelist whitelist;
    private List<Host> hosts = new ArrayList<>();
    private List<EmptyStack> emptyStacks = new ArrayList<>();
    private List<StackWithData> stacksWithData = new ArrayList<>();
    private List<RedirectorInstance> redirectorInstances = new ArrayList<>();
    private TestApplications applications;
    private List<TestNamespacedList> namespacedLists = new ArrayList<>();
    private int version;

    public ContextBuilder forApp(String appName) {
        this.appName = appName;
        return this;
    }

    public ContextBuilderMain dynamic() {
        dynamic = true;
        return this;
    }

    public ContextBuilderMain dynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    private ContextBuilder flavorRule(TestFlavorRule flavorRule) {
        this.flavorRule = flavorRule;
        return this;
    }

    private ContextBuilder urlRule(TestUrlRule urlRule) {
        this.urlRule = urlRule;
        return this;
    }

    private ContextBuilder distribution(TestDistribution distribution) {
        this.distribution = distribution;
        return this;
    }

    private ContextBuilder defaultServer(TestDefaultServer defaultServer) {
        this.defaultServer = defaultServer;
        return this;
    }

    private ContextBuilder defaultUrlParams(TestUrlParams urlParams) {
        this.defaultUrlParams = urlParams;
        return this;
    }

    private ContextBuilder hosts(List<Host> hosts) {
        this.hosts = hosts;
        return this;
    }

    private ContextBuilder emptyStacks(List<EmptyStack> emptyStacks) {
        this.emptyStacks = emptyStacks;
        return this;
    }

    private ContextBuilder stacksWithData(List<StackWithData> stacksWithData) {
        this.stacksWithData = stacksWithData;
        return this;
    }

    private ContextBuilder redirectorInstances(List<RedirectorInstance> redirectorInstances) {
        this.redirectorInstances = redirectorInstances;
        return this;
    }

    @Override
    public Supplier<ContextBuilderMain> builderMainSupplier() {
        throw new UnsupportedOperationException();
    }

    public ContextFlavorRuleBuilder withFlavorRule() {
        return new ContextFlavorRuleBuilder()  {
            private TestFlavorRule rule = new TestFlavorRule();

            @Override
            public ContextFlavorRuleBuilder id(String id) {
                rule.id = id;
                return this;
            }

            @Override
            public ContextFlavorRuleBuilder operation(Operations expression) {
                rule.expression = expression;
                return this;
            }

            @Override
            public ContextFlavorRuleBuilder flavor(String flavor) {
                rule.flavor = flavor;
                return this;
            }

            @Override
            public ContextFlavorRuleBuilder left(String left) {
                rule.left = left;
                return this;
            }

            @Override
            public ContextFlavorRuleBuilder right(String right) {
                rule.right = right;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildFlavorRule;
            }

            private ContextBuilder buildFlavorRule() {
                return ContextBuilder.this.flavorRule(rule);
            }
        };
    }

    public ContextURLRuleBuilder withUrlRule() {
        return new ContextURLRuleBuilder() {
            private TestUrlParams urlParams = new TestUrlParams();
            private TestUrlRule urlRule = new TestUrlRule();
            private Operations expression;

            @Override
            public ContextURLRuleBuilder protocol(String protocol) {
                urlParams.protocol = protocol;
                return this;
            }

            @Override
            public ContextURLRuleBuilder port(String port) {
                urlParams.port = port;
                return this;
            }

            @Override
            public ContextURLRuleBuilder ipv(String ipv) {
                urlParams.ipv = ipv;
                return this;
            }

            @Override
            public ContextURLRuleBuilder urn(String urn) {
                urlParams.urn = urn;
                return this;
            }

            @Override
            public ContextURLRuleBuilder id(String id) {
                urlRule.id = id;
                return this;
            }

            @Override
            public ContextURLRuleBuilder operation(Operations expression) {
                this.expression = expression;
                return this;
            }

            @Override
            public ContextURLRuleBuilder left(String left) {
                urlRule.left = left;
                return this;
            }

            @Override
            public ContextURLRuleBuilder right(String right) {
                urlRule.right = right;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildUrlRule;
            }

            private ContextBuilder buildUrlRule() {
                urlRule.urlParams = urlParams;
                return ContextBuilder.this.urlRule(urlRule);
            }
        };
    }

    public DistributionBuilder withDistribution() {
        return new DistributionBuilder() {
            private List<DistributionRule> rules = new ArrayList<>();
            private DistributionRule distributionRule;

            @Override
            public DistributionBuilder percent(String percent) {
                if (distributionRule == null) {
                    distributionRule = new DistributionRule();
                }
                distributionRule.percent = percent;
                return this;
            }

            @Override
            public DistributionBuilder flavor(String flavor) {
                if (distributionRule == null) {
                    distributionRule = new DistributionRule();
                }
                distributionRule.flavor = flavor;
                return this;
            }

            @Override
            public DistributionBuilder and() {
                if (distributionRule != null)
                    rules.add(distributionRule);
                distributionRule = null;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildDistribution;
            }

            private ContextBuilder buildDistribution() {
                if (distributionRule != null) {
                    rules.add(distributionRule);
                }
                return ContextBuilder.this.distribution(new TestDistribution(rules));
            }
        };
    }

    public DefaultServerBuilder withDefaultServer() {
        return new DefaultServerBuilder() {
            private TestDefaultServer defaultServer = new TestDefaultServer();

            @Override
            public DefaultServerBuilder flavor(String flavor) {
                defaultServer.flavor = flavor;
                return this;
            }

            @Override
            public DefaultServerBuilder advancedUrl(String advancedUrl) {
                defaultServer.advancedUrl = advancedUrl;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildDefaultServer;
            }

            private ContextBuilder buildDefaultServer() {
                return ContextBuilder.this.defaultServer(defaultServer);
            }
        };
    }

    public DefaultUrlParamsBuilder withDefaultUrlParams() {
        return new DefaultUrlParamsBuilder() {
            private TestUrlParams urlParams = new TestUrlParams();

            @Override
            public DefaultUrlParamsBuilder protocol(String protocol) {
                urlParams.protocol = protocol;
                return this;
            }

            @Override
            public DefaultUrlParamsBuilder port(String port) {
                urlParams.port = port;
                return this;
            }

            @Override
            public DefaultUrlParamsBuilder ipv(String ipv) {
                urlParams.ipv = ipv;
                return this;
            }

            @Override
            public DefaultUrlParamsBuilder urn(String urn) {
                urlParams.urn = urn;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildDefaultUrlParams;
            }

            private ContextBuilder buildDefaultUrlParams() {
                return ContextBuilder.this.defaultUrlParams(urlParams);
            }
        };
    }

    public ContextBuilderMain withWhitelist(String... stacks) {
        whitelist = new TestWhitelist(stacks);
        return this;
    }

    public ContextBuilderMain withApplications(String... apps) {
        applications = new TestApplications(apps);
        return this;
    }

    @Override
    public ContextBuilderMain withNamespacedList(String name, String... values) {
        namespacedLists.add(new TestNamespacedList(name, values));
        return this;
    }

    public HostsBuilder withHosts() {
        return new HostsBuilder() {
            Host currentHost;

            @Override
            public HostsBuilder stack(String stack) {
                initHost();
                currentHost.stack = stack;
                String[] dcZone = stack.substring(1).split("/");
                currentHost.dc = dcZone[0];
                currentHost.zone = dcZone[1];
                return this;
            }

            @Override
            public HostsBuilder flavor(String flavor) {
                initHost();
                currentHost.flavor = flavor;
                return this;
            }

            @Override
            public HostsBuilder app(String app) {
                initHost();
                currentHost.app = app;
                return this;
            }

            @Override
            public HostsBuilder currentApp() {
                initHost();
                currentHost.app = ContextBuilder.this.appName;
                return this;
            }

            @Override
            public HostsBuilder ipv4(String ipv4) {
                initHost();
                currentHost.ipv4 = ipv4;
                return this;
            }

            @Override
            public HostsBuilder ipv6(String ipv6) {
                initHost();
                currentHost.ipv6 = ipv6;
                return this;
            }

            private void initHost() {
                if (currentHost == null || currentHost.isFull()) {
                    currentHost = new Host();
                    hosts.add(currentHost);
                }
            }

            @Override
            public HostsBuilder flavorRuleFlavor() {
                return flavor(ContextBuilder.this.flavorRule.flavor);
            }

            @Override
            public HostsBuilder defaultFlavor() {
                return flavor(ContextBuilder.this.defaultServer.flavor);
            }

            @Override
            public HostsBuilder distributionFlavor(String distributionPercent) {
                return flavor(ContextBuilder.this.distribution.getFlavor(distributionPercent));
            }

            @Override
            public HostsBuilder distributionFlavor() {
                return flavor(ContextBuilder.this.distribution.getFirstRuleFlavor());
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildHosts;
            }

            private ContextBuilder buildHosts() {
                return ContextBuilder.this.hosts(hosts);
            }
        };
    }

    @Override
    public EmptyStacksBuilder withEmptyStacks() {
        return new EmptyStacksBuilder() {
            private EmptyStack currentEmptyStack;

            @Override
            public EmptyStacksBuilder stack(String stack) {
                initStack();
                currentEmptyStack.stack = stack;
                return this;
            }

            @Override
            public EmptyStacksBuilder flavor(String flavor) {
                initStack();
                currentEmptyStack.flavor = flavor;
                return this;
            }

            @Override
            public EmptyStacksBuilder app(String app) {
                initStack();
                currentEmptyStack.app = app;
                return this;
            }

            @Override
            public EmptyStacksBuilder currentApp() {
                initStack();
                currentEmptyStack.app = ContextBuilder.this.appName;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildEmptyStacks;
            }

            private void initStack() {
                if (currentEmptyStack == null || currentEmptyStack.isFull()) {
                    currentEmptyStack = new EmptyStack();
                    emptyStacks.add(currentEmptyStack);
                }
            }

            private ContextBuilder buildEmptyStacks() {
                return ContextBuilder.this.emptyStacks(emptyStacks);
            }
        };
    }

    @Override
    public StacksWithDataBuilder withStacksWithData() {
        return new StacksWithDataBuilder() {
            private StackWithData currentStackWithData;

            @Override
            public StacksWithDataBuilder stack(String stack) {
                initStack();
                currentStackWithData.stack = stack;
                return this;
            }

            @Override
            public StacksWithDataBuilder flavor(String flavor) {
                initStack();
                currentStackWithData.flavor = flavor;
                return this;
            }

            @Override
            public StacksWithDataBuilder app(String app) {
                initStack();
                currentStackWithData.app = app;
                return this;
            }

            @Override
            public StacksWithDataBuilder currentApp() {
                initStack();
                currentStackWithData.app = ContextBuilder.this.appName;
                return this;
            }

            @Override
            public StacksWithDataBuilder data(String data) {
                initStack();
                currentStackWithData.data = data;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildEmptyStacks;
            }

            private void initStack() {
                if (currentStackWithData == null || currentStackWithData.isFull()) {
                    currentStackWithData = new StackWithData();
                    stacksWithData.add(currentStackWithData);
                }
            }

            private ContextBuilder buildEmptyStacks() {
                return ContextBuilder.this.stacksWithData(stacksWithData);
            }
        };
    }

    @Override
    public RedirectorInstancesBuilder withRedirectorInstances() {
        return new RedirectorInstancesBuilder() {
            private RedirectorInstance currentRedirectorInstance;

            @Override
            public RedirectorInstancesBuilder instance(String instance) {
                initRedirectorInstance();
                currentRedirectorInstance.instance = instance;
                return this;
            }

            @Override
            public RedirectorInstancesBuilder data(String data) {
                initRedirectorInstance();
                currentRedirectorInstance.nodeData = data;
                return this;
            }

            @Override
            public RedirectorInstancesBuilder app(String app) {
                initRedirectorInstance();
                currentRedirectorInstance.app = app;
                return this;
            }

            @Override
            public RedirectorInstancesBuilder currentApp() {
                initRedirectorInstance();
                currentRedirectorInstance.app = ContextBuilder.this.appName;
                return this;
            }

            @Override
            public Supplier<ContextBuilderMain> builderMainSupplier() {
                return this::buildRedirectorInstances;
            }

            private void initRedirectorInstance() {
                if (currentRedirectorInstance == null || currentRedirectorInstance.isFull()) {
                    currentRedirectorInstance = new RedirectorInstance();
                    redirectorInstances.add(currentRedirectorInstance);
                }
            }

            private ContextBuilder buildRedirectorInstances() {
                return ContextBuilder.this.redirectorInstances(redirectorInstances);
            }
        };
    }

    @Override
    public ContextBuilderMain withVersion(int version) {
        this.version = version;
        return this;
    }

    public TestContext build() {
        TestContext context = new TestContext();
        context.setAppName(appName);
        context.setDynamic(dynamic);
        context.setDefaultServer(defaultServer);
        context.setDistribution(distribution);
        context.setNoHostFlavor("no-hosts");
        if (flavorRule != null) {
            context.setFlavorRule(flavorRule);
        }

        if (namespacedLists != null) {
            context.setNamespacedLists(namespacedLists);
        }

        if (urlRule != null) {
            context.setUrlRule(urlRule);
        }
        context.setDefaultUrlParams(defaultUrlParams);
        context.setWhitelist(whitelist);
        context.setApplications(applications);
        if (hosts != null) {
            hosts.stream()
                .map(host -> new TestContext.Host(host.dc, host.zone, host.flavor, host.app, host.ipv4, host.ipv6))
                .forEach(context::addHost);
        }
        if (emptyStacks != null) {
            context.setEmptyStacks(emptyStacks);
        }
        if (stacksWithData != null) {
            context.setStackWithData(stacksWithData);
        }
        if (redirectorInstances != null) {
            context.setRedirectorInstances(redirectorInstances);
        }
        context.setVersion(version);

        return context;
    }
}
