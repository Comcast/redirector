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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.common.util.RulesUtils;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.core.modelupdate.chain.validator.UrlRulesValidator;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.comcast.redirector.api.model.testsuite.visitor.TestSuiteExpressionVisitors.expressionToParameters;

class UrlRuleToTestConverter implements IToTestConverter<IfExpression> {
    private static final Logger log = LoggerFactory.getLogger(UrlRuleToTestConverter.class);

    private static final String TEST_NAME_SUFFIX = "_url_rule_autotest";

    private NamespacedListRepository namespacedLists;

    @Autowired
    private Serializer serializer;
    private String serviceName;

    UrlRuleToTestConverter(String serviceName, NamespacedListRepository namespacedLists) {
        this.serviceName = serviceName;
        this.namespacedLists = namespacedLists;
    }

    @Override
    public RedirectorTestCase toTestCase(IfExpression rule) {
        RedirectorTestCase result = new RedirectorTestCase();
        result.setApplication(serviceName);
        result.setName(rule.getId() + TEST_NAME_SUFFIX);
        result.setParameters(obtainParameters(rule));
        result.setRuleUnderTest(rule);
        result.setExpected(obtainExpectedResponse(rule));

        if (result.isNotEmpty()) {
            return result;
        } else {
            if (log.isWarnEnabled()) {
                try {
                    log.warn("Test Case was not created for rule {} since it's expressions are not supported", serializer.serialize(rule, true /* format output */));
                } catch (SerializerException e) {
                    log.error("failed to serialize rule for logging", e);
                }
            }
            return null;
        }
    }

    private List<Parameter> obtainParameters(IfExpression rule) {
        List<Parameter> parameters = new ArrayList<>();
        for (Expressions expression : rule.getItems()) {
            parameters.addAll(expressionToParameters(expression, name -> namespacedLists.getNamespacedListValues(name)));
        }

        return parameters;
    }

    private TestSuiteResponse obtainExpectedResponse(IfExpression rule) {
        TestSuiteResponse response = new TestSuiteResponse();

        for (Expressions expressions : RulesUtils.getReturn(rule)) {
            if (expressions instanceof UrlRule) {
                UrlRule urlRule = (UrlRule) expressions;
                if (UrlRulesValidator.isValidNumericValue(urlRule.getIpProtocolVersion())) {
                    response.setIpVersion(urlRule.getIpProtocolVersion());
                }
                if (StringUtils.isNotBlank(urlRule.getUrn())) {
                    response.setUrn(urlRule.getUrn());
                }

                if (UrlRulesValidator.isValidNumericValue(urlRule.getPort())) {
                    response.setPort(urlRule.getPort());
                }
                if (StringUtils.isNotBlank(urlRule.getProtocol())) {
                    response.setProtocol(urlRule.getProtocol());
                }

                response.addAppliedRuleName(rule.getId());

                return response;
            }
        }

        return null;
    }
}
