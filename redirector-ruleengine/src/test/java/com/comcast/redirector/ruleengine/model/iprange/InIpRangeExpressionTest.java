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

package com.comcast.redirector.ruleengine.model.iprange;

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

public class InIpRangeExpressionTest {
    private static final String RULE_RETURN = "rule-return";
    private static final String DEFAULT_RETURN = "fallback-return";
    private static String expression =
            "<inIpRange><param>clientAddress</param><values><value>192.168.201.5</value></values><namespacedList>Warehouses</namespacedList></inIpRange>" +
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
        NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
        namespacedListsBatch.addValues("Warehouses", new ArrayList<String>() {{
            add("50.227.22.0/25");
            add("68.46.241.105");
            add("73.116.196.0/23");
            add("FF80:0000:0000:0000:0123:1234:ABCD:EF12");
            add("2001:db8::/63");
        }});

        String selectServerXML = "<selectServer><if>" + expression + "</if><distribution>" + defaultStatement + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        model = new Model(document, StaticNamespacedListRepository.of(namespacedListsBatch));
    }

    @Test
    public void testValuesExpression() {
        Map<String, String> params = new HashMap<>();
        params.put("clientAddress", "192.168.201.5");

        Server returnedServer = (Server) model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "192.168.201.4");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());
    }

    @Test
    public void testNamespacedListExpression() {
        Map<String, String> params = new HashMap<>();
        params.put("clientAddress", "50.227.22.1");

        Server returnedServer = (Server) model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "50.227.22.129");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());

        params.put("clientAddress", "68.46.241.105");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "68.46.241.106");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());

        params.put("clientAddress", "73.116.196.0/24");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "73.116.195.0/24");
        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());
    }

    @Test
    public void testIpV6() {
        Map<String, String> params = new HashMap<>();
        params.put("clientAddress", "51.227.22.1");

        Server returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());

        params.put("clientAddress", "FF80::123:1234:ABCD:EF12");

        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "FF80::123:1234:ABCD:EF11");

        returnedServer = (Server) model.execute(params);
        Assert.assertEquals(DEFAULT_RETURN, returnedServer.getPath());

        params.put("clientAddress", "2001:db8:0000:0001:ffff:ffff:ffff:ffff");

        returnedServer = (Server)model.execute(params);
        Assert.assertEquals(RULE_RETURN, returnedServer.getPath());

        params.put("clientAddress", "2001:db8:0000:0002:ffff:ffff:ffff:ffff");

        returnedServer = (Server) model.execute(params);
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
