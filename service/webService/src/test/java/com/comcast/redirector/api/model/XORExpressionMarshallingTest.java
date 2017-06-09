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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class XORExpressionMarshallingTest extends SelectServerTemplate {

    private static final String TEST_JSON_OR_PARAM_EXP_1 = "\"equals\":[{\"param\":\"equals_param\",\"value\":\"equals_value\"}]";
    private static final String TEST_XML_OR_PARAM_EXP_1 = "<equals><param>equals_param</param><value>equals_value</value></equals>";

    private static final String TEST_JSON_OR_PARAM_EXP_2 = "\"notEqual\":[{\"param\":\"notEqual_param\",\"value\":\"notEqual_value\"}]";
    private static final String TEST_XML_OR_PARAM_EXP_2 = "<notEqual><param>notEqual_param</param><value>notEqual_value</value></notEqual>";

    private static final String TEST_JSON_EXPRESSION = "\"xor\":[{" + TEST_JSON_OR_PARAM_EXP_1 + "," + TEST_JSON_OR_PARAM_EXP_2 + "}]";
    private static final String TEST_XML_EXPRESSION = "<xor>" + TEST_XML_OR_PARAM_EXP_1 + TEST_XML_OR_PARAM_EXP_2 + "</xor>";

    private static String TEST_SELECT_SERVER_JSON;
    private static String TEST_SELECT_SERVER_XML;
    private static SelectServer TEST_SELECT_SERVER_OBJECT;

    @BeforeClass
    public static void setUp() {
        TEST_SELECT_SERVER_JSON = createJsonSelectServer(DEFAULT_RULE_TEST_NAME, TEST_JSON_EXPRESSION, DEFAULT_JSON_RETURN_SERVER);
        TEST_SELECT_SERVER_XML = createXmlSelectServer(DEFAULT_RULE_TEST_NAME, TEST_XML_EXPRESSION, DEFAULT_XML_RETURN_SERVER);

        final XORExpression testExpression = new XORExpression();
        final Equals testExpression_OR_1 = new Equals();
        testExpression_OR_1.setParam("equals_param");
        testExpression_OR_1.setValue("equals_value");
        final NotEqual testExpression_OR_2 = new NotEqual();
        testExpression_OR_2.setParam("notEqual_param");
        testExpression_OR_2.setValue("notEqual_value");
        testExpression.setItems(new ArrayList<Expressions>(){{add(testExpression_OR_1);add(testExpression_OR_2);}});
        List<IfExpression> rules = new ArrayList<IfExpression>() {{
            add(createRule(DEFAULT_RULE_TEST_NAME, new ArrayList<Expressions>(){{add(testExpression);}}));
        }};
        TEST_SELECT_SERVER_OBJECT = createSelectServer(rules, DEFAULT_RETURN_SERVER_OBJECT);
    }

    @Test
    public void testJsonToXORObject() throws Exception {
        SelectServer responseObject = unmarshalJsonStringObject(TEST_SELECT_SERVER_JSON);
        // validate responseObject
        Assert.assertEquals(TEST_SELECT_SERVER_OBJECT, responseObject);
    }

    @Test
    public void testXORObjectToJson() throws Exception {
        String response = marshalJsonObject(TEST_SELECT_SERVER_OBJECT);
        // validate response
        Assert.assertEquals(TEST_SELECT_SERVER_JSON, response);
    }

    @Test
    public void testXmlToXORObject() throws Exception {
        SelectServer responseObject = unmarshalXmlStringObject(TEST_SELECT_SERVER_XML);
        // validate responseObject
        Assert.assertEquals(TEST_SELECT_SERVER_OBJECT, responseObject);
    }

    @Test
    public void testXORObjectToXml() throws Exception {
        String response = marshalXmlObject(TEST_SELECT_SERVER_OBJECT);
        // validate response
        validateXlStringResponse(TEST_SELECT_SERVER_XML, response);
    }

    private void validateXlStringResponse(String expectedXmlString, String response) {
        Assert.assertEquals(expectedXmlString, response.substring(response.indexOf("<selectServer>"), response.length()));
    }

}
