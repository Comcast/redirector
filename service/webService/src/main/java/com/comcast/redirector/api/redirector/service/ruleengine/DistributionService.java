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
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
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

@Service
public class DistributionService implements IDistributionService {
    private static final Logger log = LoggerFactory.getLogger(DistributionService.class);

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Autowired
    private IPendingSingletonEntityWriteService<Distribution> pendingDistributionWriteService;

    @Autowired
    private ISimpleServiceDAO<Distribution> distributionDAO;

    @Autowired
    private LockHelper lockHelper;

    public void setChangesStatusService(IChangesStatusService changesStatusService) {
        this.changesStatusService = changesStatusService;
    }

    @Override
    public synchronized void saveDistribution(Distribution distribution, String serviceName) {
        SharedInterProcessLock lock = lockHelper.getLock(serviceName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                validateDistribution(serviceName, distribution);

                log.info("Saving pending distribution for application {}", serviceName);
                pendingDistributionWriteService.save(serviceName, distribution, getDistribution(serviceName));
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public OperationResult saveDistribution(String appName, Distribution distribution) {
        validateDistribution(appName, distribution);
        return pendingDistributionWriteService.save(appName, distribution, ApplicationStatusMode.OFFLINE);
    }

    @Override
    public void validateDistribution(String serviceName, Distribution distribution) {
        try {
            // validate distribution
            ModelValidationFacade.validateDistribution(distribution, OperationContextHolder.getCurrentContext());
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to save distribution for %s application due to validation error(s). %s",  serviceName, ex.getMessage());
            log.error(error, ex);
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        }
    }

    @Override
    public Distribution getDistribution(String serviceName) {
        Distribution distribution = distributionDAO.get(serviceName);
        return distribution != null ? distribution : new Distribution();
    }
}
