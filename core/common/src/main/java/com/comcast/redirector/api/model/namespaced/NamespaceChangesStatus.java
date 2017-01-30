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
 */
package com.comcast.redirector.api.model.namespaced;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.pending.PendingChange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement(name = "namespaceChangeStatus")
@XmlSeeAlso({PendingChange.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespaceChangesStatus {

    private Map<NamespacedList, ActionType> namespaceChanges = new LinkedHashMap<>();

    public NamespaceChangesStatus () {
    }

    public NamespaceChangesStatus (Map<NamespacedList, ActionType> namespaceChanges) {
        this.namespaceChanges = namespaceChanges;
    }

    public Map<NamespacedList, ActionType> getNamespaceChanges () {
        return namespaceChanges;
    }

    public void setNamespaceChanges (Map<NamespacedList, ActionType> namespaceChanges) {
        this.namespaceChanges = namespaceChanges;
    }

    public void clear() {
        namespaceChanges.clear();
    }

    public boolean isNamespacesChangesEmpty() {
        return namespaceChanges.isEmpty();
    }
}
