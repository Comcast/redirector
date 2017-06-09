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

package com.comcast.redirector.api.redirectorOffline.controllers;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.GenericOperationResult;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import com.comcast.redirector.api.redirector.service.pending.IChangesBatchWriteService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.IPendingSingletonEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.dataaccess.EntityType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Component
@Path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH)
public class PendingChangesControllerOffline {
    private static final Logger log = LoggerFactory.getLogger(PendingChangesControllerOffline.class);

    @Autowired
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Autowired
    private IPendingSingletonEntityWriteService<Distribution> pendingDistributionWriteService;

    @Autowired
    private IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService;

    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    @Autowired
    @Qualifier("pendingFlavorRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService;

    @Autowired
    @Qualifier("pendingUrlRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService;

    @Autowired
    @Qualifier("pendingTemplateUrlRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingTemplateUrlRuleWriteService;

    @Autowired
    @Qualifier("pendingTemplateFlavorRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingTemplateFlavorRuleWriteService;

    @Autowired
    @Qualifier("pendingUrlParamsWriteService")
    private IPendingEntityWriteService<UrlRule> pendingUrlParamsWriteService;

    @Autowired
    @Qualifier("pendingServerWriteService")
    private IPendingEntityWriteService<Server> pendingServerWriteService;

    @Autowired
    @Qualifier("changesBatchWriteServiceOffline")
    private IChangesBatchWriteService pendingChangesBatchWriteService;

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    IStacksService stacksService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @POST
    @Path("{appName}/preview/distribution/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDistributionPendingPreview(@PathParam("appName") final String appName, Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        Distribution nextDistribution = nextDistributionEntityViewService.getEntity(snapshot.getPendingChanges(), snapshot.getDistribution());
        return Response.ok(nextDistribution).build();
    }

    @POST
    @Path("newRuleIds/{objectType}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNewRuleIds(@PathParam("objectType") final String objectType, Snapshot snapshot) {
        return Response.ok(new RuleIdsWrapper(PendingChangeStatusHelper.getNewRulesIds(objectType, snapshot.getPendingChanges()))).build();
    }

    @POST
    @Path("{appName}/distribution/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveAllPendingDistribution(@PathParam("appName") final String appName,  SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, EntityType.DISTRIBUTION);
        GenericOperationResult result;
        if (validationReport.isSuccessValidation()) {
           result = pendingDistributionWriteService.approve(appName, ApplicationStatusMode.OFFLINE);
            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }

        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/distribution/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingDistribution(@PathParam("appName") final String appName, Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingDistributionWriteService.cancel(appName, ApplicationStatusMode.OFFLINE, 0);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/stackmanagement/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveAllPendingWhitelists(@PathParam("appName") final String appName, SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, EntityType.WHITELIST);
        GenericOperationResult result;
        if (validationReport.isSuccessValidation()) {
            WhitelistedStackUpdates currentStackUpdates = snapshot.getWhitelistedStackUpdates();
            currentStackUpdates = whiteListStackUpdateService.getNewWhitelistedStatuses(currentStackUpdates, snapshot.getPendingChanges(), appName);
            result = pendingWhitelistedWriteService.approve(appName, ApplicationStatusMode.OFFLINE);
            ServicePaths servicePaths = stacksService.updateStacksForService(appName, snapshot.getServicePaths(), (Whitelisted) ((OperationResult) result).getApprovedEntity());

            ((OperationResult) result).addEntityToUpdate(currentStackUpdates);
            ((OperationResult) result).addEntityToUpdate(servicePaths);
            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }

        return Response.ok(result).build();
    }


    @POST
    @Path("{appName}/stackmanagement/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingWhitelisted(@PathParam("appName") final String appName, Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingWhitelistedWriteService.cancel(appName,  ApplicationStatusMode.OFFLINE, 0);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/flavorrule/{ruleId}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingRule(@PathParam("appName") final String appName,
                                       @PathParam("ruleId") final String ruleId,
                                       final SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, EntityType.RULE);
        OperationResult result;
        if (validationReport.isSuccessValidation()) {
            result = pendingFlavorRuleWriteService.approve(appName, ruleId, ApplicationStatusMode.OFFLINE);

            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/flavorrule/{ruleId}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingRule(@PathParam("appName") final String appName,
                                      @PathParam("ruleId") final String ruleId,
                                      Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult operationResult = pendingFlavorRuleWriteService.cancel(appName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(operationResult).build();
    }

    @POST
    @Path("{appName}/urlrule/{ruleId}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingUrlRule(@PathParam("appName") final String appName,
                                          @PathParam("ruleId") final String ruleId,
                                          final SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, EntityType.URL_RULE);
        OperationResult result;
        if (validationReport.isSuccessValidation()) {
            result = pendingUrlRuleWriteService.approve(appName, ruleId, ApplicationStatusMode.OFFLINE);

            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }

        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/urlrule/{ruleId}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingUrlRule(@PathParam("appName") final String appName,
                                         @PathParam("ruleId") final String ruleId,
                                         Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult operationResult = pendingUrlRuleWriteService.cancel(appName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(operationResult).build();
    }

    @POST
    @Path("{appName}/templateUrlRule/{ruleId}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveTemplateUrlPendingRule(@PathParam("appName") final String appName,
                                                  @PathParam("ruleId") final String ruleId,
                                                  final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingTemplateUrlRuleWriteService.approve(appName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/templateUrlRule/{ruleId}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelTemplateUrlPendingRule(@PathParam("appName") final String appName,
                                                 @PathParam("ruleId") final String ruleId,
                                                 final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingTemplateUrlRuleWriteService.cancel(appName, ruleId,  ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/templateRule/{ruleId}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approveTemplatePendingRule(@PathParam("appName") final String appName,
                                               @PathParam("ruleId") final String ruleId,
                                               final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingTemplateFlavorRuleWriteService.approve(appName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/templateRule/{ruleId}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelTemplatePendingRule(@PathParam("appName") final String appName,
                                              @PathParam("ruleId") final String ruleId,
                                              final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingTemplateFlavorRuleWriteService.cancel(appName, ruleId, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/urlParams/{ruleName}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingUrlParams(@PathParam("appName") final String appName,
                                            @PathParam("ruleName") final String ruleName,
                                            final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingUrlParamsWriteService.approve(appName, ruleName, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/urlParams/{ruleName}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingUrlParams(@PathParam("appName") final String appName,
                                           @PathParam("ruleName") final String ruleName,
                                           final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingUrlParamsWriteService.cancel(appName, ruleName, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/server/{serverName}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingServer(@PathParam("appName") final String appName,
                                         @PathParam("serverName") final String serverName,
                                         final SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, EntityType.SERVER);
        OperationResult result;
        if (validationReport.isSuccessValidation()) {
            result = pendingServerWriteService.approve(appName, serverName, ApplicationStatusMode.OFFLINE);

            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/server/{serverName}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingServer(@PathParam("appName") final String appName,
                                        @PathParam("serverName") final String serverName,
                                        final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        OperationResult result = pendingServerWriteService.cancel(appName, serverName, ApplicationStatusMode.OFFLINE);
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response approvePendingChanges(@PathParam("appName") final String appName,
                                          final SnapshotList snapshotList) {
        validateSnapshotList(snapshotList);
        Snapshot snapshot = snapshotList.getItems().iterator().next();
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);

        RedirectorConfig redirectorConfig = snapshotList.getConfig();
        ValidationReport validationReport = getValidationReport(appName, snapshot, redirectorConfig, null);
        PendingChangesBatchOperationResult result;
        if (validationReport.isSuccessValidation()) {
            result = pendingChangesBatchWriteService.approve(appName, ApplicationStatusMode.OFFLINE);
            if (Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationReport.getValidationResultType()) {
                ErrorMessage validationResult = new ErrorMessage();
                validationResult.setMessage(validationReport.getMessage());
                result.setErrorMessage(validationResult);
            }
        } else {
            throw new WebApplicationException("Validation failed (not able to redirect) " + validationReport.getMessage(), Response.Status.BAD_REQUEST);
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("{appName}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelPendingPendingChanges(@PathParam("appName") final String appName,
                                                final Snapshot snapshot) {
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
        return Response.ok(pendingChangesBatchWriteService.cancel(appName, ApplicationStatusMode.OFFLINE)).build();
    }

    private void validateSnapshotList(SnapshotList snapshotList) {
        if (snapshotList == null || CollectionUtils.isEmpty(snapshotList.getItems())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("SnapshotList is not correct: " + snapshotList)).build());
        }
    }

    private ValidationReport getValidationReport(String serviceName, Snapshot snapshot, RedirectorConfig redirectorConfig, EntityType entityType) {
        SelectServer currentFlavorRules = snapshot.getFlavorRules();
        Distribution currentDistribution = Distribution.newInstance(snapshot.getDistribution());
        Whitelisted currentWhitelisted = snapshot.getWhitelist();
        Server currentDefaultServer = snapshot.getServers();
        currentDistribution.setDefaultServer(currentDefaultServer);
        currentFlavorRules.setDistribution(currentDistribution);
        Serializer jsonSerializer = new JsonSerializer();
        StackBackup stackBackup = null;
        try {
            stackBackup = jsonSerializer.deserialize(snapshot.getStackBackup(), StackBackup.class);
        } catch (SerializerException e) {
            log.error("failed to deserialize stack backup", e);
        }
        Set<StackData> stackDataSet = stackBackup.getAllStacks();
        return pendingChangesService.validateModelBeforeApprove(serviceName, entityType, snapshot.getPendingChanges(), currentFlavorRules, currentDistribution, currentWhitelisted, stackDataSet, currentDefaultServer, redirectorConfig);
    }

}
