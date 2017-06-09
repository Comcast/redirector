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
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ITransactionalDAO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.concurrent.locks.ReentrantLock;

public class PendingEntityWriteService<T extends Expressions> implements IPendingEntityWriteService<T> {
    private static final ReentrantLock pendingChangesOverwriteLock = new ReentrantLock();

    @Autowired
    private ITransactionalDAO transactionalDAO;

    private IChangesStatusService pendingChangesService;

    private EntityType entityType;

    public PendingEntityWriteService(EntityType entityType) {
        this.entityType = entityType;
    }

    public PendingEntityWriteService(EntityType entityType, IChangesStatusService pendingChangesService) {
        this.pendingChangesService = pendingChangesService;
        this.entityType = entityType;
    }

    @Override
    public void cancel(String appName, String id, int version) {
        cancel(appName, id, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult cancel(String appName, String id, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        PendingChangeStatusHelper.removePendingChangeByIdAndType(pendingChangesStatus, id, entityType);
        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(appName, pendingChangesStatus);
        }
        return new OperationResult(pendingChangesStatus);
    }

    @Override
    public void approve(String serviceName, String id, int version) {
        approve(serviceName, id, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult approve(String serviceName, String id,  ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        PendingChange pendingChange = PendingChangeStatusHelper.removePendingChangeByIdAndType(pendingChangesStatus, id, entityType);

        if (mode == ApplicationStatusMode.ONLINE) {
            ITransactionalDAO.ITransaction transaction = transactionalDAO.beginTransaction();
            if (pendingChange == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("You are trying to approve changes for non-existing entity")).build());
            }
            try {
                transaction.<T>operationByActionType((T) pendingChange.getChangedExpression(), entityType, serviceName, id, pendingChange.getChangeType());
                transaction.save(pendingChangesStatus, pendingChangesService.getPendingChangeType(), serviceName);
            } catch (SerializerException e) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorMessage(e.getMessage())).build());
            }
            transaction.incVersion(EntityType.MODEL_CHANGED, serviceName);
            transaction.commit();
        }

        return new OperationResult(pendingChangesStatus, (T)pendingChange.getChangedExpression());
    }

    public void save(String serviceName, String id, T pending, T current) {
        save(serviceName, id, pending, current, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult save(String serviceName, String id, T pending, T current, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        pendingChangesOverwriteLock.lock();
        try {
            ActionType changeType = PendingChangeStatusHelper.getActionType(entityType, pending, current);
            pendingChangesStatus = PendingChangeStatusHelper.updatePendingChangesStatus(pendingChangesStatus, changeType, entityType, id, pending, current);
            if (mode == ApplicationStatusMode.ONLINE) {
                pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
            }
            return new OperationResult(pendingChangesStatus, pending);
        } finally {
            pendingChangesOverwriteLock.unlock();
        }
    }
}
