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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.Percent;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.google.common.base.Optional;

import java.util.ArrayList;

import static com.comcast.redirector.api.model.testsuite.visitor.TestSuiteExpressionVisitors.expressionToParameters;

public class DistributionRuleToTestConverter implements IToTestConverter<Rule> {
    private static final String TEST_NAME_SUFFIX = "_distribution_rule_autotest";
    private String serviceName;

    public DistributionRuleToTestConverter(String serviceName) {
        this.serviceName = serviceName;
    }

    private TestSuiteResponse obtainExpectedResponse(Rule rule) {
        TestSuiteResponse response = new TestSuiteResponse();

        Optional<String> path = Optional.fromNullable((rule.getServer()).getPath());
        if (path.or("").contains("/")) {
            XreStackPath stackPath = new XreStackPath(path.get() + "/" + serviceName);
            response.setFlavor(stackPath.getFlavor());
            response.setXreStack(stackPath.getStackOnlyPath());
        } else {
            response.setFlavor(path.or(""));
        }
        response.setResponseType("DISTRIBUTION_RULE");

        return response;
    }

    @Override
    public RedirectorTestCase toTestCase(Rule rule) {
        RedirectorTestCase result = new RedirectorTestCase();
        result.setApplication(serviceName);
        result.setName("Distribution" + rule.getId() + TEST_NAME_SUFFIX);
        Percent percent = new Percent();
        percent.setValue(rule.getPercent());
        result.setParameters(new ArrayList<>(expressionToParameters(percent)));
        result.setExpected(obtainExpectedResponse(rule));
        result.setRuleUnderTest(rule);

        return result;
    }
}
