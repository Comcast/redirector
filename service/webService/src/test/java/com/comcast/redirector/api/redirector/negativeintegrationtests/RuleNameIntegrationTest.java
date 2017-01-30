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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.negativeintegrationtests;

import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.RulesHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.api.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import com.comcast.redirector.api.model.builders.IfExpressionBuilder;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.*;
import static com.comcast.redirector.api.model.factory.ServerFactory.newServerAdvanced;
import static com.comcast.redirector.api.model.factory.ServerFactory.newServerSimple;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.ServerGroupFactory.newServerGroup;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class RuleNameIntegrationTest extends BaseNegativeTests {

    private String invalidRuleName = "invalid1 #2rule3#4name%";

    @Test
    public void testInvalidRuleName() throws InstantiationException, IllegalAccessException {

        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName(invalidRuleName).
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "value2")).
                withExpression(newSingleParamExpression(NotEqual.class, "param3", "value3")).
                withExpression(newSingleParamExpression(LessThan.class, "param4", "4")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.InvalidRuleName, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testEmptyExpressionsError() throws InstantiationException, IllegalAccessException {

        // generating rule with empty expressions
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testEmptyExpressionsError").
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.EmptyExpressions, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testMissedParamNameError() throws InstantiationException, IllegalAccessException {

        // generating rule with empty param name
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testMissedParamNameError").
                withExpression(newSingleParamExpression(Equals.class, "", "value1")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "value2")).
                withExpression(newSingleParamExpression(NotEqual.class, null, "value3")).
                withExpression(newSingleParamExpression(LessThan.class, "param4", "4")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.MissedParamName, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testValueNumericTypeError() throws InstantiationException, IllegalAccessException {

        // generating rule with empty param name
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testValueNumericTypeError").
                withExpression(newSingleTypedParamExpression(LessThan.class, "numeric1", "22", Expressions.ValueType.NUMERIC)).
                withExpression(newSingleTypedParamExpression(LessOrEqualExpression.class, "invalidNumeric1", "str", Expressions.ValueType.NUMERIC)).
                withExpression(newSingleTypedParamExpression(GreaterThan.class, "numeric2", "22.5", Expressions.ValueType.NUMERIC)).
                withExpression(newSingleTypedParamExpression(GreaterOrEqualExpression.class, "invalidNumeric2", "random", Expressions.ValueType.NUMERIC)).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueNumericTypeError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testValueVersionTypeError() throws InstantiationException, IllegalAccessException {

        // generating rule with empty param name
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testValueVersionTypeError").
                withExpression(newSingleTypedParamExpression(GreaterOrEqualExpression.class, "ver1", "192.168.0.1", Expressions.ValueType.VERSION)).
                withExpression(newSingleTypedParamExpression(GreaterThan.class, "ver2", "Redirector-2.12", Expressions.ValueType.VERSION)).
                withExpression(newSingleTypedParamExpression(LessOrEqualExpression.class, "ver3", "5.55", Expressions.ValueType.VERSION)).
                withExpression(newSingleTypedParamExpression(LessThan.class, "invalidVersion", "192/168/0/1", Expressions.ValueType.VERSION)).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueVersionTypeError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testValueIPV6TypeError() throws InstantiationException, IllegalAccessException {

        // generating rule with empty param name
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testValueIPV6TypeError").
                withExpression(newSingleTypedParamExpression(GreaterOrEqualExpression.class, "IPv6_1_invalid", "192.168.0.1", Expressions.ValueType.IPV6)).
                withExpression(newSingleTypedParamExpression(GreaterThan.class, "IPv6_2", "FE80:0000:0000:0000:0202:B3FF:FE1E:8329", Expressions.ValueType.IPV6)).
                withExpression(newSingleTypedParamExpression(LessOrEqualExpression.class, "IPv6_3_invalid", ":XXX:", Expressions.ValueType.IPV6)).
                withExpression(newSingleTypedParamExpression(LessThan.class, "IPv6_4", "::", Expressions.ValueType.IPV6)).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueIPV6TypeError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testValueIPV6_4TypeError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newMultiValueExpression(InIpRange.class, "param1", Arrays.asList(new Value("192.168.0.1")))).
                withExpression(newMultiValueExpression(InIpRange.class, "param2", Arrays.asList(new Value("::0")))).
                withExpression(newMultiValueExpression(InIpRange.class, "IPv4_invalid", Arrays.asList(new Value("192.168.0_1")))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueIPV6_4TypeError, error.getErrors().keySet().iterator().next());

        // generating rule with invalid IPv6 format
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv6_error").
                withExpression(newMultiValueExpression(InIpRange.class, "param1", Arrays.asList(new Value("192.168.0.1")))).
                withExpression(newMultiValueExpression(InIpRange.class, "param2", Arrays.asList(new Value("::0")))).
                withExpression(newMultiValueExpression(InIpRange.class, "IPv6_invalid", Arrays.asList(new Value(":XXX:")))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueIPV6_4TypeError, error.getErrors().keySet().iterator().next());

        // generating rule with invalid IPv6 format
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_type_error").
                withExpression(newMultiValueExpression(InIpRange.class, "IPv4_invalid", Arrays.asList(new Value("google.com")))).
                withExpression(newMultiValueExpression(InIpRange.class, "IPv4_invalid", Arrays.asList(new Value("oracle.com")))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ValueIPV6_4TypeError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testPercentRangeError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testPercentRangeError").
                withExpression(new Percent(101)).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.PercentRangeError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testTemplateDependsOnTemplateError() throws InstantiationException, IllegalAccessException {

        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testTemplateDependsOnTemplateError").
                withTemplateName("template").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "value2")).
                withExpression(newSingleParamExpression(NotEqual.class, "param3", "value3")).
                withExpression(newSingleParamExpression(LessThan.class, "param4", "4")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(TEMPLATES_RULE_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(2, error.getErrors().size());
        Assert.assertTrue(error.getErrors().containsKey(ValidationState.ErrorType.TemplateDependsOnTemplate));
    }

    @Test
    public void testTemplateIsUsedByApprovedFlavorRuleError() throws InstantiationException, IllegalAccessException, InterruptedException {

        // create flavor template
        IfExpressionBuilder templateBuilder = new IfExpressionBuilder();
        IfExpression template = templateBuilder.
                withRuleName("flavorTemplate1").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerAdvanced("http://some")).
                build();

        // save and approve template
        WebTarget webTarget = HttpTestServerHelper.target().path(TEMPLATES_RULE_SERVICE_PATH).path(SERVICE_NAME).path(template.getId());
        ServiceHelper.post(webTarget, template, MediaType.APPLICATION_JSON);
        RulesHelper.approvePendingFlavorTemplateChanges(HttpTestServerHelper.target(), SERVICE_NAME, template.getId());

        // create flavor rule which will use created template
        IfExpressionBuilder ruleBuilder = new IfExpressionBuilder();
        IfExpression rule = ruleBuilder.
                withRuleName("testTemplateIsUsedByApprovedFlavorRuleError").
                withTemplateName(template.getId()).
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // save and approve rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(rule.getId());
        ServiceHelper.post(webTarget, rule, MediaType.APPLICATION_JSON, ValidationState.class);
        RulesHelper.approvePendingChanges(HttpTestServerHelper.target(), SERVICE_NAME, rule.getId());

        // now try to delete flavor template that is used by approved flavor rule
        webTarget = HttpTestServerHelper.target().path(TEMPLATES_RULE_SERVICE_PATH).path(SERVICE_NAME).path(template.getId());
        ValidationState error = ServiceHelper.delete(webTarget, ValidationState.class, true);

        // should get error ValidationState.ErrorType.TemplateIsUsed
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.TemplateIsUsed, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testTemplateIsUsedByUnApprovedFlavorRuleError() throws InstantiationException, IllegalAccessException, InterruptedException {

        // create flavor template
        IfExpressionBuilder templateBuilder = new IfExpressionBuilder();
        IfExpression template = templateBuilder.
                withRuleName("flavorTemplate2").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerAdvanced("http://some")).
                build();

        // save template and approve template
        WebTarget webTarget = HttpTestServerHelper.target().path(TEMPLATES_RULE_SERVICE_PATH).path(SERVICE_NAME).path(template.getId());
        ServiceHelper.post(webTarget, template, MediaType.APPLICATION_JSON);
        RulesHelper.approvePendingFlavorTemplateChanges(HttpTestServerHelper.target(), SERVICE_NAME, template.getId());

        // create flavor rule which will use created template
        IfExpressionBuilder ruleBuilder = new IfExpressionBuilder();
        IfExpression rule = ruleBuilder.
                withRuleName("testTemplateIsUsedByUnApprovedFlavorRuleError").
                withTemplateName(template.getId()).
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // save rule but do not approve it
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(rule.getId());
        ServiceHelper.post(webTarget, rule, MediaType.APPLICATION_JSON, ValidationState.class);
        //RulesHelper.approvePendingChanges(HttpTestServerHelper.target(), SERVICE_NAME, rule.getId());

        // now try to delete flavor template that is used by approved flavor rule
        webTarget = HttpTestServerHelper.target().path(TEMPLATES_RULE_SERVICE_PATH).path(SERVICE_NAME).path(template.getId());
        ValidationState error = ServiceHelper.delete(webTarget, ValidationState.class, true);

        // should get error ValidationState.ErrorType.TemplateIsUsed
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.TemplateIsUsed, error.getErrors().keySet().iterator().next());
    }

    //========================= URL RULES TESTS ======================================================================//

    @Test
    public void testAllReturnURLParamsMissedError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("tesAllReturnURLParamsMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newUrlParams(null, null, null, null)).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.AllReturnURLParamsMissed, error.getErrors().keySet().iterator().next());
    }


    @Test
    public void testDefaultURLParamsMissedError() throws InstantiationException, IllegalAccessException {

        UrlRule defaultUrlRule = new UrlRule();

        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(SERVICE_NAME).path("urlParams/default/");
        ValidationState error = ServiceHelper.post(webTarget, defaultUrlRule, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.UrlParamsHasEmptyValues, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testUrnIsInvalidError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testUrnIsInvalidError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newUrlParams("invalid# urn!", "xre", "8080", "4")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.UrnIsInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testProtocolIsInvalidError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testProtocolIsInvalidError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newUrlParams("validUrn", "invalidProtocol", "8080", "4")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ProtocolIsInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testPortIsInvalidError() throws InstantiationException, IllegalAccessException {

        // generating rule with invalid IPv4 format
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testPortIsInvalidError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newUrlParams("validUrn", "xre", "65555", "4")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(URL_RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.PortIsInvalid, error.getErrors().keySet().iterator().next());
    }

    //========================= DUPLICATES OF RELATIONAL EXPRESSIONS TESTS ===========================================//

    @Test
    public void testDuplicatedLessParamError() throws InstantiationException, IllegalAccessException {

        // generating rule with duplicated less expressions
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(LessThan.class, "param1", "5")). // this
                withExpression(newSingleParamExpression(Equals.class, "param2", "a")).
                withExpression(newSingleParamExpression(Matches.class, "param3", "some_pattern")).
                withExpression(newSingleParamExpression(LessThan.class, "param4", "6")).
                withExpression(newOrExpression(newSingleParamExpression(NotEqual.class, "param5", "6"),
                        newSingleParamExpression(LessThan.class, "param1", "6"))). // and this expressions are duplicates
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(LessThan.class, "param1", "5")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "some_pattern")).
                withExpression(newSingleParamExpression(LessThan.class, "param3", "6")).   // this
                withExpression(newXorExpression(newSingleParamExpression(LessThan.class, "param3", "6"), // and this expressions are duplicates
                newSingleParamExpression(NotEqual.class, "param4", "6"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newXorExpression(newSingleParamExpression(LessThan.class, "param1", "5"), newSingleParamExpression(LessThan.class, "param1", "5"))).
                withExpression(newOrExpression(newSingleParamExpression(LessThan.class, "param2", "5"), newSingleParamExpression(LessThan.class, "param2", "5"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessParam, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testDuplicatedGreaterParamError() throws InstantiationException, IllegalAccessException {

        // generating rule with duplicated less expressions
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(GreaterThan.class, "param1", "5")). // this
                withExpression(newSingleParamExpression(Equals.class, "param2", "a")).
                withExpression(newSingleParamExpression(Matches.class, "param3", "some_pattern")).
                withExpression(newSingleParamExpression(GreaterThan.class, "param4", "6")).
                withExpression(newOrExpression(newSingleParamExpression(NotEqual.class, "param5", "6"),
                        newSingleParamExpression(GreaterThan.class, "param1", "6"))). // and this expressions are duplicates
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(GreaterThan.class, "param1", "5")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "some_pattern")).
                withExpression(newSingleParamExpression(GreaterThan.class, "param3", "6")).   // this
                withExpression(newXorExpression(newSingleParamExpression(GreaterThan.class, "param3", "6"), // and this expressions are duplicates
                newSingleParamExpression(NotEqual.class, "param4", "6"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newXorExpression(newSingleParamExpression(GreaterThan.class, "param1", "5"), newSingleParamExpression(GreaterThan.class, "param1", "5"))).
                withExpression(newOrExpression(newSingleParamExpression(GreaterThan.class, "param2", "5"), newSingleParamExpression(GreaterThan.class, "param2", "5"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterParam, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testDuplicatedLessOrEqualParamError() throws InstantiationException, IllegalAccessException {

        // generating rule with duplicated less expressions
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(LessOrEqualExpression.class, "param1", "5")). // this
                withExpression(newSingleParamExpression(Equals.class, "param2", "a")).
                withExpression(newSingleParamExpression(Matches.class, "param3", "some_pattern")).
                withExpression(newSingleParamExpression(LessOrEqualExpression.class, "param4", "6")).
                withExpression(newOrExpression(newSingleParamExpression(NotEqual.class, "param5", "6"),
                        newSingleParamExpression(LessOrEqualExpression.class, "param1", "6"))). // and this expressions are duplicates
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessOrEqualParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newSingleParamExpression(LessOrEqualExpression.class, "param1", "5")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "some_pattern")).
                withExpression(newSingleParamExpression(LessOrEqualExpression.class, "param3", "6")).   // this
                withExpression(newXorExpression(newSingleParamExpression(LessOrEqualExpression.class, "param3", "6"), // and this expressions are duplicates
                newSingleParamExpression(NotEqual.class, "param4", "6"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessOrEqualParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testInIpRange_IPv4_error").
                withExpression(newXorExpression(newSingleParamExpression(LessOrEqualExpression.class, "param1", "5"), newSingleParamExpression(LessOrEqualExpression.class, "param1", "5"))).
                withExpression(newOrExpression(newSingleParamExpression(LessOrEqualExpression.class, "param2", "5"), newSingleParamExpression(LessOrEqualExpression.class, "param2", "5"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedLessOrEqualParam, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testDuplicatedGreaterOrEqualParamError() throws InstantiationException, IllegalAccessException {

        // generating rule with duplicated less expressions
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testDuplicatedGreaterOrEqualParamError").
                withExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param1", "5")). // this
                withExpression(newSingleParamExpression(Equals.class, "param2", "a")).
                withExpression(newSingleParamExpression(Matches.class, "param3", "some_pattern")).
                withExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param4", "6")).
                withExpression(newOrExpression(newSingleParamExpression(NotEqual.class, "param5", "6"),
                        newSingleParamExpression(GreaterOrEqualExpression.class, "param1", "6"))). // and this expressions are duplicates
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterOrEqualParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testDuplicatedGreaterOrEqualParamError").
                withExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param1", "5")).
                withExpression(newSingleParamExpression(Matches.class, "param2", "some_pattern")).
                withExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param3", "6")).   // this
                withExpression(newXorExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param3", "6"), // and this expressions are duplicates
                newSingleParamExpression(NotEqual.class, "param4", "6"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterOrEqualParam, error.getErrors().keySet().iterator().next());

        // generating rule with duplicated less expressions
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testDuplicatedGreaterOrEqualParamError").
                withExpression(newXorExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param1", "5"), newSingleParamExpression(GreaterOrEqualExpression.class, "param1", "5"))).
                withExpression(newOrExpression(newSingleParamExpression(GreaterOrEqualExpression.class, "param2", "5"), newSingleParamExpression(GreaterOrEqualExpression.class, "param2", "5"))).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();

        // now trying to post rule invalid rule
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DuplicatedGreaterOrEqualParam, error.getErrors().keySet().iterator().next());
    }

    //========================= SERVER TESTS =========================================================================//

    @Test
    public void testServerIsMissedError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerIsMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerIsMissed, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerUrlMissedError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerSimple("1.1", null)).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerUrlMissed, error.getErrors().keySet().iterator().next());

        
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerSimple("1.1", "")).build();

        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerUrlMissed, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerUrlInvalidError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerSimple("1.1", "invalid_url")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerUrlInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerPathMissedError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor(null)).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerPathMissed, error.getErrors().keySet().iterator().next());

        
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor("")).build();

        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerPathMissed, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testSimpleServerInvalidUrlError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testSimpleServerInvalidUrlError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerSimple("1.1", "{host}")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.SimpleServerInvalidUrl, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testInactivePathError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testInactivePathError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor("1.6")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.InactivePathError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testNonWhitelistedPathError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testNonWhitelistedPathError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newSimpleServerForFlavor("1.4")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.NonWhitelistedPathError, error.getErrors().keySet().iterator().next());
    }

    //========================= SERVER GROUP TESTS ===================================================================//

    @Test
    public void testServerGroupCountDownError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerGroupCountDownError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerGroup(Arrays.asList(newSimpleServerForFlavor("1.1"), newSimpleServerForFlavor("1.2")), "true", "-2")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerGroupCountDown, error.getErrors().keySet().iterator().next());

        
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerGroup(Arrays.asList(newSimpleServerForFlavor("1.1"), newSimpleServerForFlavor("1.2")), "true", "ten")).build();

        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerGroupCountDown, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerGroupEnablePrivateInvalidError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerGroupEnablePrivateInvalidError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerGroup(Arrays.asList(newSimpleServerForFlavor("1.1"), newSimpleServerForFlavor("1.2")), "invalidEnablePrivateValue", "33")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerGroupEnablePrivateInvalid, error.getErrors().keySet().iterator().next());

        
        builder = new IfExpressionBuilder();
        expression = builder.
                withRuleName("testServerUrlMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerGroup(Arrays.asList(newSimpleServerForFlavor("1.1"), newSimpleServerForFlavor("1.2")), "", "33")).build();

        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerGroupEnablePrivateInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerGroupEnablePrivateMissedError() throws InstantiationException, IllegalAccessException {
        
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("testServerGroupEnablePrivateMissedError").
                withExpression(newSingleParamExpression(Equals.class, "param1", "value1")).
                withReturnStatement(newServerGroup(Arrays.asList(newSimpleServerForFlavor("1.1"), newSimpleServerForFlavor("1.2")), null, "33")).build();

        // now trying to post rule with invalid name
        WebTarget webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ValidationState error = ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class, true);

        // should get error ValidationState.ErrorType.InvalidRuleName
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerGroupEnablePrivateMissed, error.getErrors().keySet().iterator().next());
    }


}
