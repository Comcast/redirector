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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.decider;

import com.comcast.redirector.api.decider.service.IDeciderRulesService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RuleIdsWrapper;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.common.DeciderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Component
@Path(DeciderConstants.DECIDER_RULES_PATH)
public class DeciderRulesController {

    @Autowired
    private IDeciderRulesService deciderRulesService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllRules(@Context UriInfo ui) {
        return Response.ok(deciderRulesService.getRules()).build();
    }

    @GET
    @Path("export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportAllRules(@Context UriInfo ui) {
        return Response.ok(deciderRulesService.getRules())
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.DECIDER_RULE))
                .build();
    }

    @GET
    @Path("ids")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRuleIds(@PathParam("ids") final String ruleId, @Context UriInfo ui) {
        return Response.ok(new RuleIdsWrapper(deciderRulesService.getRuleIds())).build();
    }

    @GET
    @Path("{ruleId}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRule(@PathParam("ruleId") final String ruleId, @Context UriInfo ui) {
        return Response.ok(deciderRulesService.getRule(ruleId))
                .build();
    }

    @GET
    @Path("export/{ruleId}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportRule(@PathParam("ruleId") final String ruleId, @Context UriInfo ui) {
        return Response.ok(deciderRulesService.getRule(ruleId))
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForOneEntityWithoutService(EntityType.DECIDER_RULE, ruleId))
                .build();
    }

    @POST
    @Path("{ruleName}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addRule(@PathParam("ruleName") final String ruleName, final IfExpression rule, @Context UriInfo ui) {
        rule.setId(ruleName);
        saveStatement(rule, ruleName);
        return Response.created(ui.getRequestUri()).entity(rule).build();
    }

    @DELETE
    @Path("{ruleId}/")
    public void deleteOne(@PathParam("ruleId") final String ruleId) {
        deciderRulesService.deleteRule(ruleId);
    }

    private void saveStatement(IfExpression rule, String ruleName) {
        deciderRulesService.saveRule(rule, ruleName);
    }
}
