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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.builders;

import com.comcast.redirector.api.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class IfExpressionBuilder {
    private String id;
    private String templateDependencyName;
    private List<Expressions> expressions = new ArrayList<>();
    private Expressions returnStatement;

    public IfExpressionBuilder withRuleName(String ruleName) {
        this.id = ruleName;
        return this;
    }

    public IfExpressionBuilder withTemplateName(String templateName) {
        this.templateDependencyName = templateName;
        return this;
    }

    public IfExpressionBuilder withExpressions(List<Expressions> expressions) {
        this.expressions = expressions;
        return this;
    }

    public IfExpressionBuilder withExpression(Expressions expression) {
        this.expressions.add(expression);
        return this;
    }

    public IfExpressionBuilder withReturnStatement(Expressions expressions) {
        this.returnStatement = expressions;
        return this;
    }

    public IfExpression build () {
        IfExpression ifExpression = new IfExpression();
        ifExpression.setId(id);
        ifExpression.setTemplateDependencyName(templateDependencyName);
        return buildIfExpressoin(ifExpression);
    }

    private IfExpression buildIfExpressoin(IfExpression ifExpression) {
        IfExpression current = ifExpression;
        ListIterator<Expressions> iter = expressions.listIterator();
        while (iter.hasNext()) {
            List<Expressions> items = new ArrayList<>();
            Expressions exp1 = iter.next();
            Expressions exp2 = iter.hasNext() ? iter.next() : null;

            if (isORorXOR(exp1) || isORorXOR(exp2) || exp2 == null) {
                items.add(exp1);
                if (exp2 != null) {
                    iter.previous();
                }
            }
            else if (!isORorXOR(exp1) && !isORorXOR(exp2)) {
                AndExpression andExpression = new AndExpression();
                andExpression.setItems(Arrays.asList(exp1, exp2));
                items.add(andExpression);
            }
            current.setItems(items);

            if(iter.hasNext()) {
                IfExpression nextIf = new IfExpression();
                items.add(nextIf);
                current = nextIf;
            }
        }
        current.setReturn(returnStatement);

        return ifExpression;
    }

    private boolean isORorXOR (Expressions exp) {
        return (exp != null) && (exp instanceof OrExpression) || (exp instanceof XORExpression);
    }

}
