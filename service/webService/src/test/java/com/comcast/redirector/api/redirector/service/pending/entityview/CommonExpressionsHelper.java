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

import com.comcast.redirector.api.model.IfExpression;
import junit.framework.Assert;

import java.util.Collection;

public class CommonExpressionsHelper {
    static void verifyRuleExpressionsEqual(Collection<IfExpression> expected, Collection<IfExpression> actual) {
        IfExpression[] expectedRules = expected.toArray(new IfExpression[]{});
        IfExpression[] actualRules = actual.toArray(new IfExpression[]{});
        Assert.assertEquals(expectedRules.length, actualRules.length);
        for (int i=0; i < expectedRules.length; i++) {
            Assert.assertEquals(expectedRules[i], actualRules[i]);
        }
    }
}
