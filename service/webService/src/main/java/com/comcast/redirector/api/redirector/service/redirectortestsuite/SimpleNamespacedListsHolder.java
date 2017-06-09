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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;

import java.util.HashSet;
import java.util.Set;

import static com.comcast.redirector.core.modelupdate.holder.NamespacedListsHolder.NamespacedListToIpAddressListConverter;

public class SimpleNamespacedListsHolder implements NamespacedListRepository {

    private NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
    private NamespacedListToIpAddressListConverter toIpAddressListConverter =
                new NamespacedListToIpAddressListConverter(this::getNamespacedListValues);

    public SimpleNamespacedListsHolder() {
    }

    public SimpleNamespacedListsHolder(NamespacedListsBatch namespacedListsBatch) {
        this.namespacedListsBatch = namespacedListsBatch;
    }

    @Override
    public Set<String> getNamespacedListValues(String namespacedListName) {
        Set<String> namespacedListsValues = namespacedListsBatch.getNamespacedLists().get(namespacedListName);
        return namespacedListsValues == null ? new HashSet<>() : namespacedListsValues;
    }

    @Override
    public Set<IpAddress> getIpAddressesFromNamespacedList(String name) {
        return toIpAddressListConverter.convert(name);
    }
}
