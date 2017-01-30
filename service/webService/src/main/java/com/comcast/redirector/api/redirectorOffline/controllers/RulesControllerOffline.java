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

package com.comcast.redirector.api.redirectorOffline.controllers;

import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.RULES_OFFLINE_CONTROLLER_PATH)
public class RulesControllerOffline {

    protected IFlavorRulesService flavorRulesService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @Autowired
    public RulesControllerOffline(@Qualifier("flavorRulesService") IFlavorRulesService flavorRulesService) {
        this.flavorRulesService = flavorRulesService;
    }

    @POST
    @Path("{serviceName}/{ruleName}/add")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addRule(@PathParam("serviceName") final String serviceName, @PathParam("ruleName") final String ruleName, final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(serviceName, snapshot);

        IfExpression pendingRule = (IfExpression)snapshot.getEntityToSave();
        OperationResult result = flavorRulesService.saveRule(serviceName, pendingRule, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{serviceName}/{ruleName}/delete")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteRule(@PathParam("serviceName") final String serviceName, @PathParam("ruleName") final String ruleId, final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(serviceName, snapshot);

        OperationResult result = flavorRulesService.deleteRule(serviceName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

}
