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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.negativeintegrationtests;

import com.comcast.redirector.api.model.Contains;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.builders.IfExpressionBuilder;
import com.comcast.redirector.api.model.builders.NamespacedListBuilder;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.NamespacedListsHelper;
import com.comcast.redirector.api.redirector.helpers.RulesHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.comcast.redirector.api.model.factory.ExpressionFactory.newMultiValueExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NamespacedListsNegativeIntegrationTests extends BaseNegativeTests {

    @Test
    public void testNamespacedListNameIsInvalidError() {
        NamespacedListBuilder builder = new NamespacedListBuilder();

        NamespacedList namespacedList = builder
                .withName("Invalid#name")
                .withValues("v1_testNamespacedListNameIsInvalidError")
                .build();

        WebTarget webTarget = HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedList.getName());
        ValidationState error = ServiceHelper.post(webTarget, namespacedList, MediaType.APPLICATION_JSON, ValidationState.class);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.NamespacedListNameIsInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testNamespacedListValuesAreMissedError() {
        NamespacedListBuilder builder = new NamespacedListBuilder();

        NamespacedList namespacedList = builder.withName("testNamespacedListValuesAreMissedError").withValues("v1_ValuesAreMissed", "").build();

        WebTarget webTarget = HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedList.getName());
        ValidationState error = ServiceHelper.post(webTarget, namespacedList, MediaType.APPLICATION_JSON, ValidationState.class);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.NamespacedListContainsEmptyValue, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testNamespacedlistIsUsedInApprovedRuleError() throws InstantiationException, IllegalAccessException {
        NamespacedListBuilder namespacedListBuilder = new NamespacedListBuilder();

        // 1. create namespace which is used by unapproved rule
        String namespaceName = "NamespacedlistIsInUse1";
        NamespacedList namespacedList = namespacedListBuilder.withName(namespaceName).
                withValues("v1_NamespacedlistIsUsedInApprovedRule", "v2_NamespacedlistIsUsedInApprovedRule", "v3_NamespacedlistIsUsedInApprovedRule").build();
        WebTarget webTarget = HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedList.getName());
        ServiceHelper.post(webTarget, namespacedList, MediaType.APPLICATION_JSON, ValidationState.class);

        // 2. save but do not approve rule which uses created namespace
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("unapprovedRule1").
                withExpression(newMultiValueExpression(Contains.class, "param1", Arrays.asList(new Value(namespaceName)), "namespacedList")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class);
        RulesHelper.approvePendingChanges(HttpTestServerHelper.target(), SERVICE_NAME, expression.getId());

        // 3. try to delete namespace which is used by pending rule

        ValidationState error = ServiceHelper.delete(NamespacedListsHelper.getWebTarget_Delete(namespaceName), ValidationState.class);
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.NamespacedlistIsInUse, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testNamespacedlistIsUsedInUnapprovedRuleError() throws InstantiationException, IllegalAccessException {
        NamespacedListBuilder namespacedListBuilder = new NamespacedListBuilder();

        // 1. create namespace which is used by unapproved rule
        String namespaceName = "NamespacedlistIsInUse2";
        NamespacedList namespacedList = namespacedListBuilder.withName(namespaceName).withValues("v4_NamespacedlistIsUsedInUnapprovedRule", "v5_NamespacedlistIsUsedInUnapprovedRule", "v6_NamespacedlistIsUsedInUnapprovedRule").build();
        WebTarget webTarget = HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedList.getName());
        ServiceHelper.post(webTarget, namespacedList, MediaType.APPLICATION_JSON, ValidationState.class);

        // 2. save but do not approve rule which uses created namespace
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName("unapprovedRule2").
                withExpression(newMultiValueExpression(Contains.class, "param1", Arrays.asList(new Value(namespaceName)), "namespacedList")).
                withReturnStatement(newSimpleServerForFlavor("1.1")).build();


        // now trying to post rule with invalid name
        webTarget = HttpTestServerHelper.target().path(RULES_SERVICE_PATH).path(SERVICE_NAME).path(expression.getId());
        ServiceHelper.post(webTarget, expression, MediaType.APPLICATION_JSON, ValidationState.class);

        // 3. try to delete namespace which is used by pending rule

        ValidationState error = ServiceHelper.delete(NamespacedListsHelper.getWebTarget_Delete(namespaceName), ValidationState.class);
        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.NamespacedlistIsInUse, error.getErrors().keySet().iterator().next());
    }

}
