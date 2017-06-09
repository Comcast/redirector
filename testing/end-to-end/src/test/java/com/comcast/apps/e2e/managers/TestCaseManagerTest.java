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
 */
package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

public class TestCaseManagerTest {

    public static final String APP_NAME = "appName";
    public static final String testCasesServicePath = RedirectorConstants.END_TO_END_PATH + "/testCases/" + APP_NAME;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private Response response;

    @Mock
    private Invocation.Builder builder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(serviceHelper.getRequestBuilder(testCasesServicePath, MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
    }

    @Test
    public void createTestCaseManagerTest() {
        TestCaseManager testCaseManager = new TestCaseManager(APP_NAME, serviceHelper);
        testCaseManager.getRedirectorTestCaseList();
        verify(response, times(1)).readEntity(RedirectorTestCaseList.class);
    }

}
