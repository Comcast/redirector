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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.isempty;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class IsEmptyExpressionTest {
    private static final String RULE_RETURN = "rule-return";
    private static final String DEFAULT_RETURN = "fallback-return";
    private static final String TEST_PARAM = "testParam";
    private static String expression =
            "<isEmpty><param>" + TEST_PARAM + "</param></isEmpty>" +
                    "<return>\n" +
                    "            <server>\n" +
                    "                <name>rule</name>\n" +
                    "                <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                    "                <path>" + RULE_RETURN + "</path>\n" +
                    "                <description>rule server route</description>\n" +
                    "            </server>\n" +
                    "        </return>";

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
        String selectServerXML = "<selectServer><if>" + expression + "</if><distribution>" + defaultStatement + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        model = new Model(document, StaticNamespacedListRepository.emptyHolder());
    }

    @Test
    public void testNotEmptyParameterDoNotHitTheRule() {
        Map<String, String> params = new HashMap<>();
        params.put(TEST_PARAM, "192.168.201.5");

        Server returnedServer = (Server) model.execute(params);

        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());
    }

    @Test
    public void testNullParameterHitsTheRule() {
        Map<String, String> params = new HashMap<>();
        params.put(TEST_PARAM, null);

        Server returnedServer = (Server) model.execute(params);

        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());
    }

    @Test
    public void testNonExistingParameterHitsTheRule() {
        Map<String, String> params = new HashMap<>();
        params.remove(TEST_PARAM);

        Server returnedServer = (Server)model.execute(params);

        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());
    }

    @Test
    public void testEmptyParameterHitsTheRule() {
        Map<String, String> params = new HashMap<>();
        params.put(TEST_PARAM, "");

        Server returnedServer = (Server)model.execute(params);

        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
