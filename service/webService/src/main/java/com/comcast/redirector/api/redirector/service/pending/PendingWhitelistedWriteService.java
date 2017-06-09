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

package com.comcast.redirector.api.redirector.service.pending;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingWhitelistedDiffHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import com.comcast.redirector.dataaccess.dao.ITransactionalDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class PendingWhitelistedWriteService implements IPendingSingletonEntityWriteService<Whitelisted> {

    @Autowired
    private ISimpleServiceDAO<Whitelisted> whitelistDAO;

    @Autowired
    private ITransactionalDAO transactionalDAO;

    private IChangesStatusService pendingChangesService;

    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    private IWhiteListStackUpdateService whiteListStackUpdateService;

    @Autowired
    private IStacksService stacksService;

    public void setPendingChangesService(IChangesStatusService pendingChangesService) {
        this.pendingChangesService = pendingChangesService;
    }

    public void setNextWhitelistedEntityViewService(IEntityViewService<Whitelisted> nextWhitelistedEntityViewService) {
        this.nextWhitelistedEntityViewService = nextWhitelistedEntityViewService;
    }

    public void setWhiteListStackUpdateService(IWhiteListStackUpdateService whiteListStackUpdateService) {
        this.whiteListStackUpdateService = whiteListStackUpdateService;
    }

    @Override
    public void cancel(String serviceName, int version) {
        PendingChangesStatus pendingChangesStatus = pendingChangesService.getPendingChangesStatus(serviceName, version);
        cancel(serviceName, ApplicationStatusMode.ONLINE, version);
    }

    @Override
    public OperationResult cancel(String serviceName, ApplicationStatusMode mode, int version) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        pendingChangesStatus.getWhitelisted().clear();

        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
        }

        return new OperationResult(pendingChangesStatus);
    }

    @Override
    public void approve(String serviceName, int version) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        WhitelistedStackUpdates stackUpdates = currentContext.getWhitelistedStackUpdates();

        whiteListStackUpdateService.getNewWhitelistedStatuses(stackUpdates, pendingChangesStatus, serviceName);

        approve(serviceName, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult approve(String serviceName, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        Whitelisted current = currentContext.getWhitelist();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        WhitelistedStackUpdates stackUpdates = currentContext.getWhitelistedStackUpdates();

        Whitelisted whitelisted = nextWhitelistedEntityViewService.getEntity(pendingChangesStatus, current);

        pendingChangesStatus.getWhitelisted().clear();

        if (mode == ApplicationStatusMode.ONLINE) {
            try {
                ITransactionalDAO.ITransaction transaction = transactionalDAO.beginTransaction();
                transaction.save(whitelisted, EntityType.WHITELIST, serviceName);
                transaction.save(pendingChangesStatus, pendingChangesService.getPendingChangeType(), serviceName);
                transaction.incVersion(EntityType.MODEL_CHANGED, serviceName);
                transaction.save(stackUpdates, EntityType.WHITELIST_UPDATES, serviceName);
                transaction.commit();
            } catch (SerializerException e)  {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorMessage(e.getMessage())).build());
            }
        }

        return new OperationResult(pendingChangesStatus, whitelisted);
    }

    @Override
    public void save(String serviceName, Whitelisted pending, Whitelisted current) {
        save(serviceName, pending, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult save(String serviceName, Whitelisted pending, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        Whitelisted current = currentContext.getWhitelist();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        pendingChangesStatus.setWhitelisted(PendingWhitelistedDiffHelper.getWhitelistedDiff(pending, current));
        if (mode == ApplicationStatusMode.ONLINE) {
            pendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);
        }
        return new OperationResult(pendingChangesStatus, pending);
    }

    public Whitelisted getWhitelistedStacks(String serviceName) {
        Whitelisted whitelisted = whitelistDAO.get(serviceName);
        return whitelisted != null ? whitelisted: new Whitelisted();
    }
}
