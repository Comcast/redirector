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

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.distribution.Rule;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "testCase")
@XmlSeeAlso({Parameter.class, TestSuiteResponse.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class RedirectorTestCase implements Expressions{

    @XmlElement(name = "testName")
    private String name;

    @XmlElement(name = "application")
    private String application;

    @XmlElementWrapper(name = "parameters")
    @XmlElements(@XmlElement(name="parameter", type=Parameter.class))
    private List<Parameter> parameters = new ArrayList<>();

    @XmlElement(name = "expected")
    private TestSuiteResponse expected;

    @XmlElements({
            @XmlElement(name = "distribution", type = Rule.class, required = false),
            @XmlElement(name = "ifexpression", type = IfExpression.class, required = false)
    })
    private VisitableExpression ruleUnderTest;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public TestSuiteResponse getExpected() {
        return expected;
    }

    public void setExpected(TestSuiteResponse expected) {
        this.expected = expected;
    }

    public void setRuleUnderTest(VisitableExpression ruleUnderTest) {
        this.ruleUnderTest = ruleUnderTest;
    }

    public VisitableExpression getRuleUnderTest() {
        return ruleUnderTest;
    }

    public boolean isNotEmpty() {
        return !parameters.isEmpty() && expected != null;
    }
}
