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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingDistributionDiffHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import com.comcast.redirector.dataaccess.dao.ITransactionalDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class PendingDistributionWriteService implements IPendingSingletonEntityWriteService<Distribution> {

    @Autowired
    private ITransactionalDAO transactionalDAO;

    private IChangesStatusService pendingChangesService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private ISimpleServiceDAO<Distribution> distributionDAO;

    public void setPendingChangesService(IChangesStatusService pendingChangesService) {
        this.pendingChangesService = pendingChangesService;
    }

    @Autowired
    @Qualifier ("changesBatchWriteService")
    private IChangesBatchWriteService changesBatchWriteService;

    @Override
    public void cancel(String serviceName, int version) {
        PendingChangesStatus pendingChangesStatus = pendingChangesService.getPendingChangesStatus(serviceName, version);
        pendingChangesStatus.getDistributions().clear();
        pendingChangesStatus.getServers().clear();
        pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
    }

    @Override
    public OperationResult cancel(String serviceName, ApplicationStatusMode mode, int version) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        pendingChangesStatus.getDistributions().clear();
        pendingChangesStatus.getServers().clear();
        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
        }
        return new OperationResult(pendingChangesStatus);
    }

    @Override
    public void approve(String appName, int version) {
        approve(appName, ApplicationStatusMode.ONLINE);
    }

    @Override
    public PendingChangesBatchOperationResult approve(String appName, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        WhitelistedStackUpdates stackUpdates = currentContext.getWhitelistedStackUpdates();

        PendingChangesBatchOperationResult batchResult = new PendingChangesBatchOperationResult();
        ITransactionalDAO.ITransaction transaction = null;
        if (mode == ApplicationStatusMode.ONLINE) {
            transaction = transactionalDAO.beginTransaction();
        }

        batchResult.setServers(changesBatchWriteService.approveServer(appName, transaction, mode));
        batchResult.setDistribution(changesBatchWriteService.approveDistributions(appName, transaction, mode));

        pendingChangesStatus.getServers().clear();
        pendingChangesStatus.getDistributions().clear();

        batchResult.setPendingChangesStatus(pendingChangesStatus);
        if (mode == ApplicationStatusMode.ONLINE) {
            try {
                transaction.save(pendingChangesStatus, pendingChangesService.getPendingChangeType(), appName);
            } catch (SerializerException e) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorMessage(e.getMessage())).build());
            }
            transaction.incVersion(EntityType.MODEL_CHANGED, appName);
            transaction.commit();
        }

        return batchResult;
    }

    @Override
    public void save(String serviceName, Distribution pending, Distribution current) {
        save(serviceName, pending,  ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult save(String appName, Distribution pending, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        Distribution current = currentContext.getDistribution();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        pendingChangesStatus.setDistributions(PendingDistributionDiffHelper.getDistributionsDiff(pending, current));
        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(appName, pendingChangesStatus);
        }
        return new OperationResult(pendingChangesStatus, pending);
    }

    public Distribution getDistribution(String serviceName) {
        Distribution distribution = distributionDAO.get(serviceName);
        return distribution != null ? distribution : new Distribution();
    }
}
