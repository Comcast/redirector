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

package com.comcast.redirector.api.redirector.negativeintegrationtests;

import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.api.redirector.helpers.WhitelistedHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class WhitelistNegativeIntegrationTests extends BaseNegativeTests{

    @Test
    public void testWhitelistEmptyPathError() {
        Whitelisted whitelisted = WhitelistedHelper.createWhitelisted("");
        WebTarget webTarget = HttpTestServerHelper.target().path(WHITELISTED_SERVICE_PATH).path(SERVICE_NAME);
        ValidationState error = ServiceHelper.post(webTarget, whitelisted, MediaType.APPLICATION_JSON, ValidationState.class);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.WhitelistEmptyPath, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testWhitelistDeleteError() {
        Whitelisted whitelisted = new Whitelisted();
        WebTarget webTarget = HttpTestServerHelper.target().path(WHITELISTED_SERVICE_PATH).path(SERVICE_NAME);
        ValidationState error = ServiceHelper.post(webTarget, whitelisted, MediaType.APPLICATION_JSON, ValidationState.class);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.WhitelistDeleteError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testWhitelistInvalidPathError() {
        Whitelisted whitelisted = WhitelistedHelper.createWhitelisted("#invalid path");
        WebTarget webTarget = HttpTestServerHelper.target().path(WHITELISTED_SERVICE_PATH).path(SERVICE_NAME);
        ValidationState error = ServiceHelper.post(webTarget, whitelisted, MediaType.APPLICATION_JSON, ValidationState.class);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.WhitelistInvalidPath, error.getErrors().keySet().iterator().next());
    }
}
