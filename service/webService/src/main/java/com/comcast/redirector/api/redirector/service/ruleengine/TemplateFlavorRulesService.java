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
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
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
public class TemplateFlavorRulesService extends BaseFlavorRulesService {

    private IListServiceDAO<IfExpression> rulesDAOEntity;

    @Autowired
    public TemplateFlavorRulesService(IStacksService stacksService,
                                      @Qualifier("templateFlavorRulesDAO") IListServiceDAO<IfExpression> flavorRulesDAO,
                                      @Qualifier("changesStatusService") IChangesStatusService pendingChangesService,
                                      @Qualifier("pendingTemplateFlavorRuleWriteService") IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService,
                                      @Qualifier("flavorRulesDAO") IListServiceDAO<IfExpression> rulesDAOEntity) {
        super(stacksService, flavorRulesDAO, pendingChangesService, pendingFlavorRuleWriteService);
        this.rulesDAOEntity = rulesDAOEntity;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TEMPLATE_RULE;
    }

    @Override
    public OperationResult deleteTemplateRule(String appName, String ruleId) {
        final OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        final PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();
        SelectServer templates = currentContext.getTemplatePathRules();
        Collection<IfExpression> flavorRules = currentContext.getFlavorRules().getItems();
        boolean containsTemplates = (templates != null && CollectionUtils.isNotEmpty(templates.getItems()));
        IfExpression current =  containsTemplates ? templates.getRule(ruleId) : null;
        flavorRules.addAll(PendingChangeStatusHelper.getPendingFlavorRules(pendingChangesStatus));
        return deleteRule(appName, ruleId, current, flavorRules, ApplicationStatusMode.OFFLINE);
    }

    @Override
    public Map<String, PendingChange> getRule(PendingChangesStatus pendingChangesStatus) {
        return pendingChangesStatus != null ? pendingChangesStatus.getTemplatePathRules() : null;
    }

    @Override
    public void deleteRule(String serviceName, String ruleId) {
        IfExpression expression = getRule(serviceName, ruleId);
        PendingChangesStatus pendingChangesStatus = pendingChangesService.getPendingChangesStatus(serviceName);
        List<IfExpression> rules = rulesDAOEntity.getAll(serviceName);
        rules.addAll(PendingChangeStatusHelper.getPendingFlavorRules(pendingChangesStatus));
        deleteRule(serviceName, ruleId, expression, rules, ApplicationStatusMode.ONLINE);
    }

    private OperationResult deleteRule(String serviceName, String ruleId, IfExpression current, Collection<IfExpression> rules, ApplicationStatusMode mode) {
        if (current != null) {
            validate(serviceName, current, rules);
            return pendingFlavorRuleWriteService.save(serviceName, ruleId, null/* pending changes*/, current, mode);
        } else  {
            return pendingFlavorRuleWriteService.cancel(serviceName, ruleId, mode);
        }
    }

    private void validate(String serviceName, IfExpression templateToDelete, Collection<IfExpression> rules) {
        try {
            ModelValidationFacade.validateRuleTemplateDeletion(templateToDelete, rules, EntityType.TEMPLATE_RULE, ValidationState.ActionType.DELETE);
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to delete template '%s' for %s application due to validation error(s). %s",  templateToDelete.getId(), serviceName, ex.getMessage());
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        }
    }
}
