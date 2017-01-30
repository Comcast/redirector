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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.rule;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.model.AbstractModel;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.model.UrlParams;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;

public class URLRuleModelGluingTest {

    private static String CONDITION_1 = "<if><equals><param>receiverId</param><value>123-abc</value></equals><return>" +
            "<urlRule><urn>www.uri.com</urn><port>0</port><protocol></protocol></urlRule></return></if>";

    private static String CONDITION_2 = "<if><equals><param>receiverId</param><value>123-abc</value></equals><return>" +
            "<urlRule><urn></urn><port>100</port><protocol></protocol><ipProtocolVersion>6</ipProtocolVersion></urlRule></return></if>";

    private static String CONDITION_3 = "<if><equals><param>receiverId</param><value>123-abc</value></equals><return>" +
            "<urlRule><urn></urn><port></port><protocol>xre</protocol></urlRule></return></if>";

    private static String CONDITION_4 = "<if><equals><param>receiverId</param><value>123-abc-other</value></equals><return>" +
            "<urlRule><urn></urn><port></port><protocol>xre</protocol></urlRule></return></if>";

    private static String DEFAULT_SECTION = "<default><urlRule>" +
            "<urn>default-urn</urn><port>9090</port><protocol>default-protocol</protocol><ipProtocolVersion>4</ipProtocolVersion></urlRule></default>";

    @Test
    public void getFromAllConditionsValues() throws Exception {
        AbstractModel model = fromStatements(CONDITION_1, CONDITION_2, CONDITION_3, CONDITION_4, DEFAULT_SECTION);
        Map<String, String> params = Collections.singletonMap("receiverId", "123-abc");

        UrlParams result = ((UrlParams) model.execute(params));

        Assert.assertEquals("www.uri.com", result.getUrn());
        Assert.assertEquals(100, result.getPort().intValue());
        Assert.assertEquals("xre", result.getProtocol());
        Assert.assertEquals(6, result.getIPProtocolVersion().intValue());
    }

    @Test
    public void getValueFromOneConditionAndOtherFromDefault() throws Exception {
        AbstractModel model = fromStatements(CONDITION_1, DEFAULT_SECTION);
        Map<String, String> params = Collections.singletonMap("receiverId", "123-abc");

        UrlParams result = ((UrlParams) model.execute(params));

        Assert.assertEquals("www.uri.com", result.getUrn());
        Assert.assertEquals(9090, result.getPort().intValue());
        Assert.assertEquals("default-protocol", result.getProtocol());
        Assert.assertEquals(4, result.getIPProtocolVersion().intValue());
    }

    @Test
    public void getValueFromOneConditionSecondIsIncorrectAndOtherFromDefault() throws Exception {
        AbstractModel model = fromStatements(CONDITION_1, CONDITION_4, DEFAULT_SECTION);
        Map<String, String> params = Collections.singletonMap("receiverId", "123-abc");

        UrlParams result = ((UrlParams) model.execute(params));

        Assert.assertEquals("www.uri.com", result.getUrn());
        Assert.assertEquals(9090, result.getPort().intValue());
        Assert.assertEquals("default-protocol", result.getProtocol());
    }

    @Test
    public void getValueFromDefault() throws Exception {
        AbstractModel model = fromStatements(CONDITION_4, DEFAULT_SECTION);
        Map<String, String> params = Collections.emptyMap();

        UrlParams result = ((UrlParams) model.execute(params));

        Assert.assertEquals("default-urn", result.getUrn());
        Assert.assertEquals(9090, result.getPort().intValue());
        Assert.assertEquals("default-protocol", result.getProtocol());
    }

    private static AbstractModel fromStatements(String... statements) throws RuleEngineInitException {
        StringBuilder sb = new StringBuilder();
        sb.append("<urlRules>");
        for (String statement : statements) {
            sb.append(statement);
        }
        sb.append("</urlRules>");
        return new URLRuleModel(fromString(sb.toString()), StaticNamespacedListRepository.emptyHolder());
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
