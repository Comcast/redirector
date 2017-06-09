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

package com.comcast.redirector.api.redirector.service.pending;

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;

import java.util.Collection;
import java.util.Set;

public interface IChangesStatusService {
    PendingChangesStatus getPendingChangesStatus(String serviceName) ;
    PendingChangesStatus getPendingChangesStatus(String serviceName, int version) ;
    Collection<String> getNewRulesIds(String serviceName, String objectType) ;
    void savePendingChangesStatus(String serviceName, PendingChangesStatus pendingChangesStatus) ;

    EntityType getPendingChangeType();
    PendingChange getPendingChangeByTypeAndId(String serviceName, String objectType, String changeId) ;

    ValidationReport validateModelBeforeApprove(String serviceName, EntityType entityType) ;
    ValidationReport validateModelBeforeApprove(String serviceName, EntityType entityType, PendingChangesStatus pendingChangesStatus, SelectServer currentRules, Distribution currentDistribution, Whitelisted currentWhitelisted, Set<StackData> stackData, Server currentDefaultServer, RedirectorConfig config);
}
