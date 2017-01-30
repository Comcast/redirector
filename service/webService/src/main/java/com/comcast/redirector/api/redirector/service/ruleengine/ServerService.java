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

package com.comcast.redirector.api.redirector.service.ruleengine;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class ServerService implements IServerService {
    private static final Logger log = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    private IListServiceDAO<Server> serverDAO;

    @Autowired
    @Qualifier("pendingServerWriteService")
    private IPendingEntityWriteService<Server> pendingServerWriteService;

    @Autowired
    private LockHelper lockHelper;

    @Override
    public synchronized void saveServer(String appName, Server server) {
        SharedInterProcessLock lock = lockHelper.getLock(appName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                saveServer(appName, server, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public OperationResult saveServer(String appName, Server server, ApplicationStatusMode mode) {
        final OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        validateServer(appName, server);
        return pendingServerWriteService.save(appName, RedirectorConstants.DEFAULT_SERVER_NAME, server, currentContext.getServer(), mode);
    }

    @Override
    public Server getServer(String appName) {
        return serverDAO.getById(appName, RedirectorConstants.DEFAULT_SERVER_NAME);
    }

    private void validateServer(String appName, Server server) {
        try {
            ModelValidationFacade.validateServer(server, OperationContextHolder.getCurrentContext());
        } catch (ExpressionValidationException e) {
            String error = String.format("Failed to save server '%s' for %s application due to validation error(s). %s",  appName, RedirectorConstants.DEFAULT_SERVER_NAME, e.getMessage());
            throw new WebApplicationException(error, e, Response.Status.BAD_REQUEST);
        }
    }

}
