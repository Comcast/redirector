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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.namespaced.NamespacedEntities;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name="dataSnapshot")
@XmlSeeAlso({Snapshot.class, AppNames.class, Namespaces.class, NamespacedList.class, NamespacedEntities.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class SnapshotList implements Serializable {

    @XmlElement(name="snapshots", type = Snapshot.class)
    private List<Snapshot> items = new ArrayList<>();

    @XmlElement(name="applicationsNames", required = true)
    private AppNames applicationsNames;

    @XmlElement(name="namespaces", required = true)
    private Namespaces namespaces = new Namespaces();

    @XmlElement(name="redirectorConfig", required = true)
    private RedirectorConfig config;

    @XmlAnyElement(lax=true)
    private Expressions entityToSave;

    public Collection<Snapshot> getItems() {
        return items;
    }

    public void setItems(List<Snapshot> items) {
        this.items = items;
    }

    public AppNames getApplicationsNames() {
        return applicationsNames;
    }

    public void setApplicationsNames(AppNames applicationsNames) {
        this.applicationsNames = applicationsNames;
    }

    public Namespaces getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Namespaces namespaces) {
        this.namespaces = namespaces;
    }

    public RedirectorConfig getConfig() {
        return config;
    }

    public void setConfig(RedirectorConfig config) {
        this.config = config;
    }

    public Expressions getEntityToSave() {
        return entityToSave;
    }

    public void setEntityToSave(Expressions entityToSave) {
        this.entityToSave = entityToSave;
    }
}
