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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.summary;

import com.comcast.redirector.api.model.Contains;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.builders.*;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.summary.RowSummary;
import com.comcast.redirector.api.model.summary.Summary;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ExpressionFactory.newMultiValueExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;
import static org.junit.Assert.*;


public class SummaryControllerIntegrationTest {
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void testGetSummary() throws Exception{
        String serviceName = getServiceNameForTest();
        new SummaryServiceSteps(serviceName)
                .setupTestData()
                .getSummary()
                .verifyDefaultServerSummary()
                .verifyDistributionSummary()
                .verifyRulesSummary();
    }


    private class SummaryServiceSteps {
        private static final String NAMESPACED_LIST_NAME = "SummaryNamespaced";
        private static final String PATH_DEFAULT_SERVER = "Zone2";
        private static final String PATH_DISTRIBUTION = "/DataCenter1/Region1/Zone1";
        private static final String PATH_RULE = "Zone1";
        private String serviceName;
        private Summary responseEntityObject;

        SummaryServiceSteps(String serviceName) throws Exception {
            this.serviceName = serviceName;
            setupEnv();
        }

        public SummaryServiceSteps getSummary() {
            responseEntityObject = apiFacade.getSummaryByNamespacedListForService(serviceName, NAMESPACED_LIST_NAME);
            return this;
        }

        public SummaryServiceSteps verifyDefaultServerSummary() {
            RowSummary defaultServer = responseEntityObject.getDefaultServer();
            assertNotNull(defaultServer);
            assertEquals(PATH_DEFAULT_SERVER, defaultServer.getRelease());
            assertEquals("89.7%", defaultServer.getDistribution());
            assertEquals(Integer.valueOf(1), defaultServer.getNode());

            return this;
        }

        public SummaryServiceSteps verifyDistributionSummary() {
            List<RowSummary> distributions = responseEntityObject.getDistributions();

            assertNotNull(distributions);
            assertEquals(1, distributions.size());
            RowSummary actualDistribution = distributions.get(0);
            assertEquals(PATH_DISTRIBUTION, actualDistribution.getRelease());
            assertEquals("10.3%", actualDistribution.getDistribution());
            assertEquals(Integer.valueOf(2), actualDistribution.getNode());

            return this;
        }

        public SummaryServiceSteps verifyRulesSummary() {
            Set<RowSummary> rules = responseEntityObject.getRules();
            assertNotNull(rules);
            assertEquals(1, rules.size());
            Iterator<RowSummary> ruleIterator = rules.iterator();
            if (ruleIterator.hasNext()) {
                RowSummary rowSummary = ruleIterator.next();
                assertEquals(PATH_RULE, rowSummary.getRelease());
                assertNull(rowSummary.getDistribution());
                assertEquals(Integer.valueOf(3), rowSummary.getNode());
            }

            return this;
        }

        public SummaryServiceSteps setupTestData() throws Exception {
            apiFacade.postOneNamespacedList(createNamespacedList());
            apiFacade.postDistributionForService(createDistribution(), serviceName);
            apiFacade.approvePendingChangesForService(serviceName);
            apiFacade.postFlavorRuleForService(createRule(), serviceName);
            apiFacade.approvePendingChangesForService(serviceName);

            return this;
        }

        private NamespacedList createNamespacedList() {
            NamespacedListBuilder builder = new NamespacedListBuilder();
            return builder.withName(NAMESPACED_LIST_NAME).withValues("192.168.0.1").build();
        }

        private Distribution createDistribution() {
            DistributionBuilder builder = new DistributionBuilder();
            builder.withRule(newDistributionRule(0, 10.3f, newSimpleServerForFlavor(PATH_DISTRIBUTION)));
            return builder.build();
        }

        private IfExpression createRule() throws Exception {
            IfExpressionBuilder builder = new IfExpressionBuilder();
            return builder.
                    withRuleName("Rule_Summary").
                    withExpression(newMultiValueExpression(Contains.class, "param", Arrays.asList(new Value(NAMESPACED_LIST_NAME)), "namespacedList")).
                    withReturnStatement(newSimpleServerForFlavor("Zone1")).build();
        }

        private void setupEnv() throws Exception {
            TestContext context = new ContextBuilder().forApp(serviceName)
                    .withDefaultServer().flavor(PATH_DEFAULT_SERVER)
                    .withWhitelist("/DataCenter2/Region1", "/DataCenter1/Region1", "/DataCenter2/Region2", "/DataCenter2/Zone2")
                    .withDefaultUrlParams()
                    .urn("any").protocol("any").port("0").ipv("4")
                    .withHosts()
                        .stack("/DataCenter1/Region1").flavor("Zone1").app(serviceName).ipv4("1.1.1.1").ipv6("1::1")
                        .stack("/DataCenter1/Region1").flavor("Zone1").app(serviceName).ipv4("2.2.2.2").ipv6("2::2")
                        .stack("/DataCenter2/Region1").flavor("Zone1").app(serviceName).ipv4("1.1.1.1").ipv6("1::1")
                        .stack("/DataCenter2/Region2").flavor("Zone2").app(serviceName).ipv4("1.1.1.1").ipv6("1::1")
                    .build();

            testHelperBuilder(context).setupEnv();
        }
    }
}
