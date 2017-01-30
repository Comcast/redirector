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

import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrentWhitelistedEntityViewService implements IEntityViewService<Whitelisted> {
    @Autowired
    private IWhiteListService whiteListService;

    @Override
    public Whitelisted getEntity(String serviceName) {
        return whiteListService.getWhitelistedStacks(serviceName);
    }

    @Override
    public Whitelisted getEntity(PendingChangesStatus pendingChangesStatus, Whitelisted currentEntity) {
        throw new UnsupportedOperationException();
    }
}
