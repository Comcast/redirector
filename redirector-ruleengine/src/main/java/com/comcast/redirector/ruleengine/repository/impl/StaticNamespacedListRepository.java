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

package com.comcast.redirector.ruleengine.repository.impl;

import com.comcast.redirector.ruleengine.IpAddressInitException;
import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class StaticNamespacedListRepository implements NamespacedListRepository {
    private static final Logger log = LoggerFactory.getLogger(StaticNamespacedListRepository.class);

    private NamespacedListsBatch namespacedListsBatch;

    private StaticNamespacedListRepository(NamespacedListsBatch namespacedListsBatch) {
        this.namespacedListsBatch = namespacedListsBatch;
    }

    public static StaticNamespacedListRepository of(NamespacedListsBatch namespacedListsBatch) {
        return new StaticNamespacedListRepository(namespacedListsBatch);
    }

    public static StaticNamespacedListRepository emptyHolder() {
        return of(new NamespacedListsBatch());
    }

    @Override
    public Set<String> getNamespacedListValues(String name) {
        Set<String> namespacedListsValues = namespacedListsBatch.getNamespacedLists().get(name);
        return namespacedListsValues == null ? new HashSet<String>() : namespacedListsValues;
    }

    @Override
    public Set<IpAddress> getIpAddressesFromNamespacedList(String name) {
        Set<String> namespacedListsValues = getNamespacedListValues(name);
        Set<IpAddress> result = new HashSet<>();
        for (String ipCandidate : namespacedListsValues) {
            try {
                result.add(new IpAddress(ipCandidate));
            } catch (IpAddressInitException e) {
                log.error("failed to parse ip address", e);
            }
        }
        return result;
    }
}
