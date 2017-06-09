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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.BatchChange;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ITransactionalDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;

@Service
public class ChangesBatchWriteService implements IChangesBatchWriteService {
    @Autowired
    private ITransactionalDAO transactionalDAO;

    private IChangesStatusService pendingChangesService;

    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    public void setPendingChangesService(IChangesStatusService pendingChangesService) {
        this.pendingChangesService = pendingChangesService;
    }

    public void setNextDistributionEntityViewService(IEntityViewService<Distribution> nextDistributionEntityViewService) {
        this.nextDistributionEntityViewService = nextDistributionEntityViewService;
    }

    public void setNextWhitelistedEntityViewService(IEntityViewService<Whitelisted> nextWhitelistedEntityViewService) {
        this.nextWhitelistedEntityViewService = nextWhitelistedEntityViewService;
    }

    public IWhiteListStackUpdateService getWhiteListStackUpdateService() {
        return whiteListStackUpdateService;
    }

    @Override
    public void approve(String appName, int version) {
        approve(appName, ApplicationStatusMode.ONLINE);
    }

    @Override
    public PendingChangesBatchOperationResult approve(String appName, ApplicationStatusMode mode) {

        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        Whitelisted currentWhitelist = currentContext.getWhitelist();
        WhitelistedStackUpdates whitelistedStackUpdates = currentContext.getWhitelistedStackUpdates();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();


        PendingChangesBatchOperationResult batchResult = new PendingChangesBatchOperationResult();
        ITransactionalDAO.ITransaction transaction = transactionalDAO.beginTransaction();

        batchResult.setPathRules(approveFlavorRules(transaction, pendingChangesStatus, appName, mode));
        batchResult.setUrlRules(approveUrlRules(transaction, pendingChangesStatus, appName, mode));
        batchResult.setTemplatePathRules(approveTemplateFlavorRules(transaction, pendingChangesStatus, appName, mode));
        batchResult.setTemplateUrlRules(approveTemplateUrlRules(transaction, pendingChangesStatus, appName, mode));
        batchResult.setServers(approveServer(appName,transaction, mode));
        batchResult.setDefaultUrlParams(approveUrlParams(transaction, pendingChangesStatus, appName, mode));
        batchResult.setDistribution(approveDistributions(appName, transaction,  mode));
        batchResult.setWhitelist(approveWhitelisted(transaction, currentWhitelist, whitelistedStackUpdates, pendingChangesStatus, appName, mode));
        batchResult.setPendingChangesStatus(new PendingChangesStatus());

        if (mode == ApplicationStatusMode.ONLINE) {
            try {
                transaction.save(new PendingChangesStatus(), pendingChangesService.getPendingChangeType(), appName);
            } catch (SerializerException e) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorMessage(e.getMessage())).build());
            }
            transaction.incVersion(EntityType.MODEL_CHANGED, appName);
            transaction.commit();
        }

        return batchResult;
    }

    private void addRuleToResult(BatchChange result, PendingChange change) {
        switch (change.getChangeType()) {
            case ADD:
            case UPDATE:
                result.addEntityToSave((Expressions)change.getChangedExpression());
                break;
            case DELETE:
                result.addEntityToDelete(change.getId());
                break;
        }
    }

    private BatchChange approveFlavorRules(ITransactionalDAO.ITransaction transaction,
                                    PendingChangesStatus pendingChangesStatus,
                                    String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getPathRules().entrySet()) {
            String ruleId = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getPathRules().get(ruleId);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<IfExpression>operationByActionType((IfExpression) change.getChangedExpression(), EntityType.RULE,
                            appName, ruleId, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    private BatchChange approveTemplateFlavorRules(ITransactionalDAO.ITransaction transaction,
                                    PendingChangesStatus pendingChangesStatus,
                                    String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getTemplatePathRules().entrySet()) {
            String ruleId = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getTemplatePathRules().get(ruleId);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<IfExpression>operationByActionType((IfExpression) change.getChangedExpression(), EntityType.TEMPLATE_RULE,
                            appName, ruleId, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    @Override
    public BatchChange approveServer(String appName, ITransactionalDAO.ITransaction transaction, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getServers().entrySet()) {
            String serverName = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getServers().get(serverName);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<Server>operationByActionType((Server) change.getChangedExpression(), EntityType.SERVER,
                            appName, serverName, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    private BatchChange approveUrlRules(ITransactionalDAO.ITransaction transaction,
                               PendingChangesStatus pendingChangesStatus,
                               String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getUrlRules().entrySet()) {
            String ruleId = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getUrlRules().get(ruleId);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<IfExpression>operationByActionType((IfExpression) change.getChangedExpression(), EntityType.URL_RULE,
                            appName, ruleId, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    private BatchChange approveTemplateUrlRules(ITransactionalDAO.ITransaction transaction,
                                 PendingChangesStatus pendingChangesStatus,
                                 String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getTemplateUrlPathRules().entrySet()) {
            String ruleId = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getTemplateUrlPathRules().get(ruleId);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<IfExpression>operationByActionType((IfExpression) change.getChangedExpression(), EntityType.TEMPLATE_URL_RULE,
                            appName, ruleId, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    private BatchChange approveUrlParams(ITransactionalDAO.ITransaction transaction,
                                  PendingChangesStatus pendingChangesStatus,
                                  String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        for (Map.Entry<String, PendingChange> changeEntry : pendingChangesStatus.getUrlParams().entrySet()) {
            String serverName = changeEntry.getKey();
            PendingChange change = pendingChangesStatus.getUrlParams().get(serverName);

            addRuleToResult(result, change);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.<UrlRule>operationByActionType((UrlRule) change.getChangedExpression(), EntityType.URL_PARAMS,
                            appName, serverName, change.getChangeType());
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    @Override
    public BatchChange approveDistributions(String appName, ITransactionalDAO.ITransaction transaction, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        Distribution current = currentContext.getDistribution();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        BatchChange result = new BatchChange();
        if (pendingChangesStatus.getDistributions().size() > 0) {
            Distribution distribution = nextDistributionEntityViewService.getEntity(pendingChangesStatus, current);
            result.addEntityToSave(distribution);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.save(distribution, EntityType.DISTRIBUTION, appName);
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    private BatchChange approveWhitelisted(ITransactionalDAO.ITransaction transaction,
                                           Whitelisted current,
                                           WhitelistedStackUpdates stackUpdates,
                                           PendingChangesStatus pendingChangesStatus,
                                           String appName, ApplicationStatusMode mode) {
        BatchChange result = new BatchChange();
        if (pendingChangesStatus.getWhitelisted().size() > 0) {
            Whitelisted whitelisted = nextWhitelistedEntityViewService.getEntity(pendingChangesStatus, current);
            WhitelistedStackUpdates whitelistUpdates = whiteListStackUpdateService.getNewWhitelistedStatuses(stackUpdates, pendingChangesStatus, appName);
            result.addEntityToSave(whitelisted);
            if (mode == ApplicationStatusMode.ONLINE) {
                try {
                    transaction.save(whitelisted, EntityType.WHITELIST, appName);
                    transaction.save(whitelistUpdates, EntityType.WHITELIST_UPDATES, appName);
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
        }
        return result;
    }

    @Override
    public void cancel(String serviceName, int version) {
        PendingChangesStatus pendingChangesStatus = pendingChangesService.getPendingChangesStatus(serviceName, version);
        pendingChangesStatus.clear();
        pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
    }

    @Override
    public OperationResult cancel(String appName, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        pendingChangesStatus.clear();
        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(appName, pendingChangesStatus);
        }
        return new OperationResult(pendingChangesStatus);
    }
}
