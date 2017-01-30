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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import com.comcast.redirector.ruleengine.repository.impl.StaticNamespacedListRepository;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class CaseSensitiveAttributeTest {

    private static Logger log = LoggerFactory.getLogger(CaseSensitiveAttributeTest.class);
    private static NamespacedListRepository namespacedListsHolder = StaticNamespacedListRepository.emptyHolder();

    @Test
    public void testCaseSensitive() throws RuleEngineInitException {
        String xml = buildEqualsExpression("true");
        Model model = new Model(fromString(xml), namespacedListsHolder);

        Map<String, String> params = new HashMap<String, String>();
        params.put("platform", "linux");
        Assert.assertEquals("first", ((Server)model.execute(params)).getName());

        params.put("platform", "LiNuX");
        Assert.assertEquals("second", ((Server)model.execute(params)).getName());

        xml = buildContainsExpression("true");
        model = new Model(fromString(xml), namespacedListsHolder);

        params.put("mac", "ih-jk-56");
        Assert.assertEquals("first", ((Server)model.execute(params)).getName());

        params.put("mac", "Ih-jK-56");
        Assert.assertEquals("second", ((Server)model.execute(params)).getName());
    }

    @Test
    public void testCaseInsensitive() throws RuleEngineInitException {
        String xml = buildEqualsExpression("false");
        Model model = new Model(fromString(xml), namespacedListsHolder);

        Map<String, String> params = new HashMap<String, String>();
        params.put("platform", "LiNuX");
        Assert.assertEquals("first", ((Server)model.execute(params)).getName());

        xml = buildContainsExpression("false");
        model = new Model(fromString(xml), namespacedListsHolder);

        params.put("mac", "Ih-jK-56");
        Assert.assertEquals("first", ((Server)model.execute(params)).getName());
    }

    @Ignore
    public void testInvalid() {
        String xml = buildEqualsExpression("INVALID_VALUE");
        try {
            new Model(fromString(xml), namespacedListsHolder);
            // TODO: fix
            //Assert.fail("RuleEngineInitException is expected to be thrown, caseSensitive value is invalid");
        } catch (RuleEngineInitException ex) {
            log.info("Correct behaviour. " + ex.getCause());
        }

        xml = buildContainsExpression("INVALID_VALUE");
        try {
            new Model(fromString(xml), namespacedListsHolder);
            // TODO: fix
            //Assert.fail("RuleEngineInitException is expected to be thrown, caseSensitive value is invalid");
        } catch (RuleEngineInitException ex) {
            log.info("Correct behaviour. " + ex.getCause());
        }
    }

    private static Document fromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String buildEqualsExpression(String caseSensitive) {
        return "<selectServer><if>" +
                "<equals caseSensitive=\"" + caseSensitive + "\"><param>platform</param><value>linux</value></equals>" +
                "<return><server><name>first</name><url>xre://{host}:8080/shell</url><path>/PO</path></server></return>" +
                "<return><server><name>second</name><url>xre://{host}:8080/shell</url><path>/BR</path></server></return>" +
                "</if></selectServer>";
    }

    private String buildContainsExpression(String caseSensitive) {
        return "<selectServer><if>" +
                "<contains caseSensitive=\"" + caseSensitive + "\"><param>mac</param>" +
                "<values>ab-cd-12, ef-gh-34, ih-jk-56, lm-no-78</values></contains>" +
                "<return><server><name>first</name><url>xre://{host}:8080/shell</url><path>/PO</path></server></return>" +
                "<return><server><name>second</name><url>xre://{host}:8080/shell</url><path>/BR</path></server></return>" +
                "</if></selectServer>";
    }
}
