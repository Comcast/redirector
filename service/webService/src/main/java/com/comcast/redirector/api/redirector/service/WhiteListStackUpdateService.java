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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.WhitelistUpdate;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
public class WhiteListStackUpdateService implements IWhiteListStackUpdateService {
    private static Logger log = LoggerFactory.getLogger(WhiteListStackUpdateService.class);

    private long TWO_MONTHS = 1000 * 60 * 60 * 24 * 30 * 2;


    @Autowired
    @Qualifier("whitelistUpdatesDAO")
    private ISimpleServiceDAO<WhitelistedStackUpdates> whitelistStackUpdateDAO;

    @Autowired
    private IWhiteListService whiteListService;

    @Autowired IStacksService stacksService;

    @Override
    public WhitelistedStackUpdates getWhitelistedStacksUpdates(String serviceName) {
        WhitelistedStackUpdates whitelistedUpdates = whitelistStackUpdateDAO.get(serviceName);

        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(serviceName);
        ServicePaths servicePaths = stacksService.getStacksForService(serviceName);

        if (whitelistedUpdates == null) {
            whitelistedUpdates = new WhitelistedStackUpdates();
        } else {//todo: remove this code after next release
            boolean migrate = true;
            Map<String, WhitelistUpdate> pathsToUpdate = new LinkedHashMap<>();
            for (String updateFlavor : whitelistedUpdates.getWhitelistedUpdates().keySet()) {
                if (updateFlavor.split("/").length == 4) {
                    migrate = false;
                } else {
                    pathsToUpdate.put(updateFlavor, whitelistedUpdates.getWhitelistedUpdates().get(updateFlavor));
                }
            }
            if (migrate) {
                for (String flavor : pathsToUpdate.keySet()) {
                    whitelistedUpdates.addUpdateItemToAllFlavors(new WhitelistUpdate(flavor, pathsToUpdate.get(flavor).getUpdated(), pathsToUpdate.get(flavor).getAction()), servicePaths, serviceName);
                }
                try {
                    whitelistStackUpdateDAO.save(whitelistedUpdates, serviceName);
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
            }
            //
            //migration code end
            //
        }

        WhitelistedStackUpdates syncedWhitelistedUpdates = new WhitelistedStackUpdates();
        //desync check (between history and actual whitelisted paths)
        for (String whitelistedPath: whitelisted.getPaths()) {
            Pair<String, WhitelistUpdate> whitelistUpdatePair = containsWhitelistedPath(whitelistedUpdates.getWhitelistedUpdates(), whitelistedPath);
            if (whitelistUpdatePair != null) {
                ActionType whitelistUpdateAction = whitelistUpdatePair.getValue().getAction();
                long updated = whitelistUpdatePair.getValue().getUpdated();
                long now = new Date().getTime();
                if (now - updated > TWO_MONTHS) {
                    continue;
                }
                Boolean isWhitelistedStackDeleted = ActionType.DELETE.equals(whitelistUpdateAction);
                if (isWhitelistedStackDeleted) {
                    log.error("Whitelisted stack " + whitelistedPath + " is marked as deleted in history," +
                            " but is still present in whitelisted paths list. Marking it as new (action=null)");
                    syncedWhitelistedUpdates.addUpdateItem(new WhitelistUpdate(whitelistedPath), whitelistUpdatePair.getKey());
                } else {
                    syncedWhitelistedUpdates.addUpdateItem(whitelistUpdatePair.getValue(), whitelistUpdatePair.getKey());
                }
            } else {
                log.error("Whitelisted stack " + whitelistedPath + " is not marked in history," +
                            " but is present in whitelisted paths list. Marking it as new (action=null)");
                syncedWhitelistedUpdates.addUpdateItem(new WhitelistUpdate(whitelistedPath), whitelistedPath);
            }
        }
        addAllDeletedUpdatesToHistory(whitelistedUpdates, syncedWhitelistedUpdates);
        for (String key : whitelistedUpdates.getWhitelistedUpdates().keySet()) {
            syncedWhitelistedUpdates.getWhitelistedUpdates().put(key, whitelistedUpdates.getWhitelistedUpdates().get(key));
        }
        return syncedWhitelistedUpdates;
    }

    public Pair<String, WhitelistUpdate> containsWhitelistedPath (Map<String, WhitelistUpdate> whitelistUpdateMap, String whitelistedPath) {
        for (String path: whitelistUpdateMap.keySet()) {
            if (path.contains(whitelistedPath)) {
                return new Pair<String, WhitelistUpdate>(path, whitelistUpdateMap.get(path));
            }
        }
        return null;
    }

    private void addAllDeletedUpdatesToHistory (WhitelistedStackUpdates original, WhitelistedStackUpdates toAdd) {
        for (String key : original.getWhitelistedUpdates().keySet()) {
            WhitelistUpdate whitelistUpdate = original.getWhitelistedUpdates().get(key);
            if (ActionType.DELETE.equals(whitelistUpdate.getAction())) {
                toAdd.addUpdateItem(whitelistUpdate, key);
            }
        }
    }

    @Override
    public void saveWhitelistedStacksUpdates(WhitelistedStackUpdates whitelistedStackUpdates, String serviceName) {
        log.info("Saving whitelistedUpdates for application {}", serviceName);
        try {
            whitelistStackUpdateDAO.save(whitelistedStackUpdates, serviceName);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    public WhitelistedStackUpdates getNewWhitelistedStatuses(WhitelistedStackUpdates current, PendingChangesStatus pendingChangesStatus, String serviceName) {
        ServicePaths servicePaths = stacksService.getStacksForService(serviceName);
        mergePendingChangesIntoPathsMap(pendingChangesStatus.getWhitelisted(), current, servicePaths, serviceName);
        return current;
    }

    private void mergePendingChangesIntoPathsMap(Map<String, PendingChange> pendingChanges, WhitelistedStackUpdates current, ServicePaths currentServicePaths, String serviceName) {
        for (Map.Entry<String, PendingChange> pendingChange : pendingChanges.entrySet()) {
            String changedPath = null;
            if (pendingChange.getValue().getChangedExpression() != null && pendingChange.getValue().getChangedExpression().toString().length() != 0) {
                changedPath = ((Value) pendingChange.getValue().getChangedExpression()).getValue();
            } else {
                changedPath = ((Value) pendingChange.getValue().getCurrentExpression()).getValue();
            }
            switch (pendingChange.getValue().getChangeType()) {
                case UPDATE:
                case ADD:
                    current.addUpdateItemToAllFlavors(new WhitelistUpdate(changedPath, new Date().getTime(), ActionType.ADD), currentServicePaths, serviceName);
                    break;
                case DELETE:
                    current.addUpdateItemToAllFlavors(new WhitelistUpdate(changedPath, new Date().getTime(), ActionType.DELETE), currentServicePaths, serviceName);
                    break;
            }
        }
    }

    private static class Pair<KEY, VALUE> {
        private KEY key;
        private VALUE value;

        public Pair(KEY key, VALUE value) {
            this.key = key;
            this.value = value;
        }

        public KEY getKey() {
            return key;
        }

        public VALUE getValue() {
            return value;
        }
    }
}
