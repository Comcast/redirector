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
 */
package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.utils.TestHelper;
import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.api.model.testsuite.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.SerializerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class ReportManagerTest {

    private static final String appName = "APPNAME";
    private static final String ID_1 = "SESSOIN_ID_1";
    private static final String ID_2 = "SESSOIN_ID_2";
    private static final String EVENT_MESSAGES_1 = "Event messages 1";
    private static final String EVENT_MESSAGES_2 = "Event messages 2";
    private static final String basePath = File.separator + "BASEPATH";
    private static final String reportPath = basePath + File.separator + appName + File.separator + "report.json";
    private static final String PARAMETER_NAME_1 = "Parameter_Name_1";
    private static final String PARAMETER_NAME_2 = "Parameter_Name_2";
    private static final String PARAMETER_NAME_2_1 = "Parameter_Name_2_1";

    private static final String PROTOCOL_1 = "PROTOCOL_1";
    private static final String PORT_1 = "PORT_1";
    private static final String IP_VERSION_1 = "IP_VERSION_1";
    private static final String URN_1 = "URN_1";
    private static final String XRE_STACK_1 = "XRE_STACK_1";
    private static final String FLAVOR_1 = "FLAVOR_1";
    private static final String RULE_1 = "RULE_1";
    private static final String RESPONSE_TYPE_1 = "RESPONSE_TYPE_1";

    private static final String PROTOCOL_2 = "PROTOCOL_2";
    private static final String PORT_2 = "PORT_2";
    private static final String IP_VERSION_2 = "IP_VERSION_2";
    private static final String URN_2 = "URN_2";
    private static final String XRE_STACK_2 = "XRE_STACK_2";
    private static final String FLAVOR_2 = "FLAVOR_2";
    private static final String RULE_2 = "RULE_2";
    private static final String RESPONSE_TYPE_2 = "RESPONSE_TYPE_2";

    @Mock
    private FileUtil fileUtil;

    @Mock
    private FilesPathHelper filesPathHelper;

    private RedirectorTestCaseList testCases;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private Response response;

    @Mock
    private Invocation.Builder builder;

    private ArgumentCaptor<TestCaseResultList> report;

    private TestSuiteResponse testSuiteResponse_1;
    private TestSuiteResponse testSuiteResponse_2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(serviceHelper.getRequestBuilder(ReportManager.END_TO_END_REPORT_ENDPOINT, MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(response.getStatus()).thenReturn(HttpURLConnection.HTTP_OK);

        when(builder.get()).thenReturn(response);
        when(builder.delete()).thenReturn(response);

        RedirectorTestCase testCase1 = TestHelper.createRedirectorTestCase(ID_1, appName,
                TestHelper.createTestSuiteResponse(PROTOCOL_1, PORT_1, IP_VERSION_1, URN_1, XRE_STACK_1, FLAVOR_1, RULE_1, RESPONSE_TYPE_1),
                TestHelper.createParameter(PARAMETER_NAME_1, "rule_1_1"));

        RedirectorTestCase testCase2 = TestHelper.createRedirectorTestCase(ID_2, appName,
                TestHelper.createTestSuiteResponse(PROTOCOL_2, PORT_2, IP_VERSION_2, URN_2, XRE_STACK_2, FLAVOR_2, RULE_2, RESPONSE_TYPE_2),
                TestHelper.createParameter(PARAMETER_NAME_2, "rule_2_1"), TestHelper.createParameter(PARAMETER_NAME_2_1, "rule_2_2"));

        List<RedirectorTestCase> redirectorTestCaseList = new ArrayList<>();
        redirectorTestCaseList.add(testCase1);
        redirectorTestCaseList.add(testCase2);

        testCases = new RedirectorTestCaseList();
        testCases.setRedirectorTestCases(redirectorTestCaseList);

        filesPathHelper = new FilesPathHelper(basePath);

        testSuiteResponse_1 = TestHelper.createTestSuiteResponse(PROTOCOL_1, PORT_1, IP_VERSION_1, URN_1, XRE_STACK_1, FLAVOR_1, RULE_1, RESPONSE_TYPE_1);
        testSuiteResponse_2 = TestHelper.createTestSuiteResponse(PROTOCOL_2, PORT_2, IP_VERSION_2, URN_2, XRE_STACK_2, FLAVOR_2, RULE_2, RESPONSE_TYPE_2);
        Session session_1 = TestHelper.createSession(ID_1, testSuiteResponse_1);
        session_1.setEvents(Collections.singletonList(new Event(EVENT_MESSAGES_1)));
        Session session_2 = TestHelper.createSession(ID_2, testSuiteResponse_2);
        session_2.setEvents(Collections.singletonList(new Event(EVENT_MESSAGES_2)));

        List<Session> sessionArrayList = new ArrayList<>();
        sessionArrayList.add(session_1);
        sessionArrayList.add(session_2);

        SessionList session = new SessionList();
        session.setSessions(sessionArrayList);
        when(response.readEntity(SessionList.class)).thenReturn(session);

        report = ArgumentCaptor.forClass(TestCaseResultList.class);
    }

    @Test
    public void verifyReportTest() throws IOException, SerializerException {
        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.createReport(testCases);
        reportManager.clearSessionLog();

        verify(fileUtil, times(1)).writeJson(eq(reportPath), report.capture());
        verify(builder, times(1)).delete();

        List<TestCaseResult> caseResults = (List<TestCaseResult>) report.getValue().getItems();

        Assert.assertEquals(2, caseResults.size());
        Assert.assertEquals(ID_1, caseResults.get(0).getTestCase().getName());
        Assert.assertEquals(ID_2, caseResults.get(1).getTestCase().getName());

    }

    @Test
    public void verifyReportStatus() throws IOException, SerializerException {
        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.createReport(testCases);

        verify(fileUtil, times(1)).writeJson(eq(reportPath), report.capture());

        List<TestCaseResult> caseResults = (List<TestCaseResult>) report.getValue().getItems();

        Assert.assertEquals(2, caseResults.size());
        Assert.assertEquals(TestCaseResult.Status.PASSED, caseResults.get(0).getStatus());
        Assert.assertEquals(TestCaseResult.Status.PASSED, caseResults.get(1).getStatus());
    }

    @Test
    public void verifyLogs() throws IOException, SerializerException {
        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.createReport(testCases);

        verify(fileUtil, times(1)).writeJson(eq(reportPath), report.capture());

        List<TestCaseResult> caseResults = (List<TestCaseResult>) report.getValue().getItems();

        Assert.assertEquals(2, caseResults.size());
        Assert.assertEquals(1, caseResults.get(0).getLogs().size());
        Assert.assertEquals(EVENT_MESSAGES_1, caseResults.get(0).getLogs().get(0));
        Assert.assertEquals(1, caseResults.get(1).getLogs().size());
        Assert.assertEquals(EVENT_MESSAGES_2, caseResults.get(1).getLogs().get(0));

    }

    @Test
    public void verifyTestResolt() throws IOException, SerializerException {
        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.createReport(testCases);

        verify(fileUtil, times(1)).writeJson(eq(reportPath), report.capture());

        List<TestCaseResult> caseResults = (List<TestCaseResult>) report.getValue().getItems();

        verifyRedirectorTestCaseResponce_1(caseResults);
        verifyRedirectorTestCaseResponce_2(caseResults);

    }

    @Test
    public void reportActualResultTest() throws IOException, SerializerException {

        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.createReport(testCases);

        verify(fileUtil, times(1)).writeJson(eq(reportPath), report.capture());

        List<TestCaseResult> caseResults = (List<TestCaseResult>) report.getValue().getItems();
        TestSuiteResponse testCaseResult_1 = caseResults.get(0).getActual();
        TestSuiteResponse testCaseResult_2 = caseResults.get(1).getActual();

        verifyTestCaseActual(PROTOCOL_1, testCaseResult_1.getProtocol(),
                PORT_1, testCaseResult_1.getPort(),
                IP_VERSION_1, testCaseResult_1.getIpVersion(),
                URN_1, testCaseResult_1.getUrn(),
                XRE_STACK_1, testCaseResult_1.getXreStack(),
                FLAVOR_1, testCaseResult_1.getFlavor(),
                RULE_1, testCaseResult_1.getRule(),
                RESPONSE_TYPE_1, testCaseResult_1.getResponseType());

        verifyTestCaseActual(PROTOCOL_2, testCaseResult_2.getProtocol(),
                PORT_2, testCaseResult_2.getPort(),
                IP_VERSION_2, testCaseResult_2.getIpVersion(),
                URN_2, testCaseResult_2.getUrn(),
                XRE_STACK_2, testCaseResult_2.getXreStack(),
                FLAVOR_2, testCaseResult_2.getFlavor(),
                RULE_2, testCaseResult_2.getRule(),
                RESPONSE_TYPE_2, testCaseResult_2.getResponseType());
    }

    @Test
    public void verifyClearSessionLogTest() throws IOException, SerializerException {

        when(response.getStatus()).thenReturn(HttpURLConnection.HTTP_NO_CONTENT);
        when(builder.delete()).thenReturn(response);

        ReportManager reportManager = new ReportManager(appName, fileUtil, filesPathHelper, serviceHelper);
        reportManager.clearSessionLog();

        verify(builder, times(1)).delete();
        verify(response, times(1)).getStatus();
    }

    private void verifyTestCaseActual(String protocol1, String protocol, String port1, String port, String ipVersion1, String ipVersion, String urn1, String urn, String xreStack1, String xreStack, String flavor1, String flavor, String rule1, String rule, String responseType1, String responseType) {
        Assert.assertEquals(protocol1, protocol);
        Assert.assertEquals(port1, port);
        Assert.assertEquals(ipVersion1, ipVersion);
        Assert.assertEquals(urn1, urn);
        Assert.assertEquals(xreStack1, xreStack);
        Assert.assertEquals(flavor1, flavor);
        Assert.assertEquals(rule1, rule);
        Assert.assertEquals(responseType1, responseType);
    }

    private void verifyRedirectorTestCaseResponce_2(List<TestCaseResult> caseResults) {
        RedirectorTestCase testCase_2 = caseResults.get(1).getTestCase();
        Assert.assertEquals(appName, testCase_2.getApplication());
        Assert.assertEquals(2, testCase_2.getParameters().size());
        Assert.assertEquals(PARAMETER_NAME_2, testCase_2.getParameters().get(0).getName());
        Assert.assertEquals(ID_2, testCase_2.getName());
        Assert.assertEquals(testSuiteResponse_2, testCase_2.getExpected());
    }

    private void verifyRedirectorTestCaseResponce_1(List<TestCaseResult> caseResults) {
        RedirectorTestCase testCase_1 = caseResults.get(0).getTestCase();
        Assert.assertEquals(appName, testCase_1.getApplication());
        Assert.assertEquals(1, testCase_1.getParameters().size());
        Assert.assertEquals(PARAMETER_NAME_1, testCase_1.getParameters().get(0).getName());
        Assert.assertEquals(ID_1, testCase_1.getName());
        Assert.assertEquals(testSuiteResponse_1, testCase_1.getExpected());
    }

}
