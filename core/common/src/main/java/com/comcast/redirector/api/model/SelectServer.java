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


import com.comcast.redirector.api.model.distribution.Distribution;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@XmlRootElement(name = "selectServer")
@XmlType(propOrder = {"items", "distribution", "fallbackServer"})

@XmlSeeAlso({IfExpression.class, AndExpression.class, Server.class, Distribution.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class SelectServer implements java.io.Serializable {

    @XmlElement(name = "fallbackServer", required = false)
    private Server fallbackServer;

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
            @XmlElement(name = "lessThan", type = LessThan.class),
            @XmlElement(name = "greaterThan", type = GreaterThan.class),
            @XmlElement(name = "matches", type = Matches.class),
    })
    private Collection<IfExpression> items;

    @XmlElement(name = "distribution")
    private Distribution distribution;

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public Server getFallbackServer() {
        return fallbackServer;
    }

    public void setFallbackServer(Server server) {
        fallbackServer = server;
    }

    public Collection<IfExpression> getItems() {
        return items;
    }

    public void setItems(Collection<IfExpression> items) {
        this.items = items;
    }

    public void addCondition(IfExpression item) {
        if (getItems() == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public IfExpression getRule(final String ruleId) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        return (IfExpression)CollectionUtils.find(items, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((IfExpression)object).getId().equals(ruleId);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectServer that = (SelectServer) o;
        return Objects.equals(fallbackServer, that.fallbackServer) &&
                Objects.equals(items, that.items) &&
                Objects.equals(distribution, that.distribution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fallbackServer, items, distribution);
    }
}
