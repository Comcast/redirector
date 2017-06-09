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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.namespaced.Namespaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.matchers.JUnitMatchers.hasItems;

public class NamespaceServiceIntegrationTestUtils {

    public static Namespaces createNamespaces() {
        return createNamespaces(2);
    }

    public static Namespaces createNamespaces(int size) {
        Namespaces namespaces = new Namespaces();
        List<NamespacedList> namespacedLists = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            namespacedLists.add(createNamespacedList(String.valueOf(i)));
        }
        namespaces.setNamespaces(namespacedLists);
        return namespaces;
    }

    public static NamespacedList createNamespacedList(String name) {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName("namespacedListTest" + name);
        namespacedList.setDescription("Description" + name);
        namespacedList.setValueSet(new HashSet<>(Arrays.asList(
                        new NamespacedListValueForWS("value1_" + name),
                        new NamespacedListValueForWS("value2_" + name),
                        new NamespacedListValueForWS("value3_" + name)))
        );
        return namespacedList;
    }
}
