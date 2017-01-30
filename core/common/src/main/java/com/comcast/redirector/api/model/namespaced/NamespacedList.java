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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.namespaced;


import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.VisitableExpression;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Collections2;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@XmlRootElement(name = "namespaced_list")

@XmlSeeAlso({Value.class, NamespacedListValueForWS.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespacedList extends VisitableExpression implements Expressions {

    @XmlAttribute
    private String valuesEncodingType;

    @XmlAttribute
    private long version = 0;

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "value", type = Value.class)
    @Deprecated
    private Set<Value> ret = new LinkedHashSet<>();

    // IMPORTANT: those should be converted to ret before writing to dataSource
    @XmlElement
    private Set<NamespacedListValueForWS> valueSet = new LinkedHashSet<>();

    private NamespacedListType type;

    private Integer valueCount;

    public NamespacedList() {}

    public NamespacedList(String name, Set<String> values) {
        this.name = name;
        this.setValues(values);
    }

    public String getValuesEncodingType() {
        return valuesEncodingType;
    }

    public void setValuesEncodingType(String valuesEncodingType) {
        this.valuesEncodingType = valuesEncodingType;
    }

    @Deprecated
    @JsonProperty(value = "ret")
    @JsonIgnore
    public Set<Value> getRet() {
        return ret;
    }

    @Deprecated
    public void setRet(Set<Value> ret) {
        this.ret = ret;
    }

    @XmlTransient
    @JsonIgnore
    public Set<String> getValues() {
        return new LinkedHashSet<>(Collections2.transform(valueSet, NamespacedListValueForWS::getValue));
    }

    @JsonIgnore
    public void setValues(Set<String> values) {
        valueSet = new LinkedHashSet<>(Collections2.transform(values, NamespacedListValueForWS::new));
    }

    public void updateVersion() {
        this.version = new Date().getTime();
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NamespacedListType getType() {
        return type;
    }

    public void setType(NamespacedListType type) {
        this.type = type;
    }

    public Set<NamespacedListValueForWS> getValueSet() {
        return valueSet;
    }

    public void setValueSet(Set<NamespacedListValueForWS> valueSet) {
        this.valueSet = valueSet;
    }

    public Integer getValueCount() {
        return valueCount;
    }

    public void setValueCount(Integer valueCount) {
        this.valueCount = valueCount;
    }

    @Override
    public String toString() {
        int size = 0;
        if (valueSet == null) {
            size = ret.size();
        } else  {
            size = valueSet.size();
        }
        final StringBuffer sb = new StringBuffer("NamespacedList{");
        sb.append(", version=").append(version);
        sb.append(", name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append(", valueCount=").append(valueCount == null ? size : valueCount);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamespacedList that = (NamespacedList) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(description, that.description) &&
                Objects.equals(ret, that.ret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, description, ret);
    }
}
