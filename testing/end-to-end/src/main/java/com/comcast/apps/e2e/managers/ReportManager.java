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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.api.model.testsuite.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.endpoint.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportManager {
    private static final Logger log = LoggerFactory.getLogger(ReportManager.class);

    public final static String END_TO_END_REPORT_ENDPOINT = Constants.SERVICE_URL_PREFIX + RedirectorConstants.END_TO_END_REPORT_PATH;

    private final String appName;
    private final FileUtil fileUtil;
    private final FilesPathHelper filesPathHelper;
    private final ServiceHelper serviceHelper;

    public ReportManager(String appName, FileUtil fileUtil, FilesPathHelper filesPathHelper, ServiceHelper serviceHelper) {
        this.appName = appName;
        this.fileUtil = fileUtil;
        this.filesPathHelper = filesPathHelper;
        this.serviceHelper = serviceHelper;
    }

    public void createReport(RedirectorTestCaseList testCases) throws IOException, SerializerException {
        TestCaseResultList report = prepareReport(testCases);
        fileUtil.writeJson(filesPathHelper.getFilename(FilesPathHelper.TestEntity.REPORT, appName), report);
    }

    private SessionList getSessions() {
        Response response = serviceHelper.getRequestBuilder(END_TO_END_REPORT_ENDPOINT, MediaType.APPLICATION_JSON).get();
        return response.readEntity(SessionList.class);
    }

    private TestCaseResultList prepareReport(RedirectorTestCaseList testCases) {
        SessionList sessions = getSessions();
        TestCaseResultList testCaseResultList = new TestCaseResultList();

        Map<String, RedirectorTestCase> nameToTestCaseMap = testCases.getRedirectorTestCases().stream()
            .collect(Collectors.toMap(RedirectorTestCase::getName, Function.identity()));

        List<TestCaseResult> testCaseResults = Optional.ofNullable(sessions.getSessions()).orElseGet(Collections::emptyList)
            .stream()
            .map(session -> getTestCaseResult(nameToTestCaseMap, session))
            .collect(Collectors.toList());
        testCaseResultList.setItems(testCaseResults);
        return testCaseResultList;
    }

    private TestCaseResult getTestCaseResult(Map<String, RedirectorTestCase> expectedResults, Session session) {
        TestCaseResult testCaseResult = new TestCaseResult();
        String testCaseName = session.getId();
        RedirectorTestCase originalTestCase = expectedResults.get(testCaseName);
        TestSuiteResponse expectedResult = originalTestCase.getExpected();

        RedirectorTestCase redirectorTestCase = new RedirectorTestCase();
        redirectorTestCase.setName(testCaseName);
        redirectorTestCase.setParameters(originalTestCase.getParameters());
        redirectorTestCase.setApplication(appName);
        redirectorTestCase.setExpected(expectedResult);
        redirectorTestCase.setRuleUnderTest(originalTestCase.getRuleUnderTest());

        TestSuiteResponse actualResult = session.getActual();
        testCaseResult.setStatus(TestCaseResult.Status.fromTestCase(expectedResult, actualResult));
        testCaseResult.setActual(actualResult);
        testCaseResult.setTestCase(redirectorTestCase);
        testCaseResult.setLogs(getSessionLogs(session));
        return testCaseResult;
    }

    private List<String> getSessionLogs(Session session) {
        return session.getEvents().stream().map(Event::getMessage).collect(Collectors.toList());
    }

    public void clearSessionLog() {
        Response response = serviceHelper.getRequestBuilder(END_TO_END_REPORT_ENDPOINT, MediaType.APPLICATION_JSON).delete();
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            log.info("Session log has been cleared successfully");
        } else {
            log.error("Session log has not been cleared");
        }
    }
}
