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

package com.comcast.redirector.ruleengine.repository;

import java.util.*;

public class NamespacedListsBatch {
    private long dataNodeVersion = 0;
    private Map<String, Set<String>> namespacedLists = new HashMap<>();

    public void addValues(String listName, Collection<String> values) {
        if (!namespacedLists.containsKey(listName)) {
            namespacedLists.put(listName, new HashSet<String>());
        }

        namespacedLists.get(listName).addAll(values);
    }

    public Map<String, Set<String>> getNamespacedLists() {
        return namespacedLists;
    }

    public void setNamespacedLists(Map<String, Set<String>> namespacedLists) {
        this.namespacedLists = namespacedLists;
    }

    public long getDataNodeVersion() {
        return dataNodeVersion;
    }

    public void setDataNodeVersion(long version) {
        this.dataNodeVersion = version;
    }
}
