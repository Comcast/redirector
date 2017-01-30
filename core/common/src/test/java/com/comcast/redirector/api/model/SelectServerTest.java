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

package com.comcast.redirector.api.model;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SelectServerTest extends Assert {

    @Test
    public void testCreateObject() throws Exception {

        SelectServer rules = new SelectServer();
        IfExpression firstIf = new IfExpression();
        Server newReturn = new Server();
        newReturn.setUrl("xre://xre.poc1.xcal.tv:10065/failwhale");
        firstIf.setReturn(newReturn);
        AndExpression andExpression = new AndExpression();

        Matches match = new Matches();
        match.setParam("receiverId");
        match.setPattern("T[0-9]+");

        Equals equal = new Equals();
        equal.setParam("deviceType");
        equal.setValue("Parker");

        List<Expressions> conditions = new ArrayList<>();
        conditions.add(equal);
        conditions.add(match);

        andExpression.setItems(conditions);

        List<Expressions> items = new ArrayList<Expressions>();
        items.add(andExpression);

        firstIf.setItems(items);
        rules.addCondition(firstIf);
        rules.setFallbackServer(new Server("xre://xre.poc0.xcal.tv:10065/failwhale"));

        String result = serializeIt(rules, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><selectServer><if><and><equals><param>deviceType</param><value>Parker</value></equals><matches negation=\"false\"><param>receiverId</param><pattern>T[0-9]+</pattern></matches></and><return><server><url>xre://xre.poc1.xcal.tv:10065/failwhale</url></server></return></if><fallbackServer><url>xre://xre.poc0.xcal.tv:10065/failwhale</url></fallbackServer></selectServer>",
                result);
    }

    @Test
    public void testContains() throws Exception {
        SelectServer rules = new SelectServer();
        IfExpression firstIf = new IfExpression();
        Server newReturn = new Server();
        newReturn.setUrl("xre://xre.poc1.xcal.tv:10065/failwhale");
        firstIf.setReturn(newReturn);
        List<Value> values = new ArrayList<Value>();
        values.add(new Value("14:D4:FE:55:8C:4C"));
        values.add(new Value("B4:F2:E8:03:B6:F5"));
        values.add(new Value("B4:F2:E8:03:E2:DE"));
        values.add(new Value("B4:F2:E8:05:02:CC"));

        Contains contains = new Contains();
        contains.setParam("mac");
        contains.setValues(values);


        List<Value> namespaces = new ArrayList<Value>();
        namespaces.add(new Value("n1"));
        namespaces.add(new Value("n2"));

        contains.setNamespacedLists(namespaces);

        final List<Expressions> items = new ArrayList<Expressions>();
        items.add(contains);

        firstIf.setItems(items);
        rules.addCondition(firstIf);
        rules.setFallbackServer(new Server("xre://xre.poc0.xcal.tv:10065/failwhale"));
        final String result = serializeIt(rules, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><selectServer><if><contains negation=\"false\" type=\"\"><param>mac</param><values><value>14:D4:FE:55:8C:4C</value><value>B4:F2:E8:03:B6:F5</value><value>B4:F2:E8:03:E2:DE</value><value>B4:F2:E8:05:02:CC</value></values><namespacedList><value>n1</value><value>n2</value></namespacedList></contains><return><server><url>xre://xre.poc1.xcal.tv:10065/failwhale</url></server></return></if><fallbackServer><url>xre://xre.poc0.xcal.tv:10065/failwhale</url></fallbackServer></selectServer>",
                result);
    }

    /*@Test
    public void testLessOrGreater() throws Exception {
        SelectServer rules = new SelectServer();
        IfExpression firstIf = new IfExpression();
        Server newReturn = new Server();
        newReturn.setUrl("xre://69.252.105.25:10001");
        firstIf.setReturn(newReturn);

        AndExpression andExpression = new AndExpression();

        GreaterOrEqualExpression greater = new GreaterOrEqualExpression();
        greater.setParam("clientAddress");
        greater.setValue("76.26.115.0");

        LessOrEqualExpression less = new LessOrEqualExpression();
        less.setParam("clientAddress");
        less.setValue("76.26.115.255");

        List<Expressions> andItems = new ArrayList<>();
        andItems.add(greater);
        andItems.add(less);
        andExpression.setItems(andItems);

        List<Expressions> ifItems = new ArrayList<Expressions>();
        ifItems.add(andExpression);
        firstIf.setItems(ifItems);

        rules.addCondition(firstIf);
        rules.setFallbackServer(new Server("xre://xre.poc0.xcal.tv:10065/failwhale"));
        String result = serializeIt(rules, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><selectServer><if><and><greaterOrEqual><param>clientAddress</param><value>76.26.115.0</value></greaterOrEqual><lessOrEqual><param>clientAddress</param><value>76.26.115.255</value></lessOrEqual></and><return><server><url>xre://69.252.105.25:10001</url></server></return></if><fallbackServer><url>xre://xre.poc0.xcal.tv:10065/failwhale</url></fallbackServer></selectServer>",
                result);
    }*/

    @Test
    public void testRandom() throws Exception {
         SelectServer rules = new SelectServer();
        IfExpression firstIf = new IfExpression();
        Server newReturn = new Server();
        newReturn.setUrl("xre://xre.b3c9.ccp.xcal.tv:10004/shell");
        firstIf.setReturn(newReturn);
        Random random = new Random();
        random.setValue("50");

        List<Expressions> ifItems = new ArrayList<Expressions>();
        ifItems.add(random);

        firstIf.setItems(ifItems);

        rules.addCondition(firstIf);
        rules.setFallbackServer(new Server("xre://xre.poc0.xcal.tv:10065/failwhale"));

        String result = serializeIt(rules, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><selectServer><if><random><value>50</value></random><return><server><url>xre://xre.b3c9.ccp.xcal.tv:10004/shell</url></server></return></if><fallbackServer><url>xre://xre.poc0.xcal.tv:10065/failwhale</url></fallbackServer></selectServer>",
                result);
    }

    private String serializeIt(SelectServer rules, Boolean format) throws Exception {
        JAXBContext context = JAXBContext.newInstance(SelectServer.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);

        StringWriter result = new StringWriter();
        marshaller.marshal(rules, result);

        return result.toString();
    }
}
