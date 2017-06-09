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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.tasks;

import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.managers.TestCaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateTestCasesTask implements IProcessTask {
    private static final Logger log = LoggerFactory.getLogger(GenerateTestCasesTask.class);

    @Override
    public Boolean handle(Context context) throws Exception {
        String serviceName = context.getServiceName();
        String baseUrl = context.getBaseUrl();
        TestCaseManager testCaseManager = new TestCaseManager(serviceName, new ServiceHelper(baseUrl));
        context.setRedirectorTestCaseList(testCaseManager.getRedirectorTestCaseList());
        log.info("Test cases have been loaded successfully for '{}'", serviceName);
        return true;
    }
}
