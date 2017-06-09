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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.DistributionWithDefaultAndFallbackServers;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingDistributionDiffHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class DistributionsWithDefaultService implements IDistributionsWithDefaultService {

    private static final Logger log = LoggerFactory.getLogger(DistributionsWithDefaultService.class);

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Autowired
    private LockHelper lockHelper;

    @Override
    public OperationResult saveDistribution(final String serviceName, DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers) {
        PendingChangesStatus status = validateDistributionWithDefaultServer(serviceName, distributionWithDefaultAndFallbackServers);
        return new OperationResult(status, distributionWithDefaultAndFallbackServers);
    }

    @Override
    public synchronized void save(final String serviceName, DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers) {
        SharedInterProcessLock lock = lockHelper.getLock(serviceName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                PendingChangesStatus status = validateDistributionWithDefaultServer(serviceName, distributionWithDefaultAndFallbackServers);
                changesStatusService.savePendingChangesStatus(serviceName, status);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    private PendingChangesStatus validateDistributionWithDefaultServer(final String serviceName, DistributionWithDefaultAndFallbackServers distributionWithDefaultAndFallbackServers) {
        Distribution currentDistribution = OperationContextHolder.getCurrentContext().getDistribution();
        PendingChangesStatus pendingChangesStatus = OperationContextHolder.getCurrentContext().getPendingChangesStatus();

        distributionService.validateDistribution(serviceName, distributionWithDefaultAndFallbackServers.getDistribution());
        pendingChangesStatus.setDistributions(
            PendingDistributionDiffHelper.getDistributionsDiff(distributionWithDefaultAndFallbackServers.getDistribution(), currentDistribution)
        );

        Server pendingDefaultServer = distributionWithDefaultAndFallbackServers.getDefaultServer();
        if (pendingDefaultServer.getPath() == null) {
            return pendingChangesStatus;
        }

        Server currentDefaultServer = OperationContextHolder.getCurrentContext().getServer();
        if (currentDefaultServer == null || !pendingDefaultServer.getPath().equals(currentDefaultServer.getPath())) {
            try {
                ModelValidationFacade.validateServer(pendingDefaultServer, OperationContextHolder.getCurrentContext());
            } catch (ExpressionValidationException e) {
                String error = String.format("Failed to save server '%s' for %s application due to validation error(s). %s", pendingDefaultServer.getName(), serviceName, e.getMessage());
                throw new WebApplicationException(error, e, Response.Status.BAD_REQUEST);
            }
            ActionType changeType = PendingChangeStatusHelper.getActionType(EntityType.SERVER, pendingDefaultServer, currentDefaultServer);
            pendingChangesStatus = PendingChangeStatusHelper
                    .updatePendingChangesStatus(pendingChangesStatus, changeType, EntityType.SERVER, RedirectorConstants.DEFAULT_SERVER_NAME, pendingDefaultServer, currentDefaultServer);
        }
        return pendingChangesStatus;
    }
}
