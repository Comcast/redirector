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
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FlavorRulesService extends BaseFlavorRulesService {

    @Autowired
    public FlavorRulesService(IStacksService stacksService,
                              @Qualifier("flavorRulesDAO") IListServiceDAO<IfExpression> flavorRulesDAO,
                              @Qualifier("changesStatusService") IChangesStatusService pendingChangesService,
                              @Qualifier("pendingFlavorRuleWriteService") IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService) {
        super(stacksService, flavorRulesDAO, pendingChangesService, pendingFlavorRuleWriteService);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.RULE;
    }


    @Override
    public Map<String, PendingChange> getRule(PendingChangesStatus pendingChangesStatus) {
        return pendingChangesStatus != null ? pendingChangesStatus.getPathRules() : null;
    }
}
