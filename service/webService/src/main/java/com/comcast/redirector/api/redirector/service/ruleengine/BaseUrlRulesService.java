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
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.common.RedirectorConstants;
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

public abstract class BaseUrlRulesService implements IUrlRulesService {
    private static final Logger log = LoggerFactory.getLogger(BaseUrlRulesService.class);

    protected IListServiceDAO<IfExpression> urlRulesDAO;

    protected IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService;

    protected IChangesStatusService pendingChangesService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Autowired
    private LockHelper lockHelper;

    public BaseUrlRulesService() {
    }

    public BaseUrlRulesService(IListServiceDAO<IfExpression> urlRulesDAO, IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService, IChangesStatusService pendingChangesService) {
        this.urlRulesDAO = urlRulesDAO;
        this.pendingUrlRuleWriteService = pendingUrlRuleWriteService;
        this.pendingChangesService = pendingChangesService;
    }

    @Override
    public Collection<IfExpression> getUrlRules(String appName) {
        return urlRulesDAO.getAll(appName);
    }

    @Override
    public Map<String, IfExpression> getAllRulesInMap(String appName) {
        return urlRulesDAO.getAllInMap(appName);
    }

    @Override
    public URLRules getAllRules(String appName) {
        URLRules urlRules = new URLRules();

        // 1. get url rules
        Collection<IfExpression> expressions = getUrlRules(appName);
        urlRules.setItems(expressions);

        // 2. get default url rule for application
        Default defaultStatement = new Default();
        UrlRule urlParams = urlParamsService.getUrlParams(appName);
        if (urlParams == null) {
            defaultStatement.setUrlRule(new UrlRule());
        } else {
            defaultStatement.setUrlRule(urlParams);
        }

        // 3. append default url rule to the result
        urlRules.setDefaultStatement(defaultStatement);

        return urlRules;
    }

    @Override
    public IfExpression getUrlRule(String appName, String ruleId) {
        return urlRulesDAO.getById(appName, ruleId);
    }

    @Override
    public synchronized void deleteUrlRule(String appName, String ruleId) {
        SharedInterProcessLock lock = lockHelper.getLock(appName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                deleteUrlRule(appName, ruleId, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    public OperationResult deleteUrlRule(String appName, String ruleId, ApplicationStatusMode mode) {
        final OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        IfExpression current = currentContext.getUrlRule(ruleId);
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        // check for existing rule
        if (current == null && !(pendingChangesStatus != null && getUrlRules(pendingChangesStatus).containsKey(ruleId))) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // rule not found
        }

        if (current != null) {
            return pendingUrlRuleWriteService.save(appName, ruleId, null, current, mode);
        } else {
            // if admin added rule and decided to remove it right away it's the same as cancel pending addition
            return pendingUrlRuleWriteService.cancel(appName, ruleId, mode);
        }
    }

    @Override
    public OperationResult deleteTemplateUrlRule(String serviceName, String ruleId) {
        return new OperationResult();
    }

    @Override
    public synchronized void saveUrlRule(String appName, IfExpression rule, String ruleId) {
        SharedInterProcessLock lock = lockHelper.getLock(appName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                saveUrlRule(appName, rule, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public OperationResult saveUrlRule(String serviceName, IfExpression pendingRule, ApplicationStatusMode mode) {
        String ruleId = pendingRule.getId();
        PendingChangesStatus pendingChangesStatus = OperationContextHolder.getCurrentContext().getPendingChangesStatus();
        IfExpression currentRule = OperationContextHolder.getCurrentContext().getUrlRule(ruleId);

        validate(pendingRule, serviceName, pendingChangesStatus, ruleId);
        return pendingUrlRuleWriteService.save(serviceName, ruleId, pendingRule, currentRule, mode);
    }

    private void validate(IfExpression rule, String serviceName, PendingChangesStatus pendingChangesStatus, String ruleId) {
        try {
            ModelValidationFacade.validateUrlRule(rule, pendingChangesStatus, getEntityType());
        } catch (ExpressionValidationException e) {
            String error = String.format("Failed to save URL rule '%s' for %s application due to validation error(s). %s",  ruleId, serviceName, e.getMessage());
            throw new WebApplicationException(error, e, Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build());
        }
    }


    @Override
    public Collection<String> getUrlRuleIdsByServiceName(String appName) {
        Collection<IfExpression> rules = urlRulesDAO.getAll(appName);
        return Collections2.transform(rules, new Function<IfExpression, String>() {
            @Override
            public String apply(IfExpression input) {
                return input.getId();
            }
        });
    }
}
