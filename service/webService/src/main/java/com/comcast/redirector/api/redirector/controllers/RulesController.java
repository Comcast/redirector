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
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
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
@Path(RedirectorConstants.RULES_CONTROLLER_PATH)
public class RulesController {
    private IFlavorRulesService flavorRulesService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @Autowired
    public RulesController(@Qualifier("flavorRulesService") IFlavorRulesService flavorRulesService) {
        this.flavorRulesService = flavorRulesService;
    }

    @Autowired
    private IExportFileNameHelper rulesFileNameExportHelper;

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllRules(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        return Response.ok(flavorRulesService.getAllRules(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/ids")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllRuleIds(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        RuleIdsWrapper ruleIdsWrapper = new RuleIdsWrapper();
        Collection<String> ids = flavorRulesService.getRuleIdsByServiceName(serviceName);
        if (ids == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // service name not found
        } else {
            ruleIdsWrapper.setRuleIds(ids);
        }

        return Response.ok(ruleIdsWrapper).build();
    }

    @GET
    @Path("{serviceName}/export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportAllRules(@VerifyApplicationExists @PathParam("serviceName") final String serviceName) {
        SelectServer selectServer = new SelectServer();
        Collection<IfExpression> expressions = flavorRulesService.getRules(serviceName);
        if (expressions == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // service name not found
        } else {
            selectServer.setItems(expressions);
        }

        return Response.ok(selectServer)
                .header(rulesFileNameExportHelper.getHeader(), rulesFileNameExportHelper.getFileNameForAll(EntityType.RULE, serviceName))
                .build();
    }

    @GET
    @Path("{serviceName}/{ruleId}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRule(@VerifyApplicationExists @PathParam("serviceName") final String serviceName, @PathParam("ruleId") final String ruleId) {
        IfExpression flavorRule = flavorRulesService.getRule(serviceName, ruleId);
        if (flavorRule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(flavorRule).build();
        }
    }

    @GET
    @Path("{serviceName}/export/{ruleId}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportRule(@VerifyApplicationExists @PathParam("serviceName") final String serviceName, @PathParam("ruleId") final String ruleId) {
        IfExpression rule = flavorRulesService.getRule(serviceName, ruleId);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(rule)
                .header(rulesFileNameExportHelper.getHeader(), rulesFileNameExportHelper.getFileNameForOneEntity(EntityType.RULE,
                        serviceName, ruleId))
                .build();
    }

    @POST
    @Path("{serviceName}/{ruleName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addRule(@PathParam("serviceName") final String serviceName, @PathParam("ruleName") final String ruleName, final IfExpression rule, @Context UriInfo ui) {
        operationContextHolder.buildContext(serviceName);

        rule.setId(ruleName);
        flavorRulesService.saveRule(rule, serviceName);
        return Response.created(ui.getRequestUri()).entity(rule).build();
    }

    @DELETE
    @Path("{serviceName}/{ruleId}/")
    public void deleteRule(@PathParam("serviceName") final String serviceName, @PathParam("ruleId") final String ruleId) {
        operationContextHolder.buildContext(serviceName);

        flavorRulesService.deleteRule(serviceName, ruleId);
    }
}
