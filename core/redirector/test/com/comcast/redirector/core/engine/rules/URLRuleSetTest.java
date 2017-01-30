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

package com.comcast.redirector.core.engine.rules;

import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.model.UrlParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class URLRuleSetTest {

    @Test
    public void testRuleSetNotAvailableBecauseModelIsNull() throws Exception {
        URLRuleSet ruleSet = new URLRuleSet.Builder()
                .build();

        Assert.assertFalse(ruleSet.isAvailable());
    }

    @Test
    public void testRuleSetFallbackIntoFallbackParameters() throws Exception {
        Map<String, String> context = Collections.emptyMap();
        URLRuleSet ruleSet = new URLRuleSet.Builder()
                .setFallbackIPProtocolVersion(4)
                .setFallbackPort(10004)
                .setFallbackProtocol("xre")
                .setFallbackUrn("shell")
                .build();

        UrlParams response = ruleSet.getUrlParams(context);
        Assert.assertEquals(new Integer(10004), response.getPort());
        Assert.assertEquals(new Integer(4), response.getIPProtocolVersion());
        Assert.assertEquals("xre", response.getProtocol());
        Assert.assertEquals("shell", response.getUrn());
    }

    @Test
    public void testRuleSetIsAvailableWithValidModel() throws Exception {
        URLRuleSet ruleSet = new URLRuleSet.Builder()
                .setModel(mock(URLRuleModel.class))
                .build();
        Assert.assertTrue(ruleSet.isAvailable());
    }

    @Test
    public void testSuccessResponse() {
        Map<String, String> context = Collections.emptyMap();
        URLRuleModel model = mock(URLRuleModel.class);
        UrlParams result = new UrlParams();
        result.setPort(20004);
        result.setProtocol("xres");
        result.setIPProtocolVersion(6);
        when(model.execute(anyMap())).thenReturn(result);

        URLRuleSet ruleSet = new URLRuleSet.Builder()
                .setModel(model)
                .setFallbackIPProtocolVersion(4)
                .setFallbackPort(10004)
                .setFallbackProtocol("xre")
                .setFallbackUrn("shell")
                .build();

        UrlParams response = ruleSet.getUrlParams(context);
        Assert.assertEquals(new Integer(20004), response.getPort());
        Assert.assertEquals(new Integer(6), response.getIPProtocolVersion());
        Assert.assertEquals("xres", response.getProtocol());
        Assert.assertEquals("shell", response.getUrn());
    }
}
