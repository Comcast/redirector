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

package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.*;

public class LogicalExpressionTest {
    private static final String IP_BEFORE_RANGE = "75.20.128.0";
    private static final String IP_IN_RANGE = "76.20.128.4";
    private static final String IP_AFTER_RANGE = "77.20.128.0";
    private static final String IP_FROM = "76.20.128.0";
    private static final String IP_TO = "76.20.135.255";
    private static final String RULE_RETURN = "rule-return";
    private static final String DEFAULT_RETURN = "fallback-return";

    private static final Map<String, String> IP_ADDRESSES_TO_PATHS = new HashMap<String, String>() {{
        put(IP_BEFORE_RANGE, DEFAULT_RETURN);
        put(IP_IN_RANGE, RULE_RETURN);
        put(IP_AFTER_RANGE, DEFAULT_RETURN);
    }};

    private static String expression =
        "<and>\n" +
            "            <greaterOrEqual type=\"version\">\n" +
            "                <param>clientAddress</param>\n" +
            "                <value>" + IP_FROM + "</value>\n" +
            "            </greaterOrEqual>\n" +
            "            <lessOrEqual type=\"version\">\n" +
            "                <param>clientAddress</param>\n" +
            "                <value>" + IP_TO + "</value>\n" +
            "            </lessOrEqual>\n" +
            "        </and>" +
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
    public void testInNonConcurrentEnv() throws Exception {
        for (Map.Entry<String, String> entry : IP_ADDRESSES_TO_PATHS.entrySet()) {
            Assert.assertTrue(test(entry.getKey(), entry.getValue()));
        }
    }

    @Test
    public void testConcurrentEnv() {
        Callable<Boolean> worker = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                for (;;) {
                    for (Map.Entry<String, String> entry : IP_ADDRESSES_TO_PATHS.entrySet()) {
                        if (!test(entry.getKey(), entry.getValue())) {
                            System.out.println("\n\n\n" + Thread.currentThread().getName() + " Rule failed with input " + entry.getKey() + ", expected :" + entry.getValue() + "\n\n");
                            return false;
                        }
                    }

                    try {
                        Thread.sleep((long) (Math.random() * 50));
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                return true;
            }
        };

        int corePoolSize = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(corePoolSize);
        List<Future<Boolean>> futureList = new ArrayList<>(corePoolSize);
        for (int i = 0; i < corePoolSize; i++) {
            futureList.add(executorService.submit(worker));
        }

        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("interrupted");
        }
        try {
            for (Future<Boolean> f : futureList) {
                Assert.assertTrue(f.get(1, TimeUnit.SECONDS));
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout getting false value");
        } catch (ExecutionException|InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    private boolean test(String ip, String expectedPath) {
        Map<String, String> params = new HashMap<>();
        params.put("clientAddress", ip);

        Server returnedServer = (Server)model.execute(params);
        return returnedServer.getPath().equals(expectedPath);
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
