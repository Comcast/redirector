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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.RulesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CurrentFlavorRulesEntityViewService implements IEntityViewService<SelectServer> {
    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Override
    public SelectServer getEntity(String serviceName) {
        Collection<IfExpression> rules = flavorRulesService.getRules(serviceName);
        Distribution distribution = currentDistributionEntityViewService.getEntity(serviceName);
        Server defaultServer = serverService.getServer(serviceName);
        if (defaultServer != null && distribution != null) {
            distribution.setDefaultServer(defaultServer);
        }
        return RulesUtils.buildSelectServer(rules, distribution);
    }

    @Override
    public SelectServer getEntity(PendingChangesStatus pendingChangesStatus, SelectServer currentEntity) {
        throw new UnsupportedOperationException();
    }
}
