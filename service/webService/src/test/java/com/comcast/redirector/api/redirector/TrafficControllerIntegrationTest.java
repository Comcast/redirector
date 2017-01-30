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

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.traffic.Traffic;
import com.comcast.redirector.api.model.traffic.TrafficStatsItem;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TrafficControllerIntegrationTest {

    private static final String PATH_DISTRIBUTION = "/DataCenter2/Region1";
    private static final String PATH_DEFAULT = "/DataCenter1/Region1";
    private static final String FLAVOR_1 = "zone1";
    private static final String DEFAULT_FLAVOR = "zone2";
    private static final String FLAVOR_PERCENT = "34.47";
    private static final Float UPDATED_PERCENT = 43.82f;
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void testGetCurrentTrafficJson() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new TrafficServiceSteps(serviceName)
                .withTotalNumberConnections(50)
                .withConnectionThreshold(20)
                .getCurrentTrafficJSONAndVerifyIsTrafficNotNull()
                .verifyCurrentDefaultServer()
                .verifyCurrentDistribution();
    }

    @Test
    public void testGetCurrentTrafficXML() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new TrafficServiceSteps(serviceName)
                .withTotalNumberConnections(50)
                .withConnectionThreshold(20)
                .getCurrentTrafficXMLAndVerifyIsTrafficNotNull()
                .verifyCurrentDefaultServer()
                .verifyCurrentDistribution();
    }

    @Test
    public void testGetNextTrafficJson() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new TrafficServiceSteps(serviceName)
                .withTotalNumberConnections(50)
                .withConnectionThreshold(20)
                .postAndApproveUpdatedDistribution()
                .getCurrentTrafficJSONAndVerifyIsTrafficNotNull()
                .verifyNextDefaultServer()
                .verifyNextDistribution();
    }

    private class TrafficServiceSteps {
        private String serviceName;
        private Traffic traffic;
        private int totalNumberConnections;
        private int connectionThreshold;

        TrafficServiceSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        TrafficServiceSteps withTotalNumberConnections(int totalNumberConnections) {
            this.totalNumberConnections = totalNumberConnections;
            return this;
        }

        TrafficServiceSteps withConnectionThreshold(int connectionThreshold) {
            this.connectionThreshold = connectionThreshold;
            return this;
        }

        TrafficServiceSteps getCurrentTrafficJSONAndVerifyIsTrafficNotNull() {
            traffic = apiFacade.getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThresholdReturnJSON(serviceName, totalNumberConnections, connectionThreshold);
            assertNotNull(traffic);
            return this;
        }

        TrafficServiceSteps postAndApproveUpdatedDistribution() {
            apiFacade.postDistributionForService(updateDistribution(), serviceName);
            apiFacade.approveDistributionForService(serviceName);
            return this;
        }

        TrafficServiceSteps getCurrentTrafficXMLAndVerifyIsTrafficNotNull() {
            traffic = apiFacade.getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThresholdReturnXML(serviceName, totalNumberConnections, connectionThreshold);
            assertNotNull(traffic);
            return this;
        }

        TrafficServiceSteps verifyCurrentDefaultServer() {
            TrafficStatsItem defaultServer = traffic.getDefaultServerTraffic();
            assertNotNull(defaultServer);
            assertEquals("Default server 65.53% of " + DEFAULT_FLAVOR, defaultServer.getTitle());
            assertEquals(33, defaultServer.getNumberConnections());
            assertEquals(20, defaultServer.getTotalConnectionLimit());
            assertEquals(33, defaultServer.getConnectionsHostRatio());
            return this;
        }

        TrafficServiceSteps verifyCurrentDistribution() {
            List<TrafficStatsItem> distributions = traffic.getDistributionRulesTraffic();
            assertNotNull(distributions);
            assertEquals(1, distributions.size());
            TrafficStatsItem distribution = distributions.get(0);
            assertEquals("Distribution 34.47% of " + FLAVOR_1, distribution.getTitle());
            assertEquals(17, distribution.getNumberConnections());
            assertEquals(40, distribution.getTotalConnectionLimit());
            assertEquals(8, distribution.getConnectionsHostRatio());
            return this;
        }

        TrafficServiceSteps verifyNextDefaultServer() {
            TrafficStatsItem defaultServer = traffic.getDefaultServerTraffic();
            assertNotNull(defaultServer);
            assertEquals("Default server 56.18% of " + DEFAULT_FLAVOR, defaultServer.getTitle());
            assertEquals(28, defaultServer.getNumberConnections());
            assertEquals(20, defaultServer.getTotalConnectionLimit());
            assertEquals(28, defaultServer.getConnectionsHostRatio());
            return this;
        }

        TrafficServiceSteps verifyNextDistribution() {
            List<TrafficStatsItem> distributions = traffic.getDistributionRulesTraffic();
            assertNotNull(distributions);
            assertEquals(1, distributions.size());
            TrafficStatsItem distribution = distributions.get(0);
            assertEquals("Distribution 43.82% of " + FLAVOR_1, distribution.getTitle());
            assertEquals(22, distribution.getNumberConnections());
            assertEquals(40, distribution.getTotalConnectionLimit());
            assertEquals(11, distribution.getConnectionsHostRatio());
            return this;
        }
    }

    private static void setupEnv(String serviceName) throws Exception {
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor(DEFAULT_FLAVOR)
                .withWhitelist(PATH_DEFAULT, PATH_DISTRIBUTION)
                .withDefaultUrlParams()
                .urn("any").protocol("any").port("0").ipv("4")
                .withDistribution().percent(FLAVOR_PERCENT).flavor(FLAVOR_1)
                .withHosts()
                .stack(PATH_DISTRIBUTION).flavor(FLAVOR_1).currentApp().ipv4("10.0.0.3").ipv6("ipv6")
                .stack(PATH_DISTRIBUTION).flavor(FLAVOR_1).currentApp().ipv4("10.0.0.2").ipv6("ipv6")
                .stack(PATH_DEFAULT).flavor(DEFAULT_FLAVOR).currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .build();

        testHelperBuilder(context).setupEnv();
    }

    private static Distribution updateDistribution() {
        DistributionBuilder builder = new DistributionBuilder();
        builder.withRule(newDistributionRule(0, UPDATED_PERCENT, newSimpleServerForFlavor(FLAVOR_1)));
        return builder.build();
    }
}
