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

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "or")
@XmlSeeAlso({Matches.class, Equals.class, GreaterOrEqualExpression.class, LessOrEqualExpression.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrExpression extends VisitableExpression implements Serializable, HasChildren {

    @XmlElements({
            @XmlElement(name = "if", type = IfExpression.class),
            @XmlElement(name = "and", type = AndExpression.class),
            @XmlElement(name = "or", type = OrExpression.class),
            @XmlElement(name = "xor", type = XORExpression.class),
            @XmlElement(name = "equals", type = Equals.class),
            @XmlElement(name = "notEqual", type = NotEqual.class),
            @XmlElement(name = "greaterOrEqual", type = GreaterOrEqualExpression.class),
            @XmlElement(name = "lessOrEqual", type = LessOrEqualExpression.class),
            @XmlElement(name = "contains", type = Contains.class),
            @XmlElement(name = "random", type = Random.class),
            @XmlElement(name = "percent", type = Percent.class),
            @XmlElement(name = "inIpRange", type = InIpRange.class),
            @XmlElement(name = "isEmpty", type = IsEmpty.class),
            @XmlElement(name = "lessThan", type = LessThan.class),
            @XmlElement(name = "greaterThan", type = GreaterThan.class),
            @XmlElement(name = "matches", type = Matches.class),
    })

    List<Expressions> items;

    public List<Expressions> getItems() {
        return items;
    }

    public void setItems(List<Expressions> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrExpression that = (OrExpression) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
