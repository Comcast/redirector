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
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class TemplateUrlRulesService extends BaseUrlRulesService {

    private IListServiceDAO<IfExpression> urlRulesDAOEntity;

    @Autowired
    public TemplateUrlRulesService(@Qualifier("templateUrlRulesDAO") IListServiceDAO<IfExpression> urlRulesDAO,
                                   @Qualifier("pendingTemplateUrlRuleWriteService") IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService,
                                   @Qualifier("changesStatusService") IChangesStatusService pendingChangesService,
                                   @Qualifier("urlRulesDAO") IListServiceDAO<IfExpression> urlRulesDAOEntity) {
        super(urlRulesDAO, pendingUrlRuleWriteService, pendingChangesService);
        this.urlRulesDAOEntity = urlRulesDAOEntity;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TEMPLATE_URL_RULE;
    }

    @Override
    public Map<String, PendingChange> getUrlRules(PendingChangesStatus pendingChangesStatus) {
        return pendingChangesStatus != null ? pendingChangesStatus.getTemplateUrlPathRules() : null;
    }

    @Override
    public void deleteUrlRule(String appName, String ruleId) {
        // check for existing rule
        IfExpression expression = getUrlRule(appName, ruleId);
        PendingChangesStatus pendingChangesStatus = pendingChangesService.getPendingChangesStatus(appName);
        List<IfExpression> rules = urlRulesDAOEntity.getAll(appName);
        rules.addAll(PendingChangeStatusHelper.getPendingUrlRules(pendingChangesStatus));
        deleteTemplateUrlRule(appName, ruleId, expression, rules, ApplicationStatusMode.ONLINE);
    }

    @Override
    public OperationResult deleteTemplateUrlRule(String appName, String ruleId) {
        final OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        final PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        URLRules templates = currentContext.getTemplateUrlRules();
        Collection<IfExpression> urlRules = currentContext.getUrlRules().getItems();

        boolean containsTemplates = (templates != null && CollectionUtils.isNotEmpty(templates.getItems()));
        IfExpression current =  containsTemplates ? templates.getUrlRule(ruleId) : null;
        urlRules.addAll(PendingChangeStatusHelper.getPendingUrlRules(pendingChangesStatus));
        return deleteTemplateUrlRule(appName, ruleId, current, urlRules, ApplicationStatusMode.OFFLINE);
    }


    private OperationResult deleteTemplateUrlRule(String serviceName, String ruleId, IfExpression current, Collection<IfExpression> rules, ApplicationStatusMode mode) {
        if (current != null) {
            validate(serviceName, current, rules);
            return pendingUrlRuleWriteService.save(serviceName, ruleId, null, current, mode);
        } else {
            return pendingUrlRuleWriteService.cancel(serviceName, ruleId, mode);
        }
    }


    private void validate(String serviceName, IfExpression templateToDelete, Collection<IfExpression> rules) {
        try {
            ModelValidationFacade.validateRuleTemplateDeletion(templateToDelete, rules, EntityType.TEMPLATE_URL_RULE, ValidationState.ActionType.DELETE);
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to delete template '%s' for %s application due to validation error(s). %s",  templateToDelete.getId(), serviceName, ex.getMessage());
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        }
    }
}
