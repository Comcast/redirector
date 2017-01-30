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

package com.comcast.redirector.api.model.distribution;


import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.VisitableExpression;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "rule")
@XmlType(propOrder = {"id", "percent", "server"})

@XmlSeeAlso({Server.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule extends VisitableExpression implements java.io.Serializable, Expressions {

    @XmlElement(name = "id")
    private int id;

    @XmlElement(name = "percent")
    private float percent;

    @XmlElement(name = "server")
    private Server server;

    public Rule() {
    }

    public Rule(int id, float percent, Server server) {
        this.id = id;
        this.percent = percent;
        this.server = server;
    }

    public static Rule newInstance(Rule rule) {

        if (rule == null) {
            return null;
        }

        Rule ruleCopy = new Rule();

        ruleCopy.setId(rule.getId());
        ruleCopy.setPercent(rule.getPercent());
        ruleCopy.setServer(Server.newInstance(rule.getServer()));

        return ruleCopy;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        if (id != rule.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
