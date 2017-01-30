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
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;

// TODO: test is very confusing. It should validate percentage, but it validates default statement instead. Marking it as ignore
public class URLRuleModelPercentTest {

    private static String CONDITION_TEST_1 = "<if><percent><value>80</value></percent><return>" +
            "<urlRule><urn>www.uri.com</urn><port></port><protocol></protocol></urlRule></return></if>";

    private static String CONDITION_TEST_2 = "<if><percent><value>20</value></percent><return>" +
            "<urlRule><urn></urn><port>100</port><protocol></protocol></urlRule></return></if>";

    private static String DEFAULT_SECTION = "<default><urlRule>" +
            "<urn>default-urn</urn><port>9090</port><protocol>default-protocol</protocol><ipProtocolVersion>4</ipProtocolVersion></urlRule></default>";

    @Ignore("test is very confusing. It should validate percentage, but it validates default statement instead. Marking it as ignore")
    @Test
    public void getFromAllConditionsValues() throws Exception {
        AbstractModel model = fromStatements(CONDITION_TEST_1, CONDITION_TEST_2, DEFAULT_SECTION);
        Map<String, String> params = Collections.singletonMap("mac", "B4:F2:E8:C6:F1:69");

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
