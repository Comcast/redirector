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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.common.Context;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import com.comcast.redirector.common.InstanceInfo;

import javax.ws.rs.WebApplicationException;

public class TestSuiteUtils {
    public static TestSuiteResponse getRedirectorResponse(InstanceInfo instanceInfo) {
        TestSuiteResponse response = new TestSuiteResponse();
        if (instanceInfo != null) {
            response.setFlavor(instanceInfo.getFlavor());
            response.setRule(instanceInfo.getRuleName());
            response.setAppliedUrlRules(instanceInfo.getAppliedUrlRules());
            response.setIpVersion(Integer.toString((instanceInfo.getIpProtocolVersion())));
            response.setPort(Integer.toString(instanceInfo.getPort()));
            response.setUrn(instanceInfo.getUrn());
            response.setXreStack(instanceInfo.getStack());
            if (instanceInfo.getServerGroup() != null) {
                throw new WebApplicationException("Redirector test suite does not support Server Groups", 400);
            }
            response.setResponseType(instanceInfo.getServer().getReturnStatementType().toString());
            response.setProtocol(instanceInfo.getProtocol());
        }
        return response;
    }

    public static Context getRedirectorContext(RedirectorTestCase testCase) {
        Context.Builder contextBuilder = new Context.Builder();
        for (Parameter each : testCase.getParameters()) {
            contextBuilder.addEntry(each.getName(), each.getValues());
        }

        appendSessionId(contextBuilder, testCase);
        return contextBuilder.build();
    }

    private static void appendSessionId(Context.Builder contextBuilder, RedirectorTestCase testCase) {
        contextBuilder.addEntry(Context.SESSION_ID, testCase.getName() + System.nanoTime());
    }
}
