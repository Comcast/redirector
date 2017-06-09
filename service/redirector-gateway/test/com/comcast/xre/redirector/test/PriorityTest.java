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

package com.comcast.xre.redirector.test;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.Server;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.comcast.xre.redirector.test.Utils.buildSelectServer;

public class PriorityTest {

    private static Logger log = LoggerFactory.getLogger(PriorityTest.class);

    private static String RELATIONAL = "<if><equals><param>receiverId</param><value>123-abc</value></equals><return><server>" +
            "<name>relational</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    private static String LOGICAL = "<if><or><equals><param>receiverId</param><value>456-def</value></equals><equals><param>receiverId</param><value>123-abc</value></equals></or><return><server>" +
            "<name>logical</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    private static String CONTAINS = "<if><contains><param>receiverId</param><values>123-abc,456-def,789-def</values></contains><return><server>" +
            "<name>contains</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    private static String MATCHES = "<if><matches><param>receiverId</param><pattern>12...bc</pattern></matches><return><server>" +
            "<name>matches</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    private static String RANDOM = "<if><random id=\"randomId\"><value>100</value></random><return><server>" +
            "<name>random</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    private static String DEFAULT = "<distribution><server>" +
            "<name>default</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></distribution>";


    private static String COMPLEX_CONTAINS =
            "<if>" +
                "<and>" +
                    "<equals><param>features:connection.supportsSecure</param><value>1</value></equals>" +
                    "<contains><param>mac</param><values>58:56:E8:E4:4A:13,58:56:E8:E4:4A:14</values></contains>" +
                "</and>" +
                "<if>" +
                    "<equals><param>features:allowSelfSignedWithIPAddress</param><value>1</value></equals>" +
                    "<return><server>" +
                        "<name>complex_contains</name>" +
                        "<url>xre://{host}:8080/shell</url><path>/PO</path>" +
                    "</server></return>" +
                "</if>" +
            "</if>";

    private static String COMPLEX_IP_RANGE =
            "<if>" +
                "<and>" +
                    "<equals><param>features:connection.supportsSecure</param><value>1</value></equals>" +
                    "<equals><param>features:allowSelfSignedWithIPAddress</param><value>1</value></equals>" +
                "</and>" +
                "<if>" +
                    "<and>" +
                        "<inIpRange><param>host</param><values><value>192.168.1.1</value><value>192.168.1.2</value></values></inIpRange>" +
                        "<equals><param>receiverId</param><value>123-abc</value></equals>" +
                    "</and>" +
                    "<return><server>" +
                        "<name>complex_in_ip_range</name>" +
                        "<url>xre://{host}:8080/shell</url><path>/PO</path>" +
                    "</server></return>" +
                "</if>" +
              "</if>";

    private static String COMPLEX_MIXED =
            "<if>" +
                "<or>" +
                    "<equals><param>features:connection.supportsSecure</param><value>1</value></equals>" +
                    "<equals><param>features:allowSelfSignedWithIPAddress</param><value>1</value></equals>" +
                "</or>" +
                "<if>" +
                    "<or>" +
                        "<contains><param>mac</param><values>58:56:E8:E4:4A:12,58:56:E8:E4:4A:13,58:56:E8:E4:4A:14</values></contains>" +
                        "<inIpRange><param>host</param><values><value>192.168.1.1</value><value>192.168.1.2</value></values></inIpRange>" +
                    "</or>" +
                    "<return><server>" +
                        "<name>complex_mixed</name>" +
                        "<url>xre://{host}:8080/shell</url><path>/PO</path>" +
                    "</server></return>" +
                "</if>" +
            "</if>";

    private static String COMPLEX_DEFAULT =
            "<if>" +
                "<and>" +
                    "<equals><param>features:connection.supportsSecure</param><value>1</value></equals>" +
                    "<equals><param>features:allowSelfSignedWithIPAddress</param><value>1</value></equals>" +
                "</and>" +
                "<return><server>" +
                    "<name>complex_default</name>" +
                    "<url>xre://{host}:8080/shell</url><path>/PO</path>" +
                "</server></return>" +
            "</if>";

    private static Map<String, String> params = new HashMap<String, String>() {{
        put("receiverId", "123-abc"); // fits to all expressions above
    }};

    private static Map<String, String> params_complex = new HashMap<String, String>() {{
        put("receiverId", "123-abc");
        put("features:connection.supportsSecure", "1");
        put("features:allowSelfSignedWithIPAddress", "1");
        put("host", "192.168.1.1");
        put("mac", "58:56:E8:E4:4A:13");
    }};

    @Test
    public void test() throws RuleEngineInitException {

        internalTest("relational", PriorityTest.params, CONTAINS, MATCHES, RELATIONAL, DEFAULT);

        internalTest("relational", PriorityTest.params, RANDOM, RELATIONAL, LOGICAL, DEFAULT);

        internalTest("logical", PriorityTest.params, RANDOM, LOGICAL, MATCHES, DEFAULT);

        internalTest("logical", PriorityTest.params, LOGICAL, RANDOM, CONTAINS, DEFAULT);

        internalTest("contains", PriorityTest.params, MATCHES, CONTAINS, DEFAULT);

        internalTest("contains", PriorityTest.params, CONTAINS, RANDOM, DEFAULT);

        internalTest("matches", PriorityTest.params, RANDOM, MATCHES, DEFAULT);

        internalTest("matches", PriorityTest.params, MATCHES, RANDOM, DEFAULT);


        internalTest("complex_default", PriorityTest.params_complex, COMPLEX_CONTAINS, COMPLEX_IP_RANGE, COMPLEX_MIXED, COMPLEX_DEFAULT, DEFAULT);

        internalTest("complex_in_ip_range", PriorityTest.params_complex, COMPLEX_CONTAINS, COMPLEX_IP_RANGE, COMPLEX_MIXED, DEFAULT);

        internalTest("complex_contains", PriorityTest.params_complex, COMPLEX_CONTAINS, COMPLEX_MIXED, DEFAULT);

        internalTest("complex_mixed", PriorityTest.params_complex, COMPLEX_MIXED, DEFAULT);
    }

    private static void internalTest(String expectedServerName, Map<String, String> params, String... statements)
            throws RuleEngineInitException {
        Model model = buildSelectServer(statements);
        String serverName = ((Server)model.execute(params)).getName();
        Assert.assertEquals(expectedServerName, serverName);
        log.info("Correct result. Server name = " + serverName);
    }



}
