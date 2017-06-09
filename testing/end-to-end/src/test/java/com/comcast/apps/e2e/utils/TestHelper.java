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

package com.comcast.apps.e2e.utils;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.Session;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestHelper {
    private static final Logger log = LoggerFactory.getLogger(TestHelper.class);

    public static String load(String fileName) throws IOException {
        String result;
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                result = new String(bytes, "UTF-8");
                if (StringUtils.isBlank(result)) {
                    String messageError = String.format("Failed to read file %s: data is absent", fileName);
                    log.error(messageError);
                    throw new IllegalArgumentException(messageError);
                }
            } else {
                String messageError = String.format("Failed to read file %s: file is absent", fileName);
                log.error(messageError);
                throw new IOException(messageError);
            }
        } catch (IOException e) {
            String messageError = String.format("Failed to read data from file: %s", fileName);
            log.error(messageError);
            throw new IOException(messageError);
        }
        return result;
    }

    public static String getResourcesPath() {
        String propertiesPath = FileUtil.getFilePath(E2EConfigLoader.DEFAULT_PROPERTIES_FILE_NAME);
        return propertiesPath.substring(0, propertiesPath.lastIndexOf("/"));
    }

    public static Session createSession(String id, TestSuiteResponse response) {
        Session session = new Session(id);
        session.setActual(response);
        return session;
    }

    public static TestSuiteResponse createTestSuiteResponse(String protocol, String port,
                                                            String ipVersion, String urn,
                                                            String xreStack, String flavor,
                                                            String rule, String responceType) {
        TestSuiteResponse testSuiteResponse = new TestSuiteResponse();
        testSuiteResponse.setProtocol(protocol);
        testSuiteResponse.setPort(port);
        testSuiteResponse.setIpVersion(ipVersion);
        testSuiteResponse.setUrn(urn);
        testSuiteResponse.setXreStack(xreStack);
        testSuiteResponse.setFlavor(flavor);
        testSuiteResponse.setRule(rule);
        testSuiteResponse.setResponseType(responceType);
        return testSuiteResponse;
    }

    public static RedirectorTestCase createRedirectorTestCase(String name, String application, TestSuiteResponse response, Parameter... parameter) {

        RedirectorTestCase testCase = new RedirectorTestCase();
        testCase.setName(name);
        testCase.setApplication(application);
        testCase.setParameters(createParameters(parameter));
        testCase.setExpected(response);
        return testCase;
    }

    public static List<Parameter> createParameters(Parameter... parameters) {
        List<Parameter> parameterList = new ArrayList<>();
        Collections.addAll(parameterList, parameters);
        return parameterList;
    }

    public static Parameter createParameter(String name, String valueName) {
        Parameter parameter = new Parameter();
        parameter.setName(name);

        Value value;

        if (org.apache.commons.lang3.StringUtils.isNotBlank(valueName)) {
            value = new Value(valueName);
            parameter.setValues(Collections.singletonList(value));
        }

        return parameter;
    }

    public static TestSuiteResponse createTestSuiteResponseWithFlaverAndRule(String flavor, String rule) {
        TestSuiteResponse testSuiteResponse = new TestSuiteResponse();
        testSuiteResponse.setFlavor(flavor);
        testSuiteResponse.setRule(rule);
        return testSuiteResponse;
    }
}
