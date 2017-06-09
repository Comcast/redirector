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

package com.comcast.redirector.api.decider;

import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.common.DeciderConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;


public class DeciderRulesControllerIntegrationTest {

    private static final String DECIDER_RULES_PATH = DeciderConstants.DECIDER_RULES_PATH;

    private static final IfExpression DECIDER_RULE;
    private static final String JSON_DECIDER_RULE;
    private static final String XML_DECIDER_RULE;

    private static final String JSON_DECIDER_RULE_TEMPLATE = "{\"id\":\"RULE_ID\",\"equals\":[{\"param\":\"PARAM_NAME\",\"value\":\"_VALUE_\"}],\"return\":{\"partner\":[\"PARTNER_NAME\"]}}";
    private static final String XML_DECIDER_RULE_TEMPLATE = "<if id=\"RULE_ID\"><equals><param>PARAM_NAME</param><value>_VALUE_</value></equals><return><partner>PARTNER_NAME</partner></return></if>";

    private static final String DEFAULT_DECIDER_RULE_ID = "DeciderRule1";
    private static final String DEFAULT_PARAM_NAME = "param1";
    private static final String DEFAULT_PARAM_VALUE = "value1";
    private static final String DEFAULT_PARTNER_NAME = "PartnerName1";

    static {
        DECIDER_RULE = createDefaultDeciderRule();
        // JSON_DECIDER_RULE and XML_DECIDER_RULE are the same as DECIDER_RULE object
        JSON_DECIDER_RULE = createDefaultJsonDeciderRule();
        XML_DECIDER_RULE = createDefaultXmlDeciderRule();
    }

    @After
    public void cleanUp() throws Exception {
        // clean up deciderRule data
        deleteAllRules();
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#addRule(String, IfExpression, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getRule(String, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddGetRule_JsonObject() {
        // post our deciderRule
        IfExpression responseEntityObject = ServiceHelper
                .post(getWebTarget_Post(DECIDER_RULE.getId()), DECIDER_RULE, MediaType.APPLICATION_JSON);
        // validate response
        Assert.assertEquals(DECIDER_RULE, responseEntityObject);

        // check get response again
        responseEntityObject = ServiceHelper
                .get(getWebTarget_Get(DECIDER_RULE.getId()), MediaType.APPLICATION_JSON, IfExpression.class);
        // validate response
        Assert.assertEquals(DECIDER_RULE, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#addRule(String, IfExpression, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getRule(String, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddGetRule_XmlObject() {
        // post our deciderRule
        IfExpression responseEntityObject = ServiceHelper
                .post(getWebTarget_Post(DECIDER_RULE.getId()), DECIDER_RULE, MediaType.APPLICATION_XML);

        // validate response
        Assert.assertEquals(DECIDER_RULE, responseEntityObject);

        // check get response again
        responseEntityObject = ServiceHelper
                .get(getWebTarget_Get(DECIDER_RULE.getId()), MediaType.APPLICATION_XML, IfExpression.class);
        // validate response
        Assert.assertEquals(DECIDER_RULE, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#addRule(String, IfExpression, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getRule(String, javax.ws.rs.core.UriInfo)}
     * MediaType - JSON string format to check corrected structure
     */
    @Test
    public void testAddGetRule_JsonString() {
        // post our deciderRule
        String responseEntityObject = ServiceHelper
                .post(getWebTarget_Post(DECIDER_RULE.getId()), JSON_DECIDER_RULE, MediaType.APPLICATION_JSON);
        // validate response
        Assert.assertEquals(JSON_DECIDER_RULE, responseEntityObject);

        // check get response again
        ServiceHelper.get(getWebTarget_Get(DECIDER_RULE.getId()), MediaType.APPLICATION_JSON, String.class);
        // validate response
        Assert.assertEquals(JSON_DECIDER_RULE, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#addRule(String, IfExpression, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getRule(String, javax.ws.rs.core.UriInfo)}
     * MediaType - XML string format to check corrected structure
     */
    @Test
    public void testAddGetRule_XmlString() {
        // post our deciderRule
        String responseEntityObject = ServiceHelper
                .post(getWebTarget_Post(DECIDER_RULE.getId()), XML_DECIDER_RULE, MediaType.APPLICATION_XML);

        // validate response
        Assert.assertThat(responseEntityObject, containsString(XML_DECIDER_RULE));

        // check get response again
        ServiceHelper.get(getWebTarget_Get(DECIDER_RULE.getId()), MediaType.APPLICATION_XML, String.class);
        // validate response
        Assert.assertThat(responseEntityObject, containsString(XML_DECIDER_RULE));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getAllRules(javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testGetAllRules_JsonObject() {
        // post our deciderRule
        ServiceHelper.post(getWebTarget_Post(DECIDER_RULE.getId()), DECIDER_RULE, MediaType.APPLICATION_JSON);

        // post other deciderRule
        IfExpression otherDeciderRule = createDeciderRule("DeciderRule2", "param2", "value2", "PartnerName2");
        ServiceHelper.post(getWebTarget_Post(otherDeciderRule.getId()), otherDeciderRule, MediaType.APPLICATION_JSON);

        // check get response
        SelectServer responseEntityObject = ServiceHelper
                .get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, SelectServer.class);

        // validate response
        Assert.assertEquals(2, responseEntityObject.getItems().size());
        IfExpression[] expectedItems = new IfExpression[]{DECIDER_RULE, otherDeciderRule};
        Assert.assertThat(responseEntityObject.getItems(), hasItems(expectedItems));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getAllRules(javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testGetAllRules_XmlObject() {
        // post our deciderRule
        ServiceHelper.post(getWebTarget_Post(DECIDER_RULE.getId()), DECIDER_RULE, MediaType.APPLICATION_XML);

        // post other deciderRule
        IfExpression otherDeciderRule = createDeciderRule("DeciderRule2", "param2", "value2", "PartnerName2");
        ServiceHelper.post(getWebTarget_Post(otherDeciderRule.getId()), otherDeciderRule, MediaType.APPLICATION_XML);

        // check get response
        SelectServer responseEntityObject = ServiceHelper
                .get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, SelectServer.class);
        // validate response
        Assert.assertEquals(2, responseEntityObject.getItems().size());
        IfExpression[] expectedItems = new IfExpression[]{DECIDER_RULE, otherDeciderRule};
        Assert.assertThat(responseEntityObject.getItems(), hasItems(expectedItems));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getAllRules(javax.ws.rs.core.UriInfo)}
     * MediaType - JSON string format to check corrected structure
     */
    @Test
    public void testGetAllRules_JsonString() {
        final String JSON_DECIDER_Rule_2 = createJsonDeciderRule("DeciderRule2", "param2", "value2", "PartnerName2");
        final String JSON_Rules = "{\"if\":[" + JSON_DECIDER_RULE + "," + JSON_DECIDER_Rule_2 + "]}";

        // post our deciderRule
        ServiceHelper.post(getWebTarget_Post(DECIDER_RULE.getId()), JSON_DECIDER_RULE, MediaType.APPLICATION_JSON);

        // post other deciderRule
        ServiceHelper.post(getWebTarget_Post("DeciderRule2"), JSON_DECIDER_Rule_2, MediaType.APPLICATION_JSON);

        // check get response
        String responseEntityObject = ServiceHelper
                .get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, String.class);
        // validate response
        Assert.assertEquals(JSON_Rules, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#getAllRules(javax.ws.rs.core.UriInfo)}
     * MediaType - XML string format to check corrected structure
     */
    @Test
    public void testGetAllRules_XmlString() {
        final String XML_DECIDER_Rule_2 = createXmlDeciderRule("DeciderRule2", "param2", "value2", "PartnerName2");
        final String XML_Rules = "<selectServer>" + XML_DECIDER_RULE + XML_DECIDER_Rule_2 + "</selectServer>";

        // post our deciderRule
        ServiceHelper.post(getWebTarget_Post(DECIDER_RULE.getId()), XML_DECIDER_RULE, MediaType.APPLICATION_XML);

        // post other deciderRule
        ServiceHelper.post(getWebTarget_Post("DeciderRule2"), XML_DECIDER_Rule_2, MediaType.APPLICATION_XML);

        // check get response
        String responseEntityObject = ServiceHelper
                .get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, String.class);
        // validate response
        Assert.assertThat(responseEntityObject, containsString(XML_Rules));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.DeciderRulesController#DeleteOne(String)}
     */
    @Test
    public void testDeleteOne() {
        // post our deciderRule
        ServiceHelper.post(getWebTarget_Post(DECIDER_RULE.getId()), DECIDER_RULE, MediaType.APPLICATION_JSON);

        // delete our deciderRule
        ServiceHelper.delete(getWebTarget_Delete(DECIDER_RULE.getId()));

        // check get response
        IfExpression responseEntityObject = ServiceHelper
                .get(getWebTarget_Get(DECIDER_RULE.getId()), MediaType.APPLICATION_JSON, IfExpression.class);
        // validate response
        Assert.assertNull(responseEntityObject);
    }

    private void deleteAllRules() {
        SelectServer rules = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, SelectServer.class);

        if (rules.getItems() != null && !rules.getItems().isEmpty()) {
            for (IfExpression rule : rules.getItems()) {
                ServiceHelper.delete(getWebTarget_Delete(rule.getId()));
            }
        }
    }

    private static IfExpression createDefaultDeciderRule() {
        return createDeciderRule(DEFAULT_DECIDER_RULE_ID, DEFAULT_PARAM_NAME, DEFAULT_PARAM_VALUE, DEFAULT_PARTNER_NAME);
    }

    private static IfExpression createDeciderRule(String ruleId, final String param, final String value, String partnerName) {
        IfExpression ifExpression = new IfExpression();
        ifExpression.setId(ruleId);
        List<Expressions> expressions = new ArrayList<Expressions>(){{add(new Equals(param, value));}};
        ifExpression.setItems(expressions);
        ifExpression.setReturn(new Value(partnerName));
        return ifExpression;
    }

    private static String createDefaultJsonDeciderRule() {
        return createJsonDeciderRule(DEFAULT_DECIDER_RULE_ID, DEFAULT_PARAM_NAME, DEFAULT_PARAM_VALUE, DEFAULT_PARTNER_NAME);
    }

    private static String createJsonDeciderRule(String ruleId, String paramName, String value, String partnerName) {
        return JSON_DECIDER_RULE_TEMPLATE
                .replace("RULE_ID", ruleId)
                .replace("PARAM_NAME", paramName)
                .replace("_VALUE_", value)
                .replace("PARTNER_NAME", partnerName);
    }

    private static String createDefaultXmlDeciderRule() {
        return createXmlDeciderRule(DEFAULT_DECIDER_RULE_ID, DEFAULT_PARAM_NAME, DEFAULT_PARAM_VALUE, DEFAULT_PARTNER_NAME);
    }

    private static String createXmlDeciderRule(String ruleId, String paramName, String value, String partnerName) {
        return XML_DECIDER_RULE_TEMPLATE
                .replace("RULE_ID", ruleId)
                .replace("PARAM_NAME", paramName)
                .replace("_VALUE_", value)
                .replace("PARTNER_NAME", partnerName);
    }

    private WebTarget getWebTarget_Post(String ruleId) {
        return HttpTestServerHelper.target().path(DECIDER_RULES_PATH).path(ruleId);
    }

    private WebTarget getWebTarget_Get(String ruleId) {
        return HttpTestServerHelper.target().path(DECIDER_RULES_PATH).path(ruleId);
    }

    private WebTarget getWebTarget_GetAll() {
        return HttpTestServerHelper.target().path(DECIDER_RULES_PATH);
    }

    private WebTarget getWebTarget_Delete(String ruleId) {
        return HttpTestServerHelper.target().path(DECIDER_RULES_PATH).path(ruleId);
    }
}

