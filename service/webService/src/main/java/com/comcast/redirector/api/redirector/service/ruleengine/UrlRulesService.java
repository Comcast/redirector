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

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UrlRulesService extends BaseUrlRulesService {

    @Autowired
    public UrlRulesService(
            @Qualifier("urlRulesDAO") IListServiceDAO<IfExpression> urlRulesDAO,
            @Qualifier("pendingUrlRuleWriteService") IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService,
            @Qualifier("changesStatusService") IChangesStatusService pendingChangesService) {
        super(urlRulesDAO, pendingUrlRuleWriteService, pendingChangesService);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.URL_RULE;
    }

    @Override
    public Map<String, PendingChange> getUrlRules(PendingChangesStatus pendingChangesStatus) {
        return pendingChangesStatus != null ? pendingChangesStatus.getUrlRules() : null;
    }
}
