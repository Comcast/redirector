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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NextWhitelistedEntityViewService implements IEntityViewService<Whitelisted> {
    private ApprovedWhitelistedHelper helper = new ApprovedWhitelistedHelper();

    @Autowired
    private IWhiteListService whiteListService;

    private IChangesStatusService changesStatusService;


    @Override
    public Whitelisted getEntity(String serviceName) {
        PendingChangesStatus pendingChangesStatus = changesStatusService.getPendingChangesStatus(serviceName);
        return getWhitelistedPendingPreview(serviceName, pendingChangesStatus);
    }

    @Override
    public Whitelisted getEntity(PendingChangesStatus pendingChangesStatus, Whitelisted currentEntity) {
        return helper.getWhitelisted(currentEntity, pendingChangesStatus);
    }

    private Whitelisted getWhitelistedPendingPreview(String serviceName,
                                                     PendingChangesStatus pendingChangesStatus) {
        return helper.getWhitelisted(whiteListService.getWhitelistedStacks(serviceName), pendingChangesStatus);
    }

    private static class ApprovedWhitelistedHelper {
        Whitelisted getWhitelisted(Whitelisted whitelisted, PendingChangesStatus pendingChangesStatus) {
            List<String> whitelistedPaths = whitelisted.getPaths();

            applyChangesToWhitelistedPaths(pendingChangesStatus.getWhitelisted(), whitelistedPaths);
            whitelisted.setPaths(whitelistedPaths.stream().distinct().collect(Collectors.toList()));
            return whitelisted;
        }

        private void applyChangesToWhitelistedPaths(Map<String, PendingChange> pendingChanges, List<String> paths) {
            for (Map.Entry<String, PendingChange> pendingChange : pendingChanges.entrySet()) {
                String changedPath;
                if (pendingChange.getValue().getChangedExpression() == null) {
                    changedPath = ((Value) pendingChange.getValue().getCurrentExpression()).getValue();
                } else {
                    changedPath = ((Value) pendingChange.getValue().getChangedExpression()).getValue();
                }

                switch (pendingChange.getValue().getChangeType()) {
                    case UPDATE:
                    case ADD:
                        paths.add(changedPath);
                        break;
                    case DELETE:
                        changedPath = ((Value) pendingChange.getValue().getCurrentExpression()).getValue();
                        paths.remove(changedPath);
                        break;
                }
            }
        }
    }

    void setWhiteListService(IWhiteListService whiteListService) {
        this.whiteListService = whiteListService;
    }

    public void setChangesStatusService(IChangesStatusService changesStatusService) {
        this.changesStatusService = changesStatusService;
    }
}
