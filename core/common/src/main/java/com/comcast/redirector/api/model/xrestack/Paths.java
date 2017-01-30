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

package com.comcast.redirector.api.model.xrestack;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "paths")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({PathItem.class})
public class Paths {

    @XmlAttribute(name = "serviceName")
    private String serviceName;

    @XmlElement(name = "stack", type = PathItem.class)
    private List<PathItem> stacks;

    @XmlElement(name = "flavor", type = PathItem.class)
    private List<PathItem> flavors;

    public Paths() {
        this("", new ArrayList<PathItem>(), new ArrayList<PathItem>());
    }

    public Paths(String serviceName) {
        this(serviceName, new ArrayList<PathItem>(), new ArrayList<PathItem>());
    }

    public Paths(String serviceName, List<PathItem> stacks, List<PathItem> flavors) {
        this.serviceName = serviceName;
        this.stacks = stacks;
        this.flavors = flavors;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<PathItem> getStacks() {
        return stacks;
    }

    public void setStacks(List<PathItem> stacks) {
        this.stacks = stacks;
    }

    public List<PathItem> getFlavors() {
        return flavors;
    }

    public void setFlavors(List<PathItem> flavors) {
        this.flavors = flavors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paths paths = (Paths) o;
        return Objects.equals(serviceName, paths.serviceName) &&
                Objects.equals(stacks, paths.stacks) &&
                Objects.equals(flavors, paths.flavors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, stacks, flavors);
    }
}
