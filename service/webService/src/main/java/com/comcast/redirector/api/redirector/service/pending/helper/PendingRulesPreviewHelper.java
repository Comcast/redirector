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

package com.comcast.redirector.api.redirector.service.pending.helper;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.pending.PendingChange;

import java.util.Collection;
import java.util.Map;

public class PendingRulesPreviewHelper {
    /**
     * This method merges (i.e. applies) current state rules and corresponding pending changes.
     * This method can be used for both FlavorRules and UrlRules
     *
     * @param currentRules
     * @param pendingRules
     * @return
     */
    public static Collection<IfExpression> mergeRules(Map<String, IfExpression> currentRules, Map<String, PendingChange> pendingRules) {
        for (Map.Entry<String, PendingChange> pendingRule : pendingRules.entrySet()) {
            String changedRuleName = pendingRule.getKey();
            PendingChange pendingChange = pendingRule.getValue();
            switch (pendingChange.getChangeType()) {
                case ADD:
                case UPDATE:
                    currentRules.put(changedRuleName, (IfExpression)pendingChange.getChangedExpression());
                    break;
                case DELETE:
                    currentRules.remove(changedRuleName);
            }
        }

        return currentRules.values();
    }
}
