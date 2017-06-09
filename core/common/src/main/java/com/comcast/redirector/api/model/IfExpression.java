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

import com.comcast.redirector.api.model.url.rule.UrlRule;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "if")
@XmlType(propOrder = {"items", "ret"})
@XmlSeeAlso({IfExpression.class, AndExpression.class, OrExpression.class, XORExpression.class, Equals.class, NotEqual.class, GreaterOrEqualExpression.class, LessOrEqualExpression.class, Contains.class, Random.class, Server.class, ServerGroup.class, UrlRule.class, Percent.class, InIpRange.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class IfExpression extends VisitableExpression implements java.io.Serializable, HasChildren {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "templateName")
    private String templateDependencyName;

    @XmlElementWrapper(name = "return")
    @XmlElements({
            @XmlElement(name = "server", type = Server.class),
            @XmlElement(name = "serverGroup", type = ServerGroup.class),
            @XmlElement(name = "partner", type = Value.class),
            @XmlElement(name = "urlRule", type = UrlRule.class)
    })
    private List<Expressions> ret;

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

    public List<Expressions> getReturn() {
        return ret;
    }

    public void setReturn(Expressions server) {
        if (ret == null) {
            ret = new ArrayList<Expressions>();
        }
        ret.add(server);
    }

    public List<Expressions> getItems() {
        return items;
    }

    public void setItems(List<Expressions> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateDependencyName() {
        return templateDependencyName;
    }

    public void setTemplateDependencyName(String templateDependencyName) {
        this.templateDependencyName = templateDependencyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfExpression that = (IfExpression) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(templateDependencyName, that.templateDependencyName) &&
                Objects.equals(ret, that.ret) &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, templateDependencyName, ret, items);
    }
}
