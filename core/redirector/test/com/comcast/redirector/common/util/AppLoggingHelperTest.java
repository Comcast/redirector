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

package com.comcast.redirector.common.util;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class AppLoggingHelperTest {

    @Test
    public void testMessageFormat() {
        ThreadLocalLogger logger = mock(ThreadLocalLogger.class);
        String appName = "test";
        int modelVersion = 4;
        AppLoggingHelper testee = new AppLoggingHelper(logger, appName, modelVersion);

        testee.info("sample message", "message arguments no matter for this test");

        verify(logger, times(1)).info(
            eq("rapp=" + appName + " rmv=" + modelVersion + " app=" + appName + ".version=" + modelVersion + " : sample message"),
            (Object[]) anyVararg());
    }
}
