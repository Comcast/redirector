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

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.core.modelupdate.chain.validator.UrlRulesValidator;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class UrlRulesValidatorTest {
    private UrlRulesValidator testee = new UrlRulesValidator();

    @DataProvider
    public static Object[][] urlParamsForValidation() {
        return new Object[][] {
                { "1", "1", "testProtocol", "testUrn", true },
                { "0", "1", "testProtocol", "testUrn", false },
                { "-1", "1", "testProtocol", "testUrn", false },
                { null, "1", "testProtocol", "testUrn", false },
                { "1", "0", "testProtocol", "testUrn", false },
                { "1", "-1", "testProtocol", "testUrn", false },
                { "1", null, "testProtocol", "testUrn", false },
                { "1", "1", " ", "testUrn", false },
                { "1", "1", "", "testUrn", false },
                { "1", "1", null, "testUrn", false },
                { "1", "1", "testProtocol", " ", false },
                { "1", "1", "testProtocol", "", false },
                { "1", "1", "testProtocol", null, false },
        };
    }

    @Test
    public void testInvalidBecauseNullRuleSet() throws Exception {
        Assert.assertFalse(testee.validate(null).isSuccessValidation());
    }

    @Test
    public void testInvalidBecauseNullDefaultStatement() throws Exception {
        Assert.assertFalse(testee.validate(new URLRules()).isSuccessValidation());
    }

    @Test
    public void testInvalidBecauseNullDefaultUrlRuleStatement() throws Exception {
        URLRules urlRules = new URLRules();
        urlRules.setDefaultStatement(new Default());

        Assert.assertFalse(testee.validate(urlRules).isSuccessValidation());
    }

    @Test
    public void testInvalidBecauseEmptyDefaultUrlRuleStatement() throws Exception {
        URLRules urlRules = new URLRules();
        Default defaultUrlRule = new Default();
        defaultUrlRule.setUrlRule(new UrlRule());
        urlRules.setDefaultStatement(defaultUrlRule);

        Assert.assertFalse(testee.validate(urlRules).isSuccessValidation());
    }

    @Test
    @UseDataProvider("urlParamsForValidation")
    public void testValidation(String ipProtocolVersion, String port, String protocol, String urn, boolean result) throws Exception {
        URLRules urlRules = new URLRules();
        Default defaultUrlRule = new Default();

        UrlRule rule = new UrlRule();
        rule.setIpProtocolVersion(ipProtocolVersion);
        rule.setPort(port);
        rule.setProtocol(protocol);
        rule.setUrn(urn);

        defaultUrlRule.setUrlRule(rule);
        urlRules.setDefaultStatement(defaultUrlRule);

        Assert.assertEquals(result, testee.validate(urlRules).isSuccessValidation());
    }
}
