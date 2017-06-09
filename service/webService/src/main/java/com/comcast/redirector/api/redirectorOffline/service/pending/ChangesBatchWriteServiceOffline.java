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

package com.comcast.redirector.api.redirectorOffline.service.pending;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.ChangesBatchWriteService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangesBatchWriteServiceOffline extends ChangesBatchWriteService {

    @Autowired
    IStacksService stacksService;

    @Override
    public PendingChangesBatchOperationResult approve(String appName, ApplicationStatusMode mode) {
        OperationContextHolder.OperationContext currentContext = OperationContextHolder.getCurrentContext();
        WhitelistedStackUpdates whitelistedStackUpdates = currentContext.getWhitelistedStackUpdates();
        ServicePaths servicePaths = currentContext.getServicePaths();
        PendingChangesStatus pendingChangesStatus = currentContext.getPendingChangesStatus();

        PendingChangesBatchOperationResult batchResult = super.approve(appName, ApplicationStatusMode.OFFLINE);
        if (CollectionUtils.isNotEmpty(batchResult.getWhitelist().getEntitiesToSave())) {
            Whitelisted approvedWhitelist = (Whitelisted) batchResult.getWhitelist().getEntitiesToSave().get(0);
            ServicePaths updatedServicePaths = stacksService.updateStacksForService(appName, servicePaths, approvedWhitelist);
            batchResult.addEntityToUpdate(updatedServicePaths);
            batchResult.addEntityToUpdate(getWhiteListStackUpdateService().getNewWhitelistedStatuses(whitelistedStackUpdates, pendingChangesStatus, appName));
        }
        return batchResult;
    }
}
