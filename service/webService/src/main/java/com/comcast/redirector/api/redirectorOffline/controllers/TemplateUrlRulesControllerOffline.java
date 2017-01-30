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
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path(RedirectorConstants.TEMPLATES_URL_RULE_OFFLINE_CONTROLLER_PATH)
public class TemplateUrlRulesControllerOffline extends UrlRulesControllerOffline {

    @Autowired
    private OperationContextHolder operationContextHolder;

    @Autowired
    public TemplateUrlRulesControllerOffline(@Qualifier("templateUrlRulesService") IUrlRulesService urlRulesService, @Qualifier("urlParamsService") IUrlParamsService urlParamsService) {
        super(urlRulesService, urlParamsService);
    }

    @POST
    @Path("{appName}/{ruleId}/delete")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteUrlRule(@PathParam("appName") final String appName, @PathParam("ruleId") final String ruleId, final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = urlRulesService.deleteTemplateUrlRule(appName, ruleId);
        return Response.ok(result).build();
    }
}
