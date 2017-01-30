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

package com.comcast.redirector.api.model.testsuite;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement (name = "testCaseResult")
@XmlSeeAlso({TestSuiteResponse.class, RedirectorTestCase.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class TestCaseResult {
    public enum Status {
        PASSED, FAILED;

        public static Status fromTestCase(TestSuiteResponse expected, TestSuiteResponse actual) {
            return (expected.matches(actual)) ? PASSED : FAILED;
        }
    }

    @XmlElement(name = "status")
    private Status status;

    @XmlElement(name = "actual")
    private TestSuiteResponse actual;

    @XmlElement(name = "testcase")
    private RedirectorTestCase testCase;

    @XmlElement(name = "logs")
    private List<String> logs = new ArrayList<>();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TestSuiteResponse getActual() {
        return actual;
    }

    public void setActual(TestSuiteResponse actual) {
        this.actual = actual;
    }

    public RedirectorTestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(RedirectorTestCase testCase) {
        this.testCase = testCase;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
