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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlTransient
@XmlSeeAlso({Value.class})
public class ContainsBase extends VisitableExpression implements Serializable, Expressions, NegationSupport {

    @XmlAttribute(name = "negation")
    protected boolean negation = false;

    @XmlAttribute(name = "type")
    protected String type = "";

    @XmlElement(name = "param", required = true)
    protected String param;


    @XmlElementWrapper(name="values")
    @XmlElements(@XmlElement(name="value", type=Value.class))
    protected List<Value> values;

    @XmlElementWrapper(name="namespacedList")
    @XmlElements(@XmlElement(name="value", type=Value.class))
    List<Value> namespacedLists;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    public List<Value> getNamespacedLists() {
        return namespacedLists;
    }

    public void setNamespacedLists(List<Value> namespacedLists) {
        this.namespacedLists = namespacedLists;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainsBase that = (ContainsBase) o;
        return Objects.equals(negation, that.negation) &&
                Objects.equals(type, that.type) &&
                Objects.equals(param, that.param) &&
                Objects.equals(values, that.values) &&
                Objects.equals(namespacedLists, that.namespacedLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(negation, type, param, values, namespacedLists);
    }

    @Override
    public boolean isNegated() {
        return negation;
    }
}
