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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingRulesPreviewHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.common.util.RulesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class NextFlavorRulesEntityViewService implements IEntityViewService<SelectServer> {
    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Override
    public SelectServer getEntity(String serviceName) {
        PendingChangesStatus pendingChanges = changesStatusService.getPendingChangesStatus(serviceName);
        SelectServer rules = flavorRulesService.getAllRules(serviceName);
        return  getEntity(pendingChanges, rules);
    }

    @Override
    public SelectServer getEntity(PendingChangesStatus pendingChangesStatus, SelectServer currentEntity) {
        Collection<IfExpression> rules = PendingRulesPreviewHelper.mergeRules(
                RulesUtils.mapFromCollection(currentEntity.getItems()), pendingChangesStatus.getPathRules());
        return RulesUtils.buildSelectServer(rules, currentEntity.getDistribution());
    }

    void setFlavorRulesService(IFlavorRulesService flavorRulesService) {
        this.flavorRulesService = flavorRulesService;
    }

    void setChangesStatusService(IChangesStatusService changesStatusService) {
        this.changesStatusService = changesStatusService;
    }
}
