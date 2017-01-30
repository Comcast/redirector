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
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.url.rule.Default;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class UrlParamsService implements IUrlParamsService {

    private static final Logger log = LoggerFactory.getLogger(UrlParamsService.class);

    @Autowired
    private IListServiceDAO<UrlRule> urlParamsDAO;

    @Autowired
    @Qualifier("pendingUrlParamsWriteService")
    private IPendingEntityWriteService<UrlRule> pendingUrlParamsWriteService;

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    private LockHelper lockHelper;

    @Override
    public void saveUrlParams(String appName, UrlRule urlRule, String urlParamsRuleName) {
        SharedInterProcessLock lock = lockHelper.getLock(appName, EntityType.PENDING_CHANGES_STATUS);
        try {
            log.info("Acquiring lock for pending changes");
            if (lock.acquire()) {
                saveUrlParams(appName, urlRule, urlParamsRuleName, ApplicationStatusMode.ONLINE);
            } else {
                throw new WebApplicationException("Lock timeout expired", Response.Status.BAD_REQUEST);
            }
        } finally {
            lock.release();
            log.info("Lock for pending changes is released");
        }
    }

    @Override
    public OperationResult saveUrlParams(String appName, UrlRule urlRule, String urlParamsRuleName, ApplicationStatusMode mode) {
        final OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        validate(appName, urlRule);
        return pendingUrlParamsWriteService.save(appName,
                urlParamsRuleName,
                urlRule,
                currentContext.getUrlRules().getDefaultStatement().getUrlRule(),
                mode);
    }

    public void validate(String serviceName, UrlRule defaultUrlParams) {
        try {
            ModelValidationFacade.validateUrlParams(defaultUrlParams, EntityType.URL_PARAMS);
        } catch (ExpressionValidationException e) {
            String error = String.format("Failed to save Default URL params for %s application due to validation error(s). %s",  serviceName, e.getMessage());
            throw new WebApplicationException(error, e, Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build());
        }
    }

    @Override
    public UrlRule getUrlParams(String appName) {
        return urlParamsDAO.getById(appName, RedirectorConstants.DEFAULT_URL_RULE);
    }

    @Override
    public Default getDefaultUrlParams(String appName) {
        Default defaultStatement = new Default();

        UrlRule urlParams = getUrlParams(appName);
        if (urlParams == null) {
            defaultStatement.setUrlRule(new UrlRule());
        } else {
            defaultStatement.setUrlRule(urlParams);
        }

        return defaultStatement;
    }
}
