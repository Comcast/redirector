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

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;

public class RedirectorTestSuiteControllerIntegrationTest {
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }

    @Test
    public void testAddAndGetTestCase() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        new TestCaseSteps(serviceName, serviceName + "_TestCase").createTestCase().post()
                .verifyPost().get().verifyGet().delete().verifyDelete();
    }

    @Test
    public void testGetAllDeleteMultipleTestCase() throws Exception {
        TestCaseSteps testCaseSteps[] = new TestCaseSteps[3];
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        for (int i = 0; i < 3; i++) {
            testCaseSteps[i] = new TestCaseSteps(serviceName, serviceName + "_" + i).createTestCase().post().verifyPost();
        }

        for (int i = 0; i < 3; i++) {
            testCaseSteps[i].getAll().verifyGetAll().verifyServicePresent().delete().verifyDelete();
        }
    }

    private class TestCaseSteps {
        String serviceName;
        String testCaseName;
        RedirectorTestCase testCase;
        Response responsePost;
        Response responseGet;
        Response responseGetAll;
        Response responseGetAfterDelete;

        TestCaseSteps(String serviceName, String testCaseName) {
            this.serviceName = serviceName;
            this.testCaseName = testCaseName;
        }

        TestCaseSteps createTestCase() {
            testCase = RedirectorTestSuiteControllerIntegrationTest.createTestCase(testCaseName, serviceName);
            return this;
        }

        TestCaseSteps post() {
            responsePost = apiFacade.postTestCase(testCase, serviceName);
            return this;
        }

        TestCaseSteps verifyPost() {
            Assert.assertEquals(HttpURLConnection.HTTP_CREATED, responsePost.getStatus());
            return this;
        }

        TestCaseSteps get() {
            responseGet = apiFacade.getTestCase(testCaseName, serviceName);
            return this;
        }

        TestCaseSteps getAll() {
            responseGetAll = apiFacade.getTestCase(serviceName);
            return this;
        }

        TestCaseSteps verifyGet() throws SerializerException {
            Assert.assertEquals(HttpURLConnection.HTTP_OK, responseGet.getStatus());
            return this;
        }

        TestCaseSteps verifyGetAll() throws SerializerException {
            Assert.assertEquals(HttpURLConnection.HTTP_OK, responseGetAll.getStatus());
            return this;
        }

        TestCaseSteps verifyServicePresent() throws SerializerException {
            Map<String, RedirectorTestCase> testCases = getServiceRedirectorTestCase(responseGetAll);
            Assert.assertTrue(testCases.containsKey(testCaseName));
            return this;
        }

        TestCaseSteps delete() {
            apiFacade.delete(testCaseName, serviceName);
            return this;
        }

        TestCaseSteps verifyDelete() {
            responseGetAfterDelete = apiFacade.getTestCase(testCaseName, serviceName);
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseGetAfterDelete.getStatus());
            return this;
        }
    }

    private Map<String, RedirectorTestCase> getServiceRedirectorTestCase(Response response) throws SerializerException {
        RedirectorTestCaseList list = apiFacade.getRedirectorTestCaseList(response);
        return list.getRedirectorTestCases().stream().collect(Collectors.toMap(RedirectorTestCase::getName, Function.identity()));
    }

    private static RedirectorTestCase createTestCase(String name, String serviceName) {
        RedirectorTestCase testCase = new RedirectorTestCase();
        testCase.setName(name);
        testCase.setApplication(serviceName);
        return testCase;
    }
}
