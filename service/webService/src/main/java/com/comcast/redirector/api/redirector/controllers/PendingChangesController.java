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
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Warning;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.DistributionPendingChangesWrapper;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedPendingChangesWrapper;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.pending.IChangesBatchWriteService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.IPendingSingletonEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.api.validation.VerifyApplicationExists;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.metrics.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path(RedirectorConstants.PENDING_CONTROLLER_PATH)
public class PendingChangesController {

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    @Qualifier("nextDistributionEntityViewService")
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Autowired
    @Qualifier("nextWhitelistedEntityViewService")
    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Autowired
    @Qualifier("pendingFlavorRuleWriteService")
    protected IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService;

    @Autowired
    @Qualifier("pendingTemplateFlavorRuleWriteService")
    protected IPendingEntityWriteService<IfExpression> pendingTemplateFlavorRuleWriteService;

    @Autowired
    @Qualifier("pendingUrlRuleWriteService")
    protected IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService;

    @Autowired
    @Qualifier("pendingTemplateUrlRuleWriteService")
    protected IPendingEntityWriteService<IfExpression> pendingTemplateUrlRuleWriteService;

    @Autowired
    @Qualifier("pendingServerWriteService")
    private IPendingEntityWriteService<Server> pendingServerWriteService;

    @Autowired
    @Qualifier("pendingUrlParamsWriteService")
    private IPendingEntityWriteService<UrlRule> pendingUrlParamsWriteService;

    @Autowired
    @Qualifier("pendingDistributionWriteService")
    private IPendingSingletonEntityWriteService<Distribution> pendingDistributionWriteService;

    @Autowired
    @Qualifier("pendingWhitelistedWriteService")
    private IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService;

    @Autowired
    @Qualifier("changesBatchWriteService")
    private IChangesBatchWriteService pendingChangesBatchWriteService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @GET
    @Path("{serviceName}/preview/distribution/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDistributionPendingPreview(@VerifyApplicationExists @PathParam("serviceName") final String serviceName){
        return Response.ok(getNextDistributionEntityViewService().getEntity(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/preview/stackmanagement/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWhitelistedPendingPreview(@VerifyApplicationExists @PathParam("serviceName") final String serviceName){
        return Response.ok(getNextWhitelistedEntityViewService().getEntity(serviceName)).build();
    }

    @GET
    @Path("{serviceName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPendingChanges(@PathParam("serviceName") final String serviceName){
        return Response.ok(getPendingChangesService().getPendingChangesStatus(serviceName)).build();
    }

    @GET
    @Path("{serviceName}/newRuleIds/{objectType}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNewRuleIds(@VerifyApplicationExists @PathParam("serviceName") final String serviceName,
                                      @PathParam("objectType") final String objectType){
        return Response.ok(new RuleIdsWrapper(getPendingChangesService().getNewRulesIds(serviceName, objectType))).build();
    }

    @GET
    @Path("{serviceName}/export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportPendingChanges(@PathParam("serviceName") final String serviceName){
        return Response.ok(getPendingChangesService().getPendingChangesStatus(serviceName))
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.PENDING_CHANGES_STATUS, serviceName))
                .build();
    }

    @GET
    @Path("{serviceName}/export/{objectType}/{changeId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportPendingChange(@PathParam("serviceName") final String serviceName,
                                        @PathParam("objectType") String objectType,
                                        @PathParam("changeId") String changeId){
        PendingChange result = getPendingChangesService().getPendingChangeByTypeAndId(serviceName, objectType, changeId);
        return Response.ok(result)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForOneEntity(EntityType.PENDING_CHANGES_STATUS, serviceName, changeId))
                .build();
    }

    @GET
    @Path("{serviceName}/export/" + RedirectorConstants.PENDING_STATUS_DISTRIBUTIONS + "/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportDistributionPendingChanges(@PathParam("serviceName") final String serviceName){
        DistributionPendingChangesWrapper result = new DistributionPendingChangesWrapper();
        PendingChangesStatus pendingChangesStatus = getPendingChangesService().getPendingChangesStatus(serviceName);
        result.setChangeMap(pendingChangesStatus.getDistributions());
        return Response.ok(result)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.PENDING_DISTRIBUTIONS, serviceName))
                .build();
    }

    @GET
    @Path("{serviceName}/export/" + RedirectorConstants.PENDING_STATUS_STACKMANAGEMENT + "/")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportWhitelistedPendingChanges(@PathParam("serviceName") final String serviceName){
        WhitelistedPendingChangesWrapper result = new WhitelistedPendingChangesWrapper();
        PendingChangesStatus pendingChangesStatus = getPendingChangesService().getPendingChangesStatus(serviceName);
        result.setChangeMap(pendingChangesStatus.getWhitelisted());
        return Response.ok(result)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.PENDING_STACKS, serviceName))
                .build();
    }


    @POST
    @Path("{appName}/approveWithoutValidation/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingChanges(@PathParam("appName") final String appName,
                                          @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingChangesBatchWriteService().approve(appName, version);

        reportWSModelApproveStats(appName);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/approve/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateAndApprovePendingChanges(@PathParam("appName") final String appName,
                                                     @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        ValidationReport validationReport = getPendingChangesService().validateModelBeforeApprove(appName, null);
        if (validationReport.isSuccessValidation()) {
            getPendingChangesBatchWriteService().approve(appName, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    private void reportWSModelApproveStats(String appName) {
        int version = getPendingChangesService().getPendingChangesStatus(appName).getVersion();

        Metrics.reportWSModelApproveStats(appName, version);
    }

    @POST
    @Path("{serviceName}/cancel/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingChanges(@PathParam("serviceName") final String serviceName,
                                         @PathParam("version") final int version){
        getPendingChangesBatchWriteService().cancel(serviceName, version);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/rule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingRule(@PathParam("appName") final String appName,
                                       @PathParam("ruleId") final String ruleId,
                                       @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        ValidationReport validationReport = pendingChangesService.validateModelBeforeApprove(appName, EntityType.RULE);
        if (validationReport.isSuccessValidation()) {
            getPendingFlavorRuleWriteService().approve(appName, ruleId, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    @POST
    @Path("{appName}/templateRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveTemplatePendingRule(@PathParam("appName") final String appName,
                                       @PathParam("ruleId") final String ruleId,
                                       @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingTemplateFlavorRuleWriteService().approve(appName, ruleId, version);

        reportWSModelApproveStats(appName);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/templateUrlRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveTemplateUrlPendingRule(@PathParam("appName") final String appName,
                                       @PathParam("ruleId") final String ruleId,
                                       @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingTemplateUrlRuleWriteService().approve(appName, ruleId, version);

        reportWSModelApproveStats(appName);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/urlRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingUrlRule(@PathParam("appName") final String appName,
                                          @PathParam("ruleId") final String ruleId,
                                          @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        ValidationReport validationReport = pendingChangesService.validateModelBeforeApprove(appName, EntityType.URL_RULE);
        if (validationReport.isSuccessValidation()) {
            getPendingUrlRuleWriteService().approve(appName, ruleId, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    @POST
    @Path("{appName}/urlParams/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingUrlParams(@PathParam("appName") final String appName,
                                            @PathParam("ruleId") final String ruleId,
                                            @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingUrlParamsWriteService().approve(appName, ruleId, version);

        reportWSModelApproveStats(appName);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/server/"+ RedirectorConstants.DEFAULT_SERVER_NAME +"/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingServer(@PathParam("appName") final String appName,
                                         @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);

        ValidationReport validationReport = pendingChangesService.validateModelBeforeApprove(appName, EntityType.SERVER);
        if (validationReport.isSuccessValidation()) {
            getPendingServerWriteService().approve(appName, RedirectorConstants.DEFAULT_SERVER_NAME, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    @POST
    @Path("{appName}/distribution/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveAllPendingDistribution(@PathParam("appName") final String appName,
                                                  @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        ValidationReport validationReport = pendingChangesService.validateModelBeforeApprove(appName, EntityType.DISTRIBUTION);
        if (validationReport.isSuccessValidation()) {
            getPendingDistributionWriteService().approve(appName, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    @POST
    @Path("{appName}/stackmanagement/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveAllPendingWhitelists(@PathParam("appName") final String appName,
                                                  @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingWhitelistedWriteService().approve(appName, version);

        reportWSModelApproveStats(appName);
        return Response.ok().build();
    }

    @POST
    @Path("{appName}/stackmanagement/validate/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateAndApproveAllPendingWhitelists(@PathParam("appName") final String appName,
                                                           @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        ValidationReport validationReport = pendingChangesService.validateModelBeforeApprove(appName, EntityType.WHITELIST);
        if (validationReport.isSuccessValidation()) {
            getPendingWhitelistedWriteService().approve(appName, version);

            reportWSModelApproveStats(appName);
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return buildResponse(validationReport);
    }

    @DELETE
    @Path("{appName}/rule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingRule(@PathParam("appName") final String appName,
                                      @PathParam("ruleId") final String ruleId,
                                      @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingFlavorRuleWriteService().cancel(appName, ruleId, version);
        return Response.ok().build();
    }

    @DELETE
    @Path("{appName}/templateRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelTemplatePendingRule(@PathParam("appName") final String appName,
                                      @PathParam("ruleId") final String ruleId,
                                      @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingTemplateFlavorRuleWriteService().cancel(appName, ruleId, version);
        return Response.ok().build();
    }

    @DELETE
    @Path("{appName}/templateUrlRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelTemplateUrlPendingRule(@PathParam("appName") final String appName,
                                      @PathParam("ruleId") final String ruleId,
                                      @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingTemplateUrlRuleWriteService().cancel(appName, ruleId, version);
        return Response.ok().build();
    }


    @DELETE
    @Path("{appName}/distribution/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingDistribution(@PathParam("appName") final String appName,
                                              @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingDistributionWriteService().cancel(appName, version);
        return Response.ok().build();
    }

    @DELETE
    @Path("{appName}/stackmanagement/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingWhitelisted(@PathParam("appName") final String appName,
                                              @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingWhitelistedWriteService().cancel(appName, version);
        return Response.ok().build();
    }


    @DELETE
    @Path("{appName}/urlRule/{ruleId}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingUrlRule(@PathParam("appName") final String appName,
                                         @PathParam("ruleId") final String ruleId,
                                         @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingUrlRuleWriteService().cancel(appName, ruleId, version);
        return Response.ok().build();
    }

    @DELETE
    @Path("{appName}/urlParams/{urlParamsName}/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingUrlParams(@PathParam("appName") final String appName,
                                           @PathParam("urlParamsName") final String urlParamsName,
                                           @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingUrlParamsWriteService().cancel(appName, urlParamsName, version);
        return Response.ok().build();
    }

    @DELETE
    @Path("{appName}/server/" + RedirectorConstants.DEFAULT_SERVER_NAME + "/{version}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingServer(@PathParam("appName") final String appName,
                                        @PathParam("version") final int version){
        operationContextHolder.buildContext(getPendingChangesService(), appName);
        getPendingServerWriteService().cancel(appName, RedirectorConstants.DEFAULT_SERVER_NAME, version);
        return Response.ok().build();
    }

    private Response buildResponse(ValidationReport validationReport) {
        Response response;
        if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
            Warning warning = new Warning();
            warning.setMessage(validationReport.getMessage());
            response = Response.ok(warning).build();
        } else {
            response = Response.ok().build();
        }
        return response;
    }

    public IChangesStatusService getPendingChangesService() {
        return pendingChangesService;
    }

    public IEntityViewService<Distribution> getNextDistributionEntityViewService() {
        return nextDistributionEntityViewService;
    }

    public IEntityViewService<Whitelisted> getNextWhitelistedEntityViewService() {
        return nextWhitelistedEntityViewService;
    }

    public IPendingEntityWriteService<IfExpression> getPendingFlavorRuleWriteService() {
        return pendingFlavorRuleWriteService;
    }

    public IPendingEntityWriteService<IfExpression> getPendingTemplateFlavorRuleWriteService() {
        return pendingTemplateFlavorRuleWriteService;
    }

    public IPendingEntityWriteService<IfExpression> getPendingUrlRuleWriteService() {
        return pendingUrlRuleWriteService;
    }

    public IPendingEntityWriteService<IfExpression> getPendingTemplateUrlRuleWriteService() {
        return pendingTemplateUrlRuleWriteService;
    }

    public IPendingEntityWriteService<Server> getPendingServerWriteService() {
        return pendingServerWriteService;
    }

    public IPendingEntityWriteService<UrlRule> getPendingUrlParamsWriteService() {
        return pendingUrlParamsWriteService;
    }

    public IPendingSingletonEntityWriteService<Distribution> getPendingDistributionWriteService() {
        return pendingDistributionWriteService;
    }

    public IPendingSingletonEntityWriteService<Whitelisted> getPendingWhitelistedWriteService() {
        return pendingWhitelistedWriteService;
    }

    public IChangesBatchWriteService getPendingChangesBatchWriteService() {
        return pendingChangesBatchWriteService;
    }
}
