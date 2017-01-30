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

package it.helper;

import org.junit.Assert;

public class CachedModelVerifier {
    private ModelCache modelCache;

    public CachedModelVerifier(ModelCache modelCache) {
        this.modelCache = modelCache;
    }

    public void verifyFlavorRulePresentInCache(String ruleName) {
        Assert.assertTrue(modelCache.getFlavorRules().stream().filter(rule -> ruleName.equals(rule.getId())).findFirst().isPresent());
    }

    public void verifyUrlRulePresentInCache(String ruleName) {
        Assert.assertTrue(modelCache.getUrlRules().stream().filter(rule -> ruleName.equals(rule.getId())).findFirst().isPresent());
    }

    public void verifyDefaultServerFlavor(String expectedFlavor) {
        Assert.assertEquals(expectedFlavor, modelCache.getDefaultServer().getPath());
    }

    public void verifyDistributionFlavor(String expectedFlavor) {
        Assert.assertEquals(expectedFlavor, modelCache.getDistribution().getRules().iterator().next().getServer().getPath());
    }

    public void verifyDefaultUrlProtocol(String expectedProtocol) {
        Assert.assertEquals(expectedProtocol, modelCache.getUrlParams().getIpProtocolVersion());
    }

    public void verifyWhitelistContainsStack(String stack) {
        Assert.assertTrue(modelCache.getWhitelist().getPaths().contains(stack));
    }
}
