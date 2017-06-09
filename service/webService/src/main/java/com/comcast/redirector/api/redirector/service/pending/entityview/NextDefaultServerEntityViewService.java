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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NextDefaultServerEntityViewService implements IEntityViewService<Server> {

    @Autowired
    private IServerService serverService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Override
    public Server getEntity(String serviceName) {
        Server server = serverService.getServer(serviceName);
        PendingChangesStatus pendingChangesStatus = changesStatusService.getPendingChangesStatus(serviceName);
        return getEntity(pendingChangesStatus, server);
    }

    @Override
    public Server getEntity(PendingChangesStatus pendingChangesStatus, Server currentEntity) {
        Server server = currentEntity;
        PendingChange pendingChange = pendingChangesStatus.getServers().get(RedirectorConstants.DEFAULT_SERVER_NAME);
        if (pendingChange != null) {
            server = (Server) pendingChange.getChangedExpression();
        }
        return server;
    }
}
