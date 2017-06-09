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

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import org.junit.Assert;
import org.junit.Test;

public class FlavorRulesValidatorTest {
    private FlavorRulesValidator testee = new FlavorRulesValidator();

    @Test
    public void testInvalidBecauseFlavorRulesNull() throws Exception {
        Assert.assertFalse(testee.validate(null).isSuccessValidation());
    }

    @Test
    public void testInvalidBecauseFlavorRulesDistributionNull() throws Exception {
        SelectServer selectServer = new SelectServer();

        Assert.assertFalse(testee.validate(selectServer).isSuccessValidation());
    }

    @Test
    public void testInvalidBecauseFlavorRulesDefaultServerNull() throws Exception {
        SelectServer selectServer = new SelectServer();
        selectServer.setDistribution(new Distribution());

        Assert.assertFalse(testee.validate(selectServer).isSuccessValidation());
    }

    @Test
    public void testValid() throws Exception {
        SelectServer selectServer = new SelectServer();
        Distribution distribution = new Distribution();
        distribution.setDefaultServer(new Server());
        selectServer.setDistribution(distribution);

        Assert.assertTrue(testee.validate(selectServer).isSuccessValidation());
    }
}
