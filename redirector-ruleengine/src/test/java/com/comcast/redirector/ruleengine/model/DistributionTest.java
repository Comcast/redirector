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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionTest {
    private static Logger log = LoggerFactory.getLogger(DistributionTest.class);

    private static String groupA =
            "<rule>" +
                "<percent>20</percent>" +
                    "<server>" +
                        "<name>A</name>" +
                        "<url>xre://{host}:8080/shell</url>" +
                        "<path>/PO/POC4/current/xreGuide</path>" +
                    "</server>" +
            "</rule>";

    private static String groupB =
            "<rule>" +
                "<percent>30</percent>" +
                    "<server>" +
                        "<name>B</name>" +
                        "<url>xre://{host}:8080/shell</url>" +
                        "<path>/PO/POC6/next/xreGuide</path>" +
                    "</server>" +
            "</rule>";

    private static String groupC =
                "<server>" +
                    "<name>C</name>" +
                    "<url>xre://{host}:8080/shell</url>" +
                    "<path>/BR/BRC5/current/xreGuide</path>" +
                "</server>";

    private static ObjectMapper mapper = new ObjectMapper();
    private static NamespacedListRepository namespacedListsHolder = StaticNamespacedListRepository.emptyHolder();
    private static List<String> MAC_ADDRESSES = new ArrayList<String>();
    private static List<String> ACCOUNT_IDS = new ArrayList<String>();
    static {
        try {
            ACCOUNT_IDS = mapper.readValue(DistributionTest.class.getResourceAsStream("/accountIds.json"),
                    new TypeReference<List<String>>() {});
            MAC_ADDRESSES = mapper.readValue(DistributionTest.class.getResourceAsStream("/macaddresses.json"),
                    new TypeReference<List<String>>() {});
        } catch (IOException e) {
            log.error("can't prepare list of mac addresses");
        }
    }

    @Test
    public void testPercentageCalculationByAccountId() throws Exception {
        String selectServerXML = "<selectServer><distribution>" + groupA + groupB + groupC + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        Model model = new Model(document, namespacedListsHolder);

        int aCounter = 0; // 20%
        int bCounter = 0; // 30%
        int cCounter = 0; // 50%
        final int REQUESTS_COUNT = 200000;
        for (int i = 1; i <= REQUESTS_COUNT; i++) {
            Map<String, String> params = new HashMap<>();
            if (i % (ACCOUNT_IDS.size() + 1) != 0) {
                params.put(DistributionStatement.ATTRIBUTE_ACCOUNT_ID, ACCOUNT_IDS.get(i % ACCOUNT_IDS.size()));
            }
            Server returnedServer = (Server)model.execute(params);
            String serverName = returnedServer.getName();
            if (serverName.equals("A")) {
                aCounter++;
            } else if(serverName.equals("B")) {
                bCounter++;
            } else if (serverName.equals("C")) {
                cCounter++;
            }
        }
        // 0.6% error
        double aPercent = ((double)aCounter / REQUESTS_COUNT) * 100;
        double bPercent = ((double)bCounter / REQUESTS_COUNT) * 100;
        double cPercent = ((double)cCounter / REQUESTS_COUNT) * 100;

        Assert.assertTrue(aPercent > 19.4);
        Assert.assertTrue(aPercent < 20.6);

        Assert.assertTrue(bPercent > 29.4);
        Assert.assertTrue(bPercent < 30.6);

        Assert.assertTrue(cPercent > 49.4);
        Assert.assertTrue(cPercent < 50.6);

        log.info("DistributionTest.testPercentage() passed");
    }

    @Test
    public void testPercentageCalculationByMac() throws Exception {
        String selectServerXML = "<selectServer><distribution>" + groupA + groupB + groupC + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        Model model = new Model(document, namespacedListsHolder);

        int aCounter = 0; // 20%
        int bCounter = 0; // 30%
        int cCounter = 0; // 50%
        final int REQUESTS_COUNT = 200000;
        for (int i = 1; i <= REQUESTS_COUNT; i++) {
            Map<String, String> params = new HashMap<>();
            if (i % (MAC_ADDRESSES.size() + 1) != 0) {
                params.put(DistributionStatement.ATTRIBUTE_MAC, MAC_ADDRESSES.get(i % MAC_ADDRESSES.size()));
            }
            Server returnedServer = (Server)model.execute(params);
            String serverName = returnedServer.getName();
            if (serverName.equals("A")) {
                aCounter++;
            } else if(serverName.equals("B")) {
                bCounter++;
            } else if (serverName.equals("C")) {
                cCounter++;
            }
        }
        // 0.6% error
        double aPercent = ((double)aCounter / REQUESTS_COUNT) * 100;
        double bPercent = ((double)bCounter / REQUESTS_COUNT) * 100;
        double cPercent = ((double)cCounter / REQUESTS_COUNT) * 100;

        Assert.assertTrue(aPercent > 19.4);
        Assert.assertTrue(aPercent < 20.6);

        Assert.assertTrue(bPercent > 29.4);
        Assert.assertTrue(bPercent < 30.6);

        Assert.assertTrue(cPercent > 49.4);
        Assert.assertTrue(cPercent < 50.6);

        log.info("DistributionTest.testPercentage() passed");
    }

    @Ignore
    public void testLastPercentValidation() throws Exception {
        // b = 30%, b + b + b + b = 120%
        String selectServerXML = "<selectServer><distribution>" + groupB + groupB + groupB + groupB + groupC + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        try {
            new Model(document, namespacedListsHolder);
            // TODO: fix the test. Right now rule engine doesn't stop processing if some rule fails, hence we can't rely on exception now
            //Assert.fail("RuleEngineInitException is expected to be thrown, more than 100% was specified");
        } catch (IllegalStateException ex) {
            log.info("Correct behaviour. " + ex.getCause().toString());
        }

        // a = 20%, b = 30%, a + b + a + b = 100%
        selectServerXML = "<selectServer><distribution>" + groupA + groupB + groupA + groupB + groupC + "</distribution></selectServer>";
        document = fromString(selectServerXML);
        try {
            new Model(document, namespacedListsHolder);
            log.info("Correct behaviour. 100% is expected");
        } catch (IllegalStateException ex) {
            Assert.fail("RuleEngineInitException is not expected to be thrown, 100% can be specified " + ex.getCause().toString());
        }

        log.info("DistributionTest.testLastPercentValidation() passed");
    }

    @Ignore
    public void testValidation() {
        String selectServerXML = "<selectServer><distribution>" + groupA + groupC + groupB + "</distribution></selectServer>";
        Document document = fromString(selectServerXML);
        try {
            new Model(document, namespacedListsHolder);
            // TODO: fix the test. Right now rule engine doesn't stop processing if some rule fails, hence we can't rely on exception now
            //Assert.fail("RuleEngineInitException is expected to be thrown, second distribution child is return");
        } catch (RuleEngineInitException ex) {
            log.info("Correct behaviour. " + ex.getCause().toString());
        }

        selectServerXML = "<selectServer><distribution>" + groupA + groupB + groupB + "</distribution></selectServer>";
        document = fromString(selectServerXML);
        try {
            new Model(document, namespacedListsHolder);
            //Assert.fail("RuleEngineInitException is expected to be thrown, third distribution child is not return");
        } catch (RuleEngineInitException ex) {
            log.info("Correct behaviour. " + ex.getCause().toString());
        }

        selectServerXML = "<selectServer><distribution>" + groupA + "<NOT_A_GROUP></NOT_A_GROUP>" + groupC + "</distribution></selectServer>";
        document = fromString(selectServerXML);
        try {
            new Model(document, namespacedListsHolder);
            // TODO: fix the test. Right now rule engine doesn't stop processing if some rule fails, hence we can't rely on exception now
            //Assert.fail("RuleEngineInitException is expected to be thrown, distribution contains invalid tag NOT_A_GROUP");
        } catch (RuleEngineInitException ex) {
            log.info("Correct behaviour. " + ex.getCause().toString());
        }
        log.info("DistributionTest.testValidation() passed");
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
