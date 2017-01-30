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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestCaseManager {

    private final String serviceName;
    private final ServiceHelper serviceHelper;

    public TestCaseManager(String serviceName, ServiceHelper serviceHelper) {
        this.serviceName = serviceName;
        this.serviceHelper = serviceHelper;
    }

    public RedirectorTestCaseList getRedirectorTestCaseList() {
        String testCasesServicePath = RedirectorConstants.END_TO_END_PATH + "/testCases/" + serviceName;
        Response response = serviceHelper.getRequestBuilder(testCasesServicePath, MediaType.APPLICATION_JSON).get();
        return response.readEntity(RedirectorTestCaseList.class);
    }
}
