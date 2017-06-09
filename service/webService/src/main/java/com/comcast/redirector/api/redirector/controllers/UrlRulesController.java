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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import com.comcast.redirector.api.validation.VerifyApplicationExists;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Component
@Path(RedirectorConstants.URL_RULES_CONTROLLER_PATH)
public class UrlRulesController {
    private IUrlRulesService urlRulesService;

    private IUrlParamsService urlParamsService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    public UrlRulesController() {
    }

    @Autowired
    private IStacksService stacksService;

    @Autowired
    public UrlRulesController(@Qualifier("urlRulesService") IUrlRulesService urlRulesService, @Qualifier("urlParamsService") IUrlParamsService urlParamsService) {
        this.urlRulesService = urlRulesService;
        this.urlParamsService = urlParamsService;
    }

    @Autowired
    private IExportFileNameHelper urlRulesFileNameExportHelper;

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllUrlRules(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        return Response.ok(urlRulesService.getAllRules(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/ids")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllUrlRuleIds(@VerifyApplicationExists @PathParam("serviceName")final String serviceName) {
        RuleIdsWrapper ruleIdsWrapper = new RuleIdsWrapper();
        Collection<String> ids = urlRulesService.getUrlRuleIdsByServiceName(serviceName);
        if (ids == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // service name not found
        } else {
            ruleIdsWrapper.setRuleIds(ids);
        }

        return Response.ok(ruleIdsWrapper).build();
    }

    @GET
    @Path("{serviceName}/{ruleId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUrlRule(@PathParam("serviceName") final String serviceName, @PathParam("ruleId") final String ruleId) {
        IfExpression urlRule = urlRulesService.getUrlRule(serviceName, ruleId);
        if (urlRule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        else {
            return Response.ok(urlRule).build();
        }
    }

    @GET
    @Path("{serviceName}/defaultUrlParams")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDefaultUrlParams(@PathParam("serviceName") final String serviceName) {
        return Response.ok(urlParamsService.getDefaultUrlParams(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportAllUrlRules(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        return Response
                .ok(urlRulesService.getAllRules(serviceName))
                .header(urlRulesFileNameExportHelper.getHeader(),
                        urlRulesFileNameExportHelper.getFileNameForAll(EntityType.URL_RULE, serviceName))
                .build();
    }

    @GET
    @Path("{serviceName}/export/{ruleId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportUrlRuleById(@VerifyApplicationExists @PathParam("serviceName") final String serviceName, @PathParam("ruleId") final String ruleId) {
        IfExpression urlRule = urlRulesService.getUrlRule(serviceName, ruleId);

        if (urlRule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response
                .ok(urlRule)
                .header(urlRulesFileNameExportHelper.getHeader(), urlRulesFileNameExportHelper.getFileNameForOneEntity(EntityType.URL_RULE, serviceName, ruleId))
                .build();
    }

    @POST
    @Path("{appName}/urlParams/default/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addUrlParams(@PathParam("appName") final String appName, final UrlRule urlRule) {
        operationContextHolder.buildContext(appName);

        urlParamsService.saveUrlParams(appName, urlRule, RedirectorConstants.DEFAULT_URL_RULE);
        return Response.ok(urlRule).build();
    }

    @POST
    @Path("{appName}/{ruleName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addUrlRule(@PathParam("appName") final String appName, @PathParam("ruleName") final String ruleName, final IfExpression rule, @Context UriInfo ui) {
        operationContextHolder.buildContext(appName);

        rule.setId(ruleName);
        urlRulesService.saveUrlRule(appName, rule, ruleName);
        return Response.created(ui.getRequestUri()).entity(rule).build();
    }

    @DELETE
    @Path("{appName}/{ruleId}/")
    public void deleteUrlRule(@PathParam("appName") final String appName, @PathParam("ruleId") final String ruleId) {
        operationContextHolder.buildContext(appName);

        urlRulesService.deleteUrlRule(appName, ruleId);
    }
}
