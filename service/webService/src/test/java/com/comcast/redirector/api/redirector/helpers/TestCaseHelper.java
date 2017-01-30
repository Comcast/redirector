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
 */
package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.common.RedirectorConstants;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestCaseHelper {

    public static Response post(WebTarget target, RedirectorTestCase testCase, String serviceName) {
        WebTarget webTarget = target.path(RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH).path(serviceName).path(testCase.getName());
        return webTarget.request().accept(MediaType.APPLICATION_XML).post(Entity.entity(testCase, MediaType.APPLICATION_XML));
    }

    public static Response get(WebTarget target, String testCaseName, String serviceName) {
        WebTarget webTarget = target.path(RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH).path(serviceName).path(testCaseName);
        return webTarget.request().accept(MediaType.APPLICATION_XML).get();
    }

    public static Response getAll(WebTarget target, String serviceName) {
        WebTarget webTarget = target.path(RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH).path(serviceName);
        return webTarget.request().accept(MediaType.APPLICATION_XML).get();
    }

    public static void delete(WebTarget target, String testCaseName, String serviceName) {
        WebTarget webTarget = target.path(RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH).path(serviceName).path(testCaseName);
        webTarget.request().delete();
    }

}
