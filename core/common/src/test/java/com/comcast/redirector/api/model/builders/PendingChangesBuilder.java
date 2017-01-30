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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.builders;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class PendingChangesBuilder {

    private PendingChangesStatus pendingChanges = new PendingChangesStatus();

    private Map<String, PendingChange> distributionChanges = new LinkedHashMap<>();

    public PendingChangesBuilder withDistributionPendingChange(Expressions changed, Expressions current, ActionType action) {
        PendingChange currentChange = new PendingChange(Integer.toString(distributionChanges.size()), action, changed, current);
        distributionChanges.put(Integer.toString(distributionChanges.size()), currentChange);
        if (pendingChanges != null) {
            pendingChanges.setDistributions(distributionChanges);
        }
        return this;
    }

    public PendingChangesStatus build() {
        return pendingChanges;
    }

}
