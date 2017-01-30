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
package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.namespaced.NamespaceChangesStatus;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;

import java.io.Serializable;
import java.util.Objects;

public class OfflineChangesStatus implements Serializable {

    private PendingChangesStatus pendingChangesStatus;
    private NamespaceChangesStatus namespaceChangesStatus;

    public OfflineChangesStatus () {

    }

    public OfflineChangesStatus (PendingChangesStatus pendingChangesStatus, NamespaceChangesStatus namespaceChangesStatus) {
        this.pendingChangesStatus = pendingChangesStatus;
        this.namespaceChangesStatus = namespaceChangesStatus;
    }

    public PendingChangesStatus getPendingChangesStatus () {
        return pendingChangesStatus;
    }

    public void setPendingChangesStatus (PendingChangesStatus pendingChangesStatus) {
        this.pendingChangesStatus = pendingChangesStatus;
    }

    public NamespaceChangesStatus getNamespaceChangesStatus () {
        return namespaceChangesStatus;
    }

    public void setNamespaceChangesStatus (NamespaceChangesStatus namespaceChangesStatus) {
        this.namespaceChangesStatus = namespaceChangesStatus;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfflineChangesStatus)) return false;
        OfflineChangesStatus that = (OfflineChangesStatus) o;
        return Objects.equals(getPendingChangesStatus(), that.getPendingChangesStatus()) &&
                Objects.equals(getNamespaceChangesStatus(), that.getNamespaceChangesStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPendingChangesStatus(), getNamespaceChangesStatus());
    }

}
