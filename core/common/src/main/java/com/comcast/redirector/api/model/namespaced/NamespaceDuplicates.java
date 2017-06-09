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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model.namespaced;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "namespaceDuplicates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespaceDuplicates {

    private Map<String, String> namespaceDuplicatesMap = new HashMap<String, String>();

    /**
     * When we cannot read some NS lists (e.g. due to lack of permissions), we cannot show them and their values in the list of duplicates
     * So we make this field true instead, indicating that conflict resolving is not possible.
     */
    private Boolean containsNamespacedListsWithoutReadRights = false;

    public NamespaceDuplicates() {
    }

    public NamespaceDuplicates(Map<String, String> namespaceDuplicates) {
            this.namespaceDuplicatesMap = namespaceDuplicates;
    }

    public Map<String, String> getNamespaceDuplicatesMap() {
        return namespaceDuplicatesMap;
    }

    public void setNamespaceDuplicatesMap(Map<String, String> namespaceDuplicatesMap) {
        this.namespaceDuplicatesMap = namespaceDuplicatesMap;
    }

    public void put(String key, String value) {
        namespaceDuplicatesMap.put(key, value);
    }

    public String get(String key) {
        return namespaceDuplicatesMap.get(key);
    }

    public void clear() {
        namespaceDuplicatesMap.clear();
    }

    public boolean isEmpty() {
        return namespaceDuplicatesMap.isEmpty();
    }

    public Boolean getContainsNamespacedListsWithoutReadRights() {
        return containsNamespacedListsWithoutReadRights;
    }

    public void setContainsNamespacedListsWithoutReadRights(Boolean containsNamespacedListsWithoutReadRights) {
        this.containsNamespacedListsWithoutReadRights = containsNamespacedListsWithoutReadRights;
    }
}
