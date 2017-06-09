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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.controllers.DistributionController;
import it.context.ContextBuilder;
import it.context.TestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.testHelperBuilder;

public class DistributionControllerIntegrationTest {

    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    /**
     * test check for {@link DistributionController#saveDistribution(Distribution, javax.ws.rs.core.UriInfo, String)}
     * test check for {@link DistributionController#getDistribution(String)}
     */
    @Test
    public void testSaveGetDistribution_JsonObject() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new DistributionSteps(serviceName).createDistribution().post().verify()
                .get()
                .verifyBeforeApprove().approve()
                .get().verifyAfterApprove();

    }

    private class DistributionSteps {
        String serviceName;
        Distribution distribution;
        Distribution responseEntityObject;

        DistributionSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        DistributionSteps createDistribution() {
            distribution = getDistribution();
            return this;
        }

        DistributionSteps post() {
            responseEntityObject = apiFacade.postDistributionForService(distribution, serviceName);
            return this;
        }

        DistributionSteps get() {
            responseEntityObject = apiFacade.getDistributionForService(serviceName);
            return this;
        }

        DistributionSteps verifyBeforeApprove() {
            Assert.assertEquals(0, responseEntityObject.getRules().size());
            return this;
        }

        DistributionSteps verifyAfterApprove() {
            Assert.assertEquals(distribution.getRules().size(), responseEntityObject.getRules().size());
            Assert.assertEquals(distribution.getRules(), responseEntityObject.getRules());
            return this;
        }

        DistributionSteps verify() {
            Assert.assertEquals(distribution.getRules().size(), responseEntityObject.getRules().size());
            Assert.assertEquals(distribution.getRules(), responseEntityObject.getRules());
            return this;
        }

        DistributionSteps approve() {
            apiFacade.approveDistributionForService(serviceName);
            return this;
        }

    }

    private Distribution getDistribution() {
        DistributionBuilder builder = new DistributionBuilder();
        builder.withRule(newDistributionRule(0, 10.3f, newSimpleServerForFlavor("zone3")));
        builder.withRule(newDistributionRule(1, 25.5f, newSimpleServerForFlavor("zone4")));
        return builder.build();
    }

    private static void setupEnv(String serviceName) throws Exception {
        TestContext context = new ContextBuilder().forApp(serviceName)
                .withDefaultServer().flavor("zone2")
                .withWhitelist("/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2")
                .withDefaultUrlParams()
                    .urn("any").protocol("any").port("0").ipv("4")
                .withHosts()
                    .stack("/DataCenter2/Region1").flavor("zone2").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                    .stack("/DataCenter1/Region2").flavor("zone3").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                    .stack("/DataCenter2/Region2").flavor("zone4").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .build();

        testHelperBuilder(context).setupEnv();
    }
}
