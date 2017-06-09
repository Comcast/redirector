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


package com.comcast.redirector.api.redirector.service.ruleengine;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

public abstract class BaseFlavorRulesService implements IFlavorRulesService {
    private static final Logger log = LoggerFactory.getLogger(BaseFlavorRulesService.class);

    protected IStacksService stacksService;

    protected IListServiceDAO<IfExpression> flavorRulesDAO;

    protected IChangesStatusService pendingChangesService;

    protected IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService;

    @Autowired
    private LockHelper lockHelper;

    public BaseFlavorRulesService() {
    }

    public BaseFlavorRulesService(IStacksService stacksService, IListServiceDAO<IfExpression> flavorRulesDAO,
                                  IChangesStatusService pendingChangesService,
                                  IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService) {
        this.stacksService = stacksService;
        this.flavorRulesDAO = flavorRulesDAO;
        this.pendingChangesService = pendingChangesService;
        this.pendingFlavorRuleWriteService = pendingFlavorRuleWriteService;
    }

    @Override
    public Collection<IfExpression> getRules(String serviceName) {
        return flavorRulesDAO.getAll(serviceName);
    }

    @Override
    public Map<String, IfExpression> getRulesInMap(String serviceName) {
        return flavorRulesDAO.getAllInMap(serviceName);
    }

    @Override
    public SelectServer getAllRules(String serviceName) {
        SelectServer selectServer = new SelectServer();
        Collection<IfExpression> expressions = getRules(serviceName);
        selectServer.setItems(expressions);
        return selectServer;
    }

    @Override
    public Collection<String> getRuleIdsByServiceName(String serviceName) {
        return getRuleIdsByServiceName(getRules(serviceName));
    }

    private Collection<String> getRuleIdsByServiceName(Collection<IfExpression> rules) {
        return Collections2.transform(rules, new Function<IfExpression, String>() {
            @Override
            public String apply(IfExpression input) {
                return input.getId();
            }
        });
    }

    @Override
    public IfExpression getRule(String serviceName, String ruleId) {
        return flavorRulesDAO.getById(serviceName, ruleId);
    }

    @Override
    public OperationResult deleteTemplateRule(String appName, String ruleId) {
        return new OperationResult();
    }

    @Override
    public synchronized void deleteRule(String serviceName, String ruleId) {
        SharedInterProcessLock lock = lockHelper.getLock(serviceName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                // check for existing rule
                deleteRule(serviceName, ruleId, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    public OperationResult deleteRule(String serviceName, String ruleId, ApplicationStatusMode mode) {
        IfExpression currentRule = OperationContextHolder.getCurrentContext().getFlavorRule(ruleId);
        PendingChangesStatus pendingChangesStatus = OperationContextHolder.getCurrentContext().getPendingChangesStatus();

        validateIfRuleExists(ruleId, currentRule, pendingChangesStatus);

        OperationResult operationResult;
        if (currentRule != null) {
            operationResult = pendingFlavorRuleWriteService.save(serviceName, ruleId, null, currentRule, mode);
        } else {
            // if admin added rule and decided to remove it right away it's the same as cancel pending addition
            operationResult = pendingFlavorRuleWriteService.cancel(serviceName, ruleId, mode);
        }
        return operationResult;
    }

    @Override
    public synchronized void saveRule(IfExpression rule, String serviceName) {
        SharedInterProcessLock lock = lockHelper.getLock(serviceName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                saveRule(serviceName, rule, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public synchronized OperationResult saveRule(String serviceName, IfExpression pendingRule, ApplicationStatusMode mode) {
        validate(serviceName, pendingRule);

        String ruleName = pendingRule.getId();
        IfExpression currentRule = OperationContextHolder.getCurrentContext().getFlavorRule(ruleName);
        return pendingFlavorRuleWriteService.save(serviceName, ruleName, pendingRule, currentRule, mode);
    }

    private void validateIfRuleExists(String ruleId, IfExpression rule, PendingChangesStatus pendingChangesStatus) {
        if (rule == null && !(pendingChangesStatus != null && getRule(pendingChangesStatus).containsKey(ruleId))) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // rule not found
        }
    }
    private void validate(String serviceName, IfExpression rule) {
        try {
            ModelValidationFacade.validateFlavorRule(rule, OperationContextHolder.getCurrentContext(), getEntityType());
        } catch (ExpressionValidationException e) {
            String error = String.format("Failed to save rule '%s' for %s application due to validation error(s). %s",  rule.getId(), serviceName, e.getMessage());
            throw new WebApplicationException(error, e, Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build());
        }
    }
}

