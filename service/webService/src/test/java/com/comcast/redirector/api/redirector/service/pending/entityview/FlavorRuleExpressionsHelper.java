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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlavorRuleExpressionsHelper {

    public static List<IfExpression> prepareSimpleFlavorRules(Map<? extends Expressions, String> expressionToServerPathMap) {
        List<IfExpression> expressions = new ArrayList<>();
        for (Map.Entry<? extends Expressions, String> entry : expressionToServerPathMap.entrySet()) {
            expressions.add(getFlavorIfExpression(entry.getKey(), entry.getValue()));
        }

        return expressions;
    }

    public static <T extends Expressions> IfExpression getFlavorIfExpression(final T statement, String serverPath) {
        IfExpression expression = new IfExpression();
        String ruleId = getRuleIdByServerPath(serverPath);
        expression.setId(ruleId);
        expression.setItems(new ArrayList<Expressions>() {{
            add(statement);
        }});
        expression.setReturn(ServerHelper.prepareServer(ruleId, serverPath));

        return expression;
    }

    public static String getRuleIdByServerPath(String value) {
        return "Rule_" + value;
    }
}
