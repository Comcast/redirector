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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.model.summary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
public class RowSummary {

    @XmlAttribute(name = "release")
    private String release;

    @XmlAttribute(name = "distribution")
    private String distribution;

    @XmlAttribute(name = "node")
    private Integer node;

    @XmlElement(name = "namespacedListNames", type = String.class)
    private Set<String> namespacedListNames;

    public String getRelease() {
        return release;
    }


    public void setRelease(String release) {
        this.release = release;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public Integer getNode() {
        return node;
    }

    public void setNode(Integer node) {
        this.node = node == null ? Integer.valueOf(0) : node;
    }

    public Set<String> getNamespacedListNames() {
        return namespacedListNames;
    }

    public void setNamespacedListNames(Set<String> namespacedListNames) {
        this.namespacedListNames = namespacedListNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RowSummary that = (RowSummary) o;
        return Objects.equals(release, that.release) &&
                Objects.equals(distribution, that.distribution) &&
                Objects.equals(node, that.node) &&
                Objects.equals(namespacedListNames, that.namespacedListNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(release, distribution, node, namespacedListNames);
    }
}
