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

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.pending.PendingChange;

import java.util.Map;

public class PendingChangesHelper {
    public static <T extends Expressions> void putPendingChange(Map<String, PendingChange> changes, String id, T changed, T current, ActionType actionType) {
        changes.put(id, new PendingChange(id, actionType, changed, current));
    }

    static void putServerChange(Map<String, PendingChange> changes, String name, String newPath, String oldPath) {
        changes.put(name,
                new PendingChange(name, ActionType.UPDATE,
                        ServerHelper.prepareServer(name, newPath), ServerHelper.prepareServer(name, oldPath)));
    }

    public static void putDistributionChange(Map<String, PendingChange> changes, int id, float percent, String path, ActionType actionType) {
        String idString = Integer.toString(id);
        Expressions changed = null, current = null;
        switch (actionType) {
            case ADD:
            case UPDATE:
                changed = DistributionHelper.getRule(id, percent, path);
                break;
            case DELETE:
                current = DistributionHelper.getRule(id, percent, path);
                break;
        }
        changes.put(idString, new PendingChange(idString, actionType, changed, current));
    }
}
