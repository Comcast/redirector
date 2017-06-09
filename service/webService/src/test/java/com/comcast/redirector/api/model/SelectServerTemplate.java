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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model;

import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.XMLSerializer;
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;

import javax.ws.rs.ext.MessageBodyReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SelectServerTemplate {

    private Serializer xmlSerializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());

    protected static final String DEFAULT_RULE_TEST_NAME = "Rule_test";

    // JSON BASE TEST DATA
    protected static final String JSON_SELECT_SERVER_TEMPLATE = "{\"if\":[{\"id\":\"SELECT_SERVER_NAME\",EXPRESSION,RETURN_SERVER}]}";
    protected static final String DEFAULT_JSON_EXPRESSION = "\"equals\":[{\"param\":\"default_param\",\"value\":\"default_value\"}]";
    protected static final String DEFAULT_JSON_RETURN_SERVER = "\"return\":{\"server\":[{\"isNonWhitelisted\":\"false\",\"name\":\"def_server\",\"url\":\"def_url\",\"path\":\"def_path\",\"description\":\"def_description\"}]}";
    protected static final String DEFAULT_JSON_SELECT_SERVER;
    // ==================

    // XML BASE TEST DATA
    protected static final String XML_SELECT_SERVER_TEMPLATE = "<selectServer><if id=\"SELECT_SERVER_NAME\"><EXPRESSION><RETURN_SERVER></if></selectServer>";
    protected static final String DEFAULT_XML_EXPRESSION = "<equals><param>default_param</param><value>default_value</value></equals>";
    protected static final String DEFAULT_XML_RETURN_SERVER = "<return><server isNonWhitelisted=\"false\"><name>def_server</name><url>def_url</url><path>def_path</path><description>def_description</description></server></return>";
    protected static final String DEFAULT_XML_SELECT_SERVER;
    // ==================

    protected static final Expressions DEFAULT_EXPRESSION_OBJECT;
    protected static final Server DEFAULT_RETURN_SERVER_OBJECT;
    protected static final SelectServer DEFAULT_SELECT_SERVER_OBJECT;

    static {
        DEFAULT_JSON_SELECT_SERVER = createJsonSelectServer(DEFAULT_RULE_TEST_NAME, DEFAULT_JSON_EXPRESSION, DEFAULT_JSON_RETURN_SERVER);
        DEFAULT_XML_SELECT_SERVER = createXmlSelectServer(DEFAULT_RULE_TEST_NAME, DEFAULT_XML_EXPRESSION, DEFAULT_XML_RETURN_SERVER);

        DEFAULT_EXPRESSION_OBJECT = new Equals();
        ((Equals) DEFAULT_EXPRESSION_OBJECT).setParam("default_param");
        ((Equals) DEFAULT_EXPRESSION_OBJECT).setValue("default_value");

        DEFAULT_RETURN_SERVER_OBJECT = new Server();
        DEFAULT_RETURN_SERVER_OBJECT.setName("def_server");
        DEFAULT_RETURN_SERVER_OBJECT.setPath("def_path");
        DEFAULT_RETURN_SERVER_OBJECT.setUrl("def_url");
        DEFAULT_RETURN_SERVER_OBJECT.setIsNonWhitelisted("false");
        DEFAULT_RETURN_SERVER_OBJECT.setDescription("def_description");

        DEFAULT_SELECT_SERVER_OBJECT = createDefaultSelectServerObject();
    }

    protected static String createJsonSelectServer(String selectServerName, String expression, String returnServer) {
        return JSON_SELECT_SERVER_TEMPLATE
                .replace("SELECT_SERVER_NAME", selectServerName)
                .replace("EXPRESSION", expression)
                .replace("RETURN_SERVER", returnServer);
    }

    protected static String createXmlSelectServer(String selectServerName, String expression, String returnServer) {
        return XML_SELECT_SERVER_TEMPLATE
                .replace("SELECT_SERVER_NAME", selectServerName)
                .replace("<EXPRESSION>", expression)
                .replace("<RETURN_SERVER>", returnServer);
    }

    protected static SelectServer createDefaultSelectServerObject() {
        List<IfExpression> rules = new ArrayList<IfExpression>() {{
            add(createRule(DEFAULT_RULE_TEST_NAME, new ArrayList<Expressions>(){{add(DEFAULT_EXPRESSION_OBJECT);}}));
        }};

        return createSelectServer(rules, DEFAULT_RETURN_SERVER_OBJECT);
    }

    protected static IfExpression createRule(String ruleName, List<Expressions> expressions) {
        IfExpression rule = new IfExpression();
        rule.setId(ruleName);
        rule.setItems(expressions);
        return rule;
    }

    protected static SelectServer createSelectServer(List<IfExpression> rules, Expressions returnServer) {
        // add return server to the last rule
        IfExpression lastRule = rules.get(rules.size() - 1);
        lastRule.setReturn(returnServer);
        SelectServer defaultSelectServer= new SelectServer();
        defaultSelectServer.setItems(rules);
        return defaultSelectServer;
    }

    @SuppressWarnings("unchecked")
    protected  <T> MessageBodyReader<T> getJsonMessageBodyReader() {
        return  (MessageBodyReader<T>) new MOXyJsonProvider();
    }

    protected SelectServer unmarshalJsonStringObject(String jsonStringObject) throws java.io.IOException {
        MessageBodyReader<SelectServer> messageBodyReader = getJsonMessageBodyReader();
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonStringObject.getBytes());
        SelectServer responseObject = messageBodyReader.readFrom(SelectServer.class, SelectServer.class, SelectServer.class.getAnnotations(), null, null, bais);
        bais.close();
        return responseObject;
    }

    protected String marshalJsonObject(SelectServer object) throws java.io.IOException {
        MOXyJsonProvider moxyJsonProvider = new MOXyJsonProvider();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        moxyJsonProvider.writeTo(object, SelectServer.class, SelectServer.class, SelectServer.class.getAnnotations(), null, null, baos);
        String jsonStringObject = new String(baos.toByteArray());
        baos.close();
        return jsonStringObject;
    }

    protected SelectServer unmarshalXmlStringObject(String xmlStringObject) throws SerializerException {
        return xmlSerializer.deserialize(xmlStringObject, SelectServer.class);
    }

    protected String marshalXmlObject(SelectServer object) throws SerializerException {
        return xmlSerializer.serialize(object, false);
    }

}
