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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.pathrule;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PathRuleWithMultipleNamespacesExpressionTest {
    private static final String RULE_RETURN = "rule-return";
    private static final String DEFAULT_RETURN = "default-return";

    private static final String WAREHOUSES_NAMESPACE_NAME = "Warehouses";
    private static final String SOME_INT_NAMESPACE_NAME = "SomeIntegerList";
    private static final String CLIENT_ADDRESS_PARAM = "clientAddress";

    private static String expression =
                    "<contains>\n" +
                    "    <param>" + CLIENT_ADDRESS_PARAM + "</param>\n" +
                    "    <namespacedList>\n" +
                    "        <value>" + WAREHOUSES_NAMESPACE_NAME + "</value>\n" +
                    "        <value>" + SOME_INT_NAMESPACE_NAME + "</value>\n" +
                    "    </namespacedList>\n" +
                    "</contains>\n" +
                    "<return>\n" +
                    "    <server>\n" +
                    "        <isNonWhitelisted>false</isNonWhitelisted>\n" +
                    "        <name>123</name>\n" +
                    "        <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                    "        <path>" + RULE_RETURN + "</path>\n" +
                    "        <description>123 server route</description>\n" +
                    "    </server>\n" +
                    "</return>";

    private static String defaultStatement =
            "<server>\n" +
                    "            <name>Default Server</name>\n" +
                    "            <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                    "            <secureUrl></secureUrl>\n" +
                    "            <path>" + DEFAULT_RETURN + "</path>\n" +
                    "            <description>Default server route</description>\n" +
                    "        </server>";

    private Model model;

    @Before
    public void before() throws Exception {
        NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
        namespacedListsBatch.addValues(WAREHOUSES_NAMESPACE_NAME, new ArrayList<String>() {{
            add("50.227.22.0");
            add("68.46.241.105");
            add("73.116.196.0");
        }});
        namespacedListsBatch.addValues(SOME_INT_NAMESPACE_NAME, new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
        }});

        String selectServerXML = "<selectServer><if>" + expression + "</if><distribution>" + defaultStatement + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        model = new Model(document, StaticNamespacedListRepository.of(namespacedListsBatch));
    }

    @Test
    public void testExpression() {
        Map<String, String> params = new HashMap<>();
        params.put(CLIENT_ADDRESS_PARAM, "68.46.241.105");

        Server returnedServer = (Server)model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put(CLIENT_ADDRESS_PARAM, "1");
        returnedServer = (Server)model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put(CLIENT_ADDRESS_PARAM, "100500");
        returnedServer = (Server)model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
