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

package com.comcast.redirector.thucydides.tests;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.thucydides.tests.cachedmode.*;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.comcast.redirector.common.RedirectorConstants.DEFAULT_SERVER_NAME;
import static com.comcast.redirector.common.RedirectorConstants.URL_TEMPLATE;
import static it.context.Operations.EQUALS;
import static com.comcast.redirector.thucydides.tests.CachedModeUxTestSuite.Constants.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    FlavorRulesTest.class,
})
public class CachedModeUxTestSuite {
    private static final String ZK_CONNECTION = "localhost:21823";

    public static class Constants {
        static final String APP_NAME = "xreGuide";
        static final String RULE_FLAVOR = "RULE_FLAVOR";
        static final String DISTRIBUTION_FLAVOR = "DISTRIBUTION_FLAVOR";
        static final String DEFAULT_FLAVOR = "DEFAULT_FLAVOR";
        static final String PENDING_DEFAULT_FLAVOR = "PENDING_DEFAULT_FLAVOR";

        public static final String FLAVOR_RULE_ID = "flavorRule";

        public static final String OFFLINE_MODE_DIALOG_TITLE = "Offline mode may be needed";
    }

    @BeforeClass
    public static void startTestSuite() throws Exception {
        String service = APP_NAME;
        String whiteListedStack = "/dc/uniqueStack";
        String ruleFlavor = RULE_FLAVOR;
        String defaultFlavor = DEFAULT_FLAVOR;
        String distributionFlavor = DISTRIBUTION_FLAVOR;
        String defaultUrlProtocol = "4";

        TestContext context = new ContextBuilder().forApp(service)
            .withFlavorRule().id(FLAVOR_RULE_ID).left("A").operation(EQUALS).right("B").flavor(ruleFlavor)
            .withDefaultServer().flavor(defaultFlavor)
            .withDistribution().percent("50.00").flavor(distributionFlavor)
            .withUrlRule().id("urlRule").left("A").operation(EQUALS).right("B").ipv("6")
            .withDefaultUrlParams().urn("shell").protocol("xre").port("10001").ipv(defaultUrlProtocol)
            .withWhitelist(whiteListedStack)
            .withHosts()
                .stack(whiteListedStack).flavor(ruleFlavor).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                .stack(whiteListedStack).flavor(defaultFlavor).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                .stack(whiteListedStack).flavor(distributionFlavor).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                .stack(whiteListedStack).flavor("flavor2").ipv4("10.0.0.1").ipv6("ff01::41").app(service)
            .build();

        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();

        Server expectedPendingDefaultServer = new Server();
        expectedPendingDefaultServer.setName(DEFAULT_SERVER_NAME);
        expectedPendingDefaultServer.setPath(PENDING_DEFAULT_FLAVOR);
        expectedPendingDefaultServer.setUrl(URL_TEMPLATE);

        helper.stopDataStore();
    }

    private static IntegrationTestHelper.SimpleDataSourceBasedIntegrationHelperBuilder setupTestHelperBuilder
            (it.context.TestContext context) throws Exception {
        Config config = new Config();
        config.setZooKeeperConnection(ZK_CONNECTION);
        config.setZooKeeperConnectionTimeout(1000);
        config.setZooKeeperSessionTimeout(2000);

        return new IntegrationTestHelper.Builder()
            .config(config)
            .context(context)
            .buildSimpleDataSourceBasedIntegrationHelper();
    }
}
