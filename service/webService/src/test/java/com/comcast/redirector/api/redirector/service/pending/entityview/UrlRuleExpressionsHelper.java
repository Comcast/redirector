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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.url.rule.UrlRule;

import java.util.ArrayList;

public class UrlRuleExpressionsHelper {

    public static <T extends Expressions> IfExpression getUrlRuleIfExpression(final T statement, String protocol, String ipVersion) {
        IfExpression expression = new IfExpression();
        String ruleId = getRuleIdByProtocolAndIp(protocol, ipVersion);
        expression.setId(ruleId);
        expression.setItems(new ArrayList<Expressions>() {{
            add(statement);
        }});
        expression.setReturn(prepareUrlParams(protocol, ipVersion));

        return expression;
    }

    public static UrlRule prepareUrlParams(String protocol, String ipVersion) {
        UrlRule urlRule = new UrlRule();
        urlRule.setProtocol(protocol);
        urlRule.setIpProtocolVersion(ipVersion);
        return urlRule;
    }

    public static String getRuleIdByProtocolAndIp(String protocol, String ipVersion) {
        return "url_rule_" + protocol + "_" + ipVersion;
    }
}
