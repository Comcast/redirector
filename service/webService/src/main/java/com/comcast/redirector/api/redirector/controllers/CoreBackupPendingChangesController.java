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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.api.redirector.service.pending.IChangesBatchWriteService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.IPendingSingletonEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

@Component
@Path(RedirectorConstants.CORE_BACKUP_PENDING_CONTROLLER_PATH)
public class CoreBackupPendingChangesController extends PendingChangesController {

    @Autowired
    @Qualifier("coreBackupChangesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    @Qualifier("coreBackupNextDistributionEntityViewService")
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Autowired
    @Qualifier("coreBackupNextWhitelistedEntityViewService")
    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Autowired
    @Qualifier("coreBackupPendingFlavorRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService;

    @Autowired
    @Qualifier("coreBackupPendingTemplateFlavorRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingTemplateFlavorRuleWriteService;

    @Autowired
    @Qualifier("coreBackupPendingUrlRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService;

    @Autowired
    @Qualifier("coreBackupPendingTemplateUrlRuleWriteService")
    private IPendingEntityWriteService<IfExpression> pendingTemplateUrlRuleWriteService;

    @Autowired
    @Qualifier("coreBackupPendingServerWriteService")
    private IPendingEntityWriteService<Server> pendingServerWriteService;

    @Autowired
    @Qualifier("coreBackupPendingUrlParamsWriteService")
    private IPendingEntityWriteService<UrlRule> pendingUrlParamsWriteService;

    @Autowired
    @Qualifier("coreBackupPendingDistributionWriteService")
    private IPendingSingletonEntityWriteService<Distribution> pendingDistributionWriteService;

    @Autowired
    @Qualifier("coreBackupPendingWhitelistedWriteService")
    private IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService;

    @Autowired
    @Qualifier("coreBackupChangesBatchWriteService")
    private IChangesBatchWriteService pendingChangesBatchWriteService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Override
    public IChangesStatusService getPendingChangesService() {
        return pendingChangesService;
    }

    @Override
    public IEntityViewService<Distribution> getNextDistributionEntityViewService() {
        return nextDistributionEntityViewService;
    }

    @Override
    public IEntityViewService<Whitelisted> getNextWhitelistedEntityViewService() {
        return nextWhitelistedEntityViewService;
    }

    @Override
    public IPendingEntityWriteService<IfExpression> getPendingFlavorRuleWriteService() {
        return pendingFlavorRuleWriteService;
    }

    @Override
    public IPendingEntityWriteService<IfExpression> getPendingTemplateFlavorRuleWriteService() {
        return pendingTemplateFlavorRuleWriteService;
    }

    @Override
    public IPendingEntityWriteService<IfExpression> getPendingUrlRuleWriteService() {
        return pendingUrlRuleWriteService;
    }

    @Override
    public IPendingEntityWriteService<IfExpression> getPendingTemplateUrlRuleWriteService() {
        return pendingTemplateUrlRuleWriteService;
    }

    @Override
    public IPendingEntityWriteService<Server> getPendingServerWriteService() {
        return pendingServerWriteService;
    }

    @Override
    public IPendingEntityWriteService<UrlRule> getPendingUrlParamsWriteService() {
        return pendingUrlParamsWriteService;
    }

    @Override
    public IPendingSingletonEntityWriteService<Distribution> getPendingDistributionWriteService() {
        return pendingDistributionWriteService;
    }

    @Override
    public IPendingSingletonEntityWriteService<Whitelisted> getPendingWhitelistedWriteService() {
        return pendingWhitelistedWriteService;
    }

    @Override
    public IChangesBatchWriteService getPendingChangesBatchWriteService() {
        return pendingChangesBatchWriteService;
    }
}
