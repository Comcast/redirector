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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.pending.IPendingSingletonEntityWriteService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class WhiteListService implements IWhiteListService {
    private static Logger log = LoggerFactory.getLogger(WhiteListService.class);

    @Autowired
    private ISimpleServiceDAO<Whitelisted> whitelistDAO;

    private IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService;

    @Autowired
    private LockHelper lockHelper;


    public void setPendingWhitelistedWriteService(IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService) {
        this.pendingWhitelistedWriteService = pendingWhitelistedWriteService;
    }

    @Override
    public Whitelisted getWhitelistedStacks(String serviceName) {
        Whitelisted whitelisted = whitelistDAO.get(serviceName);
        return whitelisted != null ? whitelisted: new Whitelisted();
    }

    @Override
    public Whitelisted addWhitelistedStacks(String serviceName, Whitelisted whitelisted) {
        Whitelisted currentWhitelisted = getWhitelistedStacks(serviceName);
        if (currentWhitelisted.getPaths() == null) {
            currentWhitelisted.setPaths(new ArrayList<String>());
        }
        for (String path: whitelisted.getPaths()) {
            if (!currentWhitelisted.getPaths().contains(path)) {
                currentWhitelisted.getPaths().add(path);
            }
        }
        saveWhitelistedStacks(currentWhitelisted, serviceName);

        return whitelisted;
    }

    @Override
    public void deleteWhitelistedStacks(String serviceName, String values) {
        Whitelisted whitelisted = getWhitelistedStacks(serviceName);
        if (whitelisted.getPaths() == null) {
            whitelisted.setPaths(new ArrayList<String>());
        }
        List<String> whitelistedStacks = Arrays.asList(values.split(","));
        for (String whitelistedStack : whitelistedStacks) {
            whitelisted.getPaths().remove(whitelistedStack);
        }
        saveWhitelistedStacks(whitelisted, serviceName);
    }

    @Override
    public synchronized void saveWhitelistedStacks(Whitelisted pending, String serviceName) {
        SharedInterProcessLock lock = lockHelper.getLock(serviceName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");

            if (lock.acquire()) {
                log.info("Saving pending whitelisted for application {}", serviceName);

                saveWhitelistedStacks(serviceName, pending, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public OperationResult saveWhitelistedStacks(String appName, Whitelisted pendingWhitelist, ApplicationStatusMode mode) {
        validate(appName, pendingWhitelist);
        return pendingWhitelistedWriteService.save(appName, pendingWhitelist, mode);
    }

    private void validate(String serviceName, Whitelisted pending) {
        try {

            ModelValidationFacade.validateWhitelistedStacks(pending, OperationContextHolder.getCurrentContext());
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to save whitelisted stacks for %s application due to validation error(s). %s",  serviceName, ex.getMessage());
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        }
    }
}
