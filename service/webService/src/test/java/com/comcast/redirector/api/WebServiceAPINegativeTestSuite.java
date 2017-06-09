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

package com.comcast.redirector.api;

import com.comcast.redirector.api.redirector.cache.CacheIntegrationTest;
import com.comcast.redirector.api.redirector.cache.ConcurrentPendingChangesIntegrationTest;
import com.comcast.redirector.api.redirector.cache.PendingChangesIntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CacheIntegrationTest.class,
    PendingChangesIntegrationTest.class,
    ConcurrentPendingChangesIntegrationTest.class
})
public class WebServiceAPINegativeTestSuite {
    public static final String CONFIG_OVERRIDE_PROPERTY = "redirector.override";

    @BeforeClass
    public static void before() {
        System.setProperty(CONFIG_OVERRIDE_PROPERTY, "negativetest.service.properties");
    }

    @AfterClass
    public static void after() {
        System.clearProperty(CONFIG_OVERRIDE_PROPERTY);
    }
}
