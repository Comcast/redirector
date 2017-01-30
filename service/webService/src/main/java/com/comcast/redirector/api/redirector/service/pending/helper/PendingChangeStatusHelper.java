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

package com.comcast.redirector.api.redirector.service.pending.helper;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.*;

public class PendingChangeStatusHelper {
    public static <T extends Expressions> ActionType getActionType(EntityType entityType, T pending, T current) {
        ActionType actionType = null;

        switch (entityType) {
            case SERVER:
            case URL_PARAMS:
                actionType = ActionType.UPDATE;
                break;
            case TEMPLATE_URL_RULE:
            case TEMPLATE_RULE:
            case RULE:
            case URL_RULE:
                if (pending != null && current != null) {
                    actionType = ActionType.UPDATE;
                } else if (pending != null) {
                    actionType = ActionType.ADD;
                } else if (current != null) {
                    actionType = ActionType.DELETE;
                }
                break;
            default:
        }

        return actionType;
    }

    public static <T extends Expressions> PendingChangesStatus updatePendingChangesStatus(PendingChangesStatus pendingChangesStatus,
                                                                                   ActionType changeType,
                                                                                   EntityType entityType,
                                                                                   String id, T pending, T current) {
        PendingChange change = new PendingChange(id, changeType, pending, current);
        switch (entityType) {
            case RULE:
                pendingChangesStatus.getPathRules().put(id, change);
                break;
            case SERVER:
                pendingChangesStatus.getServers().put(id, change);
                break;
            case URL_RULE:
                pendingChangesStatus.getUrlRules().put(id, change);
                break;
            case URL_PARAMS:
                pendingChangesStatus.getUrlParams().put(id, change);
                break;
            case TEMPLATE_RULE:
                pendingChangesStatus.getTemplatePathRules().put(id, change);
                break;
            case TEMPLATE_URL_RULE:
                pendingChangesStatus.getTemplateUrlPathRules().put(id, change);
                break;
            default:

        }

        return pendingChangesStatus;
    }

    public static PendingChange removePendingChangeByIdAndType(PendingChangesStatus pendingChangesStatus,
                                                        String id,
                                                        EntityType entityType) {
        PendingChange pendingChange = null;
        switch (entityType) {
            case RULE:
                pendingChange = pendingChangesStatus.getPathRules().remove(id);
                break;
            case SERVER:
                pendingChange = pendingChangesStatus.getServers().remove(id);
                break;
            case URL_RULE:
                pendingChange = pendingChangesStatus.getUrlRules().remove(id);
                break;
            case URL_PARAMS:
                pendingChange = pendingChangesStatus.getUrlParams().remove(id);
                break;
            case TEMPLATE_RULE:
                pendingChange = pendingChangesStatus.getTemplatePathRules().remove(id);
                break;
            case TEMPLATE_URL_RULE:
                pendingChange = pendingChangesStatus.getTemplateUrlPathRules().remove(id);
                break;
            default:
        }

        return pendingChange;
    }

    public static Collection<String> getNewRulesIds (String objectType, PendingChangesStatus pendingChangesStatus) {

        Map<String, PendingChange> pendingRules = new HashMap<>();
        switch (objectType) {
            case RedirectorConstants.PENDING_STATUS_PATH_RULES: {
                pendingRules = pendingChangesStatus.getPathRules();
                break;
            }
            case RedirectorConstants.PENDING_STATUS_URL_RULES: {
                pendingRules = pendingChangesStatus.getUrlRules();
                break;
            }
            case RedirectorConstants.PENDING_STATUS_TEMPLATE_PATH_RULES: {
                pendingRules = pendingChangesStatus.getTemplatePathRules();
                break;
            }
            case RedirectorConstants.PENDING_STATUS_TEMPLATE_URL_PATH_RULES: {
                pendingRules = pendingChangesStatus.getTemplateUrlPathRules();
                break;
            }
            default:
        }

        return Lists.newArrayList(Iterables.filter(
                Iterables.transform(pendingRules.values(), new Function<PendingChange, String>() {
                    @Override
                    public String apply(@Nullable PendingChange input) {
                        return input != null && input.getChangeType() == ActionType.ADD ? input.getId() : null;
                    }
                }),
                Predicates.notNull()
        ));
    }

    public static Collection<IfExpression> getNewPendingRulesAndTemplates(PendingChangesStatus pendingChangesStatus) {

        List<PendingChange> pendingRulesAndTemplates = new ArrayList<>();
        pendingRulesAndTemplates.addAll(pendingChangesStatus.getPathRules().values());
        pendingRulesAndTemplates.addAll(pendingChangesStatus.getUrlRules().values());
        pendingRulesAndTemplates.addAll(pendingChangesStatus.getTemplatePathRules().values());
        pendingRulesAndTemplates.addAll(pendingChangesStatus.getTemplateUrlPathRules().values());

        Collection<IfExpression> expressions = new ArrayList<>(pendingRulesAndTemplates.size());

        for(PendingChange change: pendingRulesAndTemplates) {
            switch (change.getChangeType()) {
                case ADD:
                    expressions.add((IfExpression)change.getChangedExpression());
                    break;
                case UPDATE:
                    expressions.add((IfExpression)change.getChangedExpression());
                    expressions.add((IfExpression)change.getCurrentExpression());
                    break;
                default:
            }
        }

        return expressions;
    }

    public static Collection<IfExpression> getPendingFlavorRules(PendingChangesStatus pendingChangesStatus) {

        List<PendingChange> pendingRules = new ArrayList<>();
        pendingRules.addAll(pendingChangesStatus.getPathRules().values());

        Collection<IfExpression> expressions = new ArrayList<>(pendingRules.size());

        for(PendingChange change: pendingRules) {
            switch (change.getChangeType()) {
                case ADD:
                case UPDATE:
                    expressions.add((IfExpression)change.getChangedExpression());
                    break;
                default:
            }
        }

        return expressions;
    }

    public static Collection<IfExpression> getPendingUrlRules(PendingChangesStatus pendingChangesStatus) {

        List<PendingChange> pendingRules = new ArrayList<>();
        pendingRules.addAll(pendingChangesStatus.getUrlRules().values());

        Collection<IfExpression> expressions = new ArrayList<>(pendingRules.size());

        for(PendingChange change: pendingRules) {
            switch (change.getChangeType()) {
                case ADD:
                    expressions.add((IfExpression)change.getChangedExpression());
                    break;
                case UPDATE:
                    expressions.add((IfExpression)change.getChangedExpression());
                    break;
                default:
            }
        }

        return expressions;
    }
}
