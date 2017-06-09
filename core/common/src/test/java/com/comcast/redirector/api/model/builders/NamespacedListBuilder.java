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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.builders;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NamespacedListBuilder {
    private String name;
    private Set<NamespacedListValueForWS> values;
    private String description;
    private NamespacedListType type;

    public NamespacedListBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public NamespacedListBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public NamespacedListBuilder withType(NamespacedListType type) {
        this.type = type;
        return this;
    }

    public NamespacedListBuilder withValues(String... values) {
        this.values = Stream.of(values).map(NamespacedListValueForWS::new).collect(Collectors.toCollection(HashSet::new));
        return this;
    }

    public NamespacedList build() {
        final NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(this.name);
        namespacedList.setDescription(this.description);
        namespacedList.setType(type);
        namespacedList.setValueSet(this.values);

        return namespacedList;
    }
}
