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

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.config.RedirectorConfig;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.redirector.cache.BaseWebServiceIntegrationTest;
import com.comcast.redirector.core.config.ConfigLoader;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static it.helper.IntegrationTestHelper.AlreadyRunningDataSourceBasedHelperBuilder;

public class IntegrationTestUtils {
    public static AlreadyRunningDataSourceBasedHelperBuilder testHelperBuilder(TestContext context) throws Exception {
        RedirectorConfig redirectorConfig = ConfigLoader.doParse(RedirectorConfig.class, "service.properties", "redirector");

        return new IntegrationTestHelper.Builder()
            .config(BaseWebServiceIntegrationTest.coreConfigFromApiConfig(redirectorConfig))
            .context(context)
            .buildHelperForAlreadyRunningDataStore();
    }

    public static String getServiceNameForTest() {
        String className = new Throwable().getStackTrace()[1].getClassName();
        return getServiceNameForTest(className.substring(className.lastIndexOf(".") + 1));
    }

    @Deprecated
    public static String getServiceNameForTest(String prefix) {
        return prefix + "_" + new Throwable().getStackTrace()[1].getMethodName() + "_" + System.currentTimeMillis();
    }

    public static void setupEnv(String serviceName) throws Exception {
        TestContext context = new ContextBuilder().forApp(serviceName)
            .withDefaultServer().flavor("zone2")
            .withWhitelist("/DataCenter1/Region1", "/DataCenter2/Region1", "/DataCenter1/Region2", "/DataCenter2/Region2", "/DataCenter2/Zone2")
            .withDefaultUrlParams()
                .urn("any").protocol("any").port("0").ipv("4")
            .withHosts()
                .stack("/DataCenter1/Region1").flavor("zone2").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
                .stack("/DataCenter2/Region1").flavor("zone1").currentApp().ipv4("10.0.0.1").ipv6("ipv6")
            .build();

        testHelperBuilder(context).setupEnv();
    }

    public static IfExpression buildUrlRule(String ruleName, String paramName, String value) throws InstantiationException, IllegalAccessException {
        return new IfExpressionBuilder().
                withRuleName(ruleName).
                withExpression(newSingleParamExpression(Equals.class, paramName, value)).
                withReturnStatement(newUrlParams("urn", "xre", "8888", "4")).
                build();
    }
}
