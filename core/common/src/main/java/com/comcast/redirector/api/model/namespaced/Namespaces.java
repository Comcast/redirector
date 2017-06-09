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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.namespaced;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "namespaces")

@XmlSeeAlso({NamespacedList.class})
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude( JsonInclude.Include.NON_EMPTY )
public class Namespaces implements Serializable {

    @XmlAttribute
    private long version = 0;

    @XmlAttribute
    private long dataNodeVersion = 0;

    @XmlElement(type = NamespacedList.class)
    private List<NamespacedList> namespace;

    @XmlElement(type = NamespacedList.class)
    private NamespacedList newNamespace;

    public NamespacedList getNewNamespace() {
        return newNamespace;
    }

    public void setNewNamespace(NamespacedList newNamespace) {
        this.newNamespace = newNamespace;
    }

    public List<NamespacedList> getNamespaces() {
        return namespace;
    }

    public void setNamespaces(List<NamespacedList> namespaces) {
        this.namespace = namespaces;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getDataNodeVersion() {
        return dataNodeVersion;
    }

    public void setDataNodeVersion(long dataNodeVersion) {
        this.dataNodeVersion = dataNodeVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Namespaces that = (Namespaces) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(dataNodeVersion, that.dataNodeVersion) &&
                Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, dataNodeVersion, namespace);
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    public NamespacedList getNamespaceByName (String name) {
        for (NamespacedList namespacedList: namespace) {
            if (namespacedList.getName().equals(name)) {
                return namespacedList;
            }
        }
        return null;
    }
}
