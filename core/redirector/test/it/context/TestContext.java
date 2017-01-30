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

import com.comcast.redirector.api.model.distribution.Distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

/**
 * Setup of environment for Redirector integration test is pretty complex thing: rules, distributions, whitelists, servers
 * should be setup. Hosts should be registered. Since we support multiple applications all this set of objects should be setup for
 * multiple apps.
 *
 * In order to simplify setup of integration test, context is introduced. Set of data necessary for redirector to be run is handled
 * by instance of {@link TestContext} class
 */
public class TestContext {
    private String appName;
    private boolean dynamic;

    private String noHostFlavor;

    private TestDistribution distribution;
    private TestFlavorRule rule;
    private TestDefaultServer server;
    private TestUrlParams defaultUrlParams;
    private TestUrlRule urlRule;
    private TestWhitelist whitelist;
    private List<TestNamespacedList> namespacedLists;
    private TestApplications applications;

    private List<Host> hosts = new ArrayList<>();
    private List<EmptyStack> emptyStacks = new ArrayList<>();
    private List<StackWithData> stackWithData = new ArrayList<>();
    private List<RedirectorInstance> redirectorInstances = new ArrayList<>();
    private int version;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getNoHostFlavor() {
        return noHostFlavor;
    }

    public void setNoHostFlavor(String noHostFlavor) {
        this.noHostFlavor = noHostFlavor;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void addHost(Host host) {
        hosts.add(host);
    }

    public List<TestNamespacedList> getNamespacedLists() {
        return namespacedLists;
    }

    public void setNamespacedLists(List<TestNamespacedList> namespacedLists) {
        this.namespacedLists = namespacedLists;
    }


    public TestDistribution getDistribution() {
        return distribution;
    }

    public Distribution getDistributionWithDefaultServer() {
        Distribution distribution = getDistribution().value();
        distribution.setDefaultServer(getDefaultServer().value());

        return distribution;
    }

    public void setDistribution(TestDistribution distribution) {
        this.distribution = distribution;
    }

    public TestFlavorRule getFlavorRule() {
        return rule;
    }

    public void setFlavorRule(TestFlavorRule rule) {
        this.rule = rule;
    }

    public TestDefaultServer getDefaultServer() {
        return server;
    }

    public TestUrlParams getDefaultUrlParams() {
        return defaultUrlParams;
    }

    public void setDefaultUrlParams(TestUrlParams defaultUrlParams) {
        this.defaultUrlParams = defaultUrlParams;
    }

    public TestUrlRule getUrlRule() {
        return urlRule;
    }

    public void setUrlRule(TestUrlRule urlRule) {
        this.urlRule = urlRule;
    }

    public TestWhitelist getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(TestWhitelist whitelist) {
        this.whitelist = whitelist;
    }

    public void setDefaultServer(TestDefaultServer server) {
        this.server = server;
    }

    public TestApplications getApplications() {
        return applications;
    }

    public void setApplications(TestApplications applications) {
        this.applications = applications;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public List<EmptyStack> getEmptyStacks() {
        return emptyStacks;
    }

    public void setEmptyStacks(List<EmptyStack> emptyStacks) {
        this.emptyStacks = emptyStacks;
    }

    public List<StackWithData> getStackWithData() {
        return stackWithData;
    }

    public void setStackWithData(List<StackWithData> stackWithData) {
        this.stackWithData = stackWithData;
    }

    public List<RedirectorInstance> getRedirectorInstances() {
        return redirectorInstances;
    }

    public void setRedirectorInstances(List<RedirectorInstance> redirectorInstances) {
        this.redirectorInstances = redirectorInstances;
    }

    public static class Host {
        private String dataCenter;
        private String stack;
        private String flavor;
        private String appName;
        private String ipv4;
        private String ipv6;
        private String weight;

        public Host(String dataCenter, String stack, String flavor, String appName, String ipv4, String ipv6) {
            this.dataCenter = dataCenter;
            this.stack = stack;
            this.flavor = flavor;
            this.appName = appName;
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
        }

        public Host(String dataCenter, String stack, String flavor, String appName, String ipv4, String ipv6, String weight) {
            this(dataCenter, stack, flavor, appName, ipv4, ipv6);
            this.weight = weight;
        }

        public String getPath() {
            return DELIMETER + Stream.of(dataCenter, stack, flavor, appName).collect(Collectors.joining(DELIMETER));
        }

        public String getDataCenter() {
            return dataCenter;
        }

        public String getStack() {
            return stack;
        }

        public String getFlavor() {
            return flavor;
        }

        public String getAppName() {
            return appName;
        }

        public String getIpv4() {
            return ipv4;
        }

        public String getIpv6() {
            return ipv6;
        }

        public String getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Host{");
            sb.append("dataCenter='").append(dataCenter).append('\'');
            sb.append(", stack='").append(stack).append('\'');
            sb.append(", flavor='").append(flavor).append('\'');
            sb.append(", appName='").append(appName).append('\'');
            sb.append(", ipv4='").append(ipv4).append('\'');
            sb.append(", ipv6='").append(ipv6).append('\'');
            sb.append(", weight='").append(weight).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
