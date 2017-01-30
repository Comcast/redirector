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

package com.comcast.xre.redirector.test.selector;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.Server;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.comcast.xre.redirector.test.Utils.buildSelectServer;

public class ZKSelectServerXMLServiceTest {
    private static String DEFAULT = "<distribution><server>" +
            "<name>default</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></distribution>";

    private static String IPv6 = "<if><equals><param>someParam</param><value>fe80:0:0:0:200:f8ff:fe21:67cf</value></equals><return><server>" +
            "<name>relational</name>" +
            "<url>xre://{host}:8080/shell</url><path>/PO</path></server></return></if>";

    @Test
    public void testEmptyIPV6ParameterDoesNotProduceException() {
        Map<String, String> params = new HashMap<String, String>() {{
            put("receiverId", "123-abc"); // fits to all expressions above
        }};
        try {
            Model model = buildSelectServer(IPv6, DEFAULT);
            ((Server) model.execute(params)).getName();
        } catch (Exception e) {
            org.junit.Assert.fail();
        }

    }
}
