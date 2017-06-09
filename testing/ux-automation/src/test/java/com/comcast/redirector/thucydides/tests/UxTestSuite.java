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

package com.comcast.redirector.thucydides.tests;

import com.comcast.redirector.thucydides.tests.main.changes.*;
import com.comcast.redirector.thucydides.tests.main.deciderRules.DeciderRulesTest;
import com.comcast.redirector.thucydides.tests.main.distribution.DistributionOfflineTest;
import com.comcast.redirector.thucydides.tests.main.distribution.DistributionTest;
import com.comcast.redirector.thucydides.tests.main.modelInitializer.ModelInitializerTest;
import com.comcast.redirector.thucydides.tests.main.namespaced.*;
import com.comcast.redirector.thucydides.tests.main.partners.PartnersTest;
import com.comcast.redirector.thucydides.tests.main.rules.flavor.rule.FlavorRulesTest;
import com.comcast.redirector.thucydides.tests.main.rules.flavor.rule.FlavorRulesTestOfflineMode;
import com.comcast.redirector.thucydides.tests.main.rules.flavor.template.FlavorRulesTemplatesTest;
import com.comcast.redirector.thucydides.tests.main.rules.flavor.template.FlavorRulesTemplatesTestOfflineMode;
import com.comcast.redirector.thucydides.tests.main.rules.url.rule.UrlRulesTest;
import com.comcast.redirector.thucydides.tests.main.rules.url.rule.UrlRulesTestOfflineMode;
import com.comcast.redirector.thucydides.tests.main.rules.url.template.UrlRulesTemplatesTest;
import com.comcast.redirector.thucydides.tests.main.rules.url.template.UrlRulesTemplatesTestOfflineMode;
import com.comcast.redirector.thucydides.tests.main.settings.SettingOfflineTest;
import com.comcast.redirector.thucydides.tests.main.settings.SettingsTest;
import com.comcast.redirector.thucydides.tests.main.stacksmanagement.StacksManagementTest;
import com.comcast.redirector.thucydides.tests.main.stacksmanagement.StacksManagementTestOffline;
import com.comcast.redirector.thucydides.tests.main.suite.SuiteTest;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.comcast.redirector.thucydides.tests.UxTestSuite.Constants.*;
import static it.context.Operations.EQUALS;
import static it.context.Operations.IN_IP_RANGE_NAMESPACED;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        StacksManagementTest.class,
        StacksManagementTestOffline.class,
        SettingsTest.class,
        SettingOfflineTest.class,
        DistributionTest.class,
        DistributionOfflineTest.class,
        FlavorRulesTest.class,
        FlavorRulesTestOfflineMode.class,
        FlavorRulesTemplatesTest.class,
        FlavorRulesTemplatesTestOfflineMode.class,
        UrlRulesTest.class,
        UrlRulesTestOfflineMode.class,
        UrlRulesTemplatesTest.class,
        UrlRulesTemplatesTestOfflineMode.class,
        NamespacedUnencodedOnlineModeTest.class,
        NamespacedIpOnlineModeTest.class,
        NamespacedEncodedOnlineModeTest.class,
        NamespacedUnencodedOfflineModeTest.class,
        NamespacedIpOfflineModeTest.class,
        NamespacedEncodedOfflineModeTest.class,
        SuiteTest.class,
        FlavorRuleAndTemplateChangesOnlineModeTest.class,
        FlavorRuleAndTemplateChangesOfflineModeTest.class,
        UrlRuleAndTemplatePendingChangesOnlineModeTest.class,
        UrlRuleAndTemplatePendingChangesOfflineModeTest.class,
        DistributionChangesOnlineModeTest.class,
        DistributionChangesOfflineModeTest.class,
        ModelInitializerTest.class,
        PartnersTest.class,
        DeciderRulesTest.class
})
public class UxTestSuite {
    private static final String ZK_CONNECTION = "localhost:21823";

    public static class Constants {
        public static final String APP_NAME = "xreGuide";
        public static final String APP_NAME_3 = "xreApp";
        public static final String URL_RULE_FOR_EDIT = "URL_RULE_FOR_EDIT";
        public static final String RULE_FLAVOR = "RULE_FLAVOR";
        public static final String RULE_FLAVOR_2 = "RULE_FLAVOR_2";
        public static final String RULE_FLAVOR_3 = "RULE_FLAVOR_3";
        public static final String RULE_FLAVOR_OFFLINE = "RULE_FLAVOR_OFFLINE";
        public static final String DISTRIBUTION_FLAVOR = "DISTRIBUTION_FLAVOR";
        public static final String OFFLINE_DISTRIBUTION_FLAVOR = "OFFLINE_DISTRIBUTION_FLAVOR";
        public static final String DEFAULT_FLAVOR = "DEFAULT_FLAVOR";
        public static final String DEFAULT_FLAVOR2 = "DEFAULT_FLAVOR2";
        public static final String DEFAULT_FLAVOR2_OFFLINE = "DEFAULT_FLAVOR2_OFFLINE";
        public static final String NAMESPACED_LIST = "NAMESPACED_LIST";
        public static final String NAMESPACED_LIST_VALUE = "10.10.10.10";
        public static final String STACK1 = "/PO/POC1";
        public static final String FLAVOR1 = "1.0";
        public static final String STACK2 = "/PO/POC2";
        public static final String FLAVOR2 = "2.0";
        public static final String STACK3 = "/PO/POC3";
        public static final String FLAVOR3 = "3.0";
    }

    private static IntegrationTestHelper helper;

    @BeforeClass
    public static void startTestSuite() throws Exception {
        String service = APP_NAME;
        String whiteListedStack = "/dc/uniqueStack";
        String defaultUrlProtocol = "4";

        TestContext context = new ContextBuilder().forApp(service)
                .withDefaultServer().flavor(DEFAULT_FLAVOR)
                .withDefaultUrlParams()
                    .urn("shell").protocol("xre").port("10001").ipv(defaultUrlProtocol)
                .withUrlRule()
                    .id(URL_RULE_FOR_EDIT).left("test").operation(EQUALS).right("value")
                    .urn("shell").protocol("xre").port("10002").ipv(defaultUrlProtocol)
                .withFlavorRule()
                    .id(RULE_FLAVOR).left("clientAddress").operation(IN_IP_RANGE_NAMESPACED).right(NAMESPACED_LIST)
                .withWhitelist(whiteListedStack).withNamespacedList(NAMESPACED_LIST, NAMESPACED_LIST_VALUE)
                .withHosts()
                    .stack(whiteListedStack).flavor(RULE_FLAVOR).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(whiteListedStack).flavor(RULE_FLAVOR_2).ipv4("10.0.0.3").ipv6("ff01::43").app(service)
                    .stack(whiteListedStack).flavor(RULE_FLAVOR_3).ipv4("10.0.0.4").ipv6("ff01::43").app(service)
                    .stack(whiteListedStack).flavor(RULE_FLAVOR_OFFLINE).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(whiteListedStack).flavor(DEFAULT_FLAVOR).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(whiteListedStack).flavor(DISTRIBUTION_FLAVOR).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(whiteListedStack).flavor(OFFLINE_DISTRIBUTION_FLAVOR).ipv4("10.0.0.2").ipv6("ff01::42").app(service)
                    .stack(whiteListedStack).flavor(DEFAULT_FLAVOR2).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(whiteListedStack).flavor(DEFAULT_FLAVOR2_OFFLINE).ipv4("10.0.0.1").ipv6("ff01::41").app(service)
                    .stack(STACK1).flavor(FLAVOR1).ipv4("10.0.1.1").ipv6("ff01::55").app(service)
                    .stack(STACK2).flavor(FLAVOR2).ipv4("10.0.1.2").ipv6("ff01::56").app(service)
                    .stack(STACK3).flavor(FLAVOR3).ipv4("10.0.1.3").ipv6("ff01::57").app(APP_NAME_3)
                .build();

        helper = setupTestHelperBuilder(context).startDataStore();
        forceUpdateOfflineWebServiceSnapshot();
    }

    private static void forceUpdateOfflineWebServiceSnapshot() throws Exception {
        helper.getDataStore().triggerModelUpdated();
    }

    @AfterClass
    public static void stopTestSuite() throws Exception {
        if (helper != null)
            helper.stopDataStore();
    }

    private static IntegrationTestHelper.SimpleDataSourceBasedIntegrationHelperBuilder setupTestHelperBuilder
            (TestContext context) throws Exception {
        Config config = new Config();
        config.setZooKeeperConnection(ZK_CONNECTION);
        config.setZooKeeperConnectionTimeout(1000);
        config.setZooKeeperSessionTimeout(2000);

        return new IntegrationTestHelper.Builder()
                .config(config)
                .context(context)
                .buildSimpleDataSourceBasedIntegrationHelper();
    }

    public static IntegrationTestHelper getIntegrationTestHelper() {
        return helper;
    }
}
