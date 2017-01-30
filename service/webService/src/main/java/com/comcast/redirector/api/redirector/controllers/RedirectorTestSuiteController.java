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

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.model.testsuite.TestCaseResult;
import com.comcast.redirector.api.model.testsuite.TestCaseResultList;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.redirectortestsuite.IRedirectorTestSuiteService;
import com.comcast.redirector.api.redirector.service.redirectortestsuite.RedirectorTestSuiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@Path(RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH)
public class RedirectorTestSuiteController {
    private static Logger log = LoggerFactory.getLogger(RedirectorTestSuiteController.class);

    @Autowired
    private IRedirectorTestSuiteService testSuiteService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @GET
    @Path("{serviceName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllTestCases(@PathParam("serviceName") final String serviceName) {
        Collection<RedirectorTestCase> testCases = testSuiteService.getAllTestCasesByServiceName(serviceName);
        RedirectorTestCaseList testCaseList = new RedirectorTestCaseList();
        testCaseList.setRedirectorTestCases(testCases);
        return Response.ok(testCaseList).build();
    }

    @GET
    @Path("export/{serviceName}/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportAllTestCases(@PathParam("serviceName") final String serviceName) {
        Collection<RedirectorTestCase> testCases = testSuiteService.getAllTestCasesByServiceName(serviceName);
        RedirectorTestCaseList testCaseList = new RedirectorTestCaseList();
        testCaseList.setRedirectorTestCases(testCases);
        return Response.ok(testCaseList)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.TEST_CASE, serviceName))
                .build();
    }

    @GET
    @Path("{serviceName}/{testName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTestCaseById(@PathParam("serviceName") final String serviceName,
                                    @PathParam("testName") final String testName) {
        RedirectorTestCase testCase = testSuiteService.getTestCase(serviceName, testName);
        if (testCase == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(testCase).build();
    }

    @GET
    @Path("export/{serviceName}/{testName}/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportTestCaseById(@PathParam("serviceName") final String serviceName,
                                       @PathParam("testName") final String testName) {
        RedirectorTestCase testCase = testSuiteService.getTestCase(serviceName, testName);
        if (testCase == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(testCase)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForOneEntity(EntityType.TEST_CASE, serviceName, testName))
                .build();
    }

    @POST
    @Path("{serviceName}/{testName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveTestCase(@PathParam("serviceName") final String serviceName,
                                 @PathParam("testName") final String testName,
                                 final RedirectorTestCase testCase,
                                 @Context UriInfo ui) {
        testSuiteService.saveTestCase(serviceName, testCase);
        return Response.created(ui.getRequestUri()).entity(testCase).build();
    }

    @DELETE
    @Path("{serviceName}/{ids}")
    public void deleteTestCasesByIds(@PathParam("serviceName") final String serviceName,
                                     @PathParam("ids") final String testIds) {
        List<String> ids = Arrays.asList(testIds.replaceAll("\\s+", "").split(","));
        testSuiteService.deleteTestCasesByIds(ids, serviceName);
    }

    @GET
    @Path("{serviceName}/{testName}/{mode}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runTestCase(@PathParam("serviceName") final String serviceName,
                                @PathParam("testName") final String testName,
                                @PathParam("mode") final String mode) {
        TestCaseResult result = testSuiteService.runTestCase(serviceName, testName, mode);
        return Response.ok(result).build();
    }

    @GET
    @Path("runAuto/{serviceName}/{mode}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runTestCase(@PathParam("serviceName") final String serviceName,
                                @PathParam("mode") final String mode,
                                @Context UriInfo ui) {
        TestCaseResultList result = new TestCaseResultList();
        String baseUrl = ui.getQueryParameters().getFirst("baseURL");
        if (isExternalRestMode(mode)) {
            validateUrl(baseUrl);
        }
        result.setItems(testSuiteService.autoCreateTestCasesAndRun(serviceName, mode, baseUrl));
        return Response.ok(result).build();
    }

    private boolean isExternalRestMode(String mode) {
        return RedirectorTestSuiteService.TestMode.EXTERNAL_REST.equals(mode);
    }

    private void validateUrl(String baseUrl) {
        try {
            new URL(baseUrl);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }
}
