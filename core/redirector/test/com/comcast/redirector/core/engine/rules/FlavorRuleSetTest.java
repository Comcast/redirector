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

package com.comcast.redirector.core.engine.rules;

import com.comcast.redirector.ruleengine.model.Model;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FlavorRuleSetTest {

    @Test
    public void testModelUpdate() {
        Map<String, String> context = Collections.emptyMap();
        Model model = mock(Model.class);
        Object result = new Object();

        when(model.execute(anyMap())).thenReturn(result);
        when(model.executeDefault(anyMap())).thenReturn(result);

        FlavorRuleSet flavorRuleSet = new FlavorRuleSet(null);
        Assert.assertFalse(flavorRuleSet.isAvailable());
        Assert.assertNull(flavorRuleSet.getResult(context));
        Assert.assertNull(flavorRuleSet.getDefault());
        flavorRuleSet = new FlavorRuleSet(model);
        Assert.assertTrue(flavorRuleSet.isAvailable());
        Assert.assertNotNull(flavorRuleSet.getResult(context));
        Assert.assertNotNull(flavorRuleSet.getDefault());
    }
}
